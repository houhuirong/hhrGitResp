package com.nantian.erp.hr.service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.ErpAdminDicMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostOrderMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostOrderRecordMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostReexamMapper;
import com.nantian.erp.hr.data.dao.PositionOperReordMapper;
import com.nantian.erp.hr.data.dao.ErpOfferMapper;
import com.nantian.erp.hr.data.dao.ErpPostMapper;
import com.nantian.erp.hr.data.dao.ErpRecordMapper;
import com.nantian.erp.hr.data.dao.ErpResumeMapper;
import com.nantian.erp.hr.data.model.ErpAdminDic;
import com.nantian.erp.hr.data.model.ErpOffer;
import com.nantian.erp.hr.data.model.ErpPost;
import com.nantian.erp.hr.data.model.ErpRecord;
import com.nantian.erp.hr.data.model.ErpResume;
import com.nantian.erp.hr.data.model.ErpResumePost;
import com.nantian.erp.hr.data.model.ErpResumePostOrder;
import com.nantian.erp.hr.data.model.ErpResumePostOrderRecord;
import com.nantian.erp.hr.data.model.ErpResumePostReexam;
import com.nantian.erp.hr.data.model.PositionOperRecond;
import com.nantian.erp.hr.util.AesUtils;
import com.nantian.erp.hr.util.RestTemplateUtils;

