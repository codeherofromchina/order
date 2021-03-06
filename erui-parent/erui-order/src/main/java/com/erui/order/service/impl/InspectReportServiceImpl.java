package com.erui.order.service.impl;

import com.erui.order.dao.*;
import com.erui.order.entity.*;
import com.erui.order.service.AttachmentService;
import com.erui.order.service.InspectReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wangxiaodan on 2017/12/11.
 */
@Service
public class InspectReportServiceImpl implements InspectReportService {

    @Autowired
    private InspectReportDao inspectReportDao;
    @Autowired
    private InspectApplyGoodsDao inspectApplyGoodsDao;
    @Autowired
    private InspectApplyDao inspectApplyDao;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private PurchDao purchDao;
    @Autowired
    private InstockDao instockDao;
    @Autowired
    private PurchGoodsDao purchGoodsDao;

    @Override
    @Transactional
    public InspectReport findById(Integer id) {
        return inspectReportDao.findOne(id);
    }

    @Override
    @Transactional
    public InspectReport detail(Integer id) {
        InspectReport inspectReport = inspectReportDao.findOne(id);
        if (inspectReport != null) {
            inspectReport.getAttachments().size();
            inspectReport.getInspectGoodsList().size();
        }

        return inspectReport;
    }

    /**
     * 按照条件分页查找
     *
     * @param condition
     * @return
     */
    @Override
    @Transactional
    public Page<InspectReport> listByPage(InspectReport condition) {

        PageRequest request = new PageRequest(condition.getPage(), condition.getPageSize(), Sort.Direction.DESC, "createTime");

        Page<InspectReport> page = inspectReportDao.findAll(new Specification<InspectReport>() {
            @Override
            public Predicate toPredicate(Root<InspectReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                // 报检单单号模糊查询
                if (StringUtils.isNotBlank(condition.getInspectApplyNo())) {
                    list.add(cb.like(root.get("inspectApplyNo").as(String.class), "%" + condition.getInspectApplyNo() + "%"));
                }


                Join<InspectReport, InspectApply> inspectApply = root.join("inspectApply");
                list.add(cb.equal(inspectApply.get("master").as(Boolean.class), Boolean.TRUE)); // 只查询主质检单

                Set<InspectApply> inspectApplySet = findByProjectNoAndContractNo(condition.getProjectNo(), condition.getContractNo());
                if (inspectApplySet != null && inspectApplySet.size() > 0) {
                    CriteriaBuilder.In<Object> idIn = cb.in(inspectApply.get("id"));
                    for (InspectApply p : inspectApplySet) {
                        idIn.value(p.getId());
                    }
                    list.add(idIn);
                }

                // 根据采购合同号模糊查询
                if (StringUtils.isNotBlank(condition.getPurchNo())) {
                    list.add(cb.like(inspectApply.get("purchNo").as(String.class), "%" + condition.getPurchNo() + "%"));
                }

                // 供应商名称模糊查询
                if (StringUtils.isNotBlank(condition.getSupplierName())) {
                    list.add(cb.like(root.get("supplierName").as(String.class), "%" + condition.getSupplierName() + "%"));
                }

                // 检验员查询
                if (condition.getCheckUserId() != null) {
                    list.add(cb.equal(root.get("checkUserId").as(Integer.class), condition.getCheckUserId()));
                }

                // 是否外检查询
                if (condition.getDirect() != null) {
                    list.add(cb.equal(inspectApply.get("direct"), condition.getDirect()));
                }

                // 质检状态查询
                if (condition.getProcess() != null) {
                    list.add(cb.equal(root.get("process").as(Boolean.class), condition.getProcess()));
                }
                // 只查询是第一次报检单的质检信息
                list.add(cb.equal(root.get("reportFirst"), Boolean.TRUE));

                Predicate[] predicates = new Predicate[list.size()];
                predicates = list.toArray(predicates);
                return cb.and(predicates);
            }
        }, request);


        if (page.hasContent()) {
            // 转换数据
            page.getContent().parallelStream().forEach(inspectReport -> {
                InspectApply inspectApply = inspectReport.getInspectApply();
                inspectReport.setPurchNo(inspectApply.getPurchNo());


                // 销售合同号
                List<String> contractNoList = new ArrayList<String>();
                // 项目号
                List<String> projectNoList = new ArrayList<String>();
                inspectApply.getInspectApplyGoodsList().forEach(vo -> {
                    Goods goods = vo.getGoods();

                    contractNoList.add(goods.getContractNo());
                    projectNoList.add(goods.getProjectNo());
                });
                inspectReport.setContractNo(StringUtils.join(contractNoList, ","));
                inspectReport.setProjectNo(StringUtils.join(projectNoList, ","));
                inspectReport.setDirect(inspectApply.getDirect());
                inspectReport.setAttachments(null);
            });
        }

        return page;
    }


