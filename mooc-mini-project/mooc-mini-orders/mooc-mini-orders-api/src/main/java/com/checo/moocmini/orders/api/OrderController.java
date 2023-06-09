package com.checo.moocmini.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.orders.config.AlipayConfig;
import com.checo.moocmini.orders.model.dto.AddOrderDto;
import com.checo.moocmini.orders.model.dto.PayRecordDto;
import com.checo.moocmini.orders.model.dto.PayStatusDto;
import com.checo.moocmini.orders.model.po.MoocminiPayRecord;
import com.checo.moocmini.orders.service.OrdersService;
import com.checo.moocmini.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Controller
@Api(value = "订单支付接口", tags = "订单支付接口")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        // 获取当前登录用户身份
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        String userId = moocminiUser.getId();
        // 调用 service，完成添加订单信息，添加支付记录，生成支付二维码
        return ordersService.createOrder(userId, addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        // 如果payNo不存在则提示重新发起支付
        MoocminiPayRecord payRecord = ordersService.getPayRecordByPayno(payNo);
        if (payRecord == null)
            MoocMiniException.castException("请重新点击支付获取二维码");
        // 支付状态
        String status = payRecord.getStatus();
        if ("601002".equals(status))
            MoocMiniException.castException("订单已支付，请勿重复支付。");

        // 构造sdk的客户端对象
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);//获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//        alipayRequest.setNotifyUrl("http://tjxt-user-t.itheima.net/xuecheng/orders/paynotify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," +
                " \"total_amount\":\"" + payRecord.getTotalPrice() + "\"," +
                " \"subject\":\"" + payRecord.getOrderName() + "\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form = "";
        try {
            // 请求支付宝下单接口，发起 http 请求
            form = client.pageExecute(alipayRequest).getBody(); // 调用 SDK 生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form); // 直接将完整的表单 html 输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @ResponseBody
    @GetMapping("/payresult")
    @ApiOperation("查询支付结果")
    public PayRecordDto payresult(String payNo) throws IOException {
        // 查询支付结果
        return ordersService.queryPayResult(payNo);
    }

    @ApiOperation("接收支付结果通知")
    @PostMapping("/receivenotify")
    public void receivenotify(HttpServletRequest request, HttpServletResponse out) throws UnsupportedEncodingException, AlipayApiException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        //验签
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if(verify_result) { // 验证成功
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            // 支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            // 交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            // appid
            String app_id = new String(request.getParameter("app_id").getBytes("ISO-8859-1"),"UTF-8");
            // total_amount
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            // 交易成功处理
            if (trade_status.equals("TRADE_SUCCESS")) {

                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setApp_id(app_id);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTotal_amount(total_amount);

                // 处理逻辑
                ordersService.saveAliPayStatus(payStatusDto);
            }
        }
    }
}
