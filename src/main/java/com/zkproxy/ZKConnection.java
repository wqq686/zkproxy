package com.zkproxy;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

import com.hotcode.common.CommonUtils;


/**
 * @author wuqq
 * 
 */
public class ZKConnection {
	
	/** default session's timeout */
	private static final int MAX_SESSION_TIMEOUT = 30 * 60 * 1000;
	
	/** use to connect to zk */
	private Lock connectionLock = new ReentrantLock();
	
	/** zookeeper */
	private ZooKeeper zooKeeper = null ;
	
	/** zk's address*/
	private final String zkLocations ;
	
	/** session's timeout */
	private final int sessionTimeOut ;
	
	public ZKConnection(String zkLocations)  {
		this(zkLocations, MAX_SESSION_TIMEOUT) ;
	}
	
	
	public ZKConnection(String zkLocations, int sessionTimeOut) {
		if(CommonUtils.isEmpty(zkLocations) || sessionTimeOut<0 ) throw new IllegalArgumentException("{zkLocations:" + zkLocations+", sessionTimeOut="+sessionTimeOut+"}") ;
		this.zkLocations = zkLocations ;
		this.sessionTimeOut = sessionTimeOut ;
	}
	
	
	
	/**
	 * connect to zookeeper
	 * @param watcher
	 * @return
	 */
	public ZKConnection connect(Watcher watcher) {
		connectionLock.lock();
        try {
            if (zooKeeper != null) throw new IllegalStateException("ZKConnection has been started.");
            
            try {
                zooKeeper = new ZooKeeper(zkLocations, sessionTimeOut, watcher);
            } catch (IOException e) {
                throw new IllegalStateException("Unable connect to zookeeper[" + zkLocations + "]" + e);
            }
        } finally { connectionLock.unlock(); }
        return this ;
    }

	
	/**
	 * close the connection.
	 */
    public void close() {
        connectionLock.lock();
        try {
            if (zooKeeper != null) {
                zooKeeper.close();
                zooKeeper = null;
            }
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt() ;
		} finally {
            connectionLock.unlock();
        }
    }
    
    
    
    
    public String create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, mode);
    }

    public void delete(String path) throws  KeeperException, InterruptedException {
    	zooKeeper.delete(path, -1);
    }
    
    public boolean exists(String path, boolean useDefaultWatcher) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, useDefaultWatcher) != null;
    }
    
    public byte[] readData(String path, Stat stat, boolean useDefaultWatcher) throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, useDefaultWatcher, stat);
    }

	public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zooKeeper.getChildren(path, watcher) ;
	}
	
	public List<String> getChildren(String path, boolean useDefaultWatcher) throws KeeperException, InterruptedException {
		return zooKeeper.getChildren(path, useDefaultWatcher) ;
	}
	
    public void writeData(String path, byte[] data) throws KeeperException, InterruptedException {
        writeData(path, data, -1);
    }

    public Stat writeData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
    	return zooKeeper.setData(path, data, version);
    }

    public States getZookeeperState() {
        return zooKeeper != null ? zooKeeper.getState() : null;
    }

    public ZooKeeper getZookeeper() {
        return zooKeeper;
    }
}
