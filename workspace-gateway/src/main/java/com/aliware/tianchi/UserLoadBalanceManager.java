package com.aliware.tianchi;

import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.rpc.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 负载均衡管理器，负责协调provider服务器群
 */
public class UserLoadBalanceManager {
    // key: quota, 该provider服务器的级别  value: ProviderInfo对象
    public static Map<String, ProviderLoadInfo> loadInfoMaps = new ConcurrentHashMap<>();
    // key: add; 即 host:port，value：该provider服务器可用线程数
    private static Map<String, AtomicInteger> limitMap = new ConcurrentHashMap<String,AtomicInteger>();

    private static final String HOST_PEFIX = "provider-";

    /**
     * 获取到某个provider服务器的负载信息，通过invoker对象
     * @param invoker
     * @return
     */
    public static ProviderLoadInfo getProviderLoadInfo(Invoker<?> invoker) {
        String host = invoker.getUrl().getHost();
        ProviderLoadInfo providerLoadInfo = loadInfoMaps.get(host);
        return providerLoadInfo;
    }

    /**
     * 
     * @param invoker
     * @return
     */
    public static AtomicInteger getAtomicInteger(Invoker<?> invoker) {
        String host = invoker.getUrl().getAddress();
        AtomicInteger limiter = limitMap.get(host);
        return limiter;
    }

    /**
     * 更新某个provider服务器的负载信息
     * 该负载信息在Gateway服务器端记录，作为负载均衡的依据
     * @param notifyStr
     */
    public static void updateProviderLoadInfo(String notifyStr) {

        // 获取该provider服务器的信息为字符串，切割为信息单元
        String[] providerLoadInfos = notifyStr.split(",");

        // 分别获取当前该provider服务器的信息：
        // 级别, 线程总数, 活跃线程数, 平均耗时
        String quota = providerLoadInfos[0];
        int providerThreadNum = Integer.valueOf(providerLoadInfos[1]);
        int activeThreadNum = Integer.valueOf(providerLoadInfos[2]);
        int avgTime = Integer.valueOf(providerLoadInfos[3]);

        String host = HOST_PEFIX + quota;
        ProviderLoadInfo providerLoadInfo = loadInfoMaps.get(host);
        if (providerLoadInfo == null) {
            // 初始化
            providerLoadInfo = new ProviderLoadInfo(quota, providerThreadNum);
            loadInfoMaps.put(host, providerLoadInfo);
        }

        // 更新活跃线程数信息
        providerLoadInfo.getActiveThreadNum().set(activeThreadNum);
        // 该provider服务器可用线程数为
        // 线程总数 - 活跃线程数
        int availThreadNum = providerThreadNum - activeThreadNum;
        providerLoadInfo.setAvgSpendTime(avgTime);

        AtomicInteger limiter = limitMap.get(host);
        if(limiter == null){
            limiter = new AtomicInteger(availThreadNum);
            limitMap.put(host, limiter);
        }else{
            limiter.set(availThreadNum);
        }
        System.out.println(String.format(
            "该provider服务器的级别：%s，当前活跃线程数：%s，当前可用线程数：%s，该provider服务器的静态权重：%s",
            quota, activeThreadNum, availThreadNum, providerLoadInfo.getWeight()));
    }
}
