<%@ page language="java" import="java.util.*" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>高级查询</title>
    <!-- 清理缓存 -->
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/scripts/ext3.4.0/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/css/icon.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/scripts/dhtmlx/dhtmlxtree.css">
    <script type="text/javascript" src="${pageContext.request.contextPath}/public/scripts/dhtmlx/dhtmlxcommon.js"></script>

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
        var ntype = 'public',obj_cate_id = null,pageindex='1',page = 'bsc_public_measure';
    </script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/Map.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/Selection.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/tree_search.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/AsyncTree.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/ListPanel.js"></script>


    <script type="text/javascript" src="${pageContext.request.contextPath}/bsc/scripts/bsc_measure_search_layout.js"></script>

</head>
<body>
<!-- 导出Excel文件Form -->
<form id="excelForm" name="excelForm" action="bscresult_exportBscMeasureDhtmlByConf.action" method="post" target="">
    <input type="hidden" name="project_id" />
    <input type="hidden" name="role_id" />
    <input type="hidden" name="month_id" />
    <input type="hidden" name="file_name" />
    <input type="hidden" name="title" />
    <input type="hidden" name="obj_cate_id" />
    <input type="hidden" name="project_name" />
    <input type="hidden" name="month_name" />
    <input type="hidden" name="measure_id"/>
    <input type="hidden" name="show_id"/>
    <input type="hidden" name="obj_id"/>
    <input type="hidden" name="time_id"/>
    <input type="hidden" name="zone_id"/>
    <input type="hidden" name="pageindex">
    <input type="hidden" name="keyword">
    <input type="hidden" name="measure_name">
    <input type="hidden" name="source_type_id">
    <input type="hidden" name="source_type_name">
    <input type="hidden" name="measure_source">
    <input type="hidden" name="objSourceName">
    <input type="hidden" name="period">
    <input type="hidden" name="objPeriodName">
    <input type="hidden" name="is_private">
</form>
</body>
</html>
