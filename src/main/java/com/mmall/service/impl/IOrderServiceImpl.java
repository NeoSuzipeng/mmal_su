package com.mmall.service.impl;

import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.hb.SysTradeInfo;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by 10353 on 2018/7/4.
 *
 */
@Service("iOrderService")
public class IOrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    private static final Logger log = LoggerFactory.getLogger(IOrderServiceImpl.class);

    private static AlipayTradeService tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }


    // Pay Start

    /**
     * 支付宝支付主流程
     * @param userId
     * @param orderNo
     * @param path
     * @return
     */
    public ServerResponse pay(Integer userId, Long orderNo, String path){
        Map<String,String> resultMap = Maps.newHashMap();
        //查询订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户没有订单");
        }
        resultMap.put("orderNo", order.getOrderNo().toString());

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("mmall商城扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单号：").append(outTradeNo).append("购买商品共")
                .append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByUserIdAndOrderNo(userId, order.getOrderNo());
        for (OrderItem orderItem: orderItemList) {
            GoodsDetail goodsDetail = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity().intValue());
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl("http://smyang.s1.natapp.cc/order/alipay_callback.do")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);



        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                //处理二维码
                File folder = new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                // 需要修改为运行机器上的路径
                //拆分文件路径和文件名
                String qrPath = String.format(path+"/qr-%s.png",
                        response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                log.info("filePath:" + qrPath);

                //二维码上传到FTP服务器
                File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.upload(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常",e);
                }
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                log.info("qrUrl:" + qrUrl);
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }

    private void dumpResponse(AlipayTradePrecreateResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body: " + response.getBody());
        }
    }

    /**
     * 支付宝回调服务接口
     * @param params
     * @return
     */
    public ServerResponse alipayCallBack(Map<String, String> params){
        //获取支付宝回调主要信息
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        log.info("trade_status",tradeStatus);
        //通过回调信息获取中订单信息
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("非mmall慕商城的订单,回调忽略");
        }
        //判断回调是否是正常逻辑
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess("支付宝重复调用");
        }

        //判断支付状态
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            //更新订单支付状态
            order.setPaymentTime(DateUtil.stringToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());

            orderMapper.updateByPrimaryKeySelective(order);
        }

        //持久化交易信息
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    /**
     * 轮询查看订单支付状态接口
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<Boolean> queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    // Pay End

    // Portal Start

    /**
     * 创建订单服务
     * @param id
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse create(Integer userId, Integer shippingId) {
        //第一步：从购物车中获取已选择商品信息
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        //第二步：生成订单，填充必要信息：如总价，邮费等
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        Order order = this.assembleOrder(userId,shippingId,payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单生成失败");
        }
        //第三步：批量添加订单商品详情
        for (OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        orderItemMapper.bashInsert(orderItemList);  //失败按照业务的不同选择忽略或者订单回滚、删除（此处选择忽略）
        log.info("订单详情生成成功");
        //第四步：减少库存
        this.reduceStock(orderItemList);
        log.info("库存减少成功");
        //第五步：清空购物车
        this.cleanCart(cartList);
        log.info("购物车清空成功");
        //第六步：组合orderVo
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 取消订单服务
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("该用户没有此订单，无法取消");
        }
        //判断订单状态
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("订单已付款，无法取消");
        }


        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int rowCount = orderMapper.updateByPrimaryKeySelective(newOrder);
        if (rowCount > 0){
            return ServerResponse.createBySuccess("取消成功");
        }
        return ServerResponse.createByErrorMessage("取消失败");
    }

    /**
     * 获取购物车中选中商品详情信息服务
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        //获取购物车中数据
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderItem orderItem :orderItemList){
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(totalPrice);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }


    /**
     * 订单详情服务
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse getOrderDetail(Integer userId, Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("该用户没有此订单");
        }

        List<OrderItem> orderItemList = orderItemMapper.getByUserIdAndOrderNo(userId, orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }


    /**
     * 用户订单列表服务
     * @param userId
     * @return
     */
    public ServerResponse<PageInfo> getOrderlist(Integer userId, Integer pageSize, Integer pageNum){

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null){
                // 管理员操作
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                //用户操作
                orderItemList = orderItemMapper.getByUserIdAndOrderNo(userId, order.getOrderNo());
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }

        return orderVoList;
    }

    /**
     * 封装OrderVo
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();

        //填充
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        //填充收货地址信息
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        //填充时间类信息
        orderVo.setPaymentTime(DateUtil.dateToString(order.getPaymentTime()));
        orderVo.setSendTime(DateUtil.dateToString(order.getSendTime()));
        orderVo.setEndTime(DateUtil.dateToString(order.getEndTime()));
        orderVo.setCreateTime(DateUtil.dateToString(order.getCreateTime()));
        orderVo.setCloseTime(DateUtil.dateToString(order.getCloseTime()));

        //填充图片信息
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        //填充订单详情信息
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     * 封装OrderItemVo
     * @param orderItem
     * @return
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateUtil.dateToString(orderItem.getCreateTime()));
        return orderItemVo;
    }

    /**
     * 封装ShippingVo
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    /**
     * 清空购物车
     * @param cartList
     */
    private void cleanCart(List<Cart> cartList) {
        for(Cart cart :cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 批量减少商品库存
     * @param orderItemList
     */
    private void reduceStock(List<OrderItem> orderItemList) {
        for(OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 生成订单
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        Long orderNo = this.generateOrderNo();

        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);

        int rowCount = orderMapper.insert(order);
        if (rowCount > 0)
            return order;
        return null;
    }

    /**
     * 计算订单总额
     * @param orderItemList
     * @return
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    /**
     * 获取购物车中详细商品信息
     * @param userId
     * @param cartList
     * @return
     */
    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cartItem: cartList){
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //检查商品是否在售卖状态
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMessage("商品:"+product.getName()+"不在售");
            }
            //检查库存
            if (product.getStock() < cartItem.getQuantity()){
                return ServerResponse.createByErrorMessage("商品"+product.getName()+"库存不足");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity().doubleValue()));

            orderItemList.add(orderItem);
        }

        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 生成订单号 version 1.0
     * @return
     */
    private Long generateOrderNo() {
        Long currentTime = System.currentTimeMillis();
        Long orderNo = currentTime + new Random().nextInt(100);
        return orderNo;
    }



    // Potal End

    // backend start

    /**
     * 订单列表管理服务
     * @param pageSize
     * @param pageNum
     * @return
     */
    public ServerResponse<PageInfo> manageList(Integer pageSize, Integer pageNum){

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 订单详情管理服务
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> manageOrderDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 订单搜索管理服务 Version 1.0
     * @param orderNo
     * @param pageSize
     * @param pageNum
     * @return
     */
    public ServerResponse<PageInfo> manageOrderSearch(Long orderNo, Integer pageSize, Integer pageNum){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     *
     * 用户订单送货管理服务
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse manageOrderSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null){
            if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                int rowCount = orderMapper.updateByPrimaryKey(order);
                if (rowCount > 0){
                    return ServerResponse.createBySuccess("发货成功");
                }
                return ServerResponse.createByErrorMessage("发货失败");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }
    // backend end

}
