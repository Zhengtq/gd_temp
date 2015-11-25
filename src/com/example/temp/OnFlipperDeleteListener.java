package com.example.temp;

public interface OnFlipperDeleteListener {
	/**
	 * ����ˢ��
	 */
	void onDownPullRefresh();
	
	/**
	 * �������ظ���
	 * @throws Exception 
	 */
	void onLoadingMore();
	
	// ����˵�����������ƶ�λ��
	public void getMoveY(float moveX, float moveY);
	
	// �ƶ�λ�þ���Ľӿ�
	public void onFlipping(float xPosition, float yPosition, float apartX, float apartY);
	
	// ���Ľӿ���������item����λ���Ƿ�ı�
	public void restoreView(float x, float y, boolean tag);
}
