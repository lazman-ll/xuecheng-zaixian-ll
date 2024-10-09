package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    /**
     * 媒资列表查询接口
     * @param pageParams
     * @param queryMediaParamsDto
     * @return
     */
    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);


    }


    /**
     * 上传文件
     * @param filedata
     * @return
     */
    @ApiOperation("上传文件")
    @PostMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata")MultipartFile filedata) throws IOException {
        //1.设置文件信息
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        //1.1文件大小
        uploadFileParamsDto.setFileSize(filedata.getSize());
        //1.2文件名称
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        //1.3文件类型
        uploadFileParamsDto.setFileType("001001");
        //2.创建临时文件，将文件临时存储到本地
        File tempFile = File.createTempFile("minio", "temp");
        filedata.transferTo(tempFile);
        //3.临时存储在本地的该文件路径
        String localFilePath = tempFile.getAbsolutePath();

        //4.TODO 获取companyId
        Long companyId = 1232141425L;
        //5.调用service上传文件
        return mediaFileService.uploadFile(companyId,uploadFileParamsDto,localFilePath);
    }

}
