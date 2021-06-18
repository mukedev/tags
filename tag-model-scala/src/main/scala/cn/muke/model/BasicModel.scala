package cn.muke.model

import cn.muke.model.utils.ShcUtil
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.Properties

/**
 * 基础模块模版
 *
 * @author zhangYu
 *
 */
trait BasicModel {

  val regionCount = "5"

  val spark = SparkSession.builder()
    .appName(s"${this.getClass.getSimpleName}")
    .master("local[6]")
    .getOrCreate()

  val config = ConfigFactory.load()

  def startFlow(): Unit = {
    // 1.读取四级标签和五级标签
    val (fourTag, fiveTags) = readBasicTag(initTagName())

    // 2.通过四级标签读取元素数据
    // 3.处理元数据，处理成结构化的方式
    val metaData = readMetaData(fourTag.id)

    // 4.使用元数据连接源表，拿到源表数据
    val (source, commonMeta) = createSource(metaData)

    // 5.匹配计算标签数据
    val result = process(source, fiveTags, commonMeta.outFields)

    // 6.把标签信息放入用户画像表
    saveUserProfile(result, commonMeta.outFields)

  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  def initTagName(): String

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame

  /**
   * 读取标签数据
   *
   * @param tagName 标签名称
   * @return
   */
  def readBasicTag(tagName: String): (Tag, Array[Tag]) = {
    // 1.1 创建配置文件 不指定配置文件默认加载application.conf文件
    val url = config.getString("jdbc.mysql.basic_tag.url")
    val table = config.getString("jdbc.mysql.basic_tag.table")

    // 1.2 创建sparkSession读取四级标签
    val source: DataFrame = spark.read.jdbc(url, table, new Properties())

    // 通过name筛选四级标签
    import spark.implicits._
    val fourTag = source.where('name === tagName) // 相当于 where = '性别'
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
    spark.read.jdbc(url, table, new Properties())
      .where(col(matchColumn) === tagId)
      .as[MetaData]
      .collect()
      .head
  }


  /**
   * 根据元数据获取标签对应的源数据
   *
   * @param metaData
   * @return
   */
  def createSource(metaData: MetaData): (DataFrame, CommonMeta) = {
    // 判断是否是HBase
    if (metaData.isHBase()) {
      val hbaseMeta = metaData.toHBaseMeta()
      val source = ShcUtil.read(spark, hbaseMeta.commonMeta.inFields, hbaseMeta.columnFamily, hbaseMeta.tableName)
      return (source, hbaseMeta.commonMeta)
    }

    // 判断是否是Mysql
    if (metaData.isRDBMS()) {

    }

    // 判断是否是Hdfs
    if (metaData.isHdfs()) {

    }

    (null, null)
  }


  /**
   * 存储画像数据
   */
  def saveUserProfile(result: DataFrame, outFields: Array[String]): Unit = {
    ShcUtil.writeToHBase(result, outFields, regionCount)
  }
}
