package com.rx.system.bsc.action;

import static com.rx.system.util.CommonUtil.getCurrentDateString;

import org.apache.axis2.databinding.types.xsd.DateTime;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Now;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.awt.Dimension;
import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.*;

import com.rx.framework.jdbc.SupportedJdbcManager;
import com.rx.system.bsc.dao.BscResultDao;
import com.rx.system.bsc.dao.DimLinkDao;
import com.rx.system.bsc.service.impl.BscResultServiceImpl;
import com.rx.system.domain.BscMeasure;
import org.apache.http.entity.ContentType;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.ServletActionContext;

import com.rx.framework.jdbc.JdbcManager;
import com.rx.log.annotation.FunDesc;
import com.rx.log.annotation.UseLog;
import com.rx.system.base.BaseDispatchAction;
import com.rx.system.bsc.service.IBscResultService;
import com.rx.system.constant.Constant;
import com.rx.system.fusionchart.DashBord;
import com.rx.system.fusionchart.FusionChart;
import com.rx.system.fusionchart.IChart;
import com.rx.system.fusionchart.TargetLine;
import com.rx.system.model.excel.utils.ExcelField;
import com.rx.system.model.excel.utils.ExcelUtil;
import com.rx.system.service.ISelectorService;
import com.rx.system.table.DhtmlTableTemplate;
import com.rx.system.table.ITableTemplate;
import org.apache.struts2.views.jsp.ui.SelectTag;
import org.jgroups.protocols.FILE_PING;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 平衡计分卡考核结果Action
 * @author chenxd
 *
 */
@SuppressWarnings("serial")
public class BscResultAction extends BaseDispatchAction {

	public static final int ROW_ACCESS_WINDOW_SIZE = 100;
	
	private IBscResultService bscResultService = null;
	private ISelectorService selectorService = null;
	
	private JdbcManager jdbcManager;
	
	public JdbcManager getJdbcManager() {
		return jdbcManager;
	}

	public void setJdbcManager(JdbcManager jdbcManager) {
		this.jdbcManager = jdbcManager;
	}

	/**
	 * 根据结果数据展示为fushionchart图形
	 * @return
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0019")
	@UseLog
	public String chart() throws Exception {
		Map<String, Object> paramMap = getRequestParam(request);
		try {
			DashBord bord = new DashBord();
			int width = Integer.parseInt(getStringValue(paramMap, "width"));
			int height = Integer.parseInt(getStringValue(paramMap, "height"));
			bord.setDimension(new Dimension(width,height));
			//积分前十对象图标
			FusionChart topColumnChart = new FusionChart();
			List<Map<String, Object>> topList = this.bscResultService. getTopPoint(paramMap);
			topColumnChart.setDataSet(topList);
			topColumnChart.setLabelName("object_name");
			topColumnChart.setValueKey("value");
			topColumnChart.setChartType(IChart.CHART_TYPE_COLUMN3D);
			TargetLine lineB = new TargetLine("2.5","合格值","009933");
			TargetLine lineA = new TargetLine("8","优秀值","009933");
			topColumnChart.addTargetLine(lineA);
			topColumnChart.addTargetLine(lineB);
			
			
			bord.add("积分排名", topColumnChart);
			
			FusionChart test = new FusionChart();
			test.setDataSet(topList);
			test.setLabelName("object_name");
			test.setValueKey("value");
			test.setChartType(IChart.CHART_TYPE_BAR2D);
			bord.add("等级分布", test);
			
			request.setAttribute("dashbord", bord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "showchart";
	}
	
	/**
	 * 发布结果
	 * @return
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0020")
	@UseLog
	public String publishResult() throws Exception {
		try {
			Map<String, Object> paramMap = getRequestParam(request);
			String canPublish = this.bscResultService.canPublish(paramMap);
			if(Integer.parseInt(canPublish) == 1){
				doFailureInfoResponse("该方案在所选月份已经发布!");
				return null;
			}else if(Integer.parseInt(canPublish) == 2){
				doFailureInfoResponse("所选月份并非该方案周期的期末月份，不允许发布!");
				return null;	
			}
			
			this.bscResultService.publishBscResult(paramMap);
			doSuccessInfoResponse("数据公布成功");
		} catch (Exception e) {
			e.printStackTrace();
			doFailureInfoResponse(e.getMessage());
		}
		return null;
	}
	
	//------------------------------------------ 考核对象结果明细方法 ------------------------------------------------------
	
	/**
	 * 查询考核结果对象明细 
	 */
	@FunDesc(code="BSC_0021")
	@UseLog
	public String dhtmlDetail() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			List<Map<String, Object>> dataList = this.bscResultService.getBscResultDetail(paramMap);
			
			ITableTemplate template = this.getDetailDhtmlConfig();
			
			template.setData(dataList);
			
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 返回考核结果对象明细图形 
	 * @return
	 * @throws Exception
	 */
	public String detailChart() throws Exception {
		return "showchart";
	}
	
