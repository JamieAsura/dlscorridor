package cn.jamie.dlscorridor.core.api;

import cn.jamie.dlscorridor.core.meta.InstanceMeta;

import java.util.List;

/**
 * @author jamieLu
 * @create 2024-03-17
 */
public interface Router {
    List<InstanceMeta> router(List<InstanceMeta> instanceMetas);

    Router Default = new Router() {
        @Override
        public List<InstanceMeta> router(List<InstanceMeta> instanceMetas) {
            return instanceMetas;
        }
    };
}
