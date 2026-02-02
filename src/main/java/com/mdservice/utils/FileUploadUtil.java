package com.mdservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
    public String getBaseUploadPath() {
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
        if(StringUtils.isEmpty(fileName)){
            return false;
        }
        fileName = fileName.substring(1);
        try{
            File classpathDir = new File(ResourceUtils.getURL("classpath:").getPath());
            String filePath = classpathDir.getAbsolutePath() + File.separator + fileName;
            File targetFile = new File(filePath);
            if(targetFile.exists() && targetFile.isFile()){
                boolean deleted = targetFile.delete();
                deleteFileFromResourceUpload(fileName);
                log.info("删除图片成功！");
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
    /**
     * 判断指定路径的图片是否存在
     * @param imagePath 图片路径（格式：/upload/test.png）
     * @return true-存在，false-不存在/参数非法/异常
     */
    public boolean isImageExists(String imagePath) {
        // 1. 参数合法性校验
        if (StringUtils.isEmpty(imagePath) || !imagePath.startsWith("/upload/")) {
            log.warn("图片路径格式非法：{}，必须以/upload/开头，识别为新图片，已上传！", imagePath);
            return false;
        }

        // 2. 提取文件名（如：/upload/test.png -> test.png）
        String fileName = imagePath.substring("/upload/".length());
        if (StringUtils.isEmpty(fileName)) {
            log.warn("图片路径中未提取到文件名：{}", imagePath);
            return false;
        }

        try {
            // 3. 区分环境判断文件是否存在
            if ("dev".equals(activeProfile)) {
                // 开发环境：检查源码目录 + target目录
                return isExistsInDevEnv(fileName);
            } else {
                // 生产环境：检查生产路径
                return isExistsInProdEnv(fileName);
            }
        } catch (Exception e) {
            log.error("判断图片是否存在时发生异常，路径：{}，异常信息：{}", imagePath, e.getMessage());
            return false;
        }
    }

    /**
     * 开发环境：检查源码目录和target目录是否存在该文件
     */
    private boolean isExistsInDevEnv(String fileName) throws FileNotFoundException {
        // 3.1 检查源码目录（src/main/resources/upload/）
        File devFile = new File(devUploadPath + fileName);
        if (devFile.exists() && devFile.isFile()) {
            log.info("开发环境-源码目录存在该图片：{}", devFile.getAbsolutePath());
            return true;
        }

        // 3.2 检查target目录（classpath:/upload/）
        File classpathDir = new File(ResourceUtils.getURL("classpath:").getPath());
        File targetFile = new File(classpathDir.getAbsolutePath() + File.separator + "upload" + File.separator + fileName);
        if (targetFile.exists() && targetFile.isFile()) {
            log.info("开发环境-target目录存在该图片：{}", targetFile.getAbsolutePath());
            return true;
        }

        log.info("开发环境未找到图片：{}，源码目录：{}，target目录：{}", fileName, devFile.getAbsolutePath(), targetFile.getAbsolutePath());
        return false;
    }

    /**
     * 生产环境：检查生产路径是否存在该文件
     */
    private boolean isExistsInProdEnv(String fileName) {
        File prodFile = new File(prodUploadPath + fileName);
        boolean exists = prodFile.exists() && prodFile.isFile();
        if (exists) {
            log.info("生产环境存在该图片：{}", prodFile.getAbsolutePath());
        } else {
            log.info("生产环境未找到图片：{}", prodFile.getAbsolutePath());
        }
        return exists;
    }
}
