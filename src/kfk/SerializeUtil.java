package kfk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cmp.GZIPUtils;

public class SerializeUtil {
    public static byte[] serialize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            // 序列化
        	// 序列化之后传输的是字节数组
        	// baos:
        	// oos:  java.io.ObjectOutputStream@32104358
        	// object:  decoder.Record@1be2b470
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);          
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            oos.close();
            baos.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }
        return null;
    }
 
    @SuppressWarnings("unchecked")
    public static <T> Object deserialize(byte[] bytes,Class<T> className) {
        ByteArrayInputStream bais = null;
        T tmpObject = null;
        try {
            // 反序列化
        	// 反序列化ois：java.io.ObjectInputStream@5d8e1238
        	// tmpObject:decoder.Record@628cef02
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            tmpObject = (T)ois.readObject();
            ois.close();
            bais.close();
            return tmpObject;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }
        return null;
    }
}

