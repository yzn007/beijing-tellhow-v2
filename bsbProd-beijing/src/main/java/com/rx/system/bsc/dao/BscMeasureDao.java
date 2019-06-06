package com.rx.system.bsc.dao;

import java.util.List;
import java.util.Map;

import com.rx.system.bsc.calc.service.IMeasure;

/**
 * 平衡计分卡积分指标操作接口
 * @author chenxd
 *
 */
public interface BscMeasureDao {
	/**
	 * 根据方案ID获取所有需要计算的指标
	 */
	public List<IMeasure> getMeasureByProjectId(String projectId)throws Exception;

	
	/**
	 * 获取指标计算依赖的指标
	 */
	public List<IMeasure> getRelaMeasure(String measureId) throws Exception;
	
	/**
	 * 获取方案所有衡量指标积分公式
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getProjectMeasure(String projectId) throws Exception;

	/**
	 * 取得运行的有效指标
	 * @return
	 * @throws Exception
	 */
	public List<IMeasure> getValidMeasure() throws Exception;

	/**
	 * 取得运行的有效指标-分页
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<IMeasure> getValidMeasureMap(Map map) throws Exception;

	/**
	 * 取得指标计算总数
	 * @return
	 * @throws Exception
	 */
	public int getValidMeasureCount() throws Exception;

}
