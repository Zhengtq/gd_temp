package com.example.temp;

public interface OnFlipperDeleteListener {
	/**
	 * 下拉刷新
	 */
	void onDownPullRefresh();
	
	/**
	 * 上拉加载更多
	 * @throws Exception 
	 */
	void onLoadingMore();
	
	// 方法说明控制上下移动位置
	public void getMoveY(float moveX, float moveY);
	
	// 移动位置具体的接口
	public void onFlipping(float xPosition, float yPosition, float apartX, float apartY);
	
	// 最后的接口用来决定item最后的位置是否改变
	public void restoreView(float x, float y, boolean tag);
}
