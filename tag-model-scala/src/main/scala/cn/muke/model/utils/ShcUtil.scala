package cn.muke.model.utils

import cn.muke.model._
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable

/**
 * SHC 工具类
 *
 * @author zhangYu
 *
 */
object ShcUtil {

  val HBASE_NAMESPACE = "default"
  val HBASE_ROWKEY_FIELD = "id"
  val HBASE_COLUMN_DEFAULT_TYPE = "string"
  val HBASE_TAG_USER_PROFILE = "tbl_user_profile"
  val HBASE_COLUMN_FAMILY = "default"

  def read(spark: SparkSession, inFields: Array[String], columnfamily: String, tableName: String): DataFrame = {
    // 创建并处理catalog对象
    val columns = mutable.HashMap.empty[String, HBaseColumn]
    columns += HBASE_ROWKEY_FIELD -> HBaseColumn("rowkey", HBASE_ROWKEY_FIELD, HBASE_COLUMN_DEFAULT_TYPE)
    for (field <- inFields) {
      columns += (field -> HBaseColumn(columnfamily, field, HBASE_COLUMN_DEFAULT_TYPE))
    }

    // 处理 HBaseTable 对象
    val table = HBaseTable(HBASE_NAMESPACE, tableName)

    // 拼接 catalog 对象
    val catalog = HBaseCatalog(table, HBASE_ROWKEY_FIELD, columns.toMap)

    // 把catalog对象转为json字符串形式
    val catalogJSON: String = objectToJson(catalog)

    // 根据catalog读取hbase数据库，得到dataFrame
    spark.read
      .option(HBaseTableCatalog.tableCatalog, catalogJSON)
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .load()
  }


  def writeToHBase(result: DataFrame, outFields: Array[String], regionCount: String): Unit = {
    // 1.编写catalog
    val columns = mutable.HashMap.empty[String, HBaseColumn]
    columns += HBASE_ROWKEY_FIELD -> HBaseColumn("rowkey", HBASE_ROWKEY_FIELD, HBASE_COLUMN_DEFAULT_TYPE)

    for (field <- outFields) {
      columns += field -> HBaseColumn(HBASE_COLUMN_FAMILY, field, HBASE_COLUMN_DEFAULT_TYPE)
    }

    // 1.1 处理 HBaseCatalog 对象
    val table = HBaseTable(HBASE_NAMESPACE, HBASE_TAG_USER_PROFILE)
    val catalog = HBaseCatalog(table, HBASE_ROWKEY_FIELD, columns.toMap)

    // 2.catalog转成Json格式
    val catalogJSON: String = objectToJson(catalog)

    // 3.保存数据到hbase
    result.write
      .option(HBaseTableCatalog.tableCatalog, catalogJSON)
      .option(HBaseTableCatalog.newTable, regionCount)
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .save()
  }

  /**
   * 把对象转成json字符串
   *
   * @param obj 对象
   * @return json字符串
   */
  def objectToJson(obj: AnyRef): String = {
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.write

    implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)
    write(obj)
  }
}