    // 根据销售号和项目号查询报检列表信息
    private Set<InspectApply> findByProjectNoAndContractNo(String projectNo, String contractNo) {
        Set<InspectApply> result = null;
        if (!(StringUtils.isBlank(projectNo) && StringUtils.isBlank(contractNo))) {
            List<InspectApply> list = inspectApplyDao.findAll(new Specification<InspectApply>() {

                @Override
                public Predicate toPredicate(Root<InspectApply> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    List<Predicate> list = new ArrayList<>();


                    Join<InspectApply, InspectApplyGoods> inspectApplyGoods = root.join("inspectApplyGoodsList");
                    Join<InspectApplyGoods, Goods> goods = inspectApplyGoods.join("goods");

                    // 销售合同号模糊查询
                    if (StringUtils.isNotBlank(contractNo)) {
                        list.add(cb.like(goods.get("contractNo").as(String.class), "%" + contractNo + "%"));
                    }

                    // 根据项目号模糊查询
                    if (StringUtils.isNotBlank(projectNo)) {
                        list.add(cb.like(goods.get("projectNo").as(String.class), "%" + projectNo + "%"));
                    }


                    Predicate[] predicates = new Predicate[list.size()];
                    predicates = list.toArray(predicates);
                    return cb.and(predicates);
                }
            });
        }


        return result;
    }


