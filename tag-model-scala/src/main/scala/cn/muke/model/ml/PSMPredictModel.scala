package cn.muke.model.ml

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.sql.DataFrame

/**
 *
 * @author zhangYu
 *
 */
object PSMPredictModel extends BasicModel {

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
    import spark.implicits._

    val psm = PSMTrainModel.psmScore(source)

    val vectored = PSMTrainModel.assemblerVector(psm)

    // 加载训练模型
    val model = KMeansModel.load(PSMTrainModel.PSM_MODEL_PATH)

    val predicted = model.transform(vectored)

    // 重新排序
    val vectoredCenter = model.clusterCenters.indices.map(i => (i, model.clusterCenters(i).toArray.sum))
      .sortBy(t => t._2)
      .reverse

    val center = vectoredCenter.indices.map(i => (vectoredCenter(i)._1, i + 1)).toDF("predict", "index")

    predicted.join(center, predicted.col("predict") === center.col("predict"))
      .select('id, 'index as outField.head)
  }
}
