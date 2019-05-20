DROP FUNCTION IF EXISTS `func_get_split_string`;

CREATE DEFINER = `root`@`%` FUNCTION `func_get_split_string`(f_string varchar(4000),f_delimiter varchar(5),f_order int)
 RETURNS varchar(255)
BEGIN
  -- Get the separated number of given string.
  declare result varchar(255) default ''; 
  set result = trim(reverse(substring_index(reverse(substring_index(f_string,f_delimiter,f_order)),f_delimiter,1))); 
return result; 
END;

DROP FUNCTION IF EXISTS `func_get_split_string_total`;

CREATE DEFINER = `root`@`%` FUNCTION `func_get_split_string_total`(f_string varchar(4000),f_delimiter varchar(5))
 RETURNS int(11)
BEGIN
  -- Get the total number of given string.
  return 1+(length(f_string) - length(replace(f_string,f_delimiter,''))); 
END;

DROP PROCEDURE IF EXISTS `findSQLColumns`;

CREATE DEFINER = `root`@`%` PROCEDURE `findSQLColumns`(IN `p_select_sql`   varchar(4000),
                          OUT `o_columns`  varchar(4000))
begin
    DECLARE v_sql_cur       int         DEFAULT  0;  -- SQL游标句柄
    DECLARE v_col_cnt       int         DEFAULT  0;  -- 字段个数

    DECLARE v_cols          varchar(1000);     -- 结果列表
    DECLARE v_sql           varchar(4000);
    DECLARE v_select_inx  int default 1; -- select index
    declare v_where_inx     int  default 1; -- where index
    DECLARE v_from_inx  int default 1; -- from index
    declare v_tablename varchar(1000) DEFAULT ''; -- tablename
    declare v_colmunname varchar(4000) DEFAULT ''; -- columnsname
DECLARE EXIT HANDLER FOR  SQLEXCEPTION
 BEGIN
    ROLLBACK ;
    set o_columns = concat('findSQLColumns executes error,Please contact your administrator!Info:[tables]:'
			,v_tablename,';[columns]',v_colmunname,'v_forminx:',v_from_inx,'v_where:',v_where_inx,'v_select:',v_select_inx);
 END;
    set v_sql = p_select_sql;
    
    drop table  if exists tmp_v_tables;
    create temporary table tmp_v_tables (table_name varchar(200) not null) DEFAULT CHARSET=gb2312;
    drop table  if exists tmp_v_columns;
    create temporary table tmp_v_columns (column_name varchar(200) not null) DEFAULT CHARSET=gb2312;

    
		set v_from_inx = LOCATE('from',v_sql,v_from_inx);
    set v_where_inx = LOCATE('where',v_sql,v_where_inx);
    while v_from_inx >0 do
        
        if v_where_inx > v_from_inx or (v_where_inx = 1 and v_from_inx >1)then
						if v_where_inx != 1 THEN
							set v_tablename = trim(substr(v_sql,LOCATE('from',v_sql,v_from_inx)+4,v_where_inx-v_from_inx-4));
						ELSE
							set v_tablename = trim(substr(v_sql,LOCATE('from',v_sql,v_from_inx)+4));
						end if;            
						
						set v_where_inx = v_where_inx + 1;
            call  split_insert_tmp( v_tablename, ',', 'tmp_v_tables');
        end if;
        set v_select_inx = LOCATE('select',v_sql,v_select_inx);
        if v_select_inx > 0 and v_select_inx<v_from_inx then
            set v_colmunname = trim(substr(v_sql,LOCATE('select',v_sql,v_select_inx)+6,v_from_inx-v_select_inx-6));
						if trim(v_colmunname) = '*' THEN
							SELECT GROUP_CONCAT(COLUMN_NAME) into v_colmunname  FROM information_schema.columns WHERE table_name= v_tablename;
													
						end if;
            call split_insert_tmp(v_colmunname,',','tmp_v_columns');
        end if;
				set v_from_inx = LOCATE('from',v_sql,v_from_inx+1);
        set v_where_inx = LOCATE('where',v_sql,v_where_inx+1);

    end while;

    select GROUP_CONCAT(CONCAT(colname,':',datatype,':',rowno))
           into o_columns
     from (select a.*,@rownum:=@rownum+1 as rowno from (select  k.column_name colname,k.data_type datatype,k.TABLE_NAME tablename from 
        information_schema.columns k ) a ,(select @rownum:=0 )t
        where exists
    (select 1 from tmp_v_tables vtb where a.tablename =  SUBSTRING_INDEX(vtb.table_name,' ',1))
    and exists
    (select 1 from tmp_v_columns cl where a.colname = cl.column_name)) d;
    

end;

