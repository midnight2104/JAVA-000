/*
SQLyog Enterprise v12.08 (64 bit)
MySQL - 5.7.30 : Database - demo1
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`demo1` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `demo1`;

/*Table structure for table `t_address` */

DROP TABLE IF EXISTS `t_address`;

CREATE TABLE `t_address` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地址id',
  `province` smallint(6) NOT NULL COMMENT '省',
  `city` smallint(6) NOT NULL COMMENT '市',
  `district` smallint(6) NOT NULL COMMENT '区',
  `detail_address` varchar(100) NOT NULL COMMENT '详细地址',
  `user_id` int(11) NOT NULL COMMENT '收货人',
  `create_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_express` */

DROP TABLE IF EXISTS `t_express`;

CREATE TABLE `t_express` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '快递id',
  `company_name` varchar(10) NOT NULL COMMENT '快递公司名称',
  `express_num` varchar(30) NOT NULL COMMENT '快递单号',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `status` varchar(30) NOT NULL COMMENT '状态：到达哪儿了',
  `create_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_order` */

DROP TABLE IF EXISTS `t_order`;

CREATE TABLE `t_order` (
  `id` char(20) NOT NULL COMMENT '订单编号',
  `status` tinyint(4) NOT NULL COMMENT '订单状态',
  `payment_method` tinyint(4) NOT NULL COMMENT '支付方式',
  `order_money` decimal(8,2) NOT NULL COMMENT '订单金额',
  `discount_money` decimal(8,2) NOT NULL COMMENT '优惠金额',
  `express_money` decimal(8,2) NOT NULL COMMENT '运费金额',
  `payment_money` decimal(8,2) NOT NULL COMMENT '支付金额',
  `create_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '创建时间',
  `pay_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '支付时间',
  `address_id` int(11) NOT NULL COMMENT '地址id',
  `express_id` int(11) NOT NULL COMMENT '快递id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_order_product` */

DROP TABLE IF EXISTS `t_order_product`;

CREATE TABLE `t_order_product` (
  `order_id` int(11) NOT NULL COMMENT '订单id',
  `product_id` int(11) NOT NULL COMMENT '商品id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_product` */

DROP TABLE IF EXISTS `t_product`;

CREATE TABLE `t_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `name` varchar(20) NOT NULL COMMENT '商品名称',
  `description` varchar(50) NOT NULL COMMENT '商品描述',
  `price` decimal(8,2) NOT NULL COMMENT '商品销售价格',
  `create_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_user` */

DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user` (
  `id` char(20) NOT NULL COMMENT '用户id：身份证',
  `username` varchar(20) NOT NULL COMMENT '用户名称',
  `email` varchar(20) NOT NULL COMMENT '邮箱',
  `mobile_phone` char(11) NOT NULL COMMENT '手机号',
  `gender` char(1) DEFAULT NULL COMMENT '性别',
  `create_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1970-01-01 10:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_user_order` */

DROP TABLE IF EXISTS `t_user_order`;

CREATE TABLE `t_user_order` (
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `order_id` int(11) NOT NULL COMMENT '订单id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