    /**
     * 保存质检单
     *
     * @param inspectReport
     * @return
     */
    @Override
    @Transactional
    public boolean save(InspectReport inspectReport) throws Exception {
        InspectReport dbInspectReport = inspectReportDao.findOne(inspectReport.getId());
        if (dbInspectReport == null) {
            return false;
        }
        InspectReport.StatusEnum statusEnum = InspectReport.StatusEnum.fromCode(inspectReport.getStatus());
        if (statusEnum == null || statusEnum == InspectReport.StatusEnum.INIT) {
            return false;
        }
        if (dbInspectReport.getStatus() == InspectReport.StatusEnum.DONE.getCode()) {
            return false;
        }

        // 处理基本数据
        dbInspectReport.setCheckUserId(inspectReport.getCheckUserId());
        dbInspectReport.setCheckUserName(inspectReport.getCheckUserName());
        dbInspectReport.setCheckDeptId(inspectReport.getCheckDeptId());
        dbInspectReport.setCheckDeptName(inspectReport.getCheckDeptName());
        dbInspectReport.setNcrNo(inspectReport.getNcrNo());
        dbInspectReport.setCheckDate(inspectReport.getCheckDate());
        dbInspectReport.setDoneDate(inspectReport.getDoneDate());
        dbInspectReport.setReportRemarks(inspectReport.getReportRemarks());
        dbInspectReport.setStatus(statusEnum.getCode());

        // 处理附件信息
        List<Attachment> attachments = attachmentService.handleParamAttachment(dbInspectReport.getAttachments(), inspectReport.getAttachments(), inspectReport.getCreateUserId(), inspectReport.getCreateUserName());
        dbInspectReport.setAttachments(attachments);

        // 处理商品信息
        Map<Integer, InspectApplyGoods> inspectGoodsMap = inspectReport.getInspectGoodsList().parallelStream().
                collect(Collectors.toMap(InspectApplyGoods::getId, vo -> vo)); // 参数的质检商品
        List<InspectApplyGoods> inspectGoodsList = dbInspectReport.getInspectGoodsList(); // 数据库原来报检商品
        if (inspectGoodsMap.size() != inspectGoodsList.size()) {
            return false;
        }
        boolean hegeFlag = true;
        for (InspectApplyGoods applyGoods : inspectGoodsList) {
            InspectApplyGoods paramApplyGoods = inspectGoodsMap.get(applyGoods.getId());
            if (paramApplyGoods == null) {
                return false;
            }

            applyGoods.setSamples(paramApplyGoods.getSamples());
            applyGoods.setUnqualified(paramApplyGoods.getUnqualified());
            applyGoods.setUnqualifiedDesc(applyGoods.getUnqualifiedDesc());
            if (applyGoods.getUnqualified() > 0) {
                hegeFlag = false;
            }

            // 设置采购商品的已合格数量
            if (statusEnum == InspectReport.StatusEnum.DONE) { // 提交动作
                // 合格数量
                int qualifiedNum = applyGoods.getInspectNum() - applyGoods.getUnqualified();
                if (qualifiedNum < 0) {
                    return false;
                }
                PurchGoods purchGoods = applyGoods.getPurchGoods();
                purchGoods.setGoodNum(purchGoods.getGoodNum() + qualifiedNum);
                purchGoodsDao.save(purchGoods);
            }
        }

        // 提交动作 则设置第一次质检，和相应的报检信息
        if (statusEnum == InspectReport.StatusEnum.DONE) {
            dbInspectReport.setProcess(false);
            InspectApply inspectApply = dbInspectReport.getInspectApply();

            if (hegeFlag && !dbInspectReport.getReportFirst()) {
                // 将第一次质检设置为完成
                InspectApply parentInspectApply = inspectApply.getParent();
                InspectReport firstInspectReport = inspectReportDao.findByInspectApplyId(parentInspectApply.getId());
                firstInspectReport.setProcess(false);
                inspectReportDao.save(firstInspectReport);
            } else if (dbInspectReport.getReportFirst() && !hegeFlag) {
                dbInspectReport.setProcess(true);
            }

            inspectApply.setStatus(hegeFlag ? InspectApply.StatusEnum.QUALIFIED.getCode() : InspectApply.StatusEnum.UNQUALIFIED.getCode());
            inspectApplyDao.save(inspectApply);
        }

        inspectReportDao.save(dbInspectReport);

        // 最后判断采购是否完成
        if (statusEnum == InspectReport.StatusEnum.DONE) {
            Purch purch = dbInspectReport.getInspectApply().getPurch();
            List<PurchGoods> purchGoodsList = purch.getPurchGoodsList();
            boolean doneFlag = true;
            for (PurchGoods pg : purchGoodsList) {
                if (pg.getGoodNum() < pg.getPurchaseNum()) {
                    doneFlag = false;
                    break;
                }
            }
            if (doneFlag) {
                purch.setStatus(Purch.StatusEnum.DONE.getCode());
                purchDao.save(purch);
            }

            // 推送数据到入库部门
            Instock instock = new Instock();
            instock.setInspectReport(dbInspectReport);
            instock.setInspectApplyNo(dbInspectReport.getInspectApplyNo()); // 报检单号
            instock.setSupplierName(dbInspectReport.getInspectApply().getPurch().getSupplierName()); // 供应商
            instock.setStatus(Instock.StatusEnum.INIT.getStatus());
            instock.setCreateTime(new Date());
            List<InstockGoods> instockGoodsList = new ArrayList<>();
            // 入库商品
            for (InspectApplyGoods inspectGoods : dbInspectReport.getInspectGoodsList()) {
                InstockGoods instockGoods = new InstockGoods();
                instockGoods.setInstock(instock);
                instockGoods.setContractNo(inspectGoods.getGoods().getContractNo());
                instockGoods.setProjectNo(inspectGoods.getGoods().getProjectNo());
                instockGoods.setInspectApplyGoods(inspectGoods);
                instockGoods.setQualifiedNum(inspectGoods.getInspectNum() - inspectGoods.getUnqualified());
                instockGoods.setInstockNum(instockGoods.getQualifiedNum()); // 入库数量
                Date date = new Date();
                instockGoods.setCreateTime(date);
                instockGoods.setUpdateTime(date);
                instockGoods.setCreateUserId(dbInspectReport.getCreateUserId());
            }
            instock.setInstockGoodsList(instockGoodsList);

            instockDao.save(instock);
        }

        return true;
    }


    @Override
    @Transactional
    public List<InspectReport> history(Integer id) {
        List<InspectReport> result = null;
        InspectReport inspectReport = inspectReportDao.findOne(id);
        // 质检多次的才有历史
        if (inspectReport != null && inspectReport.getReportFirst() != null && inspectReport.getReportFirst() && inspectReport.getCheckTimes() > 1) {
            InspectApply inspectApply = inspectReport.getInspectApply();
            Integer parentApplyId = inspectApply.getId();
            List<InspectApply> childInspectApplyList = inspectApplyDao.findByParentIdOrderByIdAsc(parentApplyId);
            List<Integer> inspectApplyIds = childInspectApplyList.parallelStream().map(InspectApply::getId).collect(Collectors.toList());
            inspectApplyIds.add(parentApplyId);
            result = inspectReportDao.findByInspectApplyIdInOrderByIdAsc(inspectApplyIds);
        }


        return result;
    }
}