/** 
 * Description: 面试service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月06日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
//@PropertySource("classpath:config/host.properties")
@PropertySource(value= {"classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpInterviewService {
	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	/*
	 * 从配置文件中获取Email相关属性
	 */
	@Value("${email.service.host}")
	private  String emailServiceHost;//邮件服务的IP地址和端口号
	@Value("${environment.type}")
	private  String environmentType;//环境类型（根据该标识，决定邮件的发送人、抄送人、收件人）
	@Value("${test.email.frommail}")
	private String testEmailFrommail;//测试环境发件人
	@Value("${test.email.bcc}")
	private String testEmailBcc;//测试环境抄送人
	@Value("${test.email.tomail}")
	private String testEmailTomail;//测试环境收件人
	@Value("${prod.email.interview.bcc}")
	private String prodEmailInterviewBcc;//生产环境抄送人
	
	@Autowired
	private ErpRecordMapper recordMapper;
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private ErpResumePostMapper resumePostMapper;
	@Autowired
	private ErpResumePostOrderMapper resumePostOrderMapper;
	@Autowired
	private ErpResumePostOrderRecordMapper resumePostOrderRecordMapper;
	@Autowired
	private ErpResumePostReexamMapper resumePostReexamMapper;
	@Autowired
	private ErpOfferMapper offerMapper;
	@Autowired
	private ErpOfferService offerService;
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	private ErpAdminDicMapper adminDicMapper;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private ErpPostMapper postMapper;
	@Autowired
	PositionOperReordMapper operRecordMapper;
	@Autowired
	private RestTemplateUtils restTemplateUtils;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * Description: 简历筛选-页面首页显示的简历信息列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月07日 下午14:38:31
	 */
	public RestResponse resumeFilterQueryList(String token) {
		logger.info("resumeFilterQueryList方法开始执行，参数是：token="+token);
		List<Map<String, Object>> findInfoPage = new ArrayList<>();
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();// 角色
			/*
			 * 判断当前登录用户的角色是否有查看数据的权限
			 */
			/*List<Integer> privilegeRoles = new ArrayList<>();
			privilegeRoles.add(1);//HR角色
			privilegeRoles.add(4);//admin角色
			boolean flag = false;
			for(Integer privilegeRole : privilegeRoles) {
				if(roles.contains(privilegeRole)) {
					flag = true;
				}
			}
			if(!flag) {
				return RestUtils.returnSuccess(findInfoPage);
			}*/
			
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isValid", true);
			paramsMap.put("status", DicConstants.INTERVIEW_STATUS_RESUME_SCREENING);
			if(!roles.contains(1)) {
				paramsMap.put("personId", id);
			}
			findInfoPage = resumePostMapper.findResumePostInfoByParams(paramsMap);
			return RestUtils.returnSuccess(findInfoPage);
		} catch (Exception e) {
			logger.info("resumeFilterQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 简历筛选-通过
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月08日 上午10:23:53
	 */
	@Transactional
	public RestResponse resumeFilterPass(String token,Map<String,Object> params) {
		logger.info("resumeFilterPass方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			
			/*
			 * 新增面试预约信息
			 */
			String attention = params.get("attention")==null?null:String.valueOf(params.get("attention"));//面试注意事项
			String contactId = String.valueOf(params.get("contactId"));//联系人
			String placeId = String.valueOf(params.get("placeId"));//面试地点
			ErpResumePostOrder resumePostOrder = new ErpResumePostOrder();
			resumePostOrder.setInterviewId(interviewId);
			resumePostOrder.setAttention(attention);
			resumePostOrder.setContactId(contactId);
			resumePostOrder.setPlaceId(placeId);
			resumePostOrderMapper.insertResumePostOrder(resumePostOrder);
			
			/*
			 * 新增面试记录信息
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setResumeId(resumeId);
			record.setProcessor(username);
			record.setContent("简历筛选通过");
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			/*
			 * 接收前端传递过来的面试官的用户Id（默认是岗位申请人，可修改为其他人）
			 * 修改面试状态为“待预约”
			 */
			Integer personId = params.get("personId")==null?null:Integer.valueOf(String.valueOf(params.get("personId")));//面试官
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setId(interviewId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_ORDER_INTERVIEW);
			resumePost.setPersonId(personId);
			resumePostMapper.updateResumePost(resumePost);
			
			/*
			 * 修改简历的状态为“面试中”
			 */
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setStatus(DicConstants.RESUME_STATUS_IN_THE_INTERVIEW);
			resumeMapper.updateResume(resume);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("resumeFilterPass方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 简历筛选-不通过
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月08日 上午11:57:26
	 */
	@Transactional
	public RestResponse resumeFilterNoPass(String token,Map<String,Object> params) {
		logger.info("resumeFilterNoPass方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String content = String.valueOf(params.get("content"));//面试记录内容
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));//岗位编号
			
			/*
			 * 简历筛选不通过需要给出原因，并写入面试记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			/*
			 * 修改面试状态为“面试不通过”
			 */
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setId(interviewId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
			resumePostMapper.updateResumePost(resumePost);
			
			//在岗位记录表中插入记录
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("简历筛选不通过:"+name); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(username);//当前处理人Id
    		operRec.setPostId(postId);
    		operRecordMapper.addPositionOperReord(operRec);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("resumeFilterNoPass方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 预约面试-页面首页显示的简历信息列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月08日 下午14:59:13
	 */
	public RestResponse interviewOrderQueryList(String token) {
		logger.info("interviewOrderQueryList方法开始执行，参数是：token="+token);
		List<Map<String, Object>> findInfoInPage = new ArrayList<>();
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色数组
			Integer id = erpUser.getUserId();//从用户信息中获取id
			/*
			 * 判断当前登录用户的角色是否有查看数据的权限
			 */
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isValid", true);
			paramsMap.put("status", DicConstants.INTERVIEW_STATUS_ORDER_INTERVIEW);
			//HR角色可以看到全部的待预约的面试，岗位申请人可以看到自己申请岗位的面试预约
			if(roles.contains(1)) {
				paramsMap.put("personId", null);
			}else {
				paramsMap.put("personId", id);
			}
			findInfoInPage = resumePostMapper.findResumePostInfoByParams(paramsMap);
			return RestUtils.returnSuccess(findInfoInPage);
		} catch (Exception e) {
			logger.info("interviewOrderQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 预约面试-根据面试流程Id查询面试预约信息
	 *
	 * @param interviewId 面试流程Id
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月08日 下午15:16:19
	 */
	public RestResponse interviewOrderQueryOrder(Integer interviewId) {
		logger.info("interviewOrderQueryOrder方法开始执行，参数是：interviewId="+interviewId);
		Map<String,Object> resumePostOrder = new HashMap<>();
		try {
			resumePostOrder = resumePostOrderMapper.selectResumePostOrderDetail(interviewId);
			return RestUtils.returnSuccess(resumePostOrder);
		} catch (Exception e) {
			logger.info("interviewOrderQueryOrder方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 预约面试-根据面试流程Id查询面试预约记录
	 *
	 * @param interviewId 面试流程Id
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月15日 下午20:55:33
	 */
	public RestResponse interviewOrderQueryOrderRecord(Integer interviewId) {
		logger.info("interviewOrderQueryOrderRecord方法开始执行，参数是：interviewId="+interviewId);
		List<ErpResumePostOrderRecord> orderRecordList = new ArrayList<>();
		try {
			orderRecordList = resumePostOrderRecordMapper.selectResumePostOrderRecord(interviewId);
			return RestUtils.returnSuccess(orderRecordList);
		} catch (Exception e) {
			logger.info("interviewOrderQueryOrderRecord方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	
	@Transactional
	public RestResponse interviewOrderSuccess(String token,Map<String,Object> params) {
		logger.info("interviewOrderSuccess方法开始执行，参数是：token="+token+",params="+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号			
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号	
			Integer proposerId = Integer.valueOf(String.valueOf(params.get("proposerId")));//面试官id
			String proposerName = String.valueOf(params.get("proposerName"));//面试官名
			Boolean carryPostInfo = params.get("carryPostInfo") == null ? false
					: Boolean.valueOf(String.valueOf(params.get("carryPostInfo")));// 是否携带岗位信息
			Boolean isNext = params.get("isNext") == null ? false
					: Boolean.valueOf(String.valueOf(params.get("isNext")));// true表示邮件发送失败不影响面试流程。
			
			String method = null;//面试方式
			if (params.get("method")!=null){
				method = String.valueOf(params.get("method"));
			}			
			String time = null;//面试时间
			if(params.get("time")!=null) {
				time = String.valueOf(params.get("time"));
			}
			String contactId = null;//联系人
			if(params.get("contactId")!=null) {
				contactId = String.valueOf(params.get("contactId"));
			}
			String placeId = null;//面试地点
			if(params.get("placeId")!=null) {
				placeId = String.valueOf(params.get("placeId"));
			}
			
			ErpResumePostOrder resumePostOrder = new ErpResumePostOrder();
			ErpResumePost resumePost = new ErpResumePost();
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			
			if (params.get("changTimeFlag")!=null){
				//更新面试信息
				resumePostOrder.setInterviewId(interviewId);
				resumePostOrder.setTime(time);
				resumePostOrderMapper.updateResumePostOrder(resumePostOrder);
				
				resumePost.setId(interviewId);
				resumePost.setPersonId(proposerId);
				resumePostMapper.updateResumePost(resumePost);
				
				//新增记录
				record.setResumeId(resumeId);
				record.setProcessor(username);
				record.setContent("修改面试信息,面试时间："+time+",面试官："+proposerName);		
				record.setTime(ExDateUtils.getCurrentStringDateTime());
				record.setProcessorId(erpUser.getUserId());
				recordMapper.insertRecord(record);
			}else{
				
				//面试预约成功
				resumePostOrder.setMethod(method);
				resumePostOrder.setContactId(contactId);
				resumePostOrder.setPlaceId(placeId);
				resumePostOrder.setInterviewId(interviewId);
				resumePostOrder.setTime(time);
				resumePostOrderMapper.updateResumePostOrder(resumePostOrder);
				
				/*
				 * 系统自动发送面试邀请邮件，发送人为登录人邮箱，接收人为简历中候选人邮箱，抄送岗位申请人邮箱
				 */
				if(!isNext) {
					String frommail = erpUser.getUsername();//发件人（当前登录人）
					if(!frommail.contains("@")) {
						frommail += "@nantian.com.cn";
					}
					
					Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));//岗位编号
					String bcc = this.getBccForSendEmail(proposerId,postId,token);//抄送人是面试官、岗位申请人
					if("error".equals(bcc)) {
						return RestUtils.returnFailure("通过用户Id未获取到用户（邮件抄送人）邮箱！");
					}
					bcc += ","+prodEmailInterviewBcc;//抄送人增加HR
					String subject = "北京南天软件有限公司面试邀请";//主题
					Map<String,Object> employeeMap = employeeMapper.findEmployeeDetail(erpUser.getUserId());//获取员工姓名
					String employeeName = String.valueOf(employeeMap.get("name"));
					String required = null;//岗位要求
					String duty = null;//岗位职责
					if(carryPostInfo) {
						List<Integer> postIds = new ArrayList<>();
						postIds.add(postId);
						List<ErpPost> postList = postMapper.findPostByPostIds(postIds);
						ErpPost post = postList.get(0);
						required = post.getRequired();//岗位要求
						duty = post.getDuty();//岗位职责
					}
					String text = this.getTextForSendEmail(interviewId, method, frommail, employeeName,
							erpUser.getUserPhone(), required, duty);// 邮件内容
					String tomail = getTomailForSendEmail(resumeId);//收件人（简历中的邮箱）
					if("error".equals(tomail)) {
						return RestUtils.returnFailure("通过简历Id未获取到候选人邮箱！");
					}
					boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail,DicConstants.INTERVIEW_INVITATION_EMAIL_TYPE,null);
					if(!sendSuccess) {
						return RestUtils.returnSuccessWithString("邮件发送失败！如果您不想影响面试流程，"
								+ "请点击“下一步”按钮，但这需要您手工发送邮件！");
					}
				}
				
				//新增面试记录信息				
				record.setResumeId(resumeId);
				record.setProcessor(username);
				record.setContent("预约成功！面试时间："+time+",面试官："+proposerName);		
				record.setTime(ExDateUtils.getCurrentStringDateTime());
				record.setProcessorId(erpUser.getUserId());
				recordMapper.insertRecord(record);
						
				/*
				 * 修改面试状态为“面试中”，根据是否是实习生修改面试环节为“初试”或“实习生面试”，当前处理人为岗位申请人
				 */
				resumePost.setId(interviewId);
				resumePost.setStatus(DicConstants.INTERVIEW_STATUS_IN_THE_INTERVIEW);
				resumePost.setPersonId(proposerId);
				resumePostMapper.updateResumePost(resumePost);
			}
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("interviewOrderSuccess方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 预约面试-再联系
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月08日 下午17:15:33
	 */
	@Transactional
	public RestResponse interviewOrderAgain(String token,Map<String,Object> params) {
		logger.info("interviewOrderAgain方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			String remark = String.valueOf(params.get("remark"));//再联系备注
			
			/*
			 * 点击再联系按钮输入预约记录，面试流程不变动
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpResumePostOrderRecord resumePostOrderRecord = new ErpResumePostOrderRecord();
			resumePostOrderRecord.setInterviewId(interviewId);
			resumePostOrderRecord.setProcessor(username);
			resumePostOrderRecord.setRemark(remark);
			resumePostOrderRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			resumePostOrderRecordMapper.insertResumePostOrderRecord(resumePostOrderRecord);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("interviewOrderAgain方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 预约面试-失败
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月09日 上午10:35:18
	 */
	@Transactional
	public RestResponse interviewOrderFailure(String token,Map<String,Object> params) {
		logger.info("interviewOrderFailure方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String content = String.valueOf(params.get("content"));//面试记录内容
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));//岗位编号
			/*
			 * 点击失败按钮输入预约失败原因，保存至面试记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			/*
			 * 面试流程结束，修改面试状态为“面试结束”，将流程状态置为失效
			 */
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setId(interviewId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
			resumePost.setIsValid(false);
			//resumePost.setPersonId(erpUser.getId());
			resumePostMapper.updateResumePost(resumePost);
			
			/*
			 * 修改简历状态为“不通过”，简历归档
			 */
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setIsValid(false);
			resume.setStatus(DicConstants.RESUME_STATUS_NO_PASS);
			resumeMapper.updateResume(resume);			

			//在岗位记录表中插入记录
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("面试预约失败:"+name); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(username);//当前处理人Id
    		operRec.setPostId(postId);
    		operRecordMapper.addPositionOperReord(operRec);
    		
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("interviewOrderFailure方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 待我面试-页面首页显示的简历信息列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月09日 下午15:05:58
	 */
	public RestResponse waitingForMeQueryList(Integer pageType, String token) {
		logger.info("waitingForMeQueryList方法开始执行，参数是：token="+token);
		List<Map<String, Object>> findInfoInPage = new ArrayList<>();
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			Map<String,Object> paramsMap = new HashMap<>();
			if(pageType != null && pageType == 1){
				//我面试过的
				paramsMap.put("processorId", erpUser.getUserId());
				paramsMap.put("processorName", erpUser.getEmployeeName());
			}else{
				//待我面试
				paramsMap.put("isValid", true);
				paramsMap.put("status", DicConstants.INTERVIEW_STATUS_IN_THE_INTERVIEW);
				paramsMap.put("personId", erpUser.getUserId());
			}
			findInfoInPage = resumePostMapper.findResumePostInfoByParams(paramsMap);
			
			List<Integer> roles = erpUser.getRoles();
			if(roles.contains(1) && (pageType == null || pageType == 0)){
				//hr可以看到所有的初试
				paramsMap.put("segment", DicConstants.INTERVIEW_SEGMENT_SOCIAL_FIRST);
				paramsMap.put("personId", null);
				paramsMap.put("decpersonId", erpUser.getUserId());
				List<Map<String, Object>> findInfoInPage2 = resumePostMapper.findResumePostInfoByParams(paramsMap);
				findInfoInPage.addAll(findInfoInPage2);
			}
			return RestUtils.returnSuccess(findInfoInPage);
		} catch (Exception e) {
			logger.info("waitingForMeQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 待我面试-社招生复试职位职级下拉菜单查询
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月09日 下午17:41:39
	 */
	@SuppressWarnings("unchecked")
	public RestResponse waitingForMeQueryPositionRankList(Integer postId) {
		logger.info("waitingForMeQueryPositionRankList方法开始执行，参数是：postId="+postId);
		List<Map<String, Object>> rankListResult=new ArrayList<Map<String,Object>>();

		try {
			/*
			 * 根据职位类别、职位子类、职位族类查询职位名称、职级列表
			 */
//			Map<String,Object> postTemplateInfo = postTemplateMapper.selectPostTemplateByPostId(postId);
			
			/*
			 * 根据职位类别、职位子类、职位族类查询职位名称、职级列表
			 */
//			Map<String,Object> paramsMap = new HashMap<>();
//			paramsMap.put("positionType", postTemplateInfo.get("jobCategory"));
//			paramsMap.put("positionChildType", postTemplateInfo.get("positionChildType"));
//			paramsMap.put("positionFamilyType", postTemplateInfo.get("familyId"));
//			positionRankList = positionRankRelationMapper.selectPositionRankList(paramsMap);
			List<Map<String, Object>> positionRankList = resumePostMapper.findPositionRankList(postId);

			Set<Map<String, Object>> returnSet = new TreeSet<Map<String, Object>>(
					Comparator.comparing(positionRank -> String.valueOf(positionRank.get("positionName")))
			);
			returnSet.addAll(new ArrayList<>(positionRankList));

			for (Map<String , Object> setMap : returnSet) {
				String oldCombineField = String.valueOf(setMap.get("positionName"));
				oldCombineField=oldCombineField.trim();
				List<Map<String , Object>> rankList = new ArrayList<>();
				for (int i = 0; i < positionRankList.size(); i++){
					String newCombineField = String.valueOf(positionRankList.get(i).get("positionName"));
					newCombineField=newCombineField.trim();
					if (oldCombineField.equals(newCombineField)) {
						rankList.add(new HashMap<>(positionRankList.get(i)));
					}
				}
				setMap.put("rank", new ArrayList<>(rankList));
			}
			return RestUtils.returnSuccess(returnSet);



//	        //用于存放最后的结果
//	        List<Map<String, Object>> countList = new ArrayList<Map<String,Object>>();
//	        for (int i = 0; i < positionRankList.size(); i++) {
//	            String oldCombineField = String.valueOf(positionRankList.get(i).get("positionName"));
//	            oldCombineField=oldCombineField.trim();
//
//	            int flag = 0;//0为新增数据，1为增加count
//	            for (int j = 0; j < countList.size(); j++) {
//	                String newCombineField = String.valueOf(countList.get(j).get("positionName"));
//	                newCombineField=newCombineField.trim();
//	                List<Map<String, Object>> rankList=new ArrayList<Map<String,Object>>();
//	                if (oldCombineField.equals(newCombineField)) {
//	                	Integer positionNo=Integer.valueOf(String.valueOf(countList.get(j).get("positionNo")));
//	                	if(positionNo==0){
//	                		List<Map<String,Object>> list=(List<Map<String, Object>>) countList.get(j).get("rank");
//	                		list.add(positionRankList.get(i));
//	                        countList.get(j).put("rank", list);
//	                	}else{
//	                		rankList.add(positionRankList.get(i));
//	                    	rankList.add(countList.get(j));
//	                    	countList.remove(j);
//	                    	Map<String,Object> map= new HashMap<String, Object>();
//	                    	map.put("positionName", newCombineField);
//	                    	map.put("rank", rankList);
//	                    	map.put("positionNo", 0);
//	                    	countList.add(map);
//	                	}
//	                    flag = 1;
//	                    break;
//	                }
//	            }
//	            if (flag == 0) {
//	                countList.add(positionRankList.get(i));
//	            }
//	        }
//			return RestUtils.returnSuccess(countList);
		} catch (Exception e) {
			logger.info("waitingForMeQueryPositionRankList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 待我面试-社招生复试信息查询
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月10日 上午10:10:31
	 */
	public RestResponse waitingForMeQueryReexamInfo(Integer interviewId) {
		logger.info("waitingForMeQueryReexamInfo方法开始执行，参数是：interviewId="+interviewId);
		ErpResumePostReexam decryptedExcelData = null;
		try {
			ErpResumePostReexam resumePostReexam = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
			
			//最终返回的Map
			Map<String, Object> mapReexamReturn = new HashMap<>();
			
			/*
			 * 如果薪酬数据不为空，那么将数据库中加密后的薪酬信息解密
			 */
			if(resumePostReexam!=null) {
				decryptedExcelData = this.decryptSalaryDataAesNew(resumePostReexam);
				
				//复试信息Objet to Map
				Map<String, Object> mapReexam = new HashMap<>();
				mapReexam = this.entityToMap(resumePostReexam);
				if(mapReexam != null) {
					mapReexamReturn.putAll(mapReexam);
				}
				
				//查询面试人的姓名
				Map<String, Object> mapPersonIdToSearch = new HashMap<>();
				mapPersonIdToSearch = resumePostMapper.findResumePostInfoForPlace(interviewId);
				if(mapPersonIdToSearch != null) {
					mapReexamReturn.put("name", mapPersonIdToSearch.get("name"));
				}else {
					mapReexamReturn.put("name", null);
				}
				//查询面试官的用户名、一级部门名称、二级部门名称
			    Map<String, Object> mapPersonIdToSearchs = new HashMap<>();
			    mapPersonIdToSearchs.put("employeeId", decryptedExcelData.getPersonId());
			    
			    Map<String, Object> mapPerson = new HashMap<>();
			    mapPerson = employeeMapper.selectByEmployeeId(mapPersonIdToSearchs);
			    
			    if(mapPerson != null) {
			     mapReexamReturn.put("personName", mapPerson.get("name"));
			     mapReexamReturn.put("firstDepartment", mapPerson.get("firstDepartment"));
			     mapReexamReturn.put("secondDepartment", mapPerson.get("secondDepartment"));
			      
			    }else {
			     mapReexamReturn.put("personName", null);
			     mapReexamReturn.put("firstDepartment", null);
			     mapReexamReturn.put("secondDepartment", null);
			    }
			}
						
			return RestUtils.returnSuccess(mapReexamReturn);
		} catch (Exception e) {
			logger.info("waitingForMeQueryReexamInfo方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	/**
	 * Description: 本类公共函数--对象转化为Map
	 *
	 * @return
	 * @Author songxiugong
	 * @Create Date: 2019年10月16日 上午10:10:31
	 */
	public  Map<String, Object> entityToMap(Object object) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : object.getClass().getDeclaredFields()){
            try {
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                Object o = field.get(object);
                map.put(field.getName(), o);
                field.setAccessible(flag);
            } catch (Exception e) {
            	logger.info("entityToMap方法出现异常："+e.getMessage(),e);
            }
        }
        return map;
	}
        
	/**
	 * Description: 待我面试-社招生初试评价
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月10日 下午14:06:21
	 */
	@Transactional
	public RestResponse waitingForMeSocialFirstJudge(String token,Map<String,Object> params) {
		logger.info("waitingForMeSocialFirstJudge方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String isPass = String.valueOf(params.get("isPass"));//面试是否通过（通过、未通过）
			String content = String.valueOf(params.get("content"));//面试记录内容
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试官
			Boolean appoint = Boolean.valueOf(String.valueOf(params.get("appoint")));//预约复试时间
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));//岗位编号
			
			/*
			 * 点击通过/未通过按钮，输入面试通过/未通过原因，保存至面试记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			

			if("Y".equals(isPass)) {
				/*
				 * 接收前端传递过来的参数
				 */
				if (params.get("personId")!=null){
					Integer personId = Integer.valueOf(String.valueOf(params.get("personId")));//复试面试官
					
					/*
					 * 修改面试环节为“复试”，更新面试流程表的当前处理人
					 */
					ErpResumePost resumePost = new ErpResumePost();
					resumePost.setId(interviewId);
					resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_SOCIAL_REEXAM);
					resumePost.setPersonId(personId);
					if (appoint){
						resumePost.setStatus(DicConstants.INTERVIEW_STATUS_ORDER_INTERVIEW);
					}
					resumePostMapper.updateResumePost(resumePost);
				}				
			}else {
				/*
				 * 面试流程结束，修改面试状态为“面试结束”
				 */
				ErpResumePost resumePost = new ErpResumePost();
				resumePost.setId(interviewId);
				resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
				//resumePost.setIsValid(false);
				//resumePost.setPersonId(erpUser.getId());
				resumePostMapper.updateResumePost(resumePost);
				
				/*
				 * 修改简历的状态为“不通过”
				 */
				ErpResume resume = new ErpResume();
				resume.setResumeId(resumeId);
				resume.setStatus(DicConstants.RESUME_STATUS_NO_PASS);
				resume.setIsValid(false);//面试不通过简历归档
				resumeMapper.updateResume(resume);
				
				//在岗位记录表中插入记录
	    		PositionOperRecond operRec = new PositionOperRecond();
	    		Date date = new Date();
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
	    		operRec.setCreateTime(format.format(date));
	    		operRec.setOperContext("面试不通过:"+name); //处理内容
	    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
	    		operRec.setCurrentPersonName(username);//当前处理人Id
	    		operRec.setPostId(postId);
	    		operRecordMapper.addPositionOperReord(operRec);
			}			
    		
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("waitingForMeSocialFirstJudge方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 待我面试-社招生复试评价
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月10日 下午15:08:28
	 */
	@Transactional
	public RestResponse waitingForMeSocialReexamJudge(String token,Map<String,Object> params) {
		logger.info("waitingForMeSocialReexamJudge方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String isPass = String.valueOf(params.get("isPass"));//面试是否通过（通过、未通过、待定）
			String content = String.valueOf(params.get("content"));//面试记录内容
			String score = String.valueOf(params.get("score"));//素质模型打分
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));
			
			String contents = String.valueOf(params.get("contents"));//面试记录内容--通过或者不通过理由	SXG 2019-09-19
			contents = "";	//赋空值，提交之后不需要保存  	SXG 2019-09-20
			/*
			 * 点击通过/未通过按钮，输入面试通过/未通过原因，保存至面试记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			String str="";
			
			if("Y".equals(isPass)) {
				/*
				 * 接收前端传递过来的参数
				 */
				String entryTime = String.valueOf(params.get("entryTime"));//入职时间
				Integer position = Integer.valueOf(String.valueOf(params.get("position")));//职位
				Integer rank = Integer.valueOf(String.valueOf(params.get("rank")));//职级
				String monthIncome = String.valueOf(params.get("monthIncome"));//月度收入
				String socialSecurityBase = String.valueOf(params.get("socialSecurityBase"));//社保基数
				String accumulationFundBase = String.valueOf(params.get("accumulationFundBase"));//公积金基数
				String socialSecurityPlace = String.valueOf(params.get("socialSecurityPlace"));//社保地
				Integer probationPeriod = Integer.valueOf(String.valueOf(params.get("probationPeriod")));//试用期期限
				Integer contractPeriod = Integer.valueOf(String.valueOf(params.get("contractPeriod")));//合同期限
				String remark = String.valueOf(params.get("remark"));//备注
				
				/*
				 * 将薪酬数据加密
				 */
				Map<String,String> salaryData = new HashMap<>();
				salaryData.put("monthIncome", monthIncome);
				salaryData.put("socialSecurityBase", socialSecurityBase);
				salaryData.put("accumulationFundBase", accumulationFundBase);
				Map<String,String> encryptedSalaryData = this.encryptSalaryDataAes(salaryData);

				/*
				 * 接收前端传递过来的参数
				 */
				Boolean isNext = Boolean.valueOf(String.valueOf(params.get("isNext")));//是否有下一轮面试
				Boolean offerApprove = false;
				if (!isNext){
					//不需要下一轮复试的情况，判断复试官是否为一级部门经理以上级别，如果不是，则需要一级部门经理再进行复试
					List<Integer> roles = erpUser.getRoles();
					Map<String,Object> post = postMapper.findByPostId(postId);
					if(!roles.contains(8) && !roles.contains(9) && !roles.contains(2)){
						//查询岗位对应部门的一级部门经理的员工ID
						params.put("personId", post.get("userId"));
						params.put("appoint", false);
						isNext = true;
						logger.info("待一级部门经理复试，参数是：{}",params);
					}else if(roles.contains(2)){
						if ((rank>4 )|| (Integer.valueOf(monthIncome) >= 18000)){
							//查询岗位对应部门的上级领导的员工ID
							params.put("personId", post.get("superLeader"));
							params.put("appoint", false);
							
							offerApprove = true;						
							isNext = true;
							logger.info("待上级领导offer审批，参数是：{}",params);
						}
					}
				}
				if(isNext) {//有下一轮面试
					/*
					 * 将素质模型打分、员工基本信息、薪资信息，组装成PO对象
					 */
					ErpResumePostReexam resumePostReexam = new ErpResumePostReexam();
					resumePostReexam.setInterviewId(interviewId);
					resumePostReexam.setScore(score);
					resumePostReexam.setEntryTime(entryTime);
					resumePostReexam.setPosition(position);
					resumePostReexam.setRank(rank);
					resumePostReexam.setMonthIncome(encryptedSalaryData.get("monthIncome"));
					resumePostReexam.setSocialSecurityBase(encryptedSalaryData.get("socialSecurityBase"));
					resumePostReexam.setAccumulationFundBase(encryptedSalaryData.get("accumulationFundBase"));
					resumePostReexam.setSocialSecurityPlace(socialSecurityPlace);
					resumePostReexam.setProbationPeriod(probationPeriod);
					resumePostReexam.setContractPeriod(contractPeriod);
					resumePostReexam.setRemark(remark);
					
					resumePostReexam.setContents(contents);	//SXG 2019-09-19
					resumePostReexam.setPass("Y");
					
					//SXG 2019-10-16
					resumePostReexam.setIsNext(false);
					resumePostReexam.setPersonId(null);
					resumePostReexam.setAppointment(false);
					
					/*
					 * 根据复试信息表中是否已存在记录，决定是复试信息表数据的新增或更新
					 */
					ErpResumePostReexam isExist = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
					if(isExist==null) {
						resumePostReexamMapper.insertResumePostReexam(resumePostReexam);
					}else {
						resumePostReexamMapper.updateResumePostReexam(resumePostReexam);
					}
					
					/*
					 * 接收前端传递过来的参数
					 */
					Integer personId = Integer.valueOf(String.valueOf(params.get("personId")));//复试面试官
					Boolean appoint = Boolean.valueOf(String.valueOf(params.get("appoint")));//预约复试时间
					
					/*
					 * 更新面试流程表的当前处理人
					 */
					ErpResumePost resumePost = new ErpResumePost();
					resumePost.setId(interviewId);
					resumePost.setPersonId(personId);
					if (appoint){
						resumePost.setStatus(DicConstants.INTERVIEW_STATUS_ORDER_INTERVIEW);
					}
					//如果当前登录人（面试官）的员工ID和下一位面试官的ID不相等，流程走到offer待审批
					//如果相等，跳过offer审批，直接到offer待处理环节
					if(personId.equals(erpUser.getUserId())) {
						offerService.offerApprove(token, params);
					}else {
						if(offerApprove){
							resumePost.setIsValid(false);
							resumePost.setStatus(DicConstants.INTERVIEW_STATUS_OFFER_APPROVE);
						}
						resumePostMapper.updateResumePost(resumePost);
					}

				}else {//没有下一轮面试
					/*
					 * 社招生复试通过，新增一条offer信息
					 */
					ErpOffer offer = new ErpOffer();
					offer.setInterviewId(interviewId);
					offer.setEntryTime(entryTime);
					offer.setPosition(position);
					offer.setRank(rank);
					offer.setSocialSecurityPlace(socialSecurityPlace);
					offer.setProbationPeriod(probationPeriod);
					offer.setContractPeriod(contractPeriod);
					offer.setStatus(DicConstants.OFFER_STATUS_WAITING);
					offerMapper.insertOffer(offer);
					
					/*
					 * 社招生复试通过，将社招生的面试谈薪情况跨工程插入到薪酬数据库中
					 */
					Map<String,Object> salaryMap = new HashMap<>();
					salaryMap.put("monthIncome", encryptedSalaryData.get("monthIncome"));
					salaryMap.put("socialSecurityBase", encryptedSalaryData.get("socialSecurityBase"));
					salaryMap.put("accumulationFundBase", encryptedSalaryData.get("accumulationFundBase"));
					salaryMap.put("offerId", offer.getOfferId());
					salaryMap.put("remark", remark);
					salaryMap.put("token", token);
					String resultStr = insertErpTalkSalary(salaryMap);
					if(!"OK".equals(resultStr)) {
						return RestUtils.returnSuccessWithString(resultStr);
					}
					
					/*
					 * 面试流程结束，修改面试状态为“面试通过”，将流程状态置为失效
					 */
					ErpResumePost resumePost = new ErpResumePost();
					resumePost.setId(interviewId);
					resumePost.setStatus(DicConstants.INTERVIEW_STATUS_PASS);
					resumePost.setIsValid(false);
					//resumePost.setPersonId(erpUser.getId());
					resumePostMapper.updateResumePost(resumePost);
					
					/*
					 * 修改简历状态为已发offer
					 */
					ErpResume resume = new ErpResume();
					resume.setResumeId(resumeId);
					resume.setStatus(DicConstants.RESUME_STATUS_PASS);
					resumeMapper.updateResume(resume);
					
					str = "面试通过：";
				}
			}else if("N".equals(isPass)){
				/*
				 * 将素质模型打分，新增到复试信息表中
				 */
				ErpResumePostReexam resumePostReexam = new ErpResumePostReexam();
				resumePostReexam.setInterviewId(interviewId);
				resumePostReexam.setScore(score);
				resumePostReexam.setContents(contents);		//2019-09-19
				resumePostReexam.setPass("N");
				
				//SXG 2019-10-16
				resumePostReexam.setIsNext(false);
				resumePostReexam.setPersonId(null);
				resumePostReexam.setAppointment(false);
				
				/*
				 * 根据复试信息表中是否已存在记录，决定是复试信息表数据的新增或更新
				 */
				ErpResumePostReexam isExist = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
				if(isExist==null) {
					resumePostReexamMapper.insertResumePostReexam(resumePostReexam);
				}else {
					resumePostReexamMapper.updateResumePostReexam(resumePostReexam);
				}
				
				
				
				/*
				 * 面试流程结束，修改面试状态为“面试结束”，将流程状态置为失效
				 */
				ErpResumePost resumePost = new ErpResumePost();
				resumePost.setId(interviewId);
				resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
				//resumePost.setIsValid(false);
				//resumePost.setPersonId(erpUser.getId());
				resumePostMapper.updateResumePost(resumePost);
				
				/*
				 * 修改简历的状态为“不通过”
				 */
				ErpResume resume = new ErpResume();
				resume.setResumeId(resumeId);
				resume.setStatus(DicConstants.RESUME_STATUS_NO_PASS);
				resume.setIsValid(false);////复试不通过简历归档
				resumeMapper.updateResume(resume);
				
				str = "面试不通过：";
			}else {
				/*
				 * 将素质模型打分，新增到复试信息表中
				 */
				ErpResumePostReexam resumePostReexam = new ErpResumePostReexam();
				resumePostReexam.setInterviewId(interviewId);
				resumePostReexam.setScore(score);
				resumePostReexam.setContents(contents);		//2019-09-19
				resumePostReexam.setPass("D");
				
				//SXG 2019-10-16
				resumePostReexam.setIsNext(false);
				resumePostReexam.setPersonId(null);
				resumePostReexam.setAppointment(false);
				
				/*
				 * 根据复试信息表中是否已存在记录，决定是复试信息表数据的新增或更新
				 */
				ErpResumePostReexam isExist = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
				if(isExist==null) {
					resumePostReexamMapper.insertResumePostReexam(resumePostReexam);
				}else {
					resumePostReexamMapper.updateResumePostReexam(resumePostReexam);
				}
				
				str = "面试待定：";
			}
			
			if (!str.equals("")){
				//在岗位记录表中插入记录
	    		PositionOperRecond operRec = new PositionOperRecond();
	    		Date date = new Date();
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
	    		operRec.setCreateTime(format.format(date));
	    		operRec.setOperContext(str+name); //处理内容
	    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
	    		operRec.setCurrentPersonName(username);//当前处理人Id
	    		operRec.setPostId(postId);
	    		operRecordMapper.addPositionOperReord(operRec);
			}
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("waitingForMeSocialReexamJudge方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 待我面试-社招生复试评价-保存  如果薪酬为空，则不加密，直接保存空字符串
	 *
	 * @return
	 * @Author songxiugong
	 * @Create Date: 2019年09月16日 下午15:00:00
	 */
	@Transactional
	public RestResponse waitingForMeSocialReexamJudgeSave(String token,Map<String,Object> params) {
		logger.info("waitingForMeSocialReexamJudgeSave方法开始执行，参数是：token="+token+","+params);
		ErpResumePostReexam erpResumePostReexam = new ErpResumePostReexam();
		try {
			
			Integer interviewId=-1;
			if(params.get("interviewId")!=null) {
				interviewId= Integer.valueOf((String.valueOf(params.get("interviewId"))));
				erpResumePostReexam.setInterviewId(interviewId);
			}

			erpResumePostReexam.setScore(String.valueOf(params.get("score")));
			erpResumePostReexam.setEntryTime(String.valueOf(params.get("entryTime")));
			erpResumePostReexam.setPass(String.valueOf(params.get("isPass")));
			
			if (params.get("position")!=null && String.valueOf(params.get("position")) != ""){
				erpResumePostReexam.setPosition(Integer.valueOf(String.valueOf(params.get("position"))));
			}

			if (params.get("rank")!=null && String.valueOf(params.get("rank")) != ""){
				erpResumePostReexam.setRank(Integer.valueOf(String.valueOf(params.get("rank"))));
			}
			
			if (params.get("socialSecurityPlace")!=null && String.valueOf(params.get("socialSecurityPlace")) != ""){
				erpResumePostReexam.setSocialSecurityPlace(String.valueOf(params.get("socialSecurityPlace")));
			}else {
				erpResumePostReexam.setSocialSecurityPlace("");
			}
			
			
			if (params.get("probationPeriod")!=null && String.valueOf(params.get("probationPeriod")) != ""){
				erpResumePostReexam.setProbationPeriod(Integer.valueOf(String.valueOf(params.get("probationPeriod"))));
			}
			
			if (params.get("contractPeriod")!=null && String.valueOf(params.get("contractPeriod")) != ""){
				erpResumePostReexam.setContractPeriod(Integer.valueOf(String.valueOf(params.get("contractPeriod"))));
			}
			
			if (params.get("remark")!=null && String.valueOf(params.get("remark")) != ""){
				erpResumePostReexam.setRemark(String.valueOf(params.get("remark")));
			}else {
				erpResumePostReexam.setRemark("");
			}
			
			//2019-09-19
			if (params.get("contents")!=null && String.valueOf(params.get("contents")) != ""){
				erpResumePostReexam.setContents(String.valueOf(params.get("contents")));
			}else {
				erpResumePostReexam.setContents("");
			}
			
			//2019-10-16
			if (params.get("isNext")!=null && String.valueOf(params.get("isNext")) != ""){
				
				erpResumePostReexam.setIsNext(Boolean.valueOf(params.get("isNext").toString()));
				
				if (Boolean.valueOf(params.get("isNext").toString()) == true){
					if (params.get("personId")!=null && String.valueOf(params.get("personId")) != ""){
						erpResumePostReexam.setPersonId(Integer.valueOf(String.valueOf(params.get("personId"))));
					}else {
						erpResumePostReexam.setPersonId(null);
					}
					
					if (params.get("appointment")!=null && String.valueOf(params.get("appointment")) != ""){
						erpResumePostReexam.setAppointment(Boolean.valueOf(params.get("appointment").toString()));
					}else {
						erpResumePostReexam.setAppointment(false);
					}
				}else {
					erpResumePostReexam.setIsNext(false);
					erpResumePostReexam.setPersonId(null);
					erpResumePostReexam.setAppointment(false);
				}
			}else {
				erpResumePostReexam.setIsNext(false);
				erpResumePostReexam.setPersonId(null);
				erpResumePostReexam.setAppointment(false);
			}
			

			/*
			 * 将薪酬数据加密
			 */
			Map<String,String> salaryData = new HashMap<>();
			
			if (params.get("monthIncome")!=null && String.valueOf(params.get("monthIncome")) != ""){
				salaryData.put("monthIncome", String.valueOf(params.get("monthIncome")));
//				erpResumePostReexam.setMonthIncome(String.valueOf(params.get("monthIncome")));
			}else {
				erpResumePostReexam.setMonthIncome("");
			}
			
			if (params.get("socialSecurityBase")!=null && String.valueOf(params.get("socialSecurityBase")) != ""){
				salaryData.put("socialSecurityBase", String.valueOf(params.get("socialSecurityBase")));
//				erpResumePostReexam.setSocialSecurityBase(String.valueOf(params.get("socialSecurityBase")));
			}else {
				erpResumePostReexam.setSocialSecurityBase("");
			}
			
			if (params.get("accumulationFundBase")!=null && String.valueOf(params.get("accumulationFundBase")) != ""){
				salaryData.put("accumulationFundBase", String.valueOf(params.get("accumulationFundBase")));
//				erpResumePostReexam.setAccumulationFundBase(String.valueOf(params.get("accumulationFundBase")));
			}else {
				erpResumePostReexam.setAccumulationFundBase("");
			}
			
			salaryData.put("monthIncome", String.valueOf(params.get("monthIncome")));
			salaryData.put("socialSecurityBase", String.valueOf(params.get("socialSecurityBase")));
			salaryData.put("accumulationFundBase", String.valueOf(params.get("accumulationFundBase")));
			Map<String,String> encryptedSalaryData = this.encryptSalaryDataAesNew(salaryData);	
			
			erpResumePostReexam.setMonthIncome(encryptedSalaryData.get("monthIncome"));
			erpResumePostReexam.setSocialSecurityBase(encryptedSalaryData.get("socialSecurityBase"));
			erpResumePostReexam.setAccumulationFundBase(encryptedSalaryData.get("accumulationFundBase"));
			
			/*
			 * 根据复试信息表中是否已存在记录，决定是复试信息表数据的新增或更新
			 */
			ErpResumePostReexam isExist = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
			if(isExist==null) {
				resumePostReexamMapper.insertResumePostReexam(erpResumePostReexam);
			}else {
				resumePostReexamMapper.updateResumePostReexam(erpResumePostReexam);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("waitingForMeSocialReexamJudgeSave方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
		return RestUtils.returnSuccess("OK");
	}

	/**
	 * Description: 待我面试-实习生面试评价
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月10日 下午16:45:13
	 */
	@Transactional
	public RestResponse waitingForMeTraineeJudge(String token,Map<String,Object> params) {
		logger.info("waitingForMeTraineeJudge方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String isPass = String.valueOf(params.get("isPass"));//面试是否通过（通过、未通过、待定）
			String content = String.valueOf(params.get("content"));//面试记录内容
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));
			
			/*
			 * 点击通过/未通过按钮，输入面试通过/未通过原因，保存至面试记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			if("Y".equals(isPass)) {
				Boolean isNext = Boolean.valueOf(String.valueOf(params.get("isNext")));//是否有下一轮面试
				if(isNext) {
					/*
					 * 接收前端传递过来的参数
					 */
					Integer personId = Integer.valueOf(String.valueOf(params.get("personId")));//复试面试官
					
					/*
					 * 更新面试流程表的当前处理人
					 */
					ErpResumePost resumePost = new ErpResumePost();
					resumePost.setId(interviewId);
					resumePost.setPersonId(personId);
					resumePostMapper.updateResumePost(resumePost);
				}else {
					/*
					 * 实习生面试通过，新增一条offer信息
					 */
					ErpOffer offer = new ErpOffer();
					offer.setInterviewId(interviewId);
					offer.setStatus(DicConstants.OFFER_STATUS_WAITING);
					offerMapper.insertOffer(offer);
					
					/*
					 * 面试流程结束，修改面试状态为“面试通过”，将流程状态置为失效
					 */
					ErpResumePost resumePost = new ErpResumePost();
					resumePost.setId(interviewId);
					resumePost.setStatus(DicConstants.INTERVIEW_STATUS_PASS);
					resumePost.setIsValid(false);
					//resumePost.setPersonId(erpUser.getId());
					resumePostMapper.updateResumePost(resumePost);
					
					/*
					 * 修改简历状态为已发offer
					 */
					ErpResume resume = new ErpResume();
					resume.setResumeId(resumeId);
					resume.setStatus(DicConstants.RESUME_STATUS_PASS);
					resumeMapper.updateResume(resume);
					
					//在岗位记录表中插入记录
		    		PositionOperRecond operRec = new PositionOperRecond();
		    		Date date = new Date();
		    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
		    		operRec.setCreateTime(format.format(date));
		    		operRec.setOperContext("面试通过："+name); //处理内容
		    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
		    		operRec.setCurrentPersonName(username);//当前处理人Id
		    		operRec.setPostId(postId);
		    		operRecordMapper.addPositionOperReord(operRec);
				}
			}else if("N".equals(isPass)) {
				/*
				 * 面试流程结束，修改面试状态为“面试结束”，将流程状态置为失效
				 */
				ErpResumePost resumePost = new ErpResumePost();
				resumePost.setId(interviewId);
				resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
				//resumePost.setIsValid(false);
				//resumePost.setPersonId(erpUser.getId());
				resumePostMapper.updateResumePost(resumePost);
				
				/*
				 * 修改简历的状态为“不通过”
				 */
				ErpResume resume = new ErpResume();
				resume.setResumeId(resumeId);
				resume.setStatus(DicConstants.RESUME_STATUS_NO_PASS);
				resume.setIsValid(false);
				resumeMapper.updateResume(resume);
				
				//在岗位记录表中插入记录
	    		PositionOperRecond operRec = new PositionOperRecond();
	    		Date date = new Date();
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
	    		operRec.setCreateTime(format.format(date));
	    		operRec.setOperContext("面试不通过："+name); //处理内容
	    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
	    		operRec.setCurrentPersonName(username);//当前处理人Id
	    		operRec.setPostId(postId);
	    		operRecordMapper.addPositionOperReord(operRec);
			}else {
				//在岗位记录表中插入记录
	    		PositionOperRecond operRec = new PositionOperRecond();
	    		Date date = new Date();
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
	    		operRec.setCreateTime(format.format(date));
	    		operRec.setOperContext("面试待定："+name); //处理内容
	    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
	    		operRec.setCurrentPersonName(username);//当前处理人Id
	    		operRec.setPostId(postId);
	    		operRecordMapper.addPositionOperReord(operRec);
			}
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("waitingForMeTraineeJudge方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 面试不通过-查询列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月11日 下午14:37:20
	 */
	public RestResponse interviewNoPassQueryList() {
		List<Map<String, Object>> findInfoPage = new ArrayList<>();
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isValid", true);
			paramsMap.put("status", DicConstants.INTERVIEW_STATUS_NO_PASS);
			findInfoPage = resumePostMapper.findResumePostInfoByParams(paramsMap);
			return RestUtils.returnSuccess(findInfoPage);
		} catch (Exception e) {
			logger.info("interviewNoPassQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 面试不通过-可推荐
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月17日 下午16:19:05
	 */
	@Transactional
	public RestResponse interviewNoPassValid(String token,Map<String,Object> params) {
		logger.info("interviewNoPassValid方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String content = String.valueOf(params.get("content"));//面试记录内容
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			
			/*
			 * 点击可推荐按钮，输入可推荐理由，理由记录到该简历的面试记录中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("简历可推荐，可推荐原因："+content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			/*
			 * 流程状态置为失效
			 */
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setId(interviewId);
			//resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
			resumePost.setIsValid(false);
			//resumePost.setPersonId(erpUser.getId());
			resumePostMapper.updateResumePost(resumePost);
			
			/*
			 * 修改简历状态为“可推荐”
			 */
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setIsValid(true);
			resume.setStatus(DicConstants.RESUME_STATUS_RECOMMENDED);
			resumeMapper.updateResume(resume);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("interviewNoPassValid方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 面试不通过-归档
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月17日 上午17:35:21
	 */
	@Transactional
	public RestResponse interviewNoPassInvalid(String token,Map<String,Object> params) {
		logger.info("interviewNoPassInvalid方法开始执行，参数是：token="+token+",params="+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			String content = String.valueOf(params.get("content"));//面试记录内容
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			
			/*
			 * 点击归档按钮，输入归档理由，理由记录到该简历的面试记录中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(content);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			/*
			 * 流程状态置为失效
			 */
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setId(interviewId);
			//resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
			resumePost.setIsValid(false);
			//resumePost.setPersonId(erpUser.getId());
			resumePostMapper.updateResumePost(resumePost);
			
			/*
			 * 修改简历状态为“归档”
			 */
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setIsValid(false);
			//resume.setStatus(DicConstants.RESUME_STATUS_RECOMMENDED);
			resumeMapper.updateResume(resume);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("interviewNoPassInvalid方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 所有进行中面试-查询列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月11日 下午16:48:57
	 */
	public RestResponse allInterviewQueryList(String token) {
		logger.info("allInterviewQueryList方法开始执行，无参数");
		List<Map<String, Object>> findInfoPage = new ArrayList<>();
		try {
			Map<String,Object> paramsMap = new HashMap<>();
//			paramsMap.put("isValid", true);
//			paramsMap.put("status", DicConstants.INTERVIEW_STATUS_IN_THE_INTERVIEW);

			//根据角色过滤数据
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();// 角色列表

			if (roles.contains(8) || roles.contains(1)|| roles.contains(10)) {// 总经理、hr
				// 查询所有的员工
			} else if (roles.contains(9)) { // 副总经理
				paramsMap.put("superLeaderId", id);
			} else if (roles.contains(2)) {// 一级部门经理角色
				paramsMap.put("leaderId", id);
			} else {
				return RestUtils.returnSuccessWithString("无可查看的面试");
			}
			findInfoPage = resumePostMapper.findResumePostInfoByParams(paramsMap);
			return RestUtils.returnSuccess(findInfoPage);
		} catch (Exception e) {
			logger.info("allInterviewQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致查询失败！");
		}
	}
	
	/**
	 * Description: 将社招生复试的薪酬数据，跨工程插入到薪酬数据库中
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月14日 下午13:52:53
	 */
	@SuppressWarnings({ "rawtypes" })
	public String insertErpTalkSalary(Map<String,Object> salaryMap) {
		logger.info("insertErpTalkSalary方法开始执行，参数是："+salaryMap);
		try {
			//调用薪酬工程，将月度收入、社保基数、公积金基数存入薪酬的数据库表中
			String url = protocolType+"nantian-erp-salary/nantian-erp/salary/talkSalary/insertErpTalkSalary";
			Map<String,Object> erpTalkSalary = new HashMap<>();
			erpTalkSalary.put("monthIncome", salaryMap.get("monthIncome"));
			erpTalkSalary.put("socialSecurityBase", salaryMap.get("socialSecurityBase"));
			erpTalkSalary.put("accumulationFundBase", salaryMap.get("accumulationFundBase"));
			erpTalkSalary.put("offerId", salaryMap.get("offerId"));
			erpTalkSalary.put("remark", salaryMap.get("remark"));
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",salaryMap.get("token").toString());//将token放到请求头中
			HttpEntity<Map<String,Object>> request = new HttpEntity<>(erpTalkSalary, requestHeaders);
			
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
			if(response.getStatusCodeValue() != 200){
				return "薪酬工程响应失败！导致薪酬数据插入失败！";
			}
			return "OK";
		} catch (Exception e) {
			logger.info("insertErpTalkSalary方法出现异常："+e.getMessage(),e);
			return "方法出现异常！导致薪酬数据插入失败！";
		}
	}
	
	/* **************************************** 封装的工具方法 **************************************** */
	
	/**
	 * Description: 薪酬数据加密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月12日 上午10:03:29
	 */
	public Map<String,String> encryptSalaryDataAes(Map<String,String> salaryData){
		//logger.info("进入加密方法，参数是："+salaryData);
		/*
		 * 获取到所有需要加密的薪酬数据
		 */
		String monthIncome = salaryData.get("monthIncome");//月度收入
		String socialSecurityBase = salaryData.get("socialSecurityBase");//社保基数
		String accumulationFundBase = salaryData.get("accumulationFundBase");//公积金基数
		
		/*
		 * 将薪酬加密后，赋值给Map
		 * 如果薪酬为空，那么赋值为0.0
		 */
		Map<String,String> encryptedSalaryData = new HashMap<>();
		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.0));
		if(monthIncome==null) {
			encryptedSalaryData.put("monthIncome", defaultSalaryValue);
		}else {
			encryptedSalaryData.put("monthIncome", AesUtils.encrypt(monthIncome));
		}
		if(socialSecurityBase==null) {
			encryptedSalaryData.put("socialSecurityBase", defaultSalaryValue);
		}else {
			encryptedSalaryData.put("socialSecurityBase", AesUtils.encrypt(socialSecurityBase));
		}
		if(accumulationFundBase==null) {
			encryptedSalaryData.put("accumulationFundBase", defaultSalaryValue);
		}else {
			encryptedSalaryData.put("accumulationFundBase", AesUtils.encrypt(accumulationFundBase));
		}
		return encryptedSalaryData;
	}
	
	/**
	 * Description: 薪酬数据解密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月12日 上午10:27:47
	 */
	public ErpResumePostReexam decryptSalaryDataAes(ErpResumePostReexam resumePostReexam){
		//logger.info("进入解密方法，参数是："+resumePostReexam);
		/*
		 * 获取到所有需要解密的薪酬数据
		 */
		String monthIncome = resumePostReexam.getMonthIncome();//基本工资
		String socialSecurityBase = resumePostReexam.getSocialSecurityBase();//社保基数
		String accumulationFundBase = resumePostReexam.getAccumulationFundBase();//公积金基数
		
		/*
		 * 将薪酬解密，将字符串类型转换为浮点型，赋值给Map
		 */
		resumePostReexam.setMonthIncome(AesUtils.decrypt(monthIncome));
		resumePostReexam.setSocialSecurityBase(AesUtils.decrypt(socialSecurityBase));
		resumePostReexam.setAccumulationFundBase(AesUtils.decrypt(accumulationFundBase));
		return resumePostReexam;
	}
	
	/**
	 * Description: 薪酬数据加密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月12日 上午10:03:29
	 */
	public Map<String,String> encryptSalaryDataAesNew(Map<String,String> salaryData){
		//logger.info("进入加密方法，参数是："+salaryData);
		/*
		 * 获取到所有需要加密的薪酬数据
		 */
		String monthIncome = salaryData.get("monthIncome");//月度收入
		String socialSecurityBase = salaryData.get("socialSecurityBase");//社保基数
		String accumulationFundBase = salaryData.get("accumulationFundBase");//公积金基数
		
		/*
		 * 将薪酬加密后，赋值给Map
		 * 如果薪酬为空，那么赋值为 ""
		 */
		Map<String,String> encryptedSalaryData = new HashMap<>();
//		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.0));
		if(monthIncome != null && String.valueOf(monthIncome) != "") {
			encryptedSalaryData.put("monthIncome", AesUtils.encrypt(monthIncome));
		}else {
			encryptedSalaryData.put("monthIncome", "");
		}
		if(socialSecurityBase != null && String.valueOf(socialSecurityBase) != "") {
			encryptedSalaryData.put("socialSecurityBase", AesUtils.encrypt(socialSecurityBase));
		}else {
			encryptedSalaryData.put("socialSecurityBase", "");
		}
		if(accumulationFundBase != null && String.valueOf(accumulationFundBase) != "") {
			encryptedSalaryData.put("accumulationFundBase", AesUtils.encrypt(accumulationFundBase));
		}else {
			encryptedSalaryData.put("accumulationFundBase", "");
		}
		return encryptedSalaryData;
	}
	
	/**
	 * Description: 薪酬数据解密
	 *
	 * @return
	 * @Author songxiugong
	 * @Create Date: 2019年09月23日 上午14:27:47
	 */
	public ErpResumePostReexam decryptSalaryDataAesNew(ErpResumePostReexam resumePostReexam){
		//logger.info("进入解密方法，参数是："+resumePostReexam);
		/*
		 * 获取到所有需要解密的薪酬数据
		 */
		String monthIncome = resumePostReexam.getMonthIncome();//基本工资
		String socialSecurityBase = resumePostReexam.getSocialSecurityBase();//社保基数
		String accumulationFundBase = resumePostReexam.getAccumulationFundBase();//公积金基数
		
		/*
		 * 将薪酬解密，将字符串类型转换为浮点型，赋值给Map
		 */
		if(monthIncome == "") {
			resumePostReexam.setMonthIncome(monthIncome);
		}else {
			resumePostReexam.setMonthIncome(AesUtils.decrypt(monthIncome));
		}
		
		if(socialSecurityBase == "") {
			resumePostReexam.setSocialSecurityBase(socialSecurityBase);
		}else {
			resumePostReexam.setSocialSecurityBase(AesUtils.decrypt(socialSecurityBase));
		}

		if(accumulationFundBase =="") {
			resumePostReexam.setAccumulationFundBase("");
		}else {
			resumePostReexam.setAccumulationFundBase(AesUtils.decrypt(accumulationFundBase));
		}
		
		return resumePostReexam;
	}
	
	
	/**
	 * Description: 邮件抄送人
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月12日 下午18:37:56
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getBccForSendEmail(Integer proposerIdForInterview,Integer postId,String token) {
		//调用ERP-权限 工程 的操作层服务接口-查询用户的邮箱
		String url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpId?empId="+proposerIdForInterview;
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token", token);
	    HttpEntity<String> requestEntity=new HttpEntity<String>(null,requestHeaders);
	    ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Map.class);
	    logger.info("跨工程调用的响应结果response="+response);
	    Map<String,Object> resultMap = response.getBody();
	    /*
	     * 跨工程调用未获取到响应
	     */
	    if(response.getStatusCodeValue() != 200 || resultMap==null || resultMap.get("data")==null){
	    	return "error";
	    }
	    /*
	     * 通过用户ID未获取到用户信息
	     */
	    Map<String,Object> userMap = (Map<String, Object>) resultMap.get("data");
	    if(userMap==null || userMap.get("username")==null){
	    	return "error";
	    }
	    String proposerEmailForInterview = String.valueOf(userMap.get("username"));//获取面试官邮箱
	    
	    Map<String, Object> postMap = postMapper.findByPostId(postId);
		Integer proposerIdForPost = Integer.valueOf(String.valueOf(postMap.get("proposerId")));//岗位申请人员工ID
	    if(proposerIdForPost == proposerIdForInterview) {
	    	logger.info("抄送人是面试官："+proposerEmailForInterview);
	    	return proposerEmailForInterview;
	    }else {
	    	//调用ERP-权限 工程 的操作层服务接口-查询用户的邮箱
			String url1 = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpId?empId="+proposerIdForPost;
			HttpHeaders requestHeaders1=new HttpHeaders();
			requestHeaders1.add("token", token);
			HttpEntity<String> requestEntity1=new HttpEntity<String>(null,requestHeaders1);
			ResponseEntity<Map> response1 = restTemplate.exchange(url1,HttpMethod.GET,requestEntity1,Map.class);
			logger.info("查询一级部门经理时，跨工程调用的响应结果response1="+response1);
			Map<String,Object> resultMap1 = response1.getBody();
			/*
			 * 跨工程调用未获取到响应
			 */
			if(response1.getStatusCodeValue() != 200 || "".equals(String.valueOf(resultMap1.get("data")))){
				logger.error("未找到岗位申请人的邮箱地址，员工Id为："+proposerIdForPost);
				return proposerEmailForInterview;
			}
			/*
			 * 通过用户ID未获取到用户信息
			 */
			Map<String,Object> userMap1 = (Map<String, Object>) resultMap1.get("data");
			if(userMap1==null || userMap1.get("username")==null){
				return "error";
			}
			String proposerEmailForPost = String.valueOf(userMap1.get("username"));//获取用户（岗位申请人）邮箱
			logger.info("抄送人是面试官+岗位申请人："+proposerEmailForInterview+","+proposerEmailForPost);
			return proposerEmailForInterview+","+proposerEmailForPost;
	    }
	}
	
	/**
	 * Description: 邮件正文
	 * required、duty可以为空
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月12日 下午17:47:51
	 */
	public String getTextForSendEmail(Integer interviewId, String method, String frommail, String employeeName,
			String userPhone, String required, String duty) {
		/*
		 * 通过面试方式不同查询不同的结果（现场面试、电话面试）
		 */
		String interviewPlace = "";//面试地点
		String interviewContact = "";//面试联系人
		String interviewContactName = "";//面试联系人名字
		String interviewContactPhone = "";//面试联系人电话
		
		Map<String,Object> interviewInfo = new HashMap<>();
		if(DicConstants.INTERVIEW_ORDER_METHOD_PLACE.equals(method)) {
			interviewInfo = resumePostMapper.findResumePostInfoForPlace(interviewId);
			interviewPlace = String.valueOf(interviewInfo.get("placeName"));
			interviewContact = String.valueOf(interviewInfo.get("contactName"));
			String[] interviewContactArr = interviewContact.split(",");
			interviewContactName = interviewContactArr[0];
			interviewContactPhone = interviewContactArr[1];
		}else {
			interviewInfo = resumePostMapper.findResumePostInfoForPhone(interviewId);
		}
		String interviewName = String.valueOf(interviewInfo.get("name"));//被面试者
		String interviewPosition = String.valueOf(interviewInfo.get("postName"));//面试职位
		String interviewTime = String.valueOf(interviewInfo.get("time"));//面试时间
		
//		String interviewPlace = String.valueOf(interviewInfo.get("placeName"));//面试地点
//		String interviewContact = String.valueOf(interviewInfo.get("contactName"));//面试联系人
//		String[] interviewContactArr = interviewContact.split(",");
//		String interviewContactName = interviewContactArr[0];//面试联系人名字
//		String interviewContactPhone = interviewContactArr[1];//面试联系人电话
//		String interviewContactEmail = interviewContactArr[2];//面试联系人邮箱
//		if(!interviewContactEmail.contains("@")) {
//			interviewContactEmail += "@nantian.com.cn";
//		}
//		String interviewContactTemp = interviewContactName + " 电话：" +interviewContactPhone;
		
		StringBuilder text = new StringBuilder();
		text.append("<div id=\"write-custom-write\" tabindex=\"0\" style=\"font-size: 14px; font-family: 宋体; outline: none;\">\r\n" + 
				"    <p>\r\n" + 
				"        <br/>\r\n" + 
				"    </p>\r\n" + 
				"</div>\r\n" + 
				"<div>\r\n" + 
				"    <div>\r\n" + 
				"        <blockquote style=\"margin-Top: 0px; margin-Bottom: 0px; margin-Left: 0.5em\">\r\n" + 
				"            <div>\r\n" + 
				"                <div class=\"FoxDiv20181015135044205123\">\r\n" + 
				"                    <p style=\"color: rgb(51, 51, 51); font-size: 14px; white-space: normal; font-family: 宋体; background-color: rgb(255, 255, 255);\">\r\n" + 
				"                        &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;<span style=\"font-size: 24px; background-color: rgb(195, 214, 155);\">北京南天软件有限公司面试邀请</span>\r\n" + 
				"                    </p>\r\n" + 
				"                    <p style=\"color: rgb(51, 51, 51); font-size: 14px; white-space: normal; font-family: 宋体; background-color: rgb(255, 255, 255);\">\r\n" + 
				"                        <span style=\"font-family: 微软雅黑; text-indent: 21pt;\"></span>\r\n" + 
				"                    </p>\r\n" + 
				"                    <p style=\"margin:0px;\">\r\n" + 
				"                        <span style=\"font-family: 微软雅黑; text-indent: 21pt;\"></span>\r\n" + 
				"                    </p>\r\n" + 
				"                    <p style=\"margin:0px;\">\r\n" + 
				"                        <span style=\"font-family: 微软雅黑; text-indent: 21pt;\"><span style=\"font-family: 微软雅黑; text-indent: 28px;\">"+interviewName+"</span></span>\r\n" + 
				"                    </p>\r\n" + 
				"                    <p style=\"color: rgb(51, 51, 51); font-size: 14px; white-space: normal; font-family: 宋体; background-color: rgb(255, 255, 255);\">\r\n" + 
				"                        <span style=\"font-family: 微软雅黑; text-indent: 21pt;\"> &nbsp;您好：</span><br/>\r\n" + 
				"                    </p>\r\n" + 
				"                    <div style=\"font-size: 14px; white-space: normal; background-color: rgb(255, 255, 255);\">\r\n" + 
				"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
				"                            &nbsp;感谢您对<strong><span style=\"font-family: 微软雅黑;\">北京南天软件有限公司</span></strong>的关注，您的简历已通过公司简历筛选环节，诚邀您参加我公司的面试。欢迎您登陆公司网站进行详细了解（<a href=\"http://www.nantian.com.cn/\" target=\"_blank\"><span style=\"font-family: 微软雅黑;\">www.nantian.com.cn</span></a>） 。\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"\">\r\n" + 
				"                            <span style=\"color: rgb(51, 51, 51); font-family: 宋体;\">　　面试职位:<span style=\"font-family: 宋体; color: rgb(255, 0, 0);\">"+interviewPosition+"</span></span>\r\n" + 
				"                        </p>\r\n");
				if(DicConstants.INTERVIEW_ORDER_METHOD_PLACE.equals(method)) {
					text.append("                        <p class=\"正文\" style=\"\">\r\n" + 
							"                            <span style=\"color: rgb(51, 51, 51); font-family: 宋体;\">　　面试时间：<span style=\"font-family: 宋体; color: rgb(255, 0, 0);\">"+interviewTime+"</span></span>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑; text-indent: 21pt;\">面试地址：<strong style=\"color: rgb(51, 51, 51); font-size: 14px; text-indent: 21pt; white-space: normal; font-family: 微软雅黑; background-color: rgb(255, 255, 255);\">"+interviewPlace+"</strong></span>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"text-indent: 21pt; font-family: 微软雅黑;\">公司福利：</span><strong style=\"text-indent: 21pt; font-family: 微软雅黑;\">五险一金、带薪年假、年终奖金、交通补助、餐费补助、话费补助</strong><br/><strong style=\"font-family: 微软雅黑; text-indent: 21pt;\"></strong>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑;\"><strong>联 系 人 ：<span style=\"color: rgb(51, 51, 51); font-size: 14px; text-indent: 28px; font-family: 微软雅黑; background-color: rgb(255, 255, 255);\"><strong>&nbsp;"+interviewContactName+"</strong></span><strong style=\"color: rgb(51, 51, 51); font-size: 14px; text-indent: 21pt; white-space: normal; font-family: 微软雅黑; background-color: rgb(255, 255, 255);\"><strong>&nbsp; &nbsp;电话：<span t=\"7\" nclick=\"return false;\" data=\"18210527756\" style=\"border-bottom-width: 1px; border-bottom-style: dashed; border-bottom-color: rgb(204, 204, 204); z-index: 1;\"><span t=\"7\" _nclick=\"return false;\" data=\"18210527756\" style=\"border-bottom-width: 1px; border-bottom-style: dashed; border-bottom-color: rgb(204, 204, 204); z-index: 1;\">"+interviewContactPhone+"</span></span></strong></strong></strong></span>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑;\"><strong>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;</strong></span>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑;\"></span>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑;\"><strong><br/></strong></span><br/>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <br/>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑; text-indent: 21pt;\">注意事项：</span>\r\n" + 
							"                        </p>\r\n" + 
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑; text-indent: 21pt;\">请携带个人简历一份、签字笔一支准时参加面试.</span>\r\n" + 
							"                        </p>\r\n");
				}else {
					text.append(
							"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
							"                            <span style=\"font-family: 微软雅黑; text-indent: 21pt;\">近期公司会给您安排电话面试，请保持电话畅通！</span>\r\n" + 
							"                        </p>\r\n");
				}
				text.append(
				"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
				"                            <br/>\r\n" + 
				"                        </p>\r\n" + 
				"						<p class=\"正文\" style=\"font-size: 14px; white-space: normal; color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑;\">岗位要求："+required+"</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"font-size: 14px; white-space: normal; color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑;\">岗位职责："+duty+"</span>\r\n" + 
				"                        </p>"+
				"                        <p class=\"正文\" style=\"color: rgb(51, 51, 51); font-family: 宋体; text-indent: 21pt;\">\r\n" + 
				"                            <br/>\r\n" + 
				"                        </p>\r\n" + 
				"                    </div>\r\n" + 
				
				"                    <div id=\"origbody\" style=\"color: rgb(51, 51, 51); font-size: 14px; white-space: normal; background-color: rgb(255, 255, 255);\">\r\n" + 
				"                        <p style=\"margin-top: 0px; margin-bottom: 0px;\">\r\n" + 
				"                            <strong><span style=\"font-size: 16px; font-family: 宋体;\"></span></strong>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\"><strong>南天公司介绍：</strong></span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;云南南天电子信息产业股份有限公司(以下简称南天信息或南天)，是以软件业务、产品服务业务、集成服务业务、智慧城市业务、创新业务等五大板块业务为主体，以服务为发展方向的现代化高科技企业，具有三十多年建设金融行业和国家部分重点行业信息化工程的丰富经验。1999年8月18日，南天信息在深圳证券交易所挂牌上市，股票代码000948。</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;南天信息分设昆明总部和北京总部，在北京、上海、广州、西安、武汉、昆明等地均设有全资或控股子公司，132个维修站遍布全国。以“与世界信息潮流同步，成为著名信息化服务企业”为企业愿景，以“金融信息化行业领先、信息技术产业一流”为发展定位，南天建立起了覆盖全国的应用开发及销售服务体系，可为客户提供高质量的IT专业服务。</span>\r\n" + 
				"                        </p>\r\n" + 
			
			
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;北京南天软件有限公司于（以下简称“南天软件”）2002年4月正式成立，是南天电子信息产业股份有限公司（股票代码：000948）控股的专门从事软件开发、系统集成服务的专业子公司。公司现有员工1300余人。</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;作为南天信息在北京的经营中心，牵头组织南天信息软件业务和集成及服务业务，负责行业客户（总行级）的关联和维护及联动区域市场。公司所承接的中国邮政储蓄银行逻辑集中系统工程是目前国内和世界上最大的基于开放平台的银行核心业务系统，打破了大型机基础架构在大规模银行核心系统应用领域的垄断，符合自主、安全、可控的国家需要，代表了国际、国内领先的技术水平，获得了工信部、人民银行、邮政集团及邮储银行相关领导的高度评价。南天软件公司始终坚持“以客户为中心”的原则，南天软件的主要服务客户包括中国银行、中国建设银行、中国邮政储蓄银行、中国光大银行、国家开发银行、中国农业银行、中国长城资产管理公司、中国信达资产管理公司、中国水利、中国民航、国家电网、国税总局、中国海关总署多家金融及非金融客户。公司在奥运、六十年大庆、世博会、亚运会、世园会等期间圆满完成了中国银行、中国建设银行、中国邮政储蓄银行、中国光大银行等银行的奥运保障和国庆保障任务。</span>\r\n" + 
				"                        </p>\r\n" + 
			
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\"> 【南天的主要资质】 </span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 信息系统集成及服务大型一级企业</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 信息系统集成及服务一级资质</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 信息系统运维一级资质</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ITSS信息技术服务运行维护标准一级</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; SEI CMMI5级认证（股份公司）</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ISO9001:2015质量管理体系认证</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ISO27001:2013 信息安全管理体系认证</span>\r\n" + 
				"                        </p>\r\n" + 
				
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 国家认定企业技术中心</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 计算机信息系统集成一级资质</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 国家火炬计划重点高新技术企业</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 国家规划布局内重点软件企业</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 软件企业认定证书</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; \"南天NANTIAN\"注册商标被认定为中国***</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 信息安全服务资质（安全工程类一级）</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ITSS信息技术服务运行维护标准符合性证书</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 云南省信息安全技术防范资质（三级）</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ISO9001:2008质量管理体系认证</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ISO20000-1:2011 IT服务管理体系认证</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ISO14001:2004环境管理体系认证</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; ISO27001:2005版信息安全管理体系认证</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; SEI CMMI4级认证（软件成熟度）</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\"> &nbsp;【南天的部分荣誉】 &nbsp;</span>\r\n" + 
				"                        </p>\r\n" + 
				"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
				"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2018年，自主可靠企业核心软件品牌（中国软件行业协会）</span>\r\n" + 
				"                        </p>\r\n" + 
				
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2018年，中国IT服务创新服务奖（ITSS）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2018年，中国互联网+智慧金融领军企业（中国信息协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2018年，连续13次入围\"中国方案商百强\"（商业伙伴）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2018、2017年，中国十佳金融行业、ISV中国智慧城市方案商50强（商业伙伴）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，连续16次入围\"中国软件业务收入前百家企业\"（工信部）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国云计算产业领军企业（工信部、赛迪研究院）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，云计算最具成长力企业（工信部）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国大数据百强企业、500 CLOUD 2017行业云服务运营商、卓越金融行业云服务商（商业伙伴）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，连续两年入围“中国信息化首选服务商”（中国计算机用户协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国信息化和软件服务综合竞争力百强企业（中国软件行业协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国软件和信息技术服务综合竞争力百强企业（中国电子信息行业联合会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国软件和信息服务业最具影响力的行业品牌（中国软件和信息服务业网）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，北京软件和信息服务业综合实力百强企业（北京软件和信息服务业协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国最具影响力软件和信息服务企业(中国软件行业协会)</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，智慧城市最具成长力企业奖（国家信息产业公共服务平台）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，创新软件企业（中国软件行业协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，全国电子信息行业创新企业（中国电子企业协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2017年，中国IT服务领军企业（ITSS）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2016年，南天连续15年入围\"中国软件业务收入前百家企业\"</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2016年，南天第11次成功入围\"中国方案商百强\"</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2016年，中国信息化和软件服务综合竞争力百强企业</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2015-2016年，中国金软件金服务十大杰出企业</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2015年，全国电子信息行业领军企业</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2014年，南天获得\"金融行业信息化最具影响力企业\"称号</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2013年，南天获得\"中国金软件——金融行业最具影响力企业\"称号</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2012年度金融科技杰出企业；南天网络工具平台同时荣获\"2012年度金融科技企业用户信赖产品\"称号</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2012中国年度创新软件企业（中国软件行业协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 2012年度中国金融科技发展论坛\"金融科技杰出企业\"奖</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; \"两岸三地科技100强\" （IT经理世界）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 中国软件生产力风云榜软件生产力第二名（中国软件行业协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 亚洲本地较为知名的系统集成商（IDC）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 中国金融电子化最主要的系统集成商（IDC）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 中国商业科技100强上榜企业（信息周刊）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 十大卓越方案商金榜、十大卓越服务商金榜（SmartPartner）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 中国信息产业IT专业服务优秀服务商（中国计算机用户协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 中国信息产业服务优质、金牌服务企业（中国计算机用户协会）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 中国最具综合竞争力的软件企业（赛迪顾问）</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">&nbsp; 【薪资、福利等相关政策】 &nbsp; </span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 五险一金</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 休假管理：带薪年假、病假、婚丧假、产假等，人性化的调休制度；</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 培训体系：新员工入职培训、专业培训、在职深造（学历提升）等；</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 员工发展：科学的绩效考核及薪酬增长体系、完善的员工职业生涯规划管理；</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 其他福利：员工意外险、年度免费体检、生日慰问、结婚慰问、生育慰问、节日慰问、六一节慰问、项目组慰问、运动协会、各类公益活动等。</span>\r\n" + 
			"                        </p>\r\n" + 
			"                        <p class=\"正文\" style=\"text-indent: 22.5pt;\">\r\n" + 
			"                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">● &nbsp; 南天软件秉承南天多年先进的技术和管理经验、深厚的行业背景以及优质的产品与服务，致力成为我国提供行业应用软件和整体解决方案最优秀的专业信息服务厂商。</span>\r\n" + 
			"                        </p>\r\n" + 
				"                        <p style=\"margin-top: 0px; margin-bottom: 0px;\">\r\n" + 
				"                            <br/>\r\n" + 
				"                        </p>\r\n" + 
				"                        <table class=\"customTableClassName\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin-bottom: 10px; border-collapse: collapse; display: table;\">\r\n" + 
				"                            <tbody>\r\n" + 
				"                                <tr class=\"firstRow\">\r\n" + 
				"                                    <td valign=\"top\" width=\"235\" style=\"border: 1px solid rgb(221, 221, 221); padding: 0cm 5.4pt; background-color: transparent;\"></td>\r\n" + 
				"                                </tr>\r\n" + 
				"                                <tr>\r\n" + 
				"                                    <td valign=\"top\" width=\"235\" style=\"border: 1px solid rgb(221, 221, 221); padding: 0cm 5.4pt; word-break: break-all; background-color: transparent;\">\r\n" + 
				"                                        <p style=\"margin:0px;\">\r\n" + 
				"                                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\"><span style=\"color: black; font-size: 9pt;\">云南南天电子信息产业股份有限公司</span><span style=\"font-size: 9pt;\">&nbsp;</span></span>\r\n" + 
				"                                        </p>\r\n" + 
				"                                        <p style=\"margin:0px;\">\r\n" + 
				"                                            <span style=\"font-size: 9pt; font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">集成业务集团__招聘专员</span>\r\n" + 
				"                                        </p>\r\n" + 
				"                                    </td>\r\n" + 
				"                                </tr>\r\n");
				//if(DicConstants.INTERVIEW_ORDER_METHOD_PLACE.equals(method)) {
					text.append("                                <tr>\r\n" + 
							"                                    <td valign=\"top\" width=\"235\" style=\"border: 1px solid rgb(221, 221, 221); padding: 0cm 5.4pt; word-break: break-all; background-color: transparent;\">\r\n" + 
							"                                        <p style=\"margin:0px;\">\r\n" + 
							"                                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;; font-size: 9pt;\">姓名："+employeeName+"</span>\r\n" + 
							"                                        </p>\r\n" + 
							"                                        <p style=\"margin:0px;\">\r\n" + 
							"                                            <span style=\"font-size: 9pt; font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">电话："+userPhone+"&nbsp; &nbsp; &nbsp; &nbsp;&nbsp;</span>\r\n" + 
							"                                        </p>\r\n" + 
							"                                    </td>\r\n" + 
							"                                </tr>\r\n");
				//}
				text.append(
				"                                <tr>\r\n" + 
				"                                    <td valign=\"top\" width=\"235\" style=\"border: 1px solid rgb(221, 221, 221); padding: 0cm 5.4pt; word-break: break-all; background-color: transparent;\">\r\n" + 
				"                                        <p style=\"margin:0px;\">\r\n" + 
				"                                            <span style=\"font-family: 微软雅黑, &#39;Microsoft YaHei&#39;; font-size: 9pt;\">地址：中国北京海淀区上地信息路10号</span>\r\n" + 
				"                                        </p>\r\n" + 
				"                                        <p style=\"margin:0px;\">\r\n" + 
				"                                            <span style=\"font-size: 9pt; font-family: 微软雅黑, &#39;Microsoft YaHei&#39;;\">邮箱：<a target=\"_blank\" class=\"mailToLink\" address=\""+frommail+"\">" + 
				"                                            "+frommail+"</a></span>\r\n" + 
				"                                        </p>\r\n" + 
				"                                    </td>\r\n" + 
				"                                </tr>\r\n" + 
				"                            </tbody>\r\n" + 
				"                        </table>\r\n" + 
				"                    </div>\r\n" + 
				"                </div>\r\n" + 
				"            </div>\r\n" + 
				"        </blockquote>\r\n" + 
				"    </div>\r\n" + 
				"</div>");
		return Pattern.compile("\t|\r|\n").matcher(text.toString()).replaceAll("");
	}
	
	/**
	 * Description: 邮件收件人
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月12日 下午19:23:43
	 */
	public String getTomailForSendEmail(Integer resumeId) {
		Map<String, Object> resumeInfo = resumeMapper.selectResumeDetail(resumeId);
		if(resumeInfo.get("email")==null || "".equals(resumeInfo.get("email").toString())) {
			return "error";
		}
		String tomail = resumeInfo.get("email").toString();
		return tomail;
	}
	
	/**
	 * Description: 字典管理-新增面试联系人
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月13日 上午10:47:21
	 */
	public RestResponse adminDicInsertContact(Map<String,Object> params) {
		logger.info("adminDicInsertContact方法开始执行，参数是：params="+params);
		try {
			String contactName = String.valueOf(params.get("contactName"));//面试联系人名字
			String contactPhone = String.valueOf(params.get("contactPhone"));//面试联系人电话
			String contactEmail = String.valueOf(params.get("contactEmail"));//面试联系人邮箱
			String dicName = contactName+","+contactPhone+","+contactEmail;//即将入库的字典表数据
			
			ErpAdminDic adminDic = new ErpAdminDic();
			adminDic.setDicType("INTERVIEW_ORDER_CONTACT");
			adminDic.setDicName(dicName);
			
			/*
			 * 字典表去重
			 */
			ErpAdminDic erpAdminDic = adminDicMapper.selectAdminDicByParams(adminDic);
			if(erpAdminDic!=null) {
				return RestUtils.returnSuccessWithString("字典表中已经存在该字段！");
			}
			/*
			 * 查询字典表中的下一个编号dic_code，然后将数据入库
			 * 如果字典表没有数据，则赋初值0；有数据的话，则加1
			 */
			Integer lastCode = adminDicMapper.selectLastCodeByType("INTERVIEW_ORDER_CONTACT");
			if(lastCode==null) {
				adminDic.setDicCode("0");
			}else {
				adminDic.setDicCode(String.valueOf(lastCode+1));
			}
			adminDicMapper.insertAdminDic(adminDic);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("adminDicInsertContact方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 字典管理-新增面试地点
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月13日 上午11:20:41
	 */
	public RestResponse adminDicInsertPlace(Map<String,Object> params) {
		try {
			String interviewPlace = String.valueOf(params.get("interviewPlace"));//面试地点
			
			ErpAdminDic adminDic = new ErpAdminDic();
			adminDic.setDicType("INTERVIEW_ORDER_PLACE");
			adminDic.setDicName(interviewPlace);
			
			/*
			 * 字典表去重
			 */
			ErpAdminDic erpAdminDic = adminDicMapper.selectAdminDicByParams(adminDic);
			if(erpAdminDic!=null) {
				return RestUtils.returnSuccessWithString("字典表中已经存在该字段！");
			}
			
			/*
			 * 查询字典表中的下一个编号dic_code，然后将数据入库
			 */
			Integer lastCode = adminDicMapper.selectLastCodeByType("INTERVIEW_ORDER_PLACE");
			adminDic.setDicCode(String.valueOf(lastCode+1));
			adminDicMapper.insertAdminDic(adminDic);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("adminDicInsertPlace方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 字典管理-查询全部的面试方式、联系人、面试地点
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月14日 上午22:20:51
	 */
	public RestResponse adminDicQueryAll() {
		logger.info("adminDicQueryAll方法开始执行，无参数");
		try {
			Map<String,Object> resultMap = new HashMap<>();
			/*
			 * 面试预约地点
			 */
			List<ErpAdminDic> placeList = adminDicMapper.selectAdminDicByType("INTERVIEW_ORDER_PLACE");
			resultMap.put("placeList", placeList);
			/*
			 * 面试预约联系人
			 */
			List<ErpAdminDic> contactList = adminDicMapper.selectAdminDicByType("INTERVIEW_ORDER_CONTACT");
			for (ErpAdminDic contact : contactList) {
				/*
				 * 解析字典表数据，按照逗号将联系人的姓名、电话拆分开，再组合成前端需要显示的样式
				 */
				String contactName = contact.getDicName();
				String[] contactNameArr = contactName.split(",");
				String tempContactName = contactNameArr[0] + " 电话：" +contactNameArr[1];
				contact.setDicName(tempContactName);
			}
			resultMap.put("contactList", contactList);
			/*
			 * 面试预约方式
			 */
			List<ErpAdminDic> methodList = adminDicMapper.selectAdminDicByType("INTERVIEW_ORDER_METHOD");
			resultMap.put("methodList", methodList);
			return RestUtils.returnSuccess(resultMap);
		} catch (Exception e) {
			logger.info("adminDicQueryAll方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
}
