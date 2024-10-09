package com.xuecheng.media;


import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {

    //分块测试
    @Test
    public void testChunk() throws Exception {
        //源文件
        File sourceFile = new File("D:\\MyFile\\BUCT\\大一\\小学期\\英语语音\\英语语音模仿材料" +
                "\\I believe I can fly\\R. Kelly - I Believe I Can Fly_高清.mp4");
        //设置分块文件的存储路径
        String chunkFilePath = "D:\\MyFile\\BUCT\\chunk\\";
        File chunkFolder = new File(chunkFilePath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //设置分块文件的大小
        int chunkSize = 1024*1024*5;
        //分块文件的数量
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从文件中读取数据，向文件中写数据
        //读流
        RandomAccessFile rafRead = new RandomAccessFile(sourceFile,"r");
        //缓存区
        byte [] bytes =new byte[1024];
        //循环分块文件的数量的次数
        for (int i = 0; i < chunkNum; i++) {
            //设置分块文件
            File chunkFile =new File(chunkFilePath + i);
            if(chunkFile.exists()){
                chunkFile.delete();
            }
            boolean newFile = chunkFile.createNewFile();
            if(newFile){
                int length = -1;
                RandomAccessFile rafReadAndWrite = new RandomAccessFile(chunkFile,"rw");
                //读取文件,读取结束时，length会被设置为-1
                while((length= rafRead.read(bytes))!=-1){
                    //往分块中写数据
                    rafReadAndWrite.write(bytes,0,length);
                    if(chunkFile.length() >= chunkSize){
                        break;
                    }
                }
                rafReadAndWrite.close();
            }
        }
        rafRead.close();
    }

    //将分块合并
    @Test
    public void testMerge() throws IOException {
        //分块文件路径
        File chunkFile =new File("D:\\MyFile\\BUCT\\chunk\\");
        //合并文件
        File mergeFile = new File("D:\\MyFile\\BUCT\\R. Kelly - I Believe I CanFly_高清_2.mp4");
        //源文件
        File sourceFile = new File("D:\\MyFile\\BUCT\\大一\\小学期\\英语语音\\英语语音模仿材料" +
                "\\I believe I can fly\\R. Kelly - I Believe I Can Fly_高清.mp4");
        //获取分块文件并排序
        File[] listFiles = chunkFile.listFiles();
        List<File> files = Arrays.asList(listFiles);
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // o1-o2升序，o2-o1降序
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //读取文件先要存储到缓存区
        byte[] bytes = new byte[1024];
        //创建合并文件
        RandomAccessFile rafWriteChunk = new RandomAccessFile(mergeFile,"rw");
        for (File file : files) {
            //读取分块文件的流
            RandomAccessFile rafRead = new RandomAccessFile(file,"rw");
            int length = -1;
            while((length=rafRead.read(bytes))!=-1){
                rafWriteChunk.write(bytes,0,length);
            }
            rafRead.close();
        }
        rafWriteChunk.close();
        //对合并的文件进行校验
        String md5_1 = DigestUtils.md5Hex(new FileInputStream(mergeFile));
        String md5_2 = DigestUtils.md5Hex(new FileInputStream(sourceFile));
        if (md5_1.equals(md5_2)){
            System.out.println("合并成功");
        }else{
            System.out.println("合并失败");
        }
    }
}
