package com.checo.moocmini.orders.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@Data
@ToString
@TableName("moocmini_orders_goods")
public class MoocminiOrdersGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private Long orderId;

    /**
     * 商品 id
     */
    private String goodsId;

    /**
     * 商品类型
     */
    private String goodsType;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品交易价，单位分
     */
    private Float goodsPrice;

    /**
     * 商品详情 Json,可为空
     */
    private String goodsDetail;
}
