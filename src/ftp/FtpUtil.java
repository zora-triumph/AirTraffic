package ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import app.RecUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.TreeSet;


/**
 * ftp上传下载工具类
 */
public class FtpUtil {
	
//	private String host;
//	private int port;
//	private String user;
//	private String password;
// TODO 超时处理
	private boolean login = false; 
	private FTPClient ftp;
	private TreeSet<String> RECDirSet;
	private String lastRECDir;
	private String mode; //数据传输模式，passive：客户机开放一个大于1024的端口，服务器使用20端口；反之，positive
	
	public FtpUtil() 
	{
		ftp = new FTPClient();
		RECDirSet = new TreeSet<String>();
	}
	public boolean connect(String ftpMode, String host,int port,String user,String password)
	{
		mode = ftpMode;
		boolean rst = false;
		//设置ftp连接超时时间
		ftp.setConnectTimeout(10*1000);
		try {
			ftp.connect(host, port);
			ftp.login(user, password);
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				rst = false;
				login = false;
			}
			else
			{
				rst = true;
				login = true;
			}
			return rst;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			login = false;
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			login = false;
			return false;
		}
		
	}
	public void close()
	{
		try {
			if(login != false)
				ftp.logout();
			if(ftp.isConnected())
				ftp.disconnect();
			System.out.println("FTP close");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//从服务器获得一批REC目录（REC_20191020_0000），batch = 0：最初的一批，batch = 1，新增的
	public TreeSet<String> getRECDirSet(int batch, String remotePath)
	{
		if(mode.equals("passive"))
			ftp.enterLocalPassiveMode();
		else if(mode.equals("positive"))
			ftp.enterLocalActiveMode();
		RECDirSet.clear();
		if(fetchRECDirSetRecursive(batch, remotePath))
			return RECDirSet;
		else
			return null;
	}
	private boolean fetchRECDirSetRecursive(int batch, String remotePath)
	{
		remotePath = RecUtils.addPathSeparator(remotePath);
		if(!ftp.isConnected() || login == false )
			return false;
		boolean rst = true;
		try {
			ftp.changeWorkingDirectory(remotePath); // 转移到FTP服务器目录
			//递归搜索目录
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ff.isDirectory()) {
					String dirName = ff.getName();
					//REC_20191020_0000
					if(dirName.length()==17 && 
							dirName.substring(0,4).equals("REC_") &&
							RecUtils.isDate(dirName.substring(4), new String[]{"yyyyMMdd_HHmm"}))
					{
						if(batch == 0)
							RECDirSet.add(remotePath+dirName);
						else if(batch == 1 && dirName.compareTo(lastRECDir)>0)
						{
							RECDirSet.add(remotePath+dirName);
						}
					}
					else
					{
						rst = fetchRECDirSetRecursive(batch, remotePath+dirName);
					}
				}
			}
			return rst;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	//下载一个REC目录下的所有文件，返回一个本地文件目录列表
	public TreeSet<String> downloadFile(String remotePath, String localPath)
	{
		if(mode.equals("passive"))
			ftp.enterLocalPassiveMode();
		else if(mode.equals("positive"))
			ftp.enterLocalActiveMode();
		remotePath = RecUtils.addPathSeparator(remotePath);
		localPath = RecUtils.addPathSeparator(localPath);
		if(!ftp.isConnected() || login == false )
			return null;
		TreeSet<String> rst = new TreeSet<String>();
		try {
			//TODO 只下载fpl csnp数据
			ftp.changeWorkingDirectory(remotePath+"central");
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (!ff.isDirectory()) {
					String fName = ff.getName();
					if((fName.substring(fName.length()-8, fName.length()).equals(".csnp.gz")
							|| fName.substring(fName.length()-5, fName.length()).equals(".csnp"))
							&& fName.substring(0,8).equals("REC_FPL_"))
					{
						File localFile = new File(localPath + fName);
						OutputStream is = new FileOutputStream(localFile);
						ftp.retrieveFile(ff.getName(), is);
						is.close();
						rst.add(localPath+fName);
					}
				}
			}
			return rst;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	public String getLastRECDir() {
		return lastRECDir;
	}
	public void setLastRECDir(String lastRECDir) {
		this.lastRECDir = lastRECDir;
	}
	/** 
	 * Description: 向FTP服务器上传文件 
	 * @param host FTP服务器hostname 
	 * @param port FTP服务器端口 
	 * @param username FTP登录账号 
	 * @param password FTP登录密码 
	 * @param basePath FTP服务器基础目录
	 * @param filePath FTP服务器文件存放路径。文件的路径为basePath+filePath
	 * @param filename 上传到FTP服务器上的文件名 
	 * @param input 输入流 
	 * @return 成功返回true，否则返回false 
	 */
	private static boolean testUploadFile(String host, int port, String username, String password, String basePath,
			String filePath, String filename, InputStream input) {
		boolean result = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(host, port);// 连接FTP服务器
			// 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
			ftp.login(username, password);// 登录
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return result;
			}
			//切换到上传目录
			if (!ftp.changeWorkingDirectory(basePath+filePath)) {
				//如果目录不存在创建目录
				String[] dirs = filePath.split("/");
				String tempPath = basePath;
				for (String dir : dirs) {
					if (null == dir || "".equals(dir)) continue;
					tempPath += "/" + dir;
					if (!ftp.changeWorkingDirectory(tempPath)) {  //进不去目录，说明该目录不存在
						if (!ftp.makeDirectory(tempPath)) { //创建目录
							//如果创建文件目录失败，则返回
							System.out.println("创建文件目录"+tempPath+"失败");
							return result;
						} else {
							//目录存在，则直接进入该目录
							ftp.changeWorkingDirectory(tempPath);	
						}
					}
				}
			}
			//设置上传文件的类型为二进制类型
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			//上传文件
			if (!ftp.storeFile(filename, input)) {
				return result;
			}
			input.close();
			ftp.logout();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return result;
	}
	
	/** 
	 * Description: 从FTP服务器下载文件 
	 * @param host FTP服务器hostname 
	 * @param port FTP服务器端口 
	 * @param username FTP登录账号 
	 * @param password FTP登录密码 
	 * @param remotePath FTP服务器上的相对路径 
	 * @param fileName 要下载的文件名 
	 * @param localPath 下载后保存到本地的路径 
	 * @return 
	 */  
	private static boolean testDownloadFile(String host, int port, String username, String password, String remotePath,
			String fileName, String localPath) {
		boolean result = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(host, port);
			// 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
			ftp.login(username, password);// 登录
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return result;
			}
			ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ff.getName().equals(fileName)) {
					File localFile = new File(localPath + "/" + ff.getName());
 
					OutputStream is = new FileOutputStream(localFile);
					ftp.retrieveFile(ff.getName(), is);
					is.close();
				}
			}
 
			ftp.logout();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return result;
	}
 
	//ftp上传文件测试main函数
	public static void main(String[] args) {
//		try {  
//	        FileInputStream in=new FileInputStream(new File("D:\\Tomcat 5.5\\pictures\\t0176ee418172932841.jpg"));  
//	        boolean flag = testDownloadFile("192.168.31.176", 21, "anonymous", "888", "/CDC_REC_TMP/REC_20191020_0000/central/","REC_FPL_191020_0000.csnp", "E:\\");  
//	        System.out.println(flag);  
//	    } catch (FileNotFoundException e) {  
//	        e.printStackTrace();  
//	    }
		FtpUtil testFtp = new FtpUtil();
		testFtp.connect("passive","192.168.31.176", 21, "anonymous", "888");
		testFtp.downloadFile("/CDC_REC_TMP", "E:\\testftp");
		testFtp.close();
	}
}