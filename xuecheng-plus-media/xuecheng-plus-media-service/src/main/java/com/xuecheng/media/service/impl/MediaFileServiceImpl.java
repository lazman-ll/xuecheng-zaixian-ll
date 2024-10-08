package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MediaFileService currentProxy;
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //上传文件到minio
        //1.先得到扩展名
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        //2.根据扩展名取出mimeType
        String mimeType = getMimeType(extension);
        //3.获取文件默认存储目录路径 年/月/日（objectName）
        //3.1获取年月日
        String folder = getDefaultFolderPath();
        //3.2获取md5值作为文件名
        String fileMd5 = getFileMd5(new File(localFilePath));
        if(StringUtils.isEmpty(fileMd5)){
            XueChengPlusException.cast("获取文件md5值失败");
        }
        //3.3拼接文件路径
        String objectName = folder + fileMd5 + extension;
        //3.上传文件到minio
        boolean result = addMediaFiles2MinIo(localFilePath, mimeType, bucket_mediafiles, objectName);
        if(!result){
            XueChengPlusException.cast("上传文件失败");
        }

        //将文件信息保存到数据库
        //1.保存文件到数据库
        //1.1获取当前类的代理对象
//        MediaFileService proxy = (MediaFileService) AopContext.currentProxy();
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(fileMd5, companyId, uploadFileParamsDto, bucket_mediafiles, objectName);
        //3.准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;

    }

    /**
     * 将文件信息保存到数据库
     * @param fileMd5
     * @param companyId
     * @param uploadFileParamsDto
     * @param bucket
     * @param objectName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MediaFiles addMediaFilesToDb(
            String fileMd5, Long companyId, UploadFileParamsDto uploadFileParamsDto,
            String bucket, String objectName) {
        //1.根据id查询文件，判断是否已经保存
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            return mediaFiles;
        }
        //2.文件不存在保存到数据库
        //3.拷贝信息
        mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
        //4.设置信息
        mediaFiles.setId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setBucket(bucket);
        mediaFiles.setFilePath(objectName);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setUrl("/"+bucket+"/"+objectName);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setStatus("1");
        mediaFiles.setAuditStatus("002003");
        //将文件信息存储到数据库
        int insert = mediaFilesMapper.insert(mediaFiles);
        if(insert<=0){
            log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
            XueChengPlusException.cast("保存文件信息到数据库失败");
            return null;
        }
        log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        return mediaFiles;
    }

    /**
     * 获取文件的md5
     * @param file
     * @return
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件默认存储目录路径 年/月/日
     * @return
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }

    /**
     * 上传文件到minio
     * @param localFilePath
     * @param mimeType
     * @param bucket
     * @param objectName
     * @return
     */
    private boolean addMediaFiles2MinIo(
            String localFilePath, String mimeType, String bucket, String objectName
    ){
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    //桶名
                    .bucket(bucket)
                    //文件名 放在子目录下
                    .object(objectName)
                    //文件路径
                    .filename(localFilePath)
                    //文件类型
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件成功,bucket:{},objectName:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件失败,bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
            return false;
        }
    }

    /**
     * 根据扩展名取出mimeType
     * @param extension
     * @return
     */
    private String getMimeType(String extension) {
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
}
