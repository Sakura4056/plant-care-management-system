package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.plant.backend.dto.PhotoDTO;
import com.plant.backend.entity.PlantPhoto;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.mysql.PlantPhotoMapper;
import com.plant.backend.service.PhotoService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl extends ServiceImpl<PlantPhotoMapper, PlantPhoto> implements PhotoService {

    @Value("${app.file-upload-path}")
    private String uploadPath;

    private final com.plant.backend.mapper.mysql.OfficialPlantMapper officialPlantMapper;
    // 根据项目结构，LocalPlantMapper应该在sqlite包中
    private final com.plant.backend.mapper.sqlite.LocalPlantMapper localPlantMapper;

    @Transactional(rollbackFor = Exception.class)
    public PlantPhoto upload(PhotoDTO.UploadRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件为空");
        }

        // 检查是否分片
        boolean isChunked = request.getChunks() != null && request.getChunks() > 1;

        File finalFile = null;

        try {
            if (isChunked) {
                // 处理分片
                finalFile = handleChunk(file, request);
                // 如果为空，表示尚未接收所有分片
                if (finalFile == null) {
                    // 返回状态指示 "继续" 或 "部分上传"
                    // 需求暗示返回照片信息。如果不完整，可能返回 null 或特殊状态？
                    // 通常标准 API 返回 200 OK 用于分片确认。
                    // 我们将返回一个虚拟 PlantPhoto 或 null，Controller 会处理它。
                    // 但这里我们需要返回 PlantPhoto。让我们返回 null 以发出未完成信号。
                    return null;
                }
            } else {
                // 单个文件
                String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + request.getFileName();
                File dest = new File(uploadPath, fileName);
                // Convert to absolute path to avoid transferTo relative path issue (resolving
                // to temp dir)
                if (!dest.isAbsolute()) {
                    dest = dest.getAbsoluteFile();
                }
                FileUtils.forceMkdirParent(dest);
                file.transferTo(dest);
                finalFile = dest;
            }

            // 文件已就绪 (合并或单文件)
            // 提取 EXIF
            LocalDateTime captureTime = extractCaptureTime(finalFile);

            // 保存数据库
            PlantPhoto photo = new PlantPhoto();
            photo.setUserId(request.getUserId());
            photo.setPlantId(request.getPlantId());
            photo.setPlantSource(request.getPlantSource());
            photo.setIsPublic(request.getIsPublic() == null ? 0 : request.getIsPublic());
            photo.setRemarks(request.getRemarks());
            photo.setFilePath(finalFile.getAbsolutePath());
            // URL 映射: 假设静态资源映射 /uploads/** -> data/uploads/
            // 我们需要配置 WebMvc 进行此映射。
            photo.setUrl("/uploads/" + finalFile.getName());
            photo.setCaptureTime(captureTime != null ? captureTime : LocalDateTime.now());

            save(photo);
            return photo;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "文件上传失败");
        }
    }

    private File handleChunk(MultipartFile file, PhotoDTO.UploadRequest request) throws IOException {
        // 临时文件夹: uploadPath/temp/{userId}/{fileName}/
        String tempDirName = request.getUserId() + "_" + request.getFileName();
        File tempDir = new File(uploadPath + "/temp/" + tempDirName);
        FileUtils.forceMkdir(tempDir);

        // 保存分片: 索引
        File chunkFile = new File(tempDir, String.valueOf(request.getChunk()));
        if (!chunkFile.isAbsolute()) {
            chunkFile = chunkFile.getAbsoluteFile();
        }
        file.transferTo(chunkFile);

        // 检查是否所有分片已存在
        // 非常基础的检查: 列出文件数 == chunks
        // 在实际应用中需要锁，但在此范围内可以接受
        if (tempDir.listFiles().length == request.getChunks()) {
            return mergeChunks(tempDir, request.getFileName());
        }

        return null;
    }

    private File mergeChunks(File tempDir, String originalFileName) throws IOException {
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFileName;
        File destFile = new File(uploadPath, fileName);

        // 按索引排序分片
        // 假设名称为 0, 1, 2...
        // 合并
        try (var outputStream = new java.io.FileOutputStream(destFile, true)) {
            for (int i = 0; i < tempDir.listFiles().length; i++) {
                File chunk = new File(tempDir, String.valueOf(i));
                FileUtils.copyFile(chunk, outputStream);
            }
        }

        // 清理临时文件
            FileUtils.deleteDirectory(tempDir);

            return destFile;
        }

        private LocalDateTime extractCaptureTime(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                }
            }
        } catch (Exception e) {
            log.warn("从{}中提取EXIF信息失败", file.getName());
        }
        return null;
    }

    @Override
    public Page<PlantPhoto> queryPhotos(PhotoDTO.Query query, Long currentUserId, String currentRole) {
        Page<PlantPhoto> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PlantPhoto> wrapper = new LambdaQueryWrapper<>();

        // 访问控制
        if (!"ADMIN".equals(currentRole)) {
            // 用户只能查看自己的
            if (query.getUserId() != null && !query.getUserId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            if (query.getUserId() == null) {
                query.setUserId(currentUserId);
            }
        }

        if (query.getUserId() != null)
            wrapper.eq(PlantPhoto::getUserId, query.getUserId());
        if (query.getPlantId() != null)
            wrapper.eq(PlantPhoto::getPlantId, query.getPlantId());
        if (query.getPlantSource() != null)
            wrapper.eq(PlantPhoto::getPlantSource, query.getPlantSource());
        if (query.getIsPublic() != null)
            wrapper.eq(PlantPhoto::getIsPublic, query.getIsPublic());

        wrapper.orderByDesc(PlantPhoto::getCaptureTime);

        Page<PlantPhoto> result = page(page, wrapper);

        // Populate Plant Name
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (PlantPhoto photo : result.getRecords()) {
                if ("OFFICIAL".equals(photo.getPlantSource())) {
                    com.plant.backend.entity.OfficialPlant p = officialPlantMapper.selectById(photo.getPlantId());
                    if (p != null) {
                        photo.setPlantName(p.getName());
                    }
                } else {
                    com.plant.backend.entity.LocalPlant p = localPlantMapper.selectById(photo.getPlantId());
                    if (p != null) {
                        photo.setPlantName(p.getName());
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlantPhoto updatePhoto(PhotoDTO.UpdateRequest request, Long userId) {
        PlantPhoto photo = getById(request.getId());
        if (photo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "照片不存在");
        }
        if (!photo.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (request.getRemarks() != null) {
            photo.setRemarks(request.getRemarks());
        }
        if (request.getIsPublic() != null) {
            photo.setIsPublic(request.getIsPublic());
        }

        updateById(photo);
        return photo;
    }

    @Override
    public File getPhotoFile(String filename) {
        // Prevent directory traversal
        if (filename.contains(".."))
            return null;
        return new File(uploadPath, filename);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long id, Long userId) {
        PlantPhoto photo = getById(id);
        if (photo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "照片不存在");
        }
        // 权限检查
        if (!photo.getUserId().equals(userId)) {
            // 如果是管理员是否允许？暂时严格检查
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 删除文件
        if (photo.getFilePath() != null) {
            File file = new File(photo.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        // 删除数据库记录
        removeById(id);
    }
}
