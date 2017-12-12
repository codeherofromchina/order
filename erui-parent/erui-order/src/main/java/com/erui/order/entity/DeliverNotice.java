package com.erui.order.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 看货通知单
 */
@Entity
@Table(name = "deliver_notice")
public class DeliverNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OneToMany
    @JoinTable(name = "deliver_notice_consign",
            joinColumns = @JoinColumn(name = "deliver_notice_id"),
            inverseJoinColumns = @JoinColumn(name = "deliver_consign_id"))
    private Set<DeliverConsign> deliverConsigns = new HashSet<>();

    @Column(name = "sender_id")
    private Integer senderId;
    @Column(name = "send_date")
    private Date sendDate;
    @Column(name = "trade_terms")
    private String tradeTerms;
    @Column(name = "to_place")
    private String toPlace;
    @Column(name = "transport_type_bn")
    private String transportTypeBn;
    @Column(name = "technical_uid")
    private Integer technicalUid;

    /**
     * 紧急程度 COMMONLY:一般 URGENT:紧急 PARTICULAR:异常紧急
     */
    private String urgency;
    @Column(name = "delivery_date")
    private Date deliveryDate;

    private Integer numers;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "create_user_id")
    private Integer createUserId;
    @Column(name = "update_time")
    private Date updateTime;
    @Column(name = "prepare_req")
    private String prepareReq;
    @Column(name = "package_req")
    private String packageReq;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "deliver_notice_attach",
            joinColumns = @JoinColumn(name = "deliver_notice_id"),
            inverseJoinColumns = @JoinColumn(name = "attach_id"))
    private Set<Attachment> attachmentSet = new HashSet<>();


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<DeliverConsign> getDeliverConsigns() {
        return deliverConsigns;
    }

    public void setDeliverConsigns(Set<DeliverConsign> deliverConsigns) {
        this.deliverConsigns = deliverConsigns;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getTradeTerms() {
        return tradeTerms;
    }

    public void setTradeTerms(String tradeTerms) {
        this.tradeTerms = tradeTerms;
    }

    public String getToPlace() {
        return toPlace;
    }

    public void setToPlace(String toPlace) {
        this.toPlace = toPlace;
    }

    public String getTransportTypeBn() {
        return transportTypeBn;
    }

    public void setTransportTypeBn(String transportTypeBn) {
        this.transportTypeBn = transportTypeBn;
    }

    public Integer getTechnicalUid() {
        return technicalUid;
    }

    public void setTechnicalUid(Integer technicalUid) {
        this.technicalUid = technicalUid;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Integer getNumers() {
        return numers;
    }

    public void setNumers(Integer numers) {
        this.numers = numers;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getPrepareReq() {
        return prepareReq;
    }

    public void setPrepareReq(String prepareReq) {
        this.prepareReq = prepareReq;
    }

    public String getPackageReq() {
        return packageReq;
    }

    public void setPackageReq(String packageReq) {
        this.packageReq = packageReq;
    }


    public Set<Attachment> getAttachmentSet() {
        return attachmentSet;
    }

    public void setAttachmentSet(Set<Attachment> attachmentSet) {
        this.attachmentSet = attachmentSet;
    }
}