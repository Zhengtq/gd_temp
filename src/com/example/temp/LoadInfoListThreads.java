package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

public class LoadInfoListThreads extends Thread {
	
	private String urlStr;
	private String urlGroupInfoStr;
	private String urlImg;
	private String groups;
	
	public LoadInfoListThreads(String url, String groups) {
		this.urlStr = url + "/web/GetMessagesServlet";
		this.urlGroupInfoStr = url + "/web/GetGroupInfoServlet";
		this.urlImg = url + "/web/logo_imgs/";
		this.groups = groups;
	}
	
	private void LoadGroupInfo(int groupId, GroupData gd) {
		URL url;
		try {
			url = new URL(this.urlGroupInfoStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			OutputStream out = conn.getOutputStream();
			String content = "groupId=" + groupId;
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			
			String result = sb.toString();	
			System.out.println(result);
			
			parseGroupInfoXml(result, gd);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
	}
	
	private void LoadGroupLogo(String imgId) {
		URL url;
		try {
			url = new URL(this.urlImg + imgId + ".png");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			InputStream is = conn.getInputStream();			
			Bitmap bm = BitmapFactory.decodeStream(is);
			is.close();
			InfoListAdapter.img.add(bm);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private void parseGroupInfoXml(String result, GroupData gd) {
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
								LoadGroupLogo(gd.url);
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
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseXml(String result) {
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
					InfoData infoData = new InfoData();
					infoData.partner = new GroupData();
					// 轮询子节点
					for (Node node = group.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) { // 子节点属性
							if (node.getNodeName().equals("messages")) {
								infoData.message = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("messages_num")) {
								infoData.messageNum = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("partners")) {
								infoData.pGroupId = Integer.parseInt(node.getFirstChild().getNodeValue());
								LoadGroupInfo(infoData.pGroupId, infoData.partner);
							}
							if (node.getNodeName().equals("id")) {
								infoData.myGroupId = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
						}
					}
					InfoListAdapter.infoList.add(infoData);
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void doPost() {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			OutputStream out = conn.getOutputStream();
			String content = "groups=" + groups;
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			
			String result = sb.toString();
			System.out.println(result);
			
			parseXml(result);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {
		doPost();
		InfoListAdapter.finishedLoadingFlag = true;
		Info.infoHandler.sendEmptyMessage(1);
	}

}
