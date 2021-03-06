package com.erui.order.service;

import com.erui.order.entity.DeliverConsign;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangxiaodan on 2017/12/11.
 */
public interface DeliverConsignService {

    /**
     * 查找出口发货通知单
     *
     * @param id
     * @return
     */
    DeliverConsign findById(Integer id);

    /**
     * 根据出口发货通知单 查询信息
     * @param deliverNoticeId  看货通知单号  字符串
     * @return
     */
    List<DeliverConsign> querExitInformMessage(Integer[] deliverNoticeId);


    /**
     * 看货通知管理   查询出口发货通知单
     * @return
     */
    List<DeliverConsign> queryExitAdvice();



    /**
     * 修改出口发货通知单
     *
     * @param deliverConsign
     * @return
     */
    boolean updateDeliverConsign(DeliverConsign deliverConsign);

    /**
     * 新增出口发货通知单
     *
     * @param deliverConsign
     * @return
     */
    boolean addDeliverConsign(DeliverConsign deliverConsign);
    /**
     * 根据订单id查找出口发货通知单
     *
     * @param orderId
     * @return
     */
    List<DeliverConsign> findByOrderId(Integer orderId);
}
