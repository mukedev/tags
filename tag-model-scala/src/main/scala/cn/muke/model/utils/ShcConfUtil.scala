package cn.muke.model.utils

import scala.collection.mutable

/**
 * catalog配置工具类
 *
 * @author zhangyu
 *
 */
object ShcConfUtil {

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

  }

  /**
   * 根据catalog的json配置创建类
   * {
   * "table":{"namespace":"default","name":"tb_test"},
   * "rowkey":"id",
   * "columns":{
   * "id":{"cf":"rowkey","col":"id","type":"string"},
   * "userName":{"cf":"default","col":"username","type":"string"}
   * }
   * }
   */
  case class HBaseCatalog(table: HBaseTable, rowkey: String, columns: Map[String, HBaseColumn])

  case class HBaseTable(namespace: String, name: String)

  case class HBaseColumn(cf: String, col: String, `type`: String)


}
