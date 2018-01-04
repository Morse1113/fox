package com.github.wenbo2018.fox.remoting.invoker.proxy;

import com.github.wenbo2018.fox.remoting.invoker.InvokerBootStrap;
import com.github.wenbo2018.fox.remoting.exception.RpcException;
import com.github.wenbo2018.fox.remoting.invoker.api.ServiceProxy;
import com.github.wenbo2018.fox.remoting.invoker.config.InvokerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shenwenbo on 16/8/6.
 */
public abstract class AbstractRemotingServiceProxy implements ServiceProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemotingServiceProxy.class);

    protected static Map<InvokerConfig, Object> services = new ConcurrentHashMap<InvokerConfig, Object>();

    @Override
    public void init() {

    }

    @Override
    public <T> T getProxy(InvokerConfig config) {
        Object service = null;
        service=services.get(config);
        if (service==null) {
            try {
                InvokerBootStrap.startup();
                ServiceInvocationProxy serviceInvocationProxy=new ServiceInvocationProxy(config);
                service=(T) Proxy.newProxyInstance(config.getClass().getClassLoader(),
                        new Class<?>[]{ config.getInterfaceClass() },serviceInvocationProxy);
            } catch (Throwable e) {
                throw new RpcException("error while trying to invoke service :{}",e);
            }
            services.put(config,service);
        }
        return (T) service;
    }
}
