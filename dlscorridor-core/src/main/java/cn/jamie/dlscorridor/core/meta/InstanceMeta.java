package cn.jamie.dlscorridor.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 注册的服务实例元数据
 *
 * @author jamieLu
 * @create 2024-03-20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {
    private String schema;
    private String host;
    private Integer port;
    private String context;
    // 上下线
    @Builder.Default
    private boolean status = true;
    private Map<String,String> parameters;

    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }
    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toAddress() {
        return String.format("%s:%d", host, port);
    }

    public static InstanceMeta httpInstanceMeta(String host, Integer port) {
        return InstanceMeta.builder().schema("http").host(host).port(port).build();
    }

    public static InstanceMeta pathToInstance(String path) {
        String[] paras = path.split("_", -1);
        assert paras.length > 1;
        return InstanceMeta.builder().host(paras[0]).port(Integer.valueOf(paras[1])).build();
    }

}
