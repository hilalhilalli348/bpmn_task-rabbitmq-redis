package com.example.bpmn_task.config;




import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPool;


@Configuration
public class RedisConfig {

    @Bean
    JedisPool jedisPool(){
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        return new JedisPool(genericObjectPoolConfig,"localhost",6379,2000);
    }

}
