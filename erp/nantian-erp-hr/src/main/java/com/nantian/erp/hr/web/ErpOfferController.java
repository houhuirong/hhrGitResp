package com.nantian.erp.hr.web;

import java.util.Map;

import com.nantian.erp.common.base.util.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpOffer;
import com.nantian.erp.hr.data.vo.ErpOfferVo;
import com.nantian.erp.hr.service.ErpOfferService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: offer的controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月13日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("/erp/offer")
@Api(value = "offer")
public class ErpOfferController {
	
	@Autowired
	private ErpOfferService erpOfferService;
	
	@RequestMapping(value="/waitingForMeQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "查询待处理的offer", notes = "无参数")
	public RestResponse waitingForMeQueryList(@RequestParam Boolean isTrainee) {
		RestResponse result = erpOfferService.waitingForMeQueryList(isTrainee);
		return result;
	}
	
	@RequestMapping(value="/validOfferQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "查询有效offer", notes = "无参数")
	public RestResponse validOfferQueryList(@RequestHeader String token, @RequestParam Boolean isTrainee) {
		RestResponse result = erpOfferService.validOfferQueryList(token, isTrainee);
		return result;
	}
	
	@RequestMapping(value="/invalidOfferQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "查询归档offer", notes = "无参数")
	public RestResponse invalidOfferQueryList(@RequestParam Boolean isTrainee) {
		RestResponse result = erpOfferService.invalidOfferQueryList(isTrainee);
		return result;
	}
	
	@RequestMapping(value = "/findOfferDetail", method = RequestMethod.GET)
	@ApiOperation(value = "查询一条offer的详情", notes = "参数是：istrainee、offerId")
	public RestResponse findOfferDetail(@RequestHeader String token,@RequestParam Boolean isTrainee,@RequestParam Integer offerId) {
		RestResponse result = erpOfferService.findOfferDetail(token,isTrainee,offerId);
		return result;
	}
	
	@RequestMapping(value = "/invalidOffer", method = RequestMethod.POST)
	@ApiOperation(value = "归档offer", notes = "参数是：offerId")
	public RestResponse invalidOffer(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpOfferService.invalidOffer(token,params);
		return result;
	}
	
	@RequestMapping(value = "/enterOffer", method = RequestMethod.POST)
	@ApiOperation(value = "录入offer", notes = "参数是：offer信息")
	public RestResponse enterOffer(MultipartFile reportFile, String saveFlag, ErpOfferVo offerVo,@RequestHeader String token) {
		RestResponse result = erpOfferService.enterOffer(reportFile,saveFlag,offerVo,token);
		return result;
	}
	
	@RequestMapping(value = "/enterOfferSave", method = RequestMethod.POST)
	@ApiOperation(value = "录入offer 保存", notes = "参数是：offer信息")
	public RestResponse enterOfferSave(MultipartFile reportFile,String saveFlag, ErpOfferVo offerVo,@RequestHeader String token) {
		RestResponse result = erpOfferService.enterOffer(reportFile,saveFlag, offerVo,token);
		return result;
	}
	
	@RequestMapping(value = "/updateOffer", method = RequestMethod.POST)
	@ApiOperation(value = "更新offer", notes = "参数是：offer信息")
	public RestResponse updateOffer(@RequestBody ErpOffer offer,@RequestHeader String token) {
		RestResponse result = erpOfferService.updateOffer(offer,token);
		return result;
	}
	
	@RequestMapping(value = "/talkSalaryChange", method = RequestMethod.POST)
	@ApiOperation(value = "offer谈薪数据修改", notes = "参数是：offer信息")
	public RestResponse talkSalaryChange(@RequestBody ErpOffer offer,@RequestHeader String token) {
		RestResponse result = erpOfferService.talkSalaryChange(offer,token);
		return result;
	}
	
	@RequestMapping(value = "/sendOffer", method = RequestMethod.POST)
	@ApiOperation(value = "发送offer", notes = "参数是：offer信息")
	public RestResponse sendOffer(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpOfferService.sendOffer(token,params);
		return result;
	}
	
	@RequestMapping(value="/approveOfferQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "待审批的offer", notes = "无参数")
	public RestResponse approveOfferQueryList(@RequestHeader String token) {
		RestResponse result = erpOfferService.approveOfferQueryList(token);
		return result;
	}
	
	@RequestMapping(value="/approveOfferQueryListForLook",method = RequestMethod.GET)
	@ApiOperation(value = "待审批的offer(仅供查看)", notes = "无参数")
	public RestResponse approveOfferQueryListForLook(@RequestHeader String token) {
		RestResponse result = erpOfferService.approveOfferQueryListForLook(token);
		return result;
	}

	@RequestMapping(value="/offerApprove",method = RequestMethod.POST)
	@ApiOperation(value = "offer审批通过", notes = "无参数")
	public RestResponse offerApprove(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpOfferService.offerApprove(token,params);
		return result;
	}

	@RequestMapping(value="/offerDeny",method = RequestMethod.POST)
	@ApiOperation(value = "offer审批不通过", notes = "无参数")
	public RestResponse offerDeny(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpOfferService.offerDeny(token,params);
		return result;
	}


	@RequestMapping(value="findAllOffer", method = RequestMethod.GET)
	@ApiOperation(value = "所有Offer", notes = "所有Offer")
	public RestResponse findAllOffer(@RequestParam String token){
		Map<String, Object> map = erpOfferService.findAllOffer(token);
		return RestUtils.returnSuccess(map);
	}
	@RequestMapping(value="/getEmailUserNameAndPassword", method = RequestMethod.GET)
	@ApiOperation(value = "获取邮件用户名密码", notes = "邮件用户名密码")
	public RestResponse getEmailUserNameAndPassword(){
		Map<String, Object> map = erpOfferService.getEmailUserNameAndPassword();
		return RestUtils.returnSuccess(map);
	}

	
	//@Scheduled(cron = "0 10 * * * ?")
	//@Scheduled(cron = "0 30 0 * * ?")
	public void automaticInvalidOfferScheduler() {
		erpOfferService.automaticInvalidOfferScheduler();
	}
	
	//@Scheduled(cron = "0 49 * * * ?")
	@Scheduled(cron = "0 0 1 * * ?")
	public void automaticDeleteTempFilesScheduler() {
		erpOfferService.automaticDeleteTempFilesScheduler();
	}
	
}