	/**
	 * 获取查询配置模板
	 * @return
	 */
	private ITableTemplate getDetailDhtmlConfig() throws Exception{
		ITableTemplate template = new DhtmlTableTemplate();
		template.setHeader(new String[]{"考核维度,#cspan,#cspan,考核指标,指标定义,指标权重(%),计划值,实际值,得分"});
		template.setColumnAlign("center,center,center,center,center,right,right,right,right");
		template.setColumnType("ro,ro,ro,ro,ro,ro,ro,ro,ro");
		template.setColumnWidth("80,80,200,160,160,80,80,80,80");
		template.setColumnFormatType("0,0,0,0,0,2,2,2,2");
		template.setDataMapKey(new String[]{"calc_desc","prorate","plan_value","measure_value","measure_point"});
		template.setGroupFields("dim_id,measure_id");
		template.setLeftTreeShowType(ITableTemplate.LEFT_SHOW_TYPE_TABLE);
		template.isFilterBlank(true);
		
		return template;
	}

	
//***********************************************************************
	/**
	 * 返回DHtml表格结果数据
	 * @return
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0023")
	@UseLog
	public String scoreDhtml() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			this.insertPageParamToMap(paramMap);//插入分页信息
			//方案所有的指标
			List<Map<String, Object>> measureList = this.bscResultService.listProjectMeasure(paramMap);
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreResult(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();
			
			String[] mapKey = new String[measureList.size()+1];
			String header = "考核对象,";
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
				paramMap.put("measure_id", getStringValue(map, "measure_id"));
				List<Map<String, Object>> subMeasureList = this.bscResultService.listSubMeasure(paramMap);
				if(subMeasureList.size()>0){
					String param = "project_id=" + getStringValue(paramMap, "project_id") + 
							"&month_id=" + getStringValue(paramMap, "month_id") + 
							"&measure_id=" + getStringValue(map, "measure_id") +
							"&cycle_type_id=" + getStringValue(paramMap, "cycle_type_id") + 
							"&obj_cate_id=" + getStringValue(paramMap, "obj_cate_id") + 
							"&monthName=" + URLDecoder.decode(getStringValue(paramMap, "monthName"), "utf-8") + 
							"&projectName=" + URLDecoder.decode(getStringValue(paramMap, "projectName"), "utf-8");
					template.addHeaderHref(i+1, "bsc_proj_result_detail.jsp?"+param);
				}
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.useSerialNumber(true);
//			template.setUseCheck(true, 0);
			
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String getStringById(String id){
		String [] ids = id.split(",");
		String meaId = "";
		if(ids.length >0){
			for(String str :ids){
				meaId += "'"+str.trim()+"'".concat(",");
			}
		}else{
			meaId ="'"+id+"'";
		}

		meaId = meaId.lastIndexOf(",")>-1?meaId.substring(0,meaId.length() - 1):meaId;
		return meaId;
	}


	public  String getStringByList(List<Map<String, Object>> measureList){
		String ids = "";
		String str = "";
		for(Map<String, Object> mp :measureList){
			str = getStringValue(mp, "measure_id");
			ids += "'"+str+"'".concat(",");
		}
		ids = ids.lastIndexOf(",")>-1?ids.substring(0,ids.length() - 1):ids;

		return ids;
	}
	
	public  String getStringById(List<Map<String, Object>> measureList){
		String ids = "";
		for(Map<String, Object> mp :measureList){
			ids += getStringValue(mp, "measure_id").concat(".");
		}
		ids = ids.lastIndexOf(".")>-1?ids.substring(0,ids.length() - 1):ids;
		
		return ids;
	}


	/**
	 * 返回DHtml表格结果数据
	 * @return
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0023")
	@UseLog
	public String scoreDhtmlByCond() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		List<Map<String, Object>> measureList = null;
		try {
			this.insertPageParamToMap(paramMap);//插入分页信息
			//方案所有的指标
			String meaId = null;
			String measureId = paramMap.get("measure_id").toString();
			if(null != measureId && !"".equals(measureId)){
				meaId = getStringById(measureId);
				paramMap.put("meaId", meaId);
				measureList = this.bscResultService.listProjectMeasureByIndexId(paramMap);
			}else{
				measureList = this.bscResultService.listProjectMeasure(paramMap);
			}
			paramMap.put("measureList", measureList);
			String ids = this.getStringById(measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreResult(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();
			String[] mapKey = new String[measureList.size()+1];
			String header = "维度名称,";
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
				paramMap.put("measure_id", getStringValue(map, "measure_id"));

				List<Map<String, Object>> subMeasureList = this.bscResultService.listSubMeasure(paramMap);
				if(subMeasureList.size()>0){

					String param = "project_id=" + getStringValue(paramMap, "project_id") +
							"&month_id=" + getStringValue(paramMap, "month_id") +
							"&measure_id=" + getStringValue(map, "measure_id") +
							"&ids=" + ids +
							"&cycle_type_id=" + getStringValue(paramMap, "cycle_type_id") +
							"&obj_cate_id=" + getStringValue(paramMap, "obj_cate_id") +
							"&monthName=" + URLDecoder.decode(getStringValue(paramMap, "monthName"), "utf-8") +
							"&projectName=" + URLDecoder.decode(getStringValue(paramMap, "projectName"), "utf-8");
					template.addHeaderHref(i+1, "bsc_proj_result_index_detail.jsp?"+param);
				}
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.useSerialNumber(true);
//			template.setUseCheck(true, 0);

			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	/**
	 * 返回DHtml表格结果数据总数
	 * @return
	 * @throws Exception
	 */
	public String scoreDhtmlCountByCond() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String totalCount = this.bscResultService.listScoreResultCount(paramMap);
			String canPublish = this.bscResultService.canPublish(paramMap);
			doSuccessInfoResponse(totalCount + "," + canPublish);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	@FunDesc(code="BSC_0024")
	@UseLog
	public String exportScoreByCond() throws Exception {
		List<Map<String, Object>> measureList = null;
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String projcetName = getStringValue(paramMap, "project_name");
			String month_id = getStringValue(paramMap, "month_id");
			String month_name = month_id.substring(0, 4)+"年"+month_id.substring(4)+"月";
			//方案所有的指标
			String meaId = null;
			String measureId = paramMap.get("measure_id").toString();
			if(null != measureId && !"".equals(measureId)){
				 meaId = getStringById(measureId);
				 paramMap.put("meaId", meaId);
				 measureList = this.bscResultService.listProjectMeasureByIndexId(paramMap);
			}else{
				 measureList = this.bscResultService.listProjectMeasure(paramMap);
			}
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreTotalResult(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();
			
			String[] mapKey = new String[measureList.size()+1];
			String header = "维度名称,";
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
			}
			
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			
			template.setTitle(paramMap.get("title").toString()); 
			String []titiles = header.split(HEADER_SPLIT);
			template.setExcelInfoRow(new String[][] {
					{ "方案名称：", projcetName },
					{ "月份：", month_name },
					{ "指标名称：", titiles[1] }
					
			});
			
			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");
			String localFileName = webBasePath + Constant.FILE_DOWNLOAD_DIR + this.getCurrentUser().getUser_id()+".xls";
			template.writeToFile(new File(localFileName));
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	/**
	 * 钻取查询下级指标明细
	 * @return
	 * @throws Exception
	 */
	public String scoreDhtmlSub() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			
			this.insertPageParamToMap(paramMap);//插入分页信息
			//方案所有的指标
			List<Map<String, Object>> measureList = this.bscResultService.listSubMeasure(paramMap);
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreSubResult(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();
			
			String[] mapKey = new String[measureList.size()+1];
			String meaFlag = paramMap.get("title").toString();
			String header = "考核对象,";
			if(MEASURE_FLAG.equals(meaFlag)){
				 header = "维度名称,";
			}
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
				paramMap.put("measure_id", getStringValue(map, "measure_id"));
				List<Map<String, Object>> subMeasureList = this.bscResultService.listSubMeasure(paramMap);
				if(subMeasureList.size()>0){
					String param = "project_id=" + getStringValue(paramMap, "project_id") + 
							"&month_id=" + getStringValue(paramMap, "month_id") + 
							"&measure_id=" + getStringValue(map, "measure_id") +
							"&cycle_type_id=" + getStringValue(paramMap, "cycle_type_id") + 
							"&obj_cate_id=" + getStringValue(paramMap, "obj_cate_id") + 
							"&monthName=" + URLDecoder.decode(getStringValue(paramMap, "monthName"), "utf-8") + 
							"&projectName=" + URLDecoder.decode(getStringValue(paramMap, "projectName"), "utf-8");
					template.addHeaderHref(i+1, "bsc_proj_result_detail.jsp?"+param);
				}
			}
			
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.useSerialNumber(true);
//			template.setUseCheck(true, 0);
			
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 返回DHtml表格结果数据总数
	 * @return
	 * @throws Exception
	 */
	public String scoreDhtmlCount() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String totalCount = this.bscResultService.listScoreResultCount(paramMap);
			String canPublish = this.bscResultService.canPublish(paramMap);
			doSuccessInfoResponse(totalCount + "," + canPublish);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@FunDesc(code="BSC_0024")
	@UseLog
	public String exportScore() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String projcetName = getStringValue(paramMap, "project_name");
			String month_id = getStringValue(paramMap, "month_id");
			String month_name = month_id.substring(0, 4)+"年"+month_id.substring(4)+"月";
			//方案所有的指标
			List<Map<String, Object>> measureList = this.bscResultService.listProjectMeasure(paramMap);
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreTotalResult(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();
			
			String[] mapKey = new String[measureList.size()+1];
			String header = "考核对象,";
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			
			template.setTitle(paramMap.get("title").toString()); 
			template.setExcelInfoRow(new String[][] {
					{ "方案名称：", projcetName },
					{ "月份：", month_name } });
			
			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");
			String localFileName = webBasePath + Constant.FILE_DOWNLOAD_DIR + this.getCurrentUser().getUser_id()+".xls";
			template.writeToFile(new File(localFileName));
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public String exportScoreSub() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String projcetName = getStringValue(paramMap, "project_name");
			String month_id = getStringValue(paramMap, "month_id");
			String month_name = month_id.substring(0, 4)+"年"+month_id.substring(4)+"月";
			//方案所有的指标
			List<Map<String, Object>> measureList = this.bscResultService.listSubMeasure(paramMap);
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreSubResult(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();
			
			String[] mapKey = new String[measureList.size()+1];

			String meaFlag = paramMap.get("title").toString();
			String header = "考核对象,";
			if(MEASURE_FLAG.equals(meaFlag)){
				 header = "维度名称,";
			}
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			
			template.setTitle(paramMap.get("title").toString()); 
			template.setExcelInfoRow(new String[][] {
					{ "方案名称：", projcetName },
					{ "月份：", month_name } });
			
			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");
			String localFileName = webBasePath + Constant.FILE_DOWNLOAD_DIR + this.getCurrentUser().getUser_id()+".xls";
			template.writeToFile(new File(localFileName));
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	@FunDesc(code="BSC_0025")
	@UseLog
	public String scoreChart() throws Exception {
		Map<String, Object> paramMap = getRequestParam(request);
		try {
			DashBord bord = new DashBord();
			int width = Integer.parseInt(getStringValue(paramMap, "width"));
			int height = Integer.parseInt(getStringValue(paramMap, "height"));
			bord.setDimension(new Dimension(width,height));
			//积分前十对象图标
			FusionChart topColumnChart = new FusionChart();
			List<Map<String, Object>> topList = this.bscResultService.getScoreTopPoint(paramMap);
			topColumnChart.setDataSet(topList);
			topColumnChart.setLabelName("object_name");
			topColumnChart.setValueKey("score");
			topColumnChart.setChartType(IChart.CHART_TYPE_LINE);
			topColumnChart.setShowLabels(false);
			topColumnChart.setShowValues(false);
			topColumnChart.setChartBottomMargin("30");
			topColumnChart.setLineColor("#f47920");
			topColumnChart.setLineThickness("2");
			bord.add("得分分布", topColumnChart);
			
			
			List<Map<String, Object>> levelCountList = this.bscResultService.getLevelList(paramMap);
			FusionChart level = new FusionChart();
			level.setDataSet(levelCountList);
			level.setLabelName("level_id");
			level.setValueKey("level_count");
			level.setChartType(IChart.CHART_TYPE_PIE3D);
			bord.add("等级分布", level);
			
			request.setAttribute("dashbord", bord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "showchart";
	}

	/**
	 * 返回DHtml表格结果数据
	 * @return
	 *
	 * 统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
	 *
	 * 统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0023")
	@UseLog
	public String scoreDhtmlByCondExt() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		String show_id = getStringValue(paramMap, "show_id");
		List<Map<String, Object>> measureList;
		List<Map<String, Object>> dataList;
		try {
			this.insertPageParamToMap(paramMap);//插入分页信息
			//是否过滤维度
			String oId = null;
			String objId = getStringValue(paramMap, "obj_id");
			if(null != objId && !"".equals(objId)){
				oId = getStringById(objId);
				paramMap.put("oId", oId);
			}
			//是否过滤时间
			String tId = null;
			String timId = getStringValue(paramMap, "time_id");
			if(null != timId && !"".equals(timId)){
				tId = getStringById(timId);
				paramMap.put("tId", tId);
			}
			//方案所有的指标  1, 多统计维度；2, 多统计年份
			String meaId = null;
			String measureId = paramMap.get("measure_id").toString();
			if(null != measureId && !"".equals(measureId)){
				meaId = getStringById(measureId);
				paramMap.put("meaId", meaId);
				measureList = this.bscResultService.listProjectMeasureByIndexId(paramMap);
			}else{
				measureList = this.bscResultService.listProjectMeasure(paramMap);
			}
			paramMap.put("measureList", measureList);

//			//调用归档历史数据->产生维度参数表
//			String monthID = getStringValue(paramMap, "month_id");
//			String projectID = getStringValue(paramMap, "project_id");
//			this.jdbcManager.execute("call pbsc_proj_obj_h('"+(monthID.length()==4?monthID+"01":monthID)+"','"+projectID+"')");

			String ids = this.getStringById(measureList);
			//2统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
			//1统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
			if(show_id.equals("1")){
				dataList = this.bscResultService.listScoreResultByYear(paramMap);
			}else{
				dataList = this.bscResultService.listScoreResult(paramMap);
			}

			ITableTemplate template = new DhtmlTableTemplate();
			String[] mapKey = new String[measureList.size()+2];
			String header = "维度名称,年份,";
			String columnAlign = "center,center,";
			String columnType = "ro,ro,";
			String columnWidth = "150,140,";
			String formatType = "0,0,";
			mapKey[0] = "object_name";
			mapKey[1] = "month_name";
			if(show_id.equals("2")){
				header = "年份,维度名称,";
				columnWidth = "140,150,";
				mapKey[0] = "month_name";
				mapKey[1] = "object_name";
			}
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+2] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "center";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
				paramMap.put("measure_id", getStringValue(map, "measure_id"));

				List<Map<String, Object>> subMeasureList = this.bscResultService.listSubMeasure(paramMap);
				if(subMeasureList.size()>0){

					String param = "project_id=" + getStringValue(paramMap, "project_id") +
							"&month_id=" + getStringValue(paramMap, "month_id") +
							"&measure_id=" + getStringValue(map, "measure_id") +
							"&ids=" + ids +
							"&cycle_type_id=" + getStringValue(paramMap, "cycle_type_id") +
							"&obj_cate_id=" + getStringValue(paramMap, "obj_cate_id") +
							"&obj_id=" + getStringValue(paramMap, "obj_id").replace(",",".") +
							"&show_id=" + getStringValue(paramMap, "show_id") +
							"&time_id=" + getStringValue(paramMap, "time_id").replace(",",".") +
							"&monthName=" + URLDecoder.decode(getStringValue(paramMap, "monthName"), "utf-8") +
							"&projectName=" + URLDecoder.decode(getStringValue(paramMap, "projectName"), "utf-8");
					template.addHeaderHref(i+2, "bsc_proj_obj_index_score_ext_detail.jsp?"+param);
				}
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.useSerialNumber(true);
//			template.setUseCheck(true, 0);

			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 返回DHtml表格结果数据总数
	 * @return
	 * @throws Exception
	 */
	public String scoreDhtmlCountByCondExt() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		String timeId = getStringValue( paramMap,"time_id");
		if(null != timeId && !"".equals(timeId)){
			timeId = getStringById(timeId);
			paramMap.put("tId", timeId);
		}
		//是否过滤维度
		String oId = null;
		String objId = getStringValue(paramMap, "obj_id");
		if(null != objId && !"".equals(objId)){
			oId = getStringById(objId);
			paramMap.put("oId", oId);
		}
		try {
			String totalCount = this.bscResultService.listScoreResultCountExt(paramMap);
			doSuccessInfoResponse(totalCount + ",0");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@FunDesc(code="BSC_0024")
	@UseLog
	public String exportScoreByCondExt() throws Exception {
		List<Map<String, Object>> measureList = null;

		Map<String, Object> paramMap = this.getRequestParam(request);
		String show_id = getStringValue(paramMap, "show_id");
		try {
			String projcetName = getStringValue(paramMap, "project_name");
			String month_id = getStringValue(paramMap, "month_id");
			String month_name = "";
			if(null !=month_id && !"".equals(month_id)){
				month_name = month_id.substring(0, 4)+"年"+month_id.substring(4)+"月";
			}

			//是否过滤维度
			String oId = null;
			String objId = getStringValue(paramMap, "obj_id");
			if(null != objId && !"".equals(objId)){
				oId = getStringById(objId);
				paramMap.put("oId", oId);

			}

			//是否过滤时间
			String tId = null;
			String timId = getStringValue(paramMap, "time_id");
			if(null != timId && !"".equals(timId)){
				tId = getStringById(timId);
				paramMap.put("tId", tId);
			}
			//方案所有的指标
			String meaId = null;
			String measureId = paramMap.get("measure_id").toString();
			if(null != measureId && !"".equals(measureId)){
				meaId = getStringById(measureId);
				paramMap.put("meaId", meaId);
				measureList = this.bscResultService.listProjectMeasureByIndexId(paramMap);
			}else{
				measureList = this.bscResultService.listProjectMeasure(paramMap);
			}
			paramMap.put("measureList", measureList);
			//2统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
			//1统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
			List<Map<String, Object>> dataList = null;
			if(show_id.equals("1")){
//				dataList = this.bscResultService.listScoreTotalResultByObj(paramMap);
				dataList = this.bscResultService.listScoreResultByYear(paramMap);
			}else{
				dataList = this.bscResultService.listScoreTotalResultByObj(paramMap);
//				dataList = this.bscResultService.listScoreTotalResultByYear(paramMap);
			}


			ITableTemplate template = new DhtmlTableTemplate();

			String[] mapKey = new String[measureList.size()+2];
			String header = "维度名称,年份,";
			String columnAlign = "center,center,";
			String columnType = "ro,ro,";
			String columnWidth = "150,140,";
			String formatType = "0,0,";
			mapKey[0] = "object_name";
			mapKey[1] = "month_name";
			if(show_id.equals("2")){
				header = "年份,维度名称,";
				columnWidth = "140,150,";
				mapKey[0] = "month_name";
				mapKey[1] = "object_name";
			}
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+2] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "center";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
			}

			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.setTitle(paramMap.get("title").toString());
			String []titiles = header.split(HEADER_SPLIT);
			String objName = "";
			if(show_id.equals("2")){
				template.setExcelInfoRow(new String[][] {
						{ "方案名称：", projcetName },
						{ "统计年份：", month_name }
//						{ "指标名称：", titiles[1] }

				});
			}else{
				List<Map<String, Object>> objectList  = this.bscResultService.getObectNameByObjId(paramMap);
				if(null !=objectList && objectList.size()>0){
					Map<String, Object> mp = objectList.get(0);
					objName = mp.get("obj_name").toString();
				}
				template.setExcelInfoRow(new String[][] {
						{ "方案名称：", projcetName },
						{ "统计维度：", objName }
//						{ "指标名称：", titiles[1] }

				});
			}

			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");
			String localFileName = webBasePath + Constant.FILE_DOWNLOAD_DIR + this.getCurrentUser().getUser_id()+".xls";
			template.writeToFile(new File(localFileName));
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String exportScoreSubExt() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String projcetName = getStringValue(paramMap, "project_name");
			String month_id = getStringValue(paramMap, "month_id");
			String month_name = month_id.substring(0, 4)+"年"+month_id.substring(4)+"月";
			String show_id = getStringValue(paramMap, "show_id");
			//方案所有的指标
			List<Map<String, Object>> measureList = this.bscResultService.listSubMeasure(paramMap);
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreSubResultExt(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();

			String[] mapKey = new String[measureList.size()+1];

			String meaFlag = paramMap.get("title").toString();
			String header = "考核对象,";
			if(MEASURE_FLAG.equals(meaFlag)){
				header = "维度名称,";
			}
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			if(show_id.equals("2")){
				header = "年份,";
				columnWidth = "200,";
				mapKey[0] = "month_name";
			}
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);

			template.setTitle(paramMap.get("title").toString());
			template.setExcelInfoRow(new String[][] {
					{ "方案名称：", projcetName },
					{ "月份：", month_name } });

			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");
			String localFileName = webBasePath + Constant.FILE_DOWNLOAD_DIR + this.getCurrentUser().getUser_id()+".xls";
			template.writeToFile(new File(localFileName));
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 钻取查询下级指标明细
	 * @return
	 * @throws Exception
	 */
	public String scoreDhtmlSubExt() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			this.insertPageParamToMap(paramMap);//插入分页信息
			String show_id = getStringValue(paramMap, "show_id");
			//方案所有的指标
			List<Map<String, Object>> measureList = this.bscResultService.listSubMeasure(paramMap);
			paramMap.put("measureList", measureList);
			List<Map<String, Object>> dataList = this.bscResultService.listScoreSubResultExt(paramMap);
			ITableTemplate template = new DhtmlTableTemplate();

			String[] mapKey = new String[measureList.size()+1];
			String meaFlag = paramMap.get("title").toString();
			String header = "考核对象,";
			if(MEASURE_FLAG.equals(meaFlag)){
				header = "维度名称,";
			}
			String columnAlign = "left,";
			String columnType = "ro,";
			String columnWidth = "260,";
			String formatType = "0,";
			mapKey[0] = "object_name";
			if(show_id.equals("2")){
				header = "年份,";
				columnWidth = "200,";
				mapKey[0] = "month_name";
			}
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+1] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "right";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
				paramMap.put("measure_id", getStringValue(map, "measure_id"));
				List<Map<String, Object>> subMeasureList = this.bscResultService.listSubMeasure(paramMap);
				if(subMeasureList.size()>0){
					String param = "project_id=" + getStringValue(paramMap, "project_id") +
							"&month_id=" + getStringValue(paramMap, "month_id") +
							"&measure_id=" + getStringValue(map, "measure_id") +
							"&cycle_type_id=" + getStringValue(paramMap, "cycle_type_id") +
							"&obj_cate_id=" + getStringValue(paramMap, "obj_cate_id") +
							"&show_id=" + getStringValue(paramMap, "show_id") +
							"&obj_id=" + getStringValue(paramMap, "obj_id") +
							"&time_id=" + getStringValue(paramMap, "time_id") +
							"&monthName=" + URLDecoder.decode(getStringValue(paramMap, "monthName"), "utf-8") +
							"&projectName=" + URLDecoder.decode(getStringValue(paramMap, "projectName"), "utf-8");
					template.addHeaderHref(i+1, "bsc_proj_obj_index_score_ext_detail.jsp?"+param);
				}
			}

			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.useSerialNumber(true);
//			template.setUseCheck(true, 0);

			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	/**
	 * 返回DHtml表格结果数据
	 * @return
	 * 增加地区代码
	 *
	 * 统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
	 *
	 * 统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0023")
	@UseLog
	public String getResultDhtmlByCondExt() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		String show_id = getStringValue(paramMap, "show_id");
		List<Map<String, Object>> measureList;
		List<Map<String, Object>> dataList;
		try {
			this.insertPageParamToMap(paramMap);//插入分页信息
			//地区代码
			String zId = "";
			String zoneId = getStringValue(paramMap, "zone_id");
			if(null != zoneId && !"".equals(zoneId)){
				zId = getStringById(zoneId);
			}
			paramMap.put("zId", zId);
			//是否过滤维度
			String oId = "";
			String objId = getStringValue(paramMap, "obj_id");
			if(null != objId && !"".equals(objId)){
				oId = getStringById(objId);
			}
			paramMap.put("oId", oId);
			//是否过滤时间
			String tId = "";
			String timId = getStringValue(paramMap, "time_id");
			if(null != timId && !"".equals(timId)){
				tId = getStringById(timId);
			}
			paramMap.put("tId", tId);
			//方案所有的指标  1, 多统计维度；2, 多统计年份
			String meaId = "";
			String measureId = paramMap.get("measure_id").toString();
			if(null != measureId && !"".equals(measureId)){
				meaId = getStringById(measureId);
				paramMap.put("meaId", meaId);
//				measureList = this.bscResultService.getProjectResultMeasureByIndexId(paramMap);
				measureList = this.bscResultService.listProjectMeasureByIndexId(paramMap);
			}else{
				measureList = this.bscResultService.getProjectResultMeasure(paramMap);
//				measureList = this.bscResultService.listProjectMeasure(paramMap);
			}
			paramMap.put("measureList", measureList);

			String ids = this.getStringByList(measureList);
			paramMap.put("ids", ids);
			//增加地区代码： 2统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
			//增加地区代码： 1统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
			if(show_id.equals("1")){
				dataList = this.bscResultService.getResultDhtmlYearByParam(paramMap);
			}else{
				dataList = this.bscResultService.getResultDhtmlOjbectByParam(paramMap);
			}

			ITableTemplate template = new DhtmlTableTemplate();
			String[] mapKey = new String[measureList.size()+3];
			String header = "地区名称,维度名称,年份,";
			String columnAlign = "center,center,center,";
			String columnType = "ro,ro,ro,";
			String columnWidth = "260,150,140,";
			String formatType = "0,0,0,";
			mapKey[0] = "zone_cd_desc";
			mapKey[1] = "object_name";
			mapKey[2] = "month_name";
			if(show_id.equals("2")){
				header = "地区名称,年份,维度名称,";
				columnWidth = "200,140,150,";
				mapKey[0] = "zone_cd_desc";
				mapKey[1] = "month_name";
				mapKey[2] = "object_name";
			}
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+3] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "left";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
				paramMap.put("measure_id", getStringValue(map, "measure_id"));

				/*List<Map<String, Object>> subMeasureList = this.bscResultService.listSubMeasure(paramMap);
				if(subMeasureList.size()>0){

					String param = "project_id=" + getStringValue(paramMap, "project_id") +
							"&month_id=" + getStringValue(paramMap, "month_id") +
							"&measure_id=" + getStringValue(map, "measure_id") +
							"&ids=" + ids +
							"&cycle_type_id=" + getStringValue(paramMap, "cycle_type_id") +
							"&obj_cate_id=" + getStringValue(paramMap, "obj_cate_id") +
							"&obj_id=" + getStringValue(paramMap, "obj_id").replace(",",".") +
							"&show_id=" + getStringValue(paramMap, "show_id") +
							"&time_id=" + getStringValue(paramMap, "time_id").replace(",",".") +
							"&monthName=" + URLDecoder.decode(getStringValue(paramMap, "monthName"), "utf-8") +
							"&projectName=" + URLDecoder.decode(getStringValue(paramMap, "projectName"), "utf-8");
					template.addHeaderHref(i+3, "bsc_proj_obj_index_score_ext_detail.jsp?"+param);
				}*/
			}
			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			template.useSerialNumber(true);
//			template.setUseCheck(true, 0);

			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(template.getTableString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 返回DHtml表格结果数据总数
	 * @return
	 * @throws Exception
	 */
	public String getResultDhtmlCountByCondExt() throws Exception {
		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			String zId = "";
			String zoneId = getStringValue(paramMap, "zone_id");
			if(null != zoneId && !"".equals(zoneId)){
				zId = getStringById(zoneId);
			}
			paramMap.put("zId", zId);
			//是否过滤维度
			String oId = "";
			String objId = getStringValue(paramMap, "obj_id");
			if(null != objId && !"".equals(objId)){
				oId = getStringById(objId);
			}
			paramMap.put("oId", oId);
			//是否过滤时间
			String tId = "";
			String timId = getStringValue(paramMap, "time_id");
			if(null != timId && !"".equals(timId)){
				tId = getStringById(timId);
			}
			paramMap.put("tId", tId);
			String totalCount = this.bscResultService.getResultDhtmlCountByCondExt(paramMap);
			doSuccessInfoResponse(totalCount + ",0");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@FunDesc(code="BSC_0024")
	@UseLog
	public String exportResultDhtmlByCondExt() throws Exception {
		List<Map<String, Object>> measureList = null;
		Map<String, Object> paramMap = this.getRequestParam(request);
		String show_id = getStringValue(paramMap, "show_id");
		String month_name = "";
		try {
			String projcetName = getStringValue(paramMap, "project_name");
			String month_id = getStringValue(paramMap, "month_id");
			if(month_id !=null && !"".equals(month_id)){
				 month_name = month_id.substring(0, 4)+"年";
			}

			//地区代码
			String zId = "";
			String zoneId = getStringValue(paramMap, "zone_id");
			if(null != zoneId && !"".equals(zoneId)){
				zId = getStringById(zoneId);
			}
			paramMap.put("zId", zId);
			List<Map<String, Object>>  zoneList = this.bscResultService.getZoneNameByZoneID(paramMap);
			String zoneNm = getStringById(zoneList);

			//是否过滤维度
			String oId = "";
			String objId = getStringValue(paramMap, "obj_id");
			if(null != objId && !"".equals(objId)){
				oId = getStringById(objId);
			}
			paramMap.put("oId", oId);
			//是否过滤时间
			String tId = "";
			String timId = getStringValue(paramMap, "time_id");
			if(null != timId && !"".equals(timId)){
				tId = getStringById(timId);

			}
			paramMap.put("tId", tId);
			//方案所有的指标
			String meaId = null;
			String measureId = paramMap.get("measure_id").toString();
			if(null != measureId && !"".equals(measureId)){
				meaId = getStringById(measureId);
				paramMap.put("meaId", meaId);
				measureList = this.bscResultService.listProjectMeasureByIndexId(paramMap);
			}else{
				measureList = this.bscResultService.getProjectResultMeasure(paramMap);
			}
			paramMap.put("measureList", measureList);

			String ids = this.getStringByList(measureList);
			paramMap.put("ids", ids);
			List<Map<String, Object>> dataList = null;
			//增加地区代码： 2统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
			//增加地区代码： 1统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
			if(SHOW_FLAG.equals(show_id)){
				dataList = this.bscResultService.getResultDhtmlYearByParamInfo(paramMap);
			}else{
				dataList = this.bscResultService.getResultDhtmlOjbectByParamInfo(paramMap);
			}
			ITableTemplate template = new DhtmlTableTemplate();
			String[] mapKey = new String[measureList.size()+3];
			String header = "地区名称,维度名称,年份,";
			String columnAlign = "center,center,center,";
			String columnType = "ro,ro,ro,";
			String columnWidth = "260,150,140,";
			String formatType = "0,0,0,";
			mapKey[0] = "zone_cd_desc";
			mapKey[1] = "object_name";
			mapKey[2] = "month_name";
			if(show_id.equals("2")){
				header = "地区名称,年份,维度名称,";
				columnWidth = "200,140,150,";
				mapKey[0] = "zone_cd_desc";
				mapKey[1] = "month_name";
				mapKey[2] = "object_name";
			}
			for (int i = 0; i < measureList.size(); i++) {
				Map<String,Object> map = measureList.get(i);
				mapKey[i+3] = "col_"+i;
				header += getStringValue(map, "mea_definition");
				columnAlign += "center";columnType += "ro";columnWidth += "120";formatType += "2";
				if(i != measureList.size()-1){
					header += ",";columnAlign += ",";columnType += ",";columnWidth += ",";formatType += ",";
				}
			}

			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);

			template.setTitle(paramMap.get("title").toString());
			String []titiles = header.split(HEADER_SPLIT);

			/**
			 * 1统计维度-》统计维度（单）-》 统计年份（多）-》指标（多）
			 * 	2统计年份 -》  统计年份（单） -》统计维度（多）-》指标（多）
			 * 			showID
			 * 			年份单选monthSelector1   多monthSelector2（monthBox2）
			 * 			统计维度单选 objSelector2 （objBox2）      多objSelector1
			 */

			String objName = "";
			if(show_id.equals("2")){
				template.setExcelInfoRow(new String[][] {
						{ "方案名称：", projcetName },
						{ "统计年份：", month_name }
				});
			}else{
				List<Map<String, Object>> objectList  = this.bscResultService.getObectNameByDimId(paramMap);
				if(null !=objectList && objectList.size()>0){
					Map<String, Object> mp = objectList.get(0);
					objName = mp.get("dim_cd_desc").toString();
				}
				template.setExcelInfoRow(new String[][] {
						{ "方案名称：", projcetName },
						{ "统计维度：", objName }
				});
			}
			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");

			if(measureList.size() < 250){
				String localFileName = webBasePath + Constant.FILE_DOWNLOAD_DIR + this.getCurrentUser().getUser_id()+".xls";
				template.writeToFile(new File(localFileName));
			}else{
				String exportHeader = "";
				if(show_id.equals("2")){
					exportHeader = "方案名称:"+projcetName+",统计年份:"+month_name;
				}else{
					exportHeader = "方案名称:"+projcetName+",统计维度:"+objName;
				}
				String [] titles  = header.split(",");			
				List<ExcelField>  excelFields = getExcelFields(show_id,titles);

				String title= projcetName;
				SXSSFWorkbook wb = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
				ExcelUtil excelUtil = new ExcelUtil(wb,excelFields,title,exportHeader);
				int startNum = 0;
				excelUtil.exportXLSX(wb,dataList,startNum);
				String fileName = projcetName+".xlsx";
				response.reset();

				response.setContentType("application/x-download");
				response.setCharacterEncoding("UTF-8");
				fileName = new String(fileName.getBytes(),"iso-8859-1");
				response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
				OutputStream out = response.getOutputStream();
				try {				
					wb.write(out);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {				
					if (out != null) {
						try {
							out.flush();
							out.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					wb.dispose();
				}
			}
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String args[]){
		ApplicationContext ioc = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext.xml");
		JdbcTemplate template= (JdbcTemplate)ioc.getBean("jdbcTemplate");
		BscResultAction bscResultAction = new BscResultAction();
		bscResultAction.setJdbcManager(new SupportedJdbcManager(template));
		BscResultServiceImpl bscResultService = new BscResultServiceImpl();
//		bscResultService.setBscResultDao((BscResultDao) ioc.getBean("bscResultDao"));
		bscResultAction.setBscResultService(bscResultService);
		try{
			bscResultAction.importMeasureFromExcel();
			bscResultAction.exportBscMeasureDhtmlByConf();
		}catch (Exception e){
			System.out.print(e);
		}
	}

	private String buildSearchMeasureSql(String columns) throws Exception {
		Map<String,Object> map = this.getRequestParam(request);
		String obj_cate_id = map.get("obj_cate_id").toString();
		String pageIndex = map.get("pageindex").toString();
		//搜索关键字
		String keyword = map.get("keyword") == null?null:map.get("keyword").toString();
		//指标id
		String measure_id = map.get("measure_id") == null?null:map.get("measure_id").toString();
		//指标名称
		String measure_name = map.get("measure_name") == null?null:map.get("measure_name").toString();
		//指标来源
		String measureSource = map.get("measure_source")==null?null:map.get("measure_source").toString();
		//指标分类
		String sourceTypeId = map.get("source_type_id")==null?null:map.get("source_type_id").toString();
		//周期
		String period = map.get("period")==null?null:map.get("period").toString();
		//维度
		String dimension = map.get("dimension")==null?null:map.get("dimension").toString();
		StringBuffer sb = new StringBuffer();
		sb.append("select  ");
		sb.append(columns);
		sb.append(" from bsc_measure where");
		//sb.append(" where (measure_name like '%"+map.get("keyword")+"%' or measure_id like '%"+map.get("keyword")+"%')");
		sb.append(" is_private = '"+map.get("is_private")+"'");
		if(!"".equals(keyword) && null != keyword){
			sb.append(" and (measure_name like '%"+map.get("keyword")+"%' or measure_id like '%"+map.get("keyword")+"%')");
		}
		if(!"".equals(measure_name) && null != measure_name){
			sb.append(" and measure_name like '%" + measure_name + "%'");
		}
		if(!"".equals(measure_id) && null != measure_id){
			sb.append(" and measure_id like '%" + measure_id + "%'");
		}
		if(!"".equals(obj_cate_id) && "2".equals(pageIndex)){
			sb.append(" and obj_cate_id = '"+obj_cate_id+"'");
		}
		if(!"".equals(measureSource) && null != measureSource){
			sb.append(" and measure_source = '"+measureSource+"'");
		}
		if(!"".equals(sourceTypeId) && null != sourceTypeId){
			sb.append(" and source_type_id = '"+sourceTypeId+"'");
		}
		if(!"".equals(period) && null != period){
			sb.append(" and countperiod = '"+period+"'");
		}
		if(!"".equals(dimension) && null != dimension){
			sb.append(" and( districtobjecttable = '"+dimension+"' or otherobjecttable ='" +dimension+"') ");
		}
		sb.append(" order by global_order_id");

		return sb.toString();
	}

	@FunDesc(code="BSC_0025")
	@UseLog
	public String exportBscMeasureDhtmlByConf() throws Exception {
		List<Map<String, Object>> measureList = null;
		Map<String, Object> paramMap = null;
		String sqlQuery = null;
		if (request != null){
			paramMap = this.getRequestParam(request);
			sqlQuery = buildSearchMeasureSql(" * ");
		}
		try {
			List<Map<String, Object>> dataList = selectorService.queryForList(sqlQuery);
			ITableTemplate template = new DhtmlTableTemplate();
			String[] mapKey = new String[14];

			String columnType = "ro,ro,ro,ro,ro,ro,ro,ro,ro,ro,ro,ro,ro,ro,";
			String formatType = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,";
			String header = "指标代码,指标父代码,指标名称,指标描述,指标类型,指标源表,指标公式表达式," +
					"指标单位,指标来源,来源名称,统计周期,预警指标类型,地区维度表名,其它维度表名,";
			String columnWidth = "140,140,180,260,90,130,180," +
					"80,80,160,80,80,140,140,";
			String columnAlign = "center,center,center,center,center,center,center," +
					"center,center,center,center,center,center,center,";
			mapKey[0] = "measure_id";
			mapKey[1] = "parent_measure_id";
			mapKey[2] = "measure_name";
			mapKey[3] = "measure_desc";
			mapKey[4] = "source_type_id";
			mapKey[5] = "source_id";
			mapKey[6] = "formula_expr";
			mapKey[7] = "measure_unit";
			mapKey[8] = "measure_source";
			mapKey[9] = "measure_source_desc";
			mapKey[10] = "countperiod";
			mapKey[11] = "alerttype";
			mapKey[12] = "districtobjecttable";
			mapKey[13] = "otherobjecttable";

			template.setHeader(new String[]{header});
			template.setColumnAlign(columnAlign);
			template.setColumnType(columnType);
			template.setColumnWidth(columnWidth);
			template.setColumnFormatType(formatType);
			template.setDataMapKey(mapKey);
			template.setData(dataList);
			if(paramMap != null)
				template.setTitle(paramMap.get("title").toString());
			else
				template.setTitle("");
			String []titiles = header.split(HEADER_SPLIT);

			String objName = "指标明细导出";


//			String webBasePath = ServletActionContext.getServletContext().getRealPath("/");
			String projcetName = "指标查询结果导出";
			String conf = "";
			//指标id
			if( paramMap.get("measure_id") != null && !"".equals(paramMap.get("measure_id").toString()))
				conf += "[指标Id]"+paramMap.get("measure_id").toString().concat("|");
			//指标名称
			if( paramMap.get("measure_name") != null && !"".equals(paramMap.get("measure_name").toString()))
				conf += "[指标名称]"+paramMap.get("measure_name").toString().concat("|");
			//指标来源
			if(paramMap.get("objSourceName")!=null && !"".equals(paramMap.get("objSourceName").toString()))
				conf += "[指标来源]"+ paramMap.get("objSourceName").toString().concat("|");
			//指标分类
			if(paramMap.get("source_type_name")!=null && !"".equals(paramMap.get("source_type_name").toString()))
				conf += "[指标分类]"+ paramMap.get("source_type_name").toString().concat("|");
			//周期
			if( paramMap.get("objPeriodName")!=null && !"".equals(paramMap.get("objPeriodName").toString()))
				conf += "[周期]"+ paramMap.get("objPeriodName").toString();
			if(conf.lastIndexOf("|")==conf.length()-1)
				conf = conf.substring(0,conf.length() -1);
			String exportHeader = "";
			exportHeader = "条件:"+conf+","+"日期:"+ DateFormat.getDateInstance().format(new Date());
			String [] titles  = header.split(",");
			List<ExcelField>  excelFields = getExcelFields(titles,mapKey);

			String title= "指标明细导出";
			SXSSFWorkbook wb = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
			ExcelUtil excelUtil = new ExcelUtil(wb,excelFields,title,exportHeader);
			int startNum = 0;
			excelUtil.exportXLSX(wb,dataList,startNum);
			String fileName = projcetName+".xlsx";
			response.reset();

			response.setContentType("application/x-download");
			response.setCharacterEncoding("UTF-8");
			fileName = new String(fileName.getBytes(),"iso-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			OutputStream out = response.getOutputStream();
//			FileOutputStream out = new FileOutputStream(
//					"D:\\泰豪\\git\\new\\beijing-tellhow-v2\\bsbProd-beijing\\src\\main\\webapp\\"
//							+ Constant.FILE_DOWNLOAD_DIR +
//							DateFormat.getDateInstance().format(new Date())+"_指标导出.xlsx");
			try {
				wb.write(out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.flush();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				wb.dispose();
			}
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<ExcelField> getExcelFields(String [] titles,String [] values) {
		List<ExcelField> excelFields = new ArrayList();
		if(titles.length == values.length )
			for(int i=0;i<titles.length;i++){
				excelFields.add(new ExcelField(titles[i], values[i]));
			}
		return excelFields;
	}

	@FunDesc(code="BSC_0026")
	@UseLog
	public String importBscMeasureTemplateDownload() throws Exception {
		String webBasePath = "";
		String filename = "指标导入模板.xlsx";
		try{
			ServletActionContext.getServletContext();
			webBasePath = ServletActionContext.getServletContext().getRealPath("/");
		}catch (NullPointerException nullEx){
			webBasePath = "D:\\泰豪\\git\\new\\beijing-tellhow-v2\\bsbProd-beijing\\src\\main\\webapp\\";
		}
		if("".equals(webBasePath))
			webBasePath = "D:\\泰豪\\git\\new\\beijing-tellhow-v2\\bsbProd-beijing\\src\\main\\webapp\\";

		String localFileName =  webBasePath + Constant.FILE_UPLOAD_DIR +"\\"+filename;

		File fileSave = new File(localFileName);
		FileInputStream fileInputStream = new FileInputStream(fileSave);

		Workbook wb = null;
		try{
			wb = new XSSFWorkbook(fileInputStream);
			response.reset();
			response.setContentType("application/x-download");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ java.net.URLEncoder.encode(filename, "utf-8"));
			OutputStream out = response.getOutputStream();
			try {
				wb.write(out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.flush();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
//				wb.destroy();
			}
			return "excelDownload";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private File file ;
	private String contentType;
	private String fileName;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private String processComplexMeausure(String formulaExp){
		String  markString = "[@";
		String  markEnd = "]";
		String exp = formulaExp;
		String ids = "";
		while(exp.indexOf(markString)>=0){
			String measureId = exp.substring(exp.indexOf(markString)+2,exp.indexOf(markEnd));
			if(measureId!=null && !"".equals(measureId))
				ids += measureId + ",";
			exp = exp.substring(exp.indexOf(markEnd)+1,exp.length());
		}
		return ids.length()>0?ids.substring(0,ids.length()-1):ids;
	}

	public String importMeasureFromExcel() throws Exception {
		String webBasePath = "";
		String filename = "指标导入模板上传.xlsx";
		Map <String,Object> paramMap = getRequestParam(request);

		try{
			ServletActionContext.getServletContext();
			webBasePath = ServletActionContext.getServletContext().getRealPath("/");
		}catch (NullPointerException nullEx){
			webBasePath = "D:\\泰豪\\git\\new\\beijing-tellhow-v2\\bsbProd-beijing\\src\\main\\webapp\\";
		}

		String localFileName =  webBasePath + Constant.FILE_UPLOAD_DIR +"\\"+DateFormat.getDateInstance().format(new Date())+"_"+filename;
//		//上传到服务器目录
		if(file.isFile()){
			//定义并初始化io流的读写操作
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			BufferedOutputStream bos = null;
			try {
				bos = new BufferedOutputStream(new FileOutputStream(localFileName));
				// 从源文件中取数据，写到目标文件中
				byte[] buff = new byte[8192];
				for (int len = -1; (len = bis.read(buff)) != -1;) {
					bos.write(buff, 0, len);
				}
				bos.flush();
			} catch (IOException ie) {
				ie.printStackTrace();
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException ie) {
						ie.printStackTrace();
					}
				}
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException ie) {
						ie.printStackTrace();
					}
				}
			}
		}
//		try{
//			//创建DiskFileItemFactory工厂
//			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
//			//创建文件上传解析器
//			ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
//			upload.setHeaderEncoding("UTF-8");
//			if(!ServletFileUpload.isMultipartContent(request)){
//				return;
//			}
//			//获取表单数据
//			List<FileItem> fileItems = upload.parseRequest(request);
//			Map itemMap = new HashMap();
//			for(FileItem fileItem :fileItems){
//				if(fileItem.isFormField()){
//
//				}
//			}
//		}catch (Exception e){
//			System.out.println(e.toString());
//		}


		File fileSave = new File(localFileName);
		FileInputStream fileInputStream = new FileInputStream(fileSave);
		MultipartFile multipartFile = new MockMultipartFile(fileSave.getName(),fileSave.getName(),
				ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

		int beginRowIndex = 4;
		int cellType = 1;
		Workbook wookbook = null;

		try{
			//wookbook = new HSSFWorkbook(fileInputStream);
			wookbook = new XSSFWorkbook(fileInputStream);
			cellType = HSSFCell.CELL_TYPE_STRING;
			List<BscMeasure> listMeasure = ExcelUtil.read2003Excel(multipartFile,beginRowIndex,BscMeasure.class);
		}catch (Exception e){
			try{
				fileInputStream =  new FileInputStream(fileSave);
//				wookbook = new HSSFWorkbook(fileInputStream);
				wookbook = new XSSFWorkbook(fileInputStream);
				cellType = HSSFCell.CELL_TYPE_STRING;
//				List<BscMeasure> listMeasure = ExcelUtil.read2003Excel(multipartFile,beginRowIndex,BscMeasure.class);
			}catch (Exception ex){
				LOG.debug(ex.toString());
			}

		}
		Sheet sheet = wookbook.getSheetAt(0);
		//开始处理上传行号-跳过标题和表头
		int startRowNum = 3;
		int startColumn = 1;
		//总数
		int totalRowNum = sheet.getLastRowNum();
		List<Map<Integer,String>> list = new ArrayList<Map<Integer,String>>();
		Map<Integer,String> map = null;
		for(int x = startRowNum ; x <= totalRowNum ; x++){
			boolean isExists = false;
			//取得行
			Row row = sheet.getRow(x);
			int cellLength = row.getLastCellNum();

			for(int y=startColumn;y<cellLength;y++){
				Cell cell = row.getCell(y);
				if(cell == null || cell.toString().equals("")){
					if(y==startColumn)
						break;
					map.put(y,"");
				}else {
					cell.setCellType(cellType);
					if(y==startColumn){
						isExists =  true;
						map = new HashMap<Integer,String>();
					}
					map.put(y, cell.getStringCellValue().toString());
				}
			}
			if(isExists)
				list.add(map);
		}
		//开始处理数据
		if(list.size()>0){
			int i = 1;
			List<String> listInsertCmd = new ArrayList<>();
			List<String> listInsertMeaRely = new ArrayList<>();
			String ids = "";
			for(Map m :list){
				ids += i++==list.size()?m.get(1).toString():m.get(1).toString()+",";
				//衍生指标
				if(getMeasureKey(m.get(4).toString()).equals("01")){
					if(m.get(6)!=null && !"".equals(m.get(6).toString())){
						String measureIds = processComplexMeausure(m.get(6).toString());
						if(measureIds.length()>0){
							String [] strIds = measureIds.split(",");
							for(String s :strIds){
								listInsertMeaRely.add("insert into bsc_measure_exe(measure_id,rely_measure_id)values('"+m.get(1)+
										"','"+s +"')");
							}
						}
					}
				}
				String insertSql
						= "insert into bsc_measure "+
						"(measure_id," +
						"parent_measure_id," +
						"measure_name," +
						"source_type_id," +
						"source_id," +
						"formula_expr," +
						"is_private," +
						"obj_cate_id," +
						"owner_org_id," +
						"measure_desc," +
						"measure_unit," +
						"measure_source," +
						"measure_source_desc," +
						"countperiod," +
//						"districtdimension," +
//						"districtdimension_desc," +
//						"ohterdimension," +
//						"ohterdimension_desc," +
						"districtobjecttable," +
						"otherobjecttable," +
						"alerttype" +
						" )values("+
						"'"+ m.get(1)+ "',"+
						"'"+ m.get(2)+ "',"+
						"'"+ m.get(3)+ "',"+
						"'"+ getMeasureKey(m.get(4).toString())+ "',"+
						"'"+ m.get(5)+ "',"+
						"'"+ m.get(6)+ "',"+
						"'N'," +
						"'BM',"+
						"'460106'," +
						"'"+ m.get(7)+ "',"+
						"'"+ m.get(8)+ "',"+
						"'"+getMeasureKey(m.get(9).toString())+"',"+
						"'"+ m.get(9)+ "',"+
						"'"+ getMeasureKey(m.get(10).toString())+ "',"+
						"'"+ m.get(11)+ "',"+
						"'"+ m.get(12)+ "',"+
						"'"+ getMeasureKey(m.get(13).toString())+ "' " +
						")";
				listInsertCmd.add(insertSql);
			}
			try{
				//删除已有
				this.jdbcManager.execute("delete from bsc_measure where measure_id in( "+getStringById(ids)+")");
				this.jdbcManager.execute("delete from bsc_measure_exe where measure_id in ("+getStringById(ids)+")");
				listInsertCmd.forEach(s->{
					try {
						this.jdbcManager.execute(s);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				listInsertMeaRely.forEach(s->{
					try {
						this.jdbcManager.execute(s);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		doSuccessInfoResponse("处理完成[" + paramMap.get("fileName")+"]文件数据，请核对！");
		return null;
	}

	protected void doSuccessInfoResponse(String info) throws Exception{
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("success", Boolean.valueOf(true));
		results.put("info", info);
		response.setHeader("Content-Type", "text/html;charset = utf-8");
		JSONObject json = new JSONObject(results);
		response.getWriter().write(json.toString());
	}

	/**
	 * 根据值取得key
	 * @param value
	 * @return
	 */
	private String getMeasureKey(String value){
		Map<String,Object> mapSource = new HashMap<>();
		//指标类型
		mapSource.put("分类目录" ,"03");
		mapSource.put("基础指标" ,"00");
		mapSource.put("衍生指标" ,"01");
		mapSource.put("预警指标" ,"04");
		//指标来源
		mapSource.put("北京市发展和改革委员会" ,"bj01");
		mapSource.put("北京市民族事务委员会" ,"bj02");
		mapSource.put("北京市人力资源和社会保障局" ,"bj03");
		mapSource.put("北京市城市管理委员会" ,"bj04");
		mapSource.put("北京市商务委员会" ,"bj05");
		mapSource.put("北京市审计局" ,"bj06");
		mapSource.put("北京市教育委员会" ,"bj07");
		mapSource.put("北京市公安局" ,"bj08");
		mapSource.put("北京市规划和国土资源管理委员会" ,"bj09");
		mapSource.put("北京市交通委员会" ,"bj10");
		mapSource.put("北京市旅游发展委员会" ,"bj11");
		mapSource.put("北京市人民政府外事办公室" ,"bj12");
		mapSource.put("北京市科学技术委员会" ,"bj13");
		mapSource.put("北京市司法局" ,"bj14");
		mapSource.put("北京市环境保护局" ,"bj15");
		mapSource.put("北京市农村工作委员会" ,"bj16");
		mapSource.put("北京市文化局" ,"bj17");
		mapSource.put("北京市经济和信息化委员会" ,"bj18");
		mapSource.put("北京市财政局" ,"bj19");
		mapSource.put("北京市住房和城乡建设委员会" ,"bj20");
		mapSource.put("北京市水务局" ,"bj21");
		mapSource.put("北京市卫生和计划生育委员会" ,"bj22");
		mapSource.put("北京市人民政府国有资产监督管理委员会" ,"bj23");
		mapSource.put("北京市地方税务局" ,"bj24");
		mapSource.put("北京市食品药品监督管理局" ,"bj25");
		mapSource.put("北京市体育局" ,"bj26");
		mapSource.put("北京市知识产权局" ,"bj27");
		mapSource.put("北京市人民政府信访办公室" ,"bj28");
		mapSource.put("北京市国有文化资产监督管理办公室" ,"bj29");
		mapSource.put("北京市统计局" ,"bj30");
		mapSource.put("北京市民防局" ,"bj31");
		mapSource.put("北京市质量技术监督局" ,"bj32");
		mapSource.put("北京市新闻出版广电局（北京市版权局);" ,"bj33");
		mapSource.put("北京市园林绿化局" ,"bj34");
		mapSource.put("北京市人民政府侨务办公室" ,"bj35");
		mapSource.put("北京市安全生产监督管理局" ,"bj36");
		mapSource.put("北京市文物局" ,"bj37");
		mapSource.put("北京市金融工作局" ,"bj38");
		mapSource.put("北京市人民政府法制办公室" ,"bj39");
		mapSource.put("北京市工商行政管理局" ,"bj40");
		mapSource.put("北京市农业局" ,"bj41");
		mapSource.put("北京市粮食局" ,"bj42");
		mapSource.put("北京市中医管理局" ,"bj43");
		mapSource.put("北京市医院管理局" ,"bj44");
		mapSource.put("北京市城市管理综合行政执法局" ,"bj45");
		mapSource.put("北京市文化市场行政执法总队" ,"bj46");
		mapSource.put("北京市社会建设工作办公室" ,"bj47");
		mapSource.put("北京市人民政府天安门地区管理委员会" ,"bj48");
		mapSource.put("北京经济技术开发区管理委员会" ,"bj49");
		mapSource.put("北京西站地区管理委员会" ,"bj50");
		mapSource.put("中关村科技园区管理委员会" ,"bj51");
		mapSource.put("北京市气象局" ,"bj52");
		mapSource.put("北京市交通管理局" ,"bj53");
		mapSource.put("外部网站" ,"bj99");
		mapSource.put("北京市人民政府办公厅" ,"bj54");
		mapSource.put("12345北京市人民政府便民电话中心","bj55");
		mapSource.put("北京市应急管理局" ,"bj56");
		mapSource.put("北京市民政局" ,"bj57");
		mapSource.put("北京市政务服务管理局" ,"bj58");
		//周期
		mapSource.put("年","02");
		mapSource.put("季","01");
		mapSource.put("月","00");
		mapSource.put("日","03");
		//预警指标类型
		mapSource.put("阀值范围","0");
		mapSource.put("复杂类型","2");

		return mapSource.get(value)!=null&&!"".equals(mapSource.get(value).toString().trim())?mapSource.get(value).toString():"";
	}

	private List<ExcelField> getExcelFields(String showID, String [] titles) {
        List<ExcelField> excelFields = new ArrayList();          
        if(SHOW_FLAG.equals(showID)){
        	   excelFields.add(new ExcelField("地区名称", "zone_cd_desc"));
               excelFields.add(new ExcelField("维度名称", "object_name"));
               excelFields.add(new ExcelField("年份", "month_name"));
        }else{
        	  excelFields.add(new ExcelField("地区名称", "zone_cd_desc")); 
              excelFields.add(new ExcelField("年份", "month_name"));
              excelFields.add(new ExcelField("维度名称", "object_name"));
        }       
        int j = 3;
        String k = "";
        for(int i=3;i<titles.length;i++){
        	 j = i-3;
        	 k = String.valueOf(j);
        	 excelFields.add(new ExcelField(titles[i], "col_"+k));
        }   
        return excelFields;
    }

	public void setBscResultService(IBscResultService bscResultService) {
		this.bscResultService = bscResultService;
	}

	public ISelectorService getSelectorService() {
		return selectorService;
	}

	public void setSelectorService(ISelectorService selectorService) {
		this.selectorService = selectorService;
	}
	
	
	private final static String HEADER_SPLIT = "维度名称,";
	
	
	private final static String  MEASURE_FLAG ="1";
	
	private final static String  SHOW_FLAG ="1";


	
}
