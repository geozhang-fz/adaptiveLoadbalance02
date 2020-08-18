package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * Gateway服务器端的监听器
 * 该类获取provider服务器端的推送信息，与 CallbackService 搭配使用
 * （可选接口）
 */
public class CallbackListenerImpl implements CallbackListener {

    /**
     * provider服务器端的CallbackServiceImpl调用，请求Gateway服务器端接收消息
     * @param msg
     */
    @Override
    public void receiveServerMsg(String msg) {

//        System.out.println("\nreceive msg from provider: " + msg);

        // 传入接收到的provider服务器的消息
        GatewayManager.updateProviderLoadInfo(msg);
    }

}
