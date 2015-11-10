package com.zkproxy;


/**
 * 
 * @author wuqq
 *
 */
public class ZkProxyFactory {
	
	/** */
	private static String zookeeperLocation = "127.0.0.1:2188" ;
	
	/** */
	private static ZKProxy instance ;
	
	
	
	/**
	 * 
	 * @param zookeeperLocation
	 * @return
	 */
	public static synchronized ZKProxy start(String zookeeperLocation) {
		if(instance == null) 
		{
			synchronized (ZkProxyFactory.class) {
				if(instance==null) 
				{
					ZkProxyFactory.zookeeperLocation = zookeeperLocation ;
					instance = new SimpleZKProxy(zookeeperLocation) ;
				}
			}
		}
		return instance ;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static ZKProxy getProxy() {
		if(instance == null) {
			throw new IllegalStateException(ZkProxyFactory.class.getName() +" hasn't been started") ;
		}
		return instance ;
	}
	
	
	
	
	/**
	 * 
	 * @return
	 */
	public static String getZookeeperLocation() {
		return zookeeperLocation ;
	}


	
	
	
	@Override
	public String toString() {
		return "ZkProxyFactory ["+zookeeperLocation+"]";
	}
	
	
	
	

}
