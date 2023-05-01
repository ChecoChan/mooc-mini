package com.checo.moocmini.orders.model.dto;


import com.checo.moocmini.orders.model.po.MoocminiPayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * 支付记录dto
 */
@Data
@ToString
public class PayRecordDto extends MoocminiPayRecord {

    //二维码
    private String qrcode;
}
