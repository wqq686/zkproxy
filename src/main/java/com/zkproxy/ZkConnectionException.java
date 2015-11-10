package com.zkproxy;

public class ZkConnectionException  extends RuntimeException {
	
	static final long serialVersionUID = -1848914611122233416L;
	
    public ZkConnectionException() {
        super();
    }

    
    public ZkConnectionException(String s) {
        super(s);
    }

    public ZkConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkConnectionException(Throwable cause) {
        super(cause);
    }

    
}
