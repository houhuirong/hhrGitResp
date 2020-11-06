package com.nantian.erp.hr.data.model;

/** 
 * Description: 面试流程表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpResumePost {
	//面试流程编号
	private Integer id;
	//简历编号
	private Integer resumeId;
	//岗位编号
	private Integer postId;
	//当前处理人用户编号（初始值为0，表示处理人为全部HR）
	private Integer personId;
	//面试状态（字典表中dic_type为INTERVIEW_STATUS_TYPE）
	private String status;
	//流程是否有效（0表示流程失效，1表示流程有效）
	private Boolean isValid;
	//面试环节（0表示初试，1表示复试，2表示实习生面试）
	private String segment;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getResumeId() {
		return resumeId;
	}
	public void setResumeId(Integer resumeId) {
		this.resumeId = resumeId;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	public Integer getPersonId() {
		return personId;
	}
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Boolean getIsValid() {
		return isValid;
	}
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	public String getSegment() {
		return segment;
	}
	public void setSegment(String segment) {
		this.segment = segment;
	}
	
	@Override
	public String toString() {
		return "ErpResumePost [id=" + id + ", resumeId=" + resumeId + ", postId=" + postId + ", personId=" + personId
				+ ", status=" + status + ", isValid=" + isValid + ", segment=" + segment + "]";
	}
	
}
