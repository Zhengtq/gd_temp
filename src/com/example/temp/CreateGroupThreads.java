package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Bundle;
import android.os.Message;

public class CreateGroupThreads extends Thread {

	GroupData input;
	String urlStr;

	public CreateGroupThreads(GroupData in, String url) {
		this.input = in;
		this.urlStr = url + "/web/InsertGroupInfoServlet";
	}

	public void doGet() {

	}

	public void doPost() {
		try {
			System.out.println(urlStr);
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(50000);			
			OutputStream out = conn.getOutputStream();
			String content = "<?xml version='1.0' encoding='gbk'?><groups>";
			content += "<group><groupname>" + input.groupName + "</groupname><starttime>" + input.freeTimeStart
					+ "</starttime><endtime>" + input.freeTimeStop + "</endtime><location>" + input.location
					+ "</location><activity>" + input.activity + "</activity><groupmembers>" + input.groupmembers
					+ "</groupmembers></group>";
			content += "</groups>";
			System.out.println(content);
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String str;
			StringBuffer sb = new StringBuffer();
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
			
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", sb.toString());
			msg.setData(b);
			CreateGroupActivity.createGroupHandler.sendMessage(msg);
			is.close();
		} catch (MalformedURLException e) {
			System.out.println("url error");
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		doPost();
	}

}
