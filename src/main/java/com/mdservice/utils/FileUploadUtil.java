package com.mdservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class FileUploadUtil {

    // 开发环境路径
    @Value("${file.upload.dev-path}")
    private String devUploadPath;

    // 生产环境路径
    @Value("${file.upload.prod-path}")
    private String prodUploadPath;

    // 环境标识（dev/prod，可通过spring.profiles.active获取）
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 上传文件到指定目录
     * @param file 前端传入的MultipartFile
     * @return 相对路径（用于存储到数据库）
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 1. 生成唯一文件名（避免重复）
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;


        // 3. 确定最终存储路径
        String basePath = getBaseUploadPath();
        File uploadDir = new File(basePath + File.separator);
        if (!uploadDir.exists()) {
            // 递归创建目录
            boolean mkdirs = uploadDir.mkdirs();
            if (!mkdirs) {
                throw new IOException("创建上传目录失败：" + uploadDir.getAbsolutePath());
            }
        }

        // 4. 目标文件
        File targetFile = new File(uploadDir, fileName);

        // 5. 复制文件（MultipartFile -> 本地文件）
        file.transferTo(targetFile.toPath());

        // 6. 开发环境：同步复制到target/classes/upload（编译目录）
        if ("dev".equals(activeProfile)) {
            copyToTargetClasses(uploadDir, fileName);
        }
        log.info("上传的文件目录：{}", targetFile.getAbsolutePath());
        // 返回前端和数据库使用的相对路径（与静态资源映射一致）
        String relativePath = "/upload/" + fileName;
        return relativePath;
    }

    /**
     * 获取基础上传路径（区分环境）
     */
    private String getBaseUploadPath() {
        if ("prod".equals(activeProfile)) {
            log.info("生产环境");
            return prodUploadPath;
        } else {
            // 开发环境返回源码目录
            log.info("开发环境");
            return devUploadPath;
        }
    }

    /**
     * 开发环境：复制文件到target/classes/upload（保证classpath下有文件）
     */
    private void copyToTargetClasses(File sourceDir, String fileName) throws IOException {
        // 获取target/classes/upload的绝对路径
        String targetClasspathUploadPath;
        try {
            // ResourceUtils.getURL("classpath:") -> file:/xxx/target/classes/
            File classpathDir = new File(ResourceUtils.getURL("classpath:").getPath());
            targetClasspathUploadPath = classpathDir.getAbsolutePath() + File.separator + "upload";
        } catch (FileNotFoundException e) {
            log.info("获取classpath路径失败: {}", e);
            throw new IOException("获取classpath路径失败", e);
        }

        // 目标目录：target/classes/upload/2025/12/18
        File targetDir = new File(targetClasspathUploadPath + File.separator);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // 源文件路径
        Path sourcePath = Paths.get(sourceDir.getAbsolutePath(), fileName);
        // 目标文件路径
        Path targetPath = Paths.get(targetDir.getAbsolutePath(), fileName);

        // 复制文件（覆盖已存在的文件）
        Files.copy(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    public Boolean deleteFile(String fileName){
        fileName = fileName.substring(1);
        try{
            File classpathDir = new File(ResourceUtils.getURL("classpath:").getPath());
            String filePath = classpathDir.getAbsolutePath() + File.separator + fileName;
            File targetFile = new File(filePath);
            if(targetFile.exists() && targetFile.isFile()){
                boolean deleted = targetFile.delete();
                deleteFileFromResourceUpload(fileName);
                return deleted;
            }else{
                log.info("文件不存在！");
                return false;
            }
        }catch(FileNotFoundException e){
            log.info("获取classpath路径失败：{}", e.getMessage());
            return false;
        }catch(Exception e){
            log.info("删除文件失败：{}", e.getMessage());
            return false;
        }
    }
    public void deleteFileFromResourceUpload(String fileName){
        // 源码目录的upload路径（相对项目根目录）
        String sourceUploadPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + fileName;
        File sourceFile = new File(sourceUploadPath);
        if (sourceFile.exists() && sourceFile.isFile()) {
            sourceFile.delete();
        }
    }
}
