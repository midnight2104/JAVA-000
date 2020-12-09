/*
SQLyog Enterprise v12.08 (64 bit)
MySQL - 5.7.30 : Database - db_order_1
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`db_order_1` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `db_order_1`;

/*Table structure for table `t_order_0` */

DROP TABLE IF EXISTS `t_order_0`;

CREATE TABLE `t_order_0` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_1` */

DROP TABLE IF EXISTS `t_order_1`;

CREATE TABLE `t_order_1` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_10` */

DROP TABLE IF EXISTS `t_order_10`;

CREATE TABLE `t_order_10` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_11` */

DROP TABLE IF EXISTS `t_order_11`;

CREATE TABLE `t_order_11` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_12` */

DROP TABLE IF EXISTS `t_order_12`;

CREATE TABLE `t_order_12` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_13` */

DROP TABLE IF EXISTS `t_order_13`;

CREATE TABLE `t_order_13` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_14` */

DROP TABLE IF EXISTS `t_order_14`;

CREATE TABLE `t_order_14` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_15` */

DROP TABLE IF EXISTS `t_order_15`;

CREATE TABLE `t_order_15` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_2` */

DROP TABLE IF EXISTS `t_order_2`;

CREATE TABLE `t_order_2` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_3` */

DROP TABLE IF EXISTS `t_order_3`;

CREATE TABLE `t_order_3` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_4` */

DROP TABLE IF EXISTS `t_order_4`;

CREATE TABLE `t_order_4` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_5` */

DROP TABLE IF EXISTS `t_order_5`;

CREATE TABLE `t_order_5` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_6` */

DROP TABLE IF EXISTS `t_order_6`;

CREATE TABLE `t_order_6` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_7` */

DROP TABLE IF EXISTS `t_order_7`;

CREATE TABLE `t_order_7` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_8` */

DROP TABLE IF EXISTS `t_order_8`;

CREATE TABLE `t_order_8` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `t_order_9` */

DROP TABLE IF EXISTS `t_order_9`;

CREATE TABLE `t_order_9` (
  `id` int(11) DEFAULT NULL COMMENT '用户id',
  `o_name` varchar(20) DEFAULT NULL COMMENT '订单名称',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
