package com.nantian.erp.salary.constants;

/**
 * @author 曹秀斌
 * ERP员工薪酬相关字典表常量类
 * 2018-09-14
 */
public class DicConstants {
	
	//================================ 员工是否是应届毕业生标识 -开始  ====================================
	public static final  String EMPLOYEE_TAKEJOBTIME_YEAR = "2017";
	//================================ 员工是否是应届毕业生标识 -结束  ====================================
	
	//================================ 员工状态常量 -开始  ====================================
	public static final  String EMPLOYEE_STATUS_TYPE = "EMPLOYEE_STATUS_TYPE";
	public static final  String EMPLOYEE_STATUS_TRAINEE = "0";//实习生
	public static final  String EMPLOYEE_STATUS_PROBATION = "1";//试用期员工
	public static final  String EMPLOYEE_STATUS_FORMAL = "2";//正式员工
	public static final  String EMPLOYEE_STATUS_DIMISSION_ING = "3";//离职中
	public static final  String EMPLOYEE_STATUS_DIMISSION_ED = "4";//已离职
	//================================ 面员工状态常量 -结束  ====================================

	//================================ 月度绩效状态常量 -开始  ====================================

	public static final  String MONTH_PERFORMANCE_APPLY_STATUS_MODIFY = "1";//修改中
	public static final  String MONTH_PERFORMANCE_APPLY_STATUS_SUBMIT = "2";//已提交
	public static final  String MONTH_PERFORMANCE_APPLY_STATUS_LOCK = "3";//锁定
	public static final  String MONTH_PERFORMANCE_APPLY_STATUS_FILE = "4";//归档
	//================================ 月度绩效状态常量 -结束  ====================================


	//================================ 加密解密证书文件常量 -开始  ====================================
	public static final String RSA_CERPATH = "/usr/local/nantian/rsa/mypublickey.cer";		//证书文件路径
	public static final String RSA_STOREPATH = "/usr/local/nantian/rsa/mykeystore.keystore";	//证书库文件路径
	public static final String RSA_ALIAS = "mykey";		//证书别名
	public static final String RSA_STOREPW= "ymxsy2008";	//证书库密码
	public static final String RSA_KEYPW = "ymxwy2008";	//证书密码
	//================================ 加密解密证书文件常量 -结束  ====================================


	//================================ 一级部门调薪状态常量 -开始  ====================================

	public static final  Integer DEPARTMENT_SALARY_ADJUST_STATUS_SAVE = 0;//未提交
	public static final  Integer DEPARTMENT_SALARY_ADJUST_STATUS_SUBMIT = 1;//已提交
	public static final  Integer DEPARTMENT_SALARY_ADJUST_STATUS_CONFIRM = 2;//已确认
	//================================ 一级部门调薪状态常量 -结束  ====================================


	//================================ 员工调薪状态常量 -开始  ====================================

	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_SUBMIT = 0;//已提交(待副总裁审批)
	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_PASS = 1;//审批通过
	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_MODIFY_PASS = 11;//修订通过
	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_SAVE = 2;//暂存
	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_REJECT = 4;//驳回
	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_WAIT_BOSS = 5;//待总裁审批
	public static final  Integer EMPLOYEE_SALARY_ADJUST_STATUS_MODIFY_WAIT_BOSS = 51;//修订待总裁审批

	//================================ 员工调薪状态常量 -结束  ====================================

}
