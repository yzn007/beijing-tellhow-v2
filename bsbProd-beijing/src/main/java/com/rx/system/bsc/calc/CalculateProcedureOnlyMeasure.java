package com.rx.system.bsc.calc;

import java.io.File;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.swing.text.html.HTMLDocument;

import com.rx.framework.jdbc.JdbcManager;
import com.rx.framework.jdbc.SupportedJdbcManager;
import com.rx.system.bsc.calc.parameter.ParameterHandler;
import com.rx.system.bsc.calc.parse.IExpression;
import com.rx.system.bsc.calc.parse.StackExpression;
import com.rx.system.bsc.calc.parse.StringUtil;
import com.rx.system.bsc.calc.service.IDataSource;
import com.rx.system.bsc.calc.service.IDataSourceService;
import com.rx.system.bsc.calc.service.IMeasure;
import com.rx.system.bsc.calc.service.IMeasureService;
import com.rx.system.bsc.dao.BscMeasureDao;
import com.rx.system.bsc.dao.BscProjectDao;
import com.rx.system.bsc.dao.DataSourceConfigDao;
import com.rx.system.bsc.dao.DimLinkDao;
import com.rx.system.bsc.service.IBscProjectService;
import com.rx.system.bsc.service.IDimLinkService;
import com.rx.system.bsc.service.impl.BscMeasureServiceImpl;
import com.rx.system.bsc.service.impl.BscProjectServiceImpl;
import com.rx.system.bsc.service.impl.DataSourceConfigServiceImpl;
import com.rx.system.bsc.service.impl.DimLinkServiceImpl;
import com.rx.system.domain.DataSource;
import com.rx.system.domain.DimLink;
import org.apache.struts2.components.Else;
import org.apache.struts2.components.ElseIf;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import static java.time.LocalDateTime.*;

/**
 * 平衡计分卡计算程序
 * 将前端指标录入的公式解析成可执行的SQL语法段,存储并执行
 * 修改将运行需要的执行方案相关剥离，仅计算指标
 * @author chenxd
 *
 */
public class CalculateProcedureOnlyMeasure extends Thread implements Procedure{

    protected int start = 0;
    protected int endrow = 10000;
    protected int threadNm = 1;


    protected Context context = null;//运行环境变量
    protected int 	  runStep = 0; //运行处理过程

    protected IDataSourceService dataSourceService = null;//数据源操作接口
    protected IMeasureService measureService = null;//指标操作接口
    protected IBscProjectService bscProjectService = null; //方案操作接口
    protected IDimLinkService dimLinkService = null;//码表接口

    //数据源缓存
    protected Map<String, IDataSource> dataScourceCache = new HashMap<String, IDataSource>();
    //数据源值缓存
    protected  Map<String,List<Map>> dataScourceValuesCache = new HashMap<String, List<Map>>();
    //指标缓存
    protected Map<String, IMeasure> measureCache = new HashMap<String, IMeasure>();
    //代码表缓冲
    protected  Map<String,List<Map>> dimlikCache = new HashMap<String, List<Map>>();

    protected ParameterHandler paramHandler = null;//参数解析类

    protected JdbcManager jdbcManager = null;

    protected String date = "";
    protected String monthID = "";

    protected String resultTable = "bsc_result_measure";//结果表表名
    protected String commandTable = "bsc_proj_mea_cmd_measure";//命令表表名
    protected String bsc_proj_mea_obj_val_measure = "bsc_proj_mea_obj_val_measure";//公式
    protected String bsc_proj_exe_mth_measure = "bsc_proj_exe_mth_measure";//方案执行情况
    protected String bsc_proj_val_cmd_measure = "bsc_proj_val_cmd_measure";//方案指标计算命令

    protected boolean run = true;//线程是都继续运行
    private 	HttpSession 	session = null;

    protected 	ThreadStatus 	status 	= new ThreadStatus();

    public CalculateProcedureOnlyMeasure() {
    }

