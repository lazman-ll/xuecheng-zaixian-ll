package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.BigFilesService;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


/**
 * 大文件的处理(本项目为视频)
 * @author 闲人指路
 */
@RestController
@Api(tags = "大文件的处理(本项目为视频)")
public class BigFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    /**
    * @author 闲人指路
    * @description 文件上传前检查文件
    * @dateTime 16:32 2024/10/9
    * @param fileMd5 文件Md5值
    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
    */
    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5){
        return mediaFileService.checkFile(fileMd5);
    }

    /**
    * @author 闲人指路
    * @description 分块文件上传前的检测
    * @dateTime 16:33 2024/10/9
    * @param fileMd5 文件Md5值
    * @param chunk 该分块文件的序号
    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
    */
    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk){
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    /**
    * @author 闲人指路
    * @description 上传分块文件
    * @dateTime 16:34 2024/10/9
    * @param file 前端传来的文件
    * @param fileMd5 文件Md5值
    * @param chunk 分块序号
    * @return com.xuecheng.base.model.RestResponse
    */
    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        //1.创建临时文件
        File tempFile = File.createTempFile("minio", "temp");
        //2.将文件转存到本地
        file.transferTo(tempFile);
        //3.获取文件的本地路径
        String localFilePath = tempFile.getAbsolutePath();
        //4.调用service上传分块文件
        return mediaFileService.uploadChunk(localFilePath, fileMd5, chunk);
    }

    /**
    * @author 闲人指路
    * @description 合并文件
    * @dateTime 16:35 2024/10/9
    * @param fileMd5 文件Md5值
    * @param fileName 文件名
    * @param chunkTotal 分块文件的个数
    * @return com.xuecheng.base.model.RestResponse
    */
    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        //TODO 获取companyId
        Long companyId = 1232141425L;
        //1.设置文件信息对象
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setTags("视频文件");
        return mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }
}
