package com.zkproxy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper.States;

import com.alibaba.fastjson.JSON;
import com.hotcode.common.CommonUtils;
import com.hotcode.common.NamedThreadFactory;
import com.zkproxy.NodeChangedListener.ChangedType;

public abstract class AbstractZKProxy implements ZKProxy, Watcher {

	/** */
	String zookeeperAddresses ;
	
	/** */
	volatile ZKConnection zkconn ;
	
	/** */
	long connectTimeout = 5000L ;
	
	/** */
	volatile CountDownLatch stateLatch = new CountDownLatch(1);
	
	/** */
	final ConcurrentMap<String, Set<NodeChangedListener>> bothChangedMap = new ConcurrentHashMap<String, Set<NodeChangedListener>>();
	
	/** */
	final ConcurrentMap<String, Set<NodeChangedListener>> dataChangedMap = new ConcurrentHashMap<String, Set<NodeChangedListener>>();
	
	/** */
	final ConcurrentMap<String, Set<NodeChangedListener>> childChangedMap = new ConcurrentHashMap<String, Set<NodeChangedListener>>();
	
	/** */
	final ExecutorService eventWorker = Executors.newCachedThreadPool(NamedThreadFactory.newPoolThreadFactory("zkproxy-event-worker"));
	
	
	
	/**
	 * 
	 * @param addresses
	 */
	AbstractZKProxy(String addresses) {
		this.zookeeperAddresses = addresses ;
		connect();
	}
	
	
	
	
	/**
	 * @return
	 */
	ZKConnection getZKConnection() {
		if(!isConnected()) {
			connect() ;
		}
		return zkconn ;
	}
	
	
	
	
	/**
	 * 
	 */
	@Override
	public boolean isConnected() {
		return zkconn != null && zkconn.getZookeeperState() == States.CONNECTED ;
	}
	
	
	
	
	/**
	 *  
	 */
	synchronized ZKProxy connect() {
		ZKConnection zkconn = this.zkconn ;
		if(zkconn==null || zkconn.getZookeeperState() != States.CONNECTED ) 
		{
			if(zkconn!=null) zkconn.close();
			
			zkconn = new ZKConnection(zookeeperAddresses) ;
			//1. new state latch
			stateLatch = new CountDownLatch(1);
			//2. 
			zkconn.connect(this) ;
			//3. 
			if(!waitingConnected()) {
				zkconn.close(); 
				throw new IllegalStateException("connect to["+zookeeperAddresses+"] timeout.");
			}
			
			//4. zkconn
			this.zkconn = zkconn ;
		}
		return this ;
	}
	

	/**
	 * 
	 */
	@Override
	public void doClose() {
		getZKConnection().close(); 
	}
	
	
	
	
	@Override
	public void subscribeChanges(String path, ChangedType chgType, NodeChangedListener listener) {
		if(path==null || listener==null) throw new NullPointerException("the path="+path+" or listener="+listener+" is null") ;
		
		if(chgType==null || ChangedType.NoneChanged==chgType) return ;
		
		ConcurrentMap<String, Set<NodeChangedListener>> listenerMap = null;
		
		switch (chgType) {
			case BothChanged: listenerMap = bothChangedMap; break;
			case DataChanged: listenerMap = dataChangedMap; break;
			case ChildrenChanged: listenerMap = childChangedMap; break;
			default: return ;
		}
		
		path = path.intern() ;
		Set<NodeChangedListener> listenerSet = listenerMap.get(path);
		synchronized (path) 
		{
			if (listenerSet == null) {
				listenerSet = new HashSet<NodeChangedListener>();
				Set<NodeChangedListener> t = listenerMap.putIfAbsent(path, listenerSet);
				if (t != null) listenerSet = t;
			}
			listenerSet.add(listener);
		}
		
		doWatch(path, chgType) ;
	}
	
	
	

