package cn.muke.model.mtag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.{Column, DataFrame}

/**
 * 政治面貌
 *
 * @author zhangyu
 *
 */
object PoliticalFaceModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "政治面貌"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {
    // 1. 导入spark隐式转换
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 2. 拼接case when
    var condition: Column = null
    for (tag <- fiveTags) {
      if (condition == null)
        condition = when('politicalFace === tag.rule, tag.id)
      else
        condition = condition.when('politicalFace === tag.rule, tag.id)
    }

    println(condition)
    // 3. 添加别名
    condition = condition.as(outField.head)

    // 4. 数据清洗
    source.select('id, condition)
  }
}
