package com.nantian.erp.hr.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nantian.erp.common.base.util.HttpClientUtil;
import com.nantian.erp.hr.util.RestTemplateUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.exception.BizException;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.ErpContractMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeRecordMapper;
import com.nantian.erp.hr.data.dao.ErpPositionRankRelationMapper;
import com.nantian.erp.hr.data.dao.ErpResumeMapper;
import com.nantian.erp.hr.data.model.ErpCertificate;
import com.nantian.erp.hr.data.model.ErpContract;
import com.nantian.erp.hr.data.model.ErpDepartment;
import com.nantian.erp.hr.data.model.ErpEducationExperience;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpEmployeeRecord;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ErpProjectExperience;
import com.nantian.erp.hr.data.model.ErpTechnicaExpertise;
import com.nantian.erp.hr.data.model.ErpWorkExperience;
import com.nantian.erp.hr.data.model.ExpenseReimbursement;
import com.nantian.erp.hr.data.vo.EmployeeQueryByDeptUserVo;
import com.nantian.erp.hr.data.vo.EmployeeVo;
import com.nantian.erp.hr.data.vo.ExpenseReimbursementVo;
import com.nantian.erp.hr.data.vo.SalaryParamsVo;
import com.nantian.erp.hr.util.FileUtils;
import com.nantian.erp.hr.util.WordToHtmlUtil;
import com.nantian.erp.hr.util.WordUtil;

import sun.misc.BASE64Encoder;

