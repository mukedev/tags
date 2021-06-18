package cn.muke.model.utils

import cn.muke.model.{HBaseCatalog, HBaseColumn, HBaseTable}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog

import scala.collection.mutable

/**
 * catalog配置工具类
 *
 * @author zhangyu
 *
 */
object ShcJsonTest {

  def main(args: Array[String]): Unit = {

    // 1.创建类，根据catalog的json格式创建
    // 2.处理catlog对象
    val table = HBaseTable("default", "tb_test")

    var columns = mutable.HashMap.empty[String, HBaseColumn]
    // rowkey 的column不能是普通的cf 必须是rowkey
    columns += "rowkey" -> HBaseColumn("rowkey", "id", "string")
    columns += "userName" -> HBaseColumn("default", "username", "string")

    val catalog = HBaseCatalog(table, "id", columns.toMap)

    // 3.奖catalog对象转成json
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.write

    implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

    val catalogJSON = write(catalog)

    println(catalogJSON)

    // 4.读取HBase代码
    val spark = SparkSession.builder()
      .appName(this.getClass.getSimpleName)
      .master("local[4]")
      .getOrCreate()

    val source = spark.read
      .option(HBaseTableCatalog.tableCatalog, catalogJSON)
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .load()

    source.show()
  }

}
