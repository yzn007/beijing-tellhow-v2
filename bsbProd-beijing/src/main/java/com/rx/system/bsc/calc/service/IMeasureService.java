package com.rx.system.bsc.calc.service;

import com.rx.system.domain.BscMeasure;

import java.util.List;
import java.util.Map;

/**
 * 指标接口
 * 考核执行程序调用该接口中方法获取方案的所以指标
 * @author chenxd
 *
 */
public interface IMeasureService {
	
	/**
	 * 获取方案需要计算的全部指标
	 * @param projectId	方案ID
	 * @return List  指标接口列表
	 * @throws Exception
	 */
	public List<IMeasure> getMeasureByProjectId(String projectId) throws Exception;

	/**
	 * 取得运行的有效指标
	 * @return
	 * @throws Exception
	 */
	public List<IMeasure> getValidMeasure() throws Exception;

	/**
	 * 取得运行的有效指标-分页
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public List<IMeasure> getValidMeasureMap(Map m) throws Exception;

	/**
	 * 取得指标计算总数
	 * @return
	 * @throws Exception
	 */
	public int getValidMeasureCount() throws Exception;
	
	/**
	 * 获取指标计算依赖的指标
	 * @param measureId
	 * @return
	 * @throws Exception
	 */
	public List<IMeasure> getRelaMeasure(String measureId) throws Exception;
	
	/**
	 * 获取方案直接隶属的指标
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getProjectMeasure(String projectId) throws Exception;
	
	
}
