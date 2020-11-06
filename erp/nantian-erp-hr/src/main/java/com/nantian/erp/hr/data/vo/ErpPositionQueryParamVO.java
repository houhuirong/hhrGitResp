package com.nantian.erp.hr.data.vo;

/**
 * 
 * 岗位列表查询参数VO
 * @author xujianhao
 *
 */
public class ErpPositionQueryParamVO {
	
	/**
	 * 申请日期开始
	 */
	private String applyDateStart;
	
	/**
	 * 申请日期结束
	 */
	private String applyDateEnd;
	
	/**
	 * 申请人
	 */
	private String applyPerson;

	/**
	 * 接口人
	 */
	private String interfacePerson;

	/**
	 * HR负责人
	 */
	private String hrCharge;
	
	/**
	 * 一级需求部门
	 */
	private Integer firstDepartment;
	
	/**
	 * 二级需求部门
	 */
	private Integer secondDepartment;
	
	/**
	 * 岗位名称
	 */
	private String postName;
	
	/**
	 * 优先级
	 */
	private Integer rank;
	
	/**
	 * 状态（1 待审批 2：发布中 3：已关闭 6:暂停）
	 */
	private Integer status;
	
	/**
	 * 开始行号
	 */
	private Integer beginNumber;
	
	/**
	 * 结束行号
	 */
	private Integer endNumber;
	
	/**
	 * 岗位id
	 */
	private Integer postId;
	
	/**
	 * hr 负责人
	 */
	private Integer hrChargeId;

	public String getApplyDateStart() {
		return applyDateStart;
	}

	public void setApplyDateStart(String applyDateStart) {
		this.applyDateStart = applyDateStart;
	}

	public String getApplyDateEnd() {
		return applyDateEnd;
	}

	public void setApplyDateEnd(String applyDateEnd) {
		this.applyDateEnd = applyDateEnd;
	}

	public String getApplyPerson() {
		return applyPerson;
	}

	public void setApplyPerson(String applyPerson) {
		this.applyPerson = applyPerson;
	}

	public String getInterfacePerson() {
		return interfacePerson;
	}

	public void setInterfacePerson(String interfacePerson) {
		this.interfacePerson = interfacePerson;
	}

	public String getHrCharge() {
		return hrCharge;
	}

	public void setHrCharge(String hrCharge) {
		this.hrCharge = hrCharge;
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

	public Integer getBeginNumber() {
		return beginNumber;
	}

	public void setBeginNumber(Integer beginNumber) {
		this.beginNumber = beginNumber;
	}

	public Integer getEndNumber() {
		return endNumber;
	}

	public void setEndNumber(Integer endNumber) {
		this.endNumber = endNumber;
	}

	
	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getHrChargeId() {
		return hrChargeId;
	}

	public void setHrChargeId(Integer hrChargeId) {
		this.hrChargeId = hrChargeId;
	}

	@Override
	public String toString() {
		return "ErpPositionQueryParamVO [applyDateStart=" + applyDateStart + ", applyDateEnd=" + applyDateEnd
				+ ", applyPerson=" + applyPerson + ", interfacePerson=" + interfacePerson + ", hrCharge=" + hrCharge
				+ ", firstDepartment=" + firstDepartment + ", secondDepartment=" + secondDepartment + ", postName="
				+ postName + ", rank=" + rank + ", status=" + status + ", beginNumber=" + beginNumber + ", endNumber="
				+ endNumber + "]";
	}
	
	
}
