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

	private static final String[] m = { "����", "λ��" };
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
		// ����ѡ������ArrayAdapter��������
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m);
		// ���������б�ķ��
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
		spinner.setAdapter(adapter);
		// ����¼�Spinner�¼�����
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		// ����Ĭ��ֵ
		spinner.setVisibility(View.VISIBLE);

		shopSv = (SearchView) findViewById(R.id.shop_sv);
		// ���ø�SearchViewĬ���Ƿ��Զ���СΪͼ��
		shopSv.setIconifiedByDefault(false);
		// Ϊ��SearchView��������¼�������
		shopSv.setOnQueryTextListener(this);
		// ���ø�SearchView��ʾ������ť
		shopSv.setSubmitButtonEnabled(true);
		// ���ø�SearchView��Ĭ����ʾ����ʾ�ı�
		shopSv.setQueryHint("����");

		searchResultListView = (ListView) findViewById(R.id.search_shop_result_listview);
		ssa = new SearchShopAdapter(this);
		searchResultListView.setAdapter(ssa);

	}

	// ʹ��������ʽ����
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
		Toast.makeText(this, "��ѡ����ǣ�" + query + " " + xiala, Toast.LENGTH_SHORT).show();
		query_txt = query;		
		System.out.println("----------------------�������󵽷�����----------------------");
		new Thread() {
			public void run() {
				// ����loginByGet����
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
			// ��������ĵ�ַ ͨ��URLEncoder.encode(String s, String enc)
			// ʹ��ָ���ı�����ƽ��ַ���ת��Ϊ application/x-www-form-urlencoded ��ʽ
			String spec = getString(R.string.server_ip) + "/web/SearchShopInfoServlet?type="
					+ URLEncoder.encode(xiala, "UTF-8") + "&query=" + URLEncoder.encode(query_txt, "UTF-8");
			// ���ݵ�ַ����URL����(������ʵ�url)
			URL url = new URL(spec);
			// url.openConnection()����������
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");// ��������ķ�ʽ
			urlConnection.setReadTimeout(5000);// ���ó�ʱ��ʱ��
			urlConnection.setConnectTimeout(5000);// �������ӳ�ʱ��ʱ��
			// ���������ͷ
			urlConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
			System.out.println(urlConnection.getResponseCode());
			// ��ȡ��Ӧ��״̬�� 404 200 505 302
			if (urlConnection.getResponseCode() == 200) {
				// ��ȡ��Ӧ������������
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
				System.out.println("------------------����ʧ��-----------------");
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

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance(); // ����������
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder(); // �õ�DOM������
			Document doc = dombuilder.parse(iss); // ����xml�ĵ������������õ�һ��Document
			Element root = doc.getDocumentElement(); // �õ�xml�ĵ��ĸ��ڵ�
			NodeList shops = root.getChildNodes(); // �õ��ڵ��ֱ�ӵ㣬ʹ��NodeList�ӿڴ������ֱ�ӵ�
			if (shops != null) {
				for (int i = 0; i < shops.getLength(); i++) {
					Node shop = shops.item(i);
					ShopData sd = new ShopData();
					// ��ѯ�ӽڵ�
					for (Node node = shop.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) { // �ӽڵ�����
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
