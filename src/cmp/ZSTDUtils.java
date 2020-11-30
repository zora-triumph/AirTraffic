package cmp;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import com.github.luben.zstd.*;


public class ZSTDUtils {
	public static long compressFile(String inFile, String outFolder, ByteBuffer inBuffer, ByteBuffer compressedBuffer, int compressionLevel) throws IOException {
	    File file = new File(inFile);
	    File outFile = new File(outFolder, file.getName() + ".zs");
	    long numBytes = 0l;

	    try (RandomAccessFile inRaFile = new RandomAccessFile(file, "r");
	        RandomAccessFile outRaFile = new RandomAccessFile(outFile, "rw");
	                FileChannel inChannel = inRaFile.getChannel();
	                FileChannel outChannel = outRaFile.getChannel()) {
	        inBuffer.clear();
	        while(inChannel.read(inBuffer) > 0) {
	            inBuffer.flip();
	            compressedBuffer.clear();

	            long compressedSize = Zstd.compressDirectByteBuffer(compressedBuffer, 0, compressedBuffer.capacity(), inBuffer, 0, inBuffer.limit(), compressionLevel);
	            numBytes+=compressedSize;
	            compressedBuffer.position((int)compressedSize);
	            compressedBuffer.flip();
	            outChannel.write(compressedBuffer);
	            inBuffer.clear(); 
	        }
	    }

	    return numBytes;
	}

	public static long decompressFile(String originalFilePath, String inFolder, ByteBuffer inBuffer, ByteBuffer decompressedBuffer) throws IOException {
	    File outFile = new File(originalFilePath);
	    File inFile = new File(inFolder, outFile.getName() + ".zs");
	    outFile = new File(inFolder, outFile.getName());

	    long numBytes = 0l;

	    try (RandomAccessFile inRaFile = new RandomAccessFile(inFile, "r");
	        RandomAccessFile outRaFile = new RandomAccessFile(outFile, "rw");
	                FileChannel inChannel = inRaFile.getChannel();
	                FileChannel outChannel = outRaFile.getChannel()) {

	        inBuffer.clear();

	        while(inChannel.read(inBuffer) > 0) {
	            inBuffer.flip();
	            decompressedBuffer.clear();
	            long compressedSize = Zstd.decompressDirectByteBuffer(decompressedBuffer, 0, decompressedBuffer.capacity(), inBuffer, 0, inBuffer.limit());
	            System.out.println(Zstd.isError(compressedSize) + " " + compressedSize);
	            numBytes+=compressedSize;
	            decompressedBuffer.position((int)compressedSize);
	            decompressedBuffer.flip();
	            outChannel.write(decompressedBuffer);
	            inBuffer.clear(); 
	        }
	    }

	    return numBytes;
	}
	
	public void main() {
		String infile = "Users/zhouna/Documents/NBJL/hbase数据压缩与去重/zstd测试/REC_FPL_191020_0000.csnp";
		String outfile = "/Users/zhouna/Documents/NBJL/hbase数据压缩与去重/zstd测试/";
		
//		compressFile(infile, outfile,, , 1);
		
	}

	
}