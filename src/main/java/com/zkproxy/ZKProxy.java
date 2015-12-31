package com.zkproxy;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import com.zkproxy.NodeChangedListener.ChangedType;

public interface ZKProxy {
	
	
	/**
	 * @param path
	 * @param klass
	 */
	<T> T get(String path, Class<T> klass) ;

	
	/**
	 * @param path
	 * @param stat
	 * @param klass
	 * @return
	 */
	<T> T get(String path, Stat stat, Class<T> klass) ;
	
	
	/**
	 * @param path
	 * @param klass
	 */
	<T> T blockingGet(String path, Class<T> klass) ;
	
	
	/**
	 * @param path
	 * @param stat
	 * @param klass
	 * @return
	 */
	<T> T blockingGet(String path, Stat stat, Class<T> klass) ;
	
	
	/**
	 * @param path
	 * @param value
	 * @param mode
	 */
	<T> String create(String path, T value, CreateMode mode) ;
	
	
	/**
	 * @param path
	 * @param value
	 * @param mode
	 */
	<T> String blockingCreate(String path, T value, CreateMode mode) ;
	
	
	/**
	 * 
	 * @param path
	 * @param value
	 * @return
	 */
	<T> String blockingCreatePersistent(String path, T value) ;
	
	
	/**
	 * 
	 * @param path
	 * @param value
	 * @return
	 */
	<T> String blockingCreateEphemeral(String path, T value) ;
	
	
	/**
	 * 
	 * @param path
	 */
	void delete(String path) ;
	
	
	/**
	 * @param path
	 */
	void blockingDelete(String path) ;
	
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	List<String> getChildren(String path) ; 
	
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	List<String> blockingGetChildren(String path) ;
	
	
	/**
	 * 
	 * @param path
	 * @param klass
	 * @return
	 */
	<T> List<T> getChildrenObject(String path, Class<T> klass) ; 
	
	
	/**
	 * 
	 * @param path
	 * @param klass
	 * @return
	 */
	<T> List<T> blockingGetChildrenObject(String path, Class<T> klass) ;
	
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	boolean exists(String path) ;
	
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	boolean blockingExists(String path) ;
	
	
	/**
	 * 消息订阅
	 * @param path
	 * @param chgType
	 * @param listener
	 */
	void subscribeChanges(String path, ChangedType chgType, NodeChangedListener listener);
	
	
	/**
	 * 解除对节点的监听
	 * 
	 * @param path
	 *            节点路径
	 * @param chgType
	 *            移除类型
	 */
	void unsubscribeChanges(String path, ChangedType chgType) ;
	
	
	/**
	 * 是否订阅
	 * @param path
	 * @return
	 */
	boolean subscribed(String path) ;
	
	
	/**
	 * 
	 * @return
	 */
	boolean isConnected() ;
	
	
	/**
	 * 
	 */
	void doClose() ;
	
	
	
}
