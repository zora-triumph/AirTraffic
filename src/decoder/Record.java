package decoder;

import java.io.Serializable;
import java.util.ArrayList;

public class Record implements Serializable{
	private String index="";	//在文件中的顺序
	private String date="";
	private String time="";
	private String type1="";	//fpl
	private String type2="";	//snp/upd
	private ArrayList<item> data = new ArrayList<item>();
	
	public void print() 
	{
		System.out.println(date+'.'+time+'.'+type1+'.'+type2+'.'+index);
		for(item i:data)
		{
			System.out.println(i.getName()+": "+i.getValue());
		}
	}
	public ArrayList<item> getData() {
		return data;
	}
	public void setData(ArrayList<item> data) {
		this.data = data;
	}
	public String getType1() {
		return type1;
	}
	public void setType1(String type1) {
		this.type1 = type1;
	}
	public String getType2() {
		return type2;
	}
	public void setType2(String type2) {
		this.type2 = type2;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getKey()
	{
		return date+'.'+time+'.'+type1+'.'+type2+'.'+index;
	}
	public static class item implements Serializable{
		private String name = "";
		private String value = "";
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
}
