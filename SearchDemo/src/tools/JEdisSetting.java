package tools;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by ckboss on 16-4-16.
 */
public class JEdisSetting {

    public static JedisPool jedisPool;

    static {
        if(jedisPool==null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(200);
            config.setMaxIdle(20);
            config.setMaxWaitMillis(1000);
            jedisPool = new JedisPool(config, "localhost", 6379);
        }
    }

    private JEdisSetting(){
    }
}
