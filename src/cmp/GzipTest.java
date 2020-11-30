package cmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
 
public class GzipTest {
 
	public static byte[] gzip(byte[] data) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data);
		gzip.finish();
		gzip.close();
		byte[] ret = bos.toByteArray();
		bos.close();
		return ret;
	}
 
	public static byte[] ungzip(byte[] data) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		GZIPInputStream gzip = new GZIPInputStream(bis);
		byte[] buf = new byte[1024];
		int num = -1;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((num = gzip.read(buf, 0, buf.length)) != -1) {
			bos.write(buf, 0, num);
		}
		gzip.close();
		bis.close();
		byte[] ret = bos.toByteArray();
		bos.flush();
		bos.close();
		return ret;
	}

public static void main(String[] args) throws Exception {
	 
	// 读取文件
	String readPath = "/Users/zhouna/Downloads/code/zstd_test/log/train2w.txt";
	File file = new File(readPath);
	FileInputStream in = new FileInputStream(file);
	byte[] data = new byte[in.available()];
	in.read(data);
	in.close();

	System.out.println("文件原始大小:" + data.length / 1024 / 1024  + " MB");

	// 测试压缩
	long compressBeginTime = System.currentTimeMillis();
	byte[] ret1 = GzipTest.gzip(data);
	long compressEndTime = System.currentTimeMillis();
    long SpendTime = compressEndTime - compressBeginTime;
    float rate_num = (((float )(data.length / 1024) / 1024) * 1000) / SpendTime;
    DecimalFormat df1 = new DecimalFormat(".00");
    String rate = df1.format(rate_num);
    System.out.println("compress rate: "+ (rate) + " MB/s");
    
    
	System.out.println("压缩之后大小:" + ret1.length / 1024 /1024 + " MB");
    float ratio_num = (float)(ret1.length)/(data.length);
    DecimalFormat df2 = new DecimalFormat("0.00%");
    String ratio = df2.format(ratio_num);
//    System.out.println("compressed data length: "+ compressed.length+" b");
    System.out.println("compress ratio: "+ (ratio));

	// 还原文件
//	byte[] ret2 = GzipTest.ungzip(ret1);
//	System.out.println("还原之后大小:" + ret2.length);

	// 写出文件
//	String writePath = System.getProperty("user.dir") + File.separatorChar
//			+ "receive" + File.separatorChar + "005.jpg";
//	FileOutputStream fos = new FileOutputStream(writePath);
//	fos.write(ret2);
//	fos.close();

}

}
