#!/bin/bash

sqoopdir=/opt/cloudera/parcels/CDH/bin/sqoop

$sqoopdir import \
--hive-import \
--create-hive-table \
--hive-table default.tbl_goods \
--connect jdbc:mysql://120.77.3.5:3306/tags_dat \
--username root \
--password zhangyuzzyy123 \
--query 'select * from tags_dat.tbl_goods where $CONDITIONS' \
--split-by id \
--direct \
--target-dir /user/hive/warehouse/tbl_goods \
--m 2


$sqoopdir import \
--hive-import \
--create-hive-table \
--hive-table default.tbl_logs \
--connect jdbc:mysql://120.77.3.5:3306/tags_dat \
--username root \
--password zhangyuzzyy123 \
--query 'select * from tags_dat.tbl_logs where $CONDITIONS' \
--hive-drop-import-delims \
--split-by id \
--delete-target-dir \
--target-dir /user/hive/warehouse/tbl_logs \
--m 10


$sqoopdir import \
--hive-import \
--create-hive-table \
--hive-table default.tbl_orders \
--connect jdbc:mysql://120.77.3.5:3306/tags_dat \
--username root \
--password zhangyuzzyy123 \
--query 'select * from tags_dat.tbl_orders where $CONDITIONS' \
--hive-drop-import-delims \
--split-by id \
--target-dir /user/hive/warehouse/tbl_orders \
--m 2



$sqoopdir import \
--hive-import \
--create-hive-table \
--hive-table default.tbl_users \
--connect jdbc:mysql://120.77.3.5:3306/tags_dat \
--username root \
--password zhangyuzzyy123 \
--query 'select * from tags_dat.tbl_users where $CONDITIONS' \
--split-by id \
--direct \
--target-dir /user/hive/warehouse/tbl_users \
--m 2
