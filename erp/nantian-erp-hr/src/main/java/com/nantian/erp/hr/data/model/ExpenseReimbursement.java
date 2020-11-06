package com.nantian.erp.hr.data.model;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class ExpenseReimbursement {
	private MultipartFile PhotoFile;
	private MultipartFile[] attachment;
	private String title;
	private String notes;
	private Integer id;
	private List<String> deleteFileList;
	public MultipartFile getPhotoFile() {
		return PhotoFile;
	}
	public void setPhotoFile(MultipartFile photoFile) {
		PhotoFile = photoFile;
	}
	public MultipartFile[] getAttachment() {
		return attachment;
	}
	public void setAttachment(MultipartFile[] attachment) {
		this.attachment = attachment;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public List<String> getDeleteFileList() {
		return deleteFileList;
	}
	public void setDeleteFileList(List<String> deleteFileList) {
		this.deleteFileList = deleteFileList;
	}
	@Override
	public String toString() {
		return "ExpenseReimbursement [PhotoFile=" + PhotoFile + ", attachment=" + Arrays.toString(attachment)
				+ ", title=" + title + ", notes=" + notes + ", id=" + id + ", deleteFileList=" + deleteFileList + "]";
	}
	
}
