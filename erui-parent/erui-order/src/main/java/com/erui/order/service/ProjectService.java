package com.erui.order.service;

import com.erui.order.entity.Project;
import com.erui.order.requestVo.ProjectListCondition;
import org.springframework.data.domain.Page;

/**
 * 订单业务类
 * Created by wangxiaodan on 2017/12/11.
 */
public interface ProjectService {
    /**
     * 根据id查询项目信息
     * @param id
     * @return
     */
    Project findById(Integer id);
    /**
     * 办理项目
     * @param project
     * @return
     */
    boolean updateProject(Project project);
    /**
     * 查看项目列表
     * @param condition
     * @return
     */
    Page<Project> findByPage(ProjectListCondition condition);
}
