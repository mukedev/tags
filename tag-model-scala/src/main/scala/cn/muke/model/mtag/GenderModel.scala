package cn.muke.model.mtag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.{Column, DataFrame}

/**
 * 性别标签
 *
 * @author zhangyu
 *
 */
object GenderModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "性别"


  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {
    // 1 导入spark隐式转换
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 2 拼接条件
    var condition: Column = null
    for (tag <- fiveTags) {
      if (condition == null)
        condition = when('gender === tag.rule, tag.id)
      else
        condition = condition.when('gender === tag.rule, tag.id)
    }

    condition = condition.as(outField.head)

    // 3 查询过滤数据
    source.select('id, condition)
  }

}
