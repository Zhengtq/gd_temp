package com.gd.model;
import java.util.HashMap;

public class ManageClientConServer {
	private static HashMap hm=new HashMap<Integer,ClientConServerThread>();
	//�Ѵ����õ�ClientConServerThread���뵽hm
	public static void addClientConServerThread(String account,ClientConServerThread ccst){
		hm.put(account, ccst);
	}
	
	//����ͨ��accountȡ�ø��߳�
	public static ClientConServerThread getClientConServerThread(String i){
		return (ClientConServerThread)hm.get(i);
	}
}
