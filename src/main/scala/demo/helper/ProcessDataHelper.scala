package demo.helper

import org.apache.spark.sql.{ DataFrame, Dataset }
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.regexp_replace
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.StringType
import demo.constants.Constants
import org.apache.spark.sql.expressions.Window
import demo.utils.DataframeReadWriteUtils
import org.apache.spark.ml.regression.{ LinearRegression, LinearRegressionModel, RandomForestRegressor }
import org.apache.spark.ml.feature.Interaction
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.tuning.{ CrossValidator, CrossValidatorModel }
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.{ Pipeline, PipelineModel }
import org.apache.spark.ml.feature.PCA
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.ml.classification.{ RandomForestClassifier, LogisticRegression,DecisionTreeClassifier }
import org.apache.spark.ml.evaluation.{ MulticlassClassificationEvaluator, BinaryClassificationEvaluator }
import org.apache.spark.sql.SparkSession

object ProcessDataHelper {
  
  def readWineData(sparkSession: SparkSession, filePath: String): DataFrame = {
    val df = DataframeReadWriteUtils.creatingDataframeFromCsv(sparkSession, filePath)
    val renameColList = List("fixed_acidity", "volatile_acidity", "citric_acid", "residual_sugar", "chlorides", "free_sulfur_dioxide", "total_sulfur_dioxide", "density", "pH", "sulphates", "alcohol", "quality")
    return df.toDF(renameColList: _*).withColumn("quality_category", expr("case when quality < 6 then 'BAD' else 'GOOD' end"))
  }

  def predictionDf(lrModel: LinearRegressionModel, df: DataFrame): DataFrame = {
    return lrModel.transform(df)
  }

  def calculateRMSE(df: DataFrame): Double = {
    val evaluator = new RegressionEvaluator().setLabelCol("quality").setMetricName("rmse").setPredictionCol("prediction")
    val rmse = evaluator.evaluate(df)
    return rmse
  }

  def assembler(): VectorAssembler = {
    new VectorAssembler().
      setInputCols(Array("fixed_acidity", "volatile_acidity", "citric_acid", "residual_sugar","chlorides", "free_sulfur_dioxide", "total_sulfur_dioxide", "density", "pH", "sulphates", "alcohol")).
      setOutputCol("features")

  }

    def standardizer(): StandardScaler = {
    new StandardScaler().setInputCol("features").setOutputCol("normFeatures").setWithStd(true)
      .setWithMean(true)
  }

  
  def LogisticModelCreation(): LogisticRegression = {
    new LogisticRegression()
      .setLabelCol("label")
      .setFeaturesCol("normFeatures")
  }

  def trainModel(pipeline: Pipeline, df: DataFrame): PipelineModel = {
    pipeline.fit(df)
  }

  def crossValidatorTuning(pipeline: Pipeline, df: DataFrame): CrossValidatorModel = {
    var cv: CrossValidator = null
    val randomForestClassifier=new RandomForestClassifier()
      val paramGrid = new ParamGridBuilder()
        .addGrid(randomForestClassifier.maxBins, Array(50,100,150))
        .addGrid(randomForestClassifier.maxDepth, Array(11, 12,13,14,15))
        .addGrid(randomForestClassifier.impurity, Array("entropy", "gini"))
        .addGrid(randomForestClassifier.numTrees, Array(50,100,150,200))
        .addGrid(randomForestClassifier.featureSubsetStrategy, Array("auto"))
        .addGrid(randomForestClassifier.seed, Array(5043L))
        .addGrid(randomForestClassifier.featuresCol, Array("normFeatures"))
        .addGrid(randomForestClassifier.minInstancesPerNode, Array(1,2,3,4))
        .build()
      cv = new CrossValidator()
        .setEstimator(pipeline)
        .setEvaluator(new BinaryClassificationEvaluator().setLabelCol("label"))
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(10)
    return cv.fit(df)
  }

  def randomForestClassification(): RandomForestClassifier = {
    new RandomForestClassifier().setFeaturesCol("normFeatures").setLabelCol("label").setImpurity("gini")
    .setNumTrees(500).setFeatureSubsetStrategy("auto").setMaxDepth(15).setMaxBins(23).setMinInstancesPerNode(3).setSeed(5043)
  }
  
  
  def decisionTreeClassification(): DecisionTreeClassifier = {
    new DecisionTreeClassifier().setSeed(5043).setImpurity("gini").setMaxDepth(2).setFeaturesCol("normFeatures").setLabelCol("label").setMaxBins(10)
  }

  def PCAModelCreation(): PCA = {
    new PCA()
      .setInputCol("features")
      .setK(10)
      .setOutputCol("pcaFeatures")
  }

  def encodeStringIndex(): StringIndexer = {
    new StringIndexer()
      .setInputCol("quality_category")
      .setOutputCol("label")
  }
  
  def calculateMetrics(sparkSession: SparkSession,df:DataFrame) ={
      import sparkSession.implicits._
      val lp = df.select("label", "prediction")
      val counttotal = df.count()
      val correct = lp.filter($"label" === $"prediction").count()
      val wrong = lp.filter(not($"label" === $"prediction")).count()
      val ratioWrong = wrong.toDouble / counttotal.toDouble
      val ratioCorrect = correct.toDouble / counttotal.toDouble
      val truep = lp.filter($"prediction" === 0.0).filter($"label" === $"prediction").count() / counttotal.toDouble
      val truen = lp.filter($"prediction" === 1.0).filter($"label" === $"prediction").count() / counttotal.toDouble
      val falsep = lp.filter($"prediction" === 1.0).filter(not($"label" === $"prediction")).count() / counttotal.toDouble
      val falsen = lp.filter($"prediction" === 0.0).filter(not($"label" === $"prediction")).count() / counttotal.toDouble
      println("Total Count: " + counttotal)
      println("Correct: " + correct)
      println("Wrong: " + wrong)
      println("Ratio wrong: " + ratioWrong)
      println("Ratio correct: " + ratioCorrect)
      println("Ratio true positive: " + truep)
      println("Ratio false positive: " + falsep)
      println("Ratio true negative: " + truen)
      println("Ratio false negative: " + falsen)
  }
}