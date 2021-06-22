package cn.muke.model.stattag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.{Column, DataFrame}

/**
 * 消费周期
 *
 * @author zhangYu
 *
 */
object BuyCycleModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "消费周期"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {

    // 导入spark隐式转换
    import org.apache.spark.sql.functions._
    import spark.implicits._

    // 求得日期差值
    val dayPass = datediff(current_timestamp(), from_unixtime('finishtime)).as("dayPass")

    // 筛选出最近的消费时间
    val stage1 = source.select('memberid, 'finishtime.cast(LongType) as "finishtime")
      .groupBy('memberid)
      .agg(max('finishtime) as "finishtime")
      .select('memberid as "id", dayPass)

    // 拼接条件
    var condition: Column = null
    for (tag <- fiveTags) {
      val ranges = tag.rule.split("-")
      val start = ranges(0)
      val end = ranges(1)
      if (condition == null) {
        condition = when('dayPass.between(start, end), tag.id)
      } else {
        condition = condition.when('dayPass.between(start, end), tag.id)
      }
    }

    // 打标签
    stage1.select('id, condition as outField.head)
  }
}
