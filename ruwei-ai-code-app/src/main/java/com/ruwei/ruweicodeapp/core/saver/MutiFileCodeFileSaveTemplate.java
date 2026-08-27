package com.ruwei.ruweicodeapp.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 抽象代码文件保存器-模板方法
 */
public  class MutiFileCodeFileSaveTemplate extends CodeFileSaveTemplate<MultiFileCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String uniqueDir) {
        //这个就是将实体内容写入
        writeFile(uniqueDir, "index.html", result.getHtmlCode());
        writeFile(uniqueDir, "style.css", result.getCssCode());
        writeFile(uniqueDir, "script.js", result.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);

        ThrowUtils.throwIf(StrUtil.isBlank(result.getHtmlCode()),ErrorCode.SYSTEM_ERROR,"HTML代码不能为空");
    }
}