    public void setDataSourceService(IDataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setBscProjectService(IBscProjectService bscProjectService) {
        this.bscProjectService = bscProjectService;
    }

    public void setMeasureService(IMeasureService measureService) {
        this.measureService = measureService;
    }

    public  void setDimLinkService(IDimLinkService dimLinkService){
        this.dimLinkService = dimLinkService;
    }

    //初始化运行环境
    public void initContext(Context context) {

        this.context = context;
        paramHandler = new ParameterHandler(context);
        this.start =  !"".equals(this.context.getEnv("start"))?Integer.valueOf( this.context.getEnv("start")):0;
        this.endrow =  !"".equals(this.context.getEnv("endrow"))?Integer.valueOf( this.context.getEnv("endrow")):6000;
        this.threadNm = !"".equals(this.context.getEnv("thread"))?Integer.valueOf( this.context.getEnv("thread")):0;
        //if(start==0)
            status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在初始化执行参数......");

        if (date==""|| null==date) {
            date = now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        this.monthID = this.context.getEnv("monthID");
        //if(start==0)
            status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"初始化执行参数完成......");
    }

    /**
     * 执行方法
     */
    public void run() {
        try {
//            清除目标数据
//            if(start==0)
//                clearTargetData();
//            else
//                sleep(5000);
            if(start==0)
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            else
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

            //查询出所有需要计算的指标
            Map map = new HashMap();
            map.put("start",start);
            map.put("endrow",endrow);
            List<IMeasure> measureList = this.measureService.getValidMeasureMap(map);

//            //查询出方案指标
//            List<Map<String, Object>> projMeasures = this.measureService.getProjectMeasure(this.projectID);
             //if(start==0)
                status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在解析底层指标计算公式......");
            System.out.println("正在解析底层指标计算公式......");
            System.out.println(status.getLogList());
            status.setStatus(ThreadStatus.STATUS_RUNNING);//设置线程运行状态
            this.status.setItemCount(measureList.size()*2*2 );
            parseLowLevelMeasure(measureList);

            //解析方案指标
             //if(start==0)
                status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在解析方案指标......");
            System.out.println("正在解析方案指标......"+start);
            executeLowLevelMeasure();
            parseProjectMeasure(measureList);

//            刷新对象
//            status.addLogExecutInfo("刷新对象列表......");
//            bscProjectService.refreshProjectObjects(this.date);

            //执行解析出的公式SQL
             //if(start==0)
                status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算指标值......");
            System.out.println("正在计算指标值......"+start);

            executeProjectMeasure();
             //if(start==0)
                this.status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在完成方案计算......");
            System.out.println("正在完成方案计算......"+start);
            this.session.setAttribute("status", this.status);


            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            //插入方案执行情况
//            String info_sql = "insert into bsc_proj_exe_mth(project_id,month_id,is_cycle_end_mth,is_published) "
//                    +"values('"+this.projectID+"','"+this.monthID+"','"+isCycleEnd(this.monthID)+"','N')";
//            this.jdbcManager.execute(info_sql);

//            //归档历史数据
//            archiveToHist();
             //if(start==0)
                this.status.addLogExecutInfo("【处理过程："+this.threadNm+"】"+"方案计算已完成......");
            System.out.println("方案计算已完成......"+start);
            this.status.setStatus(ThreadStatus.STATUS_STOP);//线程执行结束
            System.out.println("end:["+new Date()+"]"+start);
            this.session.setAttribute("status", this.status);

        } catch (Exception e) {
            e.printStackTrace();
            this.status.setStatus(ThreadStatus.STATUS_EXCEPTION);//设置线程为异常状态
             //if(start==0)
                this.status.addLogExecutInfo(e.getMessage());
            this.status.setException(e.getMessage());
            this.session.setAttribute("status", this.status);

        }
    }

    /**
     * date 日期格式 2019-04-29
     * 返回季度格式，21:表示一季度……24:表示四季度
     */
    private static String getSeasonString(String date){
        String season = date.substring(0,4) + "-21";//第一季度
        if(Integer.parseInt(date.substring(5,7))>9){
            season = date.substring(0,4) +"-24" ;//第四季度
        }else if(Integer.parseInt(date.substring(5,7))>6){
            season = date.substring(0,4) +"-23" ;//第三季度
        }else if(Integer.parseInt(date.substring(5,7))>3){
            season = date.substring(0,4) +"-22" ;//第二季度
        }
        return season;
    }

    public static void clearTargetData(String date,JdbcManager jdbcManager) throws Exception{
        //日期取得
        String dateFrm = date;
        jdbcManager.execute("delete from bsc_proj_val_cmd_measure where date= '"+dateFrm+"'" );
        dateFrm = date.substring(0,7);
        jdbcManager.execute("delete from  bsc_proj_val_cmd_measure where date= '"+dateFrm+"'" );
        dateFrm = getSeasonString(date);
        jdbcManager.execute("delete from  bsc_proj_val_cmd_measure where date= '"+dateFrm+"'" );
        dateFrm = date.substring(0,4);
        jdbcManager.execute("delete from  bsc_proj_val_cmd_measure where date= '"+dateFrm+"'" );
        //删除日数据
        jdbcManager.execute("delete from bsc_result_measure where date = '"+date+"'");
        jdbcManager.execute("delete from bsc_proj_mea_obj_val_measure where date='"+date+"'");
//        jdbcManager.execute("delete from " +bsc_proj_exe_mth_measure+" where date = '"+date+"'");
        jdbcManager.execute("delete from bsc_proj_mea_cmd_measure where date = '"+date+"'");
        //删除月数据
        jdbcManager.execute("delete from bsc_result_measure  where date = '"+date.substring(0,7)+"'");
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '00')" );
        jdbcManager.execute("delete from bsc_proj_mea_obj_val_measure where date='"+date.substring(0,7)+"'");
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '00')" );
//        jdbcManager.execute("delete from " +bsc_proj_exe_mth_measure+" where date = '"+date.substring(0,7)
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '00')" );
        jdbcManager.execute("delete from bsc_proj_mea_cmd_measure where date = '"+date.substring(0,7)+"'");
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '00')" );
        //删除季数据
        String season = getSeasonString(date);
        jdbcManager.execute("delete from bsc_result_measure where date = '"+season+"' ");
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '01')" );
        jdbcManager.execute("delete from bsc_proj_mea_obj_val_measure where date='"+season+"'" );
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '01')" );
//        jdbcManager.execute("delete from " +bsc_proj_exe_mth_measure+" where date = '"+season
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '01')" );
        jdbcManager.execute("delete from bsc_proj_mea_cmd_measure where date = '"+season+"'" );
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '01')" );
        //删除年数据
        jdbcManager.execute("delete from bsc_result_measure where date = '"+date.substring(0,4)+"'" );
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '02')" );
        jdbcManager.execute("delete from bsc_proj_mea_obj_val_measure where date='"+date.substring(0,4)+"'" );
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '02')" );
//        jdbcManager.execute("delete from " +bsc_proj_exe_mth_measure+" where date = '"+date.substring(0,4)
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '02')" );
        jdbcManager.execute("delete from bsc_proj_mea_cmd_measure where date = '"+date.substring(0,4)+"'" );
