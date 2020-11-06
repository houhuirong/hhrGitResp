package com.nantian.erp.salary.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.dao.ErpTodoListForSalaryMapper;

/** 
 * Description: salary工程首页service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月27日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
public class ErpHomePageService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired
	private ErpTodoListForSalaryMapper todoListForSalaryMapper;
	
	/**
	 * Description: 首页-待办事项
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月27日 下午14:47:28
	 */
	public RestResponse findTodoList(String token){
		logger.info("进入findTodoList方法，参数是：token="+token);
		List<Map<String,Object>> todoList = new ArrayList<>();
		try {
			//从缓存中获取登录用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			
			// SXG 2019-09-30 Begin---------------
			SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
			Calendar calendar = Calendar.getInstance();

			Date date = calendar.getTime(); 
			String socialSecMonth = null;//社保基数提交月份
			socialSecMonth = monthFormat.format(date);
			logger.info("positiveMonth:" + socialSecMonth);
			// SXG 2019-09-30 End  ---------------
			
			//待我处理的上岗工资单
			Map<String,Object> periodPayrollTodo = new HashMap<>();
			periodPayrollTodo.put("menuUrl", "nantian-erp/salary/payroll/uploadsalary/waitforme");
			periodPayrollTodo.put("menuName", "待我处理的上岗工资单");
			Map<String,Object> periodParams = new HashMap<>();
			periodParams.put("personId", erpUser.getUserId());
			periodParams.put("status", 1);
			//periodParams.put("positiveMonth", socialSecMonth);
			
			periodPayrollTodo.put("count", todoListForSalaryMapper.countPayrollTodo(periodParams));
			todoList.add(periodPayrollTodo);
			
			//待我处理的转正工资单
			Map<String,Object> positivePayrollTodo = new HashMap<>();
			positivePayrollTodo.put("menuUrl", "nantian-erp/salary/payroll/positivesalary/waitforme");
			positivePayrollTodo.put("menuName", "待我处理的转正工资单");
			Map<String,Object> positiveParams = new HashMap<>();
			positiveParams.put("personId", erpUser.getUserId());
			positiveParams.put("status", 2);
			positiveParams.put("positiveMonth", socialSecMonth);
			
			positivePayrollTodo.put("count", todoListForSalaryMapper.countPayrollTodo(positiveParams));
			todoList.add(positivePayrollTodo);
			
			return RestUtils.returnSuccess(todoList);
		} catch (Exception e) {
			logger.error("findTodoList方法发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致查询失败！");
		}
	}
	
}

