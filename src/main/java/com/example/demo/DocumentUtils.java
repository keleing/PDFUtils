package com.example.demo;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;
//import gz.gdtel.com.ParterOperationSystem.pojo.Document;
//import gz.gdtel.com.ParterOperationSystem.pojo.DocumentDir;
//import gz.gdtel.com.ParterOperationSystem.pojo.DocumentShow;
//import gz.gdtel.com.ParterOperationSystem.pojo.DocumentVo;
//import gz.gdtel.com.ParterOperationSystem.utils.documentUtil.CustomRenderListener;
//import gz.gdtel.com.ParterOperationSystem.utils.documentUtil.MatchItem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件处理工具类
 */
public class DocumentUtils {
    //文件压缩比例
    public static float FACTOR = 0.5f;
    public DocumentUtils() {
    }

    /**
     * 文档转PDF
     * @param source 源文件路径
     * @param target 目标文件目录
     * @return
     */
    public int beginToPdf(String source, String target){
        String command;
        int exitStatus=0;
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            command = "cmd /c start soffice --headless --invisible --convert-to pdf:writer_pdf_Export " + source + " --outdir " + target;
        } else {
            command = "/opt/libreoffice7.3/program/soffice --headless --invisible --convert-to pdf:writer_pdf_Export "+ source + " --outdir " + target;
        }
        //command = "/opt/libreoffice7.3/program/soffice --headless --invisible --convert-to pdf /var/www/html/dist/index.html --outdir /var/lib/tomcat9/webapps/home";
        System.out.println(command);
        exitStatus = executeOSCommand(command);
        System.out.println("-----"+exitStatus);
        return exitStatus;
    }
    /**
     * 调用操作系统的控制台，执行 command 指令
     * 执行该方法时，并没有等到指令执行完毕才返回，而是执行之后立即返回，返回结果为 0，只能说明正确的调用了操作系统的控制台指令，但执行结果如何，是否有异常，在这里是不能体现的，所以，更好的姿势是用同步转换功能。
     */
    private static int executeOSCommand(String command) {
        Process process;
        //保存进程标准输入流信息
        List<String> stdotList = new ArrayList<>();
        //保存进程标准错误流信息
        List<String> errorList = new ArrayList<>();
        try {
            process = Runtime.getRuntime().exec(command); // 转换需要时间，比如一个 3M 左右的文档大概需要 8 秒左右，但实际测试时，并不会等转换结束才执行下一行代码，而是把执行指令发送出去后就立即执行下一行代码了。
            ThreadUtil stdotThread = new ThreadUtil(stdotList,process.getInputStream());
            ThreadUtil errorThread = new ThreadUtil(errorList,process.getErrorStream());
            stdotThread.start();
            errorThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int exitStatus = 0;
        try {
            exitStatus = process.waitFor();
            System.out.println("进程结束："+exitStatus);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (exitStatus == 0) {
            System.out.println("退出值："+process.exitValue());
            exitStatus = process.exitValue();
        }
        // 销毁子进程
        process.destroy();
        return exitStatus;
    }

    /**
     * @param inputFile     添加文件路劲
     * @param outputFile    输出地址
     * @param waterMarkName 水影名字
     * @param opacity       透明度
     * @param fontsize      字体大小
     * @param angle         倾斜度
     * @param heightdensity 每页竖向水印，越大间隔越宽
     * @param widthdensity  每页横向水印，越大间隔越宽
     * @param cover         文件是否覆盖
     * @return
     */
    public static boolean addWaterMark(String inputFile, String outputFile, String waterMarkName, float opacity, int fontsize, int angle, int heightdensity, int widthdensity, boolean cover) {
        if (!cover) {
            File file = new File(outputFile);
            System.out.println(file.getAbsolutePath());
            if (file.exists()) {
                return true;
            }
        }
        File file = new File(inputFile);
        if (!file.exists()) {
            return false;
        }

        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            //int interval = -5;
            reader = new PdfReader(inputFile);
            //获取文件密码
            Field f = PdfReader.class.getDeclaredField("ownerPasswordUsed");
            f.setAccessible(true);
            f.set(reader, Boolean.TRUE);
            stamper = new PdfStamper(reader, new FileOutputStream(outputFile));
            BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
            Rectangle pageRect = null;
            PdfGState gs = new PdfGState();
            //这里是透明度设置
            gs.setFillOpacity(opacity);
            //这里是条纹不透明度
            gs.setStrokeOpacity(0.2f);
            int total = reader.getNumberOfPages() + 1;
            System.out.println("Pdf页数：" + reader.getNumberOfPages());
            JLabel label = new JLabel();
            FontMetrics metrics;
            int textH = 0;
            int textW = 0;
            label.setText(waterMarkName);
            metrics = label.getFontMetrics(label.getFont());
            //字符串的高,   只和字体有关
            textH = metrics.getHeight();
            //字符串的宽
            textW = metrics.stringWidth(label.getText());
            PdfContentByte under;
            //这个循环是确保每一张PDF都加上水印
            for (int i = 1; i < total; i++) {
                pageRect = reader.getPageSizeWithRotation(i);
                under = stamper.getOverContent(i);  //在内容上方添加水印
                //under = stamper.getUnderContent(i);  //在内容下方添加水印
                under.saveState();
                under.setGState(gs);
                under.beginText();
                //under.setColorFill(BaseColor.PINK);  //添加文字颜色  不能动态改变 放弃使用
                under.setFontAndSize(base, fontsize); //这里是水印字体大小
                for (int height = textH; height < pageRect.getHeight() * 2; height = height + textH * heightdensity) {
                    for (int width = textW; width < pageRect.getWidth() * 1.5 + textW; width = width + textW * widthdensity) {
                        // rotation:倾斜角度
                        under.showTextAligned(Element.ALIGN_LEFT, waterMarkName, width - textW, height - textH, angle);
                    }
                }
                //添加水印文字
                under.endText();
            }
            //判断是否有生成水印文件
            File file1 =new File(outputFile);
            System.out.println("添加水印成功！文件是否存在："+file1.exists());
            return true;
        } catch (IOException e) {
            System.out.println("添加水印失败！错误信息为: " + e);
            e.printStackTrace();
            return false;
        } catch (DocumentException e) {
            System.out.println("添加水印失败！错误信息为: " + e);
            e.printStackTrace();
            return false;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            //关闭流
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 定位关键字所在的页码
     * @param pdfReader pdf处理器
     * @param keyWords  关键字
     * @return 返回关键字匹配类
     * @throws IOException
     */
//    public MatchItem getKeyWords(PdfReader pdfReader, String keyWords) throws IOException {
//        //Map<String,String> map = new HashMap<>();
//        //String context;
//        int page = 0;
//        if (keyWords==null){
//            keyWords="";
//        }
//        try {
//            int pageNum = pdfReader.getNumberOfPages();
//            String allText = "";
//            String last10Str = "";
//            String returnText ="";
//            for (page = 1; page <= pageNum; page++) {
//                //提取每页内容，并加入上一页前10个字符，避免关键字跨页
//                allText = last10Str + PdfTextExtractor.getTextFromPage(pdfReader, page).replaceAll("[\\s|\\u3000]+", "");
//                if (allText.length() > 10) {
//                    last10Str = allText.substring(allText.length() - 10);
//                }
//                System.out.println("--------测试查询文本："+keyWords);
//                int index_str = allText.indexOf(keyWords);
//                if (index_str != -1) {
//                    System.out.println("found in page " + page + " idx: " + index_str);
//                    returnText = allText.substring(index_str,Math.min(allText.length(),index_str +10));
//                    MatchItem matchItem =new MatchItem(returnText,page,0,0);
//                    //MatchItem matchItem =getKeyWordLocation(pdfReader,keyWords,page);
//                    System.out.println(matchItem);
//                    return matchItem;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 返回关键字所在页面的X\Y坐标
     * @param pdfReader pdf处理器
     * @param keyWords  关键字
     * @param pageNum 关键字所在页码
     * @return  返回X\Y坐标匹配类
     * @throws IOException
     */
//    public MatchItem getKeyWordLocation(PdfReader pdfReader, String keyWords, int pageNum) throws IOException {
//        int page = pageNum;
//        if (keyWords==null){
//            keyWords="";
//        }
//        try {
//            //int pageNum = pdfReader.getNumberOfPages();
//            System.out.println("页面高度:" + pdfReader.getPageSize(pageNum).getHeight());
//            System.out.println("页面宽度:" + pdfReader.getPageSize(pageNum).getWidth());
//            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
//            CustomRenderListener renderListener = new CustomRenderListener();
//            renderListener.setKeyWord(keyWords);
//            StringBuilder allText = null;
//            //for (page = 1; page <= pageNum; page++) {
//            renderListener.setPage(page);
//            pdfReaderContentParser.processContent(page, renderListener);
//            List<MatchItem> matchItems = renderListener.getMatchItems();
//            if (matchItems != null && matchItems.size() > 0) {
//                //完全匹配
//                return matchItems.get(0);
//            }
//            List<MatchItem> allItems = renderListener.getAllItems();
//            allText = new StringBuilder();
//            for (MatchItem item : allItems) {
//                allText.append(item.getContent());
//                //关键字存在连续多个块中
//                if (allText.indexOf(keyWords) != -1) {
//                    return item;
//                }
//            }
//            //}
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 凸显关键字
     * @param pdfReader
     * @param page      页码
     * @param x         关键字 X坐标
     * @param y         关键字 Y坐标
     * @param w         底色宽度
     * @param h         底色高度
     * @param path      输出文件路径
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    public boolean markKeyWord(PdfReader pdfReader, int page, float x, float y, float w, float h, String path) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(pdfReader, new FileOutputStream(path));
        PdfContentByte canvas = stamper.getUnderContent(page);  //不可遮挡文字
//        PdfContentByte canvas = stamper.getOverContent(1);  //可以遮挡文字
        canvas.saveState();
        canvas.setColorFill(BaseColor.YELLOW);  //黄色遮挡层
//        canvas.setColorFill(BaseColor.WHITE);  //白色遮挡层
        canvas.rectangle(x, y, w, h);
        canvas.fill();
        canvas.restoreState();
        stamper.close();
        pdfReader.close();
        return true;
    }


    /**
     * PDF文件压缩
     * @param src 源文件路径，包括文件名
     * @param dest 目标文件路径，包括文件名
     */
    public void compressPdf(String src, String dest) {
        //PdfName key = new PdfName("ITXT_SpecialId");
        //PdfName value = new PdfName("123456789");
        //System.out.println("获取的文件路径:"+src);
        try {
            PdfReader reader = new PdfReader(src);
            System.out.println("文件读取成功"+src);
            //获取文件密码
            Field f = PdfReader.class.getDeclaredField("ownerPasswordUsed");
            f.setAccessible(true);
            f.set(reader, Boolean.TRUE);
            // 获取外部参照对象数量
            int n = reader.getXrefSize();
            PdfObject object;
            PRStream stream;
            // Look for image and manipulate image stream
            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                //PdfObject pdfObject = reader.getPdfObject(i);
                if (object == null || !object.isStream())
                    continue;
                stream = (PRStream) object;
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
                System.out.println(stream.type());
                if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    PdfImageObject image = new PdfImageObject(stream);
                    BufferedImage bi = image.getBufferedImage();
                    if (bi == null) continue;
                    int width = (int) (bi.getWidth() * FACTOR);
                    int height = (int) (bi.getHeight() * FACTOR);
                    //图片
                    if (width<=0){
                        width=1;
                    }
                    if (height<=0){
                        height=1;
                    }
                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    //坐标变化
                    AffineTransform at = AffineTransform.getScaleInstance(FACTOR, FACTOR);
                    Graphics2D g = img.createGraphics();
                    //按比例变换坐标
                    g.drawRenderedImage(bi, at);
                    ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                    //判断文件流的大小，超过500k的才进行压缩，否则不进行压缩
                    //System.out.println("++++++++++++:"+img.getData().getDataBuffer().getSize());
                    if(img.getData().getDataBuffer().getSize()>512000){
                        System.out.println("正在压缩-------");
                        ImageIO.write(img, "WEBP", imgBytes);
                        stream.clear();
                        stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                        stream.put(PdfName.TYPE, PdfName.XOBJECT);
                        stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                        //stream.put(key, value);
                        stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                        stream.put(PdfName.WIDTH, new PdfNumber(width));
                        stream.put(PdfName.HEIGHT, new PdfNumber(height));
                        stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                        stream.put(PdfName.COLORSPACE, PdfName.DEFAULTRGB);
                        System.out.println("压缩完成-------");
                    }else {
                        ImageIO.write(img, "JPG", imgBytes);
                    }
                }
            }
            // Save altered PDF
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
            stamper.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取路径下的所有文件/文件夹
     * @param directoryPath 需要遍历的文件夹路径
     * @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
     * @return
     */
    public  List<String> getAllFile(String directoryPath,boolean isAddDirectory) {
        List<String> list = new ArrayList<String>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if(isAddDirectory){
                    list.add(file.getAbsolutePath());
                }
                list.addAll(getAllFile(file.getAbsolutePath(),isAddDirectory));
            } else {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }
}


