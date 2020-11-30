package decoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import app.App;

public class CDCDecoder {
	//common
	private int snp_f_header;
	private int snp_r_header;
	private int upd_r_header;
	//unique
	private String type1;	//fpl
	private String type2;	//snp/upd
	private DataConfig dc = null;
	private int structSize;
	private int structPad;
	//decode
	private int recOffset;
	private byte[] binaryData;
	private String fn;//待解析的文件名
	
	public CDCDecoder(byte[] fileBinary, String fileName) {
		//common
		RECConfig rc = App.getApp().getRECConfig();	
		snp_f_header = rc.cdcConf.csnp_file_header;
		snp_r_header = rc.cdcConf.csnp_record_header;
		upd_r_header = rc.cdcConf.cupd_record_header;
		//decode
		recOffset = 0;
		binaryData = fileBinary;
		fn = fileName;
		//unique
		String file_prefix = fileName.substring(0, fileName.length()-16);
		RECConfig.CDCStruct cs= rc.cdcConf.data_t.get(file_prefix);	//根据file_prefix获取RECConfig
		type1 = cs.name;
		type2 = fileName.substring(fileName.length()-3, fileName.length());
		dc = App.getApp().getDataConf().get(cs.xml);	//根据RECConfig中的xml获取DataConfig
		structSize = cs.size;
		structPad = cs.pad;

	}
	
	public Record extractRecord() {
		int cursor = 0;
		Record rec = null;
		if(type2.equals("upd")) {
			cursor = recOffset*(upd_r_header + structSize + structPad) + upd_r_header;
		}
		else if(type2.equals("snp")) {
			cursor = snp_f_header + recOffset*(snp_r_header + structSize + structPad) + snp_r_header;
		}
		//取一条新记录
		if(cursor < binaryData.length) {
			byte[] recByte = Arrays.copyOfRange(binaryData, cursor, cursor + structSize);
			//解析
			rec = new Record();
			//TODO log
			//System.out.println("------ record "+recOffset+" ------");
			parse(rec, recByte, dc, dc.mainStructKey, "", 0);
	
			rec.setDate(fn.substring(fn.length()-16, fn.length()-10));
			rec.setTime(fn.substring(fn.length()-9, fn.length()-5));
			rec.setType1(type1);
			rec.setType2(type2);
			rec.setIndex(String.valueOf(recOffset));
			recOffset += 1;
		}
		return rec;
	}
	/**
	 从二进制数据中解析出一个record；递归
	 * @param result 最终要得到的一条rec记录
	 * @param binaryREC 一条解析前的二进制rec记录
	 * @param dc 对应的xml文件配置
	 * @param curStruct 当前要解析的结构
	 * @param namePath 节点名字前缀
	 * @param cursor 当前结构在byte[]中的起始位置
	 * @return 返回当前结构的尾后cursor
	 */

