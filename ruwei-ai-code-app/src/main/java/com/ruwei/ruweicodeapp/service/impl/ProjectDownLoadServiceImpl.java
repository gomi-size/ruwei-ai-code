package com.ruwei.ruweicodeapp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.ruweicodeapp.service.ProjectDownLoadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;
@Slf4j
@Service
public class ProjectDownLoadServiceImpl implements ProjectDownLoadService {


    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    /**
     * 下载为压缩包
     * @param projectPath
     * @param downloadFileName
     * @param response
     * @return
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {

        //基础校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR,"项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR,"下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(),ErrorCode.PARAMS_ERROR,"项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(),ErrorCode.PARAMS_ERROR,"项目路径部署一个目录");
        log.info("项目打包下载项目：{}->{}.zip",projectPath, downloadFileName );

        //设置Http响应头
        response.setStatus(HttpServletResponse.SC_OK);  // 1. 设置响应状态码为 200（成功）
        response.setContentType("application/zip");     // 2. 设置响应内容类型为 ZIP 格式
        response.addHeader("Content-Disposition",       // 3. 设置 Content-Disposition 头，控制文件下载行为
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));

        //定义文件过滤器
        FileFilter filter= file->isPathAllowed(projectDir.toPath(),file.toPath());

        //压缩
        try {
            //使用Hutool的ZipUtil直接跳过过滤后的目录压缩到响应输出流
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8,false,filter,projectDir);
            log.info("打包下载项目成功：{}->{}.zip",projectPath, downloadFileName );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"打包下载项目失败");
        }
    }

    /**
     * 检查路径是否允许包含在压缩包中
     *
     * @param projectRoot 项目根目录
     * @param fullPath    完整路径
     * @return 是否允许
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        // 获取相对路径
        Path relativePath = projectRoot.relativize(fullPath);
        // 检查路径中的每一部分
        for (Path part : relativePath) {
            String partName = part.toString();
            // 检查是否在忽略名称列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            // 检查文件扩展名
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }
}
