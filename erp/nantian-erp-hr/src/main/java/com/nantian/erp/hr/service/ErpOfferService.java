package com.nantian.erp.hr.service;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.dao.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.model.ErpEmployeeEntry;
import com.nantian.erp.hr.data.model.ErpOffer;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ErpRecord;
import com.nantian.erp.hr.data.model.ErpResume;
import com.nantian.erp.hr.data.model.ErpResumePost;
import com.nantian.erp.hr.data.model.ErpResumePostReexam;
import com.nantian.erp.hr.data.model.PositionOperRecond;
import com.nantian.erp.hr.data.vo.ErpOfferVo;
import com.nantian.erp.hr.util.AesUtils;
import com.nantian.erp.hr.util.FileUtils;
import com.nantian.erp.hr.util.RestTemplateUtils;

/** 
 * Description: offer的service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月13日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
//@PropertySource(value= {"classpath:config/sftp.properties","classpath:config/email.properties","classpath:config/host.properties"})
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpOfferService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/*
	 * 从配置文件中获取SFTP相关属性
	 */
    @Value("${sftp.basePath}")
    private String basePath;//服务器基本路径
    @Value("${sftp.resumePath}")
    private String resumePath;//简历文件路径
    @Value("${sftp.offerPath}")
    private String offerPath;//offer文件路径
    @Value("${sftp.reportPath}")
    private String reportPath;//职业性格测验报告的文件路径
    /*
	 * 从配置文件中获取Email相关属性
	 */
    @Value("${email.service.host}")
	private String emailServiceHost;//邮件服务的IP地址和端口号
	@Value("${environment.type}")
	private String environmentType;//环境类型（根据该标识，决定邮件的发送人、抄送人、收件人）
	@Value("${prod.email.offer.frommail}")
	private String prodEmailOfferFrommail;//生产环境offer发件人
	@Value("${prod.email.offer.bcc}")
	private String prodEmailOfferBcc;//生产环境offer抄送人
	@Value("${prod.email.offer.tomail}")
	private String prodEmailOfferTomail;//生产环境offer收件人
	
    @Value("${test.email.frommail}")
	private String testEmailFrommail;//测试环境发件人
	@Value("${test.email.bcc}")
	private String testEmailBcc;//测试环境抄送人
	@Value("${test.email.tomail}")
	private String testEmailTomail;//测试环境收件人
	
	/*
	 * 从配置文件中获取主机相关属性
	 */
    @Value("${protocol.type}")
    private String protocolType;//http或https
	
	@Autowired
	private ErpOfferMapper offerMapper;
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private ErpPostMapper postMapper;
	@Autowired
	private ErpEmployeeEntryMapper employeeEntryMapper;
	@Autowired
	private ErpRecordMapper recordMapper;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private FileUtils fileUtils;
	@Autowired
	private ErpResumePostMapper resumePostMapper;
	@Autowired
	private ErpInterviewService erpInterviewService;
	@Autowired
	private ErpResumePostReexamMapper resumePostReexamMapper;
	@Autowired
	private RestTemplateUtils restTemplateUtils;

	@Autowired
	PositionOperReordMapper operRecordMapper;

	@Autowired
	AdminDicMapper adminDicMapper;

	/**
	 *
	 *
	 * Description: 查询待处理的offer列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月13日 下午20:50:07
	 */
	public RestResponse waitingForMeQueryList(Boolean isTrainee) {
		logger.info("进入waitingForMeQueryList方法，参数是："+isTrainee);
		List<Map<String, Object>> offerList = null;
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isTrainee", isTrainee);
			paramsMap.put("status", DicConstants.OFFER_STATUS_WAITING);
			offerList = offerMapper.selectOfferInfoByParams(paramsMap);
			return RestUtils.returnSuccess(offerList);
		} catch (Exception e) {
			logger.error("waitingForMeQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 查询待处理的offer列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月13日 下午20:52:13
	 */
	public RestResponse validOfferQueryList(String token, Boolean isTrainee) {
		logger.info("进入validOfferQueryList方法，参数是："+isTrainee);
		List<Map<String, Object>> offerList = null;
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isTrainee", isTrainee);
			paramsMap.put("status", DicConstants.OFFER_STATUS_VALID);

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
				return RestUtils.returnSuccessWithString("无可查看的offer");
			}
			
			offerList = offerMapper.selectOfferInfoByParams(paramsMap);
			return RestUtils.returnSuccess(offerList);
		} catch (Exception e) {
			logger.error("validOfferQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 查询待处理的offer列表
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月13日 下午20:54:45
	 */
	public RestResponse invalidOfferQueryList(Boolean isTrainee) {
		logger.info("进入invalidOfferQueryList方法，参数是："+isTrainee);
		List<Map<String, Object>> offerList = null;
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isTrainee", isTrainee);
			//paramsMap.put("status", DicConstants.OFFER_STATUS_INVALID);
			offerList = offerMapper.selsectAllInvalidOffer(paramsMap);
			return RestUtils.returnSuccess(offerList);
		} catch (Exception e) {
			logger.error("invalidOfferQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 查询一条offer的详情
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月19日 下午18:18:53
	 */
	@SuppressWarnings({ "unchecked" })
	public RestResponse findOfferDetail(String token,Boolean isTrainee,Integer offerId) {
		logger.info("进入findOfferDetail方法，参数是：isTrainee="+isTrainee+",offerId="+offerId);
		Map<String, Object> offerDetail = null;
		try {
			offerDetail = offerMapper.selectOfferDetail(offerId);
			/*
			 * 实习生需要跨工程查询薪酬数据
			 */
			if(isTrainee) {
				String url = protocolType+"nantian-erp-salary/nantian-erp/salary/talkSalary/findErpTalkSalary?offerId="+offerId;
				logger.info(protocolType);
				//String url = salaryServerHost+"/nantian-erp/salary/talkSalary/findErpTalkSalary?offerId="+offerId;
				MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.add("token", token);
				HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<MultiValueMap<String,Object>>(body, headers);
				ResponseEntity<RestResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestResponse.class);
				//解析获取的数据
				if(response.getStatusCodeValue() != 200) {
					return RestUtils.returnFailure("调用薪酬工程查询实习生薪资时发生异常，导致查询失败！");
				}
				if(!"200".equals(response.getBody().getStatus())) {
					return RestUtils.returnFailure("薪酬工程查询实习生薪资时发生异常，导致查询失败！");
				}
				if(!"".equals(response.getBody().getData())) {
					Map<String,Object> traineeSalaryData = (Map<String, Object>) response.getBody().getData();
					Map<String,Object> decryptedData = decryptSalaryDataAes(traineeSalaryData);
					offerDetail.put("baseWage", decryptedData.get("baseWage"));
					offerDetail.put("monthAllowance", decryptedData.get("monthAllowance"));
					offerDetail.put("remark", decryptedData.get("remark"));
				}
			}
			return RestUtils.returnSuccess(offerDetail);
		} catch (Exception e) {
			logger.error("findOfferDetail出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}

	/**
	 * Description: 将offer状态修改为“已归档”
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月13日 下午20:58:13
	 */
	public RestResponse invalidOffer(String token,Map<String,Object> params) {
		logger.info("进入invalidOffer方法，参数是：token="+token+",params="+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer offerId = Integer.valueOf(String.valueOf(params.get("offerId")));//offer编号
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			String reason = String.valueOf(params.get("reason"));//归档原因
			
			/*
			 * 修改offer表信息，将状态改为“归档”
			 */
			ErpOffer offer = new ErpOffer();
			offer.setOfferId(offerId);
			offer.setReason(reason);
			offer.setStatus(DicConstants.NO_SENDOFFER_INVALID); //offer状态改为未发offer归档
			offerMapper.updateOffer(offer);
			
			/*
			 * 新增一条记录信息，到记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setContent(reason);
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);

			//更改流程表状态
			ErpEmployeeEntry employeeEntry = new ErpEmployeeEntry();
			employeeEntry.setStatus(4);  //放弃入职
			employeeEntry.setOfferId(offerId);
			employeeEntryMapper.updateEmployeeEntryByOfferId(employeeEntry);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("invalidOffer方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}

	/**
	 * Description: 补充录入offer信息
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月14日 下午10:08:25
	 * @version 2019年09月17日 下午12:43:00
	 */
	public RestResponse enterOffer(MultipartFile reportFile,String saveFlag, ErpOfferVo offerVo,String token) {
		logger.info("进入enterOffer方法，参数是："+offerVo+",token："+token);
		try {
			//接收前端传递过来的参数
			Integer offerId = offerVo.getOfferId();//offer编号
			Boolean isTrainee = offerVo.getIsTrainee();//是否是实习生
			String idCardNumber = offerVo.getIdCardNumber() !=null ? offerVo.getIdCardNumber().trim() : null;//身份证号码
			
			//定义一个offer对象，用于更新offer信息
			ErpOffer offer = new ErpOffer();
			
			//如果用户没有上传测试报告文件，并且数据库也没有该文件的信息，向前端返回错误信息
			Map<String, Object> offerDetail = offerMapper.selectOfferDetail(offerId);
			if(reportFile==null) {
				if (!saveFlag.equalsIgnoreCase("Y")) {	// 2019-09-17
					if(offerDetail.get("reportFileName")==null) {
//						return RestUtils.returnSuccessWithString("职业性格测验报告未上传！");
						return RestUtils.returnSuccess("","职业性格测验报告未上传！");
					}
				}
			}else {
				/*
				 * 如果文件不为空，则上传职业性格测验报告；并获取到文件的名称，赋值给PO
				 */
				/* 文件的路径  */
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH)+1;
				int date = calendar.get(Calendar.DATE);
				//通过时间，在服务器动态产生路径，来保存文件
				String datePath = "/"+year+"/"+month+"/"+date+"/";
				Map<String,Object> resultMap = fileUtils.uploadFileBySFTP(reportFile, reportPath+datePath);
				Boolean isSuccess = (Boolean) resultMap.get("isSuccess");
				if(isSuccess) {
					offer.setReportFileName(datePath + (String) resultMap.get("data"));
				}else {
					return RestUtils.returnSuccess("", "职业性格测验报告上传失败！导致offer信息录入失败！"+resultMap.get("data"));
				}
			}
			
			/*
			 * 实习生和社招生录入的offer信息不一样
			 */
			if(isTrainee) {
				/*
				 * 接收前端传递过来的实习生参数
				 */
				String entryTime = offerVo.getEntryTime();//实习开始时间
				String baseWage = offerVo.getBaseWage();//基本工资
				String monthAllowance = offerVo.getMonthAllowance();//月度项目津贴
				String salaryRemark = offerVo.getSalaryRemark();//谈薪备注
				ErpPositionRankRelation erpPositionRankRelation = resumePostMapper.findTraineePositionRankList();
				Integer positionNo=erpPositionRankRelation.getPositionNo();//实习生职位ID
				Integer rank=erpPositionRankRelation.getRank();//实习生职级ID
				/*
				 * 将薪酬数据加密
				 */
				Map<String,String> salaryData = new HashMap<>();
				salaryData.put("baseWage", baseWage);
				salaryData.put("monthAllowance", monthAllowance);
				Map<String,String> encryptedSalaryData = this.encryptSalaryDataAes(salaryData);
				
				/*
				 * 跨工程调用，将实习生的面试谈薪情况插入到薪酬数据库中
				 */
				Map<String,Object> salaryMap = new HashMap<>();
				salaryMap.put("baseWage", encryptedSalaryData.get("baseWage"));
				salaryMap.put("monthAllowance", encryptedSalaryData.get("monthAllowance"));
				salaryMap.put("offerId", offerId);
				salaryMap.put("salaryRemark", salaryRemark);
				salaryMap.put("token", token);
				
				String resultStr=null;
				if (saveFlag.equalsIgnoreCase("Y")) {	// 2019-09-17
					
					resultStr = this.insertErpTalkSalary(salaryMap);
					
					if(!"OK".equals(resultStr)) {
						return RestUtils.returnSuccess("",resultStr);
						
					}
				}else {
					RestResponse offerInfoObject = findOfferDetail(token, true,offerId);
					if(offerInfoObject.getStatus() == "200") {
						Map<String, Object> offerData = null;
						offerData = (Map<String, Object>) offerInfoObject.getData();
						if (offerData != null) {
							if(offerData.get("baseWage") != null || offerData.get("monthAllowance") != null) {
								salaryMap.put("baseWage", baseWage);
								salaryMap.put("monthAllowance", monthAllowance);
								this.updateErpTalkSalary(salaryMap);
							}else {
								resultStr = this.insertErpTalkSalary(salaryMap);
							}
						}else {
							resultStr = this.insertErpTalkSalary(salaryMap);
						}
					}
				}
								
				/*
				 * 封装实习生offer对象
				 */
				offer.setOfferId(offerId);
				offer.setIdCardNumber(idCardNumber);
				offer.setEntryTime(entryTime);
				offer.setPosition(positionNo);
				offer.setRank(rank);
			}else {
				/*
				 * 接收前端传递过来的社招生参数
				 */
				String entryTime = offerVo.getEntryTime();//入职时间
				String gradCertNumber = offerVo.getGradCertNumber();//毕业证书编号
				String jobPosition = offerVo.getJobPosition();//对应招聘岗位
				String channel = offerVo.getChannel();//招聘渠道
				String offerRemark = offerVo.getOfferRemark();//offer备注
				
				/*
				 * 封装社招生offer对象
				 */
				offer.setOfferId(offerId);
				offer.setIdCardNumber(idCardNumber);
				offer.setGradCertNumber(gradCertNumber);
				offer.setJobPosition(jobPosition);
				offer.setChannel(channel);
				offer.setRemark(offerRemark);
				offer.setEntryTime(entryTime);
			}
			//更新offer信息
			offerMapper.updateOffer(offer);
			
			//根据offerID查询该offer的详细信息
			Map<String, Object> offerDetailAfterUpdate = offerMapper.selectOfferDetail(offerId);
			
			/*
			 * 动态生成offer的excel文件，传到服务器上，并将文件的名称，赋值给PO
			 */
			//创建offer文件，如果成功后返回文件名
			String offerName = createOfferFileForSFTP(offerDetailAfterUpdate);
			if(offerName.contains("异常")) {
//				return RestUtils.returnSuccessWithString("动态生成的offer信息的excel文件创建失败！导致offer信息录入失败！"+offerName);
				return RestUtils.returnSuccess("","动态生成的offer信息的excel文件创建失败！导致offer信息录入失败！"+offerName);
				
			}
			
			/* 文件的路径  */
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH)+1;
			int date = calendar.get(Calendar.DATE);
			//通过时间，在服务器动态产生路径，来保存文件
			String datePath = "/"+year+"/"+month+"/"+date+"/";
			Map<String,Object> resultMap = fileUtils.uploadFileBySFTP(DicConstants.ENTER_OFFER_TEMP_PATH, offerName, offerPath+datePath);
			Boolean isSuccess = (Boolean) resultMap.get("isSuccess");
			if(isSuccess) {
				offer.setOfferFileName(datePath + (String) resultMap.get("data"));
				//更新offer信息
				offerMapper.updateOffer(offer);
				return RestUtils.returnSuccess("","OK");
			}else {
				return RestUtils.returnSuccess("","动态生成的offer信息的excel文件上传失败！导致offer信息录入失败！"+resultMap.get("data"));
			}
		} catch (Exception e) {
			logger.error("enterOffer方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 修改offer信息
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月19日 下午19:05:05
	 */
	public RestResponse updateOffer(ErpOffer offer,String token) {
		logger.info("进入updateOffer方法，参数是："+offer);
		try {
			offerMapper.updateOffer(offer);
			
			/*
			 * 保存面试记录表中
			 */
			Map<String,Object> offerInfo = offerMapper.selectOfferDetail(offer.getOfferId());

			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("修改offer信息");
			record.setResumeId((Integer) offerInfo.get("resumeId"));
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("updateOffer方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 修改offer薪资信息
	 *
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年2月19日 下午19:05:05
	 */
	public RestResponse talkSalaryChange(ErpOffer offer,String token) {
		logger.info("进入talkSalaryChange方法，参数是："+offer);
		try {
			Map<String,Object> offerInfo = offerMapper.selectOfferDetail(offer.getOfferId());
			
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("修改面试谈薪信息");
			record.setResumeId((Integer) offerInfo.get("resumeId"));
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("talkSalaryChange方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 发送offer
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月20日 上午19:52:11
	 */
	@Transactional
	public RestResponse sendOffer(String token,Map<String,Object> params) {
		logger.info("进入sendOffer方法，参数是：params="+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getUsername();//从用户信息中获取用户邮箱
			Integer userId=erpUser.getUserId();//用户id
			Integer offerId = Integer.valueOf(String.valueOf(params.get("offerId")));//offer编号
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));//岗位编号
			
			//入职人数达到岗位上限，不能再招
			Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);//该岗位已入职人数
			Integer conutAllvalityOffer = offerMapper.selectCountAllValiOffer(postId);//有效offer
			Integer  newCount = CountAllEntry + conutAllvalityOffer;
			Map<String, Object> postMap = postMapper.findByPostId(postId);
		    if(postMap.containsKey("numberPeople")) {
		    	Integer numberPeople = (Integer) postMap.get("numberPeople");//实际招聘人数
		    	 if (CountAllEntry.equals(numberPeople)){
		    		return RestUtils.returnSuccess("", "该岗位已招满！");
		    		}else if (newCount.equals(numberPeople)) {
			    		return RestUtils.returnSuccess("", "请先处理已发送offer！");
			    	}
		    	}
			/*
			 * 通过offerId查询三个文件的名称（简历、职业性格测验报告、动态生成的offer信息的excel）
			 */
			Map<String, Object> offerDetail = offerMapper.selectOfferDetail(offerId);
			
			Boolean isTrainee = Boolean.valueOf(String.valueOf(params.get("isTrainee")));
			Boolean flag = true;
			String info = "";
			if(isTrainee) {
				RestResponse offerInfoObject = findOfferDetail(token, true,offerId);
				if(offerInfoObject.getStatus() == "200") {
					Map<String, Object> offerData = null;
					offerData = (Map<String, Object>) offerInfoObject.getData();
					if (offerData == null) {
						flag = false;
						info = "基本工资为必填项，请填写基本工资后再提交！ 月度项目津贴为必填项，请填写月度项目津贴后再提交！ ";
					}else {
						if(offerData.get("baseWage") == null) {
							flag = false;
							info = "基本工资为必填项，请填写基本工资后再提交！  ";
						}
						if(offerData.get("monthAllowance") == null) {
							flag = false;
							info = "月度项目津贴为必填项，请填写月度项目津贴后再提交！  " + info;
						}
					}
				}
				
				if (offerDetail.get("idCardNumber") == null) {
					flag = false;
					info = "身份证为必填项，请填写身份证号后再提交！  " + info;
				}
				
			}else {
				if (offerDetail.get("idCardNumber") == null) {
					flag = false;
					info = "身份证为必填项，请填写身份证号后再提交！    " + info;
				}
				if (offerDetail.get("gradCertNumber") == null) {
					flag = false;
					info = "毕业证书编号为必填项，请填写毕业证书编号后再提交！  " + info;
				}
				if (offerDetail.get("jobPosition") == null) {
					flag = false;
					info = "对应招聘岗位为必填项，请填写对应招聘岗位后再提交！  " + info;
				}
				if (offerDetail.get("channel") == null) {
					flag = false;
					info = "招聘渠道为必填项，请填写招聘渠道后再提交！  " + info;
				}
			}
			
			if(!flag) {
				return RestUtils.returnSuccess("",info);
			}
			
			if(offerDetail.get("reportFileName")==null) {
				return RestUtils.returnSuccess("","未找到职业性格测验报告文件！");
			}
			if(offerDetail.get("offerFileName")==null) {
				return RestUtils.returnSuccess("","未找到动态生成的offer信息文件！");
			}
			String resumeDatePathAndName = null;//简历文件日期路径+文件名
			if(offerDetail.get("resumeFileName")!=null&&!"".equals(offerDetail.get("resumeFileName"))) {
				resumeDatePathAndName = String.valueOf(offerDetail.get("resumeFileName"));
			}
			String reportDatePathAndName = String.valueOf(offerDetail.get("reportFileName"));//报告文件日期路径+文件名
			String offerDatePathAndName = String.valueOf(offerDetail.get("offerFileName"));//offer文件日期路径+文件名
			
			/*
			 * 发送offer后，该记录自动变为有效offer
			 */
			ErpOffer offer = new ErpOffer();
			offer.setOfferId(offerId);
			offer.setStatus(DicConstants.OFFER_STATUS_VALID); //offer状态改为有效
			offer.setSendUserId(userId);
			offerMapper.updateOffer(offer);
			/*
			 * 修改简历的状态为“已发offer”，并将简历归档
			 */
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setIsValid(false);
			resume.setStatus(DicConstants.RESUME_STATUS_SENT_OFFER);
			resumeMapper.updateResume(resume);
			/*
			 * 新增一条员工入职流程记录
			 */
			ErpEmployeeEntry employeeEntry = new ErpEmployeeEntry();
			employeeEntry.setRoleID(1);
			employeeEntry.setStatus(1);
			employeeEntry.setOfferId(offerId);
			//ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			Integer getUserId = erpUser.getUserId();//从用户信息中获取用户名
			employeeEntry.setCurrentPersonID(getUserId);
			employeeEntryMapper.insertEmployeeEntry(employeeEntry);
			
			/*
			 * 将数据库中的文件名称字段，拆分为日期路径、文件名
			 */
			String resumeName = null;//去掉日期的简历文件名
			if(resumeDatePathAndName!=null&&!"".equals(resumeDatePathAndName)) {
				resumeName = resumeDatePathAndName.substring(resumeDatePathAndName.lastIndexOf('/')+1);
			}
			String reportName = reportDatePathAndName.substring(reportDatePathAndName.lastIndexOf('/')+1);//去掉日期的报告文件名
			String offerName = offerDatePathAndName.substring(offerDatePathAndName.lastIndexOf('/')+1);//去掉日期的offer文件名
			

			//在岗位记录表中插入记录
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("发送offer："+name); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(erpUser.getEmployeeName());//当前处理人Id
    		operRec.setPostId(postId);
    		operRecordMapper.addPositionOperReord(operRec);
			
			/*
			 * 信息录入后，点击发送offer自动发送邮件，填写收发件人（之前是在属性文件中获取发件人和收件人）
			 */
			//String frommail = prodEmailOfferFrommail;//offer发件人
			String frommail = username;//offer发件人
			String bcc = this.getBccForSendEmail(postId,token);//抄送人（岗位申请人、一级部门经理）
			if("error".equals(bcc)) {
				return RestUtils.returnFailure("通过用户Id未获取到用户（邮件抄送人）邮箱！");
			}
			bcc += ","+prodEmailOfferBcc;//抄送人增加HR
			String subject = this.getSubjectForSendEmail(offerDetail);//主题
			String text = this.getTextForSendEmail(offerDetail);//邮件内容
			String tomail = prodEmailOfferTomail;//offer收件人
			
			List<Map<String,String>> attachments = new ArrayList<>();//多个附件
			if(resumeDatePathAndName!=null&&!"".equals(resumeDatePathAndName)) {
				Map<String,String> resumeFile = new HashMap<>();
				logger.info("resumeName="+resumeName+",resumeFilePath="+basePath+resumePath+resumeDatePathAndName);
				resumeFile.put("filename", resumeName);
	    		resumeFile.put("filepath", basePath+resumePath+resumeDatePathAndName);
				attachments.add(resumeFile);
			}
			
			Map<String,String> reportFile = new HashMap<>();
			logger.info("reportName="+reportName+",reportFilePath="+basePath+resumePath+reportDatePathAndName);
			reportFile.put("filename", reportName);
			reportFile.put("filepath", basePath+reportPath+reportDatePathAndName);
			attachments.add(reportFile);
			
			Map<String,String> offerFile = new HashMap<>();
			logger.info("offerName="+offerName+",offerFilePath="+basePath+resumePath+offerDatePathAndName);
			offerFile.put("filename", offerName);
			offerFile.put("filepath", basePath+offerPath+offerDatePathAndName);
			attachments.add(offerFile);

			Map<String,Object> emailUserNamePassword = getEmailUserNameAndPassword();

			boolean sendSuccess = restTemplateUtils.sendEmailWithAttachments(frommail, bcc, subject, text, tomail, attachments, emailUserNamePassword,DicConstants.SEND_OFFER_EMAIL_TYPE,null);
			if(!sendSuccess) {
				return RestUtils.returnSuccess("", "发送offer流程进行完毕！但是offer邮件发送失败，请手工发送！");
			}
			return RestUtils.returnSuccess("", "OK");
		} catch (Exception e) {
			logger.error("sendOffer方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 定时任务每天0点半自动将入职三天后的offer归档
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月26日 上午10:36:12
	 */
	@Transactional
	public void automaticInvalidOfferScheduler() {
		logger.info("进入automaticInvalidOfferScheduler方法，无参数");
		try {
			//查询所有入职三天后的offer，作为待归档的offer
			List<ErpOffer> offerList = offerMapper.selectOfferAutomaticInvalid();
			for (ErpOffer offer : offerList) {
				offer.setStatus(DicConstants.OFFER_STATUS_INVALID);
				offer.setReason("定时任务自动归档");
				offerMapper.updateOffer(offer);
			}
			logger.info("automaticInvalidOfferScheduler执行成功！");
		} catch (Exception e) {
			logger.error("automaticInvalidOfferScheduler发生异常 ："+e.getMessage(),e);
		}
	}
	
	/**
	 * Description: 定时任务每天1点删除发送offer时产生的临时文件
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月05日 16:12:01
	 */
	public void automaticDeleteTempFilesScheduler() {
		logger.info("进入automaticDeleteTempFilesScheduler方法，无参数");
		try {
			//清理录入offer时的文件临时路径
			fileUtils.deleteFileByPath(DicConstants.ENTER_OFFER_TEMP_PATH);
			logger.info("automaticDeleteTempFilesScheduler执行成功！");
		} catch (Exception e) {
			logger.error("automaticDeleteTempFilesScheduler发生异常 ：",e.getMessage(),e);
		}
	}

	
	/* **************************************** 封装的工具方法 **************************************** */
	
	/**
	 * Description: 通过SFTP上传文件
	 * 
	 * @param resumeFile 职业性格测验报告
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月05日 上午10:52:50
	 */

	
	/**
	 * Description: 动态生成offer信息的Excel文件
	 * 
	 * @param offerId offer编号
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月20日 下午09:54:47
	 */
	public String createOfferFileForSFTP(Map<String, Object> offerDetail) {
		logger.info("进入createOfferFileForSFTP方法，参数是：offerDetail="+offerDetail);
		try {
			// 定义工作簿
			XSSFWorkbook workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("offer");
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("序号");
			firstRow.createCell(1).setCellValue("一级部门");
			firstRow.createCell(2).setCellValue("二级部门");
			firstRow.createCell(3).setCellValue("姓名");
			firstRow.createCell(4).setCellValue("身份证号");
			firstRow.createCell(5).setCellValue("性别");
			firstRow.createCell(6).setCellValue("联系电话");
			firstRow.createCell(7).setCellValue("邮箱");
			firstRow.createCell(8).setCellValue("毕业证书编号");
			firstRow.createCell(9).setCellValue("职位名称");
			firstRow.createCell(10).setCellValue("职级");
			firstRow.createCell(11).setCellValue("入职时间（劳动合同开始日期）");
			firstRow.createCell(12).setCellValue("劳动合同期限（年）");
			firstRow.createCell(13).setCellValue("试用期限（月）");
			firstRow.createCell(14).setCellValue("对应招聘岗位");
			firstRow.createCell(15).setCellValue("招聘渠道");
			firstRow.createCell(16).setCellValue("备注");
			
			//Map<String, Object> offerDetail = offerMapper.selectOfferDetail(offerId);
			// 下一行
			XSSFRow secondRow = sheet.createRow(1);
			secondRow.createCell(0).setCellValue(1);
			secondRow.createCell(1).setCellValue(String.valueOf(offerDetail.get("firstDepartment")==null?"":offerDetail.get("firstDepartment")));
			secondRow.createCell(2).setCellValue(String.valueOf(offerDetail.get("secondDepartment")==null?"":offerDetail.get("secondDepartment")));
			secondRow.createCell(3).setCellValue(String.valueOf(offerDetail.get("name")==null?"":offerDetail.get("name")));
			secondRow.createCell(4).setCellValue(String.valueOf(offerDetail.get("idCardNumber")==null?"":offerDetail.get("idCardNumber")));
			secondRow.createCell(5).setCellValue(String.valueOf(offerDetail.get("sex")==null?"":offerDetail.get("sex")));
			secondRow.createCell(6).setCellValue(String.valueOf(offerDetail.get("phone")==null?"":offerDetail.get("phone")));
			secondRow.createCell(7).setCellValue(String.valueOf(offerDetail.get("email")==null?"":offerDetail.get("email")));
			secondRow.createCell(8).setCellValue(String.valueOf(offerDetail.get("gradCertNumber")==null?"":offerDetail.get("gradCertNumber")));
			secondRow.createCell(9).setCellValue(String.valueOf(offerDetail.get("positionName")==null?"":offerDetail.get("positionName")));
			secondRow.createCell(10).setCellValue(String.valueOf(offerDetail.get("rank")==null?"":offerDetail.get("rank")));
			secondRow.createCell(11).setCellValue(String.valueOf(offerDetail.get("entryTime")==null?"":offerDetail.get("entryTime")));
			secondRow.createCell(12).setCellValue(String.valueOf(offerDetail.get("contractPeriod")==null?"":offerDetail.get("contractPeriod")));
			secondRow.createCell(13).setCellValue(String.valueOf(offerDetail.get("probationPeriod")==null?"":offerDetail.get("probationPeriod")));
			secondRow.createCell(14).setCellValue(String.valueOf(offerDetail.get("jobPosition")==null?"":offerDetail.get("jobPosition")));
			secondRow.createCell(15).setCellValue(String.valueOf(offerDetail.get("channel")==null?"":offerDetail.get("channel")));
			secondRow.createCell(16).setCellValue(String.valueOf(offerDetail.get("offerRemark")==null?"":offerDetail.get("offerRemark")));
			
			/*
			 * 将文件上传至服务器临时目录，并关闭资源:
			 */
			String filePath = DicConstants.ENTER_OFFER_TEMP_PATH;
			String name = String.valueOf(offerDetail.get("name")==null?"":offerDetail.get("name"));
			String firstDepartment = String.valueOf(offerDetail.get("firstDepartment")==null?"":offerDetail.get("firstDepartment"));
			//String secondDepartment = String.valueOf(offerDetail.get("secondDepartment"));
			String reportName = "offer--集成--"+firstDepartment+"--"+name+".xlsx";
			String fileName = filePath + "/" + reportName;
			// 判断是否有此路径的文件夹，如果没有，就一层一层创建
			File isFile = new File(filePath);
			if (!isFile.exists()) {
				isFile.mkdirs();
			}
			File file = new File(fileName);
			FileOutputStream out = new FileOutputStream(file);
			workBook.write(out);
			out.flush();
			out.close();
			workBook.close();
			return reportName;
		} catch (Exception e) {
			logger.error("createOfferFileForSFTP方法出现异常："+e.getMessage(),e);
			return "createOfferFileForSFTP方法出现异常："+e.getMessage();
		}
	}
	
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
		String baseWage = salaryData.get("baseWage");//基本工资 
		String monthAllowance = salaryData.get("monthAllowance");//月度津贴
		
		/*
		 * 将薪酬加密后，赋值给Map
		 * 如果薪酬为空，那么赋值为0.0
		 */
		Map<String,String> encryptedSalaryData = new HashMap<>();
		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.0));
		if(baseWage==null) {
			encryptedSalaryData.put("baseWage", defaultSalaryValue);
		}else {
			encryptedSalaryData.put("baseWage", AesUtils.encrypt(baseWage));
		}
		if(monthAllowance==null) {
			encryptedSalaryData.put("monthAllowance", defaultSalaryValue);
		}else {
			encryptedSalaryData.put("monthAllowance", AesUtils.encrypt(monthAllowance));
		}
		return encryptedSalaryData;
	}
	
	/**
	 * Description: 薪酬数据解密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月20日 上午14:29:37
	 */
	public Map<String,Object> decryptSalaryDataAes(Map<String,Object> traineeSalaryData){
		//logger.info("进入解密方法，参数是："+traineeSalaryData);
		/*
		 * 获取到所有需要解密的薪酬数据
		 */
		String baseWage = traineeSalaryData.get("baseWage")==null?"":String.valueOf(traineeSalaryData.get("baseWage"));//基本工资
		String monthAllowance = traineeSalaryData.get("monthAllowance")==null?"":String.valueOf(traineeSalaryData.get("monthAllowance"));//月度津贴
		
		/*
		 * 将薪酬解密，将字符串类型转换为浮点型，赋值给Map
		 */
		traineeSalaryData.put("baseWage", AesUtils.decrypt(baseWage));
		traineeSalaryData.put("monthAllowance", AesUtils.decrypt(monthAllowance));
		//logger.info("解密后的参数是："+traineeSalaryData);
		return traineeSalaryData;
	}
	
	/**
	 * Description: 将实习生的薪酬数据，跨工程插入到薪酬数据库中
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月14日 下午15:00:52
	 */
	@SuppressWarnings("rawtypes")
	public String insertErpTalkSalary(Map<String,Object> salaryMap) {
		logger.info("insertErpTalkSalary方法开始执行，参数是："+salaryMap);
		try {
			//调用薪酬工程，将月度收入、社保基数、公积金基数存入薪酬的数据库表中
			String url = protocolType+"nantian-erp-salary/nantian-erp/salary/talkSalary/insertErpTalkSalary";
			//String url = salaryServerHost+"/nantian-erp/salary/talkSalary/insertErpTalkSalary";
			Map<String,Object> erpTalkSalary = new HashMap<>();
			erpTalkSalary.put("baseWage", salaryMap.get("baseWage"));
			erpTalkSalary.put("monthAllowance", salaryMap.get("monthAllowance"));
			erpTalkSalary.put("offerId", salaryMap.get("offerId"));
			erpTalkSalary.put("remark", salaryMap.get("salaryRemark"));
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",salaryMap.get("token").toString());//将token放到请求头中
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(erpTalkSalary, requestHeaders);
			
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
			if(response.getStatusCodeValue() != 200){
				return "薪酬工程响应失败！导致薪酬数据插入失败！";
			}
			return "OK";
		} catch (Exception e) {
			logger.error("insertErpTalkSalary方法出现异常："+e.getMessage(),e);
			return "方法出现异常！导致薪酬数据插入失败！";
		}
	}
	
	/**
	 * Description: 邮件正文
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月20日 下午19:58:22
	 */
	public String getTextForSendEmail(Map<String,Object> offerDetail) {
		/*
		 * 获取发offer邮件需要的信息
		 */
		Boolean isTrainee = Boolean.valueOf(String.valueOf(offerDetail.get("isTrainee")));//是否是实习生
		String name = String.valueOf(offerDetail.get("name"));//姓名
		String firstDepartment = String.valueOf(offerDetail.get("firstDepartment"));//一级部门
		String secondDepartment = String.valueOf(offerDetail.get("secondDepartment"));//二级部门
		String text = null;
		text = "<div id=\"write-custom-write\" tabindex=\"0\" style=\"font-size: 14px; font-family: 宋体; outline: none;\">\n" + 
				"    <p>\n" + 
				"        <br/>\n" + 
				"    </p>\n" + 
				"</div>\n" + 
				"<div tabindex=\"0\" style=\"font-size: 14px; font-family: 宋体; outline: none;\">\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        &nbsp;HR：\n" +
				"    </p>\n" + 
				"</div>\n" + 
				"<div tabindex=\"0\" style=\"font-size: 14px; font-family: 宋体; outline: none;\">\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        &nbsp; &nbsp; 您好，附件中是"+firstDepartment+secondDepartment+name+"的个人信息及职业测评，烦请帮忙发出";
				if(isTrainee) {
					text += "实习邀请";
				}else {
					text += "offer";
				}
				text +="，谢谢。\n" + 
				"    </p>\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        &nbsp; &nbsp;&nbsp;\n" + 
				"    </p>\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        &nbsp; &nbsp;&nbsp;\n" + 
				"    </p>\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        &nbsp; &nbsp;\n" + 
				"    </p>\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        <br/>\n" + 
				"    </p>\n" + 
				"</div>\n" + 
				"<div>\n" + 
				"    <p style=\"margin:0px;\">\n" + 
				"        &nbsp;<span style=\"text-decoration: underline;\">&nbsp;Best wishes!&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>\n" + 
				"    </p>\n" + 
				"    <table class=\"customTableClassName\" style=\"border-collapse: collapse;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" + 
				"        <tbody>\n" + 
				"            <tr class=\"firstRow\">\n" + 
				"                <td width=\"313\" valign=\"top\" style=\"padding: 0cm 5.4pt; border: rgb(240, 240, 240); width: 235.05pt; background-color: transparent; word-break: break-all;\">\n" + 
				"                    <p style=\"margin: 0px;\">\n" + 
				"                        <span style=\"color: black; font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 14px;\">北京南天软件有限公司</span><span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 14px;\"><span style=\"color: black; font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 9pt;\"></span><span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 9pt;\">&nbsp;</span></span>\n" + 
				"                    </p>\n" + 
				"                </td>\n" + 
				"            </tr>\n" + 
				"            <tr>\n" + 
				"                <td width=\"313\" valign=\"top\" style=\"padding: 0cm 5.4pt; border: rgb(240, 240, 240); width: 235.05pt; word-break: break-all; background-color: transparent;\">\n" + 
				"                    <p style=\"margin: 0px;\">\n" + 
				"                        <span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 9pt;\">姓名：王秋掬</span>\n" + 
				"                    </p>\n" + 
				"                    <p style=\"margin: 0px;\">\n" + 
				"                        <span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 9pt;\">电话：51515925<span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; border-bottom: 1px dashed rgb(204, 204, 204); z-index: 1; font-size: 14px;\" t=\"7\" nclick=\"return false;\" data=\"18210527756\">‍</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>\n" + 
				"                    </p>\n" + 
				"                </td>\n" + 
				"            </tr>\n" + 
				"            <tr>\n" + 
				"                <td width=\"313\" valign=\"top\" style=\"padding: 0cm 5.4pt; border: rgb(240, 240, 240); width: 235.05pt; background-color: transparent; word-break: break-all;\">\n" + 
				"                    <p style=\"margin: 0px;\">\n" + 
				"                        <span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 9pt;\">地址：中国北京海淀区上地信息路10号</span>\n" + 
				"                    </p>\n" + 
				"                    <p style=\"margin: 0px;\">\n" + 
				"                        <span style=\"font-family: 微软雅黑, &quot;Microsoft YaHei&quot;; font-size: 9pt;\">邮箱：<a href=\"mailto:wanghui@nantian.com.cn\" target=\"_blank\">wangqiuju@nant\n" + 
				"                        <wbr/>ian.com.cn</a></span>\n" + 
				"                    </p>\n" + 
				"                </td>\n" + 
				"            </tr>\n" + 
				"        </tbody>\n" + 
				"    </table>\n" + 
				"</div>";
		return text;
	}
	
	/**
	 * Description: 邮件正文
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月21日 下午15:52:03
	 */
	public String getSubjectForSendEmail(Map<String,Object> offerDetail) {
		/*
		 * 获取发offer邮件需要的信息
		 */
		Boolean isTrainee = Boolean.valueOf(String.valueOf(offerDetail.get("isTrainee")));//是否是实习生
		String name = String.valueOf(offerDetail.get("name"));//姓名
		String firstDepartment = String.valueOf(offerDetail.get("firstDepartment"));//一级部门
		String secondDepartment = String.valueOf(offerDetail.get("secondDepartment"));//二级部门
		
		String subject = null;
		if(isTrainee) {
			subject = firstDepartment+"实习协议"+"——"+secondDepartment+"——"+name;
		}else {
			subject = "offer"+"--"+firstDepartment+"--"+secondDepartment+"--"+name;
		}
		return subject;
	}
		
	/**
	 * Description: 查询待审批的offer列表
	 *
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年2月18日 下午20:52:13
	 */
	public RestResponse approveOfferQueryList(String token) {
		logger.info("进入approveOfferQueryList方法");
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isValid", false);
			paramsMap.put("status", DicConstants.INTERVIEW_STATUS_OFFER_APPROVE);
			
			//根据角色过滤数据
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();// 角色列表

			if (roles.contains(8) || roles.contains(1)) {// 总裁、hr
				// 查询所有的员工
			} else if (roles.contains(9)||roles.contains(2)) { // 副总裁
				paramsMap.put("personId", id);
			} else {
				return RestUtils.returnSuccessWithString("无可查看的offer");
			}
			
			List<Map<String, Object>> interviewList = resumePostMapper.findResumePostInfoByParams(paramsMap);
			//通过面试ID查询复试信息，获取员工入职时间
			for(Map<String, Object> interview : interviewList) {
				Integer interviewId = (Integer) interview.get("interviewId");
				ErpResumePostReexam resumePostReexam = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
				interview.put("entryTime", resumePostReexam.getEntryTime());
			}
			return RestUtils.returnSuccess(interviewList);
		} catch (Exception e) {
			logger.error("approveOfferQueryList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 查询待审批的offer列表
	 * 
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年3月13日
	 */
	public RestResponse approveOfferQueryListForLook(String token) {
		logger.info("进入approveOfferQueryListForLook方法");
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isValid", false);
			paramsMap.put("status", DicConstants.INTERVIEW_STATUS_OFFER_APPROVE);
			
			//根据角色过滤数据
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();// 角色列表

			if (roles.contains(8) || roles.contains(1)) {// 总裁、hr
				// 查询所有的员工
			} else if (roles.contains(9)) { // 副总裁
				paramsMap.put("superLeaderId", id);
			} else if (roles.contains(2)) { // 一级部门经理
				paramsMap.put("leaderId", id);
			} else {
				return RestUtils.returnSuccessWithString("无可查看的offer");
			}
			
			List<Map<String, Object>> interviewList = resumePostMapper.findResumePostInfoByParams(paramsMap);
			//通过面试ID查询复试信息，获取员工入职时间
			for(Map<String, Object> interview : interviewList) {
				Integer interviewId = (Integer) interview.get("interviewId");
				ErpResumePostReexam resumePostReexam = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
				interview.put("entryTime", resumePostReexam.getEntryTime());
			}
			return RestUtils.returnSuccess(interviewList);
		} catch (Exception e) {
			logger.error("approveOfferQueryListForLook方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	public RestResponse offerApprove(String token,Map<String,Object> params) {
		logger.info("offerApprove方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			String name = String.valueOf(params.get("name"));//应聘者姓名
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));
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
			
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			List<Integer> roles = erpUser.getRoles();//当前登录人的角色
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			
			/*
			 * 将薪酬数据加密
			 */
			Map<String,String> salaryData = new HashMap<>();
			salaryData.put("monthIncome", monthIncome);
			salaryData.put("socialSecurityBase", socialSecurityBase);
			salaryData.put("accumulationFundBase", accumulationFundBase);
			Map<String,String> encryptedSalaryData = erpInterviewService.encryptSalaryDataAes(salaryData);
			
			String context = "";
			
			//比较offer数据是否有修改
			ErpResumePostReexam offerReexam = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
			if (offerReexam != null){
				if(!offerReexam.getEntryTime().equals(entryTime)){
					context = context+"入职时间、";
				}
				if(!offerReexam.getMonthIncome().equals(encryptedSalaryData.get("monthIncome"))){
					context = context+"月度收入、";
				}
				if(!offerReexam.getSocialSecurityBase().equals(encryptedSalaryData.get("socialSecurityBase"))){
					context = context+"社保基数、";
				}
				if(!offerReexam.getAccumulationFundBase().equals(encryptedSalaryData.get("accumulationFundBase"))){
					context = context+"公积金基数、";
				}
				if(!offerReexam.getPosition().equals(position)){
					context = context+"职位职级、";
				}
				if(!offerReexam.getSocialSecurityPlace().equals(socialSecurityPlace)){
					context = context+"社保地、";
				}
				if(!offerReexam.getProbationPeriod().equals(probationPeriod)){
					context = context+"试用期期限、";
				}
				if(!offerReexam.getContractPeriod().equals(contractPeriod)){
					context = context+"合同期限、";
				}
				if(!offerReexam.getRemark().equals(remark)){
					context = context+"备注";
				}
			}
			
			if (!context.equals("")){
				context = ", 修改："+context;
			}
			
			/*
			 * 保存面试记录表中
			 */
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("offer审批通过"+context);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			//在岗位记录表中插入记录
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("offer审批通过："+name); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(username);//当前处理人Id
    		operRec.setPostId(postId);
    		operRecordMapper.addPositionOperReord(operRec);
			
			//add by ZhangYuWei 20190604  6级以上的offer，除上级领导外，还需要总裁审批。 start
			if((roles.contains(9)||roles.contains(2)) && rank>5){//9是副总裁角色
				//通过权限工程查询总裁的员工ID
				List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
				Map<String,Object> map = list.get(0);
				Integer nextPerson = (Integer) map.get("userId");//注意：userId是员工ID
				Map<String, Object> resumePostInfoMap = this.resumePostMapper.findResumePostInfoById(interviewId);
				if (!nextPerson.equals(Integer.valueOf(String.valueOf(resumePostInfoMap.get("personId"))))){
					/*
					 * 将素质模型打分、员工基本信息、薪资信息，组装成PO对象
					 */
					ErpResumePostReexam resumePostReexam = new ErpResumePostReexam();
					resumePostReexam.setInterviewId(interviewId);
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
					/*
					 * 根据复试信息表中是否已存在记录，决定是复试信息表数据的新增或更新
					 */
					ErpResumePostReexam isExist = resumePostReexamMapper.selectResumePostReexamDatail(interviewId);
					if(isExist==null) {
						resumePostReexamMapper.insertResumePostReexam(resumePostReexam);
					}else {
						resumePostReexamMapper.updateResumePostReexam(resumePostReexam);
					}

					//待总裁offer审批
					ErpResumePost resumePost = new ErpResumePost();
					resumePost.setId(interviewId);
					resumePost.setPersonId(nextPerson);
					resumePostMapper.updateResumePost(resumePost);
					return RestUtils.returnSuccessWithString("OK");
				}
			}
			//add by ZhangYuWei 20190604  6级以上的offer，除上级领导外，还需要总裁审批。end
			
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
			String resultStr = erpInterviewService.insertErpTalkSalary(salaryMap);
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
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("offerApprove方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	public RestResponse offerDeny(String token,Map<String,Object> params) {
		logger.info("offerDeny方法开始执行，参数是：token="+token+","+params);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
			Integer interviewId = Integer.valueOf(String.valueOf(params.get("interviewId")));//面试编号
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));
			String context = String.valueOf(params.get("context"));
			String name = String.valueOf(params.get("name"));
			
			/*
			 * 面试流程结束，修改面试状态为“面试结束”，将流程状态置为失效
			 */
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setId(interviewId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_NO_PASS);
			resumePost.setIsValid(true);
			resumePostMapper.updateResumePost(resumePost);
			
			/*
			 * 修改简历的状态为“不通过”
			 */
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setStatus(DicConstants.RESUME_STATUS_NO_PASS);
			resumeMapper.updateResume(resume);
							
			/*
			 * 保存面试记录表中
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			String username = erpUser.getEmployeeName();//从用户信息中获取用户名
			ErpRecord record = new ErpRecord();
			record.setProcessor(username);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("offer审批不通过："+context);
			record.setResumeId(resumeId);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			//在岗位记录表中插入记录
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext("offer审批不通过:"+name+", 原因："+context); //处理内容
			operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
			operRec.setCurrentPersonName(username);//当前处理人Id
			operRec.setPostId(postId);
			operRecordMapper.addPositionOperReord(operRec);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("offerDeny方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 邮件抄送人（岗位申请人、一级部门经理）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月20日 下午14:58:57
	 * @Modify Date: 2020-03-30 bug756中,不要抄送给一级部门经理
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getBccForSendEmail(Integer postId,String token) {
		Map<String, Object> postMap = postMapper.findByPostId(postId);
		Integer proposerIdForPost = Integer.valueOf(String.valueOf(postMap.get("proposerId")));//岗位申请人员工ID
		//Integer proposerIdForManager = Integer.valueOf(String.valueOf(postMap.get("userId")));//一级部门经理员工ID
		
		//调用ERP-权限 工程 的操作层服务接口-查询用户的邮箱
		String url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpId?empId="+proposerIdForPost;
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token", token);
	    HttpEntity<String> requestEntity=new HttpEntity<String>(null,requestHeaders);
	    ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Map.class);
	    logger.info("查询岗位申请人时，跨工程调用的响应结果response="+response);
	    Map<String,Object> resultMap = response.getBody();
	    /*
	     * 跨工程调用未获取到响应
	     */
	    String proposerEmailForPost = "";
	    //String proposerEmailForManager = "";
	    if(response.getStatusCodeValue() != 200 || "".equals(String.valueOf(resultMap.get("data")))){
	    	logger.error("未查询到岗位申请人的邮箱，员工Id为："+proposerIdForPost);
	    }else {
	    	/*
		     * 通过用户ID未获取到用户信息
		     */
		    Map<String,Object> userMap = (Map<String, Object>) resultMap.get("data");
		    if(userMap==null || userMap.get("username")==null){
		    	return "error";
		    }
		    proposerEmailForPost = String.valueOf(userMap.get("username"));//获取用户（岗位申请人）邮箱
	    }
	    
//	    //调用ERP-权限 工程 的操作层服务接口-查询用户的邮箱(2020-03-30 bug756中不要抄送给一级部门经理)
//		String url1 = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpId?empId="+proposerIdForManager;
//		HttpHeaders requestHeaders1=new HttpHeaders();
//		requestHeaders1.add("token", token);
//		HttpEntity<String> requestEntity1=new HttpEntity<String>(null,requestHeaders1);
//		ResponseEntity<Map> response1 = restTemplate.exchange(url1,HttpMethod.GET,requestEntity1,Map.class);
//		logger.info("查询一级部门经理时，跨工程调用的响应结果response1="+response1);
//		Map<String,Object> resultMap1 = response1.getBody();
//		/*
//		 * 跨工程调用未获取到响应
//		 */
//		if(response1.getStatusCodeValue() != 200 || "".equals(String.valueOf(resultMap1.get("data")))){
//			logger.error("未查询到一级部门经理的邮箱，员工Id为："+proposerIdForManager);
//		}else {
//			/*
//			 * 通过用户ID未获取到用户信息
//			 */
//			Map<String,Object> userMap1 = (Map<String, Object>) resultMap1.get("data");
//			if(userMap1==null || userMap1.get("username")==null){
//				return "error";
//			}
//			proposerEmailForManager = String.valueOf(userMap1.get("username"));//获取用户（岗位申请人）邮箱
//		}
//
//	    return proposerEmailForPost+","+proposerEmailForManager;
		return proposerEmailForPost;
	}
	
	
	/**
	 * Description: 将实习生的薪酬数据，跨工程更新到薪酬数据库中
	 *
	 * @return
	 * @Author songxiugong
	 * @Create Date: 2019年09月17日 下午15:00:52
	 */
	@SuppressWarnings("rawtypes")
	public String updateErpTalkSalary(Map<String,Object> salaryMap) {
		logger.info("updateErpTalkSalary方法开始执行，参数是："+salaryMap);
		try {
			//调用薪酬工程，将月度收入、社保基数、公积金基数更新到薪酬的数据库表中
			String url = protocolType+"nantian-erp-salary/nantian-erp/salary/talkSalary/updateErpTalkSalary";
			Map<String,Object> erpTalkSalary = new HashMap<>();
			erpTalkSalary.put("baseWage", salaryMap.get("baseWage"));
			erpTalkSalary.put("monthAllowance", salaryMap.get("monthAllowance"));
			erpTalkSalary.put("offerId", salaryMap.get("offerId"));
			erpTalkSalary.put("remark", salaryMap.get("salaryRemark"));
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",salaryMap.get("token").toString());//将token放到请求头中
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(erpTalkSalary, requestHeaders);
			
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
			if(response.getStatusCodeValue() != 200){
				return "薪酬工程响应失败！导致薪酬数据插入失败！";
			}
			return "OK";
		} catch (Exception e) {
			logger.error("insertErpTalkSalary方法出现异常："+e.getMessage(),e);
			return "方法出现异常！导致薪酬数据插入失败！";
		}
	}

    public Map<String, Object> findAllOffer(String token) {
		logger.info("findAllOffer方法开始执行，传递参数：token:"+token);

		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		List<Map<String,Object>> tempList=new ArrayList<Map<String,Object>>();
		Map<String, Object> queryHashMap = new HashMap<String, Object>();
//	    Integer roleId=null;

		try {
			List<Integer> roles=erpUser.getRoles();	//当前登录人角色列表
			Map<String, Object> queryMap=new HashMap<String, Object>();
			if(roles.contains(8) || roles.contains(1)){//总经理和hr可以看到所有部门的待入职
				queryMap.put("managerId", erpUser.getUserId());
			}else if(roles.contains(9)){	//副总经理
				queryMap.put("superLeaderId", erpUser.getUserId());
			}
			else if(roles.contains(2)){//一级部门经理角色
				queryMap.put("leaderId", erpUser.getUserId());
			}
			else if(roles.contains(5)){//二级部门经理角色
				queryMap.put("secondLeaderId", erpUser.getUserId());
			}
			else{
				queryHashMap.put("allWaitEntry", tempList);
				return queryHashMap;
			}

			tempList=this.offerMapper.findAllOffer(queryMap);  //所有OfferId列表
			queryHashMap.put("allWaitEntry", tempList);
		} catch (Exception e) {

			logger.info("findAllOffer方法出现异常：" + e.getMessage(),e);
		}
		return queryHashMap;
    }

	/**
	 * 获取邮件用户名密码
	 * @return
	 */
	public Map<String, Object> getEmailUserNameAndPassword() {
		logger.info("getEmailUserNameAndPassword方法开始执行");
		Map<String, Object> returnHashMap = new HashMap<String, Object>();

		try {
			Map<String, Object> queryHashMap = new HashMap<String, Object>();
			queryHashMap.put("JOB_CATEGORY", DicConstants.EMAIL_USERNAME);
			queryHashMap.put("dicCode", '0');
			String emailUserName = adminDicMapper.findJobCategoryName(queryHashMap);
			queryHashMap.put("JOB_CATEGORY", DicConstants.EMAIL_PASSWORD);
			String emailPassword = adminDicMapper.findJobCategoryName(queryHashMap);
			returnHashMap.put("userName", emailUserName);
			returnHashMap.put("password", emailPassword);

		} catch (Exception e) {
			logger.info("getEmailUserNameAndPassword方法出现异常：" + e.getMessage(),e);
		}
		return returnHashMap;
    }
}
