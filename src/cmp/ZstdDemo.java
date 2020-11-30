package cmp;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import com.github.luben.zstd.ZstdDictTrainer;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ZstdDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZstdDemo.class);

    // 训练文件
    private static final String TRAIN_FILE = "/Users/zhouna/Downloads/code/zstd_test/log/train10.txt";

    // 待压缩文件
    private static final String COMPRESS_FILE = "/Users/zhouna/Downloads/code/zstd_test/log/train5k.txt";

    // sample 
    private static final String SAMPLE_FILE = "/Users/zhouna/Downloads/code/zstd_test/log/testA.txt";

    // 压缩等级，以时间换空间
    private static final Integer LEVEL = 10;

    // 解压时字节数组的最大值
    private static final Integer MAX_SIZE = 1000000;

    private static ZstdDictCompress compressDict;

    private static ZstdDictDecompress decompressDict;
    
    private static ZstdDictTrainer dictTrain;



    public static void train() throws IOException {
        // 初始化词典对象
        String dictContent = FileUtils.readFileToString(new File(TRAIN_FILE), StandardCharsets.UTF_8);
        byte[] dictBytes = dictContent.getBytes(StandardCharsets.UTF_8);
        compressDict = new ZstdDictCompress(dictBytes, LEVEL);
//        System.out.println("compress dict length: "+dictBytes.length/1024 +" kb");
        System.out.println("compress dict : "+compressDict);
//        decompressDict = new ZstdDictDecompress(dictBytes);
    }
    
    public static void dict() throws IOException {
    	String dictContent = FileUtils.readFileToString(new File(TRAIN_FILE), StandardCharsets.UTF_8);
        byte[] dictBytes = dictContent.getBytes(StandardCharsets.UTF_8);
        int dictSize = dictBytes.length;
        String sampleContent = FileUtils.readFileToString(new File(SAMPLE_FILE), StandardCharsets.UTF_8);
        byte[] sampleBytes = sampleContent.getBytes(StandardCharsets.UTF_8);
        int sampleSize = sampleBytes.length;
        dictTrain = new ZstdDictTrainer(sampleSize,dictSize);
        System.out.println("dict  : "+dictTrain);
    }

    public static void compress(byte[] bytes) {

        // 压缩数据
        long compressBeginTime = System.currentTimeMillis();
//        byte[] compressed = Zstd.compress(bytes, compressDict);
        byte[] compressed = Zstd.compress(bytes, compressDict);
        long compressEndTime = System.currentTimeMillis();
        long SpendTime = compressEndTime - compressBeginTime;
        System.out.println("compress spend time: "+ SpendTime +"ms");

        float rate_num = (((float )(bytes.length / 1024) / 1024) * 1000) / SpendTime;
        DecimalFormat df1 = new DecimalFormat(".00");
        String rate = df1.format(rate_num);
        System.out.println("compress rate: "+ (rate) + " MB/s");
        
        float ratio_num = (float)(compressed.length)/(bytes.length);
        DecimalFormat df2 = new DecimalFormat("0.00%");
        String ratio = df2.format(ratio_num);
        System.out.println("compress ratio: "+ (ratio));

        // 解压数据
//        long decompressBeginTime = System.currentTimeMillis();
//        // 第 3 个参数不能小于解压后的字节数组的大小
//        byte[] decompressed = Zstd.decompress(compressed, decompressDict, MAX_SIZE);
//        long decompressEndTime = System.currentTimeMillis();
//        LOGGER.info("decompress spend time: {}", decompressEndTime - decompressBeginTime);
//        LOGGER.info("decompressed data length: {}", decompressed.length);
    }
    

    public static void main(String[] args) throws IOException {
        String s = FileUtils.readFileToString(new File(COMPRESS_FILE), StandardCharsets.UTF_8);
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        long TrainBeginTime = System.currentTimeMillis();
        train();
        long TrainEndTime = System.currentTimeMillis();
        long DiffTime = TrainEndTime - TrainBeginTime;
        System.out.println("train spend time: "+ DiffTime +"ms");
        
        dict();

        

//        ZstdDictCompress tainDic = train();
        
        compress(bytes);
    }
}