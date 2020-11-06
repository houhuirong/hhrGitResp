package com.nantian.erp.hr.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nantian.erp.hr.data.dao.ErpProjectInfoMapper;
import com.nantian.erp.hr.data.model.ErpProjectInfo;

@Service
public class ErpProjectService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ErpProjectInfoMapper projectInfoMapper;
	
	public List<ErpProjectInfo> findAll() {
		logger.info("进入findAll方法，查询所有项目信息,无参数");
		List<ErpProjectInfo> list = null;
		try {
			list = projectInfoMapper.findAllProjectInfo();
		} catch (Exception e) {
			logger.error("findAll方法出现异常：" + e.getMessage(),e);
		}
		return list;
	}
	
}