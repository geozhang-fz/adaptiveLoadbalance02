package com.aliware.tianchi;

import com.aliware.tianchi.comm.ProviderLoadInfo;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author daofeng.xjf
 * <p>
 * 该类实现provider服务器端向Gateway服务器端动态推送消息
 * provider服务器接收Gateway服务器 CallbackListener 的注册，并执行推送能力
 * （可选接口）
 */
public class CallbackServiceImpl implements CallbackService {

    /**
     * 构造函数
     */
    public CallbackServiceImpl() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String nowStr = sdf.format(now);

                // 该provider服务器的级别，线程总数，活跃线程数，平均耗时
                String notifyStr = getNotifyStr();

                System.out.println(String.format("统计数据【时间:%s,%s】",
                        nowStr, notifyStr));

                if (!listeners.isEmpty()) {
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {
                            // entry.getValue()：获取到该provider服务器对应的监听器
                            // 调用receiveServerMsg()方法推送provider服务器信息
                            entry.getValue().receiveServerMsg(notifyStr);
                        } catch (Throwable t1) {
                            listeners.remove(entry.getKey());
                        }
                    }//for
                    ProviderLoadManager.resetSpendTime();
                }//if
            }
        }, 0, 5000);
    }

    /**
     * 生成推送给Gateway服务器的信息
     * @return notifyStr: 该provider服务器的信息，包括该provider服务器的级别，线程总数，活跃线程数，平均耗时
     */
    private String getNotifyStr() {

        // 获取dubbo的配置管理对象
        // Optional 是个容器：可以保存类型T的值，或者仅仅保存null
        Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);

        // 获取provider服务器群线程池的容量
        int providerThreadNum = protocolConfig.get().getThreads();

        // 获取系统参数，provider服务器的级别：large、medium、small
        String quota = System.getProperty("quota");
        // 获取当前provider服务器的负载信息
        ProviderLoadInfo providerLoadInfo = ProviderLoadManager.getProviderLoadInfo();
        long activeThreadNum = providerLoadInfo.getActiveThreadNum().get();
        long spendTimeTotal = providerLoadInfo.getSpendTimeTotal().get();
        long reqCount = providerLoadInfo.getReqCount().get();
        long avgTime = 0;
        if(reqCount != 0){
            avgTime = spendTimeTotal/reqCount;
        }

        // 整理推送给Gateway服务器的信息：
        // 该provider服务器的级别，线程总数，活跃线程数，平均耗时
        String notifyStr = String.format("%s,%s,%s,%s",
                quota,
                providerThreadNum,
                activeThreadNum,
                avgTime);

        return notifyStr;
    }

    private Timer timer = new Timer();

    /**
     * key: listener type
     * value: callback listener
     */
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();


    @Override
    public void addListener(String key, CallbackListener listener) {

        listeners.put(key, listener);
        listener.receiveServerMsg(getNotifyStr()); // send notification for change
    }
}
