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
	 * 用于展示照片墙的GridView
	 */
	private GridView mPhotoWall;
	private EditText uploadImgPathEt;
	private Button uploadImgBt;
	private Button openImgBt;

	/**
	 * GridView的适配器
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
					result = "图片成功到达服务器"; break;
				case 404: 
					result = "文件未找到, 图片上传失败"; break;
				case 500:
					result = "Internal Server Error, 图片上传失败"; break;
				case 303:
					result = "redirect error, 图片上传失败"; break;
					default:
						result = "图片上传成功";
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
				Uri originalUri = data.getData();	// 获得图像的URI
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
			conn.setReadTimeout(5000);// 设置超时的时间
			conn.setConnectTimeout(5000);// 设置链接超时的时间
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
			int bufferSize = 1024; // 每次写入1024bytes
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

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance(); // 解析器工厂
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder(); // 得到DOM解析器
			Document doc = dombuilder.parse(iss); // 解析xml文档的输入流，得到一个Document
			Element root = doc.getDocumentElement(); // 得到xml文档的根节点
			NodeList images = root.getChildNodes(); // 得到节点的直接点，使用NodeList接口存放所有直接点
			if (images != null) {
				for (int i = 0; i < images.getLength(); i++) {
					Node image = images.item(i);
					ImageInfo img = new ImageInfo();
					// 轮询子节点
					for (Node node = image.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) { // 子节点属性
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
			// 设置请求的地址 通过URLEncoder.encode(String s, String enc)
			// 使用指定的编码机制将字符串转换为 application/x-www-form-urlencoded 格式
			String urlStr = getString(R.string.server_ip) + "/web/GetImageInfoServlet";
			// 根据地址创建URL对象(网络访问的url)
			URL url = new URL(urlStr);
			// url.openConnection()打开网络链接
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");// 设置请求的方式
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
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		adapter.cancelAllTasks();
	}

}
