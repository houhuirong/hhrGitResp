package com.nantian.erp.hr.data.vo;

/**
 * 
 * 岗位列表查询返回参数VO
 * @author xujianhao
 *
 */
public class ErpPositionQueryResultVO {
	
	/**
	 * 岗位id
	 */
	private Integer postId;
	/**
	 * 申请人 Id
	 */
	private Integer applyId;

	/**
	 * 申请人名称
	 */
	private String applyPersonName;
	
	/**
	 * 申请日期
	 */
	private String applyDate;
	
	/**
	 * 一级部门名称
	 */
	private String firstDepartmentName;

	/**
	 * 二级部门名称
	 */
	private String secondDepartmentName;
	
	/**
	 * 岗位类别名称
	 */
	private String postCategoryName;
	
	/**
	 * 岗位名称
	 */
	private String postName;

	/**
	 * 职级
	 */
	private Integer rank;
	
	/**
	 * 状态（1：待上级领导审批 2：发布中 3：已关闭 4:待一级部门经理审批 5:待提交 6:暂停）
	 */
	private Integer status;
	
	/**
	 * 状态名称（1：待上级领导审批 2：发布中 3：已关闭 4:待一级部门经理审批 5:待提交 6:暂停）
	 */
	private String statusName;

	/**
	 * 招聘人数
	 */
	private Integer numberPeople;
	
	/**
	 * 当前处理人姓名
	 */
	private String currentPersonName;
	
	/**
	 * 接口人姓名
	 */
	private String principalName;

	
	/**
	 * 优先级
	 */
	private String levelPriority;

	/**
	 * 优先级Id
	 */
	private Integer levelPriorityId;

	/**
	 * hr负责人
	 */
	private String hrCharge;
	
	/**
	 * 总面试数
	 */
	private Integer totalInterView;
	
	/**
	 *已发offer人数
	 */
	private Integer offerNum;
	
	/**
	 * 已入职人数
	 */
	private Integer entryNum;

	/**
	 * 招聘原因
	 */
	private String reasonRecruit;

	/**
	 * 薪资范围
	 */
	private String salaryRange;

	public Integer getPostId() {
		return postId;
	}


	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getApplyId() {
		return applyId;
	}


	public void setApplyId(Integer applyId) {
		this.applyId = applyId;
	}
	
	public String getApplyPersonName() {
		return applyPersonName;
	}


	public void setApplyPersonName(String applyPersonName) {
		this.applyPersonName = applyPersonName;
	}


	public String getApplyDate() {
		return applyDate;
	}


	public void setApplyDate(String applyDate) {
		this.applyDate = applyDate;
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


	public String getPostCategoryName() {
		return postCategoryName;
	}


	public void setPostCategoryName(String postCategoryName) {
		this.postCategoryName = postCategoryName;
	}


	public String getPostName() {
		return postName;
	}


	public void setPostName(String postName) {
		this.postName = postName;
	}


	public Integer getRank() {
		return rank;
	}


	public void setRank(Integer rank) {
		this.rank = rank;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public String getStatusName() {
		return statusName;
	}


	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}


	public Integer getNumberPeople() {
		return numberPeople;
	}


	public void setNumberPeople(Integer numberPeople) {
		this.numberPeople = numberPeople;
	}


	public String getCurrentPersonName() {
		return currentPersonName;
	}


	public void setCurrentPersonName(String currentPersonName) {
		this.currentPersonName = currentPersonName;
	}


	public String getPrincipalName() {
		return principalName;
	}


	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}


	public String getLevelPriority() {
		return levelPriority;
	}


	public void setLevelPriority(String levelPriority) {
		this.levelPriority = levelPriority;
	}


	public String getHrCharge() {
		return hrCharge;
	}


	public void setHrCharge(String hrCharge) {
		this.hrCharge = hrCharge;
	}


	public Integer getTotalInterView() {
		return totalInterView;
	}


	public void setTotalInterView(Integer totalInterView) {
		this.totalInterView = totalInterView;
	}


	public Integer getOfferNum() {
		return offerNum;
	}


	public void setOfferNum(Integer offerNum) {
		this.offerNum = offerNum;
	}


	public Integer getEntryNum() {
		return entryNum;
	}


	public void setEntryNum(Integer entryNum) {
		this.entryNum = entryNum;
	}

	public String getReasonRecruit() {
		return reasonRecruit;
	}

	public void setReasonRecruit(String reasonRecruit) {
		this.reasonRecruit = reasonRecruit;
	}

	public Integer getLevelPriorityId() {
		return levelPriorityId;
	}

	public void setLevelPriorityId(Integer levelPriorityId) {
		this.levelPriorityId = levelPriorityId;
	}

	public String getSalaryRange() {
		return salaryRange;
	}

	public void setSalaryRange(String salaryRange) {
		this.salaryRange = salaryRange;
	}
}
