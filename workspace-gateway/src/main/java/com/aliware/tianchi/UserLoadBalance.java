package com.aliware.tianchi;

import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author daofeng.xjf
 * <p>
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 * <p>
 * 使用随机权重算法(2): 可用线程数作为权重计算依据
 */
public class UserLoadBalance implements LoadBalance {

    private static Map<Integer, Weight> weights = new HashMap<Integer, Weight>();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
//        return simpleRandomWeight(invokers);
//        return simpleRandom(invokers);

        return simpleRandomActiveThread(invokers);

//        return RoundRobin2(invokers);


    }

    public static <T> Invoker<T> simpleRandomWeight(List<Invoker<T>> invokers) {

        int offsetWeight = ThreadLocalRandom.current().nextInt(20);

        if (offsetWeight < 3) {
            return invokers.get(0);
        }
        if (offsetWeight >= 3 && offsetWeight < 10) {
            return invokers.get(1);
        }
        if (offsetWeight >= 10 && offsetWeight < 20) {
            return invokers.get(2);
        }
        return invokers.get(2);
    }

    public static <T> Invoker<T> simpleRandom(List<Invoker<T>> invokers) {
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }

    public static <T> Invoker<T> simpleRandomActiveThread(List<Invoker<T>> invokers) {
        int size = invokers.size();

        /* 计算总权重 */
        int totalWeight = 0;
        // 用于记录可用线程数大于0的provider服务器的编号
        List<Integer> availProviderArr = new ArrayList<>();
        // 用于记录可用线程数大于0的provider服务器的动态权重
        List<Integer> curWeightArr = new ArrayList<>();

        // 遍历invokers
        for (int i = 0; i < size; i++) {
            Invoker<T> invoker = invokers.get(i);
            // 获取对应的provider服务器
            ProviderLoadInfo providerLoadInfo = GatewayManager.getProviderLoadInfo(invoker);

            if (providerLoadInfo != null) {
                // 获取当前可用线程数
                AtomicInteger limiter = GatewayManager.getAtomicInteger(invoker);
                int availThreadNum = limiter.get();
                if (availThreadNum > 0) {
                    int activeThreadNum = providerLoadInfo.getActiveThreadNum().intValue();
                    availProviderArr.add(i);
                    // 将可用线程数作为该provider服务器的权重
                    curWeightArr.add(activeThreadNum);
                    totalWeight += activeThreadNum;
                }//if (availThreadNum > 0)
            }//if (providerLoadInfo != null)
        }//for

        /* 若provider服务器群满负荷 */
        // 若没有可用线程数大于0的provider服务器
        if (availProviderArr.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowStr = sdf.format(new Date());
            System.out.println(nowStr + "，服务器满负荷");
            // 那么就随机分配一台provider服务器
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }

        /* 使用随机权重算法 */
        // 在0~totalWeight之间取一个随机数
        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        for (int i = 0; i < availProviderArr.size(); i++) {
            int index = availProviderArr.get(i);
            int currentWeight = curWeightArr.get(i);
            if (offsetWeight < currentWeight) {
                return invokers.get(index);
            }
            offsetWeight = offsetWeight - currentWeight;
        }

        // 原始的随机算法兜底
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }//simpleRandomActiveThread

    public static <T> Invoker<T> RoundRobin2(List<Invoker<T>> invokers) {
//            System.out.println("map is not empty.");


        /* 如果weights为空，就初始化 */
        if (weights.isEmpty()) {
            weights.put(0, new Weight(0, 3, 0));
            weights.put(1, new Weight(1, 7, 0));
            weights.put(2, new Weight(2, 10, 0));
        }

        /* 1.计算每台机器的CurWeight */
        for (Weight w: weights.values()) {
            w.setCurWeight(w.getWeight() + w.getCurWeight());
        }

        /* 2.计算curWeight的最大值 */
        Weight maxCurWeight = weights.get(0);
        for (Weight w: weights.values()) {
            if (w == null || w.getCurWeight() > maxCurWeight.getCurWeight()) {
                maxCurWeight = w;
            }
        }

        /* 3.更新curWeight的最大值 */
        // 统计总权重
        int totalWeight = 0;
        for (Weight w: weights.values()) {
            totalWeight += w.getWeight();
        }
        maxCurWeight.setCurWeight(maxCurWeight.getCurWeight() - totalWeight);

        return invokers.get(maxCurWeight.getId());
    }//RoundRobin2
}