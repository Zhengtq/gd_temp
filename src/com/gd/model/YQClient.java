package com.gd.model;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;


import com.gd.common.YQMessage;
import com.gd.common.YQMessageType;

import android.content.Context;
import javabean.UserChat;


public class YQClient  {
	private Context context;
	private String serverIp;
	public Socket s = new Socket();
	
	public YQClient(Context context, String serverIp){
		this.context = context;
		this.serverIp = serverIp;
	}
	
	public boolean sendLoginInfo(Object obj){

		boolean b = false;
		try {
			s = new Socket();
			try{							
				SocketAddress rermoteAddr = new InetSocketAddress(serverIp, 8081);
				s.connect(rermoteAddr, 2000);		
			}catch(SocketTimeoutException e){		
				return false;
			}		

			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());						
			oos.writeObject(obj);			

			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			YQMessage ms = (YQMessage)ois.readObject();

			if(ms.getType().equals(YQMessageType.SUCCESS)){
				//创建一个该账号和服务器保持连接的线程
				ClientConServerThread ccst = new ClientConServerThread(context,s);
				//启动该通信线程
				ccst.start();
				//加入到管理类中			
				ManageClientConServer.addClientConServerThread(((UserChat)obj).getUsername(), ccst);
				b = true;

			}else if(ms.getType().equals(YQMessageType.FAIL)){
				b = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return b;
	}

}
