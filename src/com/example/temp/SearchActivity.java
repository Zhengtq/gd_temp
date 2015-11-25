package com.example.temp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity implements SearchView.OnQueryTextListener {
	private static final String[] m = { "队名", "位置" };
	private ListView searchResultListView;
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private String xiala;
	private String query_txt;
	private SearchView sv;
	private SearchGroupAdapter sga;
	boolean flag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		spinner = (Spinner) findViewById(R.id.Spinner01);
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

		sv = (SearchView) findViewById(R.id.sv);
		// 设置该SearchView默认是否自动缩小为图标
		sv.setIconifiedByDefault(false);
		// 为该SearchView组件设置事件监听器
		sv.setOnQueryTextListener(this);
		// 设置该SearchView显示搜索按钮
		sv.setSubmitButtonEnabled(true);
		// 设置该SearchView内默认显示的提示文本
		sv.setQueryHint("查找");

		searchResultListView = (ListView) findViewById(R.id.search_group_result_listview);
		sga = new SearchGroupAdapter(this);
		searchResultListView.setAdapter(sga);
		searchResultListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startItemDetail(position - 1);
			}
		});
	}

	public void startItemDetail(int position) {
		if (position >= 0 && sga.groupDataList.size() > 0 && position < sga.groupDataList.size()) {
			Intent itemDetail = new Intent(this, ListItemDetailActivity.class);
			GroupData item = sga.groupDataList.get(position);
			itemDetail.putExtra("position", position);
			itemDetail.putExtra("simple", item);
			startActivity(itemDetail);
		}
	}

	public void startSearchActivity() {
		Intent searchIntent = new Intent(this, SearchActivity.class);
		startActivity(searchIntent);
	}

	class SpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			xiala = m[arg2];
		}
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	// 用户输入字符时激发该方法
	// @Override
	public boolean onQueryTextChange(String newText) {
		// just test
		System.out.println(newText);
		return true;
	}

	// 单击搜索按钮时激发该方法
	// @Override
	public boolean onQueryTextSubmit(String query) {
		// 实际应用中应该在该方法内执行实际查询
		// 此处仅使用Toast显示用户输入的查询内容
		// text_out.setText(query + " " + xiala);
		SearchGroupAdapter.groupDataList.clear();
		SearchGroupAdapter.img.clear();
		Toast.makeText(this, "您选择的是：" + query + " " + xiala, Toast.LENGTH_SHORT).show();
		query_txt = query;		
		System.out.println("----------------------发送请求到服务器----------------------");
		new Thread() {
			public void run() {
				// 调用loginByGet方法
				searchGroupByGet(xiala, query_txt);			
				flag = true;
			};
		}.start();

		while (!flag);
		if (flag) {
			sga.notifyDataSetChanged();
			flag = false;
		}
		return true;
	}

	public void searchGroupLogoByPost(String imgUrl) {
		String urlString = getString(R.string.server_ip) + "/web/logo_imgs/" + imgUrl + ".png";
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			Bitmap img = BitmapFactory.decodeStream(is);
			SearchGroupAdapter.img.add(img);
			is.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void searchGroupByGet(String xiala, String query_txt) {

		try {
			String spec = getString(R.string.server_ip) + "/web/SearchServlet?username="
					+ URLEncoder.encode(xiala, "UTF-8") + "&userpass=" + URLEncoder.encode(query_txt, "UTF-8");
			URL url = new URL(spec);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(5000);
			urlConnection.setConnectTimeout(5000);

			urlConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
			System.out.println(urlConnection.getResponseCode());

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

	public void parseUserXml(String result) {
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
					if (!(gd.url == null)) {
						SearchGroupAdapter.groupDataList.add(gd);
						searchGroupLogoByPost(gd.url);
						Log.i("TAG", gd.groupName);
						Log.i("TAG", gd.location);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
