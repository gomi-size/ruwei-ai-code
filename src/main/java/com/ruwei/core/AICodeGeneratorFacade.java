package com.ruwei.core;

import com.ruwei.ai.AICodeGeneratorService;
import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.StreamingChatModel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成的门面类
 */
@Service
public class AICodeGeneratorFacade {


    private static final Logger log = LoggerFactory.getLogger(AICodeGeneratorFacade.class);
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
     * 统一入口：根据类型生成并保存代码（流式）
     * @param userMessage 用户prompt
     * @param codeGenType 生成类型
     * @return 生成好的文件
     * @throws Exception
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType)  {
        ThrowUtils.throwIf(codeGenType==null, ErrorCode.PARAMS_ERROR,"生成类型不能为空");

        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage="不支持的生成类型："+codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    /**
     * 生成多文件流式代码
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {

        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //字符串拼接器,用户流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        //实时收集代码片段
        return stringFlux.doOnNext(codeBuilder::append).doOnComplete(()->{
            try {
                //流式返回完成返回后，保存代码
                String codeBuilderString = codeBuilder.toString();
                //解析代码为对象
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(codeBuilderString);
                //保存代码到文件
                File file = CodeFileServer.saveMultiFileCodeResult(multiFileCodeResult);
                log.info("文件保存成功：目录为：{}",file.getAbsolutePath());
            } catch (Exception e) {
                log.error("文件创建失败{}",e.getMessage());
            }
        });
    }

    /**
     * 生成html流式代码
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        //字符串拼接器,用户流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        //实时收集代码片段
        return stringFlux.doOnNext(codeBuilder::append).doOnComplete(()->{
            try {
                //流式返回完成返回后，保存代码
                String codeBuilderString = codeBuilder.toString();
                //解析代码为对象
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(codeBuilderString);
                //保存代码到文件
                File file = CodeFileServer.saveHtmlCodeResult(htmlCodeResult);
                log.info("文件保存成功：目录为：{}",file.getAbsolutePath());
            } catch (Exception e) {
                log.error("文件创建失败{}",e.getMessage());
            }
        });
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
