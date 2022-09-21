package com.example.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@SpringBootTest
class Demo1ApplicationTests {

    DocumentUtils documentUtils =new DocumentUtils();
    @Test
    void renFile(){
        String sour ="C:\\Users\\liwei\\Desktop\\6、卫健\\";
        //String sour ="C:\\Users\\liwei\\Desktop\\文件重命名\\";
        List<String> paths = documentUtils.getAllFile(sour,false);
        Map<String ,String> nameMap= new HashMap<>();
        String renPath = "C:\\Users\\liwei\\Desktop\\文件重命名\\";
        File dir = new File(renPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (String s: paths){
            //.pdf
            String suffix = "";
            int beginIndex = s.lastIndexOf(".");
            if (beginIndex > 0) {
                suffix = s.substring(beginIndex);
            }
            //将文件重新命名
            String fileName = UUID.randomUUID()  + suffix;
            nameMap.put(s,fileName);
        }

        //输出对应关系 并复制文件
        for (String key : nameMap.keySet()) {
            System.out.println(key + "$" + nameMap.get(key));
            File dst = new File(renPath,nameMap.get(key));
            File sourfile =new File(key);
            try {
                Files.copy(sourfile.toPath(),dst.toPath(),StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /*将目录下的全部文件转为pdf*/
    @Test
    void changeToPdf(){
        String sourcePath = "C:\\Users\\liwei\\Desktop\\文件重命名\\";
        String dstPath  ="C:\\Users\\liwei\\Desktop\\pdf转化\\";
        File dir = new File(sourcePath);
        if (!dir.exists() || !dir.isDirectory()) {// 判断是否存在目录
            System.out.println("目录不存在");
        }
        File dir2 = new File(dstPath);
        if (!dir2.exists()) {// 判断是否存在目录
            dir2.mkdir();
        }
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
            documentUtils.beginToPdf(sourcePath+files[i],dstPath);
            System.out.println("转化第"+(i+1)+"个文件");
            try {
                Thread.sleep(1000*15);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*补充剩余*/

    @Test
    void changeToPdf2(){
        List<String> documentEnd = new ArrayList<>();
        documentEnd.add("679a9c28-a5a5-46ec-8110-dfbdca47e1ee.pdf");

    }

    /*将目录下的pdf全部压缩*/
    @Test
    void compressPdf() {
        //String source = "C:\\Users\\liwei\\Desktop\\pdf转化\\";
        String source = "C:\\Users\\liwei\\Desktop\\pdf转化\\";
        String dirPath = "C:\\Users\\liwei\\Desktop\\pdf压缩\\";
        File dir = new File(source);
        if (!dir.exists() || !dir.isDirectory()) {// 判断是否存在目录
            System.out.println("目录不存在");
        }
        File dir2 = new File(dirPath);
        if (!dir2.exists()) {// 判断是否存在目录
            dir2.mkdir();
        }
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
            documentUtils.compressPdf(source+files[i],dirPath+"compress_"+files[i]);
        }
    }


    @Test
    void insertTable(){
        String source = "C:\\Users\\liwei\\Desktop\\全部解压文件\\";
        String dirPath = "C:\\Users\\liwei\\Desktop\\image\\";
        File dir = new File(source);
        if (!dir.exists() || !dir.isDirectory()) {// 判断是否存在目录
            System.out.println("目录不存在");
        }
        File dir2 = new File(dirPath);
        if (!dir2.exists()) {// 判断是否存在目录
            dir2.mkdir();
        }
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
            try {
                File file =new File(source+files[i]);
                PDDocument pdDocument= PDDocument.load(file);
                PDFRenderer renderer = new PDFRenderer(pdDocument);
                int pageCount = pdDocument.getNumberOfPages();
                BufferedImage image = renderer.renderImageWithDPI(0, 50);
//          BufferedImage image = renderer.renderImage(i, 2.5f);
                String imageName = files[i].substring(files[i].lastIndexOf("_")+1,files[i].lastIndexOf("."))+".png";
                System.out.println(imageName);
                File des =new File(dirPath+imageName);
                ImageIO.write(image, "PNG",des );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
