package com.example.temp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class SearchMemberActivity extends Activity implements SearchView.OnQueryTextListener{

	private SearchView searchMemberSv;
	private ListView searchMemberResultLv;
	private SearchMemberListAdapter smla;
	private String query_txt;
	boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_member);

		searchMemberSv = (SearchView)findViewById(R.id.search_member_Sv);
		searchMemberResultLv = (ListView)findViewById(R.id.search_member_result_listview);
		smla = new SearchMemberListAdapter(this);
		searchMemberResultLv.setAdapter(smla);
		// ���ø�SearchViewĬ���Ƿ��Զ���СΪͼ��
		searchMemberSv.setIconifiedByDefault(false);
		// Ϊ��SearchView��������¼�������
		searchMemberSv.setOnQueryTextListener(this);
		// ���ø�SearchView��ʾ������ť
		searchMemberSv.setSubmitButtonEnabled(true);
		// ���ø�SearchView��Ĭ����ʾ����ʾ�ı�
		searchMemberSv.setQueryHint("����");
		searchMemberResultLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (CreateGroupActivity.memberText.getText().toString().equals("")) {
					CreateGroupActivity.memberText.setText(smla.memberDataList.get(position).username);
					CreateGroupActivity.memberId = smla.memberDataList.get(position).id + "";
				}
				else {
					CreateGroupActivity.memberText.setText(smla.memberDataList.get(position).username + "," +
							CreateGroupActivity.memberText.getText().toString());
					CreateGroupActivity.memberId = CreateGroupActivity.memberId + "|" + smla.memberDataList.get(position).id;
				}

				Intent returnIntent = new Intent();
				SearchMemberActivity.this.finish();
			}

		});
	}

	@Override
	public boolean onQueryTextSubmit(String query) {		
		SearchMemberListAdapter.memberDataList.clear();
		SearchMemberListAdapter.img.clear();
		smla.notifyDataSetChanged();
		query_txt = query;		
		System.out.println("----------------------�������󵽷�����----------------------");
		new Thread() {
			public void run() {
				// ����loginByGet����
				searchMemberByGet(query_txt);			
				flag = true;
			};
		}.start();

		while (!flag);
		if (flag) {
			smla.notifyDataSetChanged();
			flag = false;
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return true;
	}

	public void searchMemberLogoByPost(String imgUrl) {
		String urlString = getString(R.string.server_ip) + "/web/user_imgs/" + imgUrl + ".jpg";
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			Bitmap img = BitmapFactory.decodeStream(is);
			SearchMemberListAdapter.img.add(img);
			is.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void searchMemberByGet(String query_txt) {

		try {
			// ��������ĵ�ַ ͨ��URLEncoder.encode(String s, String enc)
			// ʹ��ָ���ı�����ƽ��ַ���ת��Ϊ application/x-www-form-urlencoded ��ʽ
			String spec = getString(R.string.server_ip) + "/web/SearchMemberServlet?username=" + URLEncoder.encode(query_txt, "UTF-8");
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

	public void parseUserXml(String result) {
		StringReader sr = new StringReader(result);
		InputSource iss = new InputSource(sr);

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance(); // ����������
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder(); // �õ�DOM������
			Document doc = dombuilder.parse(iss); // ����xml�ĵ������������õ�һ��Document
			Element root = doc.getDocumentElement(); // �õ�xml�ĵ��ĸ��ڵ�
			NodeList users = root.getChildNodes(); // �õ��ڵ��ֱ�ӵ㣬ʹ��NodeList�ӿڴ������ֱ�ӵ�
			if (users != null) {
				for (int i = 0; i < users.getLength(); i++) {
					Node user = users.item(i);
					User u = new User();
					// ��ѯ�ӽڵ�
					for (Node node = user.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) { // �ӽڵ�����
							if (node.getNodeName().equals("name")) {
								u.username = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("school")) {
								u.school = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("image")) {
								u.imgId = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("interest")) {
								u.interest = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("id")) {
								u.id = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
						}
					}
					if (!(u.imgId == null)) {
						SearchMemberListAdapter.memberDataList.add(u);
						searchMemberLogoByPost(u.imgId);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
