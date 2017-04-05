package com.point.iot.base.memcache;
//package com.yahe.base.memcache;
//import java.util.Date;
//
//import com.danga.MemCached.MemCachedClient;
//import com.danga.MemCached.SockIOPool;
//
///**
// * 使用memcached的缓存测试类.
// * @author 
// */
//public class MemcachedClient {
//    // 创建全局的唯一实例
//    protected static MemCachedClient mMcc = new MemCachedClient();
//
//    protected static MemcachedClient mMemCached = new MemcachedClient();
//
//    // 设置与缓存服务器的连接池
//    static {
//        // 服务器列表和其权重
//        String[] servers = {"192.168.1.3:12111"};
//        Integer[] weights = {3};
//
//        // 获取socket连接池的实例对象
//        SockIOPool pool = SockIOPool.getInstance();
//
//        // 设置服务器信息
//        pool.setServers(servers);
//        pool.setWeights(weights);
//
//        // 设置初始连接数、最小和最大连接数以及最大处理时间
//        pool.setInitConn(5);
//        pool.setMinConn(5);
//        pool.setMaxConn(250);
//        pool.setMaxIdle(1000 * 60 * 60 * 6);
//
//        // 设置主线程的睡眠时间
//        pool.setMaintSleep(30);
//
//        // 设置TCP的参数，连接超时等
//        pool.setNagle(false);
//        pool.setSocketTO(3000);
//        pool.setSocketConnectTO(0);
//
//        // 初始化连接池
//        pool.initialize();
//
///*        // 压缩设置，超过指定大小（单位为K）的数据都会被压缩
//        mcc.setCompressEnable(true);
//        mcc.setCompressThreshold(64 * 1024);*/
//    }
//
//
//    public MemcachedClient(String name){
//    	
//    }
//    public MemcachedClient() {
//
//    }
//
//    /**
//     * 获取唯一实例.
//     * @return
//     */
//    public static MemcachedClient getInstance() {
//        return mMemCached;
//    }
//
//    /**
//     * 添加一个指定的值到缓存中.
//     * @param key 键
//     * @param value 值
//     * @return 在缓存中若该key不存在，并成功添加返回true，否则将返回false
//     */
//    public boolean add(String key, Object value) {
//        return mMcc.add(key, value);
//    }
//
//    /**
//     * 添加一个键值对到缓存中.
//     * @param key 键
//     * @param value 值
//     * @param expires 超时时间
//     * @return 在缓存中若该key不存在，并成功添加返回true，否则将返回false
//     */
//    public boolean add(String key, Object value, Date expires) {
//        return mMcc.add(key, value, expires);
//    }
//
//    /**
//     * 将某个键的值改变成新值，首先需要保证该键存在.
//     * @param key 键
//     * @param value 值
//     * @return 成功返回true，失败返回false
//     */
//    public boolean replace(String key, Object value) {
//        return mMcc.replace(key, value);
//    }
//
//    /**
//     * 将某个键的值改变成新值，首先需要保证该键存在.
//     * @param key 键
//     * @param value 值
//     * @param expires 超时时间
//     * @return 成功返回true，失败返回false
//     */
//    public boolean replace(String key, Object value, Date expires) {
//        return mMcc.replace(key, value, expires);
//    }
//
//    /**
//     * 添加一个指定的值到缓存中.
//     * @param key
//     * @param value
//     * @return 成功返回true，否则返回false
//     */
//    public boolean set(String key, Object value) {
//        return mMcc.set(key, value);
//    }
//    
//    /**
//     * 添加一个指定的值到缓存中，并设置其超时时间.
//     * @param key 键
//     * @param value 值
//     * @param expires 超时时间
//     * @return 成功返回true，否则返回false
//     */
//    public boolean set(String key, Object value, int expires) {
//        return mMcc.set(key, value, expires);
//    }
//    
//    /**
//     * 根据指定的关键字获取对象.
//     * @param key
//     * @return 返回value
//     */
//    public Object get(String key) {
//        return mMcc.get(key);
//    }
//
//    /**
//     * 将指定key的value值+1，将返回最后的value值
//     * @param key 
//     * @return 返回最后的value值
//     */
//    public long incr(String key) {
//        return mMcc.incr(key);
//    }
//    
//    /**
//     * 将指定key的value值-1，将返回最后的value值
//     * @param key 
//     * @return 返回最后的value值
//     */
//    public long decr(String key) {
//        return mMcc.decr(key);
//    }
//    
//    /**
//     * 从memcache中删除指定key的value
//     * @param key 
//     * @return 
//     */
//    public boolean remove(String key) {
//        return mMcc.delete(key);
//    }
//    
//    /**
//     * 测试方法
//     * @param args
//     */
//    public static void main(String[] args) {
//    	MemcachedClient cache = new MemcachedClient();
//        cache.set("count", 123);
//        MemcachedClient cache1 = new MemcachedClient();
//        cache1.set("count", 234) ;
//        System.out.println("count=" + cache1.get("count"));
//        System.out.println("count=" + cache.get("count"));
///*        boolean flag = cache.add("schedule_2", "0");
//        System.out.println("flag=" + flag);
//        System.out.println("schedule_2=" + cache.get("schedule_2"));
//*/    }
//}