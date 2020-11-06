package com.nantian.erp.hr.data.vo;

import java.util.List;
import java.util.Map;

/**
 * 查询所有的员工返回的VO 
 * @author Administrator
 *
 */
public class EmployeeVo {
	private Integer employeeId; //员工Id
	private String   name; //姓名
	private String   socialSecurity; //社保地
	private String   sex; //性别
	private String personalEmail ;//个人邮箱
	private String   position; //职位名称
	private Integer   rank; //职级
	private Integer   resumeId; //简历id
//	private String   isActive;//是否激活
	private boolean isActive;
	private Integer   offerId; //offerID
	private Integer   projectInfoId; //关联项目id
	private String   entryTime; //入职时间
	private String   takeJobTime; //参加工作时间
	private String   contractBeginTime; //合同开始时间
	private String   contractEndTime; //合同结束时间
	private String   probationEndTime; //试用期结束时间
	private String   salaryCardNumber; //工资卡号
	private String   idCardNumber; //身份证号
	private String   school; //毕业院校
	private String   major; //专业
	private String   education; //最高学历
	private String    status;  //员工类别状态 0-实习生 1-试用期员工 2-正式员工 3-离职中 4-已离职
	private String firstDepartment; //一级部门名称
	private String secondDepartment;//二级部门名称
	private Integer firstDepartmentId; //一级部门id
	private Integer secondDepartmentId;//二级部门id
	private String  statusName;
	private String positionFamilyName;
	private String positionTypeName;
	private String positionChildName;
	
	private String userPhone;  // 员工手机
	private String username ;//用户名-邮箱
	
	private String isDetail;//是否有权限查看员工详情

	private Boolean needSendSms;//修改手机号是否需要发送验证码

	private List<Map<String, Object>> certificateList;//证书列表


	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getSocialSecurity() {
		return socialSecurity;
	}
	public void setSocialSecurity(String socialSecurity) {
		this.socialSecurity = socialSecurity;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public Integer getResumeId() {
		return resumeId;
	}
	public void setResumeId(Integer resumeId) {
		this.resumeId = resumeId;
	}

	
/*	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}*/
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}
	public Integer getProjectInfoId() {
		return projectInfoId;
	}
	public void setProjectInfoId(Integer projectInfoId) {
		this.projectInfoId = projectInfoId;
	}
	public String getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}
	public String getTakeJobTime() {
		return takeJobTime;
	}
	public void setTakeJobTime(String takeJobTime) {
		this.takeJobTime = takeJobTime;
	}
	public String getContractBeginTime() {
		return contractBeginTime;
	}
	public void setContractBeginTime(String contractBeginTime) {
		this.contractBeginTime = contractBeginTime;
	}
	public String getContractEndTime() {
		return contractEndTime;
	}
	public void setContractEndTime(String contractEndTime) {
		this.contractEndTime = contractEndTime;
	}
	public String getProbationEndTime() {
		return probationEndTime;
	}
	public void setProbationEndTime(String probationEndTime) {
		this.probationEndTime = probationEndTime;
	}
	public String getSalaryCardNumber() {
		return salaryCardNumber;
	}
	public void setSalaryCardNumber(String salaryCardNumber) {
		this.salaryCardNumber = salaryCardNumber;
	}
	public String getIdCardNumber() {
		return idCardNumber;
	}
	public void setIdCardNumber(String idCardNumber) {
		this.idCardNumber = idCardNumber;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getEducation() {
		return education;
	}
	public void setEducation(String education) {
		this.education = education;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFirstDepartment() {
		return firstDepartment;
	}
	public void setFirstDepartment(String firstDepartment) {
		this.firstDepartment = firstDepartment;
	}
	public String getSecondDepartment() {
		return secondDepartment;
	}
	public void setSecondDepartment(String secondDepartment) {
		this.secondDepartment = secondDepartment;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public String getPersonalEmail() {
		return personalEmail;
	}
	public void setPersonalEmail(String personalEmail) {
		this.personalEmail = personalEmail;
	}
	public String getUserPhone() {
		return userPhone;
	}
	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getIsDetail() {
		return isDetail;
	}
	public void setIsDetail(String isDetail) {
		this.isDetail = isDetail;
	}
	public Integer getFirstDepartmentId() {
		return firstDepartmentId;
	}
	public void setFirstDepartmentId(Integer firstDepartmentId) {
		this.firstDepartmentId = firstDepartmentId;
	}
	public Integer getSecondDepartmentId() {
		return secondDepartmentId;
	}
	public void setSecondDepartmentId(Integer secondDepartmentId) {
		this.secondDepartmentId = secondDepartmentId;
	}
	
	public String getPositionFamilyName() {
		return positionFamilyName;
	}
	public void setPositionFamilyName(String positionFamilyName) {
		this.positionFamilyName = positionFamilyName;
	}
	public String getPositionTypeName() {
		return positionTypeName;
	}
	public void setPositionTypeName(String positionTypeName) {
		this.positionTypeName = positionTypeName;
	}
	public String getPositionChildName() {
		return positionChildName;
	}
	public void setPositionChildName(String positionChildName) {
		this.positionChildName = positionChildName;
	}

	public Boolean getNeedSendSms() {
		return needSendSms;
	}

	public void setNeedSendSms(Boolean needSendSms) {
		this.needSendSms = needSendSms;
	}

	public List<Map<String, Object>> getCertificateList() {
		return certificateList;
	}

	public void setCertificateList(List<Map<String, Object>> certificateList) {
		this.certificateList = certificateList;
	}

	@Override
	public String toString() {
		return "EmployeeVo [employeeId=" + employeeId + ", name=" + name
				+ ", socialSecurity=" + socialSecurity + ", sex=" + sex
				+ ", personalEmail=" + personalEmail + ", position=" + position
				+ ", rank=" + rank + ", resumeId=" + resumeId + ", isActive="
				+ isActive + ", offerId=" + offerId + ", projectInfoId="
				+ projectInfoId + ", entryTime=" + entryTime + ", takeJobTime="
				+ takeJobTime + ", contractBeginTime=" + contractBeginTime
				+ ", contractEndTime=" + contractEndTime
				+ ", probationEndTime=" + probationEndTime
				+ ", salaryCardNumber=" + salaryCardNumber + ", idCardNumber="
				+ idCardNumber + ", school=" + school + ", major=" + major
				+ ", education=" + education + ", status=" + status
				+ ", firstDepartment=" + firstDepartment
				+ ", secondDepartment=" + secondDepartment
				+ ", firstDepartmentId=" + firstDepartmentId
				+ ", secondDepartmentId=" + secondDepartmentId
				+ ", statusName=" + statusName + ", positionFamilyName="
				+ positionFamilyName + ", positionTypeName=" + positionTypeName
				+ ", positionChildName=" + positionChildName + ", userPhone="
				+ userPhone + ", username=" + username + ", isDetail="
				+ isDetail + "]";
	}
	
	
	
	
	
	
	
	
	
	
	
}
