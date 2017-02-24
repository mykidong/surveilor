package io.shunters.surveilor.util;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SpringContextSingleton {

    private static ConcurrentMap<String, ApplicationContext> contextMap;

    private static final Object lock = new Object();


    public static ApplicationContext getInstance(String... conf)
    {
        String confPath = Arrays.asList(conf).toString();

        if(contextMap == null) {
            synchronized(lock) {
                if(contextMap == null) {

                    contextMap = new ConcurrentHashMap<>();

                    ApplicationContext ctx = new ClassPathXmlApplicationContext(conf);

                    contextMap.put(confPath, ctx);

                    System.out.println("contextMap is initialized, conf: [" + confPath + "]");
                }
            }
        }
        else
        {
            synchronized(lock) {
                if (!contextMap.containsKey(confPath)) {
                    ApplicationContext ctx = new ClassPathXmlApplicationContext(conf);

                    contextMap.put(confPath, ctx);

                    System.out.println("conf: [" + confPath + "] not exists, new context put to map.");
                }
            }
        }

        return contextMap.get(confPath);
    }
}
