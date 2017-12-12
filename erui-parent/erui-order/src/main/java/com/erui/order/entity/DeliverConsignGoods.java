package com.erui.order.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * 口发货通知单商品
 */
@Entity
@Table(name="deliver_consign_goods")
public class DeliverConsignGoods {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="deliver_consign_id")
    private DeliverConsign deliverConsign;

    @OneToOne
    @JoinColumn(name="goods_id")
    private Goods goods;

    @Column(name = "send_num")
    private Integer sendNum;

    @Column(name = "pack_require")
    private String packRequire;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "create_user_id")
    private Integer createUserId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DeliverConsign getDeliverConsign() {
        return deliverConsign;
    }

    public void setDeliverConsign(DeliverConsign deliverConsign) {
        this.deliverConsign = deliverConsign;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public Integer getSendNum() {
        return sendNum;
    }

    public void setSendNum(Integer sendNum) {
        this.sendNum = sendNum;
    }

    public String getPackRequire() {
        return packRequire;
    }

    public void setPackRequire(String packRequire) {
        this.packRequire = packRequire;
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
}