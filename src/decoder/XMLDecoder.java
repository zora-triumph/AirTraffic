package decoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import app.RecUtils;

import org.dom4j.Element;



public class XMLDecoder {
	
	public AppConfig getAppConfig(byte[] xmlBytes) {
		try{
			AppConfig appConf = new AppConfig();
			Document doc = parseBytes(xmlBytes);
			Element root = doc.getRootElement();
			Iterator it = root.elementIterator();
			while(it.hasNext()) {
				Element e = (Element)it.next();
				String text = e.getName();
				Attribute attr;
				switch(text) {
				case "DataSource":
					attr = e.attribute("value");
					appConf.setDataSource(attr.getText());
					break;
				case "REC_path":
					attr = e.attribute("value");
					appConf.setRECPath(RecUtils.addPathSeparator(attr.getText()));
					break;
				case "FTPHost":
					attr = e.attribute("value");
					appConf.setFTPHost(attr.getText());
					break;
				case "FTPPort":
					attr = e.attribute("value");
					appConf.setFTPPort(attr.getText());
					break;
				case "FTPUser":
					attr = e.attribute("value");
					appConf.setFTPUser(attr.getText());
					break;
				case "FTPPassword":
					attr = e.attribute("value");
					appConf.setFTPPassword(attr.getText());
					break;
				case "FTPMode":
					attr = e.attribute("value");
					appConf.setFTPMode(attr.getText());
					break;
				case "FTPREC_path":
					attr = e.attribute("value");
					appConf.setFTPRECPath(RecUtils.addPathSeparator(attr.getText()));
					break;
				case "FTPTransInterval":
					attr = e.attribute("value");
					appConf.setFTPTransInterval(attr.getText());
					break;
				case "HBaseConfig":
					attr = e.attribute("value");
					appConf.setHBaseConfig(attr.getText());
					break;
				case "TableName":
					attr = e.attribute("value");
					appConf.setTableName(attr.getText());
					break;
				case "KafkaConfig":
					attr = e.attribute("value");
					appConf.setKafkaConfig(attr.getText());
					break;
				case "TopicName":
					attr = e.attribute("value");
					appConf.setTopicName(attr.getText());
					break;
				case "RECConfig_path":
					attr = e.attribute("value");
					appConf.setRECConfPath(RecUtils.addPathSeparator(attr.getText()));
					break;
				case "DataConfig_path":
					attr = e.attribute("value");
					appConf.setDataConfPath(RecUtils.addPathSeparator(attr.getText()));
					break;
				case "out_path":
					attr = e.attribute("value");
					appConf.setOutPath(RecUtils.addPathSeparator(attr.getText()));
					break;
				}
			}
			return appConf;
		} catch(DocumentException e) {
			e.printStackTrace();
			System.err.println(e.toString());
			return null;
		}
	}
	public RECConfig getRECConfig(byte[] xmlBytes){
		try{
			RECConfig recConf = new RECConfig();	//返回值：rec配置
			Document doc = parseBytes(xmlBytes);
			Element root = doc.getRootElement();
			Iterator rootIte = root.elementIterator();
			while(rootIte.hasNext()) {
				Element e1 = (Element)rootIte.next();
				//cdc
				if(e1.getName().equals("cdc")) {
					Iterator it = e1.elementIterator();
					while(it.hasNext()) {
						Element e2 = (Element)it.next();
						switch(e2.getName()) {
						case "csnp":
							recConf.cdcConf.csnp_file_header = Integer.parseInt(e2.attributeValue("file_header"));
							recConf.cdcConf.csnp_record_header = Integer.parseInt(e2.attributeValue("record_header"));			
							break;
						case "cupd":
							recConf.cdcConf.cupd_record_header = Integer.parseInt(e2.attributeValue("record_header"));
							break;
						case "data_type":
							RECConfig.CDCStruct cs = new RECConfig.CDCStruct();
							cs.hashKey = e2.attributeValue("rec_prefix");
							cs.name = e2.attributeValue("name");
							cs.prefix = e2.attributeValue("rec_prefix");
							cs.xml = e2.attributeValue("xml");
							cs.size = Integer.parseInt(e2.attributeValue("size"));
							cs.pad = Integer.parseInt(e2.attributeValue("pad"));
							recConf.cdcConf.data_t.put(cs.hashKey, cs);
							break;
						}
					}
				}
//				rc.recName = e1.attribute("name").getText();
//				rc.recPrefix = e1.attribute("rec_prefix").getText();
//				rc.recXml = e1.attribute("xml").getText();
//				rc.recEx = e1.attribute("rec_ex").getText();
//				Iterator e1It = e1.elementIterator();
//				while(e1It.hasNext()) {
//					Element e2 = (Element)e1It.next();
//					//<structure><structure/>
//					RECConfig.RECStruct rs = new RECConfig.RECStruct();	//rec文件中的一个结构
//					rs.name = e2.attributeValue("name");
//					rs.type = e2.getName();
//					Iterator e2It = e2.elementIterator();
//					while(e2It.hasNext()) {
//						Element e3 = (Element)e2It.next();
//						//<simple><simple/>
//						RECConfig.RECMember rm = new RECConfig.RECMember();
//						rm.name = e3.attributeValue("name");	//结构中的一个成员
//						if(e3.attributeValue("count").equals("multiple"))
//							rm.count = -1;
//						else
//							rm.count = Integer.parseInt(e3.attributeValue("count"));
//						rm.type = e3.getName();
//						if(rm.type.equals("complex")) {
//							rm.structName = e3.attributeValue("type");
//							//从rc中查结构，计算当前成员的size
//							RECConfig.RECStruct tmp = rc.RECStrucSet.get(rm.structName);
//							for(RECConfig.RECMember m : tmp.RECMemSet)
//								rm.size+=m.size;
//						}
//						else {
//							rm.size = Integer.parseInt(e3.attributeValue("size"));
//						}
//						rs.RECMemSet.add(rm);
//					}
//					rs.hashmapKey = rs.name;
//					rc.RECStrucSet.put(rs.hashmapKey,rs);
//				}
//				rc.recHashmapKey = rc.recPrefix+"000000_0000"+rc.recEx;
//				recConf.put(rc.recHashmapKey, rc);
			}
			return recConf;
		} catch(DocumentException e) {
			e.printStackTrace();
			System.err.println(e.toString());
			return null;
		}
	}
	public DataConfig getDataConfig(byte[] xmlBytes, String xmlName){
		try{
			DataConfig dConf = new DataConfig();	//返回值：对应一个xml文件
			Document doc = parseBytes(xmlBytes);
			Element root = doc.getRootElement();
			dConf.xmlName = xmlName;
			dConf.hashmapKey = xmlName;
			//enumeration, structure
			Iterator it = root.elementIterator();
			while(it.hasNext()) {	
				Element e1 = (Element)it.next();
				String text = e1.getName();
				if(text.equals("enumeration")) {
					DataConfig.Enu enu = new DataConfig.Enu();//一个枚举类型
					enu.name = e1.attributeValue("name");
					//读枚举值
					Iterator it1 = e1.elementIterator();
					while(it1.hasNext()) {	
						Element e2 = (Element)it1.next();	//一个枚举值
						enu.value.add(e2.attributeValue("value"));
					}
					enu.hashmapKey = enu.name;
					dConf.enuSet.put(enu.hashmapKey, enu);
				}
				if(text.equals("structure")) {
					DataConfig.Struct struct = new DataConfig.Struct();//一个结构
					struct.name = e1.attributeValue("name");
					//读成员
					Iterator it1 = e1.elementIterator();
					while(it1.hasNext()) {	
						Element e2 = (Element)it1.next();	
						DataConfig.Member mem = new DataConfig.Member();//一个成员
						mem.node = e2.getName();
						//遍历成员属性
						Iterator attrIt = e2.attributeIterator();
						while(attrIt.hasNext()) {
							Attribute attr = (Attribute)attrIt.next();
							switch(attr.getName()) {
							case "name":
								mem.name = attr.getText();break;
							case "offset":
								mem.offset = Integer.parseInt(attr.getText());break;
							case "size":
								mem.size = Integer.parseInt(attr.getText());break;
							case "bit_size":
								mem.bitSize = Integer.parseInt(attr.getText());break;
							case "count":
								mem.count = Integer.parseInt(attr.getText());break;
							case "type":
								mem.type = attr.getText();break;
							case "enumeration":
								mem.enumeration = attr.getText();break;
							case "first_bit":
								mem.firstBit = Integer.parseInt(attr.getText());break;
							}
						}
						struct.memSet.add(mem);
					}
					struct.hashmapKey = struct.name;
					dConf.mainStructKey = struct.hashmapKey;
					dConf.structSet.put(struct.hashmapKey, struct);
				}
			}
			return dConf;
		} catch(DocumentException e) {
			e.printStackTrace();
			System.err.println(e.toString());
			return null;
		}
	}


	private Document parseString(String xmlText) throws DocumentException {
		Document document = DocumentHelper.parseText(xmlText);
		return document;
	}
	private Document parseBytes(byte[] xmlBytes) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new ByteArrayInputStream(xmlBytes));
		return document;
	}

}
