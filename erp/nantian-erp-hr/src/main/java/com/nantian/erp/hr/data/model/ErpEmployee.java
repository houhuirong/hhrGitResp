package com.nantian.erp.hr.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
@Table(name="employee")
public class ErpEmployee implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -212598163485332549L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer employeeId;
	
	private String name;
	
	//add by lixu
	
	private String socialSecurity; //社保地
	
	private String sex;
	
	private Integer firstDepartment;
	
	private Integer secondDepartment;
	
	private String position;
	
	private Integer rank;
	
	private Integer resumeId;
	
	private Boolean isActive;
	
	private Integer offerId;
	
	private String personalEmail;
	
	private Integer projectInfoId;
	
	//@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String entryTime;
	
	//@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String takeJobTime;
	
	private String idCardNumber;
	
	private String school;
	
	private String major;
	
	private String education;
	
	private String status;

	private String statusName;
	
	private String empFinanceNumber;//财务序号
	
	private Boolean postPayrollStatus;//上岗工资单的状态（true表示已处理，false表示未处理）
	
	private Boolean positivePayrollStatus;//转正工资单的状态（true表示已处理，false表示未处理）

	private  String salaryCardNumber;
	
	private String firstDepartmentName;
	
	private String secondDepartmentName;
	//政治面貌
    private Integer politicalStatus;
    //民族
    private Integer groups;
	
	/**
	 * add by 张玉伟 20180917  前端传递过来的数据
	 */
	private String dm;//部门经理
	private String username;//部门经理
	
	//add by lixu 增加职位编号
	@Column(name = "positionId")
	private Integer positionId;
	@Transient
	private String userPhone; //个人手机号

	private String dimissionTime;
	
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
	public Integer getFirstDepartment() {
		return firstDepartment;
	}
	public void setFirstDepartment(Integer firstDepartment) {
		this.firstDepartment = firstDepartment;
	}
	public Integer getSecondDepartment() {
		return secondDepartment;
	}
	public void setSecondDepartment(Integer secondDepartment) {
		this.secondDepartment = secondDepartment;
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
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
	
	public String getSalaryCardNumber() {
		return salaryCardNumber;
	}
	public void setSalaryCardNumber(String salaryCardNumber) {
		this.salaryCardNumber = salaryCardNumber;
	}
	/*public String getPersonalEmail() {
		return personalEmail;
	}
	public void setPersonalEmail(String personalEmail) {
		this.personalEmail = personalEmail;
	}*/
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

	public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean active) {
		isActive = active;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getEmpFinanceNumber() {
		return empFinanceNumber;
	}
	public void setEmpFinanceNumber(String empFinanceNumber) {
		this.empFinanceNumber = empFinanceNumber;
	}
	public String getDm() {
		return dm;
	}
	public void setDm(String dm) {
		this.dm = dm;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Boolean getPostPayrollStatus() {
		return postPayrollStatus;
	}
	public void setPostPayrollStatus(Boolean postPayrollStatus) {
		this.postPayrollStatus = postPayrollStatus;
	}
	public Boolean getPositivePayrollStatus() {
		return positivePayrollStatus;
	}
	public void setPositivePayrollStatus(Boolean positivePayrollStatus) {
		this.positivePayrollStatus = positivePayrollStatus;
	}
	
	
	
	public Integer getPositionId() {
		return positionId;
	}
	public void setPositionId(Integer positionId) {
		this.positionId = positionId;
	}
	
	
	
	public String getSocialSecurity() {
		return socialSecurity;
	}
	public void setSocialSecurity(String socialSecurity) {
		this.socialSecurity = socialSecurity;
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
	
	public String getFirstDepartmentName() {
		return firstDepartmentName;
	}
	public void setFirstDepartmentName(String firstDepartmentName) {
		this.firstDepartmentName = firstDepartmentName;
	}
	public String getSecondDepartmentName() {
		return secondDepartmentName;
	}
	public void setSecondDepartmentName(String secondDepartmentName) {
		this.secondDepartmentName = secondDepartmentName;
	}
	public Integer getPoliticalStatus() {
		return politicalStatus;
	}
	public void setPoliticalStatus(Integer politicalStatus) {
		this.politicalStatus = politicalStatus;
	}
	public Integer getGroups() {
		return groups;
	}
	public void setGroups(Integer groups) {
		this.groups = groups;
	}

	public String getDimissionTime() {
		return dimissionTime;
	}

	public void setDimissionTime(String dimissionTime) {
		this.dimissionTime = dimissionTime;
	}

	@Override
	public String toString() {
		return "ErpEmployee [employeeId=" + employeeId + ", name=" + name + ", socialSecurity=" + socialSecurity
				+ ", sex=" + sex + ", firstDepartment=" + firstDepartment + ", secondDepartment=" + secondDepartment
				+ ", position=" + position + ", rank=" + rank + ", resumeId=" + resumeId + ", isActive=" + isActive
				+ ", offerId=" + offerId + ", personalEmail=" + personalEmail + ", projectInfoId=" + projectInfoId
				+ ", entryTime=" + entryTime + ", takeJobTime=" + takeJobTime + ", idCardNumber=" + idCardNumber
				+ ", school=" + school + ", major=" + major + ", education=" + education + ", status=" + status
				+ ", empFinanceNumber=" + empFinanceNumber + ", postPayrollStatus=" + postPayrollStatus
				+ ", positivePayrollStatus=" + positivePayrollStatus + ", salaryCardNumber=" + salaryCardNumber
				+ ", firstDepartmentName=" + firstDepartmentName + ", secondDepartmentName=" + secondDepartmentName
				+ ", dm=" + dm + ", username=" + username + ", positionId=" + positionId + ", userPhone=" + userPhone
				+ "]";
	}
	
}
