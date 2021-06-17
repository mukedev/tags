package cn.muke.model.mtag

import cn.muke.model._
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import java.util.Properties
import scala.collection.mutable

/**
 * 性别标签
 *
 * @author zhangyu
 *
 */
object GenderModel {

  val spark = SparkSession.builder()
    .appName(s"${this.getClass.getSimpleName}")
    .master("local[6]")
    .getOrCreate()

  val config = ConfigFactory.load()

  val TAG_NAME = "性别"
  val HBASE_NAMESPACE = "default"
  val HBASE_ROWKEY_FIELD = "id"
  val HBASE_COLUMN_DEFAULT_TYPE = "string"

  def main(args: Array[String]): Unit = {
    // 1.读取四级标签和五级标签
    val (fourTag, fiveTags) = readBasicTag()

    // 2.通过四级标签读取元素数据
    // 3.处理元数据，处理成结构化的方式
    val metaData = readMetaData(fourTag.id)

    // 4.使用元数据连接源表，拿到源表数据
    val source = createSource(metaData)
    source.show()

    // 5.匹配计算标签数据
    val result = process(source, fiveTags, metaData.toHBaseMeta().commonMeta.outFields)
    result.show()

    // 6.把标签信息放入用户画像表
    saveUserProfile(result, metaData.toHBaseMeta().commonMeta)

  }

  def readBasicTag(): (Tag, Array[Tag]) = {
    // 1.1 创建配置文件 不指定配置文件默认加载application.conf文件
    val url = config.getString("jdbc.mysql.basic_tag.url")
    val table = config.getString("jdbc.mysql.basic_tag.table")

    // 1.2 创建sparkSession读取四级标签
    val source: DataFrame = spark.read.jdbc(url, table, new Properties())

    // 通过name筛选四级标签
    import spark.implicits._
    val fourTag = source.where('name === TAG_NAME) // 相当于 where = '性别'
      .as[Tag]
      .collect()
      .head

    // 1.3 读取五级标签，使用四级标签的ID，作为五级标签的pid，去查询五级标签
    val fiveTag = source.where('pid === fourTag.id)
      .as[Tag]
      .collect()

    (fourTag, fiveTag)
  }

  /**
   * 读取并处理元数据
   */
  def readMetaData(tagId: String): MetaData = {
    // 1.获取数据配置
    val url = config.getString("jdbc.mysql.meta_data.url")
    val table = config.getString("jdbc.mysql.meta_data.table")
    val matchColumn = config.getString("jdbc.mysql.meta_data.match_column")

    import org.apache.spark.sql.functions._
    import spark.implicits._
    // 2.读取元数据
    val metaData: MetaData = spark.read.jdbc(url, table, new Properties())
      .where(col(matchColumn) === tagId)
      .as[MetaData]
      .collect()
      .head

    metaData
  }

  def createSource(metaData: MetaData): DataFrame = {
    // 判断是否是HBase
    if (metaData.isHBase()) {
      val hbaseMeta = metaData.toHBaseMeta()

      // 创建catalog对象
      // 处理catalog对象
      val columns = mutable.HashMap.empty[String, HBaseColumn]

      // 添加rowkey列
      columns += HBASE_ROWKEY_FIELD -> HBaseColumn("rowkey", HBASE_ROWKEY_FIELD, HBASE_COLUMN_DEFAULT_TYPE)
      for (field <- hbaseMeta.commonMeta.inFields) {
        columns += (field -> HBaseColumn(hbaseMeta.columnFamily, field, HBASE_COLUMN_DEFAULT_TYPE))
      }
      val table = HBaseTable(HBASE_NAMESPACE, hbaseMeta.tableName)
      val catalog = HBaseCatalog(table, HBASE_ROWKEY_FIELD, columns.toMap)

      // 把catalog对象转为json字符串形式
      import org.json4s._
      import org.json4s.jackson.Serialization
      import org.json4s.jackson.Serialization.write
      implicit val formats = Serialization.formats(NoTypeHints)
      val catalogJSON = write(catalog)

      // 根据catalog读取hbase数据库，得到dataFrame
      return spark.read
        .option(HBaseTableCatalog.tableCatalog, catalogJSON)
        .format("org.apache.spark.sql.execution.datasources.hbase")
        .load()
    }

    // 判断是否是Mysql
    if (metaData.isRDBMS()) {

    }

    // 判断是否是Hdfs
    if (metaData.isHdfs()) {

    }

    null
  }

  /**
   * 将源数据与五级标签匹配，并返回五级标签的ID
   *
   * @param source
   * @param fiveTags
   * @param outField
   * @return
   */
  def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {

    import org.apache.spark.sql.functions._
    import spark.implicits._

    // 1. 拼接匹配规则，五级标签的rule就是匹配用的值
    var conditions: Column = null

    for (tag <- fiveTags) {
      conditions = if (conditions == null)
        when('gender === tag.rule, tag.id)
      else
        conditions.when('gender === tag.rule, tag.id)
    }
    conditions = conditions.as(outField.head)
    // 2. 执行规则过滤数据 select id, case when gender == rule then tag_id as tag_gender from ...
    source.select('id, conditions)
  }

  /**
   * 存储画像数据
   */
  def saveUserProfile(result: DataFrame, commonMeta: CommonMeta): Unit = {
    // 1.编写catalog
    val table = HBaseTable(HBASE_NAMESPACE, "tbl_tag_gender")
    val columns = mutable.HashMap.empty[String, HBaseColumn]
    columns += HBASE_ROWKEY_FIELD -> HBaseColumn("rowkey", HBASE_ROWKEY_FIELD, HBASE_COLUMN_DEFAULT_TYPE)
    columns += commonMeta.outFields.head -> HBaseColumn("default", commonMeta.outFields.head, HBASE_COLUMN_DEFAULT_TYPE)
    val catalog = HBaseCatalog(table, HBASE_ROWKEY_FIELD, columns.toMap)

    // 2.catalog转成Json格式
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.write

    implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)
    val catalogJSON = write(catalog)

    // 3.保存数据到hbase
    result.write
      .option(HBaseTableCatalog.tableCatalog, catalogJSON)
      .option(HBaseTableCatalog.newTable, 5)
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .save()
  }
}
