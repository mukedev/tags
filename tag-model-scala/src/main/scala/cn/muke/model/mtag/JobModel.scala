package cn.muke.model.mtag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.{Column, DataFrame}

/**
 * 职业标签
 *
 * @author zhangyu
 *
 */
object JobModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "职业"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {
    // 1. 导入spark隐式函数
    import org.apache.spark.sql.functions._
    import spark.implicits._

    // 2. 拼接Condition
    var condition: Column = null
    for (tag <- fiveTags) {
      if (condition == null)
        condition = when('job === tag.rule, tag.id)
      else
        condition = condition.when('job === tag.rule, tag.id)
    }

    // 3. 筛选过滤数据
    condition = condition.as(outField.head)
    source.select('id, condition)
  }
}
