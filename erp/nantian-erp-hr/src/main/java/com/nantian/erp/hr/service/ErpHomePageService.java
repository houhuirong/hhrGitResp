package com.nantian.erp.hr.service;

import java.util.ArrayList;
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
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.ErpEmployeeEntryMapper;
import com.nantian.erp.hr.data.dao.ErpTodoListForHrMapper;

/** 
 * Description: HR工程首页service
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
	private ErpTodoListForHrMapper todoListForHrMapper;
	@Autowired
	private ErpEmployeeEntryMapper employeeEntryMapper;
	
	/**
	 * Description: 首页-待办事项
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月27日 下午13:35:31
	 */
	public RestResponse findTodoList(String token){
		logger.info("进入findTodoList方法，参数是：token="+token);
		List<Map<String,Object>> todoList = new ArrayList<>();
		try {
			//从缓存中获取登录用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			Integer currentEmpId = erpUser.getUserId();//当前登录人的员工ID
			
			//待我审批的岗位申请
			Map<String,Object> postApplyTodo = new HashMap<>();
			postApplyTodo.put("menuUrl", "nantian-erp/hr/post/applyForMe");
			postApplyTodo.put("menuName", "待我审批的岗位申请");
			postApplyTodo.put("count", todoListForHrMapper.countPostApplyTodo(currentEmpId));
			todoList.add(postApplyTodo);
			
			//待我处理的简历筛选
			Map<String,Object> resumeFilterTodo = new HashMap<>();
			resumeFilterTodo.put("menuUrl", "nantian-erp/hr/interviewer/resumeFilter");
			resumeFilterTodo.put("menuName", "待我处理的简历筛选");
			Map<String,Object> resumeFilterParams = new HashMap<>();
			resumeFilterParams.put("isValid", true);
			resumeFilterParams.put("personId", currentEmpId);
			resumeFilterParams.put("status", DicConstants.INTERVIEW_STATUS_RESUME_SCREENING);
			resumeFilterTodo.put("count", todoListForHrMapper.countInterviewerTodo(resumeFilterParams));
			todoList.add(resumeFilterTodo);
			
			//待我处理的面试预约
			Map<String,Object> interviewOrderTodo = new HashMap<>();
			interviewOrderTodo.put("menuUrl", "nantian-erp/hr/interviewer/interviewOrder");
			interviewOrderTodo.put("menuName", "待我处理的面试预约");
			Map<String,Object> interviewOrderParams = new HashMap<>();
			interviewOrderParams.put("isValid", true);
			//interviewOrderParams.put("personId", erpUser.getUserId());
			interviewOrderParams.put("status", DicConstants.INTERVIEW_STATUS_ORDER_INTERVIEW);
			interviewOrderTodo.put("count", todoListForHrMapper.countInterviewerTodo(interviewOrderParams));
			todoList.add(interviewOrderTodo);
			
			//待我处理的面试
			Map<String,Object> interviewTodo = new HashMap<>();
			interviewTodo.put("menuUrl", "nantian-erp/hr/interviewer/page");
			interviewTodo.put("menuName", "待我处理的面试");
			Map<String,Object> interviewParams = new HashMap<>();
			interviewParams.put("isValid", true);
			interviewParams.put("personId", currentEmpId);
			interviewParams.put("status", DicConstants.INTERVIEW_STATUS_IN_THE_INTERVIEW);
			interviewTodo.put("count", todoListForHrMapper.countInterviewerTodo(interviewParams));
			todoList.add(interviewTodo);
			
			//待我审批的offer
			Map<String,Object> offerApproveTodo = new HashMap<>();
			offerApproveTodo.put("menuUrl", "nantian-erp/hr/offer/offerApprove");
			offerApproveTodo.put("menuName", "待我审批的offer");
			Map<String,Object> offerApproveParams = new HashMap<>();
			offerApproveParams.put("personId", currentEmpId);
			offerApproveParams.put("status", DicConstants.INTERVIEW_STATUS_OFFER_APPROVE);
			offerApproveTodo.put("count", todoListForHrMapper.countInterviewerTodo(offerApproveParams));
			todoList.add(offerApproveTodo);
			
			//待我处理的offer
			Map<String,Object> offerTodo = new HashMap<>();
			offerTodo.put("menuUrl", "nantian-erp/hr/offer/pendingOffer");
			offerTodo.put("menuName", "待发送的offer");
			offerTodo.put("count", todoListForHrMapper.countOfferTodo(DicConstants.OFFER_STATUS_WAITING));
			todoList.add(offerTodo);
			
			//待我处理的入职
			if(erpUser.getRoles().contains(1)){//hr可以看到所有部门的待入职
				Map<String,Object> entryTodo = new HashMap<>();
				entryTodo.put("menuUrl", "nantian-erp/hr/entry/waitingforme");
				entryTodo.put("menuName", "待我处理的入职");
				Map<String,Object> entryParams = new HashMap<>();
//				entryParams.put("personId", currentEmpId);
/*				entryParams.put("status", 1);
				if(erpUser.getRoles().contains(1)){//hr可以看到所有部门的待入职
					Map<String,Object> hrQueryMap=new HashMap<String, Object>();	
					entryParams.put("hrFlag", true);
				}
				entryTodo.put("count", todoListForHrMapper.countEntryTodo(entryParams));
				*/
				//查询所有待我处理
				entryParams.put("currentPersonID", erpUser.getUserId());			
				List<Map<String, Object>> list=this.employeeEntryMapper.findAllWaitingForMe(entryParams);
				if(erpUser.getRoles().contains(1)){//hr可以看到所有部门的待入职
					Map<String,Object> hrQueryMap=new HashMap<String, Object>();	
					hrQueryMap.put("hrFlag", true);
					list.addAll(this.employeeEntryMapper.findAllWaitingForMe(hrQueryMap));
				}
				entryTodo.put("count", list.size());
				todoList.add(entryTodo);
			}
			
			
			//待我处理的转正
			Map<String,Object> positiveTodo = new HashMap<>();
			positiveTodo.put("menuUrl", "nantian-erp/hr/postive/waitingforme");
			positiveTodo.put("menuName", "待我处理的转正");
			Map<String,Object> positiveParams = new HashMap<>();
			positiveParams.put("personId", currentEmpId);
			positiveParams.put("status", 1);
			positiveTodo.put("count", todoListForHrMapper.countPositiveTodo(positiveParams));
			todoList.add(positiveTodo);
			
			//待我审批的部门调动申请
			Map<String,Object> depTransfTodo = new HashMap<>();
			depTransfTodo.put("menuUrl", "nantian-erp/department/departmentManage/transfer-work/transfer-pending");
			depTransfTodo.put("menuName", "待我审批的部门调动申请");
			depTransfTodo.put("count", todoListForHrMapper.countDepTransfTodo(currentEmpId));
			todoList.add(depTransfTodo);
			
			//待我处理的离职
			if(erpUser.getRoles().contains(1)){//hr可以看到所有部门的待入职
				Map<String,Object> dimissionTodo = new HashMap<>();
				dimissionTodo.put("menuUrl", "nantian-erp/hr/dimission/findapply");
				dimissionTodo.put("menuName", "待我处理的离职");
				dimissionTodo.put("count", todoListForHrMapper.countDimissionTodo(currentEmpId));
				todoList.add(dimissionTodo);
			}
			
			return RestUtils.returnSuccess(todoList);
		} catch (Exception e) {
			logger.error("findTodoList方法发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致查询失败！");
		}
	}
	
}

