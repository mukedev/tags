package cn.muke.model.utils

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.client.{ClusterConnection, ColumnFamilyDescriptorBuilder, ConnectionFactory, HRegionLocator, RegionLocator, Table, TableDescriptor, TableDescriptorBuilder}
import org.apache.hadoop.hbase.{HBaseConfiguration, KeyValue, TableName}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2
import org.apache.hadoop.hbase.tool.LoadIncrementalHFiles
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

/**
 *
 *  1. 从hive中读取表数据
 *     2. 创建HBase和hadoop的配置对象
 *     3. 将数据以HFile的形式写入hdfs中
 *     4. 通过HBase client Api 将数据bulkLoad到HBase对应的表中
 *
 * @author zhangyu
 *
 */
object Hive2HBase {

  val defaultHBaseCF = "default"
  val hFilePath = "/user/admin/data_extra/hfile_cache"
  val hbaseDefaultNamespace = "default"

  val spark = SparkSession.builder()
    .appName(s"${this.getClass.getSimpleName}")
    .enableHiveSupport() // 开启hive支持
    .getOrCreate()

  /**
   *
   * @param args db.table 1.数据库名，2.表名，3·表中主键列名
   */
  def main(args: Array[String]): Unit = {
    // 校验参数 执行时需传递三个参数
    if (args.length < 3) {
      return
    }
    val sourceDbName = args(0)
    val sourceTableName = args(1)
    val sourceKeyFieldName = args(2)

    // 1.创建配置
    val hadoopConfig = HBaseConfiguration.create()
    // 1.1 配置HBase 落地的表名 output tableName
    hadoopConfig.set(TableOutputFormat.OUTPUT_TABLE, sourceTableName)
    // 1.2 配置HFile写入时的表名
    hadoopConfig.set("hbase.mapreduce.hfileoutputformat.table.name", sourceTableName)

    // 1.3 job配置，任务执行的信息，key的类型
    val job = Job.getInstance(hadoopConfig)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    // 1.4 job配置: 配置value的类型
    job.setMapOutputKeyClass(classOf[KeyValue])


  }

  /**
   * 通过spark读取数据吸入hFile
   * 来源：Hive表
   * 落地：HDFS
   */
  def hive2HFile(db: String, table: String, keyField: String, config: Configuration): Unit = {
    // 1. 加载数据
    val source: Dataset[Row] = spark.read.table(s"${db}.${table}")

    // 2. 处理数据 处理成（ImmutableBytesWritable, KeyValue）
    val transfer: RDD[(ImmutableBytesWritable, KeyValue)] = source.rdd
      .filter(filed => filed.getAs(keyField) != null)
      // 使用flatMap 是因为读取的是一行，但是一个hbase数据只是分布在一行的一个单元格
      .flatMap({
        row => {
          // hbase中所有的数据类型都是Bytes
          // 获取RowKey的值
          val rowKeyBytes = Bytes.toBytes(s"${row.getAs(keyField)}")

          // 把row拆开，转换成cells形式，多个单元格，对每一个单元格处理,转换kv类型返回
          val hFileKV: Seq[(ImmutableBytesWritable, KeyValue)] = row.schema
            .filter(field => field.name != null)
            .sortBy(field => field.name)
            .map({
              field => {
                // 获取row的每一个单元格的field name 和 value
                val fieldBytes = Bytes.toBytes(s"${field.name}")
                val valueBytes = Bytes.toBytes(s"${row.getAs(field.name)}")

                // 生成KeyValue对象返回
                val kv = new KeyValue(rowKeyBytes, defaultHBaseCF.getBytes(), fieldBytes, valueBytes)

                (new ImmutableBytesWritable(rowKeyBytes), kv)
              }
            })
          hFileKV
        }
      })

    // 3. 数据落地
    transfer.saveAsNewAPIHadoopFile(
      hFilePath,
      Class[ImmutableBytesWritable],
      Class[KeyValue],
      Class[HFileOutputFormat2],
      config
    )

  }

  /**
   * 通过hbase的Api 将hFile数据加载到hbase中
   */
  def hFile2HBase(job: Job, table: String): Unit = {
    // 1. 配置 connection admin
    val connection = ConnectionFactory.createConnection(job.getConfiguration)
    val admin = connection.getAdmin

    // 2. 判断表是否存在
    val tableName = TableName.valueOf(Bytes.toBytes(hbaseDefaultNamespace), Bytes.toBytes(table))
    if (!admin.tableExists(tableName)) {
      admin.createTable(
        TableDescriptorBuilder.newBuilder(tableName)
          .setColumnFamily(ColumnFamilyDescriptorBuilder
            .newBuilder(Bytes.toBytes(defaultHBaseCF))
            .build()
          ).build()
      )
    }

    // 3.调用API进行bulkload
    val realTable = connection.getTable(tableName)
    val loader = new LoadIncrementalHFiles(job.getConfiguration)
    val regionLocator = new HRegionLocator(tableName, connection.asInstanceOf[ClusterConnection])
    loader.doBulkLoad(new Path(hFilePath),admin,realTable,regionLocator)
  }
}
