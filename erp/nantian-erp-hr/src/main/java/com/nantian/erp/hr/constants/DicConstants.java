package com.nantian.erp.hr.constants;

/** 
 * Description: ERP人力资源相关字典表常量类
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年09月11日      		ZhangYuWei          1.0       
 * </pre>
 */
public class DicConstants {

	//================================= 短信验证常量 -开始   ================================
	public static final Integer APPID = 1400185166;//短信应用SDK AppID
	public static final String APPKEY = "80ba8c0f8c1118e84152bc3b759c2ad1";//短信应用SDK AppKey
	public static final Integer TEMPLATE_ID = 280134;//短信模板ID，需要在短信应用中申请
	public static final String SMSSIGN = "员工服务";//签名

	public static final Integer  DEPARTENTMENT_TEMPLATE_ID = 603709;//部门修改负责人短信模板ID，需要在短信应用中申请
	public static final String DEPARTENTMENT_SMSSIGN = "部门管理";//签名


	//================================= 短信验证常量 -结束   ================================

	//================================ 简历状态常量 -开始  ====================================
	public static final  String RESUME_STATUS_TYPE = "RESUME_STATUS_TYPE";
	public static final  String RESUME_STATUS_RECOMMENDED = "0";//可推荐
	public static final  String RESUME_STATUS_IN_THE_INTERVIEW = "1";//面试中
	public static final  String RESUME_STATUS_NO_PASS = "2";//不通过
	public static final  String RESUME_STATUS_PASS = "3";//通过
	public static final  String RESUME_STATUS_SENT_OFFER = "4";//已发offer
	public static final  String RESUME_STATUS_ENTRY = "5";//已入职
	//================================ 简历状态常量 -结束  ====================================
	
	//================================ 面试状态常量 -开始  ====================================
	public static final  String INTERVIEW_STATUS_TYPE = "INTERVIEW_STATUS_TYPE";
	public static final  String INTERVIEW_STATUS_RESUME_SCREENING = "0";//简历待筛选
	public static final  String INTERVIEW_STATUS_ORDER_INTERVIEW = "1";//待预约
	public static final  String INTERVIEW_STATUS_IN_THE_INTERVIEW = "2";//面试中
	public static final  String INTERVIEW_STATUS_NO_PASS = "3";//面试不通过
	public static final  String INTERVIEW_STATUS_PASS = "4";//面试通过
	public static final  String INTERVIEW_STATUS_OFFER_APPROVE = "5";//offer待审批
	//================================ 面试状态常量 -结束  ====================================
	
	//==================================== 面试环节常量 -开始 =========================================
	public static final  String INTERVIEW_SEGMENT_TYPE = "INTERVIEW_SEGMENT_TYPE";
	public static final  String INTERVIEW_SEGMENT_SOCIAL_FIRST = "0";//初试
	public static final  String INTERVIEW_SEGMENT_SOCIAL_REEXAM = "1";//复试
	public static final  String INTERVIEW_SEGMENT_TRAINEE_INTERVIEW = "2";//实习生面试
	//==================================== 面试环节常量 -结束 =========================================
	
	//==================================== 面试方式常量 -开始 =========================================
	public static final  String INTERVIEW_ORDER_METHOD = "INTERVIEW_ORDER_METHOD";
	public static final  String INTERVIEW_ORDER_METHOD_PLACE = "0";//现场面试
	public static final  String INTERVIEW_ORDER_METHOD_PHONE = "1";//电话面试
	//==================================== 面试方式常量 -结束 =========================================
	
	//==================================== offer状态常量 -开始 =========================================
	public static final  String OFFER_STATUS_TYPE = "OFFER_STATUS_TYPE";
	public static final  String OFFER_STATUS_WAITING = "0";//待处理
	public static final  String OFFER_STATUS_VALID = "1";//发布中的有效offer
	public static final  String OFFER_STATUS_INVALID = "2";//已关闭的归档offer
	public static final  String NO_SENDOFFER_INVALID = "3";//未发offer的归档offer
	public static final  String CANCER_ENTRY_INVALID = "4";//取消入职的归档offer
	//==================================== offer状态常量 -结束 =========================================
	
	//================================ 员工状态常量 -开始  ====================================
	public static final  String EMPLOYEE_STATUS_TYPE = "EMPLOYEE_STATUS_TYPE";
	public static final  String EMPLOYEE_STATUS_TRAINEE = "0";//实习生
	public static final  String EMPLOYEE_STATUS_PROBATION = "1";//试用期员工
	public static final  String EMPLOYEE_STATUS_FORMAL = "2";//正式员工
	public static final  String EMPLOYEE_STATUS_DIMISSION_ING = "3";//离职中
	public static final  String EMPLOYEE_STATUS_DIMISSION_ED = "4";//已离职
	//================================ 员工状态常量 -结束  ====================================
	
	public static final  String CERTIFICATE_PATH = "/erpftp/certificate";//服务器证书地址
	public static final  String EDUCATION_EXPERIENCE_PATH = "/erpftp/educationExperience";//服务器教育经历地址
	public static final  String EMPLOYEE_FILE_PATH = "/erpftp/employeeFile";//服务器员工文件地址（身份证正反面、个人照片）
	public static final  String EXPENSEREIMBURSEMENT_PATH = "/erpftp/expenseReimbursement";//费用报销地址
	
