package com.ruwei.core;

import com.ruwei.ai.AICodeGeneratorService;
import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI 代码生成的门面类
 */
@Service
public class AICodeGeneratorFacade {


    @Resource
    private AICodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage 用户prompt
     * @param codeGenType 生成类型
     * @return 生成好的文件
     * @throws Exception
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType)  {
        ThrowUtils.throwIf(codeGenType==null, ErrorCode.PARAMS_ERROR,"生成类型不能为空");

        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage="不支持的生成类型："+codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    /**
     * 生成多文件模式的代码生成器
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);

        return CodeFileServer.saveMultiFileCodeResult(multiFileCodeResult);

    }

    /**
     * 生成html的文件的代码生成器
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);

        return CodeFileServer.saveHtmlCodeResult(htmlCodeResult);

    }
}
