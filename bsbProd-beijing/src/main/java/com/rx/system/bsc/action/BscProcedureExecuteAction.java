package com.rx.system.bsc.action;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rx.framework.jdbc.JdbcManager;
import com.rx.log.annotation.FunDesc;
import com.rx.log.annotation.UseLog;
import com.rx.system.base.BaseDispatchAction;
import com.rx.system.bsc.calc.*;
import com.rx.system.bsc.calc.parameter.ParameterHandler;
import com.rx.system.bsc.calc.service.IDataSourceService;
import com.rx.system.bsc.calc.service.IMeasureService;
import com.rx.system.bsc.dao.BscProjectDao;
import com.rx.system.bsc.dao.DataSourceConfigDao;
import com.rx.system.bsc.dao.DimLinkDao;
import com.rx.system.bsc.service.IBscMeasureService;
import com.rx.system.bsc.service.IBscProjectService;
import com.rx.system.bsc.service.IDimLinkService;
import com.rx.system.bsc.service.impl.BscProjectServiceImpl;
import com.rx.system.bsc.service.impl.DataSourceConfigServiceImpl;
import com.rx.system.bsc.service.impl.DimLinkServiceImpl;

import static java.time.LocalDateTime.now;

/**
 * 平衡计分卡方案执行Action
 * @author chenxd
 *
 */
@SuppressWarnings("serial")
public class BscProcedureExecuteAction extends BaseDispatchAction {
	
	private IMeasureService measureService = null;
	private IDataSourceService dataSourceService = null;
	private IBscProjectService bscProjectService = null;
	private JdbcManager jdbcManager = null;
	private IDimLinkService dimLinkService = null;

	final int perProcessNum = 5000;
	final int NUM_PROCESS = 7;//线程数