//                +" and measure_id in  (select measure_id from bsc_measure where countperiod = '02')" );
    }

    protected void parseLowLevelMeasure(List<IMeasure> measureList) throws Exception{
        runStep = 0;
        this.status.setIndex(runStep);
        this.session.setAttribute("status", this.status);


        //解析每个指标的执行命令并存入表中
        for (int i = 0; i < measureList.size() && this.run; i++) {
            IMeasure measure = measureList.get(i);
            String command = this.parseMeasure(measure);
            if("".equals(command)||null==command)
                continue;
            try {
                insertExeCommand(measure, command, i + 1);
            }catch (Exception e){
                System.out.print(e.toString());
//                System.out.println("error:"+measure.getMeasureName()+"*"+command);
//                continue;
            }
            measureCache.put(measure.getMeasureId(), measure);

            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            this.status.setIndex(++runStep);
             //if(start==0)
                this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在解析底层指标计算公式......("+ (i+1) +"/"+measureList.size()+")");
            this.session.setAttribute("status", this.status);

        }
    }

    protected void parseProjectMeasure(List<IMeasure> projMeasures) throws Exception{
//        //日期取得
//        String dateFrm = this.date;
//        this.jdbcManager.execute("delete from " +this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" );
//        dateFrm = this.date.substring(0,7);
//        this.jdbcManager.execute("delete from " +this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" );
//        dateFrm = getSeasonString(this.date);
//        this.jdbcManager.execute("delete from " +this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" );
//        dateFrm = this.date.substring(0,4);
//        this.jdbcManager.execute("delete from " +this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" );

        for (int i = 0; i < projMeasures.size() && this.run; i++) {
            IMeasure map = projMeasures.get(i);
            String meausre_id = map.getMeasureId();

            String command = this.parseProjectMeasureFormula(map);
            this.insertProjectMeasureCommand(map, command);

            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            this.status.setIndex(++runStep);
             //if(start==0)
                this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在解析方案指标......("+ (i+1) +"/"+projMeasures.size()+")");
             //if(start==0)
                System.out.println("【处理过程："+this.threadNm+"】"+"正在解析方案指标......("+ (i+1) +"/"+projMeasures.size()+")");
            this.session.setAttribute("status", this.status);
        }
    }

    @SuppressWarnings("unchecked")
    protected void executeLowLevelMeasure() throws Exception{
        //日期取得
        String dateFrm = this.date;
        String sql = "select * from " + this.commandTable + " where date= '"+dateFrm+"' and rowmark='" +start+ "' order by exe_order_id";
        List<Map<String, Object>> lowMeaCommandList = this.jdbcManager.queryForList(sql);

        if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
            return;
        }
        List<String> commdFailList = new ArrayList<>();
        for (int i = 0; i < lowMeaCommandList.size() && this.run; i++) {
            Map<String, Object> map = lowMeaCommandList.get(i);
            String command = String.valueOf(map.get("exe_command".toUpperCase()));
            if (StringUtils.isEmpty(command) || command == "null")
                continue;
            try {
                command = this.replaceContextVar(command);
                this.jdbcManager.execute(command);
                Thread.sleep(50);
            }catch (Exception e){
                commdFailList.add(command);
            }

            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            this.status.setIndex(++runStep);
             //if(start==0)
                this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算底层日指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")");
            System.out.println("正在计算底层日指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")"+start);
            this.session.setAttribute("status", this.status);
        }
        //月
        dateFrm = this.date.substring(0,7);
        sql = "select * from " + this.commandTable + " where date= '"+dateFrm+"' and rowmark='" +start+ "'  order by exe_order_id";
        lowMeaCommandList.clear();
        lowMeaCommandList = this.jdbcManager.queryForList(sql);
        for (int i = 0; i < lowMeaCommandList.size() && this.run; i++) {
            Map<String, Object> map = lowMeaCommandList.get(i);
            String command = String.valueOf(map.get("exe_command".toUpperCase()));
            if (StringUtils.isEmpty(command) || command == "null")
                continue;
            try {
                command = this.replaceContextVar(command);
                this.jdbcManager.execute(command);
                Thread.sleep(50);
            }catch (Exception e){
                commdFailList.add(command);
            }

            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            this.status.setIndex(++runStep);
             //if(start==0)
                this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算底层月指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")");
            System.out.println("正在计算底层月指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")"+start);
            this.session.setAttribute("status", this.status);
        }
        //季
        dateFrm = getSeasonString(this.date);
        sql = "select * from " + this.commandTable + " where date= '"+dateFrm+"' and rowmark='" +start+ "'  order by exe_order_id";
        lowMeaCommandList.clear();
        lowMeaCommandList = this.jdbcManager.queryForList(sql);
        for (int i = 0; i < lowMeaCommandList.size() && this.run; i++) {
            Map<String, Object> map = lowMeaCommandList.get(i);
            String command = String.valueOf(map.get("exe_command".toUpperCase()));
            if (StringUtils.isEmpty(command) || command == "null")
                continue;
            try {
                command = this.replaceContextVar(command);
                this.jdbcManager.execute(command);
                Thread.sleep(50);
            }catch (Exception e){
                commdFailList.add(command);
            }

            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            this.status.setIndex(++runStep);
             //if(start==0)
                this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算底层季指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")");
            System.out.println("正在计算底层季指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")"+start);
            this.session.setAttribute("status", this.status);
        }
        //年
        dateFrm = this.date.substring(0,4);
        sql = "select * from " + this.commandTable + " where date= '"+dateFrm+"' and rowmark='" +start+ "'  order by exe_order_id";
        lowMeaCommandList.clear();
        lowMeaCommandList = this.jdbcManager.queryForList(sql);
        for (int i = 0; i < lowMeaCommandList.size() && this.run; i++) {
            Map<String, Object> map = lowMeaCommandList.get(i);
            String command = String.valueOf(map.get("exe_command".toUpperCase()));
            if (StringUtils.isEmpty(command) || command == "null")
                continue;
            try {
                command = this.replaceContextVar(command);
                this.jdbcManager.execute(command);
                Thread.sleep(50);
            }catch (Exception e){
                commdFailList.add(command);
            }

            if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
                return;
            }
            this.status.setIndex(++runStep);
             //if(start==0)
                this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算底层年指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")");
            System.out.println("正在计算底层年指标值......("+ (i+1) +"/"+lowMeaCommandList.size()+")"+start);
            this.session.setAttribute("status", this.status);
        }
        if(commdFailList.size()>0){
            for(String command:commdFailList){
                try {
                    this.jdbcManager.execute(command);
                    Thread.sleep(50);
                }catch (Exception e){
                    System.out.println("error info:{"+e.toString()+"}");
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    protected void executeProjectMeasure() throws Exception{
        //未执行的
        List<String> listNoExe = new ArrayList<>();
        //日
        String dateFrm = this.date;
        String bsc_count_sql = "select count(1) from "+this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" ;
        int cnt = this.jdbcManager.queryForInt(bsc_count_sql);
        String bsc_sql = "";
        List<Map<String, Object>> projMeaCommandList = null;
        final int perProc = 1000;
        for(int k = 0;k<cnt;k=k+perProc) {
            bsc_sql = "select * from (select tt.*, @rw:=@rw+1 rw from (select * from " + this.bsc_proj_val_cmd_measure + " where date='" + dateFrm+"' and rowmark='" +start
                    + "' order by exe_order_id)tt,(select @rw:=0) r) t1 where rw>" + String.valueOf(k) + " and rw <=" + String.valueOf(k + perProc);
            projMeaCommandList = this.jdbcManager.queryForList(bsc_sql);
            //执行积分公式SQL日
            for (int i = 0; i < projMeaCommandList.size() && this.run; i++) {
                Map<String, Object> map = projMeaCommandList.get(i);
                String command = String.valueOf(map.get("exe_command".toUpperCase()));
                command = this.replaceContextVar(command);
                try {
                    this.jdbcManager.execute(command);
                    Thread.sleep(50);
                }catch (Exception e){
                    listNoExe.add(command);
                }

                if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
//                    return;
                }
                this.status.setIndex(++runStep);
                 //if(start==0)
                    this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算方案日指标值......("+ (k+i+1) +"/"+cnt+")");
                System.out.println("正在计算方案日指标值......("+ (k+i+1) +"/"+cnt+")"+start);
                this.session.setAttribute("status", this.status);
            }
        }
        //月
        dateFrm = this.date.substring(0,7);
        bsc_count_sql = "select count(1) from "+this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" ;
        cnt = this.jdbcManager.queryForInt(bsc_count_sql);
        for(int k = 0;k<cnt;k=k+perProc){
            bsc_sql = "select * from (select tt.*, @rw:=@rw+1 rw from (select * from " +this.bsc_proj_val_cmd_measure+" where date='"+dateFrm+"' and rowmark='" +start
                    +"' order by exe_order_id)tt,(select @rw:=0) r) t1 where rw>"+String.valueOf(k)+ " and rw <=" + String.valueOf(k+perProc);
            projMeaCommandList = null;
            projMeaCommandList = this.jdbcManager.queryForList(bsc_sql);
            for (int i = 0; i < projMeaCommandList.size() && this.run; i++) {
                Map<String, Object> map = projMeaCommandList.get(i);
                String command = String.valueOf(map.get("exe_command".toUpperCase()));
                command = this.replaceContextVar(command);
                try {
                    this.jdbcManager.execute(command);
                    Thread.sleep(50);
                }catch (Exception e){
                    listNoExe.add(command);
                }

                if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
//                    return;
                }
                this.status.setIndex(++runStep);
                 //if(start==0)
                    this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算方案月指标值......("+ (k+i+1) +"/"+cnt+")");
                System.out.println("正在计算方案月指标值......("+ (k+i+1) +"/"+cnt+")"+start);
                this.session.setAttribute("status", this.status);
            }
        }
        //季
        dateFrm = getSeasonString(this.date);
        bsc_count_sql = "select count(1) from "+this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" ;
        cnt = this.jdbcManager.queryForInt(bsc_count_sql);
        for(int k = 0;k<cnt;k=k+perProc){
            bsc_sql = "select * from (select tt.*, @rw:=@rw+1 rw from (select * from " +this.bsc_proj_val_cmd_measure+" where date='"+dateFrm+"' and rowmark='" +start
                    +"' order by exe_order_id)tt,(select @rw:=0) r) t1 where rw>"+String.valueOf(k)+ " and rw <=" + String.valueOf(k+perProc);
            projMeaCommandList = null;
            projMeaCommandList = this.jdbcManager.queryForList(bsc_sql);
            for (int i = 0; i < projMeaCommandList.size() && this.run; i++) {
                Map<String, Object> map = projMeaCommandList.get(i);
                String command = String.valueOf(map.get("exe_command".toUpperCase()));
                command = this.replaceContextVar(command);
                try {
                    this.jdbcManager.execute(command);
                    Thread.sleep(50);
                }catch (Exception e){
                    listNoExe.add(command);
                }

                if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
//                    return;
                }
                this.status.setIndex(++runStep);
                 //if(start==0)
                    this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算方案季指标值......("+ (k+i+1) +"/"+cnt+")");
                System.out.println("正在计算方案季指标值......("+ (k+i+1) +"/"+cnt+")"+start);
                this.session.setAttribute("status", this.status);
            }
        }

        //年
        dateFrm = this.date.substring(0,4);
        bsc_count_sql = "select count(1) from "+this.bsc_proj_val_cmd_measure+" where date= '"+dateFrm+"' and rowmark='" +start+ "'" ;
        cnt = this.jdbcManager.queryForInt(bsc_count_sql);
        for(int k = 0;k<cnt;k=k+perProc){
            bsc_sql = "select * from (select tt.*, @rw:=@rw+1 rw from (select * from " +this.bsc_proj_val_cmd_measure+" where date='"+dateFrm+"' and rowmark='" +start
                    +"' order by exe_order_id)tt,(select @rw:=0) r) t1 where rw>"+String.valueOf(k)+ " and rw <=" + String.valueOf(k+perProc);
            projMeaCommandList = null;
            projMeaCommandList = this.jdbcManager.queryForList(bsc_sql);
            for (int i = 0; i < projMeaCommandList.size() && this.run; i++) {
                Map<String, Object> map = projMeaCommandList.get(i);
                String command = String.valueOf(map.get("exe_command".toUpperCase()));
                command = this.replaceContextVar(command);
                try {
                    this.jdbcManager.execute(command);
                    Thread.sleep(50);
                }catch (Exception e){
                    listNoExe.add(command);
                }

                if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
//                    return;
                }
                this.status.setIndex(++runStep);
                 //if(start==0)
                    this.status.updateLogExecutInfo("【处理过程："+this.threadNm+"】"+"正在计算方案年指标值......("+ (k+i+1) +"/"+cnt+")");
                System.out.println("【处理过程："+this.threadNm+"】"+"正在计算方案年指标值......("+ (k+i+1) +"/"+cnt+")"+start);
                this.session.setAttribute("status", this.status);
            }
        }
        projMeaCommandList = null;
        if(listNoExe.size()>0){
            System.out.println("未执行的sql数量为："+listNoExe.size());
        }
    }

    protected void archiveToHist() throws Exception{
         //if(start==0)
            this.status.addLogExecutInfo("正在归档历史数据......");
        this.session.setAttribute("status", this.status);
        if(this.status.getStatus() != ThreadStatus.STATUS_RUNNING){
            return;
        }
        this.jdbcManager.execute("call makeHistForProject('"+(this.monthID.length()==4?this.monthID+"01":this.monthID)+"','"+this.date+"')");
    }


    /**
     * 根据指标解析出指标的公式SQL
     * @param measure
     * @return
     * @throws Exception
     */
    protected String parseMeasure(IMeasure measure) throws Exception {
        String sql = "";
        if(measure.getSourceTypeId().equals(IMeasure.SOURCE_TYPE_DATASOURCE)) {
            //数据源指标
            sql = this.parseDataSourceMeasure(measure);
        }else if(measure.getSourceTypeId().equals(IMeasure.SOURCE_TYPE_COMPOUND)) {
            //复合指标
            sql = this.parseCompoundMeasure(measure);
        }else if(measure.getSourceTypeId().equals(IMeasure.SOURCE_TYPE_EXTERNAL)) {
            //导入指标
            sql = this.parseExternalMeasure(measure);
        }else if(measure.getSourceTypeId().equals(IMeasure.SOURCE_TYPE_ALERT)) {
            //预警指标(暂处理复合指标)
            sql = this.parseCompoundMeasure(measure);
        }else if(measure.getSourceTypeId().equals(IMeasure.SOURCE_TYPE_FOLDER)) {
            throw new Exception("指标:"+measure.getMeasureName()+"["+measure.getMeasureId()+"]类型为分类目录,不能参与计算");
        }else
            throw new Exception("指标:"+measure.getMeasureName()+"["+measure.getMeasureId()+"]类型错误,不存在类型为["+measure.getSourceTypeId()+"]的指标");
        return sql;

    }

    /**
     * 解析基础指标
     * @param measure
     * @return
     * @throws Exception
     */
    protected String parseDataSourceMeasure(IMeasure measure) throws Exception {
        String v_sourceID		=	measure.getSourceId();		//数据源ID
        String v_resultTypeID	=	measure.getResultTypeId();	//结果类型；00：集合；01：标量值
        String v_formulaExpr	=	measure.getFormula();	//公式值

        //1. 预处理判断
        if (null == v_sourceID || null == v_resultTypeID || null == v_formulaExpr){
            throw new Exception("指标:["+measure.getMeasureName()+"] 配置不完整");
        }

        //2. 把公式中的大括号替换掉
        v_formulaExpr	=	v_formulaExpr.replaceAll("\\{", "").replaceAll("\\}", "");

        //3. 开始解析
        //3.1 查找数据源属性
        IDataSource dataSource = this.getDataSource(measure.getSourceId());
        if(dataSource==null){
            return "";
        }
        String v_objectColumn	=	dataSource.getObjColumnName();
        String v_sourceExpr		=	dataSource.getExpression();
        String v_districtColumn = dataSource.getDistrictColumn();

        //查找指标公式引用的参数
        List<String> paraIDs	=	measure.getParams();

        String exprMeasureID	=	measure.getMeasureId();
        String exprObjectID		=	"";
        String exprValue		=	"";
        String exprFilter		=	"";
        String exprDistrictID   =   "";

        //3.2 如果数据源是数据集, 则使用数据源定义的对象维度；否则使用固定值PARAMETER
        if ("00".equals(v_resultTypeID)){
            if (null != v_objectColumn){
                exprObjectID	=	v_objectColumn;
            }
            if(null!= v_districtColumn){
                exprDistrictID = v_districtColumn;
            }
        }else {
            exprObjectID	=	"'"+ IMeasure.OBJ_NAME_SCALE_MEASURE +"'";
        }

        String[] formularExprs = v_formulaExpr.split("\\?");

        //3.3 设置值表达式
        if (formularExprs.length > 0){
            IExpression expr = new StackExpression(formularExprs[0]);

            expr.doParse();

            exprValue	=	expr.getParseResult();
        }

        //3.4 设置过滤表达式
        if (formularExprs.length > 1){
            IExpression expr = new StackExpression(formularExprs[1]);

            expr.doParse();

            exprFilter	=	expr.getParseResult();
        }

        //3.5 替换参数名称
        for (int i = 0; i < paraIDs.size(); i++) {
            String paraID 	=	paraIDs.get(i);
            String paraExpr	=	"coalesce(p2."+paraID+",p1."+paraID+")";

            //[,$,]在正则表达式替换式需要转义
            exprValue	=	exprValue.replaceAll( "\\[\\$" + paraID + "\\]", paraExpr);
            exprFilter	=	exprFilter.replaceAll("\\[\\$" + paraID + "\\]", paraExpr);
        }



        //3.6.2 开始替换
        exprValue	=	exprValue.replaceAll("\\[", "").replaceAll("\\]", "");
        exprFilter	=	exprFilter.replaceAll("\\[", "").replaceAll("\\]", "");

        //拆解monthId
        String sqlsel = "";
        //where后过滤字段
        String monthPlace = "";
        //过滤字段不带等号
        String noEqMonthPlace = "Data_Year";
        String expression = dataSource.getExpression().toUpperCase();
        if (expression.indexOf("'[%MONTHID]'") != -1 &&
                expression.indexOf("WHERE") != -1  ){
            List<String> mothns = new ArrayList<String>();
            String strMark = "WHERE";
            if(expression.indexOf(" AND") != -1){
                strMark = " AND";
            }
            String expressionNoConf = expression.substring(0,expression.indexOf(strMark));
            List<Map> sourceDatas = getDataSourceValues(measure.getSourceId(),expressionNoConf);
            monthPlace = expression.substring(expression.indexOf(strMark) + strMark.length(),expression.indexOf("'[%MONTHID]'"));
            String key = monthPlace.trim().substring(0,monthPlace.trim().length()-1).trim();
//            sourceDatas.stream().forEach(s->{if(!mothns.contains("'"+s.get(key).toString()+"'")) {mothns.add("'"+s.get(key).toString()+"'");}});
            sourceDatas.stream().filter(s->!mothns.contains("'"+s.get(key).toString()+"'")).forEach(s->mothns.add("'"+s.get(key).toString()+"'"));
            sqlsel = "(" + mothns.toString().replace("[","").replace("]","").substring(0,mothns.toString().length() - 2) + ")";
        }
        if (sqlsel != "" && monthPlace != ""){
            noEqMonthPlace = monthPlace.trim().substring(0,monthPlace.trim().length()-1).trim();
            v_sourceExpr = v_sourceExpr.toUpperCase().replace(monthPlace," " + noEqMonthPlace + " in " + sqlsel);
            v_sourceExpr = v_sourceExpr.replace("'[%MONTHID]'","");
        }
        //日期取得
        String dateFrm = this.date;
        if(measure.getCountPeriod().equals("00")){//月
            dateFrm = this.date.substring(0,7);
        }else if(measure.getCountPeriod().equals("01")){//季
            dateFrm = getSeasonString(this.date);
        }else if(measure.getCountPeriod().equals("02")){//年
            dateFrm = this.date.substring(0,4);
        }
        //4. 拼接SQL语句
        //判断是否存在该列
        boolean isExistsCol = true;
        try{
            this.jdbcManager.execute("select " + exprDistrictID + " from (" + v_sourceExpr + ") a limit 1");
        }catch (Exception e){
            try{
                this.jdbcManager.execute("select region_cd from (" + v_sourceExpr + ") a limit 1");
            }catch (Exception ex){
                isExistsCol = false;
                System.out.print(e.toString());
            }
        }
        String sqlStat = "insert into "+this.resultTable+" (month_id,date,measure_id,object_id,district_id,value,rowmark)" +
                "select distinct " +
                "m."+  noEqMonthPlace +" as month_id,'" + dateFrm +
                "'			 as date," +
                "'" + exprMeasureID + "' as measure_id," +
                (!"".equals(exprObjectID)?
                        "m."   + exprObjectID:"''")+ " as object_id," +
                (isExistsCol?"m."   + exprDistrictID: "''") +" as district_id," +
                "ifnull(sum(" + exprValue + "),0) as value, '" + start + "' " +
                "from (" + v_sourceExpr
                ;

        //4.1 如果存在过滤条件，则添加过滤条件
        if (null != exprFilter && !"".equals(exprFilter)) {
            sqlStat	= sqlStat + " and (" + exprFilter+") ";
        }
        //判断是否来自汇总表
        if (v_formulaExpr.toLowerCase().indexOf("ix_val")>-1){
            sqlStat += " and ix_id = '" +exprMeasureID+ "'";
        }
        sqlStat = sqlStat + ") m ";



        //4.1 如果存在参数，则需要连接参数表
        if (paraIDs.size() > 0){

            sqlStat	= sqlStat
                    + " left join (" + this.paramHandler.getParameterSource(paraIDs)  + ") p1 on 1 = 1"
                    + " left join (" + this.paramHandler.getObjParaSource(paraIDs)    + ") p2 on m.object_id=p2.object_id"
            ;
        }

        //4.2 如果数据源是数据集, 则使用数据源定义的对象维度；否则使用笛卡尔积
        if ("00".equals(v_resultTypeID)){
            //去掉维度匹配，默认全匹配
//            sqlStat	= sqlStat + " where m.object_id = m." + exprObjectID;
        }
        else{
            sqlStat	= sqlStat + " where 1=1";
        }

        sqlStat = sqlStat +  " group by "+(!"".equals(exprObjectID)?   "m. " + exprObjectID + ",":"")
                + "m." + noEqMonthPlace + (isExistsCol? ",m." + exprDistrictID:"");

        return sqlStat;
    }

    /**
     * 解析复合指标
     * @param measure
     * @return
     * @throws Exception
     */
    protected String parseCompoundMeasure(IMeasure measure) throws Exception {
        String v_resultTypeID = measure.getResultTypeId(); // 结果类型；00：集合；01：标量值
        String v_formulaExpr = measure.getFormula(); // 公式值

        // 1. 预处理判断
        if (null == v_resultTypeID || null == v_formulaExpr) {
            throw new Exception("指标:["+measure.getMeasureName()+"] 配置不完整");
        }

        // 2. 把公式中的大括号替换掉
        v_formulaExpr = v_formulaExpr.replaceAll("\\{", "").replaceAll("\\}","");

        // 3. 开始解析
        // 3.1 查找数据源属性，查找可能引用的参数
        List<String> paraIDs = measure.getParams();

        String exprProjectID = "";
        String exprMeasureID = "";
        String exprValue = "";
        String exprJoinClause = "";

        exprProjectID = this.context.getEnv("projectID");
        exprMeasureID = measure.getMeasureId();

        String[] formularExprs = v_formulaExpr.split("\\?");

        // 3.2 设置值表达式
        if (formularExprs.length > 0) {
            IExpression expr = new StackExpression(formularExprs[0]);

            expr.doParse();

            exprValue = expr.getParseResult();
        }

        List<IMeasure> relaMeasures = this.measureService.getRelaMeasure(measure.getMeasureId());
        // 3.4 连接各上游指标
        if (relaMeasures == null || relaMeasures.size() == 0) {
            throw new Exception("复合指标:["+measure.getMeasureName()+"]公式配置错误,没有依赖的指标");
        }

        for (int i = 0; i < relaMeasures.size(); i++) {
            IMeasure relaMeasure = relaMeasures.get(i);
            String relyMeasureID = relaMeasure.getMeasureId();
            String relyResultTypeID = relaMeasure.getResultTypeId();

            // 替换表达式中的指标名称，为数据库字段名称
            exprValue = exprValue.replaceAll("\\[@" + relyMeasureID+ "\\]", "m" + i + ".value");

            String meaSource = this.getMeasureResultSQL(relaMeasure);

            exprJoinClause = exprJoinClause + " left join (" + meaSource+ ") m" + i + " on ";

            // 如果指标是标量指标，则使用笛卡尔积联合
            String joinStr = "1=1";//全匹配
            if(i>0)
                joinStr = "m"+String.valueOf(i-1)+".district_id = m"+String.valueOf(i)+".district_id and m"+
                        String.valueOf(i-1)+".month_id = m"+String.valueOf(i)+".month_id and m"+
                        String.valueOf(i-1)+".object_id = m"+ String.valueOf(i)+".object_id ";
//            if ("01".equals(relyResultTypeID)) {
//                joinStr = "1=1";
//            } else {
//                joinStr = "a.object_id = m" + i + ".object_id";
//            }

            exprJoinClause = exprJoinClause + joinStr + " \n";
        }

        // 3.5 替换参数名称
        for (int i = 0; i < paraIDs.size(); i++) {
            String paraID = paraIDs.get(i);
            String paraExpr = "coalesce(p2." + paraID + ",p1." + paraID + ")";

            // [,$,]在正则表达式替换式需要转义
            exprValue = exprValue.replaceAll("\\[\\$" + paraID + "\\]",paraExpr);
        }

        //拆解monthId

        //日期取得
        String dateFrm = this.date;
        if(measure.getCountPeriod().equals("00")){//月
            dateFrm = this.date.substring(0,7);
        }else if(measure.getCountPeriod().equals("01")){//季
            dateFrm = getSeasonString(this.date);
        }else if(measure.getCountPeriod().equals("02")){//年
            dateFrm = this.date.substring(0,4);
        }

        // 4. 拼接SQL语句
        String sqlStat = "insert into "+this.resultTable+" (month_id,date,measure_id,object_id,district_id,value,rowmark)\n"
                + "select distinct "
                + "m0.month_id as month_id,'" + dateFrm
                + "' 			 as date,"
                + "'"+ exprMeasureID+ "' as measure_id,"
                + "m0.object_id as object_id,\n"
                + "m0.district_id as district_id,\n"
                + "ifnull("+ exprValue+ ",0) as value, \n"
                + "'" + start + "' "
                + "from bsc_measure a " + exprJoinClause;

        // 4.1 如果存在参数，则需要连接参数表
        if (paraIDs.size() > 0) {

            sqlStat	= sqlStat
                    + " left join (" + this.paramHandler.getParameterSource(paraIDs)  + ") p1 on 1 = 1"
                    + " left join (" + this.paramHandler.getObjParaSource(paraIDs)    + ") p2 on 1 = 1";
        }

        sqlStat = sqlStat + " where a.measure_id='" + exprMeasureID + "'";

        return sqlStat;
    }

    /**
     * 解析导入指标
     * @param measure
     * @return
     * @throws Exception
     */
    protected String parseExternalMeasure(IMeasure measure) throws Exception {
        String sql  = "insert into " + this.resultTable + "(month_id,project_id,measure_id,object_id,value,rowmark)"
                + "select "
                + "'[%monthID]' as month_id,"
                + "a.project_id as project_id, "
                + "a.measure_id as measure_id,"
                + "a.object_id  as object_id,"
                + "ifnull(a.value,0) as value, "
                + "'" + start + "' "
                + "from bsc_proj_mea_imp a "
                + "where a.project_id = '"+this.date+"' and a.measure_id = '"+measure.getMeasureId()+"' "
                + "and a.cycle_id = '"+this.getCycleIdByMonth(this.context.getEnv("cycleTypeID"), this.monthID)+"' "
                + "and a.year_id = '"+this.monthID.substring(0, 4)+"' ";
        return sql;
    }

    /**
     * 解析BSC积分公式
     * @return
     * @throws Exception
     */
    protected String parseProjectMeasureFormula(IMeasure measure) throws Exception {
        String projectID = this.context.getEnv("projectID");
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("link_id",measure.getObjLinkId());
        List<String> obj = new ArrayList<String>();
        String sqlsel = "";
        if(measure.getObjLinkId()== null){
            this.getDimLink(measure.getObjLinkId());
            Iterator<String> iter = this.dimlikCache.keySet().iterator();

            while(iter.hasNext()){
                String key = iter.next();
                List<Map> source = this.dimlikCache.get(key);
                source.stream().forEach(s->obj.add(" select '"+
                        s.get(key.split("%")[1].toUpperCase().toString()).toString()+"' as object_id from dual union "));
            }
            //拼接所有维度
            sqlsel = obj.toString().replace("[","").replace("]","").substring(0,obj.toString().length() - 8).replaceAll(",","");

        }else{
            List<Map> dimLink  = this.getDimLink(measure.getObjLinkId());
            dimLink.stream().forEach(s->obj.add(" select '"+s.get(measure.getObjLinkId().toUpperCase()).toString()+"' as object_id from dual union "));
            //取得当前维度
            sqlsel = obj.toString().replace("[","").replace("]","").substring(obj.toString().length() - 8).replaceAll(",","");
        }
        //日期取得
        String dateFrm = this.date;
        if(measure.getCountPeriod().equals("00")){//月
            dateFrm = this.date.substring(0,7);
        }else if(measure.getCountPeriod().equals("01")){//季
            dateFrm = getSeasonString(this.date);
        }else if(measure.getCountPeriod().equals("02")){//年
            dateFrm = this.date.substring(0,4);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("insert into " +this.bsc_proj_mea_obj_val_measure+" (month_id,date,measure_id,object_id,district_id,value,source_id,rowmark)\n");
        sb.append("select distinct ");
        sb.append("c.month_id as month_id,");
        sb.append("coalesce(c.date,DATE_FORMAT(SYSDATE(),'%Y-%m-%d'))	as date,");
        sb.append("'"+ measure.getMeasureId() + "' as measure_id,");
        sb.append("c.object_id  as object_id,\n");
        sb.append("c.district_id  as district_id,\n");
        sb.append("c.value as value, \n");
        sb.append("a.source_id, \n");
        sb.append("'"+start+"' ");
        sb.append("from bsc_measure a ");
        sb.append(" left join ");
        sb.append(this.resultTable + " c ");
        sb.append("on c.measure_id =  a.measure_id");
        sb.append(" left join (" + sqlsel + ") m ");
        sb.append("on c.object_id = m.object_id or (c.district_id = m.object_id and c.object_id != c.district_id)");
        sb.append(" where a.measure_id='" + measure.getMeasureId() + "'");
        sb.append(" and c.date = '"+dateFrm+"'");
        sb.append(" and length(c.month_id)<=10");

        return sb.toString();
    }

    /**
     * 获取数据源对象
     * @return
     */
    protected IDataSource getDataSource(String sourceId) throws Exception{
        IDataSource dataSource = this.dataScourceCache.get(sourceId);
        if(dataSource == null) {
            dataSource = this.dataSourceService.getDataSourceById(sourceId);
            this.dataScourceCache.put(sourceId, dataSource);
        }
        return dataSource;
    }

    /**
     * 获取数据源目标表数据
     * @return
     */
    protected List<Map> getDataSourceValues(String sourceId,String express) throws Exception{
        //数据源值缓存
        List<Map> sourceDatas = this.dataScourceValuesCache.get(sourceId);
        if(sourceDatas == null ){
            sourceDatas = this.jdbcManager.queryForList(express);
            dataScourceValuesCache.put(sourceId,sourceDatas);
        }
        return sourceDatas;
    }

    /**
     * 取得码表对象
     * @param linkId
     * @return
     * @throws Exception
     */
    protected List<Map> getDimLink(String linkId) throws  Exception{
        List<Map> dimLink = this.dimlikCache.get(linkId);
        if(dimLink == null){
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("link_id",linkId);
            List<DimLink> dl = this.dimLinkService.queryDimLinkList(paramMap);
            if(linkId == null || linkId.equals("")){

                for(DimLink dim:dl){
                    if(dimLink==null)
                        dimLink = this.jdbcManager.queryForList(dim.getSource_expression());
                    this.dimlikCache.put(dim.getLink_id()+"%"+dim.getId_field(),
                            this.jdbcManager.queryForList(dim.getSource_expression()));
                }
            }else {
                dimLink = this.jdbcManager.queryForList(dl.get(0).getSource_expression());
                this.dimlikCache.put(linkId,dimLink);
            }
        }
        return dimLink;
    }

    /**
     * 返回指标结果集SQL
     * @param measure
     * @return
     */
    protected String getMeasureResultSQL(IMeasure measure) {
        //日期取得
        String dateFrm = this.date;
        if(measure.getCountPeriod().equals("00")){//月
            dateFrm = this.date.substring(0,7);
        }else if(measure.getCountPeriod().equals("01")){//季
            dateFrm = getSeasonString(this.date);
        }else if(measure.getCountPeriod().equals("02")){//年
            dateFrm = this.date.substring(0,4);
        }
        String v_sql = "select object_id,district_id,value,month_id from "+this.resultTable+" where date='"
                + dateFrm
                + "' and measure_id = '" + measure.getMeasureId() + "'";

        // 如果指标是标量指标，则数据源再加上对象ID
        if ("01".equals(measure.getResultTypeId())) {
            v_sql = v_sql + " and object_id='"+ IMeasure.OBJ_NAME_SCALE_MEASURE + "'";
        }
        return v_sql;
    }

    /**
     * 更新指标计算执行命令
     * @throws Exception
     */
    protected void insertExeCommand(IMeasure measure,String command,int order_id) throws Exception {
        //日期取得
        String dateFrm = this.date;
        if(measure.getCountPeriod().equals("00")){//月
            dateFrm = this.date.substring(0,7);
        }else if(measure.getCountPeriod().equals("01")){//季
            dateFrm = getSeasonString(this.date);
        }else if(measure.getCountPeriod().equals("02")){//年
            dateFrm = this.date.substring(0,4);
        }
        String sql = "insert into  "+this.commandTable+"(date,measure_id,exe_order_id,exe_command,rowmark) " +
                "values('" + dateFrm + "',"+"'"+measure.getMeasureId()+"',"+ order_id +
                ",'"+command.replaceAll("'", "''")+"','"+start+"' )";
        this.jdbcManager.execute(sql);
    }

    /**
     * 插入方案指标计算命令
     * @param measure
     * @param command
     * @throws Exception
     */
    protected void insertProjectMeasureCommand(IMeasure measure, String command) throws Exception {
        //日期取得
        String dateFrm = this.date;
        if(measure.getCountPeriod().equals("00")){//月
            dateFrm = this.date.substring(0,7);
        }else if(measure.getCountPeriod().equals("01")){//季
            dateFrm = getSeasonString(this.date);
        }else if(measure.getCountPeriod().equals("02")){//年
            dateFrm = this.date.substring(0,4);
        }
        String sql = "insert into " +this.bsc_proj_val_cmd_measure+"(date,measure_id,exe_order_id,exe_command,rowmark) "
                +"values('"+dateFrm+"','"+measure.getMeasureId()+"','1','"+command.replaceAll("'", "''")+"','"+start+"' )";
        this.jdbcManager.execute(sql);
    }

    /**
     * 根据周期类型和月份 获取周期ID
     * @param cycleTypeId
     * @param monthId
     * @return
     */
    protected String getCycleIdByMonth(String cycleTypeId, String monthId) {
        int month = 0;
        String cycleId = "";
        if("00".equals(cycleTypeId)) {
            month = Integer.parseInt(monthId.substring(4));
            //月份
            cycleId = String.valueOf(month);
        }else if("01".equals(cycleTypeId)) {
            month = Integer.parseInt(monthId.substring(4));
            //季度
            cycleId = String.valueOf(month/3 + (month%3 > 0 ? 1 : 0));
        }else {
            //年份
            cycleId = "1";
        }
        return cycleId;
    }

    /**
     * 替换执行命令中的环境变量,将传入字符串转换为可执行SQL
     * @param command
     * @return
     */
    protected String replaceContextVar(String command) {
        Iterator<String> keyIter = this.context.keySet().iterator();
        while(keyIter.hasNext()) {
            String key = keyIter.next();
            String value = this.context.getEnv(key);
            command = command.replaceAll("\\[\\%"+key+"\\]", value);
        }
        return command;
    }

    public String isCycleEnd(String monthId) {
        String cycleTypeId = this.context.getEnv("cycleTypeID");
        int t = 1;
        int m = 0;
        if("00".equals(cycleTypeId)) {
            //月份
            t = 1;
            m = Integer.parseInt(monthId.substring(4));
        }else if("01".equals(cycleTypeId)) {
            //季度
            t = 3;
        }else {
            //年份
            t = 12;
        }

        if(m%t == 0)
            return "Y";

        return "N";
    }

    public void setJdbcManager(JdbcManager jdbcManager) {
        this.jdbcManager = jdbcManager;
    }


    public ThreadStatus query() {
        return this.status;
    }


    public void setThreadStop() {
        this.run = false;
    }
    public void setSession(HttpSession session)
    {
        this.session = session;
    }

    public HttpSession getSession()
    {
        return this.session;
    }
}
