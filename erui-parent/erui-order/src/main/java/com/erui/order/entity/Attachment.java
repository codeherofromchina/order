package com.erui.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.Date;

/**
 * 附件信息
 */
@Entity
@Table(name = "attachment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "`group`")
    private String group;

    private String title;

    private String url;

    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "user_name")
    private String userName;

    // 前台显示的创建时间
    @Column(name = "front_date")
    private String frontDate;
    @Column(name = "delete_flag")
    @JsonIgnore
    private Boolean deleteFlag;
    @Column(name = "delete_time")
    @JsonIgnore
    private Date deleteTime;

    @Column(name = "create_time")
    @JsonIgnore
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getFrontDate() {
        return frontDate;
    }

    public void setFrontDate(String frontDate) {
        this.frontDate = frontDate;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}