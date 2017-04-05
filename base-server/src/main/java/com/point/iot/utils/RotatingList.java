package com.point.iot.utils;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Expires keys that have not been updated in the configured number of seconds.
 * The algorithm used will take between expirationSecs and expirationSecs * (1 +
 * 1 / (numBuckets-1)) to actually expire the message.
 * 
 * get, put, remove, containsKey, and size take O(numBuckets) time to run.
 * 
 * The advantage of this design is that the expiration thread only locks the
 * object for O(1) time, meaning the object is essentially always available for
 * gets/puts.
 */
public class RotatingList<K> {
	// this default ensures things expire at most 50% past the expiration time
	private static final int DEFAULT_NUM_BUCKETS = 3;

	/**再添加重复是否删除以前重复的*/
	private boolean isDelete;

	private ConcurrentLinkedQueue<ConcurrentLinkedQueue<K>> _buckets;

	public RotatingList(int numBuckets,boolean isDelete) {
		this.isDelete=isDelete;
		if (numBuckets < 2) {
			throw new IllegalArgumentException("numBuckets must be >= 2");
		}
		_buckets = new ConcurrentLinkedQueue<ConcurrentLinkedQueue<K>>();
		for (int i = 0; i < numBuckets; i++) {
			_buckets.add(new ConcurrentLinkedQueue<K>());
		}
	}
	public RotatingList(boolean isDelete){
		this(DEFAULT_NUM_BUCKETS,isDelete);
	}
	
	public RotatingList(){
		this(DEFAULT_NUM_BUCKETS,true);
	}

	public ConcurrentLinkedQueue<K> rotate() {
		ConcurrentLinkedQueue<K> dead = _buckets.poll();
		_buckets.add(new ConcurrentLinkedQueue<K>());
		return dead;
	}

	public boolean containsKey(K key) {
		for (ConcurrentLinkedQueue<K> bucket : _buckets) {
			if (contains(key, bucket)) {
				return true;
			}
		}
		return false;
	}



	public void put(K key) {
		Iterator<ConcurrentLinkedQueue<K>> it = _buckets.iterator();
		ConcurrentLinkedQueue<K> bucket = it.next();
		bucket.add(key);
		if (isDelete) {
			while (it.hasNext()) {
				bucket = it.next();
				if (contains(key, bucket)) {
					bucket.remove(key);
				}
			}
		}
	}
	
	private boolean contains(K value,ConcurrentLinkedQueue<K> list) {
		for (K k : list) {
			if (k.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public Object remove(K key) {
		for (ConcurrentLinkedQueue<K> bucket : _buckets) {
			if (contains(key,bucket)) {
				return bucket.remove(key);
			}
		}
		return null;
	}

	public int size() {
		int size = 0;
		for (ConcurrentLinkedQueue<K> bucket : _buckets) {
			size += bucket.size();
		}
		return size;
	}
	
	
	public static void main(String[] args) {
		final RotatingList<Integer> list=new RotatingList<Integer>(10,false);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(new  Runnable() {
			public void run() {
				while (true) {
					for (Integer i: list.rotate()) {
						System.out.print(i+":");
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		
		for (int i=0 ;i<200;i++) {
			final int j=i;
			new Thread(new  Runnable() {
				public void run() {
					while (true) {
						list.put(j);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			
		}
		
		
		
	}
}
