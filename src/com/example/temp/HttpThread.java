package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

class User {
	public int id;
	public String username;
	public String password;
	public String school;
	public String email;
	public String actTime;
	public String gender;
	public String age;
	public String interest;
	public String groups;
	public String imgId;	
}

public class HttpThread extends Thread {
	
	private String username;
	private String password;
	private String url;
	public static User userInfo;
	
	private int resultFlag = -1;
	
	public HttpThread(String username, String password, String url) {
		this.username = username;
		this.password = password;
		this.url = url + "/web/FirstServlet";
	}
	
	private void parseUserXml(String str) {
		Log.i("TAG", str);
		userInfo = new User();
		StringReader sr = new StringReader(str);
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();	// 解析器工厂
		try {
			DocumentBuilder domBuilder = domfac.newDocumentBuilder();	// Dom解析器
			Document doc = domBuilder.parse(is);	// 解析XML文档输入流，得到一个Document
			Element root = doc.getDocumentElement();	// 得到xml文档的根节点
			NodeList users = root.getChildNodes();
			if (users != null) {
				Log.i("TAG", users.getLength() + "");
				for (int i = 0; i < users.getLength(); i++) {
					Node useri = users.item(i);	// 只有一个user的信息
					for (Node node = useri.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							if (node.getNodeName().equals("result")) {
								Log.i("TAG", node.getFirstChild().getNodeValue());
								if (node.getFirstChild().getNodeValue().equals("1")) {
									resultFlag = 1;
								} else {
									resultFlag = 0;
								}
							}
							if (node.getNodeName().equals("id")) {
								userInfo.id = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("username")) {
								userInfo.username = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("password")) {
								userInfo.password = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("school")) {
								userInfo.school = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("email")) {
								userInfo.school = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("imageId")) {
								userInfo.imgId = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("actTime")) {
								userInfo.actTime = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("gender")) {
								userInfo.gender = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("age")) {
								userInfo.age = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("interest")) {
								userInfo.interest = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("groups")) {
								userInfo.groups = node.getFirstChild().getNodeValue();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void doGet() throws Exception {
		url = url + "?username=" + URLEncoder.encode(username, "utf-8") + "&password=" + password;
		System.out.println(url);
		System.out.println("发送GET请求");
		try {
			URL httpUrl = new URL(url);

			HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(5000);
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String str;
			StringBuffer sb = new StringBuffer();
			while((str = reader.readLine()) != null) {
				sb.append(str);
			}
			is.close();
			System.out.println("result:" + sb.toString());
			
			String result = sb.toString();
			
			parseUserXml(result);

		} catch (MalformedURLException e) {
			System.out.println("url error");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("io error");
			e.printStackTrace();
		}
		
	}
	
	private void doPost() {
		System.out.println("发送POST请求");
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			conn.setRequestProperty("contentType", "GBK");
			OutputStream out = conn.getOutputStream();
			String content = "username=" + username + "&password=" + password;
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String str;
			StringBuffer sb = new StringBuffer();
			while((str = reader.readLine()) != null) {
				sb.append(str);
			}
			is.close();
			System.out.println("result：" + sb.toString());
			
			String result = sb.toString();
			
			parseUserXml(result);
			
		} catch (MalformedURLException e) {
			System.out.println("url error");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("io error");
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
//		try {
//			doGet();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		doPost();
		
		Login.receiveFlag(resultFlag);
	}
	
}
