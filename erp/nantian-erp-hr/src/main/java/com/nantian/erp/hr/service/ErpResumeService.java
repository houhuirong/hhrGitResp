package com.nantian.erp.hr.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import com.nantian.erp.hr.data.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.HttpClientUtil;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.model.ErpOffer;
import com.nantian.erp.hr.data.model.ErpRecord;
import com.nantian.erp.hr.data.model.ErpResume;
import com.nantian.erp.hr.data.model.ErpResumePost;
import com.nantian.erp.hr.util.FileUtils;

/** 
 * Description: 简历service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月05日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
//@PropertySource("classpath:config/sftp.properties")
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties"},ignoreResourceNotFound = true)
public class ErpResumeService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/*
	 * 从配置文件中获取SFTP相关属性
	 */
    @Value("${sftp.basePath}")
    private String basePath;//服务器基本路径
    @Value("${sftp.resumePath}")
    private String resumePath;//简历路径
    /*
	 * 从配置文件中获取Email相关属性
	 */
    @Value("${email.service.host}")
	private String emailServiceHost;//邮件服务的IP地址和端口号
	
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private ErpRecordMapper recordMapper;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private FileUtils fileUtils;
	
	@Autowired
	private ErpResumePostMapper erpResumePostMapper;

	@Autowired
	private ErpOfferMapper erpOfferMapper;

	@Autowired
	private ErpEmployeeEntryMapper erpEmployeeEntryMapper;
	/**
	 * Description: 数据库新增一条简历信息，如果简历附件不为空的话，上传附件到服务器上
	 *
	 * @param resume
	 * @param file
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午14:11:57
	 * @Update Date: 2018年11月05日 下午13:32:59
	 */
	public RestResponse insertResume(String token,ErpResume resume, MultipartFile file) {
		logger.info("进入insertResume方法，参数是："+resume.toString());
		try {
			/*
			 * 上传简历附件。如果成功，则获取到文件的名称，赋值给PO
			 */
			/* 文件的路径  */
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH)+1;
			int date = calendar.get(Calendar.DATE);
			//通过时间，在服务器动态产生路径，来保存文件
			String datePath = "/"+year+"/"+month+"/"+date+"/";
			Map<String,Object> resultMap = fileUtils.uploadFileBySFTP(file, resumePath+datePath);
			Boolean isSuccess = (Boolean) resultMap.get("isSuccess");
			if(isSuccess) {
				resume.setFileName(datePath + (String) resultMap.get("data"));
			}
			/*
			 * 简历信息入库
			 */
			ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
			Integer createPersonId = erpUser.getUserId();
			resume.setIsValid(true);
			resume.setCreatePersonId(createPersonId);//简历创建人
			resume.setStatus(DicConstants.RESUME_STATUS_RECOMMENDED);
			this.resumeMapper.insertResume(resume);
			
			//新增一条简历记录信息
			
			//通过token从缓存中获取用户名
			String employeeName = erpUser.getEmployeeName();
			
			ErpRecord record = new ErpRecord();
			record.setResumeId(resume.getResumeId());
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("新建简历");
			record.setProcessor(employeeName);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("insertResume方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("新建简历出现异常！");
		}
	}
	
	/**
	 * Description: 修改简历信息，如果附件不为空，则修改简历对应的附件文件
	 *
	 * @param resume
	 * @param file
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午14:35:36
	 * @Update Date: 2018年11月05日 下午14:55:06
	 */
	public RestResponse updateResume(String token,ErpResume resume, MultipartFile file) {
		logger.info("进入updateResume方法，参数是："+resume.toString());
		try {
			Map<String,Object> returnMessage = new HashMap<String, Object>();
			Map<String,Object> params=new HashMap<>();
			params.put("phone", resume.getPhone());
			ErpResume validResult = resumeMapper.validPhoneAndEmail(params);
			if(validResult!=null) {
				//数据库已存在该手机号
				if(!validResult.getResumeId().equals(resume.getResumeId())){
				returnMessage.put("Message", "数据库已存在手机号:"+resume.getPhone());
				return RestUtils.returnSuccess(returnMessage, "Alert");
				}
			}
			Map<String,Object> paramsEmail=new HashMap<>();
			paramsEmail.put("email", resume.getEmail());
			ErpResume emailValidResult = resumeMapper.validPhoneAndEmail(paramsEmail);
			if(emailValidResult!=null) {
				//数据库已存在该邮箱
				if(!emailValidResult.getResumeId().equals(resume.getResumeId())){
					returnMessage.put("Message", "数据库已存在邮箱:"+resume.getEmail());
					return RestUtils.returnSuccess(returnMessage, "Alert");
				}
			}
			/*
			 * 上传简历附件。如果成功，则获取到文件的名称，赋值给PO
			 */
			/* 文件的路径  */
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH)+1;
			int date = calendar.get(Calendar.DATE);
			//通过时间，在服务器动态产生路径，来保存文件
			String datePath = "/"+year+"/"+month+"/"+date+"/";
			Map<String,Object> resultMap = fileUtils.uploadFileBySFTP(file, resumePath+datePath);
			Boolean isSuccess = (Boolean) resultMap.get("isSuccess");
			if(isSuccess) {
				//新文件上传成功后，如果原文件不为空，则删除原文件
				if(resume.getFileName()!=null && !"".equals(resume.getFileName())) {
					fileUtils.deleteFileBySFTP(resumePath+resume.getFileName());
				}
				resume.setFileName(datePath + (String) resultMap.get("data"));
			}
			
			/*
			 * 修改数据库中的简历信息
			 */
			this.resumeMapper.updateResume(resume);
			
			//新增一条简历记录信息
			
			//通过token从缓存中获取用户名
			ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = erpUser.getEmployeeName();
			
			ErpRecord record = new ErpRecord();
			record.setResumeId(resume.getResumeId());
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("修改简历");
			record.setProcessor(employeeName);
			recordMapper.insertRecord(record);
			record.setProcessorId(erpUser.getUserId());
			returnMessage.put("Message", "修改简历成功!");
			return RestUtils.returnSuccess(returnMessage, "OK");
		} catch (Exception e) {
			logger.error("updateResume方法出现异常：" + e.getMessage(),e);
			Map<String,Object> returnMessage = new HashMap<String, Object>();
			returnMessage.put("Message", "修改简历出现异常!");
			return RestUtils.returnFailure(returnMessage, "Error");
		}
	}
	
	/**
	 * Description: 查询有效简历
	 * 
	 * @param isTrainee 是否是实习生
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午14:56:54
	 * @Update Date: 2018年11月05日 下午15:37:38
	 */
	public RestResponse queryValidResume(Boolean isTrainee) {
		logger.info("进入queryValidResume方法，参数是：isTrainee="+isTrainee);
		List<Map<String,Object>> resultList = new ArrayList<>();
		try {
			Map<String,Object> paramsMap = new HashMap<>();
			paramsMap.put("isTrainee", isTrainee);
			resultList = resumeMapper.findValidResume(paramsMap);
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.error("queryValidResume方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致没有查询出简历信息！");
		}
	}
	
	/**
	 * Description: 查询归档简历
	 *
	 * @param isTrainee 是否是实习生
	 * @param page 页码
	 * @param rows 行数
	 * @param keyword 关键字
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午15:14:17
	 * @Update Date: 2018年11月05日 下午16:03:11
	 */
	public RestResponse queryArchivedResume(Boolean isTrainee,Integer page, Integer rows, String keyword) {
		logger.info("进入queryArchivedResume方法，参数是：isTrainee="+isTrainee+",page="+page+",rows="+rows+",keyword="+keyword);
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> paramsMap = new HashMap<>();
			paramsMap.put("limit", rows);
			paramsMap.put("offset", rows*(page-1));
			paramsMap.put("isTrainee", isTrainee);
			if(keyword!=null && !"".equals(keyword)) {
				paramsMap.put("keyword", "%"+keyword+"%");
			}
			List<Map<String,Object>> list = resumeMapper.findArchivedResume(paramsMap);
			resultMap.put("list", list);
			Long totalCount = resumeMapper.findTotalCountOfArchivedResume(paramsMap);
			resultMap.put("total", totalCount);
			return RestUtils.returnSuccess(resultMap);
		} catch (Exception e) {
			logger.error("queryArchivedResume方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致没有查询出简历信息！");
		}
	}
	
	/**
	 * Description: 查询状态为“可推荐”的简历列表
	 * 
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月14日 下午11:27:34
	 */
	public RestResponse queryRecommendedResume(Boolean isTrainee) {
		logger.info("进入queryRecommendedResume方法，参数是："+isTrainee);
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			Map<String,Object> params = new HashMap<>();
			params.put("status", "0");
			params.put("isTrainee", isTrainee);
			resultList = this.resumeMapper.findValidResume(params);
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.error("queryRecommendedResume方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致没有查询出简历信息！");
		}
	}

	/**
	 * Description: 使简历失效
	 * 
	 * @param resumeId 简历ID
	 * @param reason 失效原因
	 * @param token 用户登录的token
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午15:37:02
	 * @Update Date: 2018年11月05日 下午17:16:24
	 * @Update Date: 2020年1月10日 10:29:44 增加如果有面试记录与offer记录将其置为失效
	 */
	@Transactional(rollbackFor = Exception.class)
	public String invaildResume(Integer resumeId, String reason, String token) throws Exception{
		logger.info("进入invaildResume方法，参数是：resumeId="+resumeId+",reason="+reason+",token="+token);
		//通过token从缓存中获取用户名
		ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
		String employeeName = erpUser.getEmployeeName();
		
		//修改简历状态为“失效”
		ErpResume resume = new ErpResume();
		resume.setResumeId(resumeId);
		resume.setIsValid(false);
		resumeMapper.updateResume(resume);
		
		//新增一条简历失效记录信息
		ErpRecord record = new ErpRecord();
		record.setResumeId(resumeId);
		record.setTime(ExDateUtils.getCurrentStringDateTime());
		record.setContent("简历失效："+reason);
		record.setProcessor(employeeName);
		record.setProcessorId(erpUser.getUserId());
		recordMapper.insertRecord(record);
		
		//查询该简历的面试记录
		List<Integer> resumePostIds = erpResumePostMapper.findIdsByResumeId(resumeId);
		if(resumePostIds != null && resumePostIds.size() > 0) {
			
			//修改面试状态为失效
			erpResumePostMapper.updateValidFalseByIds(resumePostIds);
			
			//新增一条简历失效记录信息
			ErpRecord resumePostRecord = new ErpRecord();
			resumePostRecord.setResumeId(resumeId);
			resumePostRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			resumePostRecord.setContent("面试流程失效："+reason);
			resumePostRecord.setProcessor(employeeName);
			resumePostRecord.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(resumePostRecord);
			
			//根据面试id列表查询该简历的offer记录id列表
			List<Integer> offerIds = erpOfferMapper.findIdsByResumePostIds(resumePostIds);

			if(offerIds != null && offerIds.size() > 0){
				//根据offerid列表修改offer为 3：未发offer归档
				erpOfferMapper.updateValidFalseByOfferIds(offerIds);

				//新增一条简历失效记录信息
				ErpRecord offerRecord = new ErpRecord();
				offerRecord.setResumeId(resumeId);
				offerRecord.setTime(ExDateUtils.getCurrentStringDateTime());
				offerRecord.setContent("未发offer归档："+reason);
				offerRecord.setProcessor(employeeName);
				offerRecord.setProcessorId(erpUser.getUserId());
				recordMapper.insertRecord(offerRecord);

				//入职
				List<Integer> entryIds = erpEmployeeEntryMapper.findIdsByOfferIds(offerIds);
				erpEmployeeEntryMapper.updateValidFalseByEntryIds(entryIds);


			}
		}
		
		return "OK";
	}

	/**
	 * Description: 使简历生效
	 * 
	 * @param resumeId 简历ID
	 * @param reason 生效原因
	 * @param token 用户登录的token
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午15:59:38
	 * @Update Date: 2018年11月05日 下午17:29:53
	 */
	public String vaildResume(Integer resumeId, String reason, String token) {
		logger.info("进入vaildResume方法，参数是：resumeId="+resumeId+",reason="+reason+",token="+token);
		try {
			//通过token从缓存中获取用户名
			ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = erpUser.getEmployeeName();
			
			//修改简历状态为“生效”，并修改简历状态为“可推荐”
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setIsValid(true);
			resume.setStatus(DicConstants.RESUME_STATUS_RECOMMENDED);
			resumeMapper.updateResume(resume);
			
			//新增一条简历生效记录信息
			ErpRecord record = new ErpRecord();
			record.setResumeId(resumeId);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent(reason);
			record.setProcessor(employeeName);
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			return "OK";
		} catch (Exception e) {
			logger.error("vaildResume方法出现异常：" + e.getMessage(),e);
			return "方法出现异常！导致没有使简历生效！";
		}
	}
	
	/**
	 * Description: 根据简历ID查询面试记录
	 * 
	 * @param resumeId 简历Id
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午16:33:49
	 * @Update Date: 2018年11月05日 下午18:12:01
	 */
	public RestResponse findInterviewRecord(Integer resumeId) {
		logger.info("进入findInterviewRecord方法，参数是：resumeId="+resumeId);
		List<ErpRecord> interviewRecordList = new ArrayList<>();
		try {
			interviewRecordList = recordMapper.selectRecordById(resumeId);
			return RestUtils.returnSuccess(interviewRecordList);
		} catch (Exception e) {
			logger.error("findInterviewRecord方法出现异常！"+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致没有查询出面试记录！");
		}
	}

	/**
	 * Description: 根据简历ID查询简历详细信息
	 * 
	 * @param resumeId 简历Id
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午16:56:08
	 * @Update Date: 2018年11月05日 下午18:17:31
	 */
	public RestResponse findResumeDetail(Integer resumeId) {
		logger.info("进入findResumeDetail方法，参数是：resumeId="+resumeId);
		Map<String, Object> resumeInfo = new HashMap<>();
		try {
			resumeInfo = resumeMapper.selectResumeDetail(resumeId);
			return RestUtils.returnSuccess(resumeInfo);
		} catch (Exception e) {
			logger.error("findResumeDetail方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致没有查询出简历的详细信息！");
		}
	}

	/**
	 * Description: 根据简历ID下载简历
	 * 
	 * @param resumeId 简历ID
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月19日 下午17:32:29
	 * @Update Date: 2018年11月05日 下午18:54:17
	 */
	public RestResponse downloadResume(Integer resumeId) {
		logger.info("进入downloadResume方法，参数是：resumeId="+resumeId);
	    try {
	    	Map<String, Object> erpResume = resumeMapper.selectResumeDetail(resumeId);
			if(erpResume==null || erpResume.get("fileName")==null || "".equals(erpResume.get("fileName").toString())) {
				return RestUtils.returnFailure("该简历没有附件！");
			}
			String datePathAndFileName = erpResume.get("fileName").toString();
	    	fileUtils.downloadFileBySFTP(resumePath+datePathAndFileName,"application/octet-stream");
	    	return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("downloadResume方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致下载简历失败！");
		}
	}
	
	/**
	 * Description: 根据简历ID预览简历（通过依赖包中的方法）
	 * 
	 * @param resumeId 简历Id
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月16日 上午10:59:09
	 */
	@SuppressWarnings("unchecked")
	public RestResponse previewResume(String token,Integer resumeId) {
		logger.info("进入previewResume方法，参数是：resumeId="+resumeId);
		List<String> wordType = new ArrayList<>();//word类型
		wordType.add("doc");
		wordType.add("docx");
		List<String> htmlType = new ArrayList<>();//html类型
		htmlType.add("html");
		htmlType.add("htm");
		htmlType.add("mht");
		htmlType.add("mhtml");
    	try {
    		/*
    		 * 通过简历ID查询附件的路径。如果没有附件，直接返回；如果有附件，就获取文件名、文件后缀
    		 */
			Map<String, Object> erpResume = resumeMapper.selectResumeDetail(resumeId);
			if(erpResume==null || !erpResume.containsKey("fileName") || erpResume.get("fileName")==null) {
				HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
				response.setContentType("application/json");
				response.setHeader("message", "该简历没有附件！");
				return RestUtils.returnSuccess("该简历没有附件！");
			}
			String datePathAndFileName = erpResume.get("fileName").toString();
			String houZhui = datePathAndFileName.substring(datePathAndFileName.lastIndexOf(".")+1);
			logger.info("datePathAndFileName="+datePathAndFileName+",houZhui="+houZhui);
			
			/*
			 * 文件不是word格式，则不需要转换为html文件，直接把流文件返回给前端
			 */
			if(!wordType.contains(houZhui)){
				if("pdf".equals(houZhui)){//pdf格式
					fileUtils.downloadFileBySFTP(resumePath+datePathAndFileName,"application/octet-stream");
				}else if(htmlType.contains(houZhui)){//html格式
					fileUtils.downloadFileBySFTP(resumePath+datePathAndFileName,"text/html");
				}else{//文本格式或者其他格式
					fileUtils.downloadFileBySFTP(resumePath+datePathAndFileName,"application/json");
				}
			}
			
			/*
			 * 文件是word格式
			 */
			String htmlDatePathAndName = datePathAndFileName.substring(0, datePathAndFileName.lastIndexOf("."))+".html";//html日期路径+文件名
			String cacheRedis = stringRedisTemplate.opsForValue().get(htmlDatePathAndName);
			logger.info("htmlDatePathAndName="+htmlDatePathAndName+",cacheRedis="+cacheRedis);
			/*
			 * 如果redis中有该html文件，则直接访问服务器上的html文件，无须再进行转换
			 */
			if(cacheRedis != null){
				fileUtils.downloadFileBySFTP(resumePath+htmlDatePathAndName,"text/html");
			}
			/*
			 * 如果redis中没有该文件，则转换为html文件存储到服务器上，并将路径加入redis
			 */
//			RestResponse wordResult = this.downloadFileInSFTP(datePathAndFileName);
//			if(!"200".equals(wordResult.getStatus())){
//				return RestUtils.returnFailure("该word文件在服务器已丢失！");
//			}
			String wordPathAndName = basePath+resumePath+datePathAndFileName; //word完整路径+文件名
			String htmlPathAndName = basePath+resumePath+htmlDatePathAndName; //html完整路径+文件名
			logger.info("wordPathAndName="+wordPathAndName+",htmlPathAndName="+htmlPathAndName);
			/*
			 * 调用远程服务，执行html转换,html文件路径和名字(和下载的word在一个目录下)
			 */
			Map<String,String> fileParams = new HashMap<>();
			fileParams.put("wordPathAndName", wordPathAndName);
			fileParams.put("htmlPathAndName", htmlPathAndName);
			String url = emailServiceHost+"/nantian-erp/format/convert/wordToHtml";
			Map<String,String> headers = new HashMap<>();
			headers.put("token", token);
			Map<String, String> response = HttpClientUtil.executePostMethodWithParas(url, JSON.toJSONString(fileParams), headers, "application/json", 30000);
			logger.info("code="+response.get("code")+"，result="+response.get("result"));
			if(!"200".equals(response.get("code"))) {
				return RestUtils.returnSuccessWithString("word转html失败！");
			}
			Object result = JSON.parse(response.get("result"));
			Map<String,Object> resultMap = (Map<String,Object>) result;
			if(!"200".equals(resultMap.get("status"))) {
				return RestUtils.returnSuccessWithString("word转html失败！");
			}
			
		    /*
		     * 获取转换后的html文件
		     */
//		    RestResponse htmlResult = this.downloadFileInSFTP(htmlDatePathAndName);
//		    if(!"200".equals(htmlResult.getStatus())){
//				return RestUtils.returnFailure("该html文件在服务器已丢失！");
//			}
		    fileUtils.downloadFileBySFTP(resumePath+htmlDatePathAndName,"text/html");
		    /*
		     * 将html文件路径存入redis
		     */
			stringRedisTemplate.opsForValue().set(htmlDatePathAndName, htmlDatePathAndName, 30, TimeUnit.MINUTES);
			logger.info("redis中存储的html路径："+htmlDatePathAndName);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("previewResume异常：",e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致预览简历失败！");
		}
	}
	
	/**
	 * Description: 手机号码、邮箱重复性校验。（Y表示数据库已存在）
	 *
	 * @param resume
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月30日 下午14:06:39
	 */
	public RestResponse validPhoneAndEmail(Map<String,Object> params) {
		logger.info("进入validPhoneAndEmail方法，参数是：params="+params);
		try {
			ErpResume validResult = resumeMapper.validPhoneAndEmail(params);
			if(validResult!=null) {
				//数据库已存在
				return RestUtils.returnSuccessWithString("Y");
			}else {
				//数据库不存在
				return RestUtils.returnSuccessWithString("N");
			}
		} catch (Exception e) {
			logger.error("validPhoneAndEmail方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("新建简历时校验手机号和邮箱时出现异常！");
		}
	}
    
}
