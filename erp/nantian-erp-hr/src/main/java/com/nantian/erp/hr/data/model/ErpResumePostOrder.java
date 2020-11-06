package com.nantian.erp.hr.data.model;

/** 
 * Description: 面试预约信息表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpResumePostOrder {
	//面试预约信息编号
	private Integer id;
	//面试流程表编号
	private Integer interviewId;
	//面试预约注意事项（预约注意事项可以为空，不添加到面试记录中，仅供HR预约面试环节查看）
	private String attention;
	//面试联系人（字典表中的类型INTERVIEW_ORDER_CONTACT）
	private String contactId;
	//面试地点（字典表中的类型INTERVIEW_ORDER_PLACE）
	private String placeId;
	//面试方式（现场面试、电话面试）
	private String method;
	//面试时间
	private String time;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getInterviewId() {
		return interviewId;
	}
	public void setInterviewId(Integer interviewId) {
		this.interviewId = interviewId;
	}
	public String getAttention() {
		return attention;
	}
	public void setAttention(String attention) {
		this.attention = attention;
	}
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
	public String getPlaceId() {
		return placeId;
	}
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return "ErpResumePostOrder [id=" + id + ", interviewId=" + interviewId + ", attention=" + attention
				+ ", contactId=" + contactId + ", placeId=" + placeId + ", method=" + method + ", time=" + time + "]";
	}
	
}
