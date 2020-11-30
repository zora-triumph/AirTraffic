package decoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DataConfig {
	public void print() {
		System.out.println("------------data------------");
		System.out.println("hashmapKey = "+hashmapKey);
		System.out.println("xmlName = "+xmlName);
		System.out.println("mainStructKey = "+mainStructKey);
		Iterator it = enuSet.entrySet().iterator();
		while(it.hasNext()) {
			System.out.println("------enu------");
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String)entry.getKey();
			Enu enu = (Enu)entry.getValue();
			System.out.println("hashmapKey = "+enu.hashmapKey);
			System.out.println("name = "+enu.name);
			System.out.println("value:");
			for(String s : enu.value) 
				System.out.print(s+", ");
			System.out.print("\n");
		}
		it = structSet.entrySet().iterator();
		while(it.hasNext()) {
			System.out.println("------struct------");
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String)entry.getKey();
			Struct struct = (Struct)entry.getValue();
			System.out.println("hashmapKey = "+struct.hashmapKey);
			System.out.println("name = "+struct.name);
			for(Member m:struct.memSet) {
				System.out.println("---mem---");
				System.out.println("node = "+m.node);
				System.out.println("name = "+m.name);
				System.out.println("offset = "+m.offset);
				System.out.println("size = "+m.size);
				System.out.println("firstBit = "+m.firstBit);
				System.out.println("bitSize = "+m.bitSize);
				System.out.println("count = "+m.count);
				System.out.println("type = "+m.type);
				System.out.println("enumeration = "+m.enumeration);
			}
		}
	}
	
	public String hashmapKey = "";//等于xmlName
	public String xmlName = "";
	public String mainStructKey = "";	//main结构的key
	public HashMap<String, Enu> enuSet = new HashMap<String, Enu>();
	public HashMap<String, Struct> structSet = new HashMap<String, Struct>();
	
	public static class Enu{
		public String hashmapKey = "";
		public String name = "";
		public ArrayList<String> value = new ArrayList<String>();
	}
	public static class Struct{
		public String hashmapKey = "";
		public String name = "";
		public ArrayList<Member> memSet = new ArrayList<Member>();
	}
	public static class Member{
		public String node = "";	//simple，padding, simple-array, complex, complex-array, simple 
		public String name = "";
		public int offset = 0;
		public int size = 0;
		public int firstBit = 0;
		public int bitSize = 0;
		public int count = 1;	//如果类型是xxx-array，count代表member重复次数，count默认为1
		public String type = "";
		public String enumeration = "";
	}
}
