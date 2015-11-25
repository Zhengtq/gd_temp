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

public class LoadShopInfoThreads extends Thread {
	
	private String urlStr;
	private String imgStr;
	private int startId = 0, length = 1;
	private String activity = null, location = null;
	
	public LoadShopInfoThreads(String urlStr, int startId, int length) {
		this.urlStr = urlStr + "/web/GetShopInfoServlet";
		this.imgStr = urlStr + "/web/shop_imgs/";
		this.startId = startId;
		this.length = length;
	}
	
	public LoadShopInfoThreads(String urlStr, String activity, String location, int startId, int length) {
		this.urlStr = urlStr + "/web/GetShopInfoServlet";
		this.imgStr = urlStr + "/web/shop_imgs/";
		this.startId = startId;
		this.length = length;
		this.activity = activity;
		this.location = location;
	}
	
	private void LoadShopImage(String imgId) {
		URL url;
		try {
			url = new URL(this.imgStr + imgId + ".jpg");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			InputStream is = conn.getInputStream();			
			Bitmap bm = BitmapFactory.decodeStream(is);
			is.close();
			ShopInfoListAdapter.shopLogo.add(bm);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void parseShopInfoXml(String result) {
		StringReader sr = new StringReader(result);
		InputSource iss = new InputSource(sr);

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance(); // 解析器工厂
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder(); // 得到DOM解析器
			Document doc = dombuilder.parse(iss); // 解析xml文档的输入流，得到一个Document
			Element root = doc.getDocumentElement(); // 得到xml文档的根节点
			NodeList shops = root.getChildNodes(); // 得到节点的直接点，使用NodeList接口存放所有直接点
			if (shops != null) {
				for (int i = 0; i < shops.getLength(); i++) {
					Node shop = shops.item(i);
					ShopData sd = new ShopData();
					// 轮询子节点
					for (Node node = shop.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) { // 子节点属性
							if (node.getNodeName().equals("id")) {
								sd.id = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("shopname")) {
								sd.shopName = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("rating")) {
								sd.rating = Float.parseFloat(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("location")) {
								sd.location = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("price")) {
								sd.price = Float.parseFloat(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("imgid")) {
								sd.imgUrl = node.getFirstChild().getNodeValue();
								LoadShopImage(sd.imgUrl);
							}
							if (node.getNodeName().equals("url")) {
								sd.httpUrl = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("attributes")) {
								sd.attributes = node.getFirstChild().getNodeValue();
							}
						}
					}
					
					ShopInfoListAdapter.shopInfoList.add(sd);
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
	
	public void doPost() {
		URL url;
		try {
			url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5000);
			OutputStream out = conn.getOutputStream();
			String content = "startId=" + startId + "&length=" + length + "&location=" + location + "&activity=" + activity;
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String line = "";
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			String result = sb.toString();
			
			parseShopInfoXml(result);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
	@Override
	public void run() {
		doPost();
		ShopInfoListAdapter.finishedFlag = true;
		ShoppingActivity.shopHandler.sendEmptyMessage(1);
	}

}
