package cn.muke.model.ml

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.DataFrame

/**
 * 用户活跃度-模型训练
 * RFE = R(最近一次访问时间) + F (特定时间内的访问频率) + E（活动数量）
 * 算法：k-means
 *
 * @author zhangYu
 *
 */
object RFETrainModel extends BasicModel {

  val RFE_MODEL_PATH = "/models/rfe/kmeans"

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
    val rfe = rfeScore(source)
    val assemble = assembleFeatures(rfe)
    assemble.show()
    train(assemble, fiveTags.length)
    null
  }

  /**
   * 求出RFE
   */
  def rfeScore(dataFrame: DataFrame): DataFrame = {
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // R取值-最近一次访问
    val rCol = datediff(date_sub(current_timestamp(), 665), max('log_time)).as("r")

    // F取值-访问次数
    val fCol = count('loc_url).as("f")

    // E取值-活动次数
    val eCol = countDistinct('loc_url).as("e")

    val rScore = when(col("r").between(1, 3), 5)
      .when(col("r").between(4, 6), 4)
      .when(col("r").between(7, 9), 3)
      .when(col("r").between(10, 15), 2)
      .when(col("r") >= 16, 1)
      .as("r_score")

    val fScore = when('f >= 400, 5)
      .when('f >= 300 && 'f <= 399, 4)
      .when('f >= 200 && 'f <= 299, 3)
      .when('f >= 100 && 'f <= 199, 2)
      .when('f >= 1 && 'f <= 99, 1)
      .as("f_score")

    val eScore = when('e >= 240, 5)
      .when('e >= 180 and 'e <= 239, 4)
      .when('e >= 120 and 'e <= 179, 3)
      .when('e >= 60 and 'e <= 119, 2)
      .when('e >= 1 and 'e <= 59, 1)
      .as("e_score")

    dataFrame.groupBy('global_user_id)
      .agg(rCol, fCol, eCol)
      .select('global_user_id as "id", rScore, fScore, eScore)
  }


  /**
   * 特征处理
   */
  def assembleFeatures(dataFrame: DataFrame): DataFrame = {
    new VectorAssembler() // 将多个特征列合并成一个特征列
      .setInputCols(Array("r_score", "f_score", "e_score"))
      .setOutputCol("features")
      .setHandleInvalid("skip")
      .transform(dataFrame)
  }

  /**
   * 训练
   */
  def train(dataFrame: DataFrame, k: Int): Unit = {
    val kMeans = new KMeans()
      .setK(k)
      .setSeed(10)
      .setMaxIter(10)
      .setFeaturesCol("features")
      .setPredictionCol("predict")

    kMeans.fit(dataFrame).save(RFE_MODEL_PATH)
  }
}
