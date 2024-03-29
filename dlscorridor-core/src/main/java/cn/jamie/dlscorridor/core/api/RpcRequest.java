package cn.jamie.dlscorridor.core.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    // 请求接口
    private String service;
    // 请求方法名
    private String methodSign;
    // not use 与请求参数一一对应代理方法签名
    private List<Class<?>> methodParaTypes;
    // 请求参数
    private Object[] args;
}
