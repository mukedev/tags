package cn.muke.model.stattag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.expressions.Window

/**
 *
 * @author zhangYu
 *
 */
object PaymentCodeModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "支付方式"

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
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 计算出每个用户使用最多的支付方式
    val groupData = source.groupBy('memberid, 'paymentcode).agg(count('paymentcode) as "count")
      .sort('memberid)

    val result = groupData.withColumn("rn", row_number() over (Window.partitionBy('memberid).orderBy('count desc)))
      .where('rn === 1)

    // 拼接case when条件
    var condition: Column = null
    for (tag <- fiveTags) {
      if (condition == null)
        condition = when('paymentcode === tag.rule, tag.id)
      else
        condition = condition.when('paymentcode === tag.rule, tag.id)
    }

    // 计算标签
    result.select('memberid as "id", condition as outField.head)
  }
}
