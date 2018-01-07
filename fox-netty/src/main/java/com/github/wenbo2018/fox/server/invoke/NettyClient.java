package com.github.wenbo2018.fox.server.invoke;

import com.github.wenbo2018.fox.common.bean.InvokeRequest;
import com.github.wenbo2018.fox.common.bean.InvokeResponse;
import com.github.wenbo2018.fox.common.common.FoxConstants;
import com.github.wenbo2018.fox.remoting.invoker.AbstractClient;
import com.github.wenbo2018.fox.remoting.common.ConnectInfo;
import com.github.wenbo2018.fox.remoting.invoker.async.CallbackFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wenbo2018 on 2016/8/26.
 */
public class NettyClient extends AbstractClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    private Channel channel;

    private Bootstrap bootstrap = new Bootstrap();

    private ConnectInfo connectInfo;

    private volatile boolean connected = false;

    private InvokeResponse invokeResponse;

    private InvokeRequest invokeRequest;

    private String adress;

    private static ConcurrentHashMap<String, LinkedBlockingQueue<InvokeResponse>>
            responseMap = new ConcurrentHashMap<String, LinkedBlockingQueue<InvokeResponse>>();

    public NettyClient(EventLoopGroup group, ConnectInfo connectInfo) {
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ClientChannelInitializer(this));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        this.connectInfo = connectInfo;
        this.adress = connectInfo.getHost() + ":" + connectInfo.getPort();
    }

    @Override
    protected void doConnect() {
        if (this.connected) {
            return;
        }
        // 连接 RPC 服务器
        String host = connectInfo.getHost();
        int port = connectInfo.getPort();
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            LOGGER.error("connect rpc server:{}", e);
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                LOGGER.info("fox Service success collect");
            }
        });
        if (channelFuture.isSuccess()) {
            this.channel = channelFuture.channel();
            this.connected = true;
        }
    }

    @Override
    protected InvokeResponse doWrite(InvokeRequest request, CallbackFuture callbackFuture) {
        Object[] msg = new Object[]{request, callbackFuture};
        ChannelFuture future = null;
        future = channel.writeAndFlush(request);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("fox success invoke");
                    return;
                }
            }
        });
        return null;
    }


    public void reConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }
        // 连接 RPC 服务器
        String host = connectInfo.getHost();
        int port = connectInfo.getPort();
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            LOGGER.error("connect rpc server:{}", e);
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                LOGGER.info("Fox Service success restart");
            }
        });
        if (channelFuture.isSuccess()) {
            this.channel = channelFuture.channel();
            this.connected = true;
        }
    }


    @Override
    public void setContext(String host, int port) {

    }


    @Override
    public InvokeResponse getResponse() {
        String messageId = invokeRequest.getRequestId();
        InvokeResponse invokeResponse = null;
        try {
            if (invokeRequest.getMessageType() == FoxConstants.MESSAGE_TYPE_HEART) {
                LOGGER.info("message typr:心跳消息");
            } else {
                LOGGER.info("message typr:服务消息");
            }
            invokeResponse = responseMap.get(messageId).take();
        } catch (final InterruptedException ex) {
            responseMap.remove(messageId);
        } finally {
            responseMap.remove(messageId);
        }
        while (this.invokeResponse == null) {

        }
        return this.invokeResponse;
    }

    @Override
    public String getAdress() {
        return this.adress;
    }


}
