package com.nantian.erp.hr.data.model;

/** 
 * Description: 简历表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpResume {
	//简历ID
    private Integer resumeId;
    //姓名
    private String name;
    //出生年月
    private String birthday;
    //手机号码
    private String phone;
    //性别
    private String sex;
    //工作经验
    private String experience;
    //学历
    private String degree;
    //求职方向
    private String jobDirection;
    //简历是否失效
    private Boolean isValid;
    //简历状态
    private String status;
    //附件名字
    private String fileName;
    //邮箱地址
    private String email;
    //备注信息
    private String remark;
    //是否是实习生
    private Boolean isTrainee;
    //毕业学校
    private String school;
    //简历录入人员
    private Integer createPersonId;
    
    
	public Integer getCreatePersonId() {
		return createPersonId;
	}
	public void setCreatePersonId(Integer createPersonId) {
		this.createPersonId = createPersonId;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public Integer getResumeId() {
		return resumeId;
	}
	public void setResumeId(Integer resumeId) {
		this.resumeId = resumeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name.trim();
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone.trim();
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getExperience() {
		return experience;
	}
	public void setExperience(String experience) {
		this.experience = experience;
	}
	public String getDegree() {
		return degree;
	}
	public void setDegree(String degree) {
		this.degree = degree;
	}
	public String getJobDirection() {
		return jobDirection;
	}
	public void setJobDirection(String jobDirection) {
		this.jobDirection = jobDirection;
	}
	public Boolean getIsValid() {
		return isValid;
	}
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email.trim();
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Boolean getIsTrainee() {
		return isTrainee;
	}
	public void setIsTrainee(Boolean isTrainee) {
		this.isTrainee = isTrainee;
	}
	@Override
	public String toString() {
		return "ErpResume [resumeId=" + resumeId + ", name=" + name + ", birthday=" + birthday + ", phone=" + phone
				+ ", sex=" + sex + ", experience=" + experience + ", degree=" + degree + ", jobDirection="
				+ jobDirection + ", isValid=" + isValid + ", status=" + status + ", fileName=" + fileName + ", email="
				+ email + ", remark=" + remark + ", isTrainee=" + isTrainee + ", school=" + school + ",createPersonId="
						+ createPersonId+"]";
	}

    
}