package cn.muke.model.stattag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{Column, DataFrame}

/**
 * 最近支付方式
 *
 * @author zhangYu
 *
 */
object RecentlyPaymentCodeModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "支付方式" //最近支付方式

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

    // 计算出最近的支付行为
    val result = source.select(
      'memberid as "id",
      'paymentcode,
      row_number() over Window.partitionBy('memberid, 'paymentcode).orderBy('paytime desc) as "rn"
    )
      .where('rn === 1)

    // 拼接case when 条件
    var condition: Column = null
    for (tag <- fiveTags) {
      if (condition == null)
        condition = when('paymentcode === tag.rule, tag.id)
      else
        condition = condition.when('paymentcode === tag.rule, tag.id)
    }

    // 计算标签
    result.select('id, condition as outField.head)
  }
}
