package com.example.temp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

class ImageInfo {
	public int id;
	public String url;
	public int uploaderId;
	public String uploadTime;
	public float ranking;
}

public class Share extends Fragment {

	/**
	 * ����չʾ��Ƭǽ��GridView
	 */
	private GridView mPhotoWall;
	private EditText uploadImgPathEt;
	private Button uploadImgBt;
	private Button openImgBt;

	/**
	 * GridView��������
	 */
	private PhotoWallAdapter adapter;
	
	private ArrayList<String> imgUrlArray = new ArrayList<String>();
	String imgurl_pre = "";
	
	private boolean flag = false;
	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0;

	Handler ResultHandler = null;
	Context context = null;
	FileInputStream fstream;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_share, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mPhotoWall = (GridView) getActivity().findViewById(R.id.photo_wall);
		uploadImgPathEt = (EditText) getActivity().findViewById(R.id.share_img_path_Et);
		uploadImgBt = (Button) getActivity().findViewById(R.id.upload_share_img_Bt);
		openImgBt = (Button) getActivity().findViewById(R.id.load_share_img_Bt);
		context = getActivity();
		
		uploadImgBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new  Runnable() {
					public void run() {
						int resultCode = uploadImage(fstream);	
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("resultCode", resultCode);
						msg.setData(b);
						
						ResultHandler.sendMessage(msg);
						flag = true;
					};
				}).start();
			}
			
		});
		
		openImgBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
				getAlbum.setType(IMAGE_TYPE);
				startActivityForResult(getAlbum, IMAGE_CODE);
			}
			
		});
		
		imgurl_pre = getActivity().getString(R.string.server_ip) + "/web/share_imgs/";	
		ResultHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String result = "";
				switch (msg.getData().getInt("resultCode")) {
				case 200:
					result = "ͼƬ�ɹ����������"; break;
				case 404: 
					result = "�ļ�δ�ҵ�, ͼƬ�ϴ�ʧ��"; break;
				case 500:
					result = "Internal Server Error, ͼƬ�ϴ�ʧ��"; break;
				case 303:
					result = "redirect error, ͼƬ�ϴ�ʧ��"; break;
					default:
						result = "ͼƬ�ϴ��ɹ�";
				}
				
				Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
			}
			
		};
		
		new Thread() {
			public void run() {
				loadImageUrl();
				System.out.println(imgUrlArray.size());
				flag = true;
			};
		}.start();
		
		while (!flag) ;
		flag = false;
		
		Images.imgUrlArray = imgUrlArray;
		System.out.println("****");
		System.out.println(Images.imgUrlArray.get(0));
		String[] str_no_vector = new String[imgUrlArray.size()];
		imgUrlArray.toArray(str_no_vector);
		System.out.println(str_no_vector[1]);
		//adapter = new PhotoWallAdapter(this, 0, Images.imageThumbUrls, mPhotoWall);
		adapter = new PhotoWallAdapter(this.getActivity(), 0, str_no_vector, mPhotoWall);
		mPhotoWall.setAdapter(adapter);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != getActivity().RESULT_OK) {
			return;
		}
		
		Bitmap bm = null;
		ContentResolver resolver = getActivity().getContentResolver();
		if (requestCode == IMAGE_CODE) {
			try {
				Uri originalUri = data.getData();	// ���ͼ���URI
				uploadImgPathEt.setText(originalUri.toString());
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = getActivity().managedQuery(originalUri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String path = cursor.getString(column_index);
				uploadImgPathEt.setText(path);	
				fstream = new FileInputStream(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private int uploadImage(FileInputStream fstream) {
		String urlStr = getString(R.string.server_ip) + "/web/ReceiveImageServlet";
		String boundary="*****";
		String end = "\r\n";
		String twoHyphens = "--";
		
		URL url;
		try {
			url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);// ���ó�ʱ��ʱ��
			conn.setConnectTimeout(5000);// �������ӳ�ʱ��ʱ��
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
			DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"id\"" + end);
			ds.writeBytes(end);
			ds.writeBytes(HttpThread.userInfo.id + "");
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + end);
			
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"imgPath\"" + end);
			ds.writeBytes(end);
			ds.writeBytes("share_imgs");
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + end);
			
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"file1\";filename=\"image.jpg\"" + end);
			ds.writeBytes(end);
			int bufferSize = 1024; // ÿ��д��1024bytes
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			while ((length = fstream.read(buffer)) != -1) {
				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			fstream.close();
						
			ds.flush();
			System.out.println(conn.getResponseCode());			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public void parseUserXml(String result) {
		StringReader sr = new StringReader(result);
		InputSource iss = new InputSource(sr);

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance(); // ����������
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder(); // �õ�DOM������
			Document doc = dombuilder.parse(iss); // ����xml�ĵ������������õ�һ��Document
			Element root = doc.getDocumentElement(); // �õ�xml�ĵ��ĸ��ڵ�
			NodeList images = root.getChildNodes(); // �õ��ڵ��ֱ�ӵ㣬ʹ��NodeList�ӿڴ������ֱ�ӵ�
			if (images != null) {
				for (int i = 0; i < images.getLength(); i++) {
					Node image = images.item(i);
					ImageInfo img = new ImageInfo();
					// ��ѯ�ӽڵ�
					for (Node node = image.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) { // �ӽڵ�����
							if (node.getNodeName().equals("id")) {
								img.id = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("url")) {
								img.url = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("upload_time")) {
								img.uploadTime = node.getFirstChild().getNodeValue();
							}
							if (node.getNodeName().equals("uploader_id")) {
								img.uploaderId = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
							if (node.getNodeName().equals("ranking")) {
								img.ranking = Float.parseFloat(node.getFirstChild().getNodeValue());
							}
						}
					}
					if (!(img.url == null)) {
						imgUrlArray.add(imgurl_pre + img.url + ".jpg");
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadImageUrl() {
		try {
			// ��������ĵ�ַ ͨ��URLEncoder.encode(String s, String enc)
			// ʹ��ָ���ı�����ƽ��ַ���ת��Ϊ application/x-www-form-urlencoded ��ʽ
			String urlStr = getString(R.string.server_ip) + "/web/GetImageInfoServlet";
			// ���ݵ�ַ����URL����(������ʵ�url)
			URL url = new URL(urlStr);
			// url.openConnection()����������
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");// ��������ķ�ʽ
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
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		adapter.cancelAllTasks();
	}

}
