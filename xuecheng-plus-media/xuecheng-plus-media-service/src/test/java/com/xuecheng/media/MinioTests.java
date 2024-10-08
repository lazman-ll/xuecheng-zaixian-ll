package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioTests {


    // Create a minioClient with the MinIO server playground, its access key and secret key.
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();
    @Test
    public void testUpload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }


        // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
        // 'asiatrip'.
        UploadObjectArgs testbucket = UploadObjectArgs.builder()
                .bucket("testbucket")  //桶名
                .object("test/copyMovie.avi") //文件名 放在子目录下
                .filename("D:\\MyFile\\BUCT\\copyMovie.avi") //文件路径
                .contentType(mimeType)
                .build();
        minioClient.uploadObject(testbucket);
    }

    @Test
    public void testDelete() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("testbucket")
                        .object("copyMovie.avi")
                        .build());
    }

    @Test
    public void testDownload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectArgs testbucket = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("test/copyMovie.avi")
                .build();
        FilterInputStream filterInputStream = minioClient.getObject(testbucket);
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\MyFile\\BUCT\\minoMovie.avi");
        IOUtils.copy(filterInputStream,fileOutputStream);
        //校验文件的完整性
        String source_md5 = DigestUtils.md5Hex(filterInputStream);
        String target_md5 = DigestUtils.md5Hex(new FileInputStream("D:\\MyFile\\BUCT\\minoMovie.avi"));
        if (source_md5.equals(target_md5)){
            System.out.println("下载成功");
        }else{
            System.out.println("下载失败");
        }
    }
}