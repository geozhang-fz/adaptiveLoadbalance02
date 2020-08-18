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
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 * 使用随机权重算法(2): 可用线程数作为权重计算依据
 */
public class UserLoadBalance implements LoadBalance {

    private static final int INVOKERS_SIZE = 3;

    private static Context[] contextArr = new Context[INVOKERS_SIZE];

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

//        return simpleRandomWeight(invokers);

//        return simpleRandom(invokers);

        return simpleRandomActiveThread(invokers);

//        return RoundRobinWeight1(invokers);

//        return RoundRobinWeight2(invokers);


    }

    public static <T> Invoker<T> simpleRandomWeight(List<Invoker<T>> invokers) {

        int[] weights = new int[INVOKERS_SIZE];

        // 设置静态权重
        weights[0] = 3;
        weights[1] = 7;
        weights[2] = 10;

        /* 计算总权重 */
        int totalWeight = 0;
        for (int w: weights) {
            totalWeight += w;
        }

        /* 随机选取一台服务器 */
        // 取0~totalWeight内随机整数
        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        if (offsetWeight < weights[0]) {
            return invokers.get(0);
        }
        if (offsetWeight >= weights[0] && offsetWeight < weights[0] + weights[1]) {
            return invokers.get(1);
        } else {
            return invokers.get(2);
        }
    }

    public static <T> Invoker<T> simpleRandom(List<Invoker<T>> invokers) {
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }

    public static <T> Invoker<T> simpleRandomActiveThread(List<Invoker<T>> invokers) {
        int size = invokers.size();

        /* 计算总权重 */
        // 总权重为各 provider 可用线程数之和
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
                AtomicInteger limiter = GatewayManager.getLimiter(invoker);
                int availThreadNum = limiter.get();
                if (availThreadNum > 0) {
                    availProviderArr.add(i);
                    // 将可用线程数作为该provider服务器的权重
                    curWeightArr.add(availThreadNum);
                    totalWeight += availThreadNum;
                }//if (availThreadNum > 0)
            }//if (providerLoadInfo != null)
        }//for

        /* 若provider服务器群满负荷 */
        if (availProviderArr.isEmpty()) { // 若没有可用线程数大于0的provider服务器
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

    public static <T> Invoker<T> RoundRobinWeight1(List<Invoker<T>> invokers) {

        int[] weights = new int[INVOKERS_SIZE];

        // 设置静态权重
        weights[0] = 3;
        weights[1] = 7;
        weights[2] = 10;

        /* 计算总权重 */
        int totalWeight = 0;
        for (int w: weights) {
            totalWeight += w;
        }

        /* 轮询选取一台provider服务器 */
        int pos = Context.incrementAndGetPos() % totalWeight;

        if (pos < weights[0]) {
            return invokers.get(0);
        }else if (pos >= weights[0] && pos < weights[0] + weights[1]) {
            return invokers.get(1);
        } else {
            return invokers.get(2);
        }
    }

    public static <T> Invoker<T> RoundRobinWeight2(List<Invoker<T>> invokers) {

//            System.out.println("map is not empty.");


        /* 如果weights为空，就初始化 */
        if (contextArr[0] == null) {
            contextArr[0] = new Context(0, 3, 0);
            contextArr[1] = new Context(1, 7, 0);
            contextArr[2] = new Context(2, 10, 0);
        }

        /* 1.计算每台机器的动态权重CurWeight */
        for (Context context: contextArr) {
            context.setCurWeight(context.getWeight() + context.getCurWeight());
        }

        /* 2.找到curWeight最大值对应的context对象 */
        // 防空指针，初始化最大值指向第0台provider
        Context maxCurContext = contextArr[0];
        for (Context context: contextArr) {
            if (context == null || context.getCurWeight() > maxCurContext.getCurWeight()) {
                maxCurContext = context;
            }
        }

        /* 3.更新curWeight的最大值 */
        // 统计总权重
        int totalWeight = 0;
        for (Context w: contextArr) {
            totalWeight += w.getWeight();
        }
        // 更新curWeight的最大值
        maxCurContext.setCurWeight(maxCurContext.getCurWeight() - totalWeight);

        // 返回最大动态权重对应的provider
        return invokers.get(maxCurContext.getId());
    }//RoundRobin2


}