	@Override
	public void unsubscribeChanges(String path, ChangedType chgType) {
		if( path == null || chgType == null || chgType == ChangedType.NoneChanged ) return ;
		
		ConcurrentMap<String, Set<NodeChangedListener>> listenerMap = null;
		switch (chgType) {
			case BothChanged: listenerMap = bothChangedMap; break;
			case DataChanged: listenerMap = dataChangedMap; break;
			case ChildrenChanged: listenerMap = childChangedMap; break;
			default: return ;
		}
		
		if (listenerMap != null) listenerMap.remove(path);
		
	}

	
	
	
	@Override
	public boolean subscribed(String path) {
		return path==null ? false : bothChangedMap.containsKey(path) || dataChangedMap.containsKey(path) || childChangedMap.containsKey(path) ;
	}
	
	
	
	
	@Override
	public void process(WatchedEvent event) {
		final String path = event.getPath();
		if( path == null || event.getState()!=KeeperState.SyncConnected ) {
			processConnChanged(event); 
			return;
		}
		eventWorker.execute(new WatchedEventWorker(event)) ;
	}
	
	
	
	
	/**
	 * @return
	 */
	boolean waitingConnected() {
		try {
			if(stateLatch.getCount()>0){
				return this.stateLatch.await(connectTimeout, TimeUnit.MILLISECONDS);
			}
			return true ;
		}catch(Exception e) { throw new IllegalStateException(e) ;}
	}
	
	
	
	
	
	
	/**
	 * 
	 * @param path
	 * @param chgType
	 */
	void doWatch(String path, ChangedType chgType) {
		switch (chgType) {
			case DataChanged: exists(path); break;
			case ChildrenChanged: if(exists(path)) getChildren(path); break;
			case BothChanged: if(exists(path)) getChildren(path); break ;
			default: break;
		}
	}
	
	
	
	
	
	/**
	 * 
	 * @param pathSet
	 * @param chgType
	 */
	void doWatch(Set<String> pathSet, ChangedType chgType) {
		if(!CommonUtils.isEmpty(pathSet)){
			for(String path : pathSet) try{doWatch(path, chgType) ;}catch (Exception e) {e.printStackTrace() ;}
		}
	}
	
	
	
	/**
	 * 
	 * @param event
	 */
	void processConnChanged(WatchedEvent event) {
		KeeperState newState = event.getState();
		switch (newState) {
			case SyncConnected: 
			{
				doWatch(bothChangedMap.keySet(), ChangedType.BothChanged) ;
				doWatch(dataChangedMap.keySet(), ChangedType.DataChanged) ;
				doWatch(childChangedMap.keySet(), ChangedType.ChildrenChanged) ;
				if(stateLatch.getCount()>0) {
					stateLatch.countDown();
				}
				break ;
			}
			default: connect();
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param e
	 * @return
	 */
	protected RuntimeException convertException(Exception e) {
		if( (e instanceof ConnectionLossException) || (e instanceof SessionExpiredException) ) {
			return new ZkConnectionException(e) ;
		}
		return new IllegalStateException(e) ;
	}
	
	
	
	
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	static byte[] object2Bytes(Object value) {
		if (value == null) {
			throw new NullPointerException("value is null.");
		}
		return JSON.toJSONBytes(value);
	}

	
	
	/**
	 * 
	 * @param bytes
	 * @param klass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T> T bytes2Object(byte[] bytes, Class<T> klass) {
		return (T) (bytes == null ? null : JSON.parseObject(bytes, klass));
	}

	
	/**
	 * 
	 * @param key
	 * @return
	 */
	static String filterNull(String key) {
		return key == null ? "null" : key;
	}
	
	
	class WatchedEventWorker implements Runnable {
			final WatchedEvent event;
			
			WatchedEventWorker(WatchedEvent event) {
				this.event = event;
			}

			private void doListener(String path, ChangedType chgType, ConcurrentMap<String, Set<NodeChangedListener>> changedMap) {
				Set<NodeChangedListener> listenerSet = changedMap.get(path) ;
				if(!CommonUtils.isEmpty(listenerSet)) {
					doWatch(path, chgType) ;
					for (NodeChangedListener listener : listenerSet)  listener.onWatchedEvent(event);
				}
			}
			
			
			@Override
			public void run() {
				final String path = event.getPath();
				if(path!=null) {
					doListener(path, ChangedType.BothChanged, bothChangedMap) ;
					doListener(path, ChangedType.DataChanged, dataChangedMap) ;
					doListener(path, ChangedType.ChildrenChanged, childChangedMap) ;					
				}			
			}
			
		}
}
