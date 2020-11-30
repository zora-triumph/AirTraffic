package app;

import java.io.File;
import java.util.Arrays;
import java.util.TreeSet;

import decoder.AppConfig;
import decoder.CDCDecoder;
import decoder.Record;
import ftp.FtpUtil;
import kfk.RECProducer;

public class ThreadDecode implements Runnable {

	@Override
	public void run() {
		String dataSource = App.getApp().getAppConfig().getDataSource();
		if(dataSource.equals("local"))
			getLocalData();
		if(dataSource.equals("ftp"))
			getFTPData();

			
	}
	private void getLocalData()
	{
		File RECDir = new File(App.getApp().getAppConfig().getRECPath()); 
	    File[] level_I = RECDir.listFiles();
	    if(level_I == null) {
	    	return;
	    }
	    System.out.println("Extracting REC files.");
	    //解压文件
	    for(File f_I:level_I) {
	    	if(f_I.isDirectory()) {
	    		File[] level_II = f_I.listFiles();
	    		for(File f_II:level_II) {
	    			String f_II_name = f_II.getName();
	    			//TODO 目前只解压了cdc数据
	    			//String recT[] = {"central", "fifo", "local", "system", "tfms"};
	    			String recT[] = {"central"};
	    			if(f_II.isDirectory() && Arrays.asList(recT).contains(f_II_name)) {
	    				File[] level_III = f_II.listFiles();
	    				for(File f_III:level_III) {	
	    					String fName = f_III.getName();
	    					int len = fName.length();
	    					//TODO 目前只解压FPL
	    					if(fName.substring(len-3,len).equals(".gz")&&fName.substring(0,8).equals("REC_FPL_")) {
	    						try {
	    						System.out.println("Extracting "+f_III.getAbsolutePath()+".");
	    						Process pro = Runtime.getRuntime().exec("gunzip "+f_III.getAbsolutePath()+" > /dev/null");
	    						pro.waitFor();
	    						pro.destroy();
	    						pro = Runtime.getRuntime().exec("rm -f "+f_III.getAbsolutePath()+" > /dev/null");
	    						pro.waitFor();
	    						pro.destroy();
	    						
	    						}catch(Exception e) {
	    							e.printStackTrace();
	    							System.err.println(e.toString());
	    						}
	    					}
	    				}//for
	    			}
	    		}//for
	    	}
	    }//for
		//解析数据
	    for(File f_I:level_I) {
	    	if(f_I.isDirectory()) {
	    		//15分钟一个一级目录，例如”REC_20191020_0000
	    		RECProducer recPro = new RECProducer();
	    		recPro.connect(App.getApp().getAppConfig().getTopicName());
	    		
	    		File[] level_II = f_I.listFiles();
	    		for(File f_II:level_II) {
	    			String f_II_name = f_II.getName();
	    			//TODO 未来会修改
	    			//String recT[] = {"central", "fifo", "local", "system", "tfms"};
	    			String recT[] = {"central"};
	    			if(f_II.isDirectory() && Arrays.asList(recT).contains(f_II_name)) {
						File[] RECs = f_II.listFiles();
						for(File f:RECs) {	
							String fName = f.getName();
							int len = fName.length();
							if(!fName.substring(len-3,len).equals(".gz")) {
								//TODO 现在只解析FPL，只解析快照（csnp）文件
								String file_prefix = fName.substring(0, fName.length()-16);
								String file_suffix = fName.substring(fName.length()-4, fName.length());
								if(!file_prefix.equals("REC_FPL_") || !file_suffix.equals("csnp"))
									continue;
								System.out.println("------ "+fName+" ------");
								//读REC文件
								byte[] RECData = RecUtils.readFileToBytes(f.getPath()); 
								//解析数据
								switch(f_II_name) {
								case "central":
									CDCDecoder cd= new CDCDecoder(RECData, fName);
									//TODO write to file
									//ArrayList<Record> recList = new ArrayList<Record>();
									Record rec = cd.extractRecord();
									while(rec!=null){
										//TODO write to file
										//recList.add(rec);
		
										//byte[] recStream = SerializeUtil.serialize(rec);
										//System.out.println("一条序列化的rec长度："+recStream.length);
										recPro.sendMessage(rec.getKey(),rec);
										//解析下一条rec
										rec = cd.extractRecord();
										
										//writeToCsv(recList);
										//break;	//只解析第一条rec记录
									}//while 单个文件内循环
									//writeToCsv(recList);
								}
								//break;//只解析第一个REC文件
							}
						}//for 三级目录
						//break;	//只解析central
	    			}
    			}//for 二级目录
	    		recPro.close();
	    		//模拟ftp15分钟取一次数据
	    		try {
					Thread.sleep(30*1000);	//30秒
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		//break;//只解析第一个REC_2019XXXX_XXXX
    		}
	    }//for 一级目录
	}
	private void getFTPData()
	{
		//TODO log
		//第一次导入REC数据
		FtpUtil ftp = new FtpUtil();	//ftp
		RECProducer recPro = new RECProducer();	//kafka
		
		AppConfig appConf = App.getApp().getAppConfig();
		if(!ftp.connect(appConf.getFTPMode(), appConf.getFTPHost(), Integer.parseInt(appConf.getFTPPort()), appConf.getFTPUser(), appConf.getFTPPassword()))
		{
			System.out.println("FTP connect: failed!");
		}
		System.out.println("FTP connect: successful!");
		TreeSet<String> RECDirSet = ftp.getRECDirSet(0,appConf.getFTPRECPath());
		ftpDecode(recPro, RECDirSet, ftp);
		ftp.close();
		try {
			Thread.sleep(Integer.valueOf(appConf.getFTPTransInterval())*60*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true)
		{
			if(!ftp.connect(appConf.getFTPMode(), appConf.getFTPHost(), Integer.parseInt(appConf.getFTPPort()), appConf.getFTPUser(), appConf.getFTPPassword()))
			{
				System.out.println("FTP connect: failed!");
			}
			System.out.println("FTP connect: successful!");
			RECDirSet = ftp.getRECDirSet(1,appConf.getFTPRECPath());
			ftpDecode(recPro, RECDirSet, ftp);
			ftp.close();
			try {
				Thread.sleep(Integer.valueOf(appConf.getFTPTransInterval())*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void ftpDecode(RECProducer recPro, TreeSet<String> RECDirSet, FtpUtil ftp)
	{
		AppConfig appConf = App.getApp().getAppConfig();
		recPro.connect(App.getApp().getAppConfig().getTopicName());
		for(String recDir: RECDirSet)
		{
			System.out.println(recDir);
			TreeSet<String> fileSet = ftp.downloadFile(recDir, appConf.getOutPath()+"tmp");
			//TODO log
			if(fileSet == null)
				continue;
			//记录最后一次下载的REC目录名
			String lastREC = RecUtils.rmPathSeparator(recDir.substring(recDir.lastIndexOf(File.separatorChar)+1));
			ftp.setLastRECDir(lastREC);
			//解压，删除临时文件，送到kafka
			if(fileSet.size()==0)
				System.out.println("null fileset");
			for(String fileDir:fileSet) {
				System.out.println(fileDir);
				String unzipFDir = fileDir;
				if(fileDir.substring(fileDir.length()-3,fileDir.length()).equals(".gz"))
				{
					//解压
					try {
						System.out.println("Extracting "+fileDir+".");
						Process pro = Runtime.getRuntime().exec("gunzip "+fileDir+" > /dev/null");
						pro.waitFor();
						pro.destroy();
						unzipFDir = fileDir.substring(0, fileDir.length()-3);
				
					}catch(Exception e) {
						e.printStackTrace();
						System.err.println(e.toString());
					}
				}
				//解析并送到kafka
				String fName = unzipFDir.substring(unzipFDir.lastIndexOf(File.separator)+1,unzipFDir.length());
				byte[] RECData = RecUtils.readFileToBytes(unzipFDir); 
				CDCDecoder cd= new CDCDecoder(RECData, fName);
				Record rec = cd.extractRecord();
				while(rec!=null){
					recPro.sendMessage(rec.getKey(),rec);
					rec = cd.extractRecord();
				}
				//删除文件
				try {
					Process pro = Runtime.getRuntime().exec("rm -f "+fileDir+" > /dev/null");
					pro.waitFor();
					pro.destroy();
					pro = Runtime.getRuntime().exec("rm -f "+unzipFDir+" > /dev/null");
					pro.waitFor();
					pro.destroy();
				}catch(Exception e) {
					e.printStackTrace();
					System.err.println(e.toString());
				}
			}
		}
		recPro.close();
	}
}

