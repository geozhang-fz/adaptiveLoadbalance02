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

        /* 若为空，limitMap中无记录，表示初次调用该provider服务器 */
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
        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }
}
