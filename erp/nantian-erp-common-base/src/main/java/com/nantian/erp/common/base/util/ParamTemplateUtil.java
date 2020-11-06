package com.nantian.erp.common.base.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/*
 * add by 刘文凯
 * add time:20180612
 * 用途：实现参数模板的导入和导出逻辑，兼容思科控制器和其他设备
 */
public class ParamTemplateUtil {
	
	public static String paramTemplateExport (String paramInfo, Map<String, Object> deviceInfo) {
		JSONArray paramJson = (JSONArray) JSONObject.toJSON(paramInfo);
		@SuppressWarnings("resource")
		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet sheet = workBook.createSheet("原子服务参数表");
		try {
			if ("CICSO".equalsIgnoreCase(deviceInfo.get("manufacturer").toString())) {
				/*
				 * 处理思科控制器的表格导出,生成表头
				 */
				HSSFRow rowHead = sheet.createRow(0);
				rowHead.createCell(0).setCellValue("原子操作名称");
				rowHead.createCell(1).setCellValue("xml标签");
				rowHead.createCell(2).setCellValue("标签属性");
				rowHead.createCell(3).setCellValue("属性值");
				
				for (int i = 0; i < paramJson.size(); i++) {
					Map<String, Object> opMap = paramJson.getJSONObject(i);
					String opName = opMap.get("opName").toString();
					Set<String> tagSet = opMap.keySet();
					tagSet.remove("opName");
					Integer opTotalRowNum = 0;
					for (String tag : tagSet) {
						String[] properties = (String[]) opMap.get(tag);
						for (int j = 0; j < properties.length; j++) {
							Integer lastRowNum = sheet.getLastRowNum();
							Integer nextRowNum = lastRowNum + 1;
							HSSFRow row = sheet.createRow(nextRowNum);
							row.createCell(0).setCellValue(opName);
							row.createCell(1).setCellValue(tag);
							row.createCell(2).setCellValue(properties[j]);
							row.createCell(3);
						}
						
						opTotalRowNum = opTotalRowNum + properties.length;
						/*
						 * 对tag所在的单元格合并
						 */
						sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum() - properties.length + 1, sheet.getLastRowNum(), 1, 1));
					}
					
					/*
					 * 合并opName所在的列
					 */
					sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum() - opTotalRowNum + 1, sheet.getLastRowNum(), 0, 0));
				}
			} else {
				HSSFRow rowHead = sheet.createRow(0);
				rowHead.createCell(0).setCellValue("原子操作名称");
				rowHead.createCell(1).setCellValue("参数名");
				rowHead.createCell(2).setCellValue("参数值");
				for (int i = 0; i < paramJson.size(); i++) {
					Map<String, Object> opMap = paramJson.getJSONObject(i);
					String opName = opMap.get("opName").toString();
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> properties = (List<Map<String, Object>>) opMap.get("properties");
					for (Map<String, Object> map : properties) {
						Integer lastRowNum = sheet.getLastRowNum();
						Integer nextRowNum = lastRowNum + 1;
						
						HSSFRow row = sheet.createRow(nextRowNum);
						row.createCell(0).setCellValue(opName);
						row.createCell(1).setCellValue(map.get("key").toString());
						row.createCell(2);
					}
					
					/*
					 * 对第一列：原子操作名进行单元格合并
					 */
					sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum() - properties.size() + 1, sheet.getLastRowNum(), 0, 0));
				}
			}
			HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
			response.addHeader("Content-Disposition","attachment;filename=ZTPParamModel.xls");
			ServletOutputStream os;
			try {
				os = response.getOutputStream();
				workBook.write(os);
				os.flush();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "导出模板表格失败";
			}
			return "导出模板表格成功";
		} catch (Exception e) {
			return "导出模板表格失败";
		}
	}
	
}