	/**
	 * 指标计算
	 */
	@FunDesc(code="BSC_0017")
	@UseLog
	public String measureExe() throws Exception {

		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
//			CalculateProcedureOnlyMeasure procedure = new CalculateProcedureOnlyMeasure();
//			procedure.setBscProjectService(bscProjectService);
//			procedure.setDataSourceService(dataSourceService);
//			procedure.setMeasureService(measureService);
//			procedure.setDimLinkService(dimLinkService);
//
//			procedure.setJdbcManager(this.jdbcManager);
			Context context = new Context();
//			context.put("monthID", getStringValue(paramMap, "monthID"));
//			context.put("projectID", getStringValue(paramMap, "projectID"));
			context.put("cycleTypeID", getStringValue(paramMap, "cycleTypeID"));
			context.put("is_published", "N");
//			procedure.initContext(context);
//
			session.removeAttribute("status");
//			procedure.setSession(session);
//
//			procedure.start();
//			doSuccessInfoResponse("");
			int count = measureService.getValidMeasureCount();
			String date = now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(!"".equals(getStringValue(paramMap, "cycleTypeID"))) {
				date = getStringValue(paramMap, "cycleTypeID");
				System.out.print("calculate measure date is"+date);
			}
			CalculateProcedureOnlyMeasure.clearTargetData(date,this.jdbcManager);
			int perThreadOfNum = (int)Math.ceil((float)count/NUM_PROCESS/perProcessNum)*perProcessNum;
//            int threadNum = count%perProcessNum==0?count/perProcessNum:count/perProcessNum+1;
			ExecutorService executorService =  Executors.newFixedThreadPool(NUM_PROCESS);
//            ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
			System.out.println("threadNum:"+perThreadOfNum);
			for(int k = 0; k<count;k += perThreadOfNum){
//            for(int k = 0; k<count;k += perProcessNum){
				CalculateProcedureOnlyMeasure calculateProcedureOnlyMeasure = new CalculateProcedureOnlyMeasure();
				if(k==0)
					calculateProcedureOnlyMeasure.setPriority(Thread.MAX_PRIORITY);
				else
					calculateProcedureOnlyMeasure.setPriority(Thread.NORM_PRIORITY);
				calculateProcedureOnlyMeasure.setBscProjectService(bscProjectService);
				calculateProcedureOnlyMeasure.setDataSourceService(dataSourceService);
				calculateProcedureOnlyMeasure.setMeasureService(measureService);
				calculateProcedureOnlyMeasure.setDimLinkService(dimLinkService);

				calculateProcedureOnlyMeasure.setJdbcManager(this.jdbcManager);
				context = new Context();
	//			context.put("monthID", getStringValue(paramMap, "monthID"));
	//			context.put("projectID", getStringValue(paramMap, "projectID"));
				context.put("cycleTypeID", getStringValue(paramMap, "cycleTypeID"));
				context.put("is_published", "N");
				context.put("start",""+k);
				context.put("endrow",""+(k+ perThreadOfNum));
				context.put("thread",""+(k+ perThreadOfNum)/perThreadOfNum);
				calculateProcedureOnlyMeasure.initContext(context);

//				session.removeAttribute("status");
				calculateProcedureOnlyMeasure.setSession(session);
				
//				calculateProcedureOnlyMeasure.date = date;
//				calculateProcedureOnlyMeasure.start = k;
//				calculateProcedureOnlyMeasure.endrow = k+ perThreadOfNum;
////                calculateProcedureOnlyMeasure.endrow = k+ perProcessNum;
//				DimLinkServiceImpl dimLinkService = new DimLinkServiceImpl();
//				calculateProcedureOnlyMeasure.setDimLinkService(dimLinkService );
//				BscProjectServiceImpl bscProjectService =  new BscProjectServiceImpl();
//				calculateProcedureOnlyMeasure.setBscProjectService(bscProjectService);
//				DataSourceConfigServiceImpl dataSourceService = new DataSourceConfigServiceImpl();
//				calculateProcedureOnlyMeasure.setDataSourceService(dataSourceService);
//
//				calculateProcedureOnlyMeasure.setMeasureService(bscMeasureServiceImpl);
//
////        template= (JdbcTemplate)ioc.getBean("jdbcTemplate");
////        supportedJdbcManager = new SupportedJdbcManager(template);
//				calculateProcedureOnlyMeasure.setJdbcManager(this.jdbcManager);
//
//				calculateProcedureOnlyMeasure.context = new Context();
//				DimLinkDao dimLinkDao = (DimLinkDao)ioc.getBean("dimLinkDao");
//				dimLinkService.setDimLinkDao(dimLinkDao);
//				BscProjectDao bscProjectDao = (BscProjectDao) ioc.getBean("bscProjectDao");
//				bscProjectService.setBscProjectDao(bscProjectDao);
//				DataSourceConfigDao dataSourceConfigDao = (DataSourceConfigDao) ioc.getBean("dataSourceConfigDao");
//				dataSourceService.setDataSourceConfigDao(dataSourceConfigDao);
////        BscMeasureDao bscMeasureDao = (BscMeasureDao)ioc.getBean("bscMeasureDao") ;
////        bscMeasureServiceImpl.setBscMeasureDao(bscMeasureDao);
//
//				calculateProcedureOnlyMeasure.initContext(calculateProcedureOnlyMeasure.context);
//				calculateProcedureOnlyMeasure.paramHandler = new ParameterHandler(calculateProcedureOnlyMeasure.context );
//                calculateProcedureOnlyMeasure.run();
				executorService.execute(calculateProcedureOnlyMeasure);
			}
			executorService.shutdown();
			System.out.println("start:"+new Date());
			doSuccessInfoResponse("");
		} catch (Exception e) {
			e.printStackTrace();
			session.removeAttribute("status");
			doFailureInfoResponse("执行失败:" + e.getMessage());
		}
		return null;
	}

	/**
	 * 执行方案
	 */
	@FunDesc(code="BSC_0017")
	@UseLog
	public String execute() throws Exception {

		Map<String, Object> paramMap = this.getRequestParam(request);
		try {
			CalculateProcedure procedure = new CalculateProcedure();
			procedure.setBscProjectService(bscProjectService);
			procedure.setDataSourceService(dataSourceService);
			procedure.setMeasureService(measureService);

			procedure.setJdbcManager(this.jdbcManager);
			Context context = new Context();
			context.put("monthID", getStringValue(paramMap, "monthID"));
			context.put("projectID", getStringValue(paramMap, "projectID"));
			context.put("cycleTypeID", getStringValue(paramMap, "cycleTypeID"));
			context.put("is_published", "N");
			procedure.initContext(context);

			session.removeAttribute("status");
			procedure.setSession(session);

			procedure.start();
			doSuccessInfoResponse("");
		} catch (Exception e) {
			e.printStackTrace();
			session.removeAttribute("status");
			doFailureInfoResponse("执行失败:" + e.getMessage());
		}
		return null;
	}
	
	/**
	 * 查询方案执行状态
	 * @return
	 * @throws Exception
	 */
	public String queryStatus() throws Exception {
		Thread.sleep(500);
		ThreadStatus status = (ThreadStatus) session.getAttribute("status");

		Map<String, Object> results = new HashMap<String, Object>();
		if(status!=null) {
			results.put("count", status.getItemCount());
			results.put("index", status.getIndex());
			results.put("time", status.getCalculateTime());
			results.put("state", status.getStatus());
			results.put("exception", status.getException());
			results.put("success", true);
			results.put("log", status.getLogList());
		}else{
			//非正常报错
			results.put("success", false);
			results.put("info", "数据处理中……");
		}

		doJSONResponse(results);
		
		return null;
		
	}
	
	/**
	 * 停止执行方案
	 * @return
	 * @throws Exception
	 */
	@FunDesc(code="BSC_0018")
	@UseLog
	public String stop() throws Exception {
		ThreadStatus status = (ThreadStatus) session.getAttribute("status");
		try {
			if(status != null) {
				status.setStatus(ThreadStatus.STATUS_STOP);
				session.removeAttribute("status");
			}
			doSuccessInfoResponse("");
		} catch (Exception e) {
			session.removeAttribute("status");
			e.printStackTrace();
			doFailureInfoResponse(e.getMessage());
		}
		return null;
	}

	public void setMeasureService(IMeasureService measureService) {
		this.measureService = measureService;
	}

	public void setDataSourceService(IDataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	
	public void setBscProjectService(IBscProjectService bscProjectService){
		this.bscProjectService = bscProjectService;
	}

	public void setJdbcManager(JdbcManager jdbcManager) {
		this.jdbcManager = jdbcManager;
	}

	public  void setDimLinkService(IDimLinkService dimLinkService){
		this.dimLinkService = dimLinkService;
	}
}
