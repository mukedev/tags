package cn.muke.model

import org.apache.commons.lang3.StringUtils


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



/**
 * 所有标签对应的样例类
 */
case class Tag(id: String, name: String, rule: String, pid: String)

/**
 * 元数据信息样例类
 * 支持三种源库类型：
 * RDBMS, HDFS file, HBase
 */
case class MetaData(in_type: String, driver: String, url: String, username: String, password: String, db_table: String,
                    in_path: String, sperator: String, in_fields: String, cond_fields: String, out_fields: String,
                    out_path: String, zk_hosts: String, zk_port: String, hbase_table: String, family: String,
                    select_field_names: String, where_field_names: String, where_field_values: String) {

  def isRDBMS(): Boolean = {
    in_type.toLowerCase() == "mysql" || in_type.toLowerCase() == "postgresql" ||
      in_type.toLowerCase() == "oracle" || in_type.toLowerCase() == "rdbms"
  }

  def isHBase(): Boolean = {
    in_type.toLowerCase() == "hbase"
  }

  def isHdfs(): Boolean = {
    in_type.toLowerCase() == "hdfs"
  }

  def toMySqlMeta(): MySqlMeta = {
    if (isRDBMS()) {
      return null
    }
    val commonMeta = CommonMeta(in_type, in_fields.split(","), out_fields.split(","))
    val mySqlMeta = MySqlMeta(commonMeta, driver, url, username, password, db_table)
    mySqlMeta
  }

  def toHBaseMeta(): HBaseMeta = {
    if (isHBase()) {
      return null
    }
    if (StringUtils.isBlank(in_fields) || StringUtils.isBlank(out_fields)) {
      return null
    }
    val commonMeta = CommonMeta(in_type, in_fields.split(","), out_fields.split(","))
    val hBaseMeta = HBaseMeta(commonMeta, hbase_table, family)
    hBaseMeta
  }

  def toHdfsMeta(): HdfsMeta = {
    if (isHdfs()) {
      return null
    }
    val commonMeta = CommonMeta(in_type, in_fields.split(","), out_fields.split(","))
    val hdfsMeta = HdfsMeta(commonMeta, in_path, sperator)
    hdfsMeta
  }


}

/**
 * 通用的metaData
 */
case class CommonMeta(inType: String, inFields: Array[String], outFields: Array[String])

case class MySqlMeta(commonMeta: CommonMeta, driver: String, url: String, userName: String, password: String, tableName: String)

case class HBaseMeta(commonMeta: CommonMeta, tableName: String, columnFamily: String)

case class HdfsMeta(commonMeta: CommonMeta, inPath: String, separator: String)