	//=========================== 岗位类别/族/职位类别/职位子类常量 ==============================
	public static final  String  POST_CATEGORY = "POST_CATEGORY"; //岗位类别
	public static final  String  JOB_CATEGORY = "JOB_CATEGORY_TECH"; //职位类别
	public static final  String  JOB_FAMILY = "JOB_CATEGORY_TYPE"; //职位族
	public static final  String  TECH_FAMILY_CODE = "JOB_CATEGORY_TECH"; //技术族码值
	public static final  String  MGT_FAMILY_CODE = "JOB_CATEGORY_MANAGE"; //管理族码值
	public static final  String  LEVEL_PRIORITY = "POST_LEVEL_PRIORITY"; //优先级
	public static final  String  REASON_RECRUIT = "POST_REASON_RECRUIT"; //招聘原因
	
	//================================ 加密解密证书文件常量 -开始  ====================================
	public static final String RSA_CERPATH = "/usr/local/nantian/rsa/mypublickey.cer";		//证书文件路径
	public static final String RSA_STOREPATH = "/usr/local/nantian/rsa/mykeystore.keystore";	//证书库文件路径
	public static final String RSA_ALIAS = "mykey";		//证书别名
	public static final String RSA_STOREPW= "ymxsy2008";	//证书库密码
	public static final String RSA_KEYPW = "ymxwy2008";	//证书密码
	//================================ 加密解密证书文件常量 -结束  ====================================
	
	//===================审批人开始=============================================
	public static final String APPROVE = "APPROVER";
	//===================审批人结束=============================================
	
	//================================ 动态生成offer信息Excel文件的临时路径 -开始  ====================================
	public static final String ENTER_OFFER_TEMP_PATH = "/usr/local/nantian/temp/enteroffer";//录入offer时的文件临时路径
	//public static final String SEND_OFFER_TEMP_PATH = "/usr/local/nantian/temp/sendoffer";//发送offer时的文件临时路径
	public static final String RESUME_PDF_TEMP = "/home/erpftpu/resume/pdf";//存放简历pdf文件临时路径
	//================================ 动态生成offer信息Excel文件的临时路径 -结束  ====================================
	
	public static final  String EXPORT_RESUME_TEMP_PATH = "/usr/local/nantian/temp/exportresume";//导出员工简历的临时路径
	public static final  String PACK_RESUME_TEMP_PATH = "/usr/local/nantian/temp/packresume";//导出多个员工简历压缩包的临时路径
	public static final  String PREVIEW_RESUME_TEMP_PATH = "/usr/local/nantian/temp/previewresume";//预览员工简历的临时路径
	public static final  String EXPORT_POST_TEMP_PATH = "/usr/local/nantian/temp/exportpost";//岗位信息的临时路径
	
	//=================部门类型开始================================
		public static final String DEPARTMENT_TYPE = "DEPARTMENT_TYPE";
	//=================部门类型结束================================

	//=================邮箱信息================================
	public static final String EMAIL_USERNAME = "EMAIL_USERNAME";
	public static final String EMAIL_PASSWORD = "EMAIL_PASSWORD";
	//=================邮箱信息================================

	//===================部门调动申请状态开始=============================================
	public static final String TSAPPLY_WAITING = "1"; //等待审批
	public static final String TSAPPLY_LOADDING = "2"; //审批中
	public static final String TSAPPLY_SUCCESS = "3"; //审批通过
	public static final String TSAPPLY_FAIL = "4";    //审批未通过-待修改
	public static final String TSAPPLY_END = "5";    //部门调整完成
	//===================部门调动申请状态结束=============================================
	
	//=================== Redis的key值前缀 =============================================
	public static final String REDIS_PREFIX_EMPLOYEE = "employee_"; //员工信息
	public static final String REDIS_PREFIX_DEPARTMENT = "department_"; //部门信息
	public static final String REDIS_PREFIX_EMPLOYEE_SMS = "employee_sms_"; //员工短信redis
	public static final String REDIS_PREFIX_USER = "user_employee_"; //用户信息(key为employeeId，v为邮箱手机号)

	//=================== Redis的key值前缀 =============================================

	//===================部门调动邮件服务类型=============================================
	public static final Integer DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE = 0; //部门调动审批OA邮件服务类型
	public static final Integer DEPARTMENT_TRANSF_APPLY_EMAIL_TYPE = 1; //部门调动申请邮件服务类型
	public static final Integer DEP_TRANSF_OUT_APPROVAL_EMAIL_TYPE=2;//调出部门审批邮件
	public static final Integer DEP_TRANSF_IN_APPROVAL_EMAIL_TYPE=3;//调入部门审批邮件
	public static final Integer DEP_TRANSF_APPLY_UPDATE_EMAIL_TYPE=4;//修改部门调动申请邮件
	public static final Integer INTERVIEW_INVITATION_EMAIL_TYPE=5;//面试邀请邮件
	public static final Integer MODIFY_MOBILE_NUMBER_EMAIL_TYPE=6;//修改手机号邮件
	public static final Integer POSITION_APPLICATION_APPROVED_EMAIL_TYPE=7;//岗位申请审批通过邮件
	public static final Integer SEND_OFFER_EMAIL_TYPE=8;//发送offer邮件


	//===================部门调动邮件服务类型=============================================

}
