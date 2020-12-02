package com.xd.springbootshardingreadwrite.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import groovy.transform.EqualsAndHashCode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("test_user")
public class User extends Model<User> {


    /** id */
    private Long id;
    /** 姓名 */
    private String name;
    /** 手机号 */
    private String phone;
    /** 职称职别 */
    private String title;
    /** 邮箱 */
    private String email;
    /** 性别 */
    private String gender;
    /** 出生时间 */
    private Date dateOfBirth;
    /** 1:已删除,0:未删除 */
    private Integer deleted;
    /** 创建时间 */
    private Date sysCreateTime;
    /** 创建人 */
    private String sysCreateUser;
    /** 更新时间 */
    private Date sysUpdateTime;
    /** 更新人 */
    private String sysUpdateUser;
    /** 版本号 */
    private Long recordVersion;
}
