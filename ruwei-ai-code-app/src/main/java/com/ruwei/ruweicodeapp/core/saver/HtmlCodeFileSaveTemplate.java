package com.ruwei.ruweicodeapp.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 抽象代码文件保存器-模板方法
 */
public  class HtmlCodeFileSaveTemplate extends CodeFileSaveTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String uniqueDir) {
        writeFile(uniqueDir,"index.html",result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);

        ThrowUtils.throwIf(StrUtil.isBlank(result.getHtmlCode()),ErrorCode.SYSTEM_ERROR,"HTML代码不能为空");
    }
}
