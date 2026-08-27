package com.ruwei.ruweicodeapp.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.enums.CodeGenTypeEnum;

import java.io.File;

import static com.ruwei.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * 抽象代码文件保存器-模板方法
 */
public abstract class CodeFileSaveTemplate<T> {

    //文件保存的根目录
    private static final String FILE_SAVE_ROOT_DIR= CODE_OUTPUT_ROOT_DIR;


    /**
     * 模板方法，保存代码的标准流程
     * @param result
     * @return
     */
    public final File saveCode(T result,Long appId){
        //1.验证输入
        validateInput(result);
        //2.构建唯一路径
        String uniqueDir = buildUniqueDir(appId);
        //3.保存文件（子类实现）
        saveFiles(result,uniqueDir);
        //4.返回文件目录
        return FileUtil.file(uniqueDir);
    }


    /**
     * 保存单个文件
     * 其实就是生成文件然后将content写入到filePath文件中
     * @param dirPath 整个路径
     * @param filename 文件名称
     * @param content 文件内容
     */
    public final void writeFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)){
            String filePath = dirPath + "/" + filename;
            FileUtil.writeUtf8String(content,filePath);
        }
    }

    /**
     * 构建文件的唯一路径:tmp/code_output/bizType_雪花算法ID
     * 其实就是生成文件夹
     * @param appId appID
     * @return
     */
    private  String buildUniqueDir(Long appId ){
        ThrowUtils.throwIf(appId==null,ErrorCode.PARAMS_ERROR,"appId不能为空");
        String value = getCodeType().getValue();
        String uniqueDirName=FILE_SAVE_ROOT_DIR+"/"+value+"_"+ appId;
        FileUtil.mkdir(uniqueDirName);
        return uniqueDirName;
    }

    /**
     * 获取代码类型
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件
     * @param result
     * @param uniqueDir
     */
    protected abstract void saveFiles(T result, String uniqueDir) ;

    /**
     * 校验文件
     * @param result
     */
    protected  void validateInput(T result){
        ThrowUtils.throwIf(result==null, ErrorCode.NOT_FOUND_ERROR,"请求参数不能空");
    };
}
