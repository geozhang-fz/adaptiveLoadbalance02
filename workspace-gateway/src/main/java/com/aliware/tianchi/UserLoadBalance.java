package com.aliware.tianchi;

import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int size = invokers.size();

        /* 计算总权重 */
        int totalWeight = 0;

        // 获取耗时最大的那个provider服务器，通过invoker获取
        // 遍历invokers
        for (int i = 0; i < size; i++) {
            // 获取当前
            Invoker<T> invoker = invokers.get(i);
            ProviderLoadInfo providerLoadInfo = UserLoadBalanceManager.getProviderLoadInfo(invoker);
            AtomicInteger limiter = UserLoadBalanceManager.getAtomicInteger(invoker);

            if (providerLoadInfo != null) {
                int permits = limiter.get();
            }
        }


        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
