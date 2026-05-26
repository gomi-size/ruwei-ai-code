package com.ruwei.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruwei.core.AICodeGeneratorFacade;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.exception.ThrowUtils;
import com.ruwei.model.dto.app.AppQueryRequest;
import com.ruwei.model.entity.App;
import com.ruwei.mapper.AppMapper;
import com.ruwei.model.entity.User;
import com.ruwei.model.enums.CodeGenTypeEnum;
import com.ruwei.model.vo.AppVO;
import com.ruwei.model.vo.UserVO;
import com.ruwei.service.AppService;
import com.ruwei.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;




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

        //5.调用AI生成代码

        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, enumByValue, appId);
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


}
