package com.tbit.tbitblesdk.bike;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wouter on 9-10-14.
 */
public class OtaFile {
	private static String filesDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Suota";
	private static final int fileChunkSize = 20;
	private InputStream inputStream;
	private byte crc;
	private byte[] bytes;

	private byte[][][] blocks;

	private int fileBlockSize = 0;
	private int bytesAvailable;
	private int numberOfBlocks = -1;
	private int chunksPerBlockCount;
	private int totalChunkCount;

	private OtaFile(InputStream inputStream) throws IOException {
		this.inputStream = inputStream;
		this.bytesAvailable = this.inputStream.available();

		this.bytes = new byte[this.bytesAvailable + 1];
		this.inputStream.read(this.bytes);
		this.crc = getCrc();
		this.bytes[this.bytesAvailable] = this.crc;
	}

	public int getFileBlockSize() {
		return this.fileBlockSize;
	}

	public int getNumberOfBytes() {
		return this.bytes.length;
	}

	public void setFileBlockSize(int fileBlockSize) {
		this.fileBlockSize = fileBlockSize;
		this.chunksPerBlockCount = (int) Math.ceil((double) fileBlockSize / (double) fileChunkSize);
		this.numberOfBlocks = (int) Math.ceil((double) this.bytes.length / (double) this.fileBlockSize);
		this.initBlocks();
	}

	private void initBlocksSuota() {
		int totalChunkCounter = 0;
		blocks = new byte[this.numberOfBlocks][][];
		int byteOffset = 0;
		// Loop through all the bytes and split them into pieces the size of the default chunk size
		for (int i = 0; i < this.numberOfBlocks; i++) {
			int blockSize = this.fileBlockSize;
			if (i + 1 == this.numberOfBlocks) {
				blockSize = this.bytes.length % this.fileBlockSize;
			}
			int numberOfChunksInBlock = (int) Math.ceil((double) blockSize / fileChunkSize);
			int chunkNumber = 0;
			blocks[i] = new byte[numberOfChunksInBlock][];
			for (int j = 0; j < blockSize; j += fileChunkSize) {
				// Default chunk size
				int chunkSize = fileChunkSize;
				// Last chunk of all
				if (byteOffset + fileChunkSize > this.bytes.length) {
					chunkSize = this.bytes.length - byteOffset;
				}
				// Last chunk in block
				else if (j + fileChunkSize > blockSize) {
					chunkSize = this.fileBlockSize % fileChunkSize;
				}

//				Log.d("chunk", "total bytes: " + this.bytes.length + ", offset: " + byteOffset + ", block: " + i + ", chunk: " + (chunkNumber + 1) + ", blocksize: " + blockSize + ", chunksize: " + chunkSize);
				byte[] chunk = Arrays.copyOfRange(this.bytes, byteOffset, byteOffset + chunkSize);
				blocks[i][chunkNumber] = chunk;
				byteOffset += chunkSize;
				chunkNumber++;
				totalChunkCounter++;
			}
		}
		// Keep track of the total chunks amount, this is used in the UI
		this.totalChunkCount = totalChunkCounter;
	}


	// Create the array of blocks using the given block size.
	private void initBlocks() {
		this.initBlocksSuota();
	}

	public byte[][] getBlock(int index) {
		return blocks[index];
	}

	public void close() {
		if (this.inputStream != null) {
			try {
				this.inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getNumberOfBlocks() {
		return this.numberOfBlocks;
	}

	public int getChunksPerBlockCount() {
		return chunksPerBlockCount;
	}

	public int getTotalChunkCount() {
		return this.totalChunkCount;
	}

	private byte getCrc() throws IOException {
		byte crc_code = 0;
		for (int i = 0; i < this.bytesAvailable; i++) {
			Byte byteValue = this.bytes[i];
			int intVal = byteValue.intValue();
			crc_code ^= intVal;
		}
		Log.d("crc", String.format("Fimware CRC: %#04x", new Object[]{Integer.valueOf(crc_code & 255)}));
		return crc_code;
	}

	public static OtaFile getByFileName(String filename) throws IOException {
		// Get the file and store it in fileStream
		InputStream is = new FileInputStream(filesDir + "/" + filename);
		return new OtaFile(is);
	}

	public static OtaFile getByFile(File file) throws IOException {
		// Get the file and store it in fileStream
		InputStream is = new FileInputStream(file);
		return new OtaFile(is);
	}

	public static Map list() {
		java.io.File f = new java.io.File(filesDir);
		java.io.File file[] = f.listFiles();
		Log.d("Files", "Size: "+ file.length);
		Map map = new HashMap<Integer, String>();
		for (int i=0; i < file.length; i++)
		{
			Log.d("Files", "FileName:" + file[i].getName());
			map.put(file[i].getName(), file[i].getName());
		}

//		Field[] fields = R.raw.class.getFields();
//		for (int count = 0; count < fields.length; count++) {
//			Field file = fields[count];
//			int resourceID = -1;
//			try {
//				resourceID = file.getInt(file);
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
//			String name = file.getName();
//			map.put(resourceID, name);
//		}
		return map;
	}

//	public static InputStream get(Context c, String filename) {
//		InputStream inputStream = c.getResources().openRawResource(resourceID);
//		return inputStream;
//	}

	public static void createFileDirectories(Context c) {
		String directoryName = filesDir;
		java.io.File directory;
		directory = new java.io.File(directoryName);
		directory.mkdirs();
	}
}
