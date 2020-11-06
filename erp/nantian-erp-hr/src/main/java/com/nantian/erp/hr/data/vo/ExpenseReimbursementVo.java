package com.nantian.erp.hr.data.vo;

public class ExpenseReimbursementVo {
	private Integer id;
	private String title;
	private String PhotoPath;
	private String[] attachmentPath;	
	private String notes;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getPhotoPath() {
		return PhotoPath;
	}
	public void setPhotoPath(String photoPath) {
		PhotoPath = photoPath;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String[] getAttachmentPath() {
		return attachmentPath;
	}
	public void setAttachmentPath(String[] attachmentPath) {
		this.attachmentPath = attachmentPath;
	}
}
