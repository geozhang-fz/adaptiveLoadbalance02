package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 在provider服务器端拦截请求和响应，捕获 rpc 调用时产生、provider服务器端返回的已知异常。
 * 即为远程调用前后包裹上代理
 *
 * （可选接口）
 */
// 如果过滤器使用者属于Constants.PROVIDER（服务提供方）就激活使用这个过滤器
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 本次本地调用是否成功的标志位
        boolean isSuccess = false;
        // 记录本地调用的开始时间
        long startTime = System.currentTimeMillis();

        try{
            // 本地调用开始之前，更新provider服务器的信息
            ProviderManager.beforeInvoke();

            Result result = invoker.invoke(invocation);
            isSuccess = true;
            return result;
        }catch (Exception e){
            throw e;
        }finally{

            // 本地调用结束之后，更新provider服务器的信息
            ProviderManager.afterInvoke(System.currentTimeMillis() - startTime, isSuccess);;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }

}
