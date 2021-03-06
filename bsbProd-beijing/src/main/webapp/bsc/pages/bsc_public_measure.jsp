<%@ page language="java" import="java.util.*" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>公共指标维护</title>
    <!-- 清理缓存 -->
    <meta http-equiv="pragma" content="no-cache"> 
	<meta http-equiv="cache-control" content="no-cache"> 
	<meta http-equiv="expires" content="0">
	
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/scripts/ext3.4.0/resources/css/ext-all.css" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/css/icon.css" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/scripts/dhtmlx/dhtmlxtree.css">
	  <script type="text/javascript" src="${pageContext.request.contextPath}/public/scripts/jquery/jquery-1.9.1.js"></script>
	
	<%@ include file="/skin.jsp"%>

	<script type="text/javascript" src="${pageContext.request.contextPath}/public/scripts/ext3.4.0/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/public/scripts/ext3.4.0/ext-all.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/public/scripts/ext3.4.0/locale/ext-lang-zh_CN.js"></script>
	
	<script type="text/javascript">
  		var pathUrl = "${pageContext.request.contextPath}";
		var extPath = "${pageContext.request.contextPath}/public/scripts/ext3.4.0";
		Ext.BLANK_IMAGE_URL = extPath + '/resources/images/default/s.gif';
        Ext.Ajax.timeout = 600000;
		Ext.QuickTips.init();
		var ntype = 'public',obj_cate_id = null,pageindex='1',page = 'bsc_public_measure',cycleTypeID ='',published='N';
    </script>
    
    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/Map.js"></script>	
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/Selection.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/tree_search.js"></script>	
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/AsyncTree.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/ListPanel.js"></script>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/source_measure_formula.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/formula_panel.js"></script>
	<!-- 
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/formulaConfig.js"></script>
	 -->
  	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/bsc_public_measure_layout.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/bsc_public_measure.js"></script>
	  <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/bsc_calc_exec_common_layout.js"></script>
	  <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/bsc_calc_exec_common.js"></script>
	  <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/do_execute_project.js"></script>
	  <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/progress.js"></script>
  </head>
  <body>
  </body>
</html>
