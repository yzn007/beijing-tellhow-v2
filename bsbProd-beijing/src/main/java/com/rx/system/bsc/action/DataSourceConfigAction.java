package com.rx.system.bsc.action;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

import com.rx.log.annotation.FunDesc;
import com.rx.log.annotation.UseLog;
import com.rx.system.base.BaseDispatchAction;
import com.rx.system.bsc.service.IDataSourceConfigService;
import com.rx.system.constant.Constant;
import com.rx.system.domain.BscMeasure;
import com.rx.system.domain.DataSource;
import com.rx.system.model.excel.utils.ExcelUtil;
import com.rx.system.service.ISelectorService;
import com.rx.system.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * bsc_datasource:数据源配置
 * <b>Date:</b>Jun 26, 2013<br>
 * @author wangfl
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class DataSourceConfigAction extends BaseDispatchAction {

    private IDataSourceConfigService dataSourceConfigService = null;

    private ISelectorService selectorService = null;

    public void setSelectorService(ISelectorService selectorService) {
        this.selectorService = selectorService;
    }

    /**
     * 添加数据源
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0026")
	@UseLog
    public String add() throws Exception {
        try {
            DataSource dataSource = this.getParamObject(DataSource.class);
            this.dataSourceConfigService.addDataSource(dataSource);
            doSuccessInfoResponse("添加成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            doFailureInfoResponse("添加失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 编辑数据源属性
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0027")
	@UseLog
    public String edit() throws Exception {
        try {
            DataSource dataSource = this.getParamObject(DataSource.class);
            this.dataSourceConfigService.editDataSource(dataSource);
            doSuccessInfoResponse("修改成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            doFailureInfoResponse("修改失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 删除数据源记录
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0028")
	@UseLog
    public String delete() throws Exception {
        try {
            String sourceId = request.getParameter("source_id");
            this.dataSourceConfigService.removeDataSource(sourceId);
            doSuccessInfoResponse("删除成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            doFailureInfoResponse("删除失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 查询数据源记录
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0029")
	@UseLog
    public String list() throws Exception {
        Map<String, Object> paramMap = this.getRequestParam(request);
        this.insertPageParamToMap(paramMap);
        try {
            List<DataSource> sourceList = this.dataSourceConfigService.listDataSource(paramMap);
            //Integer total = this.dataSourceConfigService.listDataSourceCount(paramMap);
            //request.setAttribute("total", total);
            doJSONResponse(sourceList);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加数据源字段
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0030")
	@UseLog
    public String sourceFieldAdd() throws Exception {
        try {
            Map<String, Object> paramMap = this.getRequestParam(request);
            if (request.getParameter("link_id") == null)
                paramMap.put("link_id", "");
            this.dataSourceConfigService.addDataSourceField(paramMap);
            doSuccessInfoResponse("添加成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            doFailureInfoResponse("添加失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 编辑数据源字段属性
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0031")
	@UseLog
    public String sourceFieldEdit() throws Exception {
        try {
            Map<String, Object> paramMap = this.getRequestParam(request);
            if (request.getParameter("link_id") == null)
                paramMap.put("link_id", "");
            this.dataSourceConfigService.editDataSourceField(paramMap);
            doSuccessInfoResponse("修改成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            doFailureInfoResponse("修改失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 删除数据源字段记录
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0032")
	@UseLog
    public String sourceFieldDelete() throws Exception {
        try {
            this.dataSourceConfigService.deleteDataSourceField(this.getRequestParam(request));
            doSuccessInfoResponse("删除成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            doFailureInfoResponse("删除失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 查询数据源字段记录
     * @return
     * @throws Exception
     */
    @FunDesc(code="BSC_0033")
	@UseLog
    public String sourceFieldList() throws Exception {
        Map<String, Object> paramMap = this.getRequestParam(request);
        try {
        	List<Map<String,Object>> paramList = this.dataSourceConfigService.listDataSourceField(paramMap);
        	for(Map<String,Object> m : paramList){
        		m.put("mea_fullname", "[" + m.get("column_name") + "] " + m.get("column_biz_name"));
        		m.put("dim_fullname", "[" + m.get("column_name") + "] " + m.get("column_biz_name"));
        		
        		//判断是否加密功能id
        		String rid = "BSC20_10_05";
        		if("0".equals(session.getAttribute("isDirect"))){
        			rid = GlobalUtil.encryptValStr(session.getId(), "BSC20_10_05");
        		}
        		if(null != request.getParameter("type") && "dataConfig".equals(request.getParameter("type"))){
            		if(null != m.get("link_id") && !"".equals(m.get("link_id")))
        			m.put("link_name_in_dataconfig", "<a href=\"javascript:void(0);\" onclick = \"gotoPage('"+rid+"','/bsc/pages/bsc_dim_link.jsp?link_id=" + m.get("link_id") + "')\">" + m.get("link_name") + "</a>");
            	}
        	}
            doJSONResponse(paramList);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public IDataSourceConfigService getDataSourceConfigService() {
        return dataSourceConfigService;
    }

    public void setDataSourceConfigService(IDataSourceConfigService dataSourceConfigService) {
        this.dataSourceConfigService = dataSourceConfigService;
    }

    /**
     * 
     * 查询考核对象类型 数据
     * 
     * @throws Exception
     * @author: wangfl
     */
    public void queryObjCate() throws Exception {
        try {
            List<Map<String, Object>> list = this.dataSourceConfigService.queryObjCate();
            doJSONResponse(GlobalUtil.lowercaseListMapKey(list));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 
     * 参数链接 数据
     * 
     * @throws Exception
     * @author: wangfl
     */
    public void queryDimLink() throws Exception {
        try {
            List<Map<String, Object>> list = this.dataSourceConfigService.queryDimLink();
            doJSONResponse(GlobalUtil.lowercaseListMapKey(list));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * 查询字段 数据
     * 
     * @throws Exception
     * @author: wangfl
     */
    public void queryDataType() throws Exception {
        try {
            List<Map<String, Object>> list = this.dataSourceConfigService.queryDataType();
            doJSONResponse(GlobalUtil.lowercaseListMapKey(list));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 根据数据源表达式得到此表达式中涉及的所有--字段名、字段类型、字段数序
     * */
    public void getSorExpFields(){
    	String exp = request.getParameter("source_exp").replace("\n", " ");
    	String id = request.getParameter("source_id");
    	if(null != exp && !"".equals(exp)){
			try {
				List<Map<String, Object>> list = this.dataSourceConfigService.getSorExpFields(exp,id);
				doJSONResponse(list);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * 校验添加时ID是否存在
     * @return
     * @throws Exception
     */
    public String hasSourceID() throws Exception{
		Map<String, Object> paramMap = this.getRequestParam(request);
		if(this.dataSourceConfigService.hasSourceID(paramMap))
			doSuccessInfoResponse("存在该数据源ID");
		else
			doFailureInfoResponse("不存在该数据源ID");
		return null;
	}
    
    public String hasSourceName() throws Exception{
		Map<String, Object> paramMap = this.getRequestParam(request);
		if(this.dataSourceConfigService.hasSourceName(paramMap))
			doSuccessInfoResponse("存在该数据源名称");
		else
			doFailureInfoResponse("不存在该数据源名称");
		return null;
	}
    
    public String editHasSourceName() throws Exception{
		Map<String, Object> paramMap = this.getRequestParam(request);
		if(this.dataSourceConfigService.editHasSourceName(paramMap))
			doSuccessInfoResponse("存在该数据源名称");
		else
			doFailureInfoResponse("不存在该数据源名称");
		return null;
	}

    @FunDesc(code="BSC_0034")
    @UseLog
    public String importTemplateDownload() throws Exception {
        String webBasePath = "";
        String filename = "importDataSouceTemplate.xlsx";
        try{
            ServletActionContext.getServletContext();
            webBasePath = ServletActionContext.getServletContext().getRealPath("/");
        }catch (NullPointerException nullEx){
            webBasePath = "D:\\泰豪\\git\\new\\beijing-tellhow-v2\\bsbProd-beijing\\src\\main\\webapp\\";
        }

        String localFileName =  webBasePath + Constant.UPLOAD_DIR +filename;

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

    public String importFromExcel() throws Exception {
        String webBasePath = "";
        String filename = "数据源导入模板上传.xlsx";
        Map <String,Object> paramMap = getRequestParam(request);

        try{
            ServletActionContext.getServletContext();
            webBasePath = ServletActionContext.getServletContext().getRealPath("/");
        }catch (NullPointerException nullEx){
            webBasePath = "D:\\泰豪\\git\\new\\beijing-tellhow-v2\\bsbProd-beijing\\src\\main\\webapp\\";
        }

        String localFileName =  webBasePath + Constant.FILE_UPLOAD_DIR +"\\"+ DateFormat.getDateInstance().format(new Date())+"_"+filename;
        //上传到服务器目录
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
        List<Map<String, Object>> dataList = this.selectorService.queryForList("select '' LINK_ID," +
                "'全部' LINK_NAME, null SOURCE_EXPRESSION ,'N' IS_TREE ,'' ID_FIELD ,'' PARENT_ID_FIELD,'' " +
                "LABEL_FIELD,'' ROOT_VALUE from dual union select * from bsc_dim_link ");

        Sheet sheet = wookbook.getSheetAt(0);
        //开始处理上传行号-跳过标题和表头
        int startRowNum = 3;
        //总数
        int totalRowNum = sheet.getLastRowNum();
        int count = 0;
        for(int x = startRowNum ; x <= totalRowNum ; x++){
            Row row = sheet.getRow(x);
            DataSource dataSource = new DataSource();
            dataSource.setSource_id(row.getCell(1).getStringCellValue().trim()); // 数据源ID
            dataSource.setSource_name(row.getCell(2).getStringCellValue().trim()); // 数据源名称
            dataSource.setObj_column(row.getCell(3).getStringCellValue().trim()); // 对象维度
            dataSource.setObj_cate_id("BM");
            dataSource.setDistrict_column(row.getCell(4).getStringCellValue().trim()); //地区维度
            dataSource.setSource_expression(row.getCell(5).getStringCellValue().trim()); // 表达式

            if (StringUtils.isEmpty(dataSource.getSource_id()))
                continue;

            for(Map<String, Object> map:dataList) {
                if (dataSource.getObj_column().equals(map.get("link_name"))) {
                    dataSource.setObj_column(map.get("label_field").toString());
                }
            }
            if (dataSource.getDistrict_column().equals("全国地区代码")) {
                dataSource.setDistrict_column("Country_Region_Link");
            } else {
                dataSource.setDistrict_column("Zone_Cd");
            }
            try{this.dataSourceConfigService.addDataSource(dataSource); count++;}catch (Exception ex){}
        }
        doSuccessInfoResponse("成功导入" + count + "条数据");
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
}
