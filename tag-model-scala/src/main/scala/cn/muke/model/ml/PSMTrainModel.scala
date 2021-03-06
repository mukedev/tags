package cn.muke.model.ml

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.DoubleType

/**
 * PSM Score = 优惠订单占比 + (平均优惠金额 / 平均每单应收) + 优惠金额占比
 * @author zhangYu
 *
 */
object PSMTrainModel extends BasicModel {

  val PSM_MODEL_PATH = "/models/psm/kmeans"

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "促销敏感度"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {
    val psm = psmScore(source)
    val vectored = assemblerVector(psm)
    train(vectored, fiveTags.length)
    null
  }

  /**
   * 计算psm 分数
   */
  def psmScore(dataFrame: DataFrame): DataFrame = {
    /**
     * * 优惠订单占比
     * 优惠订单 / 总单数
     * 优惠订单占比 = 优惠的订单数量 / 总单数
     * 未优惠订单占比 = 未优惠的订单数量 / 总单数
     * 平均优惠金额
     * 总优惠金额 / 优惠单数
     * 平均每单应收
     * 总应收 / 总单数
     * 优惠金额占比
     * 总优惠金额 / 总应收金额
     *
     * * 直接需要计算的字段
     * 读取 tbl_orders
     * 不需要 Group 直接可以求得
     * 订单的优惠状态, 0 -> 无优惠, 1 -> 有优惠
     * 订单的优惠金额
     * 应收金额 = 优惠金额 + 订单金额
     * 需要按照用户 Group 才能求得
     * 优惠订单数量
     * 未优惠订单数量
     * 总单数
     * 需要 Group 总优惠金额
     * 需要 Group 总应收金额
     * 需要使用 Group 后的字段, 优惠订单占比
     * 需要使用 Group 后的字段, 平均优惠金额
     * 需要使用 Group 后的字段, 平均每单应收
     * 需要使用 Group 后的字段, 优惠金额占比
     */

    import org.apache.spark.sql.functions._
    import spark.implicits._
    // 计算不分组字段
    // 应收金额
    val receivableAmount = ('couponcodevalue + 'orderamount).cast(DoubleType) as "receivableAmount"
    // 优惠金额
    val discountAmount = 'couponcodevalue.cast(DoubleType) as "discountAmount"
    // 实收金额
    val practicalAmount = 'orderamount.cast(DoubleType) as "practicalAmount"
    // 是否优惠，用于累计优惠单量
    val state = when('couponcodevalue =!= 0.0d, 1)
      .when('couponcodevalue === 0.0d, 0)
      .as("state")

    val stage1 = dataFrame.select('memberid as "id", receivableAmount, discountAmount, practicalAmount, state)

    // 计算分组字段
    // 优惠订单数
    val discountCount = sum('state).as("discountCount")
    // 订单总数
    val orderCount = count('state).as("orderCount")
    // 优惠总额
    val totalDiscountAmount = sum('discountAmount).as("totalDiscountAmount")
    // 应收总额
    val totalReceivableAmount = sum('receivableAmount).as("totalReceivableAmount")

    val stage2 = stage1.groupBy('id)
      .agg(discountCount, orderCount, totalDiscountAmount, totalReceivableAmount)

    stage2.show()

    // 计算集成字段
    // 平均优惠金额
    val avgDiscountAmount = ('totalDiscountAmount / 'discountCount).as("avgDiscountAmount")

    // 平均每单应收
    val avgReceivableAmount = ('totalReceivableAmount / 'orderCount).as("avgReceivableAmount")

    // 优惠订单占比
    val discountPercent = ('discountCount / 'orderCount).as("discountPercent")

    // 平均优惠金额占比
    val avgDiscountPercent = (avgDiscountAmount / avgReceivableAmount).as("avgDiscountPercent")

    // 优惠金额占比
    val discountAmountPercent = ('totalDiscountAmount / 'totalReceivableAmount).as("discountAmountPercent")

    // 4.计算 PSM
    // PSM Score = 优惠订单占比 + (平均优惠金额 / 平均每单应收) + 优惠金额占比
    val psmScore = (discountPercent + (avgDiscountAmount / avgReceivableAmount) + discountAmountPercent).as("psm_score")

    stage2.select('id, psmScore)
  }


  /**
   * 特征处理
   */
  def assemblerVector(dataFrame: DataFrame): DataFrame = {
    new VectorAssembler()
      .setInputCols(Array("psm_score"))
      .setOutputCol("features")
      .setHandleInvalid("skip")
      .transform(dataFrame)
  }

  /**
   * 训练模型
   */
  def train(dataFrame: DataFrame, k: Int): Unit = {
    new KMeans()
      .setK(k)
      .setSeed(10)
      .setMaxIter(10)
      .setPredictionCol("predict")
      .setFeaturesCol("features")
      .fit(dataFrame)
      .save(PSM_MODEL_PATH)
  }
}
