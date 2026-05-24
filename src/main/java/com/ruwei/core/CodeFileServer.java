package com.ruwei.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import com.ruwei.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 文件保存器
 */
public class CodeFileServer {

    //文件保存的根目录
    private static final String FILE_SAVE_ROOT_DIR= System.getProperty("user.dir")+"/tmp/code_output";

    /**
     * 保存文件HTML网页代码
     * @param htmlCodeResult
     * @return
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        //创建的是各种的文件夹
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        //这个就是将实体内容写入
        writeFile(baseDirPath,"index.html",htmlCodeResult.getHtmlCode());

        return new File(baseDirPath);
    }

    /**
     * 保存 MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        //创建的是各种的文件夹
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        //这个就是将实体内容写入
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
        writeFile(baseDirPath, "style.css", result.getCssCode());
        writeFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建文件的唯一路径:tmp/code_output/bizType_雪花算法ID
     * 其实就是生成文件夹
     * @param bizType 代码生成类型
     * @return
     */
    private static String buildUniqueDir(String bizType){
        String uniqueDirName=FILE_SAVE_ROOT_DIR+"/"+bizType+"_"+ IdUtil.getSnowflakeNextIdStr();
        FileUtil.mkdir(uniqueDirName);
        return uniqueDirName;
    }

    /**
     * 保存单个文件
     * 其实就是生成文件然后将content写入到filePath文件中
     * @param dirPath 整个路径
     * @param filename 文件名称
     * @param content 文件内容
     */
    private static void writeFile(String dirPath, String filename, String content) {

        String filePath = dirPath + "/" + filename;

        FileUtil.writeUtf8String(content,filePath);

    }



}
