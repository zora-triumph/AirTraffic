package decoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RECConfig {
	
//	public void print() {
//		System.out.println("------------rec------------");
//		System.out.println("recHashmapKey = "+recHashmapKey);
//		System.out.println("recName = "+recName);
//		System.out.println("recPrefix = "+recPrefix);
//		System.out.println("recXml = "+recXml);
//		System.out.println("recEx = "+recEx);
//		Iterator iter = RECStrucSet.entrySet().iterator();
//		while (iter.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter.next();
//			String key = (String)entry.getKey();
//			System.out.println("------struct------");
//			RECStruct val = (RECStruct)entry.getValue();
//			System.out.println("hashmapkey = "+val.hashmapKey);
//			System.out.println("name = "+val.name);
//			System.out.println("type = "+val.type);
//			for(RECMember m : val.RECMemSet) {
//				System.out.println("---member---");
//				System.out.println("name = "+m.name);
//				System.out.println("type = "+m.type);
//				System.out.println("structName = "+m.structName);
//				System.out.println("count = "+String.valueOf(m.count));
//				System.out.println("size = "+String.valueOf(m.size));
//			}
//		}
//		
//	}
//	public String recHashmapKey = "";
//	public String recName = "";	//"fpl"
//	public String recPrefix = "";	//rec文件命名
//	public String recXml = "";	//xml文件名	
//	public String recEx = "";	//rec文件后缀名
//	public HashMap<String, RECStruct> RECStrucSet = new HashMap<String, RECStruct>(); //结构是按名字查找的，是无序的。
//	
//	public static class RECStruct{
//		public String hashmapKey = "";
//		public String name = "";	//结构名
//		public String type = "";	//structure, file
//		public ArrayList<RECMember> RECMemSet = new ArrayList<RECMember>();	//成员是有序的，必须用ArrayList
//	}
//	public static class RECMember{
//		public String name = "";	//成员名
//		public String type = "";	//simple, complex, main
//		public String structName = "";	//对应的结构名
//		public int count = 0;	//出现次数
//		public int size = 0;	//大小
//	}
	public void print() {
		System.out.println("------------rec------------");
		System.out.println("------cdc------");
		System.out.println("csnp_file_header = "+cdcConf.csnp_file_header);
		System.out.println("csnp_record_header = "+cdcConf.csnp_record_header);
		System.out.println("cupd_record_header = "+cdcConf.cupd_record_header);
		Iterator iter = cdcConf.data_t.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			CDCStruct cs = (CDCStruct)entry.getValue();
			System.out.println("---"+cs.name+"---");
			System.out.println("hashkey = "+cs.hashKey);
			System.out.println("name = "+cs.name);
			System.out.println("prefix = "+cs.prefix);
			System.out.println("xml = "+cs.xml);
			System.out.println("size = "+cs.size);
			System.out.println("pad = "+cs.pad);
		}
	}
	public CDCConfig cdcConf = new CDCConfig();
	
	public class CDCConfig {
		public int csnp_file_header = 0;
		public int csnp_record_header = 0;
		public int cupd_record_header = 0;
		public HashMap<String, CDCStruct> data_t = new HashMap<String, CDCStruct>();
	}
	public static class CDCStruct{
		public String hashKey = "";//等于prefix
		public String name = "";
		public String prefix = "";
		public String xml = "";
		public int size = 0;
		public int pad = 0;
	}
}
