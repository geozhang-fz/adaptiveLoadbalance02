package com.aliware.tianchi;


import com.aliware.tianchi.comm.ProviderLoadInfo;

/**
 * provider服务器的管理器，负责单个provider服务器的接口管理
 */
public class ProviderManager {

    private static final ProviderLoadInfo PROVIDER_LOAD_INFO = new ProviderLoadInfo();

    /**
     * 本地调用开始之前的操作，更新provider服务器的信息
     */
    public static void beforeInvoke(){
        // 对应的provider服务器的活跃线程数加1
        PROVIDER_LOAD_INFO.getActiveThreadNum().incrementAndGet();
    }

    /**
     * 本地调用结束之后的操作，更新provider服务器的信息
     * @param expend
     * @param isSuccess
     */
    public static void afterInvoke(long expend, boolean isSuccess) {
        // 远程调用完成，对应的provider服务器的活跃线程数减1
        PROVIDER_LOAD_INFO.getActiveThreadNum().decrementAndGet();
        // 该provider服务器处理的请求总数加1
        PROVIDER_LOAD_INFO.getReqCount().incrementAndGet();
    }

    public static ProviderLoadInfo getProviderLoadInfo() {
        return PROVIDER_LOAD_INFO;
    }

    public static void reset(){
        PROVIDER_LOAD_INFO.getReqCount().set(0L);
    }
}
