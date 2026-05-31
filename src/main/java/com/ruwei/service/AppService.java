package com.ruwei.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ruwei.model.dto.app.AppQueryRequest;
import com.ruwei.model.entity.App;
import com.ruwei.model.entity.User;
import com.ruwei.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 */
public interface AppService extends IService<App> {

    /**
     * 异步生成截图服务并更新封面
     * @param appId
     * @param appDeployUrl
     */
    void generateAppScreenshotAsync(Long appId, String appDeployUrl);

    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 分页查询
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装类列表
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 通过对话生成代码应用
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);


    /**
     * 应用部署
     */
    String deployApp(Long appId,User loginUser);

}
