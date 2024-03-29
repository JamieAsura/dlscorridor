package cn.jamie.dlscorridor.core.consumer;

import cn.jamie.dlscorridor.core.api.RpcContext;
import cn.jamie.dlscorridor.core.api.RpcRequest;
import cn.jamie.dlscorridor.core.api.RpcResponse;
import cn.jamie.dlscorridor.core.filter.CacheFilter;
import cn.jamie.dlscorridor.core.filter.FilterChain;
import cn.jamie.dlscorridor.core.filter.RpcFilterChain;
import cn.jamie.dlscorridor.core.meta.InstanceMeta;
import cn.jamie.dlscorridor.core.transform.HttpInvoker;
import cn.jamie.dlscorridor.core.util.HttpUtil;
import cn.jamie.dlscorridor.core.util.RpcMethodUtil;
import cn.jamie.dlscorridor.core.util.RpcReflectUtil;
import cn.jamie.dlscorridor.core.util.RpcUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消费这动态代理调用服务提供者
 */
@Slf4j
public class JMInvocationHandler implements InvocationHandler {
    private Class<?> service;
    private RpcContext rpcContext;
    private List<InstanceMeta> instanceMetas;

    public JMInvocationHandler(Class<?> service, RpcContext rpcContext, List<InstanceMeta> instanceMetas) {
        this.service = service;
        this.rpcContext = rpcContext;
        this.instanceMetas = instanceMetas;
    }
    private final HttpInvoker httpInvoker = new HttpInvoker();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Object父类方法禁止远程调用
        if (RpcMethodUtil.notPermissionMethod(method.getName())) {
            return null;
        }
        // 组装远程调用参数
        RpcRequest rpcRequest = RpcRequest.builder()
            .service(service.getCanonicalName())
            .methodSign(RpcReflectUtil.analysisMethodSign(method))
            .args(args).build();
        RpcResponse rpcResponse = RpcResponse.builder().status(false).data(null).build();
        rpcContext.getFilterChain().doFilter(rpcRequest, rpcResponse, this::handler);
        if (rpcResponse.isStatus()) {
            return JSON.to(method.getReturnType(), rpcResponse.getData());
        } else {
            Exception exception = rpcResponse.getEx();
            log.error(exception.getMessage(),exception);
            throw exception;
        }
    }
    private RpcResponse handler(RpcRequest rpcRequest) {
        // 远程调用路由
        InstanceMeta instanceMeta = rpcContext.getLoadBalancer().choose(rpcContext.getRouter().router(instanceMetas));
        log.info("real invoke url:" + instanceMeta.toAddress());
        return post(rpcRequest,HttpUtil.convertIpAddressToHttp(instanceMeta.toAddress()));
    }

    private RpcResponse post(RpcRequest rpcRequest, String url) {
        String res = null;
        try {
            res = httpInvoker.postOkHttp(url, JSON.toJSONString(rpcRequest));
            return JSON.parseObject(res, RpcResponse.class);
        } catch (IOException e) {
            return RpcResponse.builder().status(false).ex(e).build();
        }
    }
}
