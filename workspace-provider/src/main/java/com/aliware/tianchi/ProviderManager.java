package com.aliware.tianchi;


import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.rpc.Invoker;

/**
 * provider服务器的管理器，负责单个provider服务器的接口管理
 */
public class ProviderManager {

    private static ProviderLoadInfo providerLoadInfo = new ProviderLoadInfo();

    /**
     * 本地调用开始之前的操作，更新provider服务器的信息
     */
    public static void beforeInvoke(){
        // 对应的provider服务器的活跃线程数加1
        providerLoadInfo.getActiveThreadNum().incrementAndGet();
    }

    /**
     * 本地调用结束之后的操作，更新provider服务器的信息
     * @param expend
     * @param isSuccess
     */
    public static void afterInvoke(long expend, boolean isSuccess) {
        // 远程调用完成，对应的provider服务器的活跃线程数减1
        providerLoadInfo.getActiveThreadNum().decrementAndGet();
        // 该provider服务器处理的请求总数加1
        providerLoadInfo.getReqCount().incrementAndGet();
    }

    public static ProviderLoadInfo getProviderLoadInfo() {
        return providerLoadInfo;
    }

    public static void reset(){
        providerLoadInfo.getReqCount().set(0L);
    }
}
