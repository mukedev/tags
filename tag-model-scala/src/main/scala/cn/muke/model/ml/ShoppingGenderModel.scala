package cn.muke.model.ml

import cn.muke.model.{BasicModel, MultiSourceModel, Tag}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.sql.DataFrame

/**
 *
 * @author zhangYu
 *
 */
object ShoppingGenderModel extends MultiSourceModel {

  def main(args: Array[String]): Unit = {
    startFlow()
  }

  /**
   * 抽象方法：初始化标签名称
   *
   * @return
   */
  override def initTagName(): String = "购物性别"

  /**
   * 抽象方法：计算标签
   *
   * @param source   DataFrame
   * @param fiveTags 五级标签
   * @param outField 输出字段
   * @return
   */
  override def process(source: Array[DataFrame], fiveTags: Array[Tag], outField: Array[String]): DataFrame = {
    // 1.导入spark隐式转换
    import spark.implicits._
    import org.apache.spark.sql.functions._

    // 2.拿到两个DataFrame source 一个是tbl_goods,另一个是tbl_orders
    val goodsSource = source(0)
    val orderSource = source(1)

    // 3.匹配规则
    val label = when('ogcolor.equalTo("樱花粉")
      .or('ogcolor.equalTo("白色"))
      .or('ogcolor.equalTo("香槟色"))
      .or('ogcolor.equalTo("香槟金"))
      .or('producttype.equalTo("料理机"))
      .or('producttype.equalTo("挂烫机"))
      .or('producttype.equalTo("吸尘器/除螨仪")), 1)
      .otherwise(0)
      .alias("gender")

    val stage1 = goodsSource.select('cordersn, 'ogcolor as "color", 'producttype as "productType", label)
      .join(orderSource, goodsSource.col("cordersn") === orderSource.col("ordersn"))
      .select('memberid as "id", 'color, 'productType, 'gender)

    // 4.特征处理
    val colorIndex = new StringIndexer()
      .setInputCol("color")
      .setOutputCol("color_index")

    val productTypeIndex = new StringIndexer()
      .setInputCol("productType")
      .setOutputCol("product_type_index")

    val featureAssembler = new VectorAssembler()
      .setInputCols(Array("color_index", "product_type_index"))
      .setOutputCol("feature")

    val featureIndex = new VectorIndexer()
      .setInputCol("feature")
      .setOutputCol("feature_index")

    // 决策树算法
    val decisionTree = new DecisionTreeClassifier()
      .setFeaturesCol("feature_index")
      .setLabelCol("gender")
      .setPredictionCol("predict")
      .setMaxDepth(5)
      .setImpurity("gini")

    // 5.模型训练和组装
    val pipeline = new Pipeline()
      .setStages(Array(colorIndex, productTypeIndex, featureAssembler, featureIndex, decisionTree))

    // 在有监督学习时，数据集划分为两个部分，一个是训练，一个是预测
    val Array(trainDF, testDF) = stage1.randomSplit(Array(0.8, 0.2))

    val model = pipeline.fit(trainDF)
    val result = model.transform(testDF)
    result.show()

    // 6.预测

    // 7.计算精确率
    val accEvaluator = new MulticlassClassificationEvaluator()
      .setPredictionCol("predict")
      .setLabelCol("gender")
      .setMetricName("accuracy")

    val acc = accEvaluator.evaluate(result)
    println(acc)

    // 8 标签设置，按照用户进行分组，求得男女偏好的占比，超过8成，则认为是对应的标签
    null
  }
}
