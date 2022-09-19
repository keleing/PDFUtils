package com.example.demo;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author 李伟
 * @date 2022-07-22
 * @description <p>DocumentController</p>
 **/
@RestController
@RequestMapping("/tables")
@Validated //校验字段
@CrossOrigin
public class TableController {

    /**
     * 文件上传最大值为 （50MB）
     */
    public static final int DOCUMENT_MAX_SIZE = 50 * 1024 * 1024;

    /**
     * 允许头像上传的文件类型
     */
    public static final List<String> DOUCUMENT_TYPE = new ArrayList<>();
    static {
        DOUCUMENT_TYPE.add("xlsx");
        DOUCUMENT_TYPE.add("xls");
    }




    /**
     * 文档上传
     * @param file 上传文件
     * @return
     */
    @PostMapping("/uploadTable")
    public Result uploadDocument(@RequestParam("source") MultipartFile file){
        Result result=new Result();
        if (file.isEmpty()) {
            result.setResultCode(500);
            result.setMessage("上传文件为空");
            return result;
        }
        if (file.getSize() > DOCUMENT_MAX_SIZE) {
            result.setResultCode(500);
            result.setMessage("上传的文件大小不能超过50M");
            return result;
        }
        //获取文件类型 .doc
        String suffix = "";
        String originFileName = file.getOriginalFilename();
        int beginIndex = originFileName.lastIndexOf(".");
        if (beginIndex > 0) {
            suffix = originFileName.substring(beginIndex+1);
        }
        System.out.println("文件类型" + suffix);
        if (!DOUCUMENT_TYPE.contains(suffix)) {
            result.setResultCode(500);
            result.setMessage("文件类型需要是：xlsx，xls");
            return result;
        }
        //解析
        try {
            InputStream inputStream = file.getInputStream();
            Workbook wb;
            if ( "xls".equals(suffix)){
                //文件流对象
                wb = new HSSFWorkbook(inputStream);
            }else {
                wb = new XSSFWorkbook(inputStream);
            }
            //开始解析
            Sheet sheet = wb.getSheet("Sheet1");     //读取Sheet1
            if (sheet==null){
                result.setResultCode(404);
                result.setMessage("没有找个对应的sheet表");
                return result;
            }
            //读取表头,第一行
            Row tableHead =sheet.getRow(sheet.getFirstRowNum());
            //读取2到最后一行
            int firstRowIndex = sheet.getFirstRowNum()+1;
            int lastRowIndex = sheet.getLastRowNum();
            System.out.println("firstRowIndex: "+firstRowIndex);
            System.out.println("lastRowIndex: "+lastRowIndex);

            for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
                System.out.println("rIndex: " + rIndex);
                Row row = sheet.getRow(rIndex);
                if (row != null) {
                    int firstCellIndex = row.getFirstCellNum();
                    int lastCellIndex = row.getLastCellNum();
                    for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
                        Cell cell = row.getCell(cIndex);
                        if (cell != null) {
                            System.out.println(cell.toString());
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }




}