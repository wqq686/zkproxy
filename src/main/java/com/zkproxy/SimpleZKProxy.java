package com.zkproxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import com.hotcode.common.CommonUtils;



public class SimpleZKProxy extends AbstractZKProxy {
	
	public SimpleZKProxy(String addresses) {
		super(addresses) ;
	}
	
	@Override
	public <T> T get(String path, Class<T> klass) {
		return get(path, null, klass) ;
	}
	
	
	@Override
	public <T> T get(String path, Stat stat, Class<T> klass) {
		try 
		{
			byte[] bytes = getZKConnection().readData(path, stat, subscribed(path)) ;
			return bytes2Object(bytes, klass) ;
		} catch(Exception e) { throw convertException(e) ; }
	}
	
	
	@Override
	public <T> T blockingGet(String path, Class<T> klass) {
		return blockingGet(path, null, klass) ;
	}
	
	
	@Override
	public <T> T blockingGet(String path, Stat stat, Class<T> klass) {
		while (true) {
			try 
			{
				return get(path, stat, klass) ;
			} catch(Exception e) {
				if(!(e instanceof ZkConnectionException)) {
					throw e ;
				}
			}
		}
	}
	
	
	
	
	@Override
	public <T> String create(String path, T value, CreateMode mode) {
		try 
		{
			byte[] bytes = object2Bytes(value) ;
			return getZKConnection().create(path, bytes, mode);
		} catch(Exception e) { throw convertException(e) ; }
	}


	@Override
	public <T> String blockingCreate(String path, T value, CreateMode mode) {
		while (true) {
			try {
				return create(path, value, mode) ;
			} catch(Exception e) {
				if(!(e instanceof ZkConnectionException)) {
					throw e ;
				}
			}
		}
	}

	

	@Override
	public void delete(String path) {
		try {
			getZKConnection().delete(path);
		} catch(Exception e) { throw convertException(e) ; }
		
	}

	
	
	@Override
	public void blockingDelete(String path) {
		while (true) {
			try 
			{
				delete(path); break ;
			} catch(Exception e) {
				if(!(e instanceof ZkConnectionException)) {
					throw e ;
				}
			}
		}
	}

	
	@Override
	public List<String> getChildren(String path) {
		try {
			return getZKConnection().getChildren(path, subscribed(path)) ;
		} catch(Exception e) { throw convertException(e) ; }
	}

	
	
	
	@Override
	public List<String> blockingGetChildren(String path) {
		while (true) {
			try {
				return getChildren(path) ;
			} catch(Exception e) {
				if(!(e instanceof ZkConnectionException)) {
					throw e ;
				}
			}
		}
	}
	
	
	
	
	@Override
	public <T> List<T> getChildrenObject(String path, Class<T> klass) {
		List<T> ret = new ArrayList<>() ;
		try {
			List<String> child = getZKConnection().getChildren(path, subscribed(path)) ;
			if(!CommonUtils.isEmpty(child)) {
				for(String name : child) {
					String cpath = path + "/" + name ;
					T e = get(cpath, klass) ;
					ret.add(e) ;
				}
			}
		} catch(Exception e) { throw convertException(e) ; }
		return ret ;
	}

	
	
	@Override
	public <T> List<T> blockingGetChildrenObject(String path, Class<T> klass) {
		while (true) {
			try {
				return getChildrenObject(path, klass) ;
			} catch(Exception e) {
				if(!(e instanceof ZkConnectionException)) {
					throw e ;
				}
			}
		}
	}
	
	
	
	
	@Override
	public boolean exists(String path) {
		try {
			return getZKConnection().exists(path, subscribed(path)) ;
		} catch(Exception e) { throw convertException(e) ; }
	}
	
	
	
	
	
	@Override
	public boolean blockingExists(String path) {
		while (true) {
			try {
				return exists(path) ;
			} catch(Exception e) {
				if(!(e instanceof ZkConnectionException)) {
					throw e ;
				}
			}
		}
	}

	
	
	
	
	
	@Override
	public <T> String blockingCreatePersistent(String path, T value) {
		return blockingCreate(path, value, CreateMode.PERSISTENT) ;
	}

	
	
	
	@Override
	public <T> String blockingCreateEphemeral(String path, T value) {
		return blockingCreate(path, value, CreateMode.EPHEMERAL) ;
	}

	
	



}
