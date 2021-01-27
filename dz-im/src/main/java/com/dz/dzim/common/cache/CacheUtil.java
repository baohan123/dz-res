package com.dz.dzim.common.cache;/**
 * @description: some desc
 * @author: lenovo
 * @email: xxx@xx.com
 * @date: 2021/1/26 12:12
 */

import org.apache.logging.log4j.util.PropertiesUtil;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.Cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baohan
 * @className 缓存管理 Manager
 * @description TODO
 * @date 2021/1/26 12:12
 */
public class CacheUtil {
    /**
     * 隐藏构造器
     */
    private CacheUtil() {
    }

    // 主缓存，用于存放需要存放的对象
    private static final Map<String, Object> CACHE_MAP = new ConcurrentHashMap<>();

    // 用户存放过期的时间
    private static final Map<String, Long> TIME_MAP = new ConcurrentHashMap<String, Long>();

    /** 保存对象的长度 （个数） **/
    private static final int OBJ_LENGTH = 1000;

    /** 保存对象的大小（以B为单位，包括了，过期时间的大小和对象的大小） **/
    private static final int OBJ_SIZE = 1024 * 1024;

    /**
     * 刷新缓存，清除过期数据（同步处理）
     *
     * @throws Exception
     *             异常
     */
    public static void flushPastCacheSyn() {

        // 迭代器删除过期数据（删除缓存时也）
        Iterator<Map.Entry<String, Object>> cacheIterator = CACHE_MAP.entrySet().iterator();

        while (cacheIterator.hasNext()) {

            Map.Entry<String, Object> entry = cacheIterator.next();

            String key = entry.getKey();

            if (TIME_MAP.containsKey(key)) {

                // 获取过期时间
                long expireTime = TIME_MAP.get(key);

                // 已经过期则删除
                if (System.currentTimeMillis() > expireTime) {

                    // 缓存内容
                    cacheIterator.remove();
                    // 删除截止时间
                    TIME_MAP.remove(key);
                }
            }
        }
    }

    /**
     * 刷新缓存，清除过期数据（异步处理）
     *
     * @throws Exception
     *             异常
     */
    public static void flushPastCacheAsyn() {

        // 先删除过期数据
        RunTemp temp = new RunTemp();
        temp.start();
    }

    /**
     * 清除缓存，清空数据
     */
    public static void clearPastCache() {

        CACHE_MAP.clear();
        TIME_MAP.clear();
    }

    /**
     * 设置value永久的
     *
     * @param key
     *            key
     * @param value
     *            value
     * @throws Exception
     *             异常
     */
    public static void set(String key, Object value) throws Exception {

        // 添加数据时，先删除过期数据，这样可以做到动态更新，在SpringBoot中可以用线程池管理，但是此处这么写显然不是很合适，所以注掉
        // RunTemp temp = new RunTemp();
        // temp.start();

        // 不过期也就是默认100年后过期
        final long expiretime = System.currentTimeMillis() + 100 * 365 * 24 * 60 * 60 * 1000;
        CACHE_MAP.put(key, value);
        TIME_MAP.put(key, expiretime);

        // 不能存储
        if (!canCacheObject(key)) {
            throw new Exception("超长超大了");
        }
    }

    /**
     * 设置value
     *
     * @param key
     *            key
     * @param value
     *            value
     * @param ms
     *            毫秒过期时间
     * @throws Exception
     *             异常
     */
    public static void set(final String key, Object value, int ms) throws Exception {

        // 先删除过期数据
        // RunTemp temp = new RunTemp();
        // temp.start();

        final long expireTime = System.currentTimeMillis() + ms;

        CACHE_MAP.put(key, value);
        TIME_MAP.put(key, expireTime);

        // 不能存储
        if (!canCacheObject(key)) {
            throw new Exception("超长超大了");
        }
    }

    /**
     * 获取指定的value，如果key不存在或者已过期，则返回null
     *
     * @param key
     *            key
     * @return 结果
     * @throws Exception
     *             异常
     */
    public static Object get(String key) throws Exception {

        // 判断key是否存在
        if (!CACHE_MAP.containsKey(key) || !TIME_MAP.containsKey(key)) {
            return null;
        }

        // 缓存失效，已过期
        if (TIME_MAP.get(key) < System.currentTimeMillis()) {

            // 如果获取的是已过期的key，直接将其删除
            CACHE_MAP.remove(key);
            TIME_MAP.remove(key);

            return null;
        }

        // 返回value
        return CACHE_MAP.get(key);
    }

    /**
     *
     * 能够否继续储存
     *
     * @param key
     *            对象key
     * @return 能否存储
     * @throws Exception
     *             异常
     */
    public static boolean canCacheObject(final String key) throws Exception {

        // 超长超大(最好在put前判断大小，可以防止map被撑爆，而put后判断可以做到精准判断，这里以实现功能为主)
        if (CACHE_MAP.size() > OBJ_LENGTH || getCacheSize(CACHE_MAP, TIME_MAP) > OBJ_SIZE) {

            // 如果超大，超长了，删除刚填进去的key
            CACHE_MAP.remove(key);
            TIME_MAP.remove(key);

            return false;
        }
        return true;
    }

    /**
     * 描述:异步删除过期了的缓存
     *
     * @author baoahn
     */
    private static class RunTemp extends Thread {

        /**
         * 多线程异步删除过期缓存（每次运行添加数据时执行一次清除操作，若是在SpringBoot项目中可以利用定时器实现定时清除，此处仅做模拟，所以在添加数据的时候清理一下）
         */
        @Override
        public void run() {

            // 删除过期内容
            flushPastCacheSyn();
        }
    }

    /**
     * key是否存在
     *
     * @param key
     *            key
     * @return 是否存在
     * @throws Exception
     *             异常
     */
    public static boolean isExistKey(String key) throws Exception {
        return CACHE_MAP.containsKey(key);
    }

    /**
     * 值是否存在
     *
     * @param value
     *            value
     * @return 是否存在
     * @throws Exception
     *             异常
     */
    public static boolean isExistValue(String value) throws Exception {
        return CACHE_MAP.containsValue(value);
    }

    /**
     * @param maps
     *            多个map的内存总大小（以字节B为单位）
     * @return 返回map的所占内存大小
     * @throws IOException
     *             异常
     */
    @SafeVarargs
    public static int getCacheSize(Map<String, ? extends Object>... maps) throws IOException {

        int size = 0;

        A: for (Map<String, ? extends Object> map : maps) {

            // 判断map是否为空
            if (null == map || map.size() < 0) {
                break A;
            }
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;

            try {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);

                oos.writeObject(map);

                // 获取字节大小（单位为B）
                byte[] byteArray = bos.toByteArray();

                size += byteArray.length;
            } catch (IOException e) {

                // 打印异常
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    bos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            }
        }

        return size;
    }
}
