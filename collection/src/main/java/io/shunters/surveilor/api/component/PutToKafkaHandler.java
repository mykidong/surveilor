package io.shunters.surveilor.api.component;

/**
 * Created by mykidong on 2017-02-24.
 */
public interface PutToKafkaHandler<T> {

    public void put(T msg) throws Exception ;
}
