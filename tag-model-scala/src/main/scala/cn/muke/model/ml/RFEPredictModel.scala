package cn.muke.model.ml

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.sql.DataFrame

/**
 * 用户活跃度-数据预测
 *
 * @author zhangYu
 *
 */
object RFEPredictModel extends BasicModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "用户活跃度"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {
    // 导入隐式转换
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 数据打分
    val rfe = RFETrainModel.rfeScore(source)

    val vectored = RFETrainModel.assembleFeatures(rfe)

    val model = KMeansModel.load(RFETrainModel.RFE_MODEL_PATH)

    val predicted = model.transform(vectored)

    predicted.groupBy('predict)
      .agg(max('r_score + 'f_score + 'e_score).as("max_score"), min('r_score + 'f_score + 'e_score).as("min_score"))
      .sort('predict)
      .show()

    val clusterCenter = model.clusterCenters.indices.map(i => (i, model.clusterCenters(i).toArray.sum))
      .sortBy(t => t._2)
      .reverse //倒序

    val predictCenter = clusterCenter.indices.map(i => (clusterCenter(i)._1, i + 1)).toDF("predict", "index")

    // 执行预测
    predicted.join(predictCenter, predicted.col("predict") === predictCenter.col("predict"))
      .select('id, 'index as outField.head)
  }
}
