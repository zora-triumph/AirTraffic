package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.google.gson.stream.JsonWriter;

import decoder.Record;

public class RecUtils {
	public static byte[] readFileToBytes(String path) {
		File file = new File(path);
        if(!file.isFile() || !file.exists()) {
        	return null;
        }
        long length = file.length();  
        byte[] content = new byte[(int)length];  
        
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(content);  
            in.close();  
            return content;
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
            return null;
        } catch (IOException e) {  
            e.printStackTrace();  
            System.err.println(e.toString());
            return null;
        }
	}
	static public String readXMLToString(String path) {
		File file = new File(path);
        if(!file.isFile() || !file.exists()) {
        	return null;
        }
        long length = file.length();  
        byte[] content = new byte[(int)length];  
        
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(content);  
            in.close();  
            return new String(content, App.getApp().getEncoding());  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
            return null;
        } catch (IOException e) {  
            e.printStackTrace();  
            System.err.println(e.toString());
            return null;
        }
	}
	//写入json文件
	static public void writeToJSON(ArrayList<Record> recList) {
		//创建目录
		Record rec0 = recList.get(0);
		String outPath = App.getApp().getAppConfig().getOutPath()+rec0.getType1();
		File dir = new File(outPath);
        if (!dir.exists()) {
        	if (dir.mkdirs()) {
                System.out.println("创建目录" + outPath + "成功！");
            } else {
                System.out.println("创建目录" + outPath + "失败！");
                return;
            }
        }
        //tojson
		//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		//String str = gson.toJson(recList);
        
		//write to file
		String fn = rec0.getType1()+"_"+rec0.getType2()+"_"+rec0.getDate()+"_"+rec0.getTime()+".json";
		File jsonFile = new File(outPath+"/"+fn);	
		OutputStreamWriter oStreamWriter;
		try {
			oStreamWriter = new OutputStreamWriter(new FileOutputStream(jsonFile, true), "utf-8");	//追加模式
			//to json
			JsonWriter jswriter = new JsonWriter(oStreamWriter);
			
			jswriter.beginObject();
			jswriter.name("type1").value(rec0.getType1());
			jswriter.name("type2").value(rec0.getType2());
			jswriter.name("Date").value(rec0.getDate());
			jswriter.name("Time").value(rec0.getTime());
			jswriter.name("rec").beginArray();
			for(Record r:recList) {
				jswriter.beginObject();
				jswriter.name("index").value(r.getIndex());
				for(Record.item ri:r.getData())
					jswriter.name(ri.getName()).value(ri.getValue());
				jswriter.endObject();
			}
			jswriter.endArray();
			jswriter.endObject();	
			
			jswriter.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	//写入xlsx性能奇差
	/*
	static public void writeToXlsx(ArrayList<Record> recList) {	
		//创建目录
		Record rec0 = recList.get(0);
		String outPath = appConf.getOutPath()+rec0.getType1();
		File dir = new File(outPath);
        if (!dir.exists()) {
        	if (dir.mkdirs()) {
                System.out.println("创建目录" + outPath + "成功！");
            } else {
                System.out.println("创建目录" + outPath + "失败！");
                return;
            }
        }
        try {
        	
            
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("sheet1");
			XSSFRow row0 = sheet.createRow(0);
			ArrayList<Record.item> ari = recList.get(0).getData();
			for(int j=0; j<ari.size(); ++j)
			{
				XSSFCell cell = row0.createCell(j);
				cell.setCellValue(ari.get(j).getName());
			}
			for(int i=0;i<recList.size();++i)
			//for(int i=0;i<5;++i)
			{
				XSSFRow row = sheet.createRow(i+1);
				ArrayList<Record.item> ri = recList.get(i).getData();
				for(int j=0; j<ri.size(); ++j)
				{
					XSSFCell cell = row.createCell(j);
					cell.setCellValue(ri.get(j).getValue());
				}
			}
        
			String fn = rec0.getType1()+"_"+rec0.getType2()+"_"+rec0.getDate()+"_"+rec0.getTime()+".xlsx";
        	File xlsxFile = new File(outPath+pathSep+fn);	//wsl
            FileOutputStream fileoutputStream = new FileOutputStream(xlsxFile);
            wb.write(fileoutputStream);
            fileoutputStream.close();
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	*/
	//写入csv
	static public void writeToCsv(ArrayList<Record> recList) 
	{
		//创建目录
		Record rec0 = recList.get(0);
		String outPath = App.getApp().getAppConfig().getOutPath()+rec0.getType1();
		File dir = new File(outPath);
        if (!dir.exists()) {
        	if (dir.mkdirs()) {
                System.out.println("创建目录" + outPath + "成功！");
            } else {
                System.out.println("创建目录" + outPath + "失败！");
                return;
            }
        }

        StringBuffer buf = new StringBuffer();
        //生成表头
        ArrayList<Record.item> ari = recList.get(0).getData();
		for(int j=0; j<ari.size(); ++j)
			buf.append(ari.get(j).getName()).append(',');
		buf.append("\r\n");
		//生成数据
		for(int i=0;i<recList.size();++i)
		{
			ArrayList<Record.item> ri = recList.get(i).getData();
			for(int j=0; j<ri.size(); ++j)
				buf.append(ri.get(j).getValue()).append(',');
			buf.append("\r\n");	
		}
		//write to file
		String fn = rec0.getType1()+"_"+rec0.getType2()+"_"+rec0.getDate()+"_"+rec0.getTime()+".csv";
		File csvFile = new File(outPath+"/"+fn);	
		try {
			OutputStreamWriter oStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile, false), "utf-8");//非追加
			oStreamWriter.write(buf.toString());
			oStreamWriter.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}	
	//在路径末尾加上路径分隔符
	static public String addPathSeparator(String path)
	{
		if(path == null)
			return null;
		if(path.length()!=0 && !(path.substring(path.length()-1).equals(File.separator)))
		{
			if(File.separator.equals("\\"))
				return path+File.separator+File.separator;
			else
				return path+File.separator;
		}
		return path;
	}
	//去掉路径末尾的路径分隔符
	static public String rmPathSeparator(String path)
	{
		if(path == null)
			return null;
		if(path.length()!=0 && (path.substring(path.length()-2).equals(File.separator+File.separator)))
			return path.substring(0,path.length()-2);
		if(path.length()!=0 && (path.substring(path.length()-1).equals(File.separator)))
			return path.substring(0,path.length()-1);
		return path;
	}
	//判断String是否为日期格式
	static public boolean isDate(String str, String parsePatterns[]) {
        if (parsePatterns == null) {
            return false;
        }
        try {
			DateUtils.parseDate(str, parsePatterns);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	//创建目录
	static public boolean createDir(String path)
	{
		File dir = new File(path);
        if (!dir.exists()) {
        	if (dir.mkdirs()) {
                System.out.println("创建目录" + path + "成功！");
                return true;
            } else {
                System.out.println("创建目录" + path + "失败！");
                return false;
            }
        }
        return false;
	}
}
