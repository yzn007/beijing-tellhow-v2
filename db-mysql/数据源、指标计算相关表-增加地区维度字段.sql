ALTER TABLE `bsc_datasource`

ADD COLUMN `district_column`  varchar(36) NOT NULL COMMENT '����ά��' AFTER `obj_column`;

update bsc_datasource set district_column = 'Zone_Cd';

ALTER TABLE `bsc_result_measure`

ADD COLUMN `district_id`  varchar(36)  NULL COMMENT '����ά��' AFTER `object_id`;

ALTER TABLE `bsc_proj_mea_obj_val_measure`

ADD COLUMN `district_id`  varchar(36)  NULL COMMENT '����ά��' AFTER `object_id`;