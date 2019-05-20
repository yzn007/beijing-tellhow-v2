/*
Navicat MySQL Data Transfer

Source Server         : 172.26.52.179--192.166.162.150
Source Server Version : 50627
Source Host           : 172.26.52.179:3506
Source Database       : bsc_emp_lh

Target Server Type    : MYSQL
Target Server Version : 50627
File Encoding         : 65001

Date: 2019-05-16 16:23:40
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for i01_s06_sfaied_on_ps
-- ----------------------------
DROP TABLE IF EXISTS `i01_s06_sfaied_on_ps`;
CREATE TABLE `i01_s06_sfaied_on_ps` (
  `data_year_sec_cd` varchar(50) NOT NULL,
  `zone_cd` varchar(50) NOT NULL,
  `fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `faet_town_inv_amt` decimal(18,2) DEFAULT NULL,
  `faet_town_est_dev_inv_amt` decimal(18,2) DEFAULT NULL,
  `faet_rurl_inv_amt` decimal(18,2) DEFAULT NULL,
  `faet_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `faet_arch_install_inv_amt` decimal(18,2) DEFAULT NULL,
  `new_incr_faet_amt` decimal(18,2) DEFAULT NULL,
  `fix_invs_amt_yoy_gr` decimal(18,4) DEFAULT NULL,
  `faet_town_yoygr` decimal(18,4) DEFAULT NULL,
  `faet_town_est_dev_yoygr` decimal(18,4) DEFAULT NULL,
  `faet_rurl_yoygr` decimal(18,4) DEFAULT NULL,
  `faet_base_equip_yoygr` decimal(18,4) DEFAULT NULL,
  `faet_arch_install_yoygr` decimal(18,4) DEFAULT NULL,
  `state_owne_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `coltvt_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `unin_oper_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `stk_sys_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `hmt_mercht_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `frgn_mercht_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `prvt_prvt_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `other_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `fst_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `scd_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `scd_inds_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `scd_inds_ein_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `thd_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `thd_tpe_fix_invs_amt` decimal(18,2) DEFAULT NULL,
  `iftinv_amt` decimal(18,2) DEFAULT NULL,
  `enrgy_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `elet_enrgy_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `hetg_enrgy_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `gas_enrgy_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `watr_enrgy_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `pub_srv_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `ladpng_pub_srv_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `envi_pub_srv_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `mege_pub_srv_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `trspt_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `raly_trspt_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `higy_trspt_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `city_pub_trspt_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `atn_trspt_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `ptl_tele_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `ptl_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `tele_iftinv_amt` decimal(18,2) DEFAULT NULL,
  `iftinv_inv_ratio` decimal(18,4) DEFAULT NULL,
  `hous_cra` decimal(18,2) DEFAULT NULL,
  `hous_home_cra` decimal(18,2) DEFAULT NULL,
  `hous_fcs` decimal(18,2) DEFAULT NULL,
  `hous_home_fcs` decimal(18,2) DEFAULT NULL,
  `hous_ctr_fcs` decimal(18,2) DEFAULT NULL,
  `hous_locl_fcs` decimal(18,2) DEFAULT NULL,
  `hous_lso_fcs` decimal(18,2) DEFAULT NULL,
  `hous_lct_fcs` decimal(18,2) DEFAULT NULL,
  `est_dev_corp_qty` int(11) DEFAULT NULL,
  `est_dev_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_lfe_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_home_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_ofb_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_cnho_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_cie_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_pet_inv_amt` decimal(18,2) DEFAULT NULL,
  `annual_complt_land_dra` decimal(18,2) DEFAULT NULL,
  `est_dev_ldp_inv_inv_amt` decimal(18,2) DEFAULT NULL,
  `est_dev_copa_inv_amt` decimal(18,2) DEFAULT NULL,
  `chus_sale_amt` decimal(18,2) DEFAULT NULL,
  `chus_home_sale_amt` decimal(18,2) DEFAULT NULL,
  `DATA_DT` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`data_year_sec_cd`,`zone_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of i01_s06_sfaied_on_ps
-- ----------------------------
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y01', '110100000000', '286.80', '234.40', null, '50.80', '40.60', null, '184.40', '18.8000', '11.7000', null, null, '10.3000', null, '223.80', '22.60', null, null, null, null, '40.40', null, null, null, null, null, null, null, '40.60', '9.40', '5.40', null, null, null, '11.20', null, null, null, '11.10', '3.90', '5.50', null, '1.70', '5.20', null, null, null, null, null, '3941.60', '2383.40', '1723.20', '2218.40', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, '2018-10-20');
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y02', '110100000000', '724.10', '634.90', '22.50', '76.50', '117.90', null, '417.40', '14.8000', '16.8000', null, '1.5000', '20.0000', null, '609.60', '83.10', null, null, null, null, '31.40', null, null, null, null, null, null, null, '117.90', '39.80', '21.10', null, null, null, '25.30', null, null, null, '28.30', '5.00', '19.30', null, '4.00', '20.30', null, null, null, null, null, '5142.40', '2939.90', '2635.10', '2507.30', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, '2018-10-20');
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y03', '110100000000', '2358.70', '2181.50', '568.40', '154.10', '494.90', '1297.10', '1229.10', '34.2000', '36.0000', '59.9000', '19.7000', '40.8000', null, '1764.20', '188.70', null, null, null, null, '24.80', null, null, null, null, null, null, null, '494.90', '134.10', '84.60', '12.30', '15.20', '22.00', '62.90', '2.90', null, '57.30', '123.00', '50.80', '30.60', '22.40', '4.60', '137.30', '9.50', '127.80', null, null, null, '6206.70', '3707.20', '1869.20', '4337.50', null, null, null, '568.40', null, '264.80', null, null, '341.90', null, '2169.90', null, null, '855.60', '816.50', '2018-10-20');
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y04', '110100000000', '5461.70', '5063.80', '1979.50', '322.70', '1382.10', '3069.50', '4051.20', '8.9000', '8.2000', '3.9000', '16.2000', '19.7000', '9.9000', '3380.80', '261.80', null, null, null, null, '155.30', null, null, null, null, null, null, null, '1382.10', '363.40', '239.70', '45.20', '41.40', '37.10', '321.00', '14.20', '27.10', '275.90', '337.40', '26.00', '131.30', '80.00', '3.20', '299.60', '6.00', '293.60', null, null, null, '9644.30', '5979.90', '2726.70', '6917.60', null, null, null, '1979.50', '161.50', '950.70', '351.50', '161.30', '1280.60', '95.50', '1501.90', '161.50', null, '2416.70', '2199.20', '2018-10-20');
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y05', '110100000000', '10857.40', '10033.60', '5974.00', '755.40', '2260.00', '5926.20', '6897.40', '17.7000', '17.9000', '29.0000', '20.1000', '8.5000', '18.0000', '3922.40', '303.30', null, null, null, null, '809.40', null, null, null, null, null, null, null, '2260.00', '301.70', '165.20', '52.70', '53.90', '29.90', '668.60', '23.10', '79.90', '511.40', '766.20', '23.20', '294.00', '302.80', '137.60', '384.00', '6.20', '377.90', null, null, null, '17781.60', '11992.20', '1733.00', '16048.60', null, null, null, '5974.00', '993.60', '3239.50', '696.10', '368.30', '3498.90', '137.80', '4124.30', '993.60', null, '10084.30', '9354.80', '2018-10-20');
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y06', '110100000000', '21538.50', '19678.60', '10863.20', '1859.90', '6137.30', '9860.60', '11666.80', '14.4000', '14.2000', '12.0000', '16.2000', '24.2000', '7.7000', '8162.90', '398.00', null, '9206.80', '903.80', '1480.80', '1121.20', '265.00', null, null, null, null, null, null, '6137.30', '780.30', '443.60', '176.90', '57.60', '102.10', '1640.00', '44.80', '349.40', '1117.70', '3010.90', '358.70', '732.80', '1456.70', '457.70', '462.90', '8.20', '454.40', null, null, null, '20059.10', '10993.80', '2002.60', '18056.40', null, null, null, '10863.20', '3642.00', '5211.50', '1055.20', '1270.80', '4550.50', '255.00', null, '3642.00', null, '12507.00', '9221.00', '2018-10-20');
INSERT INTO `i01_s06_sfaied_on_ps` VALUES ('Y07', '110100000000', '34958.80', '31863.20', '17810.70', '3095.70', '9167.70', '15608.70', '15996.70', '9.9000', '9.9000', '8.7000', '10.1000', '9.0000', '13.3000', '11452.20', '635.50', null, '17733.90', '1525.30', '1468.80', '1707.30', '435.70', null, null, null, null, null, null, '9167.70', '1323.20', '611.20', '264.00', '119.90', '328.30', '2335.70', '125.60', '236.90', '1668.60', '3640.70', '191.80', '506.80', '1918.90', '987.90', '636.80', '12.50', '624.20', null, null, null, '20883.80', '10717.40', '1919.20', '18964.50', null, null, null, '17810.70', '7264.10', '9055.60', '3017.10', '4275.80', '7037.80', '227.50', null, null, null, null, null, '2018-10-20');
