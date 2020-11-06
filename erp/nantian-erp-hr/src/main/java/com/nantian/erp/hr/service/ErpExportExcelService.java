package com.nantian.erp.hr.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nantian.erp.hr.data.dao.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.base.util.StringUtil;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;

/**
 * serviceImpl 接口实现层 人力资源 - 列表导出
 * 
 * @author caoxb
 * @date 2018-09-05
 */
@Service
@PropertySource(value = { "classpath:config/sftp.properties", "file:${spring.profiles.path}/config/sftp.properties",
		"classpath:config/email.properties", "file:${spring.profiles.path}/config/email.properties",
		"classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class ErpExportExcelService {
	@Value("${protocol.type}")
	private String protocolType;// http或https
	@Autowired
	private ErpOfferMapper offerMapper;

	@Autowired
	private ErpResumeMapper resumeMapper;

	@Autowired
	private ErpResumePostMapper interViewMapper;

	@Autowired
	private ErpEntryRecordMapper entryRecordMapper;

	@Autowired
	private ErpPostMapper postMapper;

	@Autowired
	private ErpEmployeePostiveMapper employeePostiveMapper;

	@Autowired
	private ErpPositiveRecordMapper positiveRecordMapper;

	@Autowired
	private ErpEmployeeEntryMapper employeeEntryMapper;

	@Autowired
	private ErpEmployeeMapper erpEmployeeMapper;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private ErpPositionRankRelationMapper positionRankRelationMapper;

	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpEmployeeService ErpEmployeeService;

	@Autowired
	HttpServletRequest request;

	@Autowired
	RestTemplate restTemplate;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/*--offer相关列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * add by caoxb 有效/归档offer导出 根据起始时间和截止时间分割 update by ZhangYuWei 20180926
	 * 根据offer是否有效来导出
	 */
	/*
	 * public String exportOffer(Integer isValid) { String str = ""; try {
	 * Map<String, Object> param = new HashMap<>(); Boolean isValidParam = null;
	 * //有效offer标识 1-有效 2-失效 if(isValid==1) { isValidParam=true; }else {
	 * isValidParam=false; }
	 * 
	 * //有效offer相关信息 List<Map<String, Object>> resultList =
	 * this.offerMapper.findAllOfferByIsValid(isValidParam);
	 * 
	 * for (Map<String, Object> map : resultList) { if(1 == isValid) { //面试记录
	 * Integer resumeId = Integer.valueOf(String.valueOf(map.get("resumeId")));
	 * //简历ID List<Map<String, Object>> tempList =
	 * this.resumeMapper.findResumeContent(resumeId); StringBuffer tempStr = new
	 * StringBuffer(); for (Map<String, Object> map2 : tempList) {
	 * tempStr.append(String.valueOf(map2.get("content"))); } map.put("content",
	 * tempStr); }else { map.put("content", ""); } } str =
	 * this.exportExcelForOffer(resultList, isValid); } catch (Exception e) { }
	 * return str; }
	 */
	public String exportOffer(List<Integer> offerIds) {
		logger.info("exportOffer方法开始执行，传递参数：List:size=" + offerIds.size());

		String str = null;
		try {
			str = "";
			List<Map<String, Object>> resultList = new ArrayList<>();
			try {
				for (Integer offerId : offerIds) {
					Map<String, Object> offerMap = this.offerMapper.selectOfferDetail(offerId);
					resultList.add(offerMap);
				}
				str = this.exportExcelForOffer(resultList);
			} catch (Exception e) {

				logger.info("exportOffer方法出现异常：" + e.getMessage());
			}
		} catch (Exception e) {

			logger.info("exportOffer方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	/*
	 * public String exportExcelForOffer (List<Map<String, Object>>
	 * resultList,Integer isValid) {
	 * 
	 * //定义工作簿 XSSFWorkbook workBook = null; try { workBook = new XSSFWorkbook();
	 * XSSFSheet sheet = null; if(isValid==1) { sheet =
	 * workBook.createSheet("有效offer"); }else { sheet =
	 * workBook.createSheet("归档offer"); } //生成第一行 当为归档offer时 得添加归档理由 XSSFRow
	 * firstRow = sheet.createRow(0); firstRow.createCell(0).setCellValue("姓名");
	 * firstRow.createCell(1).setCellValue("性别");
	 * firstRow.createCell(2).setCellValue("手机");
	 * firstRow.createCell(3).setCellValue("一级部门");
	 * firstRow.createCell(4).setCellValue("二级部门");
	 * firstRow.createCell(5).setCellValue("岗位");
	 * firstRow.createCell(6).setCellValue("职位");
	 * firstRow.createCell(7).setCellValue("职级");
	 * firstRow.createCell(8).setCellValue("入职时间");
	 * //firstRow.createCell(9).setCellValue("面试记录"); if(isValid==2) {
	 * firstRow.createCell(9).setCellValue("归档原因"); }
	 * 
	 * //下一行 XSSFRow nextRow = null; Map<String, Object> param = null; //循环 填充表格 for
	 * (int i = 0; i < resultList.size(); i++) { param = resultList.get(i); nextRow
	 * = sheet.createRow(i+1);
	 * nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
	 * nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
	 * nextRow.createCell(2).setCellValue(String.valueOf(param.get("phone")));
	 * nextRow.createCell(3).setCellValue(String.valueOf(param.get(
	 * "firstDepartment")));
	 * nextRow.createCell(4).setCellValue(String.valueOf(param.get(
	 * "secondDepartment")));
	 * nextRow.createCell(5).setCellValue(String.valueOf(param.get("postName"))) ;
	 * nextRow.createCell(6).setCellValue(String.valueOf(param.get("position"))) ;
	 * nextRow.createCell(7).setCellValue(String.valueOf(param.get("rank")));
	 * nextRow.createCell(8).setCellValue(String.valueOf(param.get("entryTime")) );
	 * //nextRow.createCell(9).setCellValue(String.valueOf(param.get("content")) );
	 * //针对归档offer 多一个归档原因 if(isValid==2) {
	 * nextRow.createCell(9).setCellValue(String.valueOf(param.get("reason"))); } }
	 * } catch (Exception e2) { } return this.exportExcelToComputer(workBook); }
	 */
	public String exportExcelForOffer(List<Map<String, Object>> resultList) {
		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			sheet = workBook.createSheet("offer");
			// 生成第一行 当为归档offer时 得添加归档理由
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("手机");
			firstRow.createCell(3).setCellValue("一级部门");
			firstRow.createCell(4).setCellValue("二级部门");
			firstRow.createCell(5).setCellValue("岗位");
			firstRow.createCell(6).setCellValue("职位");
			firstRow.createCell(7).setCellValue("职级");
			firstRow.createCell(8).setCellValue("入职时间");
			firstRow.createCell(9).setCellValue("归档原因");

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("phone")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("firstDepartment")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("secondDepartment")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("postName")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("position")));
				nextRow.createCell(7).setCellValue(String.valueOf(param.get("rank")));
				nextRow.createCell(8).setCellValue(String.valueOf(param.get("entryTime")));
				if (param.get("reason") == null) {
					nextRow.createCell(9).setCellValue("");
				} else {
					nextRow.createCell(9).setCellValue(String.valueOf(param.get("reason")));
				}

			}
		} catch (Exception e2) {

		}
		return this.exportExcelToComputer(workBook);
	}
	/*--offer相关列表导出  end --------------------------------------------------------------------------------------------*/

	/*--面试相关列表导出  start --------------------------------------------------------------------------------------------*/

	/**
	 * 待我处理/所有进行中的面试 导出 add by caoxb update by ZhangYuWei 20180926
	 */
	public String exportInterviewing(Integer flag, String token) {
		logger.info("exportInterviewing方法开始执行，传递参数1：flag:" + flag + ",参数2:token" + token);

		String str = "";
		try {
			// 是否为 待我面试 1-待我面试 2-所有
			// Integer flag =
			// Integer.valueOf(String.valueOf(param.get("flag")));
			Map<String, Object> param = new HashMap<>();
			if (flag == 1) {
				String userName = stringRedisTemplate.opsForValue().get(token);
				param.put("userName", userName);
			}
			// 所有进行中的面试相关信息
			List<Map<String, Object>> resultList = this.interViewMapper.findResumePostInfoByParams(param);

			/*
			 * for (Map<String, Object> map : resultList) { //面试记录 Integer resumeId =
			 * Integer.valueOf(String.valueOf(map.get("resumeId"))); //简历ID List<Map<String,
			 * Object>> tempList = this.resumeMapper.findResumeContent(resumeId);
			 * StringBuffer tempStr = new StringBuffer(); for (Map<String, Object> map2 :
			 * tempList) { tempStr.append(String.valueOf(map2.get("content"))); }
			 * map.put("content", tempStr); }
			 */
			str = this.exportExcelForInterviewing(resultList, flag);
		} catch (Exception e) {

			logger.info("exportInterviewing方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	public String exportExcelForInterviewing(List<Map<String, Object>> resultList, Integer flag) {

		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			if (flag == 1) {
				sheet = workBook.createSheet("待我面试");
			} else {
				sheet = workBook.createSheet("所有进行中的面试");
			}
			// 生成第一行 当为归档offer时 得添加归档理由
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("出生年月");
			firstRow.createCell(3).setCellValue("手机");
			firstRow.createCell(4).setCellValue("派个人邮箱");
			firstRow.createCell(5).setCellValue("工作经验");
			firstRow.createCell(6).setCellValue("学历");
			firstRow.createCell(7).setCellValue("岗位");
			// firstRow.createCell(8).setCellValue("面试记录");
			if (flag == 2) {
				firstRow.createCell(8).setCellValue("当前处理人");
			}

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("birthday")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("phone")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("email")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("experience")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("degree")));
				nextRow.createCell(7).setCellValue(String.valueOf(param.get("postName")));
				// nextRow.createCell(8).setCellValue(String.valueOf(param.get("content")));
				// 针对所有进行中的面试 多一个 当前处理人
				if (flag == 2) {
					nextRow.createCell(8).setCellValue(String.valueOf(param.get("proposerEmail")));
				}
			}
		} catch (Exception e2) {

			logger.info("exportExcelForInterviewing方法出现异常：" + e2.getMessage());
		}
		return this.exportExcelToComputer(workBook);
	}

	/*--面试相关列表导出  end --------------------------------------------------------------------------------------------*/

	/*--岗位相关列表导出  start --------------------------------------------------------------------------------------------*/

	/**
	 * 发布中的岗位 导出 add by caoxb update by ZhangYuWei 20180926
	 */
	public String exportPost(Integer isClosed) {
		logger.info("exportPost方法开始执行，传递参数:isClosed:" + isClosed);

		String str = "";
		try {
			// 是否为 发布中的岗位 1-已完成 2-发布中
			// Integer isClosed =
			// Integer.valueOf(String.valueOf(param.get("isClosed")));
			// Map<String, Object> param = new HashMap<>();
			// param.put("isClosed", isClosed);
			Boolean isClosedParam = null;
			if (isClosed == 1) {
				isClosedParam = true;
			} else {
				isClosedParam = false;
			}
			// 发布中的岗位相关信息
			List<Map<String, Object>> resultList = this.postMapper.findByIsClosed(isClosedParam);
			// 已入职人数
			Integer numForEntry = 0;
			for (Map<String, Object> map : resultList) {
				// 招聘记录
				Integer postId = Integer.valueOf(String.valueOf(map.get("postId"))); // 简历ID
				numForEntry = this.postMapper.getPostCountForEntry(postId);
				Integer totalNumForPost = Integer.valueOf(String.valueOf(map.get("numberPeople")));
				map.put("postRecord", "已入职:" + numForEntry + "  " + "招聘总人数" + totalNumForPost);
			}
			str = this.exportExcelForPost(resultList, isClosed);
		} catch (Exception e) {

			logger.info("exportPost方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	public String exportExcelForPost(List<Map<String, Object>> resultList, int isClosed) {

		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			if (1 == isClosed) {
				sheet = workBook.createSheet("已关闭岗位");
			} else {
				sheet = workBook.createSheet("发布中的岗位");
			}
			// 生成第一行 当为归档offer时 得添加归档理由
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("申请人");
			firstRow.createCell(1).setCellValue("一级部门");
			firstRow.createCell(2).setCellValue("二级部门");
			firstRow.createCell(3).setCellValue("岗位名称");
			firstRow.createCell(4).setCellValue("岗位类别");
			firstRow.createCell(5).setCellValue("岗位要求");
			firstRow.createCell(6).setCellValue("岗位薪资");
			firstRow.createCell(7).setCellValue("招聘记录");
			if (1 == isClosed) {
				firstRow.createCell(8).setCellValue("关闭原因");
			}

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("proposerEmail")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("firstDepartment")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("secondDepartment")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("postName")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("categoryName")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("required")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("salary")));
				nextRow.createCell(7).setCellValue(String.valueOf(param.get("postRecord"))); // 招聘记录
				// 针对已关闭的岗位 多一个关闭原因
				if (1 == isClosed) {
					nextRow.createCell(8).setCellValue(String.valueOf(param.get("closedReason")));
				}
			}
		} catch (Exception e2) {

			logger.info("exportExcelForPost方法出现异常：" + e2.getMessage());
		}
		return this.exportExcelToComputer(workBook);
	}

	/*--岗位相关列表导出  end --------------------------------------------------------------------------------------------*/

	/*--简历相关列表导出  start --------------------------------------------------------------------------------------------*/

	/**
	 * 有效简历/失效简历导出 根据起始时间和截止时间分割 add by caoxb update by ZhangYuWei 20180926
	 */
	public String exportResume(Integer isValid) {
		logger.info("exportResume方法开始执行，传递参数:isValid:" + isValid);

		String str = "";
		try {
			// 有效/失效简历标识 1-有效 2-无效
			// Integer isValid =
			// Integer.valueOf(String.valueOf(param.get("isValid")));
			Map<String, Object> param = new HashMap<>();
			if (isValid == 1) {
				param.put("isValid", true);
			} else {
				param.put("isValid", false);
			}
			// 失效简历相关信息
			List<Map<String, Object>> resultList = this.resumeMapper.findResumeByValid(param);
			/*
			 * for (Map<String, Object> map : resultList) { //面试记录 Integer resumeId =
			 * Integer.valueOf(String.valueOf(map.get("resumeId"))); //简历ID List<Map<String,
			 * Object>> tempList = this.resumeMapper.findResumeContent(resumeId); String
			 * tempStr = ""; for (Map<String, Object> map2 : tempList) { tempStr =
			 * String.valueOf(map2.get("content")) + tempStr; } map.put("content", tempStr);
			 * }
			 */
			str = this.exportExcelForResume(resultList, isValid);
		} catch (Exception e) {

			logger.info("exportResume方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	public String exportExcelForResume(List<Map<String, Object>> resultList, int isValid) {

		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			if (1 == isValid) {
				sheet = workBook.createSheet("有效简历");
			} else {
				sheet = workBook.createSheet("失效简历");
			}
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("出生年月");
			firstRow.createCell(3).setCellValue("手机");
			firstRow.createCell(4).setCellValue("个人邮箱");
			firstRow.createCell(5).setCellValue("工作经验");
			firstRow.createCell(6).setCellValue("学历");
			firstRow.createCell(7).setCellValue("求职方向");
			firstRow.createCell(8).setCellValue("状态");
			// firstRow.createCell(9).setCellValue("面试记录");

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("birthday")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("phone")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("email")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("experience")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("degree")));
				nextRow.createCell(7).setCellValue(String.valueOf(param.get("jobDirection")));
				nextRow.createCell(8).setCellValue(String.valueOf(param.get("status")));
				// nextRow.createCell(9).setCellValue(String.valueOf(param.get("content")));
				// 目前有效简历和失效简历并无差异字段 暂时注掉--
				/*
				 * if(2 == isValid) { firstRow.createCell(10).setCellValue("归档原因");
				 * nextRow.createCell(10).setCellValue(String.valueOf(param.get( "reason"))); }
				 */
			}
		} catch (Exception e2) {

			logger.info("exportExcelForResume方法出现异常：" + e2.getMessage());
		}
		return this.exportExcelToComputer(workBook);
	}
	/*--简历相关列表导出  end --------------------------------------------------------------------------------------------*/

	/*--转正相关列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 转正 待我转正/所有待转正导出 根据起始时间和截止时间分割 add by caoxb update by ZhangYuWei 20180926
	 */
	public String exportPositive(Integer flag, String token) {
		logger.info("exportPositive方法开始执行，传递参数1:flag:" + flag + ",参数2:token:" + token);

		String str = "";
		try {
			// 待我转正/所有待转正 1-待我转正 2-所有
			// Integer flag =
			// Integer.valueOf(String.valueOf(param.get("flag")));
			Map<String, Object> param = new HashMap<>();
			// 所有待转正 相关信息
			if (1 == flag) {
				String userName = stringRedisTemplate.opsForValue().get(token);
				param.put("userName", userName);
			}
			List<Map<String, Object>> resultList = this.employeePostiveMapper.findAllPositive(param);
			/*
			 * for (Map<String, Object> map : resultList) { //处理记录 Integer employeeId =
			 * Integer.valueOf(String.valueOf(map.get("employeeId"))); //员工ID Example
			 * example = new Example(ErpPositiveRecord.class);
			 * example.setOrderByClause("time desc"); Criteria createCriteria =
			 * example.createCriteria(); createCriteria.andEqualTo("employeeId",employeeId);
			 * List<ErpPositiveRecord> list =
			 * this.positiveRecordMapper.selectByExample(example); StringBuffer tempStr =
			 * new StringBuffer(); for (ErpPositiveRecord positiveRecord : list) {
			 * tempStr.append(String.valueOf(positiveRecord.getContext())); }
			 * map.put("content", tempStr); }
			 */
			str = this.exportExcelForPositive(resultList, flag);
		} catch (Exception e) {

			logger.info("exportPositive方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	public String exportExcelForPositive(List<Map<String, Object>> resultList, int flag) {

		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			if (1 == flag) {
				sheet = workBook.createSheet("转正-待我处理");
			} else {
				sheet = workBook.createSheet("转正-所有待转正");
			}
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("一级部门");
			firstRow.createCell(3).setCellValue("二级部门");
			firstRow.createCell(4).setCellValue("职位");
			firstRow.createCell(5).setCellValue("职级");
			firstRow.createCell(6).setCellValue("转正时间");
			// firstRow.createCell(7).setCellValue("处理记录");
			if (2 == flag) {
				firstRow.createCell(7).setCellValue("当前处理人");
			}

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("firstDepartment")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("secondDepartment")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("position")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("rank")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("probationEndTime")));
				// nextRow.createCell(7).setCellValue(String.valueOf(param.get("content")));
				// 针对归档offer 多一个归档原因
				if (2 == flag) {
					nextRow.createCell(7).setCellValue(String.valueOf(param.get("currentPerson")));
				}
			}
		} catch (Exception e2) {

			logger.info("exportExcelForPositive方法出现异常：" + e2.getMessage());
		}
		return this.exportExcelToComputer(workBook);
	}
	/*--转正相关列表导出  end --------------------------------------------------------------------------------------------*/

	/*--入职相关列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 入职 待我处理导出 根据起始时间和截止时间分割 add by caoxb update by ZhangYuWei 20180926
	 */
	public String exportEntry(Integer flag, String token) {
		logger.info("exportEntry方法开始执行，传递参数1:flag:" + flag + ",参数2:token:" + token);

		String str = "";
		try {
			// 是否是待我处理 1-待我处理 2-所有
			// Integer flag =
			// Integer.valueOf(String.valueOf(param.get("flag")));
			Map<String, Object> param = new HashMap<>();
			if (1 == flag) {
				String userName = stringRedisTemplate.opsForValue().get(token);
				param.put("userName", userName);
			}
			// 入职 相关信息
//			List<Map<String, Object>> resultList = this.employeeEntryMapper.findAllEntry(param);
			/*
			 * for (Map<String, Object> map : resultList) { //面试记录 Integer resumeId =
			 * Integer.valueOf(String.valueOf(map.get("resumeId"))); //简历ID List<Map<String,
			 * Object>> tempList = this.resumeMapper.findResumeContent(resumeId);
			 * StringBuffer tempStr = new StringBuffer(); for (Map<String, Object> map2 :
			 * tempList) { tempStr.append(String.valueOf(map2.get("content"))); }
			 * map.put("content", tempStr); if(2 == flag) { //处理记录 Integer offerId =
			 * Integer.valueOf(String.valueOf(map.get("offerId"))); //员工ID Example example =
			 * new Example(ErpEntryRecord.class); example.setOrderByClause("time desc");
			 * Criteria createCriteria = example.createCriteria();
			 * createCriteria.andEqualTo("offerId",offerId); List<ErpEntryRecord> list =
			 * this.entryRecordMapper.selectByExample(example); StringBuffer tempStr1 = new
			 * StringBuffer(); for (ErpEntryRecord entryRecord : list) {
			 * tempStr1.append(String.valueOf(entryRecord.getContent())); }
			 * map.put("content1", tempStr1); } }
			 */
//			str = this.exportExcelForEntry(resultList, flag);
		} catch (Exception e) {

			logger.info("exportEntry方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	public String exportExcelForEntry(List<Map<String, Object>> resultList, int flag) {

		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			if (1 == flag) {
				sheet = workBook.createSheet("入职-待我处理");
			} else {
				sheet = workBook.createSheet("入职-所有待入职");
			}
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("手机");
			firstRow.createCell(3).setCellValue("一级部门");
			firstRow.createCell(4).setCellValue("二级部门");
			firstRow.createCell(5).setCellValue("岗位");
			firstRow.createCell(6).setCellValue("职位");
			firstRow.createCell(7).setCellValue("职级");
			firstRow.createCell(8).setCellValue("入职时间");
			// firstRow.createCell(9).setCellValue("面试记录");
			if (2 == flag) {
				firstRow.createCell(9).setCellValue("当前处理人");
				firstRow.createCell(10).setCellValue("处理记录");
			}

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("phone")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("firstDepartment")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("secondDepartment")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("postName")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("position")));
				nextRow.createCell(7).setCellValue(String.valueOf(param.get("rank")));
				nextRow.createCell(8).setCellValue(String.valueOf(param.get("entryTime")));
				// nextRow.createCell(9).setCellValue(String.valueOf(param.get("content")));
				// 针对归档offer 多一个归档原因
				if (2 == flag) {
					nextRow.createCell(9).setCellValue(String.valueOf(param.get("currentPerson")));
					nextRow.createCell(10).setCellValue(String.valueOf(param.get("content1")));// 处理记录
				}
			}
		} catch (Exception e2) {

			logger.info("exportExcelForEntry方法出现异常：" + e2.getMessage());
		}
		return this.exportExcelToComputer(workBook);
	}
	/*--转正相关列表导出  end --------------------------------------------------------------------------------------------*/

	/*--部门-员工信息列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 部门-员工信息 列表导出 add by caoxb
	 */
	public String exportEmployeeInfo(List<Integer> employeeIdList) {
		logger.info("exportEmployeeInfo方法开始执行，传递参数:List:size=" + employeeIdList.size());

		String str = "";
		try {
			if (employeeIdList.size() != 0) {
				// 入职 相关信息
				List<Map<String, Object>> resultList = new ArrayList<>();
				for (int i = 0; i < employeeIdList.size(); i++) {
					Integer employeeId = employeeIdList.get(i);
					Map<String, Object> employee = this.erpEmployeeMapper.selectAllEmployeeById(employeeId);
					List<Map<String, Object>> certificateList = this.erpEmployeeMapper
							.findAllCertificateByEmp(employeeId);
					employee.put("certificate", certificateList);
					resultList.add(employee);
				}
				str = this.exportEmployeeInfoUtil(resultList);
			}
		} catch (Exception e) {

			logger.info("exportEmployeeInfo方法出现异常：" + e.getMessage());
		}
		return str;
	}

	/*
	 * 功能：导出 参数：List<Map<String, Object>> resultList 针对每个列表差异字段处理 共用工具类
	 */
	@SuppressWarnings("unused")
	public String exportEmployeeInfoUtil(List<Map<String, Object>> resultList) {

		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("部门-员工信息");
			// 生成表头（第一行）
			int titleNumber = 0;
			XSSFRow firstRow = sheet.createRow(titleNumber);
			firstRow.createCell(titleNumber++).setCellValue("员工编号");
			firstRow.createCell(titleNumber++).setCellValue("姓名");
			firstRow.createCell(titleNumber++).setCellValue("社保地");
			firstRow.createCell(titleNumber++).setCellValue("性别");
			firstRow.createCell(titleNumber++).setCellValue("一级部门");
			firstRow.createCell(titleNumber++).setCellValue("二级部门");
			firstRow.createCell(titleNumber++).setCellValue("职位族");
			firstRow.createCell(titleNumber++).setCellValue("职位类");
			firstRow.createCell(titleNumber++).setCellValue("职位子类");
			firstRow.createCell(titleNumber++).setCellValue("职位");
			firstRow.createCell(titleNumber++).setCellValue("职级");
			firstRow.createCell(titleNumber++).setCellValue("员工状态");
			firstRow.createCell(titleNumber++).setCellValue("入职时间");
			firstRow.createCell(titleNumber++).setCellValue("合同开始时间");
			firstRow.createCell(titleNumber++).setCellValue("试用期结束时间");
			firstRow.createCell(titleNumber++).setCellValue("合同结束时间");
			firstRow.createCell(titleNumber++).setCellValue("续签合同开始时间1");
			firstRow.createCell(titleNumber++).setCellValue("续签合同结束时间1");
			firstRow.createCell(titleNumber++).setCellValue("续签合同开始时间2");
			firstRow.createCell(titleNumber++).setCellValue("续签合同结束时间2");
			firstRow.createCell(titleNumber++).setCellValue("工资卡号");
			firstRow.createCell(titleNumber++).setCellValue("手机");
			firstRow.createCell(titleNumber++).setCellValue("公司邮箱");
			firstRow.createCell(titleNumber++).setCellValue("个人邮箱");
			firstRow.createCell(titleNumber++).setCellValue("身份证号");
			firstRow.createCell(titleNumber++).setCellValue("首次参加工作时间");
			firstRow.createCell(titleNumber++).setCellValue("毕业院校");
			firstRow.createCell(titleNumber++).setCellValue("专业");
			firstRow.createCell(titleNumber++).setCellValue("最高学历");
			firstRow.createCell(titleNumber++).setCellValue("民族");
			firstRow.createCell(titleNumber).setCellValue("政治面貌");
			List<Map<String, Object>> cerList = new ArrayList<>();
			int col = titleNumber;
			int light = 0;
			int index = 0;
			for (int i = 0; i < resultList.size(); i++) {
				cerList = (List<Map<String, Object>>) resultList.get(i).get("certificate");
				if (cerList != null) {
					if (cerList.size() > light) {
						light = cerList.size();
						index = i;
					}
				}
			}
			cerList = (List<Map<String, Object>>) resultList.get(index).get("certificate");
			if (cerList != null) {
				for (int i = 1; i < cerList.size()+1; i++) {
					firstRow.createCell(col + 1).setCellValue("证书名称" + i);
					firstRow.createCell(col + 2).setCellValue("发证机构" + i);
					firstRow.createCell(col + 3).setCellValue("证书等级" + i);
					col = col + 3;
				}
			}

			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				logger.info("执行第" + i + "行");
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				int valueNumber = 0;
				//员工编号
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("employeeId")));
				//姓名
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("name")));
				//社保地
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("socialSecurity")));
				//性别
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("sex")));
				//一级部门
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("firstDepartment")));
				//二级部门
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("secondDepartment")));
				//职位族类
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("positionFamilyName")));
				//职位类别
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("positionTypeName")));
				//职位子类
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("positionChildName")));
				// 职位编号
				String st = objectToString(param.get("positionId"));
				Integer positionId = 0;
				if (StringUtils.isBlank(st)) {// 当职位编号是null时
					positionId = -1;

				} else {
					positionId = new Integer((String.valueOf(param.get("positionId"))));
				}

				ErpPositionRankRelation positionRankRelation = null;
				// 当职位Id =0 时，取职位名称
				if (positionId == 0) {
					nextRow.createCell(valueNumber++).setCellValue(String.valueOf(param.get("position")));
				} else if (positionId > 0) {// 当职位Id>0 时，找职位职级表中职位名称
					// 根据职位ID 查找职位名称
					positionRankRelation = positionRankRelationMapper
							.selectErpPositionRankRelationByPostionNo(positionId);
					if (positionRankRelation != null) {
						String postionName = positionRankRelation.getPositionName();
						nextRow.createCell(valueNumber++).setCellValue(postionName);
					} else {// 当没查到对象 赋值""
						nextRow.createCell(valueNumber++).setCellValue("");
					}
				} else if (positionId == -1) {// 当职位编号是null时 ,返回职位名称""
					nextRow.createCell(valueNumber++).setCellValue("");
				}
				// 调用权限工程获取用户的手机号
				Integer employeeId = Integer.valueOf(String.valueOf(param.get("employeeId")));
				RestResponse response = getUserInfoByEmpId(employeeId);
				Object obj = response.getData();
				Map<String, Object> user = null;
				String phone = "";
				String userName = "";
				if (obj != null && StringUtils.isNoneEmpty(obj.toString())) {
					user = (Map<String, Object>) obj;
					phone = objectToString(user.get("userPhone")); // 个人电话
					userName = objectToString(user.get("username")); // 公司邮箱
				}

