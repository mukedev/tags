

#spark shell提交执行命令：
spark-submit --class cn.itcast.model.utils.Hive2HBase --master yarn --deploy-mode client --executor-memory 2G --executor-cores 2 --driver-memory 2G --num-executors 3 tag_model_new-0.0.1-jar-with-dependencies.jar default tbl_goods id
spark-submit --class cn.muke.model.utils.Hive2HBase --master yarn --deploy-mode client --executor-memory 2G --executor-cores 2 --driver-memory 2G --num-executors 3 tag-model-scala-0.0.1-jar-with-dependencies.jar default tbl_goods id