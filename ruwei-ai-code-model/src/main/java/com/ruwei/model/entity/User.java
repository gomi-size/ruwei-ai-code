package com.ruwei.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.KeyGenerator;

/**
 * 用户
 *
 * @TableName user
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 账号
     */
    @Column(value = "userAccount")
    private String userAccount;

    /**
     * 密码
     */
    @Column(value = "userPassword")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Column(value = "userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column(value = "userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column(value = "userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Column(value = "userRole")
    private String userRole;

    /**
     * 编辑时间
     */
    @Column(value = "editTime")
    private Date editTime;

    /**
     * 创建时间
     */
    @Column(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;


}