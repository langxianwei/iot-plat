package com.point.iot.base.timer;


public class Action {
	private TimerActionListener mListener;
	private Object mObj;
	private long mExecuteTime;
	private Object mKey;
	
	/**
	 * 构造函数
	 * @param listener 定时动作的监听类
	 * @param delay 延迟的毫秒数
	 * @param obj 参数对象
	 */
	public Action(Object key, TimerActionListener listener, long delay, Object obj) {
		mListener = listener;
		mExecuteTime = System.currentTimeMillis() + delay;
		mObj = obj;
		mKey = key;
	}
	/**
	 * 设置多少毫秒后执行
	 * @param delay
	 */
	public void setDelay (long delay) {
		mExecuteTime += delay;
	}
	
	/**
	 * 得到执行时间点
	 * @return
	 */
	public long getExecuteTime() {
		return mExecuteTime;
	}
	/**
	 * 得到key
	 * @return
	 */
	public Object getKey() {
		return mKey;
	}
	/**
	 * 执行Action
	 */
	public void execute() {
		mListener.onTimerActionExcute(mObj);
	}
	
	public String toString() {
		return " mListener:" + mListener + " ExecuteTime:" + mExecuteTime + " ParamObj:" + mObj;
	}
}
