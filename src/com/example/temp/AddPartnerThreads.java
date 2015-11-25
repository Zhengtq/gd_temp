package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;

public class AddPartnerThreads extends Thread {
	
	String url;
	private int mid, pid;
	
	public AddPartnerThreads(String url, int mid, int pid) {
		this.url = url + "/web/AddPartnersServlet";
		this.mid = mid;
		this.pid = pid;
	}
	
	private void doGet() {
		
	}
	
	private void doPost() {
		try {
			URL httpUrl = new URL(url);

			HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			conn.setRequestProperty("contentType", "GBK");
			OutputStream out = conn.getOutputStream();
			String content = "myid=" + mid + "&partnerid=" + pid;
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String str;
			StringBuffer sb = new StringBuffer();
			while((str = reader.readLine()) != null) {
				sb.append(str);
			}
			is.close();
			System.out.println("result£º" + sb.toString());
			
			ListItemDetailActivity.result = sb.toString();
			
		} catch (MalformedURLException e) {
			System.out.println("url error");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("io error");
			e.printStackTrace();
		}
	}
	
	public void run() {
		doPost();
	}
}
