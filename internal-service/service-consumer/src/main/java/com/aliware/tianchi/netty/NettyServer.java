package com.aliware.tianchi.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.dubbo.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Netty，Gateway对外作为Netty服务器，该函数作为Netty服务器的启动程序，配置启动项
     */
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();

        /* 创建 BossGroup 和 WorkerGroup */
        bossGroup = new NioEventLoopGroup(
                1, new NamedThreadFactory("Dubbo-Proxy-Boss"));
        workerGroup = new NioEventLoopGroup(
                        Runtime.getRuntime().availableProcessors() * 2,
                        new NamedThreadFactory("Dubbo-Proxy-Worker"));
        HttpProcessHandler handler = new HttpProcessHandler();

        /* 创建服务器端的启动对象，配置参数 */
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new HttpObjectAggregator(0));
                                pipeline.addLast(handler);
                            }
                        })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        /* 启动服务器(并绑定端口) */
        // consumer对外作为一个服务器，这是这个服务器的地址和端口
//        String host = "0.0.0.0";
        String host = "127.0.0.1";
        int port = 8087;
//        int port = 20880;
        try {
            // Consumer对外作为服务器，监听请求的地址和端口
            ChannelFuture f = bootstrap.bind(host, 8087).sync();
//            ChannelFuture f = bootstrap.bind(host, 20880).sync();
            logger.info("Dubbo proxy started, host is {}, port is {}.", host, port);

            /* 监听通道中的“关闭”事件 */
            f.channel().closeFuture().sync();
            logger.info("Dubbo proxy closed, host is {} , 8087 is {}.", host, port);
        } catch (InterruptedException e) {
            logger.error("DUBBO proxy start failed", e);
        } finally {

            /* 处理异常 */
            destroy();
        }
    }

    public void destroy() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}
