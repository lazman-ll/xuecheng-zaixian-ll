package com.xuecheng.media.service;

import com.sun.org.apache.xpath.internal.operations.And;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.soap.Addressing;
import java.io.File;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     */
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * 上传文件
     * @param companyId 机构id
     * @param uploadFileParamsDto  文件信息
     * @param localFilePath 本地文件路径
     * @return
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

    /**
     * 将文件信息添加到数据库
     * @param fileMd5
     * @param companyId
     * @param uploadFileParamsDto
     * @param bucket
     * @param objectName
     * @return
     */
    MediaFiles addMediaFilesToDb(
            String fileMd5, Long companyId, UploadFileParamsDto uploadFileParamsDto,
            String bucket, String objectName);

   /**
    * @author 闲人指路
    * @description 检查文件是否存在
    * @dateTime 18:20 2024/10/9
    * @param fileMd5 文件的md5
    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
   */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @author 闲人指路
     * @description 检查分块文件是否存在
     * @dateTime 18:22 2024/10/9
     * @param fileMd5 文件的md5
     * @param chunkIndex 分块序号
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
    */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * @author 闲人指路
     * @description 上传分块文件
     * @dateTime 18:50 2024/10/9
     * @param localFilePath 文件的本地存储路径
     * @param fileMd5 文件的md5
     * @param chunk 分块序号
     * @return com.xuecheng.base.model.RestResponse
    */
    RestResponse uploadChunk(String localFilePath, String fileMd5, int chunk);

    /**
     * @author 闲人指路
     * @description 合并分块
     * @dateTime 20:25 2024/10/9
     * @param companyId companyId
     * @param fileMd5 文件md5
     * @param chunkTotal 分块总个数
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
    */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

    /**
     * @author 闲人指路
     * @description 从minio下载文件
     * @dateTime 20:41 2024/10/9
     * @param bucket 桶
     * @param objectName 对象名称
     * @return java.io.File
     */
    public File downloadFileFromMinIO(String bucket, String objectName);

    /**
     * 上传文件到minio
     * @param localFilePath
     * @param mimeType
     * @param bucket
     * @param objectName
     * @return
     */
     boolean addMediaFiles2MinIo(String localFilePath, String mimeType, String bucket, String objectName);

    /**
     * 获取已转码视频的url
     * @param mediaId
     * @return
     */
    MediaFiles getFileById(String mediaId);
}
