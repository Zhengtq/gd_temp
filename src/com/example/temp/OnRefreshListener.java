package com.example.temp;

public interface OnRefreshListener {
	/**
	 * ����ˢ��
	 */
	void onDownPullRefresh();
	
	/**
	 * �������ظ���
	 * @throws Exception 
	 */
	void onLoadingMore();
}
