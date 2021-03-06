package com.erui.order.service.impl;

import com.erui.comm.NewDateUtil;
import com.erui.comm.util.data.string.StringUtil;
import com.erui.order.dao.OrderDao;
import com.erui.order.dao.ProjectDao;
import com.erui.order.entity.*;
import com.erui.order.entity.Order;
import com.erui.order.requestVo.AddOrderVo;
import com.erui.order.requestVo.OrderListCondition;
import com.erui.order.requestVo.PGoods;
import com.erui.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Created by wangxiaodan on 2017/12/11.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ProjectDao projectDao;

    @Override
    @Transactional
    public Order findById(Integer id) {
        Order order = orderDao.findOne(id);
        order.getGoodsList().size();
        order.getAttachmentSet().size();
        order.getOrderPayments().size();
        return order;
    }

    @Transactional
    @Override
    public Page<Order> findByPage(final OrderListCondition condition) {
        PageRequest pageRequest = new PageRequest(condition.getPage(), condition.getRows(), null);
        Page<Order> pageList = orderDao.findAll(new Specification<Order>() {
            @Override
            public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                // 根据销售同号模糊查询
                if (StringUtil.isNotBlank(condition.getContractNo())) {
                    list.add(cb.like(root.get("contractNo").as(String.class), "%" + condition.getContractNo() + "%"));
                }
                //根据Po号模糊查询
                if (StringUtil.isNotBlank(condition.getPoNo())) {
                    list.add(cb.like(root.get("poNo").as(String.class), "%" + condition.getPoNo() + "%"));
                }
                //根据询单号查询
                if (StringUtil.isNotBlank(condition.getInquiryNo())) {
                    list.add(cb.like(root.get("inquiryNo").as(String.class), "%" + condition.getInquiryNo() + "%"));
                }
                //根据市场经办人查询
                if (StringUtil.isNotBlank(condition.getAgentName())) {
                    list.add(cb.like(root.get("agentName").as(String.class), "%" + condition.getAgentName() + "%"));
                }
                //根据订单签订时间查询
                if (condition.getSigningDate() != null) {
                    list.add(cb.equal(root.get("signingDate").as(Date.class), NewDateUtil.getDate(condition.getSigningDate())));
                }
                //根据合同交货日期查询
                if (condition.getDeliveryDate() != null) {
                    list.add(cb.equal(root.get("deliveryDate").as(Date.class), NewDateUtil.getDate(condition.getDeliveryDate())));
                }
                //根据crm客户代码查询
                if (StringUtil.isNotBlank(condition.getCrmCode())) {
                    list.add(cb.like(root.get("crmCode").as(String.class), "%" + condition.getCrmCode() + "%"));
                }
                //根据crm客户代码查询
                if (StringUtil.isNotBlank(condition.getFrameworkNo())) {
                    list.add(cb.like(root.get("frameworkNo").as(String.class), "%" + condition.getFrameworkNo() + "%"));
                }
                //根据订单类型
                if (condition.isOrderType() != null) {
                    list.add(cb.equal(root.get("orderType"), condition.isOrderType()));
                }
                //根据汇款状态
                if (condition.getPayStatus() != null) {
                    list.add(cb.equal(root.get("payStatus").as(Integer.class), condition.getPayStatus()));
                }
                if (condition.getStatus() != null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), condition.getStatus()));
                }
                //根据订单来源查询
                if (StringUtil.isNotBlank(condition.getOrderSource())) {
                    list.add(cb.like(root.get("orderSource").as(String.class), "%" + condition.getOrderSource() + "%"));
                }
                list.add(cb.equal(root.get("deleteFlag"), false));
                Predicate[] predicates = new Predicate[list.size()];
                predicates = list.toArray(predicates);
                return cb.and(predicates);
            }
        }, pageRequest);
        return pageList;
    }

    @Override
    public void deleteOrder(int[] ids) {
        List<Order> orderList = orderDao.findByIdIn(ids);
        List<Order> collect = orderList.parallelStream()
                .filter(vo -> vo.getStatus() == 1)
                .map(vo -> {
                    vo.setDeleteFlag(true);
                    vo.setDeleteTime(new Date());
                    return vo;
                }).collect(Collectors.toList());
        orderDao.save(collect);
    }

    @Override
    @Transactional
    public boolean updateOrder(AddOrderVo addOrderVo) {
        Order order = orderDao.findOne(addOrderVo.getId());
        if (order == null) {
            return false;
        }
        addOrderVo.copyBaseInfoTo(order);
        order.setAttachmentSet(addOrderVo.getAttachDesc());
        List<PGoods> pGoodsList = addOrderVo.getGoodDesc();
        Goods goods = null;
        List<Goods> goodsList = new ArrayList<>();
        for (PGoods pGoods : pGoodsList) {
            goods = new Goods();
        //    goods.setSeq(pGoods.getSeq());
            goods.setId(pGoods.getId());
            goods.setSku(pGoods.getSku());
            goods.setMeteType(pGoods.getMeteType());
            goods.setNameEn(pGoods.getNameEn());
            goods.setNameZh(pGoods.getNameZh());
            goods.setContractGoodsNum(pGoods.getContractGoodsNum());
            goods.setUnit(pGoods.getUnit());
            goods.setModel(pGoods.getModel());
            goods.setClientDesc(pGoods.getClientDesc());
            goods.setBrand(pGoods.getBrand());
            goods.setContractNo(order.getContractNo());
            goods.setOutstockNum(0);
            goodsList.add(goods);
        }
        order.setGoodsList(goodsList);
        order.setOrderPayments(addOrderVo.getContractDesc());
        order.setDeleteFlag(false);
        Order orderUpdate = orderDao.saveAndFlush(order);
        if (addOrderVo.getStatus() != 1) {
            Project projectAdd = new Project();
            projectAdd.setOrder(orderUpdate);
            projectAdd.setExecCoName(orderUpdate.getExecCoName());
            projectAdd.setContractNo(orderUpdate.getContractNo());
            projectAdd.setDistributionDeptName(orderUpdate.getDistributionDeptName());
            projectAdd.setBusinessUnitName(orderUpdate.getBusinessUnitName());
            projectAdd.setRegion(orderUpdate.getRegion());
            projectAdd.setProjectStatus("SUBMIT");
            projectDao.save(projectAdd);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean addOrder(AddOrderVo addOrderVo) {
        Order order = new Order();
        addOrderVo.copyBaseInfoTo(order);
       /* order.setContractNo(addOrderVo.getContractNo());
        order.setFrameworkNo(addOrderVo.getFrameworkNo());
        order.setPoNo(addOrderVo.getPoNo());
        order.setContractNoOs(addOrderVo.getContractNoOs());
        order.setInquiryNo(addOrderVo.getInquiryNo());
        order.setLogiQuoteNo(addOrderVo.getLogiQuoteNo());
        order.setOrderType(addOrderVo.isOrderType());
        order.setOrderSource(addOrderVo.getOrderSource());
        order.setSigningDate(addOrderVo.getSigningDate());
        order.setDeliveryDate(addOrderVo.getDeliveryDate());
        order.setSigningCo(addOrderVo.getSigningCo());
        order.setAgentId(addOrderVo.getAgentId());
        order.setAgentName(addOrderVo.getAgentName());
        order.setExecCoId(addOrderVo.getExecCoId());
        order.setRegion(addOrderVo.getRegion());
        order.setDistributionDeptId(addOrderVo.getDistributionDeptId());
        order.setCountry(addOrderVo.getCountry());
        order.setCrmCode(addOrderVo.getCrmCode());
        order.setCustomerType(addOrderVo.isCustomerType());
        order.setPerLiableRepay(addOrderVo.getPerLiableRepay());
        order.setBusinessUnitId(addOrderVo.getBusinessUnitId());
        order.setTechnicalId(addOrderVo.getTechnicalId());
        order.setGrantType(addOrderVo.getGrantType());
        order.setIsPreinvest(addOrderVo.isPreinvest());
        order.setIsFinancing(addOrderVo.isFinancing());
        order.setTradeTerms(addOrderVo.getTradeTerms());
        order.setTransportType(addOrderVo.getTransportType());
        order.setFromCountry(addOrderVo.getFromCountry());
        order.setFromPlace(addOrderVo.getFromPlace());
        order.setFromPort(addOrderVo.getFromPort());
        order.setToCountry(addOrderVo.getToCountry());
        order.setToPlace(addOrderVo.getToPlace());
        order.setToPort(addOrderVo.getToPort());
        order.setTotalPrice(addOrderVo.getTotalPrice());
        order.setCurrencyBn(addOrderVo.getCurrencyBn());
        order.setTaxBearing(addOrderVo.getTaxBearing());
        order.setPaymentModeBn(addOrderVo.getPaymentModeBn());
        order.setQualityFunds(addOrderVo.getQualityFunds());
        order.setPayStatus(addOrderVo.getPayStatus());
        order.setStatus(addOrderVo.getStatus());
        order.setDeliveryRequires(addOrderVo.getDeliveryRequires());
        order.setCustomerContext(addOrderVo.getCustomerContext());
        order.setBusinessUnitName(addOrderVo.getBusinessUnitName());
        order.setExecCoName(addOrderVo.getExecCoName());
        order.setDistributionDeptName(addOrderVo.getDistributionDeptName());*/
        order.setAttachmentSet(addOrderVo.getAttachDesc());
        List<PGoods> pGoodsList = addOrderVo.getGoodDesc();
        Goods goods = null;

        List<Goods> goodsList = new ArrayList<>();
        for (PGoods pGoods : pGoodsList) {
            goods = new Goods();
            //goods.setSeq(pGoods.getSeq());
            goods.setSku(pGoods.getSku());
            goods.setOutstockNum(0);
            goods.setMeteType(pGoods.getMeteType());
            goods.setNameEn(pGoods.getNameEn());
            goods.setNameZh(pGoods.getNameZh());
            goods.setContractGoodsNum(pGoods.getContractGoodsNum());
            goods.setUnit(pGoods.getUnit());
            goods.setModel(pGoods.getModel());
            goods.setClientDesc(pGoods.getClientDesc());
            goods.setBrand(pGoods.getBrand());
            goods.setContractNo(order.getContractNo());
            goodsList.add(goods);

        }
      /*  List<OrderPayment> orderPaymentList = addOrderVo.getContractDesc();
        for (OrderPayment orderPayment:orderPaymentList) {
            orderPayment.setOrderId(order.getId());
        }*/
        order.setGoodsList(goodsList);
        order.setOrderPayments(addOrderVo.getContractDesc());
        order.setCreateTime(new Date());
        order.setDeleteFlag(false);
        Order order1 = orderDao.save(order);
        if (addOrderVo.getStatus() != 1) {
            Project project = new Project();
            project.setOrder(order1);
            project.setContractNo(order1.getContractNo());
            project.setExecCoName(order1.getExecCoName());
            project.setBusinessUnitName(order1.getBusinessUnitName());
            project.setDistributionDeptName(order1.getDistributionDeptName());
            project.setRegion(order1.getRegion());
            project.setProjectStatus("SUBMIT");
            projectDao.save(project);
        }
        return true;
    }

    @Override
    @Transactional
    public Order detail(Integer orderId) {
        Order order = orderDao.findOne(orderId);
        if (order != null) {
            order.getGoodsList().size();
        }
        return order;
    }


}
