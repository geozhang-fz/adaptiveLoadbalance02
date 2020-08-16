package com.aliware.tianchi.comm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * provider服务器的负载信息
 *
 */
public class ProviderLoadInfo {
    // 该provider服务器级别：large、medium、small
    private String quota = null;
    public static Map<String, String> QUOTA_TO_PORT = new HashMap<String, String>();

    // provider服务器的线程数量
    private int providerThreadNum = 0;
    // 静态权重
    private volatile int weight = 0;
    // 请求总数(上一个5秒)
    private AtomicLong reqCount = new AtomicLong(0);
    // 当前任务数量
    private AtomicLong activeThreadNum = new AtomicLong(0);
    // 总耗时(上一个5秒)
//    private AtomicLong spendTimeTotal = new AtomicLong(0);
    private AtomicInteger curWeight = new AtomicInteger(0);

    public AtomicInteger getCurWeight() {
        return curWeight;
    }


    static {
        QUOTA_TO_PORT.put("small", "20880");
        QUOTA_TO_PORT.put("medium", "20870");
        QUOTA_TO_PORT.put("large", "20890");
    }

    /**
     * 构造方法
     */
    public ProviderLoadInfo() {

    }

    /**
     * 构造方法
     * @param quota
     * @param providerThreadNum
     */
    public ProviderLoadInfo(String quota, int providerThreadNum){
        System.out.println("Initialize the providerLoadInfo " + quota);
        this.quota = quota;
        // 在实际线程池大小基础上，打8折
        this.providerThreadNum = (int) (providerThreadNum * 0.8);

        if(quota.equals("small")){
            this.weight = 2;
        }else if(quota.equals("medium")){
            this.weight = 9;
        }else if(quota.equals("large")){
            this.weight = 15;
        }else{
            this.weight = 1;
        }
    }//ProviderLoadInfo



    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public int getProviderThreadNum() {
        return providerThreadNum;
    }

    public void setProviderThreadNum(int providerThreadNum) {
        this.providerThreadNum = providerThreadNum;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public AtomicLong getReqCount() {
        return reqCount;
    }

    public void setReqCount(AtomicLong reqCount) {
        this.reqCount = reqCount;
    }

    public AtomicLong getActiveThreadNum() {
        return activeThreadNum;
    }

    public void setActiveThreadNum(AtomicLong activeThreadNum) {
        this.activeThreadNum = activeThreadNum;
    }

}
