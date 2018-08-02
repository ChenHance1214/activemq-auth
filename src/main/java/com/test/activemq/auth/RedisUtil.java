package com.test.activemq.auth;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
	private static JedisPool jedisPool;
	private static RedisUtil instance = new RedisUtil();

	private RedisUtil() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(10);
		jedisPool = new JedisPool(jedisPoolConfig, "192.168.1.128");
	}

	public static RedisUtil getInstance(){
		return instance;
	}

	public static void set(String key, String value) {
		Jedis jedis = null;
		try {
			// 从连接池获取一个Jedis实例
			jedis = jedisPool.getResource();
			// 设置 redis 字符串数据 SET 10km blog.csdn.net/10km
			jedis.set(key, value);
			// 获取存储的数据并输出
			System.out.println("redis 存储的内容key: " + key +" ,value: " + value);
		} finally {
			if (null != jedis)
				jedis.close(); // 释放资源还给连接池
		}
	}

	public static String get(String key) {
		Jedis jedis = null;
		String value = null;
		try {
			// 从连接池获取一个Jedis实例
			jedis = jedisPool.getResource();
			// 设置 redis 字符串数据 SET 10km blog.csdn.net/10km
			value = jedis.get(key);
			// 获取存储的数据并输出
			System.out.println("redis 取出的字符串为: " + value);
		} finally {
			if (null != jedis)
				jedis.close(); // 释放资源还给连接池
		}
		return value;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		RedisUtil instance2 = RedisUtil.getInstance();
		instance2.set("chenwenhaoxxxx", "test");
		String string = instance2.get("chenwenhaoxxxx");
		System.out.println(string);
	}

}
