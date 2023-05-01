package com.checo.moocmini.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {

    /**
     * 文件分块测试
     */
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("C:/IT/TestGarbage/BigFileTest/BigFileTest.mp4");
        // 分块文件存储路径
        String chunkFilePath = "C:/IT/TestGarbage/BigFileTest/Chunk/";
        // 分块文件的大小，MinIO 要求分块的大小至少是 5MB
        long chunkSize = 1024 * 1024 * 5;
        // 分块数
        long chunkNum = (long) Math.ceil((double) sourceFile.length() / chunkSize);
        // 使用流从源文件读取数据
        RandomAccessFile randomAccessFileRead = new RandomAccessFile(sourceFile, "r");
        // 缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            // 使用流向分块文件中写入数据
            RandomAccessFile randomAccessFileReadWrite = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = randomAccessFileRead.read(bytes)) != -1) {
                randomAccessFileReadWrite.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize)
                    break;
            }
            randomAccessFileReadWrite.close();
        }
        randomAccessFileRead.close();
    }

    /**
     * 文件分块合并测试
     */
    @Test
    public void testMerge() throws IOException {
        // 分块文件目录
        File chunkFolder = new File("C:/IT/TestGarbage/BigFileTest/Chunk/");
        // 分块数组
        File[] chunkFileArray = chunkFolder.listFiles();
        // 将分块数组转成分块链表，方便排序
        List<File> chunkFileList = Arrays.asList(chunkFileArray);
        // 按照文件名从小到达排序
        chunkFileList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));

        // 合并文件
        File mergeFile = new File("C:/IT/TestGarbage/BigFileTest/BigFileTestMerge.mp4");
        if (mergeFile.exists())
            mergeFile.delete();
        mergeFile.createNewFile();
        RandomAccessFile randomAccessFileReadWrite = new RandomAccessFile(mergeFile, "rw");
        randomAccessFileReadWrite.seek(0); // 指针指向文件顶端
        byte[] bytes = new byte[1024];
        for (File chunkFile : chunkFileList) {
            RandomAccessFile randomAccessFileRead = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = randomAccessFileRead.read(bytes)) != -1)
                randomAccessFileReadWrite.write(bytes, 0, len);
            randomAccessFileRead.close();
        }
        randomAccessFileReadWrite.close();

        // 校验文件
        File originalFile = new File("C:/IT/TestGarbage/BigFileTest/BigFileTest.mp4");
        FileInputStream originalFileInputStream = new FileInputStream(originalFile);
        FileInputStream mergeFileInputStream = new FileInputStream(mergeFile);
        String originalFileMd5 = DigestUtils.md5Hex(originalFileInputStream);
        String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
        originalFileInputStream.close();
        mergeFileInputStream.close();
        if (mergeFileMd5.equals(originalFileMd5))
            System.out.println("文件合并完成");
        else
            System.out.println("文件合并失败");
    }
}
