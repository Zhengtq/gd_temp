package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.example.temp.SearchActivity.SpinnerSelectedListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class SearchShopActivity extends Activity implements SearchView.OnQueryTextListener{

	private static final String[] m = { "类型", "位置" };
	private ListView searchResultListView;
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private String xiala;
	private String query_txt;
	private SearchView shopSv;
	private SearchShopAdapter ssa;
	boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_shop);

		spinner = (Spinner) findViewById(R.id.shop_sp);
		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m);
		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		spinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		// 设置默认值
		spinner.setVisibility(View.VISIBLE);

		shopSv = (SearchView) findViewById(R.id.shop_sv);
		// 设置该SearchView默认是否自动缩小为图标
		shopSv.setIconifiedByDefault(false);
		// 为该SearchView组件设置事件监听器
		shopSv.setOnQueryTextListener(this);
		// 设置该SearchView显示搜索按钮
		shopSv.setSubmitButtonEnabled(true);
		// 设置该SearchView内默认显示的提示文本
		shopSv.setQueryHint("查找");

		searchResultListView = (ListView) findViewById(R.id.search_shop_result_listview);
		ssa = new SearchShopAdapter(this);
		searchResultListView.setAdapter(ssa);

	}

	// 使用数组形式操作
	class SpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			xiala = m[arg2];
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		SearchShopAdapter.shopInfoList.clear();
		SearchShopAdapter.shopLogo.clear();
		Toast.makeText(this, "您选择的是：" + query + " " + xiala, Toast.LENGTH_SHORT).show();
		query_txt = query;		
		System.out.println("----------------------发送请求到服务器----------------------");
		new Thread() {
			public void run() {
				// 调用loginByGet方法
				searchShopByGet(xiala, query_txt);			
				flag = true;
			};
		}.start();

		while (!flag);
		if (flag) {
			ssa.notifyDataSetChanged();
			flag = false;
		}
		return true;
	}
	
	public void searchShopByGet(String xiala, String query_txt) {

		try {
			// 设置请求的地址 通过URLEncoder.encode(String s, String enc)
			// 使用指定的编码机制将字符串转换为 application/x-www-form-urlencoded 格式
			String spec = getString(R.string.server_ip) + "/web/SearchShopInfoServlet?type="
					+ URLEncoder.encode(xiala, "UTF-8") + "&query=" + URLEncoder.encode(query_txt, "UTF-8");
			// 根据地址创建URL对象(网络访问的url)
			URL url = new URL(spec);
			// url.openConnection()打开网络链接
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");// 设置请求的方式
			urlConnection.setReadTimeout(5000);// 设置超时的时间
			urlConnection.setConnectTimeout(5000);// 设置链接超时的时间
			// 设置请求的头
			urlConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
			System.out.println(urlConnection.getResponseCode());
			// 获取响应的状态码 404 200 505 302
			if (urlConnection.getResponseCode() == 200) {
				// 获取响应的输入流对象
				InputStream is = urlConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
				String str;
				StringBuffer sb = new StringBuffer();
				while ((str = reader.readLine()) != null) {
					sb.append(str);
				}
				is.close();
				System.out.println("result:" + sb.toString());

				String result = sb.toString();

				parseUserXml(result);
			} else {
				System.out.println("------------------链接失败-----------------");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void LoadShopImage(String imgId) {
		URL url;
		try {
			url = new URL(getString(R.string.server_ip) + "/web/shop_imgs/" + imgId + ".jpg");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			InputStream is = conn.getInputStream();			
			Bitmap bm = BitmapFactory.decodeStream(is);
			is.close();
			SearchShopAdapter.shopLogo.add(bm);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void parseUserXml(String result) {
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
					
					SearchShopAdapter.shopInfoList.add(sd);
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

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}

}
