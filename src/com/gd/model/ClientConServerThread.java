/**
 * �ͻ��˺ͷ������˱���ͨ�ŵ��߳�
 * ���ϵض�ȡ����������������
 */
package com.gd.model;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.gd.common.YQMessage;
import com.gd.common.YQMessageType;

import android.content.Context;
import android.content.Intent;

public class ClientConServerThread extends Thread {
	private Context context;
	private  Socket s;
	public Socket getS() {return s;}
	public ClientConServerThread(Context context,Socket s){
		this.context=context;
		this.s=s;
	}
	
	@Override
	public void run() {
		while(true){
			ObjectInputStream ois = null;
			YQMessage m;
			try {
				ois = new ObjectInputStream(s.getInputStream());
				m=(YQMessage) ois.readObject();
				if(m.getType().equals(YQMessageType.COM_MES)){//�������������
					//�Ѵӷ�������õ���Ϣͨ���㲥����
					Intent intent = new Intent("org.yhn.yq.mes");
					String[] message=new String[]{
						m.getSender()+"",
						m.getSenderNick(),
						m.getSenderAvatar()+"",
						m.getContent(),
						m.getSendTime()};
					intent.putExtra("message", message);
					context.sendBroadcast(intent);					
				}
			} catch (Exception e) {
				try {
					if(s!=null){
						s.close();
					}
				} catch (IOException e1) {
				}
			}
		}
	}
	
}