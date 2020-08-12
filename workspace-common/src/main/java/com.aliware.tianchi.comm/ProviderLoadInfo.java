package com.aliware.tianchi.comm;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * provider服务器的负载信息
 *
 */
public class ProviderLoadInfo {
    // 该provider服务器级别：large、medium、small
    private String quota = null;
    // provider服务器的线程数量
    private int providerThreadNum = 0;
    // 静态权重
    private volatile int weight = 0;
    // 请求总数(上一个5秒)
    private AtomicLong reqCount = new AtomicLong(0);
    // 当前任务数量
    private AtomicLong activeThreadNum = new AtomicLong(0);
    // 总耗时(上一个5秒)
    private AtomicLong spendTimeTotal = new AtomicLong(0);
    // 上次请求失败时间
    private long lastFailTime;
    private int avgSpendTime;

    private AtomicLong clientTotalTimeSpent = new AtomicLong(0);
    private AtomicInteger clientReqCount = new AtomicInteger(0);
    private volatile int clientAvgTimeSpent = 0;
    private volatile long clientLastAvgTime = System.currentTimeMillis();
    private AtomicBoolean clientLastAvgTimeFlag = new AtomicBoolean(false);

    public AtomicLong getClientTotalTimeSpent() {
        return clientTotalTimeSpent;
    }

    public void setClientTotalTimeSpent(AtomicLong clientTotalTimeSpent) {
        this.clientTotalTimeSpent = clientTotalTimeSpent;
    }

    public AtomicInteger getClientReqCount() {
        return clientReqCount;
    }

    public void setClientReqCount(AtomicInteger clientReqCount) {
        this.clientReqCount = clientReqCount;
    }

    public int getClientAvgTimeSpent() {
        return clientAvgTimeSpent;
    }

    public void setClientAvgTimeSpent(int clientAvgTimeSpent) {
        this.clientAvgTimeSpent = clientAvgTimeSpent;
    }

    public long getClientLastAvgTime() {
        return clientLastAvgTime;
    }

    public void setClientLastAvgTime(long clientLastAvgTime) {
        this.clientLastAvgTime = clientLastAvgTime;
    }

    public AtomicBoolean getClientLastAvgTimeFlag() {
        return clientLastAvgTimeFlag;
    }

    public void setClientLastAvgTimeFlag(AtomicBoolean clientLastAvgTimeFlag) {
        this.clientLastAvgTimeFlag = clientLastAvgTimeFlag;
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

        this.quota = quota;
        // 在实际线程池大小基础上，打8折
        this.providerThreadNum = (int) (providerThreadNum * 0.8);

        if(quota.equals("small")){
            this.weight = 2;
        }else if(quota.equals("medium")){
            this.weight = 5;
        }else if(quota.equals("large")){
            this.weight = 8;
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

    public AtomicLong getSpendTimeTotal() {
        return spendTimeTotal;
    }

    public void setSpendTimeTotal(AtomicLong spendTimeTotal) {
        this.spendTimeTotal = spendTimeTotal;
    }

    public long getLastFailTime() {
        return lastFailTime;
    }

    public void setLastFailTime(long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }

    public int getAvgSpendTime() {
        return avgSpendTime;
    }

    public void setAvgSpendTime(int avgSpendTime) {
        this.avgSpendTime = avgSpendTime;
    }
}
