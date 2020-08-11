package com.aliware.tianchi;


import com.aliware.tianchi.comm.ProviderLoadInfo;

/**
 * provider服务器的管理器，负责单个provider服务器的接口管理
 */
public class ProviderLoadManager {


    public static ProviderLoadInfo providerLoadInfo = new ProviderLoadInfo();

    public static void resetSpendTime(){
        providerLoadInfo.getSpendTimeTotal().set(0L);
        providerLoadInfo.getReqCount().set(0L);
    }

    public static ProviderLoadInfo getProviderLoadInfo() {
        return providerLoadInfo;
    }
}
