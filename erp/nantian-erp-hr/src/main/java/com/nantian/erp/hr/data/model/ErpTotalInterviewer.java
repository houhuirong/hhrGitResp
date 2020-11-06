package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="total_interviewer")
public class ErpTotalInterviewer {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name="resumeId")
	private Integer resumeId;
	@Column(name="postId")
	private Integer postId;
	
	
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
	@Override
	public String toString() {
		return "ErpTotalInterviewer [id=" + id + ", resumeId=" + resumeId + ", postId=" + postId + "]";
	}
	
}
