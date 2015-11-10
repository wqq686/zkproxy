package com.zkproxy;

import org.apache.zookeeper.WatchedEvent;

/**
 * 
 * @author qingquanwu
 * 
 */
public interface NodeChangedListener {

	/**
	 * 
	 * @param event
	 */
	void onWatchedEvent(WatchedEvent event);

	
	
	/**
	 * 
	 * @author qingquanwu
	 * 
	 */
	public enum ChangedType {
		/** Node's data changed*/
		DataChanged,
		/** Node's children changed: new or dispair */
		ChildrenChanged,
		/** DataChanged & ChildrenChanged */
		BothChanged, 
		/** nothing */
		NoneChanged;

		
		/**
		 * 
		 * @param event
		 * @return
		 */
		public static ChangedType getChangedType(WatchedEvent event) {
			switch (event.getType()) {
			case None:
				return NoneChanged;
			case NodeChildrenChanged:
				return ChildrenChanged;
			default:
				return DataChanged;
			}
		}

	}

}
