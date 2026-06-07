package com.ruwei.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruwei.ai.AiCodeGenTypeRoutingService;
import com.ruwei.ai.AiCodeGenTypeRoutingServiceFactory;
import com.ruwei.constant.AppConstant;
import com.ruwei.core.AICodeGeneratorFacade;
import com.ruwei.core.builder.VueProjectBuilder;
import com.ruwei.core.handler.StreamHandlerExecutor;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.dto.app.AppAddRequest;
import com.ruwei.model.dto.app.AppQueryRequest;
import com.ruwei.model.entity.App;
import com.ruwei.mapper.AppMapper;
import com.ruwei.model.entity.User;
import com.ruwei.model.enums.ChatHistoryMessageTypeEnum;
import com.ruwei.model.enums.CodeGenTypeEnum;
import com.ruwei.model.vo.AppVO;
import com.ruwei.model.vo.UserVO;
import com.ruwei.service.AppService;
import com.ruwei.service.ChatHistoryService;
import com.ruwei.service.ScreenshotService;
import com.ruwei.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;


    /**
     * 创建app应用
     * @param appAddRequest
     * @param request
     * @return
     */
    @Override
    public Long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());

        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));

        // 根据ai只能选择代码生成类型(多例模式)
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeType(initPrompt);
        app.setCodeGenType(codeGenTypeEnum.getValue());

        // 插入数据库
        boolean result = save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }




    /**
     * 生成应用
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {

        //1.参数校验
        ThrowUtils.throwIf(appId==null,ErrorCode.PARAMS_ERROR,"appId不能为空");
        ThrowUtils.throwIf(message==null,ErrorCode.PARAMS_ERROR,"消息不能不能为空");
        //2.查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        //3.权限校验，仅本人可以和自己对话
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"不是本人无权限访问应用");

        //4.获取应用的代码生成
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(enumByValue==null,ErrorCode.PARAMS_ERROR,"构造类型不存在");

        //5.在ai前先保存，先保存用户消息
        chatHistoryService.addChatMessage(appId,message, ChatHistoryMessageTypeEnum.USER.getValue(),loginUser.getId());

        //6.调用AI生成代码(流式，这里我们使用流式，不使用普通的)
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, enumByValue, appId);


        //7.收集AI响应的内容，并且完成后保存记录到对话中(这里进行了处理，可以分别处理原版的文件格式和Vue文件格式)
       return streamHandlerExecutor.doExecute(stringFlux,chatHistoryService,appId,loginUser, enumByValue);
    }

    /**
     * 部署应用
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {

        //1.校验参数
        ThrowUtils.throwIf(appId==null||appId<=0,ErrorCode.PARAMS_ERROR,"应用id错误");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"用户未登录");

        //2.查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"没有该应用");

        //3.校验权限，仅本人使用
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"无权限，部署该应用");

        //4.查询是否已有deployId（字母+数字）
        String deployKey = app.getDeployKey();
        if(StrUtil.isBlank(deployKey)){
            //如果没有就生成deployId
            deployKey= RandomUtil.randomString(6);
        }

        //5.获取代码生成类型，获取原始代码生成路径(应用访问目录)
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/" + sourceDirName;


        //6.检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists()||!sourceDir.isDirectory(),ErrorCode.SYSTEM_ERROR,
                "代码路径不存在，请先生成应用");
        //7.Vue 项目特殊处理构造
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if(enumByValue==CodeGenTypeEnum.VUE_PROJECT){
            //构建Vue项目，这里是同步执行
            boolean result = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR,"Vue项目构建失败，请重试");

            //检查dist目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(),ErrorCode.SYSTEM_ERROR,"Vue项目构建完成但未生成dist目录");
            //构建完成后，进行部署
            sourceDir=distDir;

        }


        //8.复制文件道部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;

        try {
            //第一个是要复制的文件的路径，第二个是目的文件
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用部署失败"+e.getMessage());
        }

        //9.更新数据库
        App upadeApp = new App();
        upadeApp.setId(appId);
        upadeApp.setDeployKey(deployKey);
        upadeApp.setDeployedTime(LocalDateTime.now());
        boolean updated = updateById(upadeApp);
        ThrowUtils.throwIf(!updated,ErrorCode.SYSTEM_ERROR,"更新应用部署失败");

        //10.返回可访问的URL地址
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        //11.异步异步生成截图并且更新应用封面
        generateAppScreenshotAsync(appId,appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成截图服务并更新封面
     * @param appId
     * @param appDeployUrl
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appDeployUrl) {
        //启用虚拟线程并执行
        Thread.startVirtualThread(()->{
                //调用截图服务生成截图并上传
                String screenshotUrl = screenshotService.generateAndUploadScreenshot(appDeployUrl);
                App app = new App();
                app.setId(appId);
                app.setCover(screenshotUrl);
                boolean updated = updateById(app);
                ThrowUtils.throwIf(!updated,ErrorCode.OPERATION_ERROR,"更新应用封面字段失败");
            });
    }






    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 分页查询
     * @param appQueryRequest 查询请求
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

}