//				nextRow.createCell(5).setCellValue(String.valueOf(param.get("position")));
				//职级
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("rank")));
				//员工状态
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("statusName")));
				//入职时间
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("entryTime")));
				//合同开始时间
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("contractBeginTime")));
				//试用期结束时间
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("probationEndTime")));
				//合同结束时间
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("contractEndTime")));
				//续签合同开始时间1
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("renewalStartTime1")));
				//续签合同结束时间1
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("renewalEndTime1")));
				//续签合同开始时间2
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("renewalStartTime2")));
				//续签合同结束时间2
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("renewalEndTime2")));
				//工资卡号
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("salaryCardNumber")));
				//个人电话
				nextRow.createCell(valueNumber++).setCellValue(phone);
				//公司邮箱
				nextRow.createCell(valueNumber++).setCellValue(userName);
				//个人邮箱
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("personalEmail")));
				//身份证号
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("idCardNumber")));
				//首次参加工作时间
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("takeJobTime")));
				//毕业院校
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("school")));
				//专业
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("major")));
				//最高学历
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("education")));
				//民族
				nextRow.createCell(valueNumber++).setCellValue(objectToString(param.get("groupsName")));
				//政治面貌
				nextRow.createCell(valueNumber).setCellValue(objectToString(param.get("politicalName")));
				//证书
				int cols = valueNumber;
				List<Map<String, Object>> certificateList = (List<Map<String, Object>>) param.get("certificate");
				if (certificateList != null) {
					for (Map<String, Object> cerMap : certificateList) {
						//证书名称
						nextRow.createCell(cols + 1).setCellValue(objectToString(cerMap.get("certificateName")));
						//发证机构
						nextRow.createCell(cols + 2).setCellValue(objectToString(cerMap.get("organization")));
						//证书等级 
						nextRow.createCell(cols + 3).setCellValue(objectToString(cerMap.get("level")));
						cols = cols + 3;
					}
				}
			}
		} catch (Exception e) {

			logger.info("exportEmployeeInfoUtil方法出现异常：" + e.getMessage());
		}
		return this.exportExcelToComputer(workBook);
	}
	/*--部门-员工信息列表导出  end --------------------------------------------------------------------------------------------*/

	/*--部门-员工导入模板导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 员工导入模板导出
	 * 
	 * @return
	 */
	public String downloadExportTemplate() {
		logger.info("downloadExportTemplate方法开始执行，传递参数:无");

		String str = "";
		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("部门-员工信息");
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("社保地");
			firstRow.createCell(2).setCellValue("性别");
			firstRow.createCell(3).setCellValue("一级部门");
			firstRow.createCell(4).setCellValue("二级部门");
			firstRow.createCell(5).setCellValue("职位");
			firstRow.createCell(6).setCellValue("职级");
			firstRow.createCell(7).setCellValue("员工状态");
			firstRow.createCell(8).setCellValue("入职时间");
			firstRow.createCell(9).setCellValue("合同开始时间");
			firstRow.createCell(10).setCellValue("试用期结束时间");
			firstRow.createCell(11).setCellValue("合同结束时间");
			firstRow.createCell(12).setCellValue("工资卡号");
			firstRow.createCell(13).setCellValue("手机");
			firstRow.createCell(14).setCellValue("公司邮箱");
			firstRow.createCell(15).setCellValue("个人邮箱");
			firstRow.createCell(16).setCellValue("身份证号");
			firstRow.createCell(17).setCellValue("首次参加工作时间");
			firstRow.createCell(18).setCellValue("毕业院校");
			firstRow.createCell(19).setCellValue("专业");
			firstRow.createCell(20).setCellValue("最高学历");
			firstRow.createCell(21).setCellValue("最后发薪日");
			firstRow.createCell(22).setCellValue("办理手续时间");
			firstRow.createCell(23).setCellValue("离职原因");
			XSSFDataValidationHelper employeeDvHelper = new XSSFDataValidationHelper(sheet);

			//手机号为数字
			XSSFDataValidationConstraint phoneDvConstraint = (XSSFDataValidationConstraint) employeeDvHelper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN,"0","999999999999");
			CellRangeAddressList phoneAddressList = new CellRangeAddressList(1, 9999, 13, 13);
			XSSFDataValidation phoneValidation = (XSSFDataValidation) employeeDvHelper.createValidation(phoneDvConstraint, phoneAddressList);
			phoneValidation.setShowErrorBox(true);
			sheet.addValidationData(phoneValidation);
			// 生成"性别"下拉框内容
			String[] sexStrings = new String[]{"男","女"};
			XSSFDataValidationConstraint sexDvConstraint = (XSSFDataValidationConstraint) employeeDvHelper.createExplicitListConstraint(sexStrings);
			CellRangeAddressList sexAddressList = new CellRangeAddressList(1, 9999, 2, 2);
			XSSFDataValidation sexValidation = (XSSFDataValidation) employeeDvHelper.createValidation(sexDvConstraint, sexAddressList);
			sexValidation.setShowErrorBox(true);
			sheet.addValidationData(sexValidation);

			// 生成"员工状态"下拉框内容
			String[] statusStrings = new String[]{"实习生","试用期","正式员工","离职中","已离职"};
			XSSFDataValidationConstraint statusDvConstraint = (XSSFDataValidationConstraint) employeeDvHelper.createExplicitListConstraint(statusStrings);
			CellRangeAddressList statusAddressList = new CellRangeAddressList(1, 9999, 7, 7);
			XSSFDataValidation statusValidation = (XSSFDataValidation) employeeDvHelper.createValidation(statusDvConstraint, statusAddressList);
			statusValidation.setShowErrorBox(true);
			sheet.addValidationData(statusValidation);

			//设置时间格式 入职时间、合同开始时间、试用期结束时间、合同结束时间、首次参加工作时间、办理手续时间
			setExcelDate(workBook, sheet, new int[]{8,9,10,11,17,22});

			XSSFCellStyle dateStyle = workBook.createCellStyle();
			XSSFDataFormat format = workBook.createDataFormat();
			dateStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
			sheet.setDefaultColumnStyle(8, dateStyle);
			sheet.setDefaultColumnStyle(9, dateStyle);
			sheet.setDefaultColumnStyle(10, dateStyle);
			sheet.setDefaultColumnStyle(11, dateStyle);
			sheet.setDefaultColumnStyle(17, dateStyle);
			sheet.setDefaultColumnStyle(22, dateStyle);

			XSSFSheet sheet2 = workBook.createSheet("教育经历");
			XSSFRow firstRow2 = sheet2.createRow(0);
			firstRow2.createCell(0).setCellValue("姓名");
			firstRow2.createCell(1).setCellValue("身份证号");
			firstRow2.createCell(2).setCellValue("开始时间");
			firstRow2.createCell(3).setCellValue("结束时间");
			firstRow2.createCell(4).setCellValue("学校");
			firstRow2.createCell(5).setCellValue("专业");
			firstRow2.createCell(6).setCellValue("学历");
			//设置时间格式 开始时间、结束时间
			setExcelDate(workBook, sheet2, new int[]{2,3});

			XSSFSheet sheet3 = workBook.createSheet("工作经历");
			XSSFRow firstRow3 = sheet3.createRow(0);
			firstRow3.createCell(0).setCellValue("姓名");
			firstRow3.createCell(1).setCellValue("身份证号");
			firstRow3.createCell(2).setCellValue("开始时间");
			firstRow3.createCell(3).setCellValue("结束时间");
			firstRow3.createCell(4).setCellValue("公司");
			firstRow3.createCell(5).setCellValue("职务");

			//设置时间格式 开始时间、结束时间
			setExcelDate(workBook, sheet3, new int[]{2,3});

			XSSFSheet sheet4 = workBook.createSheet("项目经历");
			XSSFRow firstRow4 = sheet4.createRow(0);
			firstRow4.createCell(0).setCellValue("姓名");
			firstRow4.createCell(1).setCellValue("身份证号");
			firstRow4.createCell(2).setCellValue("开始时间");
			firstRow4.createCell(3).setCellValue("结束时间");
			firstRow4.createCell(4).setCellValue("项目名称");
			firstRow4.createCell(5).setCellValue("项目中职务");
			firstRow4.createCell(6).setCellValue("项目描述");
			firstRow4.createCell(7).setCellValue("责任描述");
			//设置时间格式 开始时间、结束时间
			setExcelDate(workBook, sheet4, new int[]{2,3});

			XSSFSheet sheet5 = workBook.createSheet("技术特长");
			XSSFRow firstRow5 = sheet5.createRow(0);
			firstRow5.createCell(0).setCellValue("姓名");
			firstRow5.createCell(1).setCellValue("身份证号");
			firstRow5.createCell(2).setCellValue("技能名称");
			firstRow5.createCell(3).setCellValue("熟练程度");

			XSSFSheet sheet6 = workBook.createSheet("证书");
			XSSFRow firstRow6 = sheet6.createRow(0);
			firstRow6.createCell(0).setCellValue("姓名");
			firstRow6.createCell(1).setCellValue("身份证号");
			firstRow6.createCell(2).setCellValue("获取日期");
			firstRow6.createCell(3).setCellValue("证书名称");
			firstRow6.createCell(4).setCellValue("证书等级");
			firstRow6.createCell(5).setCellValue("发证机构");
			firstRow6.createCell(6).setCellValue("证书描述");
			firstRow6.createCell(7).setCellValue("证书分类");
			firstRow6.createCell(8).setCellValue("证书文件名");
			//设置时间格式 获取日期
			setExcelDate(workBook, sheet6, new int[]{2});

			XSSFSheet sheet7 = workBook.createSheet("部门职位调整");
			XSSFRow firstRow7 = sheet7.createRow(0);
			firstRow7.createCell(0).setCellValue("姓名");
			firstRow7.createCell(1).setCellValue("身份证号");
			firstRow7.createCell(2).setCellValue("一级部门");
			firstRow7.createCell(3).setCellValue("二级部门");
			firstRow7.createCell(4).setCellValue("职位");
			firstRow7.createCell(5).setCellValue("职级");
			firstRow7.createCell(6).setCellValue("生效时间");
			//设置时间格式 获取日期
			setExcelDate(workBook, sheet7, new int[]{6});

			//部门设置名称管理器
			createNamedRange(workBook, employeeDvHelper, sheet, 3,4);
			//部门设置名称管理器
			createNamedRange(workBook, employeeDvHelper, sheet7, 2, 3);

		} catch (Exception e) {
			logger.error("downloadExportTemplate方法出现异常：",e.getMessage(),e);
		}
		str = this.exportExcelToComputer(workBook);
		return str;
	}

	/**
	 *
	 * @param workBook
	 * @param employeeDvHelper
	 * @param sheet
	 * @param first 一级部门列号
	 * @param second 二级部门列号
	 */
	private void createNamedRange(XSSFWorkbook workBook, XSSFDataValidationHelper employeeDvHelper, XSSFSheet sheet, int first, int second) {

		String hiddenSheetName = "departmentNames";
		XSSFSheet hiddenSheet = workBook.getSheet(hiddenSheetName);
		if(hiddenSheet == null){
			hiddenSheet = workBook.createSheet(hiddenSheetName);
		}
		workBook.setSheetHidden(7,true);
		int rowNum = 1;
		List<Map<String,Object>> firstDepartmentlist = erpDepartmentMapper.findFirstDepartments();
		for(Map<String,Object> firstDepartment : firstDepartmentlist) {
			String rangeName = "Name_" + rowNum;//名称管理器命名
			XSSFRow row = hiddenSheet.createRow(rowNum++);
			int colNum = 0;
			row.createCell(colNum++).setCellValue(firstDepartment.get("departmentName").toString());//第一列是一级部门名称
			row.createCell(colNum++).setCellValue(rangeName);//第二列是对应的名称管理器
			List<Map<String,Object>> secondDepartmentlist = erpDepartmentMapper.findSecondDepByFirstDep(Integer.valueOf(firstDepartment.get("departmentId").toString()));
			// 添加名称管理器
			if(workBook.getName(rangeName) == null){
				Name name = workBook.createName();
				name.setNameName(rangeName);
				String formula = hiddenSheetName + "!" + getRange(2, rowNum, secondDepartmentlist.size());
				name.setRefersToFormula(formula);
			}
			XSSFDataValidationConstraint departmentDvConstraint = (XSSFDataValidationConstraint) employeeDvHelper.createFormulaListConstraint("departmentNames!$A$2:$A$999");
			CellRangeAddressList departmentAddressList = new CellRangeAddressList(1, 9999, first, first);
			XSSFDataValidation departmentValidation = (XSSFDataValidation) employeeDvHelper.createValidation(departmentDvConstraint, departmentAddressList);
			departmentValidation.setShowErrorBox(true);
			sheet.addValidationData(departmentValidation);
			int rowNumber = 0;
			for(Map<String,Object> secondDepartment : secondDepartmentlist) {
				row.createCell(colNum++).setCellValue(secondDepartment.get("departmentName").toString());//下一列是二级部门名称
				String secondFormula ="INDIRECT(VLOOKUP($"+getColName(first)+""+(rowNumber+2)+",departmentNames!$A$2:$B$999,2,FALSE))";
				XSSFDataValidationConstraint secondDepartmentDvConstraint = (XSSFDataValidationConstraint) employeeDvHelper.createFormulaListConstraint(secondFormula);
				CellRangeAddressList secondDepartmentAddressList = new CellRangeAddressList(1, 9999, second, second);
				XSSFDataValidation secondDepartmentValidation = (XSSFDataValidation) employeeDvHelper.createValidation(secondDepartmentDvConstraint, secondDepartmentAddressList);
				secondDepartmentValidation.setShowErrorBox(true);
				sheet.addValidationData(secondDepartmentValidation);
				rowNumber++;
			}

		}
	}

	/**
	 * 加载下拉列表内容
	 * @param formulaString
	 * @param naturalRowIndex
	 * @param naturalColumnIndex
	 * @param dvHelper
	 * @return
	 */
	private static  DataValidation getDataValidationByFormula(
			String formulaString, int naturalRowIndex, int naturalColumnIndex,XSSFDataValidationHelper dvHelper) {
		// 加载下拉列表内容
		// 举例：若formulaString = "INDIRECT($A$2)" 表示规则数据会从名称管理器中获取key与单元格 A2 值相同的数据，
		//如果A2是江苏省，那么此处就是江苏省下的市信息。
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(formulaString);
		// 设置数据有效性加载在哪个单元格上。
		// 四个参数分别是：起始行、终止行、起始列、终止列
		int firstRow = naturalRowIndex -1;
		int lastRow = naturalRowIndex - 1;
		int firstCol = naturalColumnIndex - 1;
		int lastCol = naturalColumnIndex - 1;
		CellRangeAddressList regions = new CellRangeAddressList(firstRow,
				lastRow, firstCol, lastCol);
		// 数据有效性对象
		// 绑定
		XSSFDataValidation data_validation_list = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
		data_validation_list.setEmptyCellAllowed(false);
		if (data_validation_list instanceof XSSFDataValidation) {
			data_validation_list.setSuppressDropDownArrow(true);
			data_validation_list.setShowErrorBox(true);
		} else {
			data_validation_list.setSuppressDropDownArrow(false);
		}
		// 设置输入错误提示信息
		data_validation_list.createErrorBox("选择错误提示", "你输入的值未在备选列表中，请下拉选择合适的值！");
		return data_validation_list;
	}
	/**
	 * 设置excell下拉联动事件
	 * @param offset 偏移量，如果给0，表示从A列开始，1，就是从B列
	 * @param rowId 第几行
	 * @param colCount 一共多少列
	 * @return 如果给入参 1,1,10. 表示从B1-K1。最终返回 $B$1:$K$1
	 *
	 */
	public  String getRange(int offset, int rowId, int colCount) {
		char start = (char)('A' + offset);
		if (colCount <= 25) {
			char end = (char)(start + colCount - 1);
			return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
		} else {
			char endPrefix = 'A';
			char endSuffix = 'A';
			if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
				if ((colCount - 25) % 26 == 0) {// 边界值
					endSuffix = (char)('A' + 25);
				} else {
					endSuffix = (char)('A' + (colCount - 25) % 26 - 1);
				}
			} else {// 51以上
				if ((colCount - 25) % 26 == 0) {
					endSuffix = (char)('A' + 25);
					endPrefix = (char)(endPrefix + (colCount - 25) / 26 - 1);
				} else {
					endSuffix = (char)('A' + (colCount - 25) % 26 - 1);
					endPrefix = (char)(endPrefix + (colCount - 25) / 26);
				}
			}
			return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
		}
	}

	public  String getColName(int colCount) {
		char start = 'A';
		if (colCount <= 25) {
			char end = (char) (start + colCount );
			return String.valueOf(end);
		} else {
			char endPrefix = 'A';
			char endSuffix = 'A';
			if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
				if ((colCount - 25) % 26 == 0) {// 边界值
					endSuffix = (char) ('A' + 25);
				} else {
					endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
				}
			} else {// 51以上
				if ((colCount - 25) % 26 == 0) {
					endSuffix = (char) ('A' + 25);
					endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
				} else {
					endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
					endPrefix = (char) (endPrefix + (colCount - 25) / 26);
				}
			}
			return  String.valueOf(endPrefix) +String.valueOf(endSuffix);
		}
	}
	/**
	 * 设置excel列为时间格式
	 * @param workBook
	 * @param sheet
	 * @param cols 列号数组
	 */
	public void setExcelDate(XSSFWorkbook workBook, XSSFSheet sheet, int[] cols){
		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
		XSSFDataValidationConstraint dateDvConstraint = (XSSFDataValidationConstraint)dvHelper.createDateConstraint(
				DataValidationConstraint.OperatorType.BETWEEN,"date(1900,1,1)","date(2900,1,1)","yyyy-MM-dd");
		XSSFCellStyle dateStyle = workBook.createCellStyle();
		XSSFDataFormat format = workBook.createDataFormat();
		dateStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
		for(int col :cols){
			CellRangeAddressList dateAddressList = new CellRangeAddressList(1, 9999, col, col);
			XSSFDataValidation dateValidation = (XSSFDataValidation) dvHelper.createValidation(dateDvConstraint, dateAddressList);
			dateValidation.setShowErrorBox(true);
			sheet.addValidationData(dateValidation);
			sheet.setDefaultColumnStyle(col, dateStyle);
		}
	}

	/*--部门-员工导入模板导出  end --------------------------------------------------------------------------------------------*/

	/*
	 * 功能：导出测试 参数：workBook
	 */
	public String exportExcelToComputer(XSSFWorkbook workBook) {
		logger.info("exportExcelToComputer方法开始执行，传递参数:XSSFWorkbook");

		// 本地测试导出文件
		
		/*  try { FileOutputStream fos = new
		 FileOutputStream("D:\\test.xlsx");
		 workBook.write(fos); fos.flush(); fos.close(); } catch (FileNotFoundException
		 e1) { e1.printStackTrace(); } catch (IOException e) { }
*/
		// 与前端联调导出文件
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		response.addHeader("Content-Disposition", "attachment;filename=ModelExcel.xlsx");
		ServletOutputStream os;
		try {
			os = response.getOutputStream();
			workBook.write(os);
			os.flush();
			os.close();
		} catch (IOException e) {


			return "导出模板表格失败";
		}
		return "导出模板表格成功";
	}

	/**
	 * 通过员工id查找用户信息
	 * 
	 * @param empId 员工主键
	 * @return
	 */
	private RestResponse getUserInfoByEmpId(Integer empId) {
		String token = request.getHeader("token");
		// 根据用户Id调用权限工程获取 userId
		MultiValueMap<String, Object> erpUser = new LinkedMultiValueMap<String, Object>(); // 用户对象 map
		erpUser.add("userId", empId); // 参数
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("token", token);// 封装token
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser,
				requestHeaders);
