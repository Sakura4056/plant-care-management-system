package com.plant.backend.controller;

import com.plant.backend.dto.PhotoDTO;
import com.plant.backend.entity.PlantPhoto;
import com.plant.backend.service.PhotoService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/photo")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final JwtUtil jwtUtil;

    @PostMapping("/upload")
    public Result<PlantPhoto> upload(PhotoDTO.UploadRequest request, @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setUserId(userId);

        PlantPhoto photo = photoService.upload(request, file);
        if (photo == null) {
            // Null 意味着分片已处理但尚未完成
            // 返回成功并带有 null 数据以指示 "分片已接收"
            return Result.success();
        }
        return Result.success(photo);
    }

    @PostMapping("/update")
    public Result<PlantPhoto> update(
            @RequestBody @Validated PhotoDTO.UpdateRequest request,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(photoService.updatePhoto(request, userId));
    }

    @GetMapping("/query")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<PlantPhoto>> query(PhotoDTO.Query query,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        return Result.success(photoService.queryPhotos(query, userId, role));
    }

    @GetMapping("/view/{filename}")
    public void view(@PathVariable("filename") String filename, jakarta.servlet.http.HttpServletResponse response) {
        File file = photoService.getPhotoFile(filename);

        if (file == null || !file.exists()) {
            response.setStatus(404);
            return;
        }

        response.setContentType("image/jpeg"); // Naive content type
        try (FileInputStream fis = new FileInputStream(file);
                OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        photoService.deletePhoto(id, userId);
        return Result.success();
    }
}
