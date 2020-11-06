package com.nantian.erp.common.base.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.exception.BizException;



public class ExcelUtils{
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
	private final static String xls = "xls";  
    private final static String xlsx = "xlsx";  
    
	/**
	 * 解析excel返回解析结果
	 * @param inputFile
	 * @return
	 * [{COL1=第一行第二列, COL0=第一行第一列}, {COL1=第二行第二列, COL0=第二行第一列}, {COL1=第三行第二列, COL0=第三行第一列}, {COL1=第四行第二列, COL0=第四行第一列}]
	 */
	 private static List<Map<String,Object>> getSheetValue(Sheet sheet) {
		 List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
         try {
             Row row = null;
             Cell cell = null;
             // 遍历每行
             Iterator<Row> rowIterator = sheet.iterator();
             while (rowIterator.hasNext()) {
            	 Map<String,Object> m = new HashMap<String,Object>();
                 row = rowIterator.next();
                 // 遍历每行的列元素
                 Iterator<Cell> cellIterator = row.cellIterator();
                 /**
                  * CellType 类型 值
					CELL_TYPE_NUMERIC 数值型 0
					CELL_TYPE_STRING 字符串型 1
					CELL_TYPE_FORMULA 公式型 2
					CELL_TYPE_BLANK 空值 3
					CELL_TYPE_BOOLEAN 布尔型 4
					CELL_TYPE_ERROR 错误 5
                  */
                 while (cellIterator.hasNext()) {
                         cell = cellIterator.next();
                         switch (cell.getCellType()) { //判断列元素的类型
                         case Cell.CELL_TYPE_BOOLEAN:
                             m.put("COL"+cell.getColumnIndex(), cell.getBooleanCellValue());
                             m.put("COL_TYPE"+cell.getColumnIndex(), Cell.CELL_TYPE_BOOLEAN);
                             break;
                         case Cell.CELL_TYPE_NUMERIC:
                        	 m.put("COL"+cell.getColumnIndex(), cell.getNumericCellValue()+" ");
                        	 m.put("COL_TYPE"+cell.getColumnIndex(), Cell.CELL_TYPE_NUMERIC);
                             break;
                         case Cell.CELL_TYPE_STRING:
                        	 m.put("COL"+cell.getColumnIndex(), cell.getStringCellValue());
                        	 m.put("COL_TYPE"+cell.getColumnIndex(), Cell.CELL_TYPE_STRING);
                             break;
                         case Cell.CELL_TYPE_BLANK:
                        	 m.put("COL"+cell.getColumnIndex(), "");
                        	 m.put("COL_TYPE"+cell.getColumnIndex(), Cell.CELL_TYPE_BLANK);
                             break;
                         case Cell.CELL_TYPE_FORMULA:
                        	 m.put("COL"+cell.getColumnIndex(), cell.getCellFormula());
                        	 m.put("COL_TYPE"+cell.getColumnIndex(), Cell.CELL_TYPE_FORMULA);
                        	 break;
                         case Cell.CELL_TYPE_ERROR:
                        	 m.put("COL"+cell.getColumnIndex(), "");
                        	 m.put("COL_TYPE"+cell.getColumnIndex(), Cell.CELL_TYPE_ERROR);
                             break;	 
                         default:
                        	 m.put("COL"+cell.getColumnIndex(), "");
                        	 m.put("COL_TYPE"+cell.getColumnIndex(), "9999");
                        	 logger.error("无此解析类型,CellType()::"+cell.getCellType());
                         }
                 }
                 list.add(m);
             }

         } catch (Exception ioe) {
        	 logger.error("解析XML错误!",ioe); 
        	 throw new BizException("解析XML错误!"+ioe.getMessage());
         }
         return list;
	  }
	 
	 /**
	  * 检查文件有效性
	  * @param file
	  * @throws IOException
	  */
	 private static void checkFile(MultipartFile file) throws IOException{  
        //判断文件是否存在  
        if(null == file){  
            logger.error("文件不存在！");  
            throw new FileNotFoundException("文件不存在！");  
        }  
        //获得文件名  
        String fileName = file.getOriginalFilename();  
        //判断文件是否是excel文件  
        if(!fileName.endsWith(xls) && !fileName.endsWith(xlsx)){  
            logger.error(fileName + "不是excel文件");  
            throw new IOException(fileName + "不是excel文件");  
        }  
    }
    
    /**
     * 根据传入文件后缀判断Workbook实例化方法
     * @param file
     * @return
     * @throws IOException 
     */
    private static Workbook getWorkBook(InputStream is,MultipartFile file) throws IOException {  
        //获得文件名  
        //String fileName = file.getOriginalFilename();  
        //创建Workbook工作薄对象，表示整个excel  
        Workbook workbook = null;  
        //获取excel文件的io流  
        //InputStream is = new FileInputStream(file);  
        //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象  
        if(file.getOriginalFilename().endsWith(xls)){  
            //2003  
            workbook = new HSSFWorkbook(is);  
        }else if(file.getOriginalFilename().endsWith(xlsx)){  
            //2007  
            workbook = new XSSFWorkbook(is);  
        }
        return workbook;  
    }
    
    /**
     * 读取excel
     * @param file
     * @return
     * @throws IOException
     */
	public static List<List<Map<String,Object>>> readExcel(InputStream is,MultipartFile file) throws IOException{
		//检查文件  
        checkFile(file);  
		//获得Workbook工作薄对象  
        Workbook workbook = getWorkBook(is,file);  
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回  
        List<List<Map<String,Object>>> list = new ArrayList<List<Map<String,Object>>>(); 
        if(workbook != null){  
        	if(workbook.getNumberOfSheets()>0){
        		for(int i=0;i<workbook.getNumberOfSheets();i++){
        			List<Map<String,Object>> list1 = null;  
        			Sheet sheet = workbook.getSheetAt(i);  
        			list1 = getSheetValue(sheet);
        			list.add(list1);
        		}
        	}
            workbook.close();  
        } 
        return list;
	}
	
	/**
	 * EXCEL中如果列为数字型，需要转换为字符型的，可以调用此方法（直接强转会报错）
	 * @param type
	 * @param col
	 * @return
	 */
	public static String intToString(Object type,Object col) {
		String str = "";
		if(type != null && type.equals(Cell.CELL_TYPE_NUMERIC)){//判断是否传入数字
			DecimalFormat df=(DecimalFormat)NumberFormat.getInstance(); 
			df.setMaximumFractionDigits(0); 
			str = df.format(NumberUtils.toDouble((String)col));//该交换机所在的U位
		}else{
			str = (String)col;//该交换机所在的U位
		}
		return str;
	}
	
	
	
}