//		String url = "https://nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId"; 
		String url = protocolType + "nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId";
		ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
		RestResponse response = responseEntity.getBody();
		// {result={"status":"200","msg":"新增成功 ！","data":""}, code=200}
		if (!"200".equals(response.getStatus())) {
			logger.error("调用权限工程获取用户信息失败" + response.getMsg());
			return RestUtils.returnFailure("调用权限工程获取用户信息失败" + response.getMsg());
		}
		RestResponse ResponseUser = responseEntity.getBody();
		return ResponseUser;
	}

	/**
	 * 某个部门下的员工信息的导出
	 * 
	 * @param depIdList 部门ID
	 * @return
	 */
	public String exportEmployeeInfoByDepId(List<Integer> depIdList) {
		logger.info("exportEmployeeInfoByDepId方法开始执行，传递参数:List:size=" + depIdList.size());

		String str = "";
		List<Map<String, Object>> resultList = null;
		try {
			if (depIdList.size() > 0) {
				// 查询一级部门下所有员工
				resultList = this.erpEmployeeMapper.selectAllEmployeeByDepId(depIdList);
			} else {
				Map<String, Object> queryMap = new HashMap<String, Object>();
				resultList = this.erpEmployeeMapper.selectAllEmployee(queryMap);
				for (Map<String, Object> map : resultList) {
					Integer employeeId = Integer.valueOf(String.valueOf(map.get("employeeId")));
					List<Map<String, Object>> certificateList = this.erpEmployeeMapper
							.findAllCertificateByEmp(employeeId);
					map.put("certificate", certificateList);
				}
			}
			str = this.exportEmployeeInfoUtil(resultList);
		} catch (Exception e) {
			logger.error("exportEmployeeInfoByDepId()方法出现异常：" + e.getMessage());
			str = "exportEmployeeInfoByDepId()方法出现异常：" + e.getMessage();
		}
		return str;
	}
	
    /**
	 * Object的数据转化成String类型
	 * @param clob
	 * @return
	 */
	public static String objectToString(Object object) {
		return object == null ? "" : String.valueOf(object);
	}
	/**
	 * 部门模块中员工在项信息
	 * 
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public RestResponse exportEmployeeInProjectInfo(String token,
			Map<String, Object> params) {
		logger.info("exportEmployeeInProjectInfo开始执行，参数={}",params);
		RestResponse projectInEmployees=ErpEmployeeService.employeeInProjectInfo(token,params);
		List<Map<String,Object>> list=(List<Map<String,Object>>) projectInEmployees.getData();
		// 定义工作簿
		XSSFWorkbook workBook = new XSSFWorkbook();
		XSSFSheet sheet = workBook.createSheet("项目人员统计");
		// 生成第一行
		XSSFRow firstRow = sheet.createRow(0);
		firstRow.createCell(0).setCellValue("序号");
		firstRow.createCell(1).setCellValue("员工姓名");
		firstRow.createCell(2).setCellValue("一级部门");
		firstRow.createCell(3).setCellValue("二级部门");
		firstRow.createCell(4).setCellValue("职位名称");
		firstRow.createCell(5).setCellValue("职级");
		firstRow.createCell(6).setCellValue("员工类型");
		firstRow.createCell(7).setCellValue("当前是否在项");
		firstRow.createCell(8).setCellValue("入项时间");
		firstRow.createCell(9).setCellValue("项目名称");
		firstRow.createCell(10).setCellValue("其他项目名称");
		// 第二行数据行
		XSSFRow nextRow = null;
		Map<String,Object> resultMap = null;
		int indexNumber = 1;
		// 循环 填充表格
		for (int dataRowNumber = 0; dataRowNumber < list.size(); dataRowNumber++) {
			resultMap = list.get(dataRowNumber);
			nextRow = sheet.createRow(dataRowNumber + 1);
			int cellNumber = 0;
			nextRow.createCell(cellNumber++).setCellValue(indexNumber++);
			nextRow.createCell(cellNumber++).setCellValue(resultMap.get("name") == null ? "" : String.valueOf(resultMap.get("name")));
			nextRow.createCell(cellNumber++).setCellValue(resultMap.get("firstDepartment") == null ? "" : String.valueOf(resultMap.get("firstDepartment")));
			nextRow.createCell(cellNumber++).setCellValue(resultMap.get("secondDepartment") == null ? "" : String.valueOf(resultMap.get("secondDepartment")));
			nextRow.createCell(cellNumber++).setCellValue(resultMap.get("position") == null ? "" : String.valueOf(resultMap.get("position")));
			nextRow.createCell(cellNumber++).setCellValue(resultMap.get("rank") == null ? "" : String.valueOf(resultMap.get("rank")));
			nextRow.createCell(cellNumber++).setCellValue(resultMap.get("statusName") == null ? "" : String.valueOf(resultMap.get("statusName")));
			Map<String,Object> empRelProjectMap=(Map<String,Object>) resultMap.get("empRelProjectMap");
			nextRow.createCell(cellNumber++).setCellValue(Boolean.valueOf(String.valueOf(empRelProjectMap.get("isFlag"))) == false ? "否" : "是");
			nextRow.createCell(cellNumber++).setCellValue(empRelProjectMap.get("startTime") == null ? "" : String.valueOf(empRelProjectMap.get("startTime")));
			nextRow.createCell(cellNumber++).setCellValue(empRelProjectMap.get("projectName") == null ? "" : String.valueOf(empRelProjectMap.get("projectName")));
			Map<String,Object> empWorkTimeMap=(Map<String, Object>)resultMap.get("empWorkTimeList");
			nextRow.createCell(cellNumber++).setCellValue(empWorkTimeMap == null ? "" : String.valueOf(empWorkTimeMap.get("otherProjectName")));
		}
		this.exportExcelToComputer(workBook);
		return RestUtils.returnSuccessWithString("导出成功！");
	}

}