	private int parse(Record rec, byte[] binaryREC, DataConfig dc, String curStruct, String namePath, int cursor) {
		String int_t[] = {"b8","b32","u8","u16","u32","s8","s16","s32"};
		String str_t[] = {"string", "cdata"};
		String flt_t[] = {"f32","f64"};
		String enu_t[] = {"e32","e8"};
		String bit_t[] = {"bit-b","bit-u","bit-e"};

		int res = 0;//返回值
		
		DataConfig.Struct dcs = dc.structSet.get(curStruct);
		int memIndex= 0;
		
		for(DataConfig.Member m :dcs.memSet) {	//mem
			switch(m.node) {
			case "simple":case "simple-array":
				int size = 0;//Member大小，用于计算返回值
				for(int i=0; i < m.count; ++i) {	//array
					
					Record.item ri = new Record.item();
					if(m.count > 1)
						ri.setName(namePath+m.name+"#"+i);
					else
						ri.setName(namePath+m.name);
					
					if(Arrays.asList(int_t).contains(m.type)) {	//整型
						//从字段类型中获取字段大小
						size = Integer.parseInt(m.type.substring(1, m.type.length()))/8;
						//获取保存字段的原始二进制数据
						int begin = cursor + m.offset + i*size;
						long v = bytesToLong(binaryREC, begin, size, App.getApp().getRECByteOrder());
						
						ri.setValue(String.valueOf(v));
					}
					else if(m.type.equals(flt_t[0])) {	//float
						size = 4;
						int begin = cursor + m.offset + i*4;
						byte[] orig = Arrays.copyOfRange(binaryREC, begin, begin+4);
						ByteBuffer buffer = ByteBuffer.allocate(4);
						buffer.put(orig);
						buffer.flip();
						buffer.order(App.getApp().getRECByteOrder());
						float v = buffer.getFloat();

						ri.setValue(String.valueOf(v));
					}
					else if(m.type.equals(flt_t[1])) {	//double
						size = 8;
						int begin = cursor + m.offset + i*8;
						byte[] orig = Arrays.copyOfRange(binaryREC, begin, begin+8);
						ByteBuffer buffer = ByteBuffer.allocate(8);
						buffer.put(orig);
						buffer.flip();
						buffer.order(App.getApp().getRECByteOrder());
						double v = buffer.getDouble();

						ri.setValue(String.valueOf(v));
					}
					else if(Arrays.asList(str_t).contains(m.type)) {	//字符串
						size = m.size;
						int begin = cursor + m.offset + i*m.size;
						byte[] orig = Arrays.copyOfRange(binaryREC, begin, begin+m.size);
						//转为String
						String v = new String(orig, App.getApp().getEncoding());

						ri.setValue(v);
					}
					else if(Arrays.asList(enu_t).contains(m.type)){	  //枚举
						//从字段类型中获取字段大小
						size = Integer.parseInt(m.type.substring(1, m.type.length()))/8;
						//获取保存字段的原始二进制数据
						int begin = cursor + m.offset + i*size;
						long index = bytesToLong(binaryREC, begin, size, App.getApp().getRECByteOrder());
						//查找枚举值
						String v;
						//TODO 枚举值可能出现异常
						if(!(index >= dc.enuSet.get(m.enumeration).value.size())) {
							v = dc.enuSet.get(m.enumeration).value.get((int) index);
						}
						else {
							v = dc.enuSet.get(m.enumeration).value.get(0);
							//TODO test
							System.out.println("exception enum");
							System.out.println(m.node+": "+m.name);
							System.out.println("\ttype = "+m.type);
							System.out.println("\t0x "+bytesToHex(binaryREC, begin, size));
							System.out.println("\toffset = "+begin);
							System.out.println("\tenum value: "+index);
						}

						ri.setValue(v);	
					}
					//按位存储的字段应该不存在array
					else if(Arrays.asList(bit_t).contains(m.type)) {	//按位操作
						size = 1;
						//取出对应的字节
						byte orig = binaryREC[cursor+m.offset];
						//把位转成整型
						int v = bitsToInt(orig, m.firstBit, m.bitSize); 
						if(m.type.equals(bit_t[2])) {	//bit-e
							//查找枚举值
							String vStr;
							//TODO 枚举值可能出现异常
							if(!(v>=dc.enuSet.get(m.enumeration).value.size())) {
								vStr = dc.enuSet.get(m.enumeration).value.get(v);
							}
							else {
								vStr = dc.enuSet.get(m.enumeration).value.get(0);
								//TODO log
								System.out.println("异常值");
								System.out.println(m.node+": "+m.name);
								System.out.println("\ttype = "+m.type);
								System.out.println("\t0x"+bytesToHex(binaryREC, cursor+m.offset, 1));
								System.out.println("\toffset = "+(cursor+m.offset));
								System.out.println("\t枚举值："+v);
							}
							ri.setValue(vStr);
						}
						else {	//bit-b, bit-u
							ri.setValue(String.valueOf(v));
						}
					}
					rec.getData().add(ri);
				}//for array
				if(memIndex == dcs.memSet.size()-1)	//最后一个mem
					res = cursor + m.offset + m.count*size;
				break;
			case "padding":
				res = cursor + m.offset;
				break;
			case "complex":case "complex-array":
				int subCursor = cursor + m.offset;
				for(int i=0; i<m.count; ++i) {
					//递归地解析complex结构
					String newnp;
					if(m.count > 1)
						newnp = namePath+m.name+"#"+i+".";
					else
						newnp = namePath+m.name+".";
					subCursor = parse(rec, binaryREC, dc, m.type, newnp, subCursor);
				}
				if(memIndex == dcs.memSet.size()-1)
					res = subCursor;
				break;
			}
			++memIndex;
		}//for mem
		return res;
	}
	private int bitsToInt(byte orig, int first_bit, int bit_size) {
		//先左移再进行无符号右移,去掉高位的无用数据
//		int tmp = orig<<(first_bit + 24);
		int tmp = orig<<(8-(first_bit+bit_size)+24);	//一个字节的最右边的位算作第0位
		return (tmp>>>(32-bit_size)) & 0xff;
	}
	//将byte[]数组转换为long类型
	private long bytesToLong(byte[] src, int pos, int size, ByteOrder bo) {
		byte[] orig = {0,0,0,0,0,0,0,0};
		if(bo == ByteOrder.BIG_ENDIAN) 	//大端字节序
			System.arraycopy(src, pos, orig, 8-size, size);
		else if(bo == ByteOrder.LITTLE_ENDIAN) {	//小端字节序
			for(int i=0;i<size;++i) 
				orig[7-i] = src[pos+i];
		}
		//将字节数组转换为long
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(orig);
		buffer.flip();
		buffer.order(ByteOrder.BIG_ENDIAN);//按大端字节序获取long的值
		long v = buffer.getLong();
		return v;
	}

	private String bytesToHex(byte[] src, int pos, int size) {  
		byte[] bytes = new byte[size];
		System.arraycopy(src, pos, bytes, 0, size);
	    StringBuffer sb = new StringBuffer();  
	    for(int i = 0; i < bytes.length; i++) {  
	        String hex = Integer.toHexString(bytes[i] & 0xFF);  
	        if(hex.length() < 2){  
	            sb.append(0);  
	        }  
	        sb.append(hex); 
	    }  
	    return sb.toString();
	}  

}
