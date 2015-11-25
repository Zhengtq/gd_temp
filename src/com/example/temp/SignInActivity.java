package com.example.temp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignInActivity extends Activity {

	private EditText siname, sipass,siemail;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);			
		siname = (EditText) findViewById(R.id.user);
		sipass = (EditText) findViewById(R.id.pwd);
		siemail = (EditText) findViewById(R.id.email);
		
	}

	public void sign(View v) {

		int id = v.getId();		
		switch (id) {		
		case R.id.submit:
			final String gdName = siname.getText().toString();
			final String gdPass = sipass.getText().toString();
			final String gdEmail = siemail.getText().toString();

			if (TextUtils.isEmpty(gdName) || TextUtils.isEmpty(gdPass)|| TextUtils.isEmpty(gdEmail)) 
			{
				Toast.makeText(this, "用户名或者密码&不能为空", Toast.LENGTH_LONG).show();
			}
			else 
			{	
				final Handler mHandler = new Handler() {
					public void handleMessage(android.os.Message msg) {
						switch (msg.what) {
						case 1:
							boolean b = (Boolean)msg.obj;
							break;
						default:
							break;
						}
					};
				};				
				new Thread() 
				{
					public void run()
					{
						boolean b = signByGet(gdName, gdPass,gdEmail);
						Message msg = new Message();
						msg.what = 1;
						msg.obj = b;
						mHandler.sendMessage(msg);
					};
				}.start();
			}
			break;
		default:
			break;
		}
	}

	public Boolean signByGet(String gdName, String gdPass, String gdEmail ) {
		String result = "0";
		try 
		{
			String spec = getString(R.string.server_ip) + "/web/SignInServlet?gdname="+gdName+"&gdpass="+gdPass + "&gdemail="+gdEmail;  						
			URL url = new URL(spec);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(5000);
			urlConnection.setConnectTimeout(5000);
			urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");			
			if (urlConnection.getResponseCode() == 200) 
			{
				// 获取响应的输入流对象
				InputStream is = urlConnection.getInputStream();
				// 创建字节输出流对象
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				// 定义读取的长度
				int len = 0;
				// 定义缓冲区
				byte buffer[] = new byte[1024];
				// 按照缓冲区的大小，循环读取
				while ((len = is.read(buffer)) != -1) {
					// 根据读取的长度写入到os对象中
					os.write(buffer, 0, len);
				}
				// 释放资源
				is.close();
				os.close();
				// 返回字符串
				result = new String(os.toByteArray());				
				System.out.println("***************" + result
						+ "******************");

			} else {
				System.out.println("------------------链接失败-----------------");
			}								
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(result.equals("0"))
		{
			return false;
		}
		else
		{
			return true;
		}
	}	
}
