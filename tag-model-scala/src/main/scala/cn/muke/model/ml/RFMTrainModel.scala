package cn.muke.model.ml

import cn.muke.model.{BasicModel, Tag}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.DataFrame

/**
 * 用户价值训练模型  （调度周期一个月一次）
 * 算法：k-means
 * @author zhangYu
 *
 */
object RFMTrainModel extends BasicModel {

  val MODEL_PATH = "/models/rfm/kmeans"

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
    val rfm = rfmScore(source)
    val vectored = assembleFeatures(rfm)
    train(vectored, fiveTags.length)
    null
  }


  /**
   * 求RFM
   */
  def rfmScore(dataFrame: DataFrame): DataFrame = {
    // 导入spark隐式转换
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 1.分别计算RFM的值
    // 求得R值，（R值表示最后一次消费距今的时间）
    val rCol = datediff(date_sub(current_timestamp(), 665), from_unixtime(max('finishtime))).as("r")

    // 求得F值， （F值表示消费频率）
    val fCol = count('ordersn).as("f")

    // 求得M值， (M值表示消费总金额）
    val mCol = sum('orderamount).as("m")

    // 2.为RFM 打分
    // R：1-3天=5分，4-6天=4分，7-9天=3分，10-15天=2分， 大于16天=1分
    // F：>=200=5分，150-199=4分，100-149=3分，50-99=2分，1-49=1分
    // M：>=20w=5分，10-19w=4分，5-9w=3分，1-4w=2分，<=1w=1分
    val rScore = when('r.between(1, 3), 5)
      .when('r.between(4, 6), 4)
      .when('r.between(7, 9), 3)
      .when('r.between(10, 15), 2)
      .when('r >= 16, 1)
      .as("r_score")

    val fScore = when('f >= 200, 5)
      .when('f.between(150, 199), 4)
      .when('f.between(100, 149), 3)
      .when('f.between(50, 99), 2)
      .when('f.between(1, 49), 1)
      .as("f_score")

    val mScore = when(col("m") >= 200000, 5)
      .when(col("m").between(100000, 199999), 4)
      .when(col("m").between(50000, 99999), 3)
      .when(col("m").between(10000, 49999), 2)
      .when(col("m") <= 9999, 1)
      .as("m_score")

    dataFrame.groupBy('memberid)
      .agg(rCol, fCol, mCol)
      .select('memberid as "id", rScore, fScore, mScore)
  }

  /**
   * 特征处理（模型训练前处理特征）
   */
  def assembleFeatures(dataFrame: DataFrame): DataFrame = {
    new VectorAssembler() // 将多个特征列合并成一个特征列
      .setInputCols(Array("r_score", "f_score", "m_score"))
      .setOutputCol("features")
      .setHandleInvalid("skip")
      .transform(dataFrame)
  }


  /**
   * 训练模型
   */
  def train(dataFrame: DataFrame, k: Int): Unit = {
    val classification = new KMeans()
      .setK(k)
      .setSeed(10) //随机数种子
      .setMaxIter(10) //最大迭代次数
      .setFeaturesCol("features")
      .setPredictionCol("predict")

    classification.fit(dataFrame).save(MODEL_PATH)
  }

}
