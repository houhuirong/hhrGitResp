package com.nantian.erp.hr.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nantian.erp.hr.data.dao.ErpPostMapper;

@Service
public class ErpPostService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ErpPostMapper postMapper;

	/**
	 * 从字典表中查询全部的岗位类别
	 * @return
	 */
	public List<Map<String,Object>> findAllCategory() {
		logger.info("进入findAllCategory方法，无参数");
		List<Map<String,Object>> list = null;
		try {
			list = postMapper.findAllCategoryFromAdminDic();
		} catch (Exception e) {
			logger.info("findAllCategory方法出现异常：" + e.getMessage(),e);
		}
		return list;
	}

}
