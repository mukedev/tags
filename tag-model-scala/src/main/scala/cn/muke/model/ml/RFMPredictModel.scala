package cn.muke.model.ml

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{IntegerType, LongType, StringType}

/**
 * RFM预测模型    （一天调度一次）
 *
 * @author zhangYu
 *
 */
object RFMPredictModel extends BasicModel{

  def main(args: Array[String]): Unit = {
    startFlow()
  }
  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "客户价值"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: DataFrame, fiveTags: Array[Tag], outField: Array[String]): DataFrame = {

    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 1.预测结果
    // 1.1预测结果前依然要给数据打分
    val rfm = RFMTrainModel.rfmScore(source)
    val vectored = RFMTrainModel.assembleFeatures(rfm)

    // 1.2模型读取
    val model = KMeansModel.load(RFMTrainModel.MODEL_PATH)

    // 1.3执行预测
    val predicted = model.transform(vectored)

    // 2.对结果进行排序
    // 测试排序
//    predicted.groupBy('predict)
//      .agg(max('r_score + 'f_score + 'm_score) as "max_score",min('r_score + 'f_score + 'm_score) as "min_score")
//      .sort("predict")
//      .show()

    // 使用质心排序
    // 注意点：predict与clusterCenters中的原始顺序是一致的
    val sortedCenters = model.clusterCenters.indices.map(i => (i, model.clusterCenters(i).toArray.sum))
      .sortBy(t => t._2)
      .reverse

    // 此次map是将正确的顺序打上index
    val centerIndex = sortedCenters.indices.map(i => (sortedCenters(i)._1, i + 1)).toDF("predict", "index")

    predicted.join(centerIndex, predicted.col("predict") === centerIndex.col("predict"))
      .select(predicted.col("id"),centerIndex.col("index") as outField.head)
  }
}
