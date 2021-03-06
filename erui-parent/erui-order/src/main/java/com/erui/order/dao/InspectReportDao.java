package com.erui.order.dao;

import com.erui.order.entity.Area;
import com.erui.order.entity.InspectReport;
import com.erui.order.entity.Purch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangxiaodan on 2017/12/11.
 */
public interface InspectReportDao extends JpaRepository<InspectReport, Serializable>, JpaSpecificationExecutor<InspectReport> {
    InspectReport findByInspectApplyId(Integer InspectApplyId);

    /**
     * 根据报检单id查询质检单列表
     * @param inspectApplyIds
     * @return
     */
    List<InspectReport> findByInspectApplyIdInOrderByIdAsc(List<Integer> inspectApplyIds);
}
