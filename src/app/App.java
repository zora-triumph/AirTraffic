package app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import decoder.AppConfig;
import decoder.DataConfig;
import decoder.RECConfig;
import decoder.XMLDecoder;
import hbs.HBaseConnector;
import kfk.RECConsumer;
import kfk.RECProducer;


public class App {
	private static final App DecoderApp = new App();
	
	public static App getApp() {
		return DecoderApp;
	}
	private App() {
		//TODO 测试时修改
		String AppConfPath = "./app_config.xml";
		
		System.out.println("Read app_config.xml from "+AppConfPath+".");
		byte[] xmlBytes = RecUtils.readFileToBytes(AppConfPath); 
//		System.out.println("Reading app_config.xml from /home/swj/air_traffic/app_config.xml.");
//		byte[] xmlBytes = readFileToBytes("/home/swj/air_traffic/app_config.xml"); 

		if(xmlBytes != null) {
			XMLDecoder dec = new XMLDecoder();
			appConf = dec.getAppConfig(xmlBytes);
		}
		if(xmlBytes == null || appConf == null) {
			System.err.println("Warning: An error has occurred when reading app_config.xml!");
			System.exit(1);
		}
	}
	
	private ByteOrder RECByteOrder = ByteOrder.LITTLE_ENDIAN;	//ADA程序是小端字节序
	private Charset encoding = StandardCharsets.UTF_8;
	//app
	private AppConfig appConf;
	private RECConfig recConf;
	private HashMap<String, DataConfig> dataConf = new HashMap<String, DataConfig>(); 

	public void init() {
		System.out.println("Reading rec_config.xml from "+appConf.getRECConfPath()+"REC_config.xml.");
		XMLDecoder dec = new XMLDecoder();
		//读rec_config.xml文件
		byte[] xmlBytes = RecUtils.readFileToBytes(appConf.getRECConfPath()+"REC_config.xml"); 
		if(xmlBytes == null) {
			System.err.println("Error: An Error has occurred when reading REC_config.xml, program exit!");
			System.exit(1);
		}
		if(xmlBytes != null) {
			recConf = dec.getRECConfig(xmlBytes);
			if(recConf == null) {
				System.err.println("Error: An Error has occurred when parsing REC_config.xml, program exit!");
				System.exit(1);
			}
		}

		//读入所有的数据xml文件
		System.out.println("Reading data configuration files from "+appConf.getDataConfPath()+".");
		File[] dcfileList = new File(appConf.getDataConfPath()).listFiles();
		for(File f:dcfileList) {
			String fn = f.getName();
			xmlBytes = RecUtils.readFileToBytes(appConf.getDataConfPath()+fn);
			if(xmlBytes == null) {
				System.err.println("Error: An Error has occurred when reading "+fn+", program exit!");
				System.exit(1);
			}
			if(xmlBytes != null) {
				DataConfig dc = dec.getDataConfig(xmlBytes, fn);
				if(dc == null) {
					System.err.println("Error: An Error has occurred when parsing "+fn+", program exit!");
					System.exit(1);
				}
				dataConf.put(dc.hashmapKey, dc);
			}
		}
		App.getApp().print();
		//创建out，以及out/tmp目录
		RecUtils.createDir(appConf.getOutPath());
		RecUtils.createDir(appConf.getOutPath()+"tmp");
		
		new Thread(new ThreadStorage()).start(); //启动消费者
		new Thread(new ThreadDecode()).start();	//启动生产者
	}
	
	public AppConfig getAppConfig() {
		return appConf;
	}
	public RECConfig getRECConfig() {
		return recConf;
	}
	public HashMap<String, DataConfig> getDataConf() {
		return dataConf;
	}
	public Charset getEncoding() {
		return encoding;
	}
	public ByteOrder getRECByteOrder() {
		return RECByteOrder;
	}

	public void print() {
		System.out.println("------------------------AppConfig------------------------");
		App.getApp().getAppConfig().print();
		System.out.println("------------------------RECConfig------------------------");
		RECConfig rc = App.getApp().getRECConfig();
		rc.print();
		System.out.println("------------------------xmlFormat------------------------");
		//打印DataConfig对应的xml文件的结构
		HashSet<String> level1 = new HashSet<String>();
		HashSet<String> level2 = new HashSet<String>();
		HashSet<String> memAttr = new HashSet<String>();
		HashSet<String> simType = new HashSet<String>();
		
		File[] dcfileList = new File(appConf.getDataConfPath()).listFiles();
		for(File f:dcfileList) {
			System.out.println(f.getName());
			XMLDecoder dec = new XMLDecoder();
			byte[] xmlBytes = RecUtils.readFileToBytes(appConf.getDataConfPath()+f.getName());
			try{
				SAXReader reader = new SAXReader();
				Document doc = reader.read(new ByteArrayInputStream(xmlBytes));
				Element root = doc.getRootElement();
				Iterator it = root.elementIterator();
				while(it.hasNext()) {
					Element e = (Element)it.next();
					level1.add(e.getName());
					Iterator it2 = e.elementIterator();
					while(it2.hasNext()) {
						Element e2 = (Element)it2.next();
						level2.add(e2.getName());
						if(e.getName().equals("structure")) {
							Iterator itattr = e2.attributeIterator();
							while(itattr.hasNext()) {
								Attribute attr = (Attribute)itattr.next();
								memAttr.add(attr.getName());
							}
							if(e2.getName().equals("simple")) {
								String t = e2.attributeValue("type");
								simType.add(t);
							}
						}
					}
				}
			} catch(DocumentException e) {
				e.printStackTrace();
				System.err.println(e.toString());
			}
		}
		System.out.println("\nlevel1:");
		for(String s:level1)
			System.out.print(s+", ");
		System.out.println("\nlevel2:");
		for(String s:level2)
			System.out.print(s+", ");
		System.out.println("\nmemAttr:");
		for(String s:memAttr)
			System.out.print(s+", ");
		System.out.println("\nsimType:");
		for(String s:simType)
			System.out.print(s+", ");
		System.out.println("\n");
		//打印dataConf
//		System.out.println("------------------------DataConfig------------------------");
//		Iterator dcIt = dataConf.entrySet().iterator();
//		while(dcIt.hasNext()) {
//			Map.Entry entry = (Map.Entry)dcIt.next();
//			DataConfig dc = (DataConfig)entry.getValue();
//			if(dc.hashmapKey.equals("flight_plan.xml"))
//				dc.print();
//		}
	}
	public static void main(String[] args) {
		//TODO test
		//重定向sysout
//		try {      
//            PrintStream print=new PrintStream("./log.txt"); //日志临时打印目录
//            System.setOut(print);  
//        } catch (FileNotFoundException e) {  
//            e.printStackTrace();  
//        }
		App.getApp().init();
		// 程序初始化
//		App.getApp().print();
	}
}
