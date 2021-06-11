package cn.muke.model.utils

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog

/**
 *
 * @author zhangyu
 *
 */
object ShcTest {

  def main(args: Array[String]): Unit = {
    //1.读取HBase表
    // 配置SHC配置，json格式
    // 注意点: 指定了 RowKey 的话, 这个 RowKey 一定要出现在 DataFrame 中, 所以 Columns 中, 一定要配置 RowKey 列
    // columns 指定了要出现在 DataFrame 中的所有列
    // columns 是一个 JSON 对象, 属性名是出现在 DataFrame 中名字, 属性值就是 HBase 中的列
    val catalog =
    s"""
       |{
       |  "table":{"namespace":"default","name":"tb_test"},
       |  "rowkey":"id",
       |  "columns":{
       |    "id":{"cf":"rowkey","col":"id","type":"string"},
       |    "userName":{"cf":"default","col":"username","type":"string"}
       |  }
       |}
       |""".stripMargin
    //2.使用Spark连接HBase
    val spark = SparkSession.builder()
      .appName("shc test")
      .master("local[4]")
      .getOrCreate()

    val source = spark.read
      .option(HBaseTableCatalog.tableCatalog, catalog)
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .load()

    source.show()

    //3.
    val catalogWrite =
      s"""
         |{
         |  "table":{"namespace":"default","name":"tb_test_write"},
         |  "rowkey":"id",
         |  "columns":{
         |    "id":{"cf":"rowkey","col":"id","type":"string"},
         |    "userName":{"cf":"default","col":"username","type":"string"}
         |  }
         |}
         |""".stripMargin

    source.write
      .option(HBaseTableCatalog.tableCatalog, catalogWrite)
      .option(HBaseTableCatalog.newTable, "5")
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .save()
  }

}
