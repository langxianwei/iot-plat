package com.point.iot.base.timer;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class ActionManager {
	private static ActionManager mInstance;
	Logger logger = Logger.getLogger(ActionManager.class);
	/**
	 * 动作序列，按执行时间排序
	 */
	private LinkedList<Action> mList;
	/**
	 * 存储动作的哈希表，用于对Action的手动删除
	 */
	private Map<Object, Action> mHash;

	public static ActionManager getInstance() {
		if (mInstance == null) {
			synchronized (ActionManager.class) {
				if (mInstance == null) {
					mInstance = new ActionManager();
				}
			}
		}
		return mInstance;
	}

	public ActionManager() {
		mList = new LinkedList<Action>();
		mHash = new ConcurrentHashMap<Object, Action>();
	}

	/**
	 * 在线程中循环调用
	 */
	public void run() {
		Action act = getAction();
		while (act != null) {
			act.execute();
			act = getAction();
		}
	}

	/**
	 * 得到一个动作
	 * 
	 * @return
	 */
	private Action getAction() {
		synchronized (mList) {
			if (mList.size() == 0) {
				return null;
			}
			Action act = mList.get(0);
			if (act.getExecuteTime() > System.currentTimeMillis()) {
				return null;
			}
			mList.remove(0);
			mHash.remove(act.getKey());
			return act;
		}
	}

	/**
	 * 插入定时动作
	 * 
	 * @param key
	 *            Action的key，执行前手动删除Action时使用。
	 * @param listener
	 *            定时动作监听器
	 * @param delay
	 *            延迟毫秒数
	 * @param obj
	 *            对象参数
	 */
	public void insertAction(Object key, TimerActionListener listener, long delay, Object obj) {
		Action act = new Action(key, listener, delay, obj);
		synchronized (mList) {
			// 用二分法找到合适的插入位置
			int nPos = getInsertPos(act.getExecuteTime(), 0, mList.size() - 1);
			mList.add(nPos, act);
			if (mHash.containsKey(key)) {
				logger.error("hash表中已经存在 " + key);
			}
			mHash.put(key, act);
		}
	}

	/**
	 * 查询是否存在这个任务
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(Object key) {
		return mHash.containsKey(key);
	}

	/**
	 * 手动终止一个定时任务
	 * 
	 * @param key
	 * @return 
	 */
	public boolean abortAction(Object key) {
		boolean b = false;
		synchronized (mList) {
			Action act = mHash.get(key);
			if (act != null) {
				b = mList.remove(act);
				mHash.remove(key);
				return b;
			}
		}
		return b;
	}
	
	public long abortActionGetRemainTime(Object key) {
		long result = 0;
		synchronized (mList) {
			Action act = mHash.get(key);
			if(act != null) {
				result = act.getExecuteTime() - System.currentTimeMillis();
				mList.remove(act);
				mHash.remove(key);
			}
		}
		return result;
	}

	/**
	 * 重新延迟任务
	 * 
	 * @param key
	 * @param delay
	 */
	public void redelayAction(Object key, long delay) {
		synchronized (mList) {
			Action act = mHash.get(key);
			if(act==null)return;
			act.setDelay(delay);
			mList.remove(act);
			// 用二分法找到合适的插入位置
			int nPos = getInsertPos(act.getExecuteTime(), 0, mList.size() - 1);
			mList.add(nPos, act);
		}
	}

	/**
	 * 手动终止一个定时任务
	 * 
	 * @param key
	 */
	public void removeAction(Object key) {
		synchronized (mList) {
			Action act = mHash.get(key);
			if (act != null) {
				mHash.remove(key);
				mList.remove(act);
			}
		}
	}
	
	public int getActionCnt() {
		return mList.size();
	}


	/**
	 * 找到合适的插入位置
	 * 
	 * @param nExecTime
	 * @param low
	 * @param high
	 * @return
	 */
	private int getInsertPos(long nExecTime, int low, int high) {
		if (mList.size() > 0 && nExecTime >= mList.peekLast().getExecuteTime()) {
			return mList.size();
		}
		
		int idx = 0;
		for (Action act : mList) {
			if (act.getExecuteTime() > nExecTime) {
				return idx; 
			}
			idx ++;
		}
		return idx;
		
		
//		if (mList.size() == 0 || nExecTime == 0) {
//			return 0;
//		}
//		if (high - low <= 1) {
//			if (nExecTime <= mList.get(low).getExecuteTime()) {
//				return low;
//			} else if (nExecTime >= mList.get(high).getExecuteTime()) {
//				return high + 1;
//			} else {
//				return high;
//			}
//		}
//		if (nExecTime <= mList.get(low).getExecuteTime()) {
//			return low;
//		}
//		if (nExecTime >= mList.get(high).getExecuteTime()) {
//			return high + 1;
//		}
//		int mid = (low + high) / 2;
//
//		if (nExecTime < mList.get(mid).getExecuteTime()) {
//			return getInsertPos(nExecTime, low, mid);
//		} else if (nExecTime > mList.get(mid).getExecuteTime()) {
//			return getInsertPos(nExecTime, mid, high);
//		} else {
//			return mid;
//		}
	}
}