@Service
@PropertySource(value = { "classpath:config/sftp.properties", "file:${spring.profiles.path}/config/sftp.properties",
		"classpath:config/email.properties", "file:${spring.profiles.path}/config/email.properties",
		"classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class ErpEmployeeService {

	@Value("${protocol.type}")
	private String protocolType;// http或https
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpEmployeeRecordMapper erpEmployeeRecordMapper;
	@Autowired
	private ErpContractMapper contractMapper;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ErpEmployeeRecordMapper employeeRecordMapper;
	@Autowired
	private ErpPositionRankRelationMapper positionRankRelationMapper;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private WordUtil wordUtil;
	@Autowired
	private FileUtils fileUtils;
	@Autowired
	private WordToHtmlUtil wordToHtmlUtil;
	@Autowired	
	private AdminDicMapper adminDicMapper;
	@Autowired
	private RestTemplateUtils restTemplateUtils;

	@Value("${email.service.host}")
	private  String emailServiceHost;//邮件服务的IP地址和端口号

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String CERTIFICATE_PATH = "/home/erpupload/certificate";// 简历文件
	
	/**
	 * 修改员工信息
	 * 
	 * @param employee
	 * @return
	 */
	public String updateEmployee(String token, Map<String, Object> employee) {

		logger.info("updateEmployee方法开始执行，传递参数：ErpEmployee：" + employee.toString());

		List<Integer> roleIdList = null; //用户角色ID列表
		String userPhone = null;
		Integer employeeId = Integer.valueOf(String.valueOf(employee.get("employeeId")));
		MultiValueMap<String, Object> findMultiValueMap = new LinkedMultiValueMap<String, Object>();
		findMultiValueMap.add("userId", employeeId);
		HttpHeaders findRequestHeaders = new HttpHeaders();
		findRequestHeaders.add("token", token);// 封装token
		HttpEntity<MultiValueMap<String, Object>> findRequest = new HttpEntity<MultiValueMap<String, Object>>(findMultiValueMap,
				findRequestHeaders);

		String findUrl = protocolType + "nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId";
		ResponseEntity<RestResponse> findResponseEntity = this.restTemplate.exchange(findUrl, HttpMethod.POST, findRequest,
				RestResponse.class);
		RestResponse findResponse = findResponseEntity.getBody();
		if (!findResponse.getStatus().equals("200")) {
			return "调用权限工程findErpUserByUserId失败:";
		} else {
			Object obj = findResponse.getData();
			if (obj != null && !obj.equals("")) {
				String result = JSON.toJSONString(obj);
				Map<String, Object> mapVo = JSON.parseObject(result, Map.class);
				if (mapVo.containsKey("userPhone")) {
					userPhone = String.valueOf(mapVo.get("userPhone"));
				}
				if (mapVo.containsKey("roleIdList")) {
					roleIdList = (List)mapVo.get("roleIdList");
				}
			}

		}

		if (roleIdList.contains(8) || roleIdList.contains(9) ||  roleIdList.contains(2) ){
			//总裁、副总裁、一级部门经理 修改手机号发短信
			if(!String.valueOf(employee.get("userPhone")).equals(userPhone)){
				RestResponse  restResponse = checkSms(employee.get("code")==null ? "" :String.valueOf(employee.get("code")) , employeeId, token);
				if(!restResponse.getData().equals("OK")){
					return "验证码无效!";
				}
			}
		}

		Map<String, Object> param = new HashMap<>();
		if (employee.containsKey("name")) {
			param.put("name", employee.get("name"));
		}
		if (employee.containsKey("sex")) {
			param.put("sex", employee.get("sex"));
		}
		if (employee.containsKey("salaryCardNumber")) {
			param.put("salaryCardNumber", employee.get("salaryCardNumber"));
		}
		if (employee.containsKey("idCardNumber")) {
			param.put("idCardNumber", employee.get("idCardNumber"));
		}
		if (employee.containsKey("school")) {
			param.put("school", employee.get("school"));
		}
		if (employee.containsKey("major")) {
			param.put("major", employee.get("major"));
		}
		if (employee.containsKey("education")) {
			param.put("education", employee.get("education"));
		}
		if (employee.containsKey("employeeId")) {
			param.put("employeeId", employee.get("employeeId"));
		}
		if (employee.containsKey("takeJobTime")) {
			param.put("takeJobTime", employee.get("takeJobTime"));
		}
		if (employee.containsKey("politicalStatus")) {
			param.put("politicalStatus", employee.get("politicalStatus"));
		}
		if (employee.containsKey("groups")) {
			param.put("groups", employee.get("groups"));
		}

		try {
			employeeMapper.updateEmployeeByMap(param);
			
			/**
			 * 续签合同
			 */
			ErpContract contract = new ErpContract();
			contract.setEmployeeId((Integer)employee.get("employeeId"));
			contract.setRenewalStartTime1((String)employee.get("renewalStartTime1"));
			contract.setRenewalEndTime1((String)employee.get("renewalEndTime1"));
			contract.setRenewalStartTime2((String)employee.get("renewalStartTime2"));
			contract.setRenewalEndTime2((String)employee.get("renewalEndTime2"));
			contractMapper.updateContractByEmployeeId(contract);

			// 增加员工在职记录表
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId((Integer) employee.get("employeeId"));
			employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			employeeRecord.setContent("修改员工信息");
			employeeRecord.setProcessoer(erpUser.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);

		} catch (Exception e) {
			logger.error("updateEmployee方法出现异常：" + e.getMessage(),e);
		}
		/*
		 * if(employee.containsKey("userPhone")){ param.put("userPhone",
		 * employee.get("userPhone")); }
		 */
		if (null != employee.get("userPhone")) {
			MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<String, Object>();
			multiValueMap.add("userPhone", employee.get("userPhone"));
			multiValueMap.add("userId", employee.get("employeeId"));
			// 调用权限工程修改个人手机号
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", request.getHeader("token"));// 封装token
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(
					multiValueMap, requestHeaders);
			String url = protocolType + "nantian-erp-authentication/nantian-erp/erp/updateErpUserforForHr";
			logger.info("调用权限工程更新方法开始：" + url);
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.exchange(url, HttpMethod.POST, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			if (!response.getStatus().equals("200")) {
				logger.error("调用权限工程更新方法异常:" + response.getMsg());
				return "调用权限工程更新方法异常:" + response.getMsg();
			}
		}
		return "修改成功！";
	}
	
	/**
	 * @description 供薪酬調用，更新员工信息
	 * @author songxiugong
	 * @update 2019-11-20
	 */
	public RestResponse updateEmployeeBySalary(ErpEmployee paramsMap) {
//		System.out.println("****====" + String.valueOf(paramsMap.get("employee")));
		employeeMapper.updateEmployee(paramsMap);
		return RestUtils.returnSuccess("OK");
	}
	
	/**
	 * @description 添加员工的教育经历
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
	public RestResponse insertOrUpdateEmployeeFile(MultipartFile photoFile,Map<String,Object> params) {
		logger.info("insertOrUpdateEmployeeFile方法开始执行参数是：params={}",params);
		try {
			if (photoFile == null) {
				return RestUtils.returnSuccess("文件为空");
			}
			
			String type = (String) params.get("type");//文件类型
			Integer employeeId = Integer.valueOf((String) params.get("employeeId"));//员工ID

			//将旧文件删除
			Map<String, Object> oldEmployeeInfo = employeeMapper.selectByEmployeeIdForlx(employeeId);
			switch(type) {
				case "empPicture":
					if(oldEmployeeInfo.get("empPictureFileName")!=null) {
						String oldFileName = oldEmployeeInfo.get("empPictureFileName").toString();
						File oldFile = new File(DicConstants.EMPLOYEE_FILE_PATH+"/"+oldFileName);
						oldFile.delete();
					}
					break;
				case "frontIdCard":
					if(oldEmployeeInfo.get("frontIdCardFileName")!=null) {
						String oldFileName = oldEmployeeInfo.get("frontIdCardFileName").toString();
						File oldFile = new File(DicConstants.EMPLOYEE_FILE_PATH+"/"+oldFileName);
						oldFile.delete();
					}
					break;
				case "backIdCard":
					if(oldEmployeeInfo.get("backIdCardFileName")!=null) {
						String oldFileName = oldEmployeeInfo.get("backIdCardFileName").toString();
						File oldFile = new File(DicConstants.EMPLOYEE_FILE_PATH+"/"+oldFileName);
						oldFile.delete();
					}
					break;
				default:
					break;
			}
			
			// 图片存储的路径
			String filePath = DicConstants.EMPLOYEE_FILE_PATH;
			// 获得原始文件名
			String originalFileName = photoFile.getOriginalFilename();

			int indexOfSplit = originalFileName.lastIndexOf('.');
			String photoFileMc = originalFileName.substring(0, indexOfSplit);
			String photoFileHz = originalFileName.substring(indexOfSplit+1);

			String photoFileName = photoFileMc + String.valueOf(System.currentTimeMillis()) + "."
					+ photoFileHz;
			String fileName = filePath + "/" + photoFileName;
			// 判断是否有此路径的文件夹，如果没有，就一层一层创建
			File isFile = new File(filePath);
			if (!isFile.exists()) {
				isFile.mkdirs();
			}
			File file = new File(fileName);
			FileOutputStream out = new FileOutputStream(file);
			out.write(photoFile.getBytes());
			out.flush();
			out.close();
			
			//将图片信息更新到员工表中
			Map<String,Object> employeeInfo = new HashMap<>();
			switch(type) {
				case "empPicture":
					employeeInfo.put("empPictureFileName", photoFileName);
					break;
				case "frontIdCard":
					employeeInfo.put("frontIdCardFileName", photoFileName);
					break;
				case "backIdCard":
					employeeInfo.put("backIdCardFileName", photoFileName);
					break;
				default:
					break;
			}
			employeeInfo.put("employeeId", employeeId);
			employeeMapper.updateEmployeeByMap(employeeInfo);
			return RestUtils.returnSuccess("OK");
		} catch (Exception e) {
			logger.error("insertOrUpdateEmployeeFile方法出现异常：",e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，新增图片失败！");
		}
	}
	
	/**
	 * @description 添加员工的教育经历
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
//	public RestResponse updateEmployeeFile(MultipartFile photoFile,Map<String,Object> params) {
//		logger.info("updateEmployeeFile方法开始执行，参数是：params={}",params);
//		try {
//			if (photoFile == null) {
//				return RestUtils.returnSuccess("文件为空");
//			}
//
//			
//			// 图片存储的路径
//			String filePath = DicConstants.EMPLOYEE_FILE_PATH;
//			// 获得原始文件名
//			String originalFileName = photoFile.getOriginalFilename();
//
//			int indexOfSplit = originalFileName.lastIndexOf('.');
//			String photoFileMc = originalFileName.substring(0, indexOfSplit);
//			String photoFileHz = originalFileName.substring(indexOfSplit+1);
//
//			String photoFileName = photoFileMc + String.valueOf(System.currentTimeMillis()) + "."
//					+ photoFileHz;
//			String fileName = filePath + "/" + photoFileName;
//			// 判断是否有此路径的文件夹，如果没有，就一层一层创建
//			File isFile = new File(filePath);
//			if (!isFile.exists()) {
//				isFile.mkdirs();
//			}
//			File file = new File(fileName);
//			FileOutputStream out = new FileOutputStream(file);
//			out.write(photoFile.getBytes());
//			out.flush();
//			out.close();
//			Map<String,Object> employeeInfo = new HashMap<>();
//			employeeInfo.put("empPictureFileName", photoFileName);
//			employeeMapper.updateEmployeeByMap(employeeInfo);
//			return RestUtils.returnSuccess("OK");
//		} catch (Exception e) {
//			logger.error("updateEmployeeFile方法出现异常：",e.getMessage(),e);
//			return RestUtils.returnFailure("方法异常，新增图片失败！");
//		}
//	}

	@SuppressWarnings("unchecked")
	public List<EmployeeVo> findAllEmployee(String token, String allFlag, String isDimissionPage) {
		logger.info("findEmployeeById方法开始执行，传递参数：allFlag="+allFlag);
		List<Map<String, Object>> resultList = null;
		List<EmployeeVo> empVoList = new ArrayList<>();// 返回前端的list vo
		try {
			// 根据角色过滤数据
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();// 角色列表
			Map<String, Object> queryMap = new HashMap<String, Object>();
			if ("true".equals(allFlag)) {

			}else {

				if (roles.contains(8) || roles.contains(4)) {// 总经理、管理员
					// 查询所有的员工
				} else if (roles.contains(1) || roles.contains(10)) { // hr、投标专员
					// 查询所有的员工（特殊）
				} else if (roles.contains(9)) { // 副总经理
					queryMap.put("superLeaderId", id);
				} else if (roles.contains(2)) {// 一级部门经理角色
					queryMap.put("leaderId", id);
				} else if (roles.contains(5)) {// 二级部门经理角色
					queryMap.put("secLeaderId", id);
				} else {
					return null;
				}
			}

			// 查询符合条件的员工
			if(isDimissionPage == null || "0".equals(isDimissionPage)){
				resultList = employeeMapper.selectAllEmployee(queryMap);
			}else{
				resultList = employeeMapper.selectNotDimissionEmployee(queryMap);
			}

			// 调用权限工程获取 员工的公司邮箱，和手机号
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 封装token
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(null,
					requestHeaders);

			String url = protocolType + "nantian-erp-authentication/nantian-erp/erp/findAllErpUserInfo";
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			Integer userId = null; // erpuser表中员工编号
			String userPhone = null; // erpuser表中个人手机号
			String username = null; // erpuser表中用户名-公司邮箱
			Integer positionId = 0; // 职位Id
			List<Integer> roleIdList = null;
			List<Map<String, Object>> list = new ArrayList<>(); // 调用权限工程获取userList
			if (response.getStatus().equals("200")) {
				list = (List<Map<String, Object>>) response.getData();
			}
			// 根据erpuser表中员工编号比对员工信息
			for (Map<String, Object> empMap : resultList) {// 最外层是employee list
				EmployeeVo vo = new EmployeeVo(); // 如果userId = employeeId 则创建vo对象
				for (Map<String, Object> mapUser : list) {// 内层user list
					if (mapUser.containsKey("userId")) {
						userId = Integer.valueOf(String.valueOf(mapUser.get("userId")));
						username = String.valueOf(mapUser.get("username"));
						userPhone = String.valueOf(mapUser.get("userPhone"));
						roleIdList = (List)mapUser.get("roleIdList");

						if (userId.equals(Integer.valueOf(String.valueOf(empMap.get("employeeId"))))) {
							vo.setUserPhone(userPhone); // 个人手机号
							vo.setUsername(username); // 用户名-公司邮箱

							if (roleIdList.contains(8) || roleIdList.contains(9) ||  roleIdList.contains(2) ){
								//总裁、副总裁、一级部门经理 修改手机号发短信
								vo.setNeedSendSms(true);
							}else {
								vo.setNeedSendSms(false);
							}

							break; // 跳出循环
						}
					}
				}
				ErpPositionRankRelation positionRankRelation = null;
				if (empMap.containsKey("employeeId"))
					vo.setEmployeeId(Integer.valueOf(String.valueOf(empMap.get("employeeId"))));// 员工Id
				if (empMap.containsKey("name"))
					vo.setName(empMap.get("name").toString());// 员工Name
				if (empMap.containsKey("sex"))
					vo.setSex(String.valueOf(empMap.get("sex")));// 性别
				if (empMap.containsKey("idCardNumber"))
					vo.setIdCardNumber(String.valueOf(empMap.get("idCardNumber")));// 身份证号
				String positionName = "";
				if (empMap.containsKey("positionId")) {
					positionId = Integer.valueOf(String.valueOf(empMap.get("positionId")));
				}
				if (empMap.containsKey("position")) {// 员工表里有职位名称显示
					vo.setPosition(String.valueOf(empMap.get("position")));// 职位名称
				} else {// 通过职位ID查找职位职级管理表 获取职位名称
					positionRankRelation = positionRankRelationMapper
							.selectErpPositionRankRelationByPostionNo(positionId);
					if (positionRankRelation == null) {
						vo.setPosition("");
					} else {
						positionName = positionRankRelation.getPositionName();
						vo.setPosition(positionName);
					}
				}
				if (empMap.containsKey("rank"))
					vo.setRank(Integer.valueOf(String.valueOf(empMap.get("rank"))));// 职级
				if (empMap.containsKey("resumeId"))
					vo.setResumeId(Integer.valueOf(String.valueOf(empMap.get("resumeId"))));// 简历id
				if (empMap.containsKey("isActive"))
					vo.setActive((Boolean) empMap.get("isActive"));// 是否激活
				if (empMap.containsKey("projectInfoId"))
					vo.setProjectInfoId(Integer.valueOf(String.valueOf(empMap.get("projectInfoId")))); // 关联项目id
				if (empMap.containsKey("status"))
					vo.setStatus(String.valueOf(empMap.get("status")));// 员工类别状态（0-实习生，1-试用期员工，2-正式员工，3-离职中，4-已离职）
				if (empMap.containsKey("firstDepartment"))
					vo.setFirstDepartment(String.valueOf(empMap.get("firstDepartment")));// 一级部门名称
				if (empMap.containsKey("secondDepartment"))
					vo.setSecondDepartment(String.valueOf(empMap.get("secondDepartment")));// 二级部门名称
				if (empMap.containsKey("statusName"))
					vo.setStatusName(String.valueOf(empMap.get("statusName")));
				if (empMap.containsKey("firstDepartmentId"))
					vo.setFirstDepartmentId(Integer.valueOf(String.valueOf(empMap.get("firstDepartmentId")))); // 关联项目id
				if (empMap.containsKey("secondDepartmentId"))
					vo.setSecondDepartmentId(Integer.valueOf(String.valueOf(empMap.get("secondDepartmentId")))); // 关联项目id
				if (empMap.containsKey("positionFamilyName"))
					vo.setPositionFamilyName(String.valueOf(String.valueOf(empMap.get("positionFamilyName")))); // 关联项目id
				if (empMap.containsKey("positionTypeName"))
					vo.setPositionTypeName(String.valueOf(String.valueOf(empMap.get("positionTypeName")))); // 关联项目id
				if (empMap.containsKey("positionChildName"))
					vo.setPositionChildName(String.valueOf(String.valueOf(empMap.get("positionChildName")))); // 关联项目id
				if(empMap.containsKey("education"))
					vo.setEducation(String.valueOf(String.valueOf(empMap.get("education"))));

				List<Map<String, Object>> certificateList = employeeMapper.findAllCertificateByEmp(Integer.valueOf(String.valueOf(empMap.get("employeeId"))));
				vo.setCertificateList(certificateList);
				vo.setIsDetail("N");
				empVoList.add(vo);
			}

			/*
			 * 给部门经理等角色开通查看员工详情、修改员工信息等功能的权限
			 */
			Map<String, Object> queryMapSpecial = new HashMap<String, Object>();
			if (roles.contains(8) || roles.contains(1) || roles.contains(4)) {// 总经理、HR
				// 查询所有的员工
			} else if (roles.contains(9)) { // 副总经理
				queryMapSpecial.put("superLeaderId", id);
			} else if (roles.contains(2)) {// 一级部门经理角色
				queryMapSpecial.put("leaderId", id);
			} else if (roles.contains(5)) {// 二级部门经理角色
				queryMapSpecial.put("secLeaderId", id);
			} else {
				queryMapSpecial.put("leaderId", -1);
			}
			// 查询符合条件的员工
			List<Map<String, Object>> specialResultList = employeeMapper.selectAllEmployee(queryMapSpecial);

			if (specialResultList != null && specialResultList.size() > 0) {
				for (EmployeeVo empVo : empVoList) {
					for (Map<String, Object> specialResult : specialResultList) {
						Integer employeeId = Integer.valueOf(String.valueOf(specialResult.get("employeeId")));
						if (employeeId.equals(empVo.getEmployeeId())) {
							empVo.setIsDetail("Y");
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("findAllEmployee方法出现异常：" + e.getMessage(), e);
		}
		return empVoList;
	}

	public RestResponse getEmployeeRecordById(Integer employeeId) {
		logger.info("getEmployeeRecordById方法开始执行，传递参数：employeeId=" + employeeId);

		List<ErpEmployeeRecord> employeeRecord = erpEmployeeRecordMapper.findEmployeeRecord(employeeId);

		return RestUtils.returnSuccess(employeeRecord);
	}

	public RestResponse findEmployeeById(Integer employeeId) {
		logger.info("findEmployeeById方法开始执行，传递参数：employeeId=" + employeeId);
		Map<String, Object> employee = null;
		MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<String, Object>();
		multiValueMap.add("userId", employeeId);
		// 调用权限工程获取个人手机号
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("token", request.getHeader("token"));// 封装token
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(multiValueMap,
				requestHeaders);
		String url = protocolType + "nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId";
		Map<String, Object> mapVo = new HashMap<>();
		String userPhone = null;// 个人电话号
		String username = null; // 用户名-邮箱
		Integer userId = null; // 用户名编号
		List<Integer> roleIdList = null; //用户角色ID列表

		try {
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.exchange(url, HttpMethod.POST, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			if (!response.getStatus().equals("200")) {
				return RestUtils.returnFailure("调用权限工程findErpUserByUserId失败:");
			} else {
				Object obj = response.getData();
				if (obj != null && !obj.equals("")) {
					String result = JSON.toJSONString(obj);
					mapVo = JSON.parseObject(result, Map.class);
					if (mapVo.containsKey("userPhone")) {
						userPhone = String.valueOf(mapVo.get("userPhone"));
					}
					if (mapVo.containsKey("username")) {
						username = String.valueOf(mapVo.get("username"));
					}
					if (mapVo.containsKey("userId")) {
						userId = new Integer(String.valueOf(mapVo.get("userId")));
					}
					if (mapVo.containsKey("roleIdList")) {
						roleIdList = (List)mapVo.get("roleIdList");
					}
				}

			}

			// employee = employeeMapper.selectByEmployeeId(employeeId);
			employee = employeeMapper.selectByEmployeeIdForlx(employeeId);
			if(employee!=null&&employee.size()>0) {
				employee.put("userPhone", userPhone);
				employee.put("username", username);
				employee.put("employeeId", employeeId);
				//add by ZhangYuWei 给前端返回 BASE64的图片
				String empPictureFileString = employee.get("empPictureFileName")==null?"":wordUtil
						.getImageStr(DicConstants.EMPLOYEE_FILE_PATH + "/" + employee.get("empPictureFileName"));
				String frontIdCardFileString = employee.get("frontIdCardFileName")==null?"":wordUtil
						.getImageStr(DicConstants.EMPLOYEE_FILE_PATH + "/" + employee.get("frontIdCardFileName"));
				String backIdCardFileString = employee.get("backIdCardFileName")==null?"":wordUtil
						.getImageStr(DicConstants.EMPLOYEE_FILE_PATH + "/" + employee.get("backIdCardFileName"));
				employee.put("empPictureFileString", empPictureFileString);
				employee.put("frontIdCardFileString", frontIdCardFileString);
				employee.put("backIdCardFileString", backIdCardFileString);

				if (roleIdList.contains(8) || roleIdList.contains(9) ||  roleIdList.contains(2) ){
					//总裁、副总裁、一级部门经理 修改手机号发短信
					employee.put("needSendSms", true);
				}else {
					employee.put("needSendSms", false);
				}

			}
		} catch (Exception e) {
			logger.error("方法出现异常：{}",e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常");
		}
		return RestUtils.returnSuccess(employee);
	}

	// ========================================== 供薪酬、项目工程调用（开始） ==========================================
	/**
	 * Description: 通过员工姓名和身份证号查询员工ID（供薪酬工程调用）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 上午9:50:39
	 */
	public RestResponse findEmpIdByIdCardNumAndName(Map<String, Object> paramsMap) {
		logger.info("findEmpIdByIdCardNumAndName方法开始执行，传递参数：params=" + paramsMap);
		Integer employeeId = null;
		try {
			employeeId = employeeMapper.findEmpIdByIdCardNumAndName(paramsMap);
			return RestUtils.returnSuccess(employeeId);
		} catch (Exception e) {
			 
			logger.info("findEmpIdByIdCardNumAndName方法出现异常：" + e.getMessage());
			return RestUtils.returnFailure("findEmpIdByIdCardNumAndName方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * Description: 通过一级部门ID查询所有二级部门的员工信息（供薪酬工程调用）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月24日 上午10:30:43
	 * @Update Date: 2019年05月08日 上午11:09:27
	 */
	public RestResponse findSecDepEmpInfoByFirDepId(Integer firstDepartmentId) {
		logger.info("findSecDepEmpInfoByFirDepId方法开始执行，传递参数：firstDepartmentId=" + firstDepartmentId);
		List<List<Map<String, Object>>> resultList = new ArrayList<>();
		try {
			List<Map<String, Object>> secondRankDepartmentList = erpDepartmentMapper
					.findAllSecondDepartmentByFirDepId(firstDepartmentId);
			for (Map<String, Object> secondRankDepartment : secondRankDepartmentList) {
				// 通过二级部门ID查询其下所有员工的基本信息
				Integer secondDepartmentId = Integer.valueOf(String.valueOf(secondRankDepartment.get("departmentId")));
				Map<String,Object> params = new HashMap<>();
				params.put("secondDepartmentId", secondDepartmentId);
				List<Map<String, Object>> secondDepEmpInfo = employeeMapper.findEmployeeTable(params);
				if (!secondDepEmpInfo.isEmpty()) {
					resultList.add(secondDepEmpInfo);
				}
			}
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.error("findSecDepEmpInfoByFirDepId方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("findSecDepEmpInfoByFirDepId方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * Description: 条件查询所有一级部门下的员工信息（供薪酬工程调用）
	 * queryMode是查询模式（0或者不传参数：查询在职员工+离职员工，1：查询在职员工，2：查询在职员工+指定时间段的离职员工）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月24日 上午09:16:19
	 * @Update Date: 2019年05月08日 上午11:12:34
	 */
	public RestResponse findEmpInfoOfAllFirDepByParams(Map<String, Object> params) {
		logger.info("findEmpInfoOfAllFirDepByParams方法开始执行，传递参数：params=" + params);
		try {
			List<List<List<Map<String, Object>>>> firDepEmpInfoList = new ArrayList<>();// 所有一级部门下的员工信息
			// 根据指定的条件查询所有的一级部门
			List<Map<String, Object>> firstDepartmentList = erpDepartmentMapper.findAllFirstDepartmentByParams(params);
			for (Map<String, Object> firstDepartment : firstDepartmentList) {
				List<List<Map<String, Object>>> secDepEmpInfoList = new ArrayList<>();// 所有二级部门下的员工信息
				// 根据一级部门Id查询所有的二级部门
				Integer firstDepartmentId = Integer.valueOf(String.valueOf(firstDepartment.get("departmentId")));
				List<Map<String, Object>> secondDepartmentList = erpDepartmentMapper.findAllSecondDepartmentByFirDepId(firstDepartmentId);
				for (Map<String, Object> secondDepartment : secondDepartmentList) {
					Map<String,Object> employeeParams = new HashMap<>();//SQL查询条件
					Integer secondDepartmentId = Integer.valueOf(String.valueOf(secondDepartment.get("departmentId")));
					employeeParams.put("secondDepartmentId", secondDepartmentId);
					List<Integer> statusList = null;
					//查询模式（0或者不传参数：查询在职员工+离职员工，1：查询在职员工，2：查询在职员工+指定时间段的离职员工）
					if("1".equals(params.get("queryMode"))) {
						statusList = new ArrayList<>();
						statusList.add(0);
						statusList.add(1);
						statusList.add(2);
						statusList.add(3);
					}else if("2".equals(params.get("queryMode"))) {
						employeeParams.put("dimissionTimeStart", params.get("dimissionTimeStart"));
						employeeParams.put("dimissionTimeEnd", params.get("dimissionTimeEnd"));
					}
					employeeParams.put("statusList", statusList);
					if(params.get("keyword")!=null) {
						employeeParams.put("keyword", "%"+params.get("keyword").toString()+"%");
					}
					// 通过二级部门ID、员工状态查询其下所有员工的基本信息
					List<Map<String, Object>> secondDepEmpInfo = employeeMapper.findEmployeeTable(employeeParams);
					if (!secondDepEmpInfo.isEmpty()) {
						secDepEmpInfoList.add(secondDepEmpInfo);
					}
				}
				if (!secDepEmpInfoList.isEmpty()) {
					firDepEmpInfoList.add(secDepEmpInfoList);
				}
			}
			return RestUtils.returnSuccess(firDepEmpInfoList);
		} catch (Exception e) {
			logger.info("findEmpInfoOfAllFirDepByParams方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}
	/**
	 * Description: 通过员工Id查询员工信息（供薪酬工程调用）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月02日 下午13:31:37
	 */
	public RestResponse findEmployeeDetail(Integer employeeId) {
		logger.info("findEmployeeDetail方法开始执行，传递参数：employeeId=" + employeeId);
		Map<String, Object> employeeMap = new HashMap<>();
		try {
			employeeMap = employeeMapper.findEmployeeDetail(employeeId);
			logger.info("employeeMap=" + employeeMap);
			return RestUtils.returnSuccess(employeeMap);
		} catch (Exception e) {
			logger.info("findEmployeeDetail方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}

	/**
	 * Description: 查询全部的员工信息（供薪酬工程调用）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月03日 下午13:52:07
	 */
	public RestResponse findEmployeeAll() {
		logger.info("findEmployeeAll方法开始执行，无参数");
		List<Map<String, Object>> employeeMapList = new ArrayList<>();
		try {
			employeeMapList = employeeMapper.findEmployeeAll();
			return RestUtils.returnSuccess(employeeMapList);
		} catch (Exception e) {
			logger.info("findEmployeeAll方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 查询员工表信息，参数可以为空（供项目或薪酬工程调用）
	 *
	 * @Author ZhangYuWei
	 * @Create Date: 2019年04月02日
	 */
	public RestResponse findEmployeeTable(Map<String,Object> params) {
		logger.info("findEmployeeTable方法开始执行，传递参数：params={}",params);
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			//查询模式（0或者不传参数：查询在职员工+离职员工，1：查询在职员工，2：查询在职员工+指定时间段的离职员工）
			if("1".equals(params.get("queryMode"))) {
				List<Integer> statusList = new ArrayList<>();
				statusList.add(0);
				statusList.add(1);
				statusList.add(2);
				statusList.add(3);
				params.put("statusList", statusList);
			}else if("2".equals(params.get("queryMode"))) {
				params.put("dimissionTimeStart", params.get("dimissionTimeStart"));
				params.put("dimissionTimeEnd", params.get("dimissionTimeEnd"));
			}
			
			resultList = employeeMapper.findEmployeeTable(params);
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.error("findEmployeeTable方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("findEmployeeTable方法出现异常：" + e.getMessage());
		}
	}
	
	/**
	 * Description: HR和salary数据一致性检查，由Salary调用
	 *
	 * @Author songxiugong
	 * @Create Date: 2019年11月10日
	 */
	public RestResponse automaticComareEmpSalary() {
		logger.info("automaticComareEmpSalary方法开始执行，无参数");
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			//查询HR employee 和 employee_postive关联的数据，由于employee和employee_postive【重复入职】是一对多关系，需要去重操作：获取employee_postive 表的最大id的和employee进行双向一对一关联
			
			resultList = employeeMapper.findEmployeeUnionMaxPositiveID();
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.error("automaticComareEmpSalary方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("automaticComareEmpSalary方法出现异常：" + e.getMessage());
		}
	}
	
	/**
	 * Description: 根据传入的员工ID、部门查询指定一级部门下的员工信息（供薪酬工程--社保公积金调用）
	 * 参数说明：detpId:部门ID，employeeName：员工姓名，page：需要查询的第几页，默认是第一个页，limit：每页展示的记录数
	 *			 status：员工状态是一个字符串,元素是字符串，是"0"、"1"、"2"、"3"、"4"值的组合，中间为英文","分隔。
	 * 逻辑：1.如果当前登陆人是一级部门领导及以上领导，则查询部门下的所有人员
	  		 2.如果当前登陆人只是二级部门领导，则只查询所属二级部门下的所有人员
	 * @return
	 * @Author Songxiugong
	 * @Create Date: 2020年02月20日

	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public RestResponse findEmployeeByDeptAndUser(String token,Map<String,Object> params) {
		logger.info("findEmployeeByDeptAndUser Beggin");
	
		try {
			//0.获取当前登录的角色
//			String token = String.valueOf(params.get("token"));
//			String token = token;
			List<Integer> roles = new ArrayList<>();
			Map<String, Object> employeeMap = new HashMap<>();
			Integer id = null;
			if(token != null){
				ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
				id = erpUser.getUserId();// 从用户信息中获取角色信息
				roles = erpUser.getRoles();// 角色列表
				employeeMap = employeeMapper.findEmployeeDetail(id);
			}

			//qureyMap结构：limit：每页显示多少条，offset：偏移量，deptId：一级部门ID，sdeptId：二级部门ID，superLeaderId：上级部门领导ID（总裁、副总裁），leaderId：一级部门领导ID，employeeName：员工名称
			Map<String,Object> queryMap = new HashMap<String, Object>();	//查询的参数
			
			String rolesSpecified = params.get("roles") == null || String.valueOf(params.get("roles")).equals("")  ?
					"" : String.valueOf(params.get("roles"));
			if(rolesSpecified != null && rolesSpecified !="") {
				List<String> rolesListString = Stream.of(rolesSpecified).collect(Collectors.toList());
				List<Integer> rolesListInteger = rolesListString.stream().map(Integer::valueOf).collect(Collectors.toList());
				roles.addAll(rolesListInteger);
			}
			if(params.get("isAllData") != null && Boolean.valueOf(String.valueOf(params.get("isAllData")))){
				//如果是考勤管理员查询所有月度考勤则查询所有数据
				roles.add(1);
			}

			if(token != null){
				//获取总裁、经管、HR及子角色ID、副总裁及子角色ID、一级部门经理角色及子角色ID、二级部门经理角色及子角色ID
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.add("token", token);// 封装token
				HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(null,
						requestHeaders);

				String url=protocolType+"nantian-erp-authentication/nantian-erp/authentication/role/findRoleIdsAndChildRoleIds";
				ResponseEntity<RestResponse> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, request,
						RestResponse.class);
				RestResponse response = responseEntity.getBody();
				Map<String, Object> roleMap = null; // 调用权限工程获取userList
				if (response.getStatus().equals("200")) {
					roleMap = (Map<String, Object>) response.getData();
				}else{
					logger.error("调用权限工程获取角色列表信息失败!!!");
					return RestUtils.returnFailure("调用权限工程获取角色列表信息失败");
				}
				if(roleMap != null){
					//总裁、经管、HR及子角色ID
					List<Integer> ceoManageHrAndChildRoleIdList = (List<Integer>) roleMap.get("ceoManageHrAndChildRoleIdList");
					//副总裁及子角色ID
					List<Integer> deputyAndChildRoleIdList = (List<Integer>) roleMap.get("deputyAndChildRoleIdList");
					//一级部门经理角色及子角色ID
					List<Integer> firstDepartmentManageAndChildRoleIdList = (List<Integer>) roleMap.get("firstDepartmentManageAndChildRoleIdList");
					//二级部门经理角色及子角色ID
					List<Integer> secondDepartmentManageAndChildRoleIdList = (List<Integer>) roleMap.get("secondDepartmentManageAndChildRoleIdList");

					List<Integer> ceoManageHrRoleIds = roles.stream().filter(item -> ceoManageHrAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
					List<Integer> deputyRoleIds = roles.stream().filter(item -> deputyAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
					List<Integer> firstDepartmentManageRoleIds = roles.stream().filter(item -> firstDepartmentManageAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
					List<Integer> secondDepartmentManageRoleIds = roles.stream().filter(item -> secondDepartmentManageAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
					if(ceoManageHrRoleIds != null && ceoManageHrRoleIds.size() > 0){
						// 总裁、经管、HR
					}else if(deputyRoleIds != null && deputyRoleIds.size() > 0){
						//副总裁
						queryMap.put("superLeaderId", id);
					}else if(firstDepartmentManageRoleIds != null && firstDepartmentManageRoleIds.size() > 0){
						//一级部门经理角色
						if(firstDepartmentManageRoleIds.contains(2)){
							//一级部门经理角色
							queryMap.put("leaderId", id);
						}else{
							//一级部门经理子角色
							queryMap.put("firstDeptId", employeeMap.get("firstDepartmentId"));
						}

					}else if(secondDepartmentManageRoleIds != null && secondDepartmentManageRoleIds.size() > 0){
						if(secondDepartmentManageRoleIds.contains(5)){
							//二级部门经理
							queryMap.put("sdLeaderID", id);	//预留
						}else{
							//二级部门经理子角色
							queryMap.put("secondDeptId", employeeMap.get("secondDepartmentId"));
						}
					}else {
						queryMap.put("other", id);	//其它角色
						return RestUtils.returnSuccess("没有获取数据的权限，请联系管理员！", "NotAuth");
					}
				}else {
					return RestUtils.returnSuccess("没有获取数据的权限，请联系管理员！", "NotAuth");
				}
			}

//			//0-1.角色判断
//			if (roles.contains(8) || roles.contains(7) || roles.contains(1)) {// 总裁、经管、HR
//
//			} else if (roles.contains(9)) { //副总裁
//				queryMap.put("superLeaderId", id);
//			} else if (roles.contains(2)) {	// 一级部门经理角色
//				queryMap.put("leaderId", id);
//			} else if (roles.contains(5)) {	//二级部门经理
//				queryMap.put("sdLeaderID", id);	//预留
//			} else {
////				queryMap.put("other", id);	//其它角色
//				return RestUtils.returnSuccess("没有获取数据的权限，请联系管理员！", "NotAuth");
//			}
			
			//queryMap 中设置 employeeName
			String employeeName = String.valueOf(params.get("employeeName"));
			if(employeeName!=null) {
				if(!employeeName.equals("") && !employeeName.equals("null")) {
					queryMap.put("employeeName", employeeName);
				}
			}
			
			//queryMap 中设置 limit、offset
			String page = String.valueOf(params.get("page"));
			String limit = String.valueOf(params.get("limit"));
			if(page !=null && limit !=null) {
				if(!page.equals("") && !page.equals("null") && !limit.equals("") && !limit.equals("null")) {
					Integer intPage = Integer.valueOf(page);
					Integer intLimit = Integer.valueOf(limit);
					queryMap.put("limit", intLimit);				//每一次查询显示的多少条
					queryMap.put("offset", intLimit*(intPage-1));	//每一次查询相对与之前所有查询页的偏移量（即使每次查询时设置的返回数据条数不一致受影响）	
				}
			}

			
			//1.根据传递的参数分类处理
			List<EmployeeQueryByDeptUserVo> employeList = new ArrayList<EmployeeQueryByDeptUserVo>();	//查询到的员工信息【有分页】
			String deptId = String.valueOf(params.get("deptId"));
			List<Integer> firDeptIds=new ArrayList<Integer>();
			List<Integer> secDeptIds=new ArrayList<Integer>();
			if(params.get("firDeptIds")!=null && params.get("secDeptIds")!=null){
				firDeptIds=(List<Integer>)params.get("firDeptIds");
				secDeptIds=(List<Integer>)params.get("secDeptIds");
			}
			//1.1 如果dept 为空，根据当前登录人角色
			if(firDeptIds.size()!=0||secDeptIds.size()!=0){
				queryMap.put("firDeptIds", firDeptIds);	
				queryMap.put("secDeptIds", secDeptIds);	
			}else{
				if(deptId == null || deptId.equals("null") || deptId.equals("")) {
					//1.1.1如果角色最高为是总裁，指获取所有一级部门的员工
					//1.1.2如果角色最高为是副总裁，获取所管辖的所有一级部门所有员工
					//1.1.3如果角色最高为是一级部门经理，获取所管辖的一级部门所有员工
					//1.1.4如果是角色最高为二级部门经理，获取所管辖的二级部门所有员工

	                if(params.get("exportDepartmentIdList") != null){
	                    queryMap.put("exportDepartmentIdList", (List)params.get("exportDepartmentIdList"));
	                }
				}else {
				//1.2如果Dept 不为空，判断当前登人的角色，获取指定部门的所有人员信息，
					//先判断部门类型，分为一级部门和二级部门
					//根据ID查询部门信息
					
					Integer departmentId = Integer.valueOf(deptId);
					ErpDepartment erpDepartment = erpDepartmentMapper.findByDepartmentId(departmentId);
					Integer rank = Integer.valueOf(erpDepartment.getRank());

					//1.2.1当前登陆人为总裁:ID为一级部门获取指定一级部门的的指定人员；ID为二级部门获取指定二级部门的人员
					//1.2.2当前登陆人为副总裁:ID为一级部门获取指定一级部门的的指定人员；ID为二级部门获取指定二级部门的人员
					//1.2.3当前登陆人为一级部门经理：ID为一级部门获取指定一级部门的的指定人员；ID为二级部门获取指定二级部门的人员
					//1.2.4当前登陆人为二级部门经理：ID为一级部门获取指定一级部门下所管辖的所有二级部门人员，ID为二级部门获取指定二级部门的人员
					queryMap.put("deptId", departmentId);	
					queryMap.put("rank", rank);	
				}
			}
			
			if(params.get("entryTime") != null){
				//只查询当月及以前入职的员工，下月份的不显示
				queryMap.put("entryTime", String.valueOf(params.get("entryTime")));
			}
			if(params.get("dimissionTime") != null){
				//当月及以后离职的员工，之前的不显示
				queryMap.put("dimissionTime", String.valueOf(params.get("dimissionTime")));
			}
			
			if(params.get("status") != null){
				//需要查询的员工状态
				String statusStr = String.valueOf(params.get("status"));
				List<String> statusList = Stream.of(statusStr.split(",")).collect(Collectors.toList());
				queryMap.put("status", statusList);
			}
			
			employeList = employeeMapper.findEmployeeByDeptAndUser(queryMap);
			
			
			Map<String,Object> returnMap = new HashMap<String, Object>();
			returnMap.put("employeList", employeList);
			
			//统计
			queryMap.put("count", "count");	
			queryMap.remove("page");
			queryMap.remove("limit");
			List<EmployeeQueryByDeptUserVo> employeListCount = new ArrayList<EmployeeQueryByDeptUserVo>();	//统计人员信息
			employeListCount = employeeMapper.findEmployeeByDeptAndUser(queryMap);
			if(employeListCount == null) {
				returnMap.put("count", 0);
			}else if(employeListCount.size() == 0) {
				returnMap.put("count", 0);
			}else {
				EmployeeQueryByDeptUserVo employeeQueryByDeptUserVo = employeListCount.get(0);
				returnMap.put("count", employeeQueryByDeptUserVo.getCount());
			}
								
			logger.info("findEmployeeByDeptAndUser End");
			return RestUtils.returnSuccess(returnMap,"OK");
			
		}catch(Exception e) {
			logger.error("findEmployeeByDeptAndUser 出现异常:"+e.getMessage(), e);
			return RestUtils.returnFailure("findEmployeeByDeptAndUser异常。");
		}
	}
	// ========================================== 供薪酬、项目工程调用（结束）==========================================

	/**
	 * add by 张玉伟 查询待我处理（或处理完毕）上岗（或转正）工资单（或薪酬调整）的员工列表
	 * 
	 * @param salaryParamsVo
	 * @return
	 */
	public RestResponse postOrPositivePayroll(SalaryParamsVo salaryParamsVo) {
		logger.info("postOrPositivePayroll方法开始执行，传递参数：SalaryParamsVo:" + salaryParamsVo.toString());
		List<Map<String, Object>> employeeListOfPayroll = null;
		try {
			String username = salaryParamsVo.getUsername();
			String payrollType = salaryParamsVo.getPayrollType();
			Map<String, Object> paramsMap = new HashMap<>();
			if (StringUtils.isNotEmpty(username) && "post".equals(payrollType)) {// 待我处理的上岗工资单
				// 通过用户名找到一级部门ID，然后查询属于一级部门，上岗工资单待处理的员工列表
				/*
				 * List<ErpDepartment> erpDepartmentList =
				 * erpDepartmentMapper.findByDepartmentManagerEmail(username);
				 * if(erpDepartmentList.size()==0) { return
				 * RestUtils.returnSuccessWithString(null); }
				 */
				/*
				 * StringBuffer departmentIdList = new StringBuffer(); for(int
				 * i=0;i<erpDepartmentList.size();i++) { ErpDepartment erpDepartment =
				 * erpDepartmentList.get(i); if(i==0) {
				 * departmentIdList.append("("+erpDepartment.getDepartmentId()); }else
				 * if(i==erpDepartmentList.size()-1){
				 * departmentIdList.append(","+erpDepartment.getDepartmentId()+ ")"); }else{
				 * departmentIdList.append(","+erpDepartment.getDepartmentId()); } }
				 * paramsMap.put("departmentIdList", departmentIdList.toString());
				 */
				/*
				 * List<Integer> departmentIdList = new ArrayList<>(); for(int
				 * i=0;i<erpDepartmentList.size();i++) { ErpDepartment erpDepartment =
				 * erpDepartmentList.get(i);
				 * departmentIdList.add(erpDepartment.getDepartmentId()); }
				 */
				paramsMap.put("departmentManagerEmail", username);
				paramsMap.put("postPayrollStatus", false);
			} else if (StringUtils.isEmpty(username) && "post".equals(payrollType)) {// 所有已处理的上岗工资单
				paramsMap.put("postPayrollStatus", true);
			} else if (StringUtils.isNotEmpty(username) && "positive".equals(payrollType)) {// 待我处理的转正工资单
				paramsMap.put("departmentManagerEmail", username);
				paramsMap.put("positivePayrollStatus", false);
			} else if (StringUtils.isEmpty(username) && "positive".equals(payrollType)) {// 所有已处理的转正工资单
				paramsMap.put("positivePayrollStatus", true);
			} else {
				return RestUtils.returnSuccessWithString("payrollType类型错误！");
			}
			employeeListOfPayroll = employeeMapper.findEmployeeListByParams(paramsMap);
		} catch (Exception e) {
			 
			logger.info("postOrPositivePayroll方法出现异常：" + e.getMessage());
		}
		return RestUtils.returnSuccess(employeeListOfPayroll);
	}

	/**
	 * add by 张玉伟 已经处理完毕上岗（或转正）工资单（或薪酬调整），修改工资单的处理状态为“已处理”
	 * 
	 * @param salaryParamsVo
	 * @return
	 */
	public String processedPayroll(SalaryParamsVo salaryParamsVo) {
		logger.info("processedPayroll方法开始执行，传递参数：SalaryParamsVo:" + salaryParamsVo.toString());
		try {
			Integer employeeId = salaryParamsVo.getEmployeeId();
			String payrollType = salaryParamsVo.getPayrollType();
			// 修改员工的上岗（或转正）工资单（或薪酬调整）的状态为已处理
			ErpEmployee erpEmployee = new ErpEmployee();
			erpEmployee.setEmployeeId(employeeId);
			if ("post".equals(payrollType)) {
				erpEmployee.setPostPayrollStatus(true);
			} else if ("positive".equals(payrollType)) {
				erpEmployee.setPositivePayrollStatus(true);
			} else {
				return "payrollType类型错误！";
			}
			employeeMapper.updateEmployee(erpEmployee);
		} catch (Exception e) {
			 
			logger.info("processedPayroll方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * add by 张玉伟 查询待我处理（或处理完毕）薪酬调整的员工列表（供薪酬工程调用）
	 * 
	 * @param salaryParamsVo
	 * @return
	 */
	public RestResponse adjustSalary(SalaryParamsVo salaryParamsVo) {
		logger.info("adjustSalary方法开始执行，传递过来的参数是：" + salaryParamsVo.toString());
		List<Map<String, Object>> employeeListOfAdjustResult = null;

		try {
			String username = salaryParamsVo.getUsername();
			List<Integer> employeeIds = salaryParamsVo.getEmployeeIds();
			// 封装查询数据库的条件参数
			Map<String, Object> paramsMap = new HashMap<>();
			employeeListOfAdjustResult = new ArrayList<>();
			if (StringUtils.isNotEmpty(username)) {// 待我处理的薪酬调整
				// 通过用户名找到一级部门ID，然后查询属于一级部门，上岗工资单待处理的员工列表
				paramsMap.put("departmentManagerEmail", username);
				List<Map<String, Object>> employeeListOfAdjust = employeeMapper.findEmployeeListByParams(paramsMap);
				for (Map<String, Object> employeeOfAdjust : employeeListOfAdjust) {
					if (employeeIds.contains(employeeOfAdjust.get("employeeId"))) {
						employeeListOfAdjustResult.add(employeeOfAdjust);
					}
				}
			} /*
				 * else {//所有已处理的上岗工资单 paramsMap.put("postPayrollStatus", true); }
				 */
		} catch (Exception e) {
			 
			logger.info("adjustSalary方法出现异常：" + e.getMessage());
		}
		return RestUtils.returnSuccess(employeeListOfAdjustResult);
	}

	// ==================================== add by ZhangYuWei 教育经历（开始）====================================
	/**
	 * @description 查看员工的教育经历
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
	public RestResponse findAllEducationByEmp(Integer employeeId) {
		logger.info("findAllEducationByEmp方法开始执行，传递参数：employeeId={}",employeeId);
		List<Map<String,Object>> resultList = null;
		try {
			resultList = employeeMapper.findAllEducationByEmp(employeeId);
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.error("findAllEducationByEmp方法出现异常：",e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，查询教育经历失败！");
		}
	}
	
	/**
	 * @description 添加员工的教育经历
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
	public RestResponse addEducationByEmp(MultipartFile photoFile,MultipartFile photoFile1,ErpEducationExperience educationExperience) {
		logger.info("addEducationByEmp方法开始执行，传递参数：{}",educationExperience);
		try {
			if (photoFile != null) {
				// 图片存储的路径
				String filePath = DicConstants.EDUCATION_EXPERIENCE_PATH;
				// 获得原始文件名
				String originalResumeName = photoFile.getOriginalFilename();

				int indexOfSplit = originalResumeName.lastIndexOf('.');
				String photoFileMc = originalResumeName.substring(0, indexOfSplit);
				String photoFileHz = originalResumeName.substring(indexOfSplit+1);

				String photoFileName = photoFileMc + String.valueOf(System.currentTimeMillis()) + "."
						+ photoFileHz;
				String fileName = filePath + "/" + photoFileName;
				// 判断是否有此路径的文件夹，如果没有，就一层一层创建
				File isFile = new File(filePath);
				if (!isFile.exists()) {
					isFile.mkdirs();
				}
				File file = new File(fileName);
				FileOutputStream out = new FileOutputStream(file);
				out.write(photoFile.getBytes());
				out.flush();
				out.close();
				educationExperience.setFilename(photoFileName);
			}
			
			if (photoFile1 != null) {
				// 图片存储的路径
				String filePath = DicConstants.EDUCATION_EXPERIENCE_PATH;
				// 获得原始文件名
				String originalResumeName = photoFile1.getOriginalFilename();

				int indexOfSplit = originalResumeName.lastIndexOf('.');
				String photoFileMc = originalResumeName.substring(0, indexOfSplit);
				String photoFileHz = originalResumeName.substring(indexOfSplit+1);

				String photoFileName = photoFileMc + String.valueOf(System.currentTimeMillis()) + "."
						+ photoFileHz;
				String fileName = filePath + "/" + photoFileName;
				// 判断是否有此路径的文件夹，如果没有，就一层一层创建
				File isFile = new File(filePath);
				if (!isFile.exists()) {
					isFile.mkdirs();
				}
				File file = new File(fileName);
				FileOutputStream out = new FileOutputStream(file);
				out.write(photoFile1.getBytes());
				out.flush();
				out.close();
				educationExperience.setFilename1(photoFileName);
			}
			
			employeeMapper.addEducationByEmp(educationExperience);
			return RestUtils.returnSuccess("OK");
		} catch (Exception e) {
			logger.error("addEducationByEmp方法出现异常：",e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，新增教育经历失败！");
		}
	}
	
	/**
	 * @description 删除员工的教育经历
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
	public RestResponse deleteEducationByEmp(Integer id) {
		logger.info("deleteEducationByEmp方法开始执行，传递参数：id={}",id);
		try {
			//将旧文件删除
			Map<String, Object> education = employeeMapper.findEducationById(id);
			if(education!=null) {
				if(education.get("filename")!=null) {
					String fileName = education.get("filename").toString();
					File file = new File(DicConstants.EDUCATION_EXPERIENCE_PATH+"/"+fileName);
					file.delete();
				}
				if(education.get("filename1")!=null) {
					String fileName = education.get("filename1").toString();
					File file = new File(DicConstants.EDUCATION_EXPERIENCE_PATH+"/"+fileName);
					file.delete();
				}
			}
			
			//数据库删除数据
			employeeMapper.deleteEducationByEmp(id);
			return RestUtils.returnSuccess("OK");
		} catch (Exception e) {
			logger.error("deleteEducationByEmp方法出现异常：",e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，删除教育经历失败！");
		}
	}

	/**
	 * @description 修改员工的教育经历
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
	public RestResponse updateEducationByEmp(MultipartFile photoFile,MultipartFile photoFile1,ErpEducationExperience educationExperience) {
		logger.info("updateEducationByEmp方法开始执行，传递参数：{}",educationExperience);
		try {
			if (photoFile != null) {
				//如果有旧文件，将旧文件删除
				Map<String, Object> education = employeeMapper.findEducationById(educationExperience.getId());
				if(education!=null && education.get("filename")!=null) {
					String oldFileName = String.valueOf(education.get("filename"));
					File oldFile = new File(DicConstants.EDUCATION_EXPERIENCE_PATH+"/"+oldFileName);
					oldFile.delete();
				}
				
				// 图片存储的路径
				String filePath = DicConstants.EDUCATION_EXPERIENCE_PATH;
				// 获得原始文件名
				String originalResumeName = photoFile.getOriginalFilename();

				int indexOfSplit = originalResumeName.lastIndexOf('.');
				String photoFileMc = originalResumeName.substring(0, indexOfSplit);
				String photoFileHz = originalResumeName.substring(indexOfSplit+1);

				String photoFileName = photoFileMc + String.valueOf(System.currentTimeMillis()) + "."
						+ photoFileHz;
				String fileName = filePath + "/" + photoFileName;
				// 判断是否有此路径的文件夹，如果没有，就一层一层创建
				File isFile = new File(filePath);
				if (!isFile.exists()) {
					isFile.mkdirs();
				}
				File file = new File(fileName);
				FileOutputStream out = new FileOutputStream(file);
				out.write(photoFile.getBytes());
				out.flush();
				out.close();
				educationExperience.setFilename(photoFileName);
			}
			
			if (photoFile1 != null) {
				//如果有旧文件，将旧文件删除
				Map<String, Object> education = employeeMapper.findEducationById(educationExperience.getId());
				if(education!=null && education.get("filename1")!=null) {
					String oldFileName = String.valueOf(education.get("filename1"));
					File oldFile = new File(DicConstants.EDUCATION_EXPERIENCE_PATH+"/"+oldFileName);
					oldFile.delete();
				}
				
				// 图片存储的路径
				String filePath = DicConstants.EDUCATION_EXPERIENCE_PATH;
				// 获得原始文件名
				String originalResumeName = photoFile1.getOriginalFilename();

				int indexOfSplit = originalResumeName.lastIndexOf('.');
				String photoFileMc = originalResumeName.substring(0, indexOfSplit);
				String photoFileHz = originalResumeName.substring(indexOfSplit+1);

				String photoFileName = photoFileMc + String.valueOf(System.currentTimeMillis()) + "."
						+ photoFileHz;
				String fileName = filePath + "/" + photoFileName;
				// 判断是否有此路径的文件夹，如果没有，就一层一层创建
				File isFile = new File(filePath);
				if (!isFile.exists()) {
					isFile.mkdirs();
				}
				File file = new File(fileName);
				FileOutputStream out = new FileOutputStream(file);
				out.write(photoFile1.getBytes());
				out.flush();
				out.close();
				educationExperience.setFilename1(photoFileName);
			}
			
			employeeMapper.updateEducationByEmp(educationExperience);
			return RestUtils.returnSuccess("OK");
		} catch (Exception e) {
			logger.error("updateEducationByEmp方法出现异常：",e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，更新教育经历失败！");
		}
	}
	
	/**
	 * @description 证书的下载（因为图片有多个，所以参数是文件名，不是主键）
	 * @author ZhangYuWei
	 * @update 2019-04-16
	 */
	public RestResponse downloadEducationByEmp(String fileName) {
		logger.info("downloadEducationByEmp方法开始执行，传递参数：fileName={}",fileName);
		try {
//			Map<String, Object> education = employeeMapper.findEducationById(id);
//			String fileName = education.get("filename").toString();

			String filePathAndName = DicConstants.EDUCATION_EXPERIENCE_PATH + "/" + fileName;
			fileUtils.downloadFileToComputer(filePathAndName,"application/octet-stream");
			return RestUtils.returnSuccess("OK");
		} catch (Exception e) {
			logger.error("downloadEducationByEmp方法出现异常：",e.getMessage(),e);
			return RestUtils.returnFailure("下载时出现了异常！");
		}
	}
	// ==================================== add by ZhangYuWei 教育经历（结束）====================================
	

	// ===============================工作经历====================================
	/**
	 * 查看员工的工作经历
	 * 
	 * @return
	 */
	public List<Map<String, Object>> findAllWorkExperienceByEmp(Integer employeeId) {
		logger.info("findAllWorkExperienceByEmp方法开始执行，传递参数：employeeId=" + employeeId);
		List<Map<String, Object>> resultList = null;
		try {
			if (employeeId != null) {
				resultList = employeeMapper.findAllWorkExperienceByEmp(employeeId);
			}
		} catch (Exception e) {
			 
			logger.info("findAllWorkExperienceByEmp方法出现异常：" + e.getMessage());

		}
		return resultList;
	}

	/**
	 * 添加员工的工作经历
	 * 
	 * @return
	 */
	public String addWorkExperienceByEmp(ErpWorkExperience erpWorkExperience) {
		logger.info("addWorkExperienceByEmp方法开始执行，传递参数：ErpWorkExperience:" + erpWorkExperience.toString());

		try {
			if (erpWorkExperience != null) {
				employeeMapper.addWorkExperienceByEmp(erpWorkExperience);
			}
		} catch (Exception e) {
			 
			logger.info("addWorkExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * 修改员工的工作经历
	 * 
	 * @return
	 */
	public String updateWorkExperienceByEmp(ErpWorkExperience erpWorkExperience) {
		logger.info("updateWorkExperienceByEmp方法开始执行，传递参数：ErpWorkExperience:" + erpWorkExperience.toString());
		try {
			if (erpWorkExperience != null) {
				employeeMapper.updateWorkExperienceByEmp(erpWorkExperience);
			}
		} catch (Exception e) {
			 
			logger.info("updateWorkExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * 删除员工的工作经历
	 * 
	 * @return
	 */
	public String deleteWorkExperienceByEmp(Integer id) {
		logger.info("deleteWorkExperienceByEmp方法开始执行，传递参数：id=" + id);
		try {
			if (id != null) {
				employeeMapper.deleteWorkExperienceByEmp(id);
			}
		} catch (Exception e) {
			 
			logger.info("deleteWorkExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	// ===============================项目经历====================================
	/**
	 * 查看员工的项目经历
	 * 
	 * @return
	 */
	public List<Map<String, Object>> findAllProjectExperienceByEmp(Integer employeeId) {
		logger.info("findAllProjectExperienceByEmp方法开始执行，传递参数：employeeId=" + employeeId);
		List<Map<String, Object>> resultList = null;
		try {
			if (employeeId != null) {
				resultList = employeeMapper.findAllProjectExperienceByEmp(employeeId);
			}
		} catch (Exception e) {
			 
			logger.info("findAllProjectExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return resultList;
	}

	/**
	 * 添加员工的项目经历
	 * 
	 * @return
	 */
	public String addProjectExperienceByEmp(ErpProjectExperience erpProjectExperience) {
		logger.info("addProjectExperienceByEmp方法，传递参数：ErpProjectExperience:" + erpProjectExperience.toString());
		try {
			if (erpProjectExperience != null) {
				employeeMapper.addProjectExperienceByEmp(erpProjectExperience);
			}
		} catch (Exception e) {
			 
			logger.info("addProjectExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * 修改员工的项目经历
	 * 
	 * @return
	 */
	public String updateProjectExperienceByEmp(ErpProjectExperience erpProjectExperience) {
		logger.info("updateProjectExperienceByEmp方法，传递参数：ErpProjectExperience:" + erpProjectExperience.toString());
		try {
			if (erpProjectExperience != null) {
				employeeMapper.updateProjectExperienceByEmp(erpProjectExperience);
			}
		} catch (Exception e) {
			 
			logger.info("updateProjectExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * 删除员工的项目经历
	 * 
	 * @return
	 */
	public String deleteProjectExperienceByEmp(Integer id) {
		logger.info("deleteProjectExperienceByEmp方法开始执行，传递参数：id=" + id);
		try {
			if (id != null) {
				employeeMapper.deleteProjectExperienceByEmp(id);
			}
			employeeMapper.deleteProjectExperienceByEmp(id);
		} catch (Exception e) {
			 
			logger.info("deleteProjectExperienceByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	// ===============================技术特长====================================
	/**
	 * 查看员工的技术特长
	 * 
	 * @return
	 */
	public List<Map<String, Object>> findAllTechnicaExpertiseByEmp(Integer employeeId) {
		logger.info("findAllTechnicaExpertiseByEmp方法开始执行，传递参数：employeeId=" + employeeId);
		List<Map<String, Object>> resultList = null;
		try {
			if (employeeId != null) {
				resultList = employeeMapper.findAllTechnicaExpertiseByEmp(employeeId);
			}
		} catch (Exception e) {
			 
			logger.info("findAllTechnicaExpertiseByEmp方法出现异常：" + e.getMessage());
		}
		return resultList;
	}

	/**
	 * 添加员工的技术特长
	 * 
	 * @return
	 */
	public String addTechnicaExpertiseByEmp(ErpTechnicaExpertise erpTechnicaExpertise) {
		logger.info("addTechnicaExpertiseByEmp方法，传递参数：ErpTechnicaExpertise:" + erpTechnicaExpertise.toString());
		try {
			if (erpTechnicaExpertise != null) {
				employeeMapper.addTechnicaExpertiseByEmp(erpTechnicaExpertise);
			}
		} catch (Exception e) {
			 
			logger.info("addTechnicaExpertiseByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * 修改员工的技术特长
	 * 
	 * @return
	 */
	public String updateTechnicaExpertiseByEmp(ErpTechnicaExpertise erpTechnicaExpertise) {
		logger.info("updateTechnicaExpertiseByEmp方法，传递参数：ErpTechnicaExpertise:" + erpTechnicaExpertise.toString());
		try {
			if (erpTechnicaExpertise != null) {
				employeeMapper.updateTechnicaExpertiseByEmp(erpTechnicaExpertise);
			}
		} catch (Exception e) {
			 
			logger.info("updateTechnicaExpertiseByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	/**
	 * 删除员工的技术特长
	 * 
	 * @return
	 */
	public String deleteTechnicaExpertiseByEmp(Integer id) {
		logger.info("deleteTechnicaExpertiseByEmp方法开始执行，传递参数：id=" + id);
		try {
			if (id != null) {
				employeeMapper.deleteTechnicaExpertiseByEmp(id);
			}
			employeeMapper.deleteTechnicaExpertiseByEmp(id);
		} catch (Exception e) {
			 
			logger.info("deleteTechnicaExpertiseByEmp方法出现异常：" + e.getMessage());
		}
		return "OK";
	}

	// ===============================证书====================================
	/**
	 * 查看员工的证书
	 * 
	 * @return
	 */
	public List<Map<String, Object>> findAllCertificateByEmp(Integer employeeId) {
		logger.info("findAllCertificateByEmp方法开始执行，传递参数：employeeId=" + employeeId);
		List<Map<String, Object>> resultList = null;
		try {
			if (employeeId != null) {
				resultList = employeeMapper.findAllCertificateByEmp(employeeId);
			}
		} catch (Exception e) {
			logger.error("findAllCertificateByEmp方法出现异常：" + e.getMessage(),e);
		}
		return resultList;
	}

	/**
	 * 添加员工的证书
	 * 
	 * @return
	 * @throws IOException
	 */
	public RestResponse addCertificateByEmp(MultipartFile PhotoFile, ErpCertificate erpCertificate) throws IOException {
		logger.info("addCertificateByEmp方法开始执行，参数是：" + erpCertificate);
		try {
			// 图片存储的路径
			String filePath = DicConstants.CERTIFICATE_PATH;
			if (PhotoFile != null) {
				// 获得原始文件名
				String originalResumeName = PhotoFile.getOriginalFilename();

				String[] certificateArray = originalResumeName.split("\\.");
				String certificateMc = certificateArray[0];
				String certificateHz = certificateArray[1];

				String certificateName = certificateMc + String.valueOf(System.currentTimeMillis()) + "."
						+ certificateHz;
				String fileName = filePath + "/" + certificateName;
				// 判断是否有此路径的文件夹，如果没有，就一层一层创建
				File isFile = new File(filePath);
				if (!isFile.exists()) {
					isFile.mkdirs();
				}
				File file = new File(fileName);
				FileOutputStream out = null;
				/*
				 * try { //调用File的transferTo方法把接收到的文件写入到对象中去 PhotoFile.transferTo(newPhotoFile);
				 * } catch (IllegalStateException | IOException e) {   }
				 */
				out = new FileOutputStream(file);
				out.write(PhotoFile.getBytes());
				out.flush();
				out.close();
				erpCertificate.setFilename(certificateName);
			}
			if (erpCertificate != null) {
				employeeMapper.addCertificateByEmp(erpCertificate);
			}
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("addCertificateByEmp方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("证书上传失败！");
		}
	}

	/**
	 * 修改员工的证书
	 * 
	 * @return
	 */
	public RestResponse updateCertificateByEmp(MultipartFile PhotoFile, ErpCertificate erpCertificate)
			throws IOException {
		logger.info("updateCertificateByEmp方法开始执行，传递参数1：MultipartFile类型文件" + "，参数2：ErpCertificate:"
				+ erpCertificate.toString());
		try {
			// 图片存储的路径
			String filePath = DicConstants.CERTIFICATE_PATH;
			if (PhotoFile != null) {
				//将旧文件删除
				Map<String, Object> certificate = employeeMapper.seceltCertificateById(erpCertificate.getId());
				if(certificate!=null && certificate.get("filename")!=null) {
					String fileName = String.valueOf(certificate.get("filename"));
					File file = new File(DicConstants.CERTIFICATE_PATH+"/"+fileName);
					file.delete();
				}
				
				// 获得原始文件名
				String originalResumeName = PhotoFile.getOriginalFilename();

				String[] certificateArray = originalResumeName.split("\\.");
				String certificateMc = certificateArray[0];
				String certificateHz = certificateArray[1];

				String certificateName = certificateMc + String.valueOf(System.currentTimeMillis()) + "."
						+ certificateHz;
				String fileName = filePath + "/" + certificateName;

				File file = new File(fileName);
				FileOutputStream out = null;
				/*
				 * try { //调用File的transferTo方法把接收到的文件写入到对象中去 PhotoFile.transferTo(newPhotoFile);
				 * } catch (IllegalStateException | IOException e) {   }
				 */
				out = new FileOutputStream(file);
				out.write(PhotoFile.getBytes());
				out.flush();
				out.close();
				erpCertificate.setFilename(certificateName);
			}
			if (erpCertificate != null) {
				employeeMapper.updateCertificateByEmp(erpCertificate);
			}
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("updateCertificateByEmp方法出现异常：" + e.getMessage());
			return RestUtils.returnFailure("证书修改失败！");
		}
	}

	/**
	 * 删除员工的证书
	 * 
	 * @return
	 */
	public String deleteCertificateByEmp(Integer id) {
		logger.info("deleteCertificateByEmp方法开始执行，传递参数：id=" + id);
		try {
			//将旧文件删除
			Map<String, Object> certificate = employeeMapper.seceltCertificateById(id);
			String fileName = String.valueOf(certificate.get("filename"));
			File file = new File(DicConstants.CERTIFICATE_PATH+"/"+fileName);
			file.delete();
			//删除数据库数据
			if (id != null) {
				employeeMapper.deleteCertificateByEmp(id);
			}
		} catch (Exception e) {
			logger.error("deleteCertificateByEmp方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}

	/**
	 * 证书的下载
	 * 
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public RestResponse downloadCertificate(Integer id, HttpServletRequest request, HttpServletResponse response) {
		logger.info("downloadCertificate方法开始执行，传递参数：id=" + id);
		Map<String, Object> certificate = employeeMapper.seceltCertificateById(id);
		String fileName = certificate.get("filename").toString();
		try {
			RestResponse result = this.downloadPhoto(id, fileName);
			return result;
		} catch (IOException e) {
			return RestUtils.returnSuccessWithString("下载时出现了异常！");
		}
	}

	// 功能方法：下载
	public RestResponse downloadPhoto(Integer id, String fileName) throws IOException {

		Map<String, Object> certificate = employeeMapper.seceltCertificateById(id);
		String certificateName = certificate.get("certificateName").toString();

		File file = new File(DicConstants.CERTIFICATE_PATH + "/" + fileName);
		if (!file.exists()) {
			return RestUtils.returnSuccessWithString("文件不存在！");
		}
		FileInputStream fis = new FileInputStream(file);
		long size = file.length();
		byte[] temp = new byte[(int) size];
		fis.read(temp, 0, (int) size);
		fis.close();
		byte[] data = temp;

		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		response.setHeader("Content-Disposition", "attachment;filename=" + certificateName);
		// form-data
		response.setContentType("multipart/form-data");
		OutputStream out = response.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
		return RestUtils.returnSuccessWithString("OK");
	}

	public List<Map<String, Object>> findEmployeeByPositiveMonth(String probationEndTime) {
		logger.info("findEmployeeByPositiveMonth方法开始执行m,参数probationEndTime=" + probationEndTime);
		List<Map<String, Object>> resultList = null;
		try {
			resultList = employeeMapper.findEmployeeByPositiveMonth(probationEndTime);
		} catch (Exception e) {
			 
			logger.error("findEmployeeByPositiveMonth出现异常" + e.getMessage());
			RestUtils.returnFailure("findEmployeeByPositiveMonth()出现异常" + e.getMessage());
		}
		return resultList;
	}

	public RestResponse findEmpInfoById(String token, List<Map<String, Object>> employeeIdList) {

		logger.info("findEmployeeById方法开始执行，传递参数：employeeIdList=" + employeeIdList);
		List<Map<String, Object>> returnEmployee = new ArrayList<Map<String, Object>>();

		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
		List<Integer> roles = erpUser.getRoles();// 角色列表
		try {
			Map<String, Object> queryMap = new HashMap<String, Object>();
			if (roles.contains(8) || roles.contains(7) || roles.contains(1)) {// 总经理可以看到所有部门的待入职

			} else if (roles.contains(9)) { // 副总经理
				queryMap.put("superLeaderId", id);
			} else if (roles.contains(2)) {// 一级部门经理角色
				queryMap.put("leaderId", id);
			} else {
				return RestUtils.returnSuccess(returnEmployee);
			}
			for (int i = 0; i < employeeIdList.size(); i++) {
				Map<String, Object> employeeMap = employeeIdList.get(i);
				queryMap.put("employeeId", (Integer) employeeMap.get("userId"));
				Map<String, Object> employeeInfo = employeeMapper.selectByEmployeeId(queryMap);
				if (employeeInfo != null) {
					// 查看员工是否有试用期
					employeeInfo.putAll(employeeMap);
					returnEmployee.add(employeeInfo);
				}
			}
		} catch (Exception e) {
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常findEmployeeById()" + e.getMessage());
		}
		return RestUtils.returnSuccess(returnEmployee);
	}

	public RestResponse findPostApplicantNameByEmpId(Integer employeeId) {
		logger.info("findPostApplicantNameByEmpId方法开始执行，传递参数：employeeId=" + employeeId);
		Map<String, Object> employee = null;

		try {
			employee = employeeMapper.findPostApplicantNameByEmpId(employeeId);
		} catch (Exception e) {
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常findEmployeeById()" + e.getMessage());
		}
		return RestUtils.returnSuccess(employee);
	}

	public RestResponse findEmpNameMapById(Map<String, Object> map) {
		System.out.println(".............");
		System.out.println("map..." + map);
		List<Map<String, Object>> listmap = (List<Map<String, Object>>) map.get("listMap");
		System.out.println("listmap" + listmap);
		try {
			for (Map<String, Object> map2 : listmap) {
				System.out.println("map2..." + map2);
				Integer userId = Integer.valueOf(String.valueOf(map2.get("superLeaderId")));
				System.out.println("userId" + userId);
				Map<String, Object> employee = employeeMapper.selectEmployeeById(userId);
				String name = (String) employee.get("name");
				Object employeeId = employee.get("employeeId");
				map2.remove("superLeaderUserId");
				map2.put("superLeaderName", name);
				map2.put("superLeaderEId", employeeId);
				System.out.println("superLeaderName" + name);
			}
			System.out.println("listmap" + listmap);
		} catch (Exception e) {
			logger.error("调用getUserIdById方法出错" + e.getMessage());
			return RestUtils.returnFailure("调用getUserIdById方法出错" + e.getMessage());
		}
		return RestUtils.returnSuccess(listmap);
	}

	public RestResponse findEmpNameByEIdMap(Map<String, Object> map) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> listmap = (List<Map<String, Object>>) map.get("listMap");
		try {
			for (Map<String, Object> map2 : listmap) {
				Integer erp_employee_id = (Integer) map2.get("erp_employee_id");
				Integer approverId = (Integer) map2.get("approverId");
				Map<String, Object> employee1 = employeeMapper.selectEmployeeById(erp_employee_id);
				String erp_employee_name = (String) employee1.get("name");
				Map<String, Object> employee2 = employeeMapper.selectEmployeeById(approverId);
				String approverName = (String) employee2.get("name");
				map2.remove("superLeaderUserId");
				map2.put("erp_employee_name", erp_employee_name);
				map2.put("approverName", approverName);
			}
		} catch (Exception e) {
			logger.error("调用findEmpNameByEIdMap方法出错" + e.getMessage());
			return RestUtils.returnFailure("调用findEmpNameByEIdMap方法出错" + e.getMessage());
		}
		return RestUtils.returnSuccess(listmap);
	}

	/**
	 * Description: 根据员工Id导出员工简历到临时路径
	 *
	 * @return 文件路径+文件名称
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月01日 上午10:03:39
	 * @Update Date: 2019年04月18日 下午15:51:43
	 */
	public String createFileForExportEmployeeResume(Integer employeeId, String fileType) {
		logger.info("进入createFileForExportEmployeeResume方法，参数是：employeeId={},fileType={}",employeeId,fileType);
		try {
			/*
			 * 数据库查询需要的数据
			 */
			Map<String, Object> employeeMap = employeeMapper.findEmployeeDetail(employeeId);
			logger.info("（员工基本信息）employeeMap=",employeeMap);
			// 有些员工是直接导入的，没有经过招聘流程，所以没有简历Id
			Integer resumeId = Integer
					.valueOf(String.valueOf(employeeMap.get("resumeId") == null ? -1 : employeeMap.get("resumeId")));
			Map<String, Object> resumeMap = resumeMapper.selectResumeDetail(resumeId);
			logger.info("（员工简历信息）resumeMap={}",resumeMap);
			List<Map<String, Object>> educationExperience = employeeMapper.findAllEducationByEmp(employeeId);
			logger.info("（教育经历）educationExperience={}",educationExperience);
			List<Map<String, Object>> workExperience = employeeMapper.findAllWorkExperienceByEmp(employeeId);
			logger.info("（工作经历）workExperience={}",workExperience);
			List<Map<String, Object>> projectExperience = employeeMapper.findAllProjectExperienceByEmp(employeeId);
			logger.info("（项目经历）projectExperience={}",projectExperience);
			List<Map<String, Object>> technicaExpertise = employeeMapper.findAllTechnicaExpertiseByEmp(employeeId);
			logger.info("（技能特长）technicaExpertise={}",technicaExpertise);
			List<Map<String, Object>> certificate = employeeMapper.findAllCertificateByEmp(employeeId);
			logger.info("（证书）certificate={}",certificate);

			/** 用于组装word页面需要的数据 */
			Map<String, Object> dataMap = new HashMap<>();

			/** 组装数据 */
			dataMap.put("employeeName", employeeMap.get("name") == null ? "" : employeeMap.get("name"));// 姓名
			dataMap.put("sex", employeeMap.get("sex") == null ? "" : employeeMap.get("sex"));// 性别
			dataMap.put("degree", employeeMap.get("education") == null ? "" : employeeMap.get("education"));// 学历
			dataMap.put("school", employeeMap.get("school") == null ? "" : employeeMap.get("school"));// 学校
			dataMap.put("major", employeeMap.get("major") == null ? "" : employeeMap.get("major"));// 专业
			dataMap.put("positionTile", "");// 职称资格
			dataMap.put("takeJobTime", employeeMap.get("takeJobTime") == null ? "" : employeeMap.get("takeJobTime"));// 参加工作时间
			dataMap.put("post", employeeMap.get("position") == null ? "" : employeeMap.get("position"));// 职务
			dataMap.put("postInProject", "");// 拟在本项目中承担职务
			
			String idCardNumber = employeeMap.get("idCardNumber") == null ? "" : (String) employeeMap.get("idCardNumber");//身份证号码
			dataMap.put("idCardNumber", idCardNumber);
			
			/*
			 * 如果简历生日为空，那么从身份证号中提取生日
			 */
			String birthday = "";// 生日
			if(resumeMap == null || resumeMap.get("birthday") == null) {
				if("".equals(idCardNumber)) {
					birthday = "";
				}else {
					birthday = idCardNumber.substring(6, 10) + "-" + idCardNumber.substring(10, 12);
				}
			}else {
				birthday = (String) resumeMap.get("birthday");
			}
			dataMap.put("birthday", birthday);
			
			for (Map<String, Object> educationExperienceMap : educationExperience) {
				String endTime = educationExperienceMap.get("endTime") == null ? ""
						: String.valueOf(educationExperienceMap.get("endTime"));
				educationExperienceMap.put("educationTime", String.valueOf(educationExperienceMap.get("startTime")) + "-" + endTime);
				educationExperienceMap.put("educationSchool", educationExperienceMap.get("school"));
				educationExperienceMap.put("educationDegree", educationExperienceMap.get("degree"));
			}
			dataMap.put("educationExperience", educationExperience);// 教育经历

			for (Map<String, Object> workExperienceMap : workExperience) {
				String endTime = workExperienceMap.get("endTime") == null ? ""
						: String.valueOf(workExperienceMap.get("endTime"));
				workExperienceMap.put("workTime", String.valueOf(workExperienceMap.get("startTime")) + "-" + endTime);
				workExperienceMap.put("workCompany", workExperienceMap.get("company"));
				workExperienceMap.put("workPosition", workExperienceMap.get("position"));
			}
			dataMap.put("workExperience", workExperience);// 工作经历

			for (Map<String, Object> projectExperienceMap : projectExperience) {
				String endTime = projectExperienceMap.get("endTime") == null ? ""
						: String.valueOf(projectExperienceMap.get("endTime"));
				projectExperienceMap.put("projectTime", String.valueOf(projectExperienceMap.get("startTime")) + "-" + endTime);
				projectExperienceMap.put("projectName", projectExperienceMap.get("projectName"));
				projectExperienceMap.put("projectPost", projectExperienceMap.get("post"));
				projectExperienceMap.put("projectDescription", projectExperienceMap.get("description"));
			}
			dataMap.put("projectExperience", projectExperience);// 项目经历

			for (Map<String, Object> technicaExpertiseMap : technicaExpertise) {
				technicaExpertiseMap.put("technicalName", technicaExpertiseMap.get("technicalName"));
				technicaExpertiseMap.put("technicalQualification", technicaExpertiseMap.get("qualification"));
			}
			dataMap.put("technicaExpertise", technicaExpertise);// 技能特长

			StringBuilder certificateNames = new StringBuilder();
			List<Map<String, Object>> certificateFileList = new ArrayList<>();
			for (int i = 0; i < certificate.size(); i++) {
				if (i == 0) {
					certificateNames.append(certificate.get(0).get("certificateName"));
				} else {
					certificateNames.append("、" + certificate.get(i).get("certificateName"));
				}
				if (certificate.get(i).get("filename") != null) {
					Map<String, Object> certificateFileMap = new HashMap<>();
					certificateFileMap.put("index", i);
					String imgString = wordUtil
							.getImageStr(DicConstants.CERTIFICATE_PATH + "/" + certificate.get(i).get("filename"));
					certificateFileMap.put("imgString", imgString);
					certificateFileList.add(certificateFileMap);
				}
			}
			dataMap.put("qualifications", certificateNames.toString());// 资质
			dataMap.put("certificateNames", certificateNames.toString());// 证书名称
			dataMap.put("certificateFileList", certificateFileList);// 证书图片
			
			
			dataMap.put("educationExperienceName", "教育经历");// 教育经历名称
			List<Map<String, Object>> educationExperienceFileList = new ArrayList<>();
			for (int i = 0; i < educationExperience.size(); i++) {
				Map<String,Object> educationExperienceMap = educationExperience.get(i);
				if (educationExperienceMap.get("filename") != null) {
					Map<String, Object> educationExperienceFileMap = new HashMap<>();
					educationExperienceFileMap.put("index", i+"filename");
					String imgString = wordUtil
							.getImageStr(DicConstants.EDUCATION_EXPERIENCE_PATH + "/" + educationExperienceMap.get("filename"));
					educationExperienceFileMap.put("imgString", imgString);
					educationExperienceFileList.add(educationExperienceFileMap);
				}
				
				if (educationExperienceMap.get("filename1") != null) {
					Map<String, Object> educationExperienceFileMap = new HashMap<>();
					educationExperienceFileMap.put("index", i+"filename1");
					String imgString = wordUtil
							.getImageStr(DicConstants.EDUCATION_EXPERIENCE_PATH + "/" + educationExperienceMap.get("filename1"));
					educationExperienceFileMap.put("imgString", imgString);
					educationExperienceFileList.add(educationExperienceFileMap);
				}
			}
			dataMap.put("educationExperienceFileList", educationExperienceFileList);// 教育经历图片
			
			
			dataMap.put("empPictureName", "个人照片");// 个人照片名称
			String empPictureFileString = "";// 个人照片的BASE64字符串
			if (employeeMap.get("empPictureFileName")!=null) {
				empPictureFileString = wordUtil
						.getImageStr(DicConstants.EMPLOYEE_FILE_PATH + "/" + employeeMap.get("empPictureFileName"));
			}
			dataMap.put("empPictureFileString", empPictureFileString);
			
			
			dataMap.put("frontIdCardFileName", "身份证正面照片");// 身份证正面照片
			String frontIdCardFileString = "";// 身份证正面照片的BASE64字符串
			if (employeeMap.get("frontIdCardFileName")!=null) {
				frontIdCardFileString = wordUtil
						.getImageStr(DicConstants.EMPLOYEE_FILE_PATH + "/" + employeeMap.get("frontIdCardFileName"));
			}
			dataMap.put("frontIdCardFileString", frontIdCardFileString);
			
			
			dataMap.put("backIdCardFileName", "身份证反面照片");// 身份证反面照片
			String backIdCardFileString = "";// 身份证反面照片的BASE64字符串
			if (employeeMap.get("backIdCardFileName")!=null) {
				backIdCardFileString = wordUtil
						.getImageStr(DicConstants.EMPLOYEE_FILE_PATH + "/" + employeeMap.get("backIdCardFileName"));
			}
			dataMap.put("backIdCardFileString", backIdCardFileString);
			
			
			String exportPath; // 导出的文件路径
			String fileOnlyName; // 文件唯一名称
			// String filePathAndName;//文件的路径加名称
			if ("doc".equals(fileType)) {
				exportPath = DicConstants.EXPORT_RESUME_TEMP_PATH;
				fileOnlyName = String.valueOf(employeeMap.get("name")) + "_" + System.currentTimeMillis() + ".doc";
				wordUtil.createFileByTemplate(dataMap, "exportResumeFromDoc.ftl", exportPath, fileOnlyName);
			} else if ("html".equals(fileType)) {
				exportPath = DicConstants.PREVIEW_RESUME_TEMP_PATH;
				fileOnlyName = String.valueOf(employeeMap.get("name")) + "_" + System.currentTimeMillis() + ".html";
				wordUtil.createFileByTemplate(dataMap, "exportResumeFromHtml.ftl", exportPath, fileOnlyName);
			} else {
				return "error";
			}

			// 将文件路径放入缓存中，避免短时间内大量新增文件
			// stringRedisTemplate.opsForValue().set("exportWord_"+employeeId,
			// filePathAndName, 30, TimeUnit.MINUTES);

			return exportPath + "/" + fileOnlyName;
		} catch (Exception e) {
			logger.error("createWordForExportEmployeeResume方法出现异常：" + e.getMessage(), e);
			return "error";
		}
	}

	/**
	 * Description: 根据员工Id导出员工简历到用户电脑
	 *
	 * @return 文件流
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月28日 上午09:42:26
	 */
	public RestResponse exportResumeByEmployeeId(Integer employeeId) {
		logger.info("进入exportResumeByEmployeeId方法，参数是：employeeId={}",employeeId);
		try {
//			//先查看redis缓存中有没有该员工简历的word文件
//			String wordPathAndName = stringRedisTemplate.opsForValue().get("exportWord_"+employeeId);
//			logger.info("redis中存储的word路径："+wordPathAndName);
//			if(wordPathAndName==null) {
//				wordPathAndName = this.createWordForExportEmployeeResume(employeeId);
//				//根据员工ID创建word文件。成功时，返回文件路径；失败时，返回"error"。
//		        if("error".equals(wordPathAndName)) {
//		        	return RestUtils.returnSuccessWithString("生成word文件时发生了异常！");
//		        }
//			}

			String wordPathAndName = this.createFileForExportEmployeeResume(employeeId, "doc");
			if ("error".equals(wordPathAndName)) {
				return RestUtils.returnSuccessWithString("生成word文件时发生异常！");
	        }
			fileUtils.downloadFileToComputer(wordPathAndName,"application/octet-stream");
			//return RestUtils.returnSuccessWithString("OK");
			//此处返回null，不会打印错误日志
			return null;
		} catch (Exception e) {
			logger.error("exportResumeByEmployeeId方法出现异常：" + e.getMessage(), e);
			HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getResponse();
			response.setHeader("error", "unknown");
			return RestUtils.returnFailure("方法出现异常，导致操作失败！");
		}
	}

	/**
	 * Description: 根据员工Id导出员工简历（支持批量）到用户电脑
	 *
	 * @return 文件流
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月28日 上午09:42:26
	 */
	public RestResponse exportResumeMoreByEmployeeIds(List<Integer> employeeIds) {
		logger.info("进入exportResumeMoreByEmployeeIds方法，参数是：employeeIds={}",employeeIds);
		try {
			// 导出一个人的简历，不需要压缩
			if (employeeIds.size() == 1) {
				return this.exportResumeByEmployeeId(Integer.valueOf(employeeIds.get(0)));
			}

			List<File> srcFiles = new ArrayList<>();
			for (Integer employeeId : employeeIds) {
				String wordPathAndName = this.createFileForExportEmployeeResume(employeeId, "doc");
				if ("error".equals(wordPathAndName)) {
					logger.error("生成word文件时发生异常！");
				} else {
					srcFiles.add(new File(wordPathAndName));
				}
			}
			String exportPathAndName = DicConstants.PACK_RESUME_TEMP_PATH+"/resumes"+System.currentTimeMillis()+".zip";
			fileUtils.zipFiles(srcFiles, new File(exportPathAndName));
			fileUtils.downloadFileToComputer(exportPathAndName,"application/octet-stream");
			//return RestUtils.returnSuccessWithString("OK");
			//此处返回null，不会打印错误日志
			return null;
		} catch (Exception e) {
			logger.error("exportResumeMoreByEmployeeIds方法出现异常：", e.getMessage(), e);
			HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getResponse();
			response.setHeader("error", "unknown");
			return RestUtils.returnFailure("方法出现异常，导致操作失败！");
		}
	}

	/**
	 * Description: 根据员工Id预览员工简历（通过依赖包中的方法）
	 * 注意：通过redis缓存可以解决频繁生成相同文件的问题，但是当有信息更新的时候，导出的文件依然是旧的，所以暂时不用redis缓存文件路径
	 * 
	 * @param employeeId 员工Id
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月01日 上午09:37:44
	 */
	public RestResponse previewResumeByEmployeeId(Integer employeeId) {
		logger.info("进入previewResumeByEmployeeId方法，参数是：employeeId=" + employeeId);
		try {
//    		//先查看redis缓存中有没有该员工简历的Html文件，如果有直接下载
//			String htmlPathAndName = stringRedisTemplate.opsForValue().get("previewHtml_"+employeeId);
//			logger.info("redis中存储的html路径："+htmlPathAndName);
//			if(htmlPathAndName!=null) {
//				String result = this.downloadFileToComputer(htmlPathAndName);
//		        if("OK".equals(result)) {
//		        	return RestUtils.returnSuccessWithString("OK");
//		        }else {
//		        	return RestUtils.returnSuccessWithString("下载html文件时发生异常！");
//		        }
//			}

//			//将html文件路径存入redis
//			stringRedisTemplate.opsForValue().set("previewHtml_"+employeeId, outPath, 30, TimeUnit.MINUTES);
			String htmlPathAndName = this.createFileForExportEmployeeResume(employeeId,"html");
			fileUtils.downloadFileToComputer(htmlPathAndName,"text/html");
			//return RestUtils.returnSuccessWithString("OK");
			//此处返回null，不会打印错误日志
			return null;
		} catch (Exception e) {
			logger.error("previewResumeByEmployeeId异常：", e.getMessage(), e);
			HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getResponse();
			response.setHeader("error", "unknown");
			return RestUtils.returnFailure("方法发生异常，导致预览简历失败！");
		}
	}

	/**
	 * Description: 定时任务每天1点半删除预览员工简历时产生的临时文件
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月05日 16:15:09
	 */
	public void automaticDeleteTempFilesScheduler() {
		logger.info("进入automaticDeleteTempFilesScheduler方法，无参数");
		try {
			//清理导出员工简历的临时路径
			fileUtils.deleteFileByPath(DicConstants.EXPORT_RESUME_TEMP_PATH);
			//清理导出多个员工简历压缩包的临时路径
			fileUtils.deleteFileByPath(DicConstants.PACK_RESUME_TEMP_PATH);
			//清理预览员工简历的临时路径
			fileUtils.deleteFileByPath(DicConstants.PREVIEW_RESUME_TEMP_PATH);
			logger.info("automaticDeleteTempFilesScheduler执行成功！");
		} catch (Exception e) {
			logger.error("automaticDeleteTempFilesScheduler发生异常 ：", e.getMessage(), e);
		}
	}

	public RestResponse addFrequentContacts(Map<String, Object> map) {
		logger.info("参数是：map=" + map);
		try {
			System.out.println("..." + map.get("name"));
			employeeMapper.addFrequentContacts(map);
		} catch (Exception e) {
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常addfrequentContacts()" + e.getMessage());
		}

		return RestUtils.returnSuccess("添加成功");
	}

	public RestResponse updateFrequentContacts(Map<String, Object> map) {
		// TODO Auto-generated method stub
		try {
			employeeMapper.updateFrequentContacts(map);
		} catch (Exception e) {
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常updateFrequentContacts()" + e.getMessage());
		}

		return RestUtils.returnSuccess("修改成功");
	}

	public RestResponse deleteFrequentContacts(Integer id) {
		// TODO Auto-generated method stub
		try {
			employeeMapper.deleteFrequentContacts(id);
		} catch (Exception e) {
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常deleteFrequentContacts()" + e.getMessage());
		}

		return RestUtils.returnSuccess("删除成功");
	}

	public RestResponse findFrequentContacts() {
		// TODO Auto-generated method stub
		List<Map<String, Object>> frequentContacts = null;
		try {
			frequentContacts = employeeMapper.findFrequentContacts();
		} catch (Exception e) {
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常findFrequentContacts()" + e.getMessage());
		}

		return RestUtils.returnSuccess(frequentContacts);
	}

	@Transactional(rollbackFor = Exception.class)
	public RestResponse addExpenseReimbursement(MultipartFile PhotoFile, MultipartFile[] attachment, String title,
			String notes) {
		logger.info("addExpenseReimbursement方法开始执行，传递参数1：MultipartFile类型文件" + "参数2：MultipartFile类型文件数组");
		try {
			String originalResumeName = null;
			String upfilePath = null;
			if (PhotoFile != null) { // 获得原始文件名 String
				originalResumeName = PhotoFile.getOriginalFilename();
				upfilePath = upfile(PhotoFile, originalResumeName);
				System.out.println("upfilePath:" + upfilePath);
			}
			ExpenseReimbursementVo expenseReimbursementVo = new ExpenseReimbursementVo();
			expenseReimbursementVo.setTitle(title);
			expenseReimbursementVo.setNotes(notes);
			expenseReimbursementVo.setPhotoPath(upfilePath);
			employeeMapper.addExpenseReimbursement(expenseReimbursementVo);

			Integer id = expenseReimbursementVo.getId();
			Map<String, Object> map1 = new HashMap<>();
			if (attachment != null) {
				for (MultipartFile multipartFile : attachment) {
					String originalFilename = multipartFile.getOriginalFilename();
					String upfilePath1 = upfile(multipartFile, originalFilename);
					map1.put("expReimId", id);
					map1.put("path", upfilePath1);
					employeeMapper.addAttachmentPath(map1);
				}
			}
		} catch (Exception e) {
			 
			logger.info("addExpenseReimbursement方法出现异常：" + e.getMessage());
		}
		return RestUtils.returnSuccess("添加成功");
	}

	public String upfile(MultipartFile PhotoFile, String originalResumeName) {
		String fileName = null;
		try {
			// 存储的路径
			String filePath = DicConstants.EXPENSEREIMBURSEMENT_PATH;

			String[] upfile = originalResumeName.split("\\.");
			String upfileMc = upfile[0];
			String upfileHz = upfile[1];

			originalResumeName = upfileMc + String.valueOf(System.currentTimeMillis()) + "." + upfileHz;
			fileName = filePath + "/" + originalResumeName;
			// 判断是否有此路径的文件夹，如果没有，就创建
			File isFile = new File(filePath);
			if (!isFile.exists()) {
				isFile.mkdirs();
			}
			File file = new File(fileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(PhotoFile.getBytes());
				out.flush();
				out.close();
			} catch (FileNotFoundException e1) {
				logger.error("出现异常："+e1.getMessage(),e1);
			}
		} catch (IOException e) {
			 
			logger.info("费用报销 upfile方法出现异常：" + e.getMessage());
		}
		return fileName;
	}

	public RestResponse findExpenseReimbursement() {
		// TODO Auto-generated method stub
		List<Map<String, Object>> expenseReimbursement = employeeMapper.findExpenseReimbursement();
		for (Map<String, Object> map : expenseReimbursement) {
			if (map != null) {
				if (map.get("photoPath") != null) {
					String photoFile = map.get("photoPath").toString();
					String imageStr = wordUtil.getImageStr(photoFile);
					map.put("imageStr", imageStr);
					String imageName = photoFile.substring(photoFile.lastIndexOf("/") + 1);
					map.put("imageName", imageName);
				} else {
					map.put("imageStr", null);
					map.put("imageName", null);
				}
				Integer id = Integer.valueOf(map.get("id").toString());
				List<String> attachmentPathList = employeeMapper.findAttachmentPath(id);
				List<Map<String, Object>> newAttachmentPathList = new ArrayList<>();
				if (attachmentPathList != null) {
					for (String attachmentPath : attachmentPathList) {
						Map<String, Object> map2 = new HashMap<>();
						String attachmentName = attachmentPath.substring(attachmentPath.lastIndexOf("/") + 1);
						map2.put("attachmentName", attachmentName);
						map2.put("attachmentPath", attachmentPath);
						newAttachmentPathList.add(map2);
					}
					map.put("attachmentPathList", newAttachmentPathList);
				}
			}
		}
		return RestUtils.returnSuccess(expenseReimbursement);
	}

	public RestResponse deleteExpenseReimbursementByMap(Map<String, Object> map) {
		try {
			Object id = map.get("id");
			Object photoPath = map.get("photoPath");
			if (photoPath != null) {
				boolean photoPathR = new File(photoPath.toString()).delete();
				logger.info("在deleteExpenseReimbursementByMap()方法中" + photoPath.toString() + "  费用报销  图片：--->该位置文件"
						+ photoPathR + "删除");
			}
			if (map.get("attachmentPathList") != null) {
				List<Map<String, Object>> attachmentPathList = (List<Map<String, Object>>) map
						.get("attachmentPathList");
				for (Map<String, Object> map2 : attachmentPathList) {
					Object attachmentPath = map2.get("attachmentPath");
					if (attachmentPath != null) {
						boolean attachmentPathR = new File(attachmentPath.toString()).delete();
						logger.info("在deleteExpenseReimbursementByMap()方法中" + attachmentPath.toString()
								+ "  费用报销  附件：--->该位置问价" + attachmentPathR + "删除");
						String htmlDatePathAndName = (attachmentPath.toString()).substring(0, (attachmentPath.toString()).lastIndexOf(".")) + ".html";
						File testFile = new File(htmlDatePathAndName); 
						 if(testFile .exists()) {	  
							 boolean attachmentPathR1 = new File(htmlDatePathAndName).delete();
								logger.info("在deleteExpenseReimbursementByMap()方法中" + htmlDatePathAndName
										+ "  费用报销  附件html文件：--->该位置问价" + attachmentPathR1 + "删除");
						 }
					}
				}
			}
			Integer id1 = Integer.parseInt(id.toString());
			employeeMapper.deleteAttachmentByExpReimId(id1);
			
			 
			 
			employeeMapper.deleteExpenseReimbursementById(id1);
		} catch (Exception e) {
			// TODO: handle exception
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常deleteExpenseReimbursement()" + e.getMessage());
		}
		return RestUtils.returnSuccess("删除成功");
	}

	public RestResponse updateExpenseReimbursementByMap(ExpenseReimbursement expenseReimbursement) {
		try {
			logger.info("updateExpenseReimbursementByMap()方法中：" + "expenseReimbursement:" + expenseReimbursement);
			List<String> deleteFileList = expenseReimbursement.getDeleteFileList();
			Integer id = expenseReimbursement.getId();
			String notes = expenseReimbursement.getNotes();
			MultipartFile PhotoFile = expenseReimbursement.getPhotoFile();
			MultipartFile[] attachment = expenseReimbursement.getAttachment();
			String title = expenseReimbursement.getTitle();
			if (deleteFileList != null) {
				for (String path : deleteFileList) {
					employeeMapper.deleteExpenseReimbursementById2(id, path);
					boolean photoPathR = new File(path).delete();
					logger.info("updateExpenseReimbursementByMap()方法中：" + path + "  费用报销  图片：--->该位置文件" + photoPathR
							+ "删除");
				}
			}
			Map<String, Object> map1 = new HashMap<>();
			if (attachment != null) {
				for (MultipartFile multipartFile : attachment) {
					String originalFilename = multipartFile.getOriginalFilename();
					String upfilePath1 = upfile(multipartFile, originalFilename);
					map1.put("expReimId", id);
					map1.put("path", upfilePath1);
					employeeMapper.addAttachmentPath(map1);
					
				}
			}
			String originalResumeName = null;
			String upfilePath = null;
			Map<String, Object> map = new HashMap<>();
			map.put("title", title);
			map.put("notes", notes);
			map.put("id", id);
			if (PhotoFile != null) { // 获得原始文件名 String
				originalResumeName = PhotoFile.getOriginalFilename();
				upfilePath = upfile(PhotoFile, originalResumeName);
				map.put("photoPath", upfilePath);
				String oldPhotoPath = employeeMapper.findExpenseReimbursementById(id);
				boolean photoPathR2 = new File(oldPhotoPath).delete();
				logger.info("updateExpenseReimbursementByMap()方法中：" + oldPhotoPath + "  费用报销  图片：--->旧的图片该位置文件" + photoPathR2
						+ "删除");
			}
		
			
			employeeMapper.updateExpenseReimbursementByMap(map);
		} catch (Exception e) {
			// TODO: handle exception
			 
			logger.error("方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常updateExpenseReimbursementByMap()" + e.getMessage());
		}
		return RestUtils.returnSuccess("修改成功");
	}

	public RestResponse previewAttachment(String attachmentPath,String token) {
		// TODO Auto-generated method stub
		try {
			logger.info("进入previewAttachment方法，参数是：attachmentPath = " + attachmentPath);
			List<String> wordType = new ArrayList<>();// word类型
			wordType.add("doc");
			wordType.add("docx");
			List<String> htmlType = new ArrayList<>();// html类型
			htmlType.add("html");
			htmlType.add("htm");
			htmlType.add("mht");
			htmlType.add("mhtml");

			String fileExtension = attachmentPath.substring(attachmentPath.lastIndexOf(".") + 1);
			if (!wordType.contains(fileExtension)) {
				String result = "OK";
				try {
					if ("pdf".equals(fileExtension)) {// pdf格式
						fileUtils.downloadFileToComputer(attachmentPath, "application/octet-stream");
					} else if (htmlType.contains(fileExtension)) {// html格式
						fileUtils.downloadFileToComputer(attachmentPath, "text/html");
					} else {// 文本格式或者其他格式
					    fileUtils.downloadFileToComputer(attachmentPath, "text/html");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					 
				}
				if ("OK".equals(result)) {
					return RestUtils.returnSuccess("非word简历不需要转换格式");
				} else {
					return RestUtils.returnSuccess(result);
				}
			}
			String htmlDatePathAndName = attachmentPath.substring(0, attachmentPath.lastIndexOf(".")) + ".html";// html日期路径+文件名
			String cacheRedis = stringRedisTemplate.opsForValue().get(htmlDatePathAndName);
			logger.info("htmlDatePathAndName=" + htmlDatePathAndName + ",cacheRedis=" + cacheRedis);
			/*
			 * 如果redis中有该html文件，则直接访问服务器上的html文件，无须再进行转换
			 */
			if (cacheRedis != null) {
				fileUtils.downloadFileToComputer(htmlDatePathAndName, "text/html");
				return RestUtils.returnFailure("ok");
			}


			try {
				File inFile = new File(attachmentPath);
				InputStream inStream = new FileInputStream(inFile);
				
				if(attachmentPath.endsWith("doc")){
					wordToHtmlUtil.doc2Html(inStream, htmlDatePathAndName);
					
				}else if (attachmentPath.endsWith("docx")){
					wordToHtmlUtil.docx2Html(inStream, htmlDatePathAndName);
				}else {
					logger.info("该文件不是word格式！");
				}
				inStream.close();	
			} catch (Exception e) {
				logger.info("wordToHtml发生异常："+e.getMessage(),e);
				return RestUtils.returnFailure("转换html过程中发生异常！");
			}
			fileUtils.downloadFileToComputer(htmlDatePathAndName, "text/html");
			  
			  //将html文件路径存入redis
			  
		   stringRedisTemplate.opsForValue().set(htmlDatePathAndName,htmlDatePathAndName, 30, TimeUnit.MINUTES);
		   logger.info("redis中存储的html路径："+htmlDatePathAndName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 
		} 
		  return RestUtils.returnSuccessWithString("OK");
	}

	public String downloadAttachment(String attachmentPath, String contentType) {
		String attachmentName = attachmentPath.substring(attachmentPath.lastIndexOf("/") + 1);
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		try {
			response.setHeader("filename",  new String(attachmentName.getBytes(),"utf-8"));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			
		}
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		byte[] buffer = new byte[1024];
		FileInputStream fis = null; // 文件输入流
		BufferedInputStream bis = null;

		OutputStream os = null; // 输出流
		try {
			os = response.getOutputStream();
			fis = new FileInputStream(attachmentPath);
			bis = new BufferedInputStream(fis);
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer);
				i = bis.read(buffer);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			 
		}
		try {
			bis.close();
			fis.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			
		}
		return "OK";
	}

	public RestResponse downloadAttachment(String attachmentPath) {
		// TODO Auto-generated method stub
		try {
			fileUtils.downloadFileToComputer(attachmentPath, "application/octet-stream");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 
		}
		return RestUtils.returnSuccess("ok");
	}
	
	public RestResponse findFirstDepAndEmp(Map<String,Object> listMap) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> date = (List<Map<String, Object>>) listMap.get("li");
		List<Map<String, Object>> newDate = new ArrayList<>();
		try {
			Integer flag = 0; 
			//第一个for循环 为了 我往list中 通过数据库查询 各个项目组有多少人数 并且 根据id查询name
			for(int i = 0; i < date.size(); i++) {
				Map<String, Object> map = date.get(i);
				Integer groupId = Integer.valueOf(String.valueOf(map.get("groupId")));
				Integer number = employeeMapper.findPeopleCountByGroupId(groupId);
				if(number != null) {
					map.put("number", number);
				}else {
					map.put("number", 0);
				}
				if(map.get("department") != null) {
					Integer departmentId = Integer.valueOf(String.valueOf(map.get("department")));
					ErpDepartment erpDepartment = erpDepartmentMapper.findByDepartmentId(departmentId);
					String departmentName = erpDepartment.getDepartmentName();
					map.put("firstDepartment", departmentName);
				}else {
					map.put("firstDepartment", null);
				}
				if(map.get("manager") != null) {
					Integer managerId = Integer.valueOf(String.valueOf(map.get("manager")));
					String name = employeeMapper.findSimpleEmployeeById(managerId);
					map.put("projectManager", name);
				}else {
					map.put("projectManager", null);
				}
			}
			
			//第二个循环 按客户 切割 list 分成多个list，再在list中根据人数多少进行排序  最终 把分成的多个list 在全部放到一个list中 返回给前端 
			for(int i = 0; i < date.size(); i++) {
				Map<String, Object> map = date.get(i);
				if(i+1 < date.size() ) {
					Map<String, Object> map1 = date.get(i+1);
					if(map.get("customer") != map1.get("customer") || i == (date.size()-2)) {
						List<Map<String, Object>> subList = null;
						if(i == (date.size()-2)) {
							subList = date.subList(flag, i+2);
						}else {
							subList = date.subList(flag, i+1);
						}
						flag = i+1;
						  Collections.sort(subList, new Comparator<Map<String, Object>>() {
					            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					                Integer number1 = Integer.valueOf(o1.get("number").toString()) ;
					                Integer number2 = Integer.valueOf(o2.get("number").toString()) ; 
					                return number2.compareTo(number1);
					            }
					        });
						  newDate.addAll(subList);
					}
				}
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return RestUtils.returnSuccess(newDate);
	}
	
	public RestResponse findProGroDetailsByGId(Integer id) {
		List<Map<String, Object>> li = new ArrayList<>();
		try {
			List<Map<String, Object>> findPositionPeopByGroupId = employeeMapper.findPositionPeopByGroupId(id);
			for (Map<String, Object> map : findPositionPeopByGroupId) {
				Object positionName = map.get("positionName");
				Map<String,Object> map1 = new HashMap<>();
				map1.put("name", map.get("name"));
				if(positionName != null){
					map1.put("positionName", positionName);
				}else {
					Object position = map.get("position");
					map1.put("positionName", position);
				}
				li.add(map1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("该findProGroDetailsByGId中 出现异常 异常为 ：" + e);
		}
		return RestUtils.returnSuccess(li);
	}

	public RestResponse findProjectMessHr(Map<String, Object> listMap) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> li = null;
		try {
			logger.info("findProjectMessHr 输入的参数 listMap : " + listMap);
			li = (List<Map<String, Object>>) listMap.get("li");
			if(li != null) {
				for (Map<String, Object> map : li) {
					//下面这个if是供项目模块 根据项目经理名称 查询名称
					if(map.get("manager") != null) {
						Integer employeeId = Integer.valueOf(String.valueOf(map.get("manager")));
						String name = employeeMapper.findSimpleEmployeeById(employeeId);
						map.put("projectManager", name);
					}
					//下面两个if是供售前项目 根据id查询名字
					if(map.get("sell_manager") != null) {
						Integer employeeId = Integer.valueOf(String.valueOf(map.get("sell_manager")));
						String name = employeeMapper.findSimpleEmployeeById(employeeId);
						map.put("salesManager", name);
					}
					
					if(map.get("pre_sell_manager") != null) {
						Integer employeeId = Integer.valueOf(String.valueOf(map.get("pre_sell_manager")));
						String name = employeeMapper.findSimpleEmployeeById(employeeId);
						map.put("preSalesManager", name);
					}
					if(map.get("processor") != null) {
						Integer employeeId = Integer.valueOf(String.valueOf(map.get("employeeId")));
						String employeeName = employeeMapper.findSimpleEmployeeById(employeeId);
						map.put("employeeName", employeeName);
						Integer processorId = Integer.valueOf(String.valueOf(map.get("processor")));
						String name = employeeMapper.findSimpleEmployeeById(processorId);
						Map<String, Object> selectByEmployeeIdForlx = employeeMapper.selectByEmployeeIdForlx(processorId);
						map.put("firstDepartment",selectByEmployeeIdForlx.get("firstDepartment"));
						map.put("processorName", name);
						map.put("secondDepartment",selectByEmployeeIdForlx.get("secondDepartment"));
					}
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return RestUtils.returnSuccess(li);
	}
	
	/**
	 * @author songzixuan
	 * @date 2019-3-25
	 * @description 根据员工字符串查询员工
	 */
	public RestResponse findEmployeeByEmpIdArray(String employeeId) {
		try {
			logger.info("findEmployeeByEmpIdArray方法开始执行,参数employeeList:"+employeeId);
			List<Map<String,Object>> employeeList = null;
					//employeeMapper.findEmployeeByEmpIdArray(employeeId);
			
			Map<String, Object> employee = null;
			MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<String, Object>();
			multiValueMap.add("userId", employeeId);
			String userPhone = null;// 个人电话号
			String username = null; // 用户名-邮箱
			Integer userId = null; // 用户名编号
			String urlForHr = null;
			HttpHeaders requestHeadersForHr = null;
			HttpEntity<String> requestForHr = null;
			ResponseEntity<RestResponse> responseForHr = null;
			List<Map<String,Object>> erpUserList = null;
			urlForHr = protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserIdArray?userId="+employeeId;
			requestHeadersForHr = new HttpHeaders();
			requestHeadersForHr.add("token", request.getHeader("token"));
		    requestForHr = new HttpEntity<String>(null,requestHeadersForHr);
		    
		    responseForHr = restTemplate.exchange(urlForHr,HttpMethod.GET,requestForHr,RestResponse.class);
		    //跨工程调用响应失败
		    if(200 != responseForHr.getStatusCodeValue() || !"200".equals(responseForHr.getBody().getStatus())) {
		    	logger.error("调用权限工程发生异常，响应失败！"+responseForHr);
		    	return RestUtils.returnFailure("调用权限工程发生异常，响应失败！");
			}
			//解析请求的结果
		    erpUserList = (List<Map<String, Object>>) responseForHr.getBody().getData();
			
			employeeList = employeeMapper.findEmployeeByEmpIdArray(employeeId);
			if(erpUserList!=null&&erpUserList.size()>0) {
				for(Map<String,Object> employee1 : employeeList) {
					for(Map<String,Object> erpuser : erpUserList) {
						if (employee1.get("employeeId").equals(erpuser.get("userId"))){
							userPhone = (String)erpuser.get("userPhone");
							username = (String)erpuser.get("username");
							userId = (Integer)erpuser.get("userId");
							employee1.put("userPhone", userPhone);
							employee1.put("username", username);
							employee1.put("userId", userId);
							break;
						}
					}
				}
			}
			return RestUtils.returnSuccess(employeeList);
		}catch(Exception e) {
			logger.error("findEmployeeByEmpIdArray方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，查询员工数据失败");
		}
	}
	
	/**
	 * @author songzixuan
	 * @date 2019-3-28
	 * @description 根据员工编号查询员工的部门经理信息
	 */
	public RestResponse findDepartmentManagerByEmpId(Integer employeeId) {
		try {
			Map<String,Object> map = employeeMapper.findDepartmentManagerByDepId(employeeId);
			return RestUtils.returnSuccess(map);
		}catch(Exception e) {
			logger.error("findDepartmentManagerByEmpId方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，查询员工数据失败");
		}
	}
	
	/**
	 * @author songzixuan
	 * @date 2019-3-28
	 * @description 根据员工编号查询员工的一级部门经理信息
	 */
	public RestResponse findDepartmentManager() {
		try {
			List<Map<String,Object>> list = null;
			list = employeeMapper.findDepartmentManager();
			return RestUtils.returnSuccess(list);
		}catch(Exception e) {
			logger.error("findDepartmentManager方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，查询员工数据失败");
		}
	}
	
	public RestResponse findSimpleEmpById(Integer employeeId) {
		
		try {
			Map<String, Object> employeeById = employeeMapper.selectEmployeeById(employeeId);
			return RestUtils.returnSuccess(employeeById);
		}catch(Exception e) {
			logger.error("findSimpleEmpById方法出现异常:"+e);
			return RestUtils.returnFailure("方法发生异常，查询员工数据失败");
		}
	}
	
	
	/**
	 * @author songzixuan
	 * @date 2019-04-15
	 * @description 查询员工所在项目的所有员工信息
	 */ 
	public RestResponse findEmployeeByEmpProjectId(Integer employeeId) {
		try {
			Map<String,Object> empMap = employeeMapper.selectEmployeeById(employeeId);
			List<Map<String, Object>> employeeById = null;
			if(empMap.get("projectInfoId")!=null) {
				employeeById = employeeMapper.findEmployeeByProjectId((Integer)empMap.get("projectInfoId"));
			}
			return RestUtils.returnSuccess(employeeById);
		}catch(Exception e) {
			logger.error("findSimpleEmpById方法出现异常:"+e);
			return RestUtils.returnFailure("方法发生异常，查询员工数据失败");
		}
	}
	
	
	public RestResponse updateProjectId(Map<String,Object> params){
		logger.info("工作调动批准后根据员工id修改项目组id");
		try{
			Integer employeeId = (Integer) params.get("employeeId");
			Integer projectInfoId = (Integer) params.get("projectInfoId");
			
			ErpEmployee erpuser = new ErpEmployee();
			erpuser.setEmployeeId(employeeId);
			erpuser.setProjectInfoId(projectInfoId);
			employeeMapper.updataEmpProjectid(erpuser);
			return RestUtils.returnSuccess("ok");
		}catch (Exception e) {
			logger.error("findSimpleEmpById方法出现异常:"+e);
			return RestUtils.returnFailure("方法发生异常，查询员工数据失败");
		}
	}
	
	/**
	 * 用户姓名联想框数据(供项目工程调用)
	 * @param name
	 * @return
	 * @author hehui
	 * @createtime 2019-4-20
	 */
	public RestResponse getUserNameList(String name) {
		try {
			return RestUtils.returnSuccess(this.employeeMapper.getUserNameList(name+"%"));
		}catch(Exception e) {
			logger.error("getUserNameList方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("getUserNameList方法发生异常，获取用户姓名失败");
		}
	}
	
	/**
	 * 根据职位获取员工列表(供项目工程调用)
	 * @param position
	 * @return
	 * @author hehui
	 * @createtime 2019-4-20
	 */
	public RestResponse getUserListByPosition(String position) {
		try {
			return RestUtils.returnSuccess(this.employeeMapper.getUserListByPosition(position));
		}catch(Exception e) {
			logger.error("getUserNameList方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("getUserNameList方法发生异常，获取用户姓名失败");
		}
	}

	/**
	 * 从字典表中查询全部的民族
	 * @return
	 */
	public List<Map<String, Object>> findAllGroups() {
		logger.info("进入findAllGroups方法，无参数");
		List<Map<String,Object>> list = null;
		try {
			list = adminDicMapper.findAllGroupsFromAdminDic();
		} catch (Exception e) {
			logger.info("findAllGroups方法出现异常：" + e.getMessage(),e);
		}
		return list;
	}
	
	/**
	 * 从字典表中查询全部的政治面貌
	 * @return
	 */
	public List<Map<String, Object>> findAllPolitical() {
		logger.info("进入findAllPolitical方法，无参数");
		List<Map<String,Object>> list = null;
		try {
			list = adminDicMapper.findAllPoliticalFromAdminDic();
		} catch (Exception e) {
			logger.info("findAllPolitical方法出现异常：" + e.getMessage(),e);
		}
		return list;
	}

	/**
	 * 查询员工map详细信息
	 * @return
	 */
    public RestResponse findEmployeeInfoMap(String departmentName, String employeeName) {
		logger.info("进入findEmployeeInfoMap方法，无参数");
		Map<String,Object> returnMap = new HashMap<>();
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("departmentName", departmentName);
			param.put("employeeName", employeeName);
			List<Map<String,Object>> employeeList = employeeMapper.findEmployeeAllByParams(param);
			for (Map<String,Object> employeeMap : employeeList){
				returnMap.put(String.valueOf(employeeMap.get("employeeId")),employeeMap);
			}
		} catch (Exception e) {
			logger.info("findEmployeeInfoMap方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(returnMap);
    }

	/**
	 * Description: 定时器调用无token，根据传入的员工ID、部门查询指定一级部门下的员工信息（供薪酬工程--社保公积金调用）
	 * 参数说明：detpId:部门ID，employeeName：员工姓名，page：需要查询的第几页，默认是第一个页，limit：每页展示的记录数
	 *			 status：员工状态是一个字符串,元素是字符串，是"0"、"1"、"2"、"3"、"4"值的组合，中间为英文","分隔。
	 * 逻辑：1.如果当前登陆人是一级部门领导及以上领导，则查询部门下的所有人员
	 2.如果当前登陆人只是二级部门领导，则只查询所属二级部门下的所有人员
	 * @return
	 * @Author Songxiugong
	 * @Create Date: 2020年02月20日

	 */
	public RestResponse findEmployeeByDeptAndUserNoToken(Map<String, Object> params) {
		return findEmployeeByDeptAndUser(null, params);
	}

	/**
	 * 发送短信
	 * @param token
	 * @param type 1员工修改手机号，2部门修改领导
	 * @param changeType 1部门领导，2上级领导
	 * @return
	 */
	public RestResponse sendSms(String token, Integer employeeId, Integer type, Integer changeType) {
		if(type != 1 && type != 2){
			return RestUtils.returnFailure("参数错误");
		}
		// 短信应用SDK AppID
		int appid = DicConstants.APPID;

		// 短信应用SDK AppKey
		String appkey = DicConstants.APPKEY;

		/*
		 * 调用权限工程，通过员工Id查询用户Id
		 */
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>();
		if(type == 2 && changeType == 2){
			//部门修改领导并且修改的是上级领导
			//通过权限工程查询总裁的员工ID
			List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
			Map<String,Object> map = list.get(0);
			Integer nextPerson = (Integer) map.get("userId");//注意：userId是员工ID
			erpUser.add("userId", nextPerson);//员工Id
		}else{
			erpUser.add("userId", employeeId);//员工Id
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("token",token);
		HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<>(erpUser,requestHeaders);
		String userUrl =  protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId";
		ResponseEntity<Map> userResponse = this.restTemplate.postForEntity(userUrl, request, Map.class);
		//跨工程调用响应失败
		if(200 != userResponse.getStatusCodeValue() || !"200".equals(userResponse.getBody().get("status"))) {
			return RestUtils.returnFailure("调用权限工程时发生异常，响应失败！");
		}
		if(userResponse.getBody().get("data")==null || "".equals(userResponse.getBody().get("data"))) {
			return RestUtils.returnSuccessWithString("OK");
		}
		Map<String,Object> userInfo = (Map<String, Object>) userResponse.getBody().get("data");
		Integer userId = Integer.valueOf(String.valueOf(userInfo.get("id")));//用户Id
		String userPhone  = String.valueOf(userInfo.get("userPhone"));//用户手机
		String userEmail = String.valueOf(userInfo.get("username"));
		// 需要发送短信的手机号码
		//String[] phoneNumbers = {phone};

		// 短信模板ID，需要在短信应用中申请
		// NOTE: 这里的模板ID`7839`只是一个示例，真实的模板ID需要在短信控制台中申请
		String checkCode = getNonceStr();
		int templateId = DicConstants.TEMPLATE_ID;
		// 签名
		String smsSign = DicConstants.SMSSIGN;
		if(type == 1){
			// save the sms code into redis
			stringRedisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_EMPLOYEE_SMS + userId, checkCode, 1, TimeUnit.MINUTES);

			//将验证码发送邮件给用户
			String frommail = "nt_admin@nantian.com.cn";
			String bcc = null;
			String subject = "南天ERP系统修改手机号";
			String text = "您正在修改手机号，验证码一分钟内有效！验证码为："+checkCode;
			String tomail = userEmail;
			boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail,DicConstants.MODIFY_MOBILE_NUMBER_EMAIL_TYPE,null);
			if(sendSuccess) {
				logger.info(tomail+"邮件发送成功，请到邮件中获取验证码！");
			}else {
				logger.info(tomail+"邮件发送失败，请稍后尝试！");
			}


		}
	    if(type == 2){
			templateId = DicConstants.DEPARTENTMENT_TEMPLATE_ID;
			// 签名
			smsSign = DicConstants.DEPARTENTMENT_SMSSIGN;
			// save the sms code into redis
			stringRedisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_EMPLOYEE_SMS + userId, checkCode, 5, TimeUnit.MINUTES);
		}

		//调用邮件管理工程，将发送短信验证码的必要信息传递过去
		Map<String,Object> smvcParams = new HashMap<>();
		smvcParams.put("checkCode", checkCode);
		smvcParams.put("appid", appid);
		smvcParams.put("appkey", appkey);
		smvcParams.put("phone", userPhone);
		smvcParams.put("templateId", templateId);
		smvcParams.put("smsSign", smsSign);
		String url = emailServiceHost+"/nantian-erp/secondarycheck/smvc/send";
		Map<String,String> headers = new HashMap<>();
		headers.put("token", token);
		//headers.put("Content-Type", "application/json; charset=utf-8");
		Map<String, String> response = HttpClientUtil.executePostMethodWithParas(url, JSON.toJSONString(smvcParams), headers, "application/json", 30000);
		logger.info("发短信验证码的结果response="+response);
		if(!"200".equals(response.get("code"))) {
			return RestUtils.returnSuccessWithString("短信验证码发送失败！");
		}
		Map<String,Object> resultMap = (Map<String,Object>) JSON.parse(response.get("result"));
		if(!"200".equals(resultMap.get("status"))) {
			return RestUtils.returnSuccessWithString("短信验证码发送失败！");
		}
//		try {
//			//数组具体的元素个数和模板中变量个数必须一致，例如事例中templateId:5678对应一个变量，参数数组中元素个数也必须是一个
//			String[] params = {checkCode};
//			SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
//			SmsSingleSenderResult result = ssender.sendWithParam("86", phoneNumbers[0],
//					// 签名参数未提供或者为空时，会使用默认签名发送短信
//					templateId, params, smsSign, "", "");
//			logger.info("发送结果：result=" + result);
//		} catch (HTTPException e) {
//			// HTTP响应码错误
//			logger.error("HTTP错误" + e.getMessage());
//		} catch (JSONException e) {
//			// json解析错误
//			logger.error("json错误" + e.getMessage());
//		} catch (IOException e) {
//			// 网络IO错误
//			logger.error("网络IO错误" + e.getMessage());
//		}
		return RestUtils.returnSuccessWithString("OK");
	}

	public RestResponse checkSms(String code, Integer employeeId, String token){
		logger.info("code="+code+",token="+token);
		/*
		 * 调用权限工程，通过员工Id查询用户Id
		 */
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>();
		erpUser.add("userId", employeeId);//员工Id
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("token",token);
		HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<>(erpUser,requestHeaders);
		String userUrl =  protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId";
		ResponseEntity<Map> userResponse = this.restTemplate.postForEntity(userUrl, request, Map.class);
		//跨工程调用响应失败
		if(200 != userResponse.getStatusCodeValue() || !"200".equals(userResponse.getBody().get("status"))) {
			return RestUtils.returnFailure("调用权限工程时发生异常，响应失败！");
		}
		if(userResponse.getBody().get("data")==null || "".equals(userResponse.getBody().get("data"))) {
			return RestUtils.returnSuccessWithString("OK");
		}
		Map<String,Object> userInfo = (Map<String, Object>) userResponse.getBody().get("data");
		Integer userId = Integer.valueOf(String.valueOf(userInfo.get("id")));//用户Id

		if (code.equals(stringRedisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_EMPLOYEE_SMS + userId))) {
			return RestUtils.returnSuccessWithString("OK");
		} else {
			return RestUtils.returnSuccessWithString("验证码无效！");
		}
	}


	/**
	 * 权限管理
	 * 生成随机验证码：4位数字
	 */
	private  String getNonceStr() {
		String symbols = "0123456789";
		Random random = new SecureRandom();
		char[] nonceChars = new char[4];

		for (int index = 0; index < nonceChars.length; ++index) {
			nonceChars[index] = symbols.charAt(random.nextInt(symbols.length()));
		}

		return new String(nonceChars);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse employeeInProjectInfo(String token,Map<String, Object> queryMap) {
		logger.info("employeeInProjectInfo方法开始执行,参数queryMap={}",queryMap);
		 List<Map<String,Object>> employeeProjectList=new ArrayList<Map<String,Object>>();
		// 根据角色过滤数据
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
		List<Integer> roles = erpUser.getRoles();// 角色列表
		try{
			if (roles.contains(8)) {// 总经理、管理员
				// 查询所有的员工
			} else if (roles.contains(9)) { // 副总经理
				queryMap.put("superLeaderId", id);
			} else if (roles.contains(2)) {// 一级部门经理角色
				queryMap.put("leaderId", id);
			} else if (roles.contains(5)) {// 二级部门经理角色
				queryMap.put("secLeaderId", id);
			} else {
				return RestUtils.returnSuccess(new HashMap<String, Object>());
			}
			List<Map<String,Object>> resultList = employeeMapper.selectAllEmployee(queryMap);//查询所有
			queryMap.put("list", resultList);
			String url = protocolType+"nantian-erp-project/nantian-erp/project/project/findEmpInProjectAndWorkTime";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
			HttpEntity<Map<String,Object>> salaryRequestEntity = new HttpEntity<>(queryMap, requestHeaders);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,salaryRequestEntity,Map.class);
		    logger.info("跨工程调用的响应结果response={}",response);
		    Map<String,Object> resultMap = response.getBody();
		    if(response.getStatusCodeValue() != 200 || resultMap.get("data")==null || "".equals(resultMap.get("data"))){
		    	logger.error("项目工程响应失败！");
		    	return RestUtils.returnFailure("项目工程响应失败！");
		    }
		    employeeProjectList = (List<Map<String,Object>>) resultMap.get("data");
		}catch(Exception e){
			e.printStackTrace();
			logger.error("employeeInProjectInfo方法出现异常,",e.getMessage(),e);
			return RestUtils.returnFailure("employeeInProjectInfo方法出现异常");
		}
		return RestUtils.returnSuccess(employeeProjectList);
	}

	public RestResponse projectInEmployees(Map<String, Object> params) {
		logger.info("projectInEmployees方法开始执行,参数params={}",params);
		Map<String,Object> returnMap=new HashMap<String, Object>();
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>();
		Integer flag=Integer.valueOf(String.valueOf(params.get("flag")));
		try{
			if (params.get("employeeId")!=null) {// 项目主管id所在一级部门所有员工
				Integer employeeId=Integer.valueOf(String.valueOf(params.get("employeeId")));
				List<Integer> emplpyeeIds=this.employeeMapper.findProjectDirectorOfEmployee(employeeId);
				if(emplpyeeIds.size()>0){
					params.put("employeeIds", emplpyeeIds);
				}
			}
			if(params.get("employeeIds")!=null){
				resultList = employeeMapper.selectAllEmployee(params);//查询所有
			}
			returnMap.put("list", resultList);
			returnMap.put("flag", flag);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("projectInEmployees方法出现异常,",e.getMessage(),e);
			return RestUtils.returnFailure("projectInEmployees方法出现异常");
		}
		return RestUtils.returnSuccess(returnMap);
	}
}
