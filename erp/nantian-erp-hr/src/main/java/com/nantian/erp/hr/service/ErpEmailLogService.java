package com.nantian.erp.hr.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.ErrorEmailLogMapper;
import com.nantian.erp.hr.data.dao.TEmailServiceConfigMapper;
import com.nantian.erp.hr.data.model.ErpDimission;
import com.nantian.erp.hr.data.model.ErrorEmailLog;
import com.nantian.erp.hr.util.FileUtils;
import com.nantian.erp.hr.util.RestTemplateUtils;

/** 
 * Description: EmailLog的Service
 *
 * @author hhr
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2020年05月26日      		hhr          1.0       
 * </pre>
 */
@Service
public class ErpEmailLogService {
	
	@Value("${sftp.departmentTransfPath}")
	private String departmentTransfPath;
	@Value("${sftp.basePath}")
	private String basePath;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private ErrorEmailLogMapper errorEmailLogMapper;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private RestTemplateUtils restTemplateUtils;
	@Autowired
	AdminDicMapper adminDicMapper;
	@Autowired
	private FileUtils fileUtils;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 查询所有邮件日志
	 * @return RestResponse
	 */
	public RestResponse findEmailLogDetail(Integer type,String startTime,String endTime) {
		logger.info("findEmailLogDetail方法开始执行，无参数");
	    List<Map<String,Object>> tempList=new ArrayList<Map<String,Object>>();
		try {	
			Map<String,Object> map=new HashMap<>();
			map.put("type", type);
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			tempList=this.errorEmailLogMapper.selectALL(map);  //所有邮件日志
			for(Map<String,Object> emailLog:tempList){
				String attachmentPath=String.valueOf(emailLog.get("attachment_path"));//附件路径
				if(attachmentPath!=null){
					if(attachmentPath.contains(",")){
						String[] strs=attachmentPath.split(",");
						StringBuilder emailCcStringBuilder = new StringBuilder();
						for(String str:strs){
							String[] filePath=str.split("/");
							emailCcStringBuilder.append(filePath[filePath.length-1]).append(",");
						}
						if(emailCcStringBuilder.length() > 0){
							emailCcStringBuilder = emailCcStringBuilder.deleteCharAt(emailCcStringBuilder.length()-1);
						}
						emailLog.put("fileName", emailCcStringBuilder);
					}else{
						String[] strs=attachmentPath.split("/");
						emailLog.put("fileName", strs[strs.length-1]);
					}
				}
			}
		} catch (Exception e) {
			logger.info("findEmailLogDetail方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(tempList);
	}

	/**
	 * 修改邮件日志
	 * @return RestResponse
	 */
	public RestResponse updateEmailLog(ErrorEmailLog errorEmailLog) {
		logger.info("updateEmailLog方法开始执行，参数是：params={}",errorEmailLog.toString());
		errorEmailLogMapper.updateByPrimaryKeySelective(errorEmailLog);
		return RestUtils.returnSuccess("已修改");
	}

	/**
	 * 发送指定邮件
	 * @return RestResponse
	 */
	public RestResponse sendEmailLogById(Map<String,Object> map) {
		logger.info("sendEmailLogById方法开始执行，参数是：params={}",map);
		boolean sendSuccess=true;
		Integer id=Integer.valueOf(String.valueOf(map.get("id")));
		List<Map<String,Object>> emailLog = this.errorEmailLogMapper.selectByParam(map);
		if(emailLog.size()!=0){
			Map<String,Object> emailLogMap=emailLog.get(0);
			Integer type=Integer.valueOf(String.valueOf(emailLogMap.get("type")));//是否发送成功
			if(type!=1){//发送不成功
				String attachmentPath=String.valueOf(emailLogMap.get("attachment_path"));//附件路径
				String sender=String.valueOf(emailLogMap.get("sender"));//发送人
				String bcc=String.valueOf(emailLogMap.get("bcc"));//抄送人
				String subject=String.valueOf(emailLogMap.get("subject"));//邮件主题
				String text=String.valueOf(emailLogMap.get("email_message"));//邮件内容
				String recipient=String.valueOf(emailLogMap.get("recipient"));//收件人
				Integer emaiServiceType=Integer.valueOf(String.valueOf(emailLogMap.get("email_service_type")));//邮件服务类型
				
				if("null".equals(attachmentPath)||"".equals(attachmentPath)){//不带附件邮件
					sendSuccess = restTemplateUtils.sendEmail(sender, bcc,subject ,
							text,recipient , emaiServiceType,id);
				}else{
					if(attachmentPath.contains(",")){//多附件邮件
						Map<String,Object> emailUserNamePassword = getEmailUserNameAndPassword();
						String[] strs=attachmentPath.split(",");
						List<Map<String,String>> attachments=new ArrayList<>();
						for(String str:strs){
							String fName=str.trim();
							String fileName=fName.substring(fName.lastIndexOf('/')+1);//去掉日期的报告文件名
							Map<String,String> fileMap=new HashMap<>();
							fileMap.put("filepath", str);
							fileMap.put("filename", fileName);
							attachments.add(fileMap);
						}
						sendSuccess=restTemplateUtils.sendEmailWithAttachments(sender, bcc, subject, text, recipient, attachments, emailUserNamePassword, emaiServiceType,id);
					}else{//单附件
						String[] strs=attachmentPath.split("/");
						String filename=strs[strs.length-1];
						sendSuccess=restTemplateUtils.sendEmailWithAttachment(sender, bcc, subject, text, recipient, filename, attachmentPath, emaiServiceType,id);
					}
				}
			}
		}
		if(sendSuccess){
			return RestUtils.returnSuccess("邮件发送成功！");
		}else{
			return RestUtils.returnSuccess("邮件发送失败！");
		}
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

	/**
	 * Description: 根据邮件日志ID下载附件
	 * 
	 *  邮件日志表主键
	 * @return
	 */
	public RestResponse downloadEmailAttachment(String attachmentPath) {
		logger.info("进入downloadEmailAttachment方法，参数是：attachmentPath="+attachmentPath);
	    try {
 			if(attachmentPath==null || "".equals(attachmentPath)){
 				return RestUtils.returnFailure("该邮件没有附件！");
 			}
 			if(attachmentPath.startsWith(basePath)){
 	 			attachmentPath=attachmentPath.substring(basePath.length());
 			}
	    	fileUtils.downloadFileBySFTP(attachmentPath,"application/octet-stream");
 	    	return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("downloadEmailAttachment方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致下载邮件附件失败！");
		}
	}
}
