package cn.muke.model.stattag

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.sql.{Column, DataFrame}

/**
 *
 * @author zhangyu
 *
 */
object AgeGroupModel extends BasicModel{

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "年龄段"

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
      val start = tag.rule.substring(0,3)

      if (condition == null)
        condition = when('birthday.startsWith(start), tag.id)
      else
        condition = condition.when('birthday.startsWith(start), tag.id)
    }

    // 定义别名
    condition = condition.as(outField.head)

    // 3. 数据清洗转换
    source.select('id, condition)
  }
}
