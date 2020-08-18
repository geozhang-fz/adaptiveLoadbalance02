package com.aliware.tianchi;

import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.rpc.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 负载均衡管理器，负责协调provider服务器群
 */
public class GatewayManager {
    // key: IP:Port, 该provider服务器的地址，value: ProviderInfo对象
    private static final Map<String, ProviderLoadInfo> LOAD_INFO_MAP = new ConcurrentHashMap<>();
    // key: IP:Port, 该provider服务器的地址，value：该provider服务器可用线程数
    private static final Map<String, AtomicInteger> LIMITER_MAP = new ConcurrentHashMap<String,AtomicInteger>();


//    private static final String HOST_PREFIX = "provider-";
    private static final String HOST_PREFIX = "127.0.0.1";

    /**
     * 获取到某个provider服务器的负载信息，通过invoker对象
     * @param invoker
     * @return
     */
    public static ProviderLoadInfo getProviderLoadInfo(Invoker<?> invoker) {
        String addr = invoker.getUrl().getAddress();
        ProviderLoadInfo providerLoadInfo = LOAD_INFO_MAP.get(addr);
        return providerLoadInfo;
    }

    /**
     * 
     * @param invoker
     * @return
     */
    public static AtomicInteger getLimiter(Invoker<?> invoker) {
        String addr = invoker.getUrl().getAddress();
        AtomicInteger limiter = LIMITER_MAP.get(addr);
        return limiter;
    }

    /**
     * Gateway服务器端一旦收到某个provider服务器的消息后，就会更新其负载信息
     * 该负载信息在Gateway服务器端记录，作为负载均衡的依据
     *
     * @param notifyStr
     */
    public static void updateProviderLoadInfo(String notifyStr) {

        // 获取该provider服务器的字符串信息，切割为信息单元
        String[] msgs = notifyStr.split(",");

        /* 获取当前provider服务器的信息 */
        // 级别, 线程总数, 活跃线程数
        // quota信息直接获取自系统参数
        // CallbackServiceImpl.getNotifyStr()中调用System.getProperty()方法
        String quota = msgs[0];
        // 其余的以下信息来自ProviderLoadManager.getProviderLoadInfo()
        int providerThreadNum = Integer.valueOf(msgs[1]);
        int activeThreadNum = Integer.valueOf(msgs[2]);

        // addr = IP + Port
        String addr = HOST_PREFIX + ":" + ProviderLoadInfo.mapQuotaToPort(quota);
        ProviderLoadInfo providerLoadInfo = LOAD_INFO_MAP.get(addr);
        if (providerLoadInfo == null) {
            // 初始化
//            System.out.println("Initialize the providerLoadInfo " + quota);
            providerLoadInfo = new ProviderLoadInfo(quota, providerThreadNum);
            LOAD_INFO_MAP.put(addr, providerLoadInfo);
        }

        // 更新活跃线程数信息
        providerLoadInfo.getActiveThreadNum().set(activeThreadNum);
        // 该provider服务器可用线程数为
        // 线程总数 - 活跃线程数
        int availThreadNum = providerThreadNum - activeThreadNum;

        AtomicInteger limiter = LIMITER_MAP.get(addr);
        if(limiter == null){
            limiter = new AtomicInteger(availThreadNum);
            LIMITER_MAP.put(addr, limiter);
        }else{
            limiter.set(availThreadNum);
        }
        System.out.println(String.format(
            "\nGateway服务器端更新负载信息：\n该provider服务器的级别：%s，当前活跃线程数：%s，当前可用线程数：%s",
            quota, activeThreadNum, availThreadNum));
    }
}
