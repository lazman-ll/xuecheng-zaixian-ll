package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private MediaProcessMapper mediaProcessMapper;
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
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
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
        if(StringUtils.isEmpty(objectName)){
            //若不传objectName，则使用年月日作为存储路径，md5值为文件名
            objectName = folder + fileMd5 + extension;
        }
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
        //记录待处理任务
        addWaitingTask(mediaFiles);

        log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        return mediaFiles;
    }


    /**
     * @author 闲人指路
     * @description 添加待处理任务
     * @dateTime 16:08 2024/10/11
     * @param mediaFiles 媒资文件信息
     * @return void
    */
    private void addWaitingTask(MediaFiles mediaFiles){
        //1.获取文件名
        String filename = mediaFiles.getFilename();
        //2.获取文件后缀
        String extension = filename.substring(filename.lastIndexOf("."));
        //3.根据后缀获取mime-type
        String mimeType = getMimeType(extension);
        //4.判断是否是要被处理的类型
        if(!mimeType.equals("video/x-msvideo")){
            //不是，直接返回
            return;
        }
        //4.新建一个MediaProcess对象
        MediaProcess mediaProcess = new MediaProcess();
        BeanUtils.copyProperties(mediaFiles,mediaProcess);
        //设置状态为待处理
        mediaProcess.setStatus("1");
        //设置上传时间为当前时间
        mediaProcess.setCreateDate(LocalDateTime.now());
        //设置url为null
        mediaProcess.setUrl(null);
        //5.将MediaProcess对象插入到数据库
        mediaProcessMapper.insert(mediaProcess);
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

    @Override
    public boolean addMediaFiles2MinIo(
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
        if(extension == null){
            extension = " ";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //1.先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles==null){
            //数据库中不存在，直接返回
            return RestResponse.success(false);
        }
        //2.如果数据库存在，再查询minio
        return checkMinioFile(mediaFiles.getFilePath(), mediaFiles.getBucket());
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //1.拼接分块的存储路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5)+chunkIndex;
        //2.查询minio
        return checkMinioFile(chunkFileFolderPath, bucket_video);
    }

    /**
     * @author 闲人指路
     * @description 查询文件状态
     * @dateTime 18:43 2024/10/9
     * @param chunkFileFolderPath 文件在minio中的路径
     * @param bucket 文件的存储桶
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
    */
    @NotNull
    private RestResponse<Boolean> checkMinioFile(String chunkFileFolderPath, String bucket) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(chunkFileFolderPath)
                .build();
        try {
            //获取文件信息
            FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
            if (filterInputStream != null) {
                //文件已经存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            //抛出异常代表文件不存在
            e.printStackTrace();
            return RestResponse.success(false);
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * @author 闲人指路
     * @description 得到分块文件的目录
     * @dateTime 18:35 2024/10/9
     * @return 分块文件的路径
    */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadChunk(String localFilePath, String fileMd5, int chunk) {
        //1.获取mimeType
        String mimeType = getMimeType(null);
        //2.获取文件存储在minio的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunk;
        //3.上传文件到minio
        boolean isSuccess = addMediaFiles2MinIo(localFilePath, mimeType, bucket_video, chunkFileFolderPath);
        if (isSuccess){
            //上传成功
            return RestResponse.success(true);
        }else {
            //上传失败
            return RestResponse.validfail(false,"上传分块文件失败");
        }
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //1.找到分块文件调用minio的SDK进行合并
        List<ComposeSource> sources =new ArrayList<>();
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        for (int i=0; i<chunkTotal ;i++) {

            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucket_video)
                    .object(chunkFileFolderPath + i)
                    .build();
            sources.add(composeSource);
        }

        //获取源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //获取扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String objectName = getFilePathByMd5(fileMd5, extension);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sources)
                .build();
        //报错: size 1048576 must be greater than 5242880
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错，bucket:{}，objectName:{},错误信息:{}",bucket_video,objectName,e.getMessage());
            return RestResponse.validfail(false,"合并文件出错");
        }
        //2.进行文件检验
        //先下载文件
        File fileNew = downloadFileFromMinIO(bucket_video, objectName);
        //设置文件的大小
        uploadFileParamsDto.setFileSize(fileNew.length());

        try (FileInputStream fis = new FileInputStream(fileNew)){
            //计算其Md5
            String md5HexNew = DigestUtils.md5Hex(fis);
            //比较md5值
            if(!md5HexNew.equals(fileMd5)){
                log.error("校验合并文件md5值不一致，原始文件:{}，合并文件:{},错误信息:{}",fileMd5,md5HexNew);
                return RestResponse.validfail(false,"文件校验失败");
            }
        }catch (Exception e){
            log.error("校验文件出错，bucket:{}，objectName:{},错误信息:{}",bucket_video,objectName,e.getMessage());
            return RestResponse.validfail(false,"文件校验失败");
        }
        //3.将文件信息入库
        //用代理对像调用，保证事物
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(fileMd5, companyId, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles == null){
            return RestResponse.validfail(false,"文件入库失败");
        }
        //4.清理分块文件
        clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }



    /**
     * @author 闲人指路
     * @description 得到合并后的文件的地址
     * @dateTime 20:35 2024/10/9
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return java.lang.String
    */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


    @Override
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @author 闲人指路
     * @description 清除分块文件
     * @dateTime 20:58 2024/10/9
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     * @return void
    */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal) {

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清理分块文件失败,objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清理分块文件失败,chunkFileFolderPath:{}", chunkFileFolderPath, e);
        }
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        // 从数据库查询媒资文件信息
        return mediaFilesMapper.selectById(mediaId);
    }
}
