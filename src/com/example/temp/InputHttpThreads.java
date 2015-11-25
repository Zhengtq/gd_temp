package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class InputHttpThreads extends Thread {
	private String urlStr;
	private int position;
	private String queryId = null;
	private String flagStr = null;
	private int minRow = -1, maxRow = -1;
	private String groupId = null;

	public InputHttpThreads(String url) {
		this.urlStr = url;
	}
	
	public InputHttpThreads(String url, String groupId, int minRow, int maxRow) {
		this.urlStr = url;
		this.groupId = groupId;
		this.minRow = minRow;
		this.maxRow = maxRow;
		this.queryId = null;
	}

	public InputHttpThreads(String url, int position) {
		this.urlStr = url;
		this.position = position;
	}

	public InputHttpThreads(String url, String str) {
		this.urlStr = url;
		this.queryId = str;
		this.groupId = null;
		this.minRow = 0;
		this.maxRow = 0;
	}
	
	public InputHttpThreads(String url, int position, String flagStr) {
		this.urlStr = url;
		this.position = position;
		this.flagStr = flagStr;
	}

	private void GetNetInputStream() {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			OutputStream out = conn.getOutputStream();
			String content = "groupId=" + groupId + "&queryId=" + queryId + "&minRow=" + minRow + "&maxRow=" + maxRow;
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String result = sb.toString();
			StringReader sr = new StringReader(result);
			InputSource iss = new InputSource(sr);

			DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance(); // 解析器工厂
			try {
				DocumentBuilder dombuilder = domfac.newDocumentBuilder(); // 得到DOM解析器
				Document doc = dombuilder.parse(iss); // 解析xml文档的输入流，得到一个Document
				Element root = doc.getDocumentElement(); // 得到xml文档的根节点
				NodeList groups = root.getChildNodes(); // 得到节点的直接点，使用NodeList接口存放所有直接点
				if (groups != null) {
					for (int i = 0; i < groups.getLength(); i++) {
						Node group = groups.item(i);
						GroupData gd = new GroupData();
						// 轮询子节点
						for (Node node = group.getFirstChild(); node != null; node = node.getNextSibling()) {
							if (node.getNodeType() == Node.ELEMENT_NODE) { // 子节点属性
								if (node.getNodeName().equals("groupname")) {
									gd.groupName = node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("location")) {
									gd.location = node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("starttime")) {
									gd.freeTime = node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("endtime")) {
									gd.freeTime = gd.freeTime + " ~ " + node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("logoname")) {
									gd.url = node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("activity")) {
									gd.activity = node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("groupmembers")) {
									gd.groupmembers = node.getFirstChild().getNodeValue();
								}
								if (node.getNodeName().equals("id")) {
									gd.id = Integer.parseInt(node.getFirstChild().getNodeValue());
								}
							}
						}
						if (!(gd.url == null) && queryId == null) {
							GroupListAdapter.groupDataList.add(gd);
							Home.homeHandler.sendEmptyMessage(2);
							Log.i("TAG", gd.groupName);
							Log.i("TAG", gd.location);
						}
						if (queryId != null) {
							MyGroupListAdapter.groupDataList.add(gd);
							MyGroups.myGroupHandler.sendEmptyMessage(2);
							Log.i("TAG", gd.groupName);
						}
					}
				}

			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (queryId == null) {
			GroupListAdapter.flag = true;
			Home.homeHandler.sendEmptyMessage(3);
		} else {
			MyGroupListAdapter.flag = true;
			MyGroups.myGroupHandler.sendEmptyMessage(1);
		}
	}

	private void GetNetImage() {
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();						
			if (flagStr == null) {
				System.out.println(GroupListAdapter.img.size());
				Bitmap img = BitmapFactory.decodeStream(is);
				GroupListAdapter.img.add(img);
				System.out.println(GroupListAdapter.img.size());
				GroupListAdapter.flag = true;
				Home.homeHandler.sendEmptyMessage(2);
			} else {
				Bitmap img = BitmapFactory.decodeStream(is);
				MyGroupListAdapter.img.add(img);
				MyGroupListAdapter.flag = true;
				MyGroups.myGroupHandler.sendEmptyMessage(2);
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (urlStr.indexOf("png") == -1) {
			GetNetInputStream();
		} else {
			GetNetImage();
		}
	}

}
