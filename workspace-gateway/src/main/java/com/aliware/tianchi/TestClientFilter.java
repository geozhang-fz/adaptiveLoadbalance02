package com.aliware.tianchi;

import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 * <p>
 * 客户端过滤器
 * 可选接口
 * 用户可以在Gateway服务器端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {

    private long avgTime = 1000;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取invoker对应的provider服务器可用线程数
        AtomicInteger limiter = UserLoadBalanceManager.getAtomicInteger(invoker);
        ProviderLoadInfo providerLoadInfo = UserLoadBalanceManager.getProviderLoadInfo(invoker);

        /* 若为空，limitMap中无记录，表示此为初次调用该provider服务器 */
        if (limiter == null) {
            // 直接远程调用，并返回结果
            // 注：limitMap中的记录会在本次provider服务器返回消息后
            // 在 CallbackListenerImpl 中调用updateProviderLoadInfo实现初始化
            return invoker.invoke(invocation);
        }

        /* 否则，limitMap中存在记录，非初次调用 */
        // 记录远程调用的开始时间
        long startTime = System.currentTimeMillis();
        // 该provider服务器可用线程数-1
        limiter.decrementAndGet();
        // 获取远程调用的结果
        Result result = invoker.invoke(invocation);
        if (result instanceof AsyncRpcResult) {
            AsyncRpcResult asyncResult = (AsyncRpcResult) result;
            asyncResult.getResultFuture().whenComplete((actual, t) -> {
                // 该provider服务器可用线程数+1
                limiter.incrementAndGet();
                long endTime = System.currentTimeMillis();
                // 本次远程调用耗时
                long spend = endTime - startTime;

                /* 更新该provider服务器的负载信息 */
                // 累计Gateway服务器端总耗时
                providerLoadInfo.getClientTotalTimeSpent().addAndGet(spend);
                // Gateway服务器端请求数+1
                int curCount = providerLoadInfo.getClientReqCount().incrementAndGet();

                long clientLastAvgTime = providerLoadInfo.getClientLastAvgTime();
                if (endTime - clientLastAvgTime >= avgTime) {
                    if (providerLoadInfo.getClientLastAvgTimeFlag().compareAndSet(false, true)) {

                        clientLastAvgTime = providerLoadInfo.getClientLastAvgTime();
                        if (endTime - clientLastAvgTime >= avgTime) {
                            // 计算平均每份请求的耗时
                            int avg = providerLoadInfo.getClientTotalTimeSpent().intValue() / curCount;
                            providerLoadInfo.setClientAvgTimeSpent(avg);
                            // 重置
                            providerLoadInfo.getClientReqCount().set(0);
                            providerLoadInfo.getClientTotalTimeSpent().set(0);
                            providerLoadInfo.setClientLastAvgTime(endTime);
                            providerLoadInfo.getClientLastAvgTimeFlag().set(false);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String nowStr = sdf.format(new Date());

                            // 打印统计数据
                            System.out.println(String.format(
                                    "时间：%s，provider-%s统计数据：可用线程数：%s，请求数：%s，平均耗时：%s",
                                    nowStr, providerLoadInfo.getQuota(), limiter.get(), curCount, avg)
                            );
                        }
                    }
                }//if
            });
        }//if (result instanceof AsyncRpcResult)
        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }
}
