package com.ruwei.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ruwei.model.dto.user.UserQueryRequest;
import com.ruwei.model.entity.User;
import com.ruwei.model.vo.LoginUserVO;
import com.ruwei.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


/**
 * @author MECHREVO
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-05-23 11:41:00
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    String getEncryptPassword(String defaultPassword);
}
