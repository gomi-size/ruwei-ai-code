package com.ruwei.core;

import com.ruwei.ai.AICodeGeneratorService;
import com.ruwei.ai.AICodeGeneratorServiceFactory;
import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import com.ruwei.core.parser.CodeParserExecutor;
import com.ruwei.core.saver.CodeFileSaverExecutor;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.enums.CodeGenTypeEnum;
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
    private AICodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage 用户prompt
     * @param codeGenType 生成类型
     * @return 生成好的文件
     * @throws Exception
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType,Long appId)  {
        ThrowUtils.throwIf(codeGenType==null, ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        //根据appId获取相应的ai实例
        AICodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAICodeGeneratorService(appId);
        return switch (codeGenType) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);

                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);

                yield  CodeFileSaverExecutor.executeSaver(multiFileCodeResult,CodeGenTypeEnum.MULTI_FILE,appId);
            }
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
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType,Long appId)  {
        ThrowUtils.throwIf(codeGenType==null, ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        //根据appId获取相应的ai实例
        AICodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAICodeGeneratorService(appId);
        return switch (codeGenType) {
            case HTML -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield  processCodeStream(stringFlux, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
               yield  processCodeStream(stringFlux, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                String errorMessage="不支持的生成类型："+codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }
    /**
     * 通用的流式处理
     * @param codeStream 文件流
     * @param codeGenType 文件类型
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenType,Long appId) {
        //字符串拼接器,用户流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        //实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(()->{
            try {
                //流式返回完成返回后，保存代码
                String completeCode = codeBuilder.toString();
                //解析代码为对象
                Object executeParser = CodeParserExecutor.executeParser(completeCode, codeGenType);
                //保存代码到文件
                File file = CodeFileSaverExecutor.executeSaver(executeParser,codeGenType,appId);
                log.info("文件保存成功：目录为：{}",file.getAbsolutePath());
            } catch (Exception e) {
                log.error("文件创建失败{}",e.getMessage());
            }
        });
    }
}
