package com.example.temp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class Myself extends Fragment {
	private EditText usernameText, ageText, schoolText, interestText, newPasswordEt, passwordConfirmEt;
	private RadioButton male, female;
	private Button resetBt, commitBt;
	private ImageView newUserLogo;
	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0;
	private static FileInputStream fstream = null;
	private static Context context;
	private static Handler updateUserInfoHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show();				
				break;
			}			
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_myself, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		context = getActivity();
		usernameText = (EditText)getActivity().findViewById(R.id.userNameText);
		newPasswordEt = (EditText)getActivity().findViewById(R.id.newPasswordEt);
		passwordConfirmEt = (EditText)getActivity().findViewById(R.id.passwordConfirmEt);
		ageText = (EditText)getActivity().findViewById(R.id.ageText);
		schoolText = (EditText)getActivity().findViewById(R.id.schoolText);
		interestText = (EditText)getActivity().findViewById(R.id.interstText);
		male = (RadioButton)getActivity().findViewById(R.id.male);
		female = (RadioButton)getActivity().findViewById(R.id.female);
		resetBt = (Button)getActivity().findViewById(R.id.resetBt);
		commitBt = (Button)getActivity().findViewById(R.id.commitBt);
		newUserLogo = (ImageView)getActivity().findViewById(R.id.newUserLogo);

		usernameText.setText(HttpThread.userInfo.username);
		if (HttpThread.userInfo.age.compareTo("null") != 0)
			ageText.setText(HttpThread.userInfo.age);
		if (HttpThread.userInfo.school.compareTo("null") != 0)
			schoolText.setText(HttpThread.userInfo.school);
		if (HttpThread.userInfo.interest.compareTo("null") != 0)
			interestText.setText(HttpThread.userInfo.interest);
		if (HttpThread.userInfo.gender.equals("1")) {
			male.setChecked(true);
			female.setChecked(false);
		}
		else {
			male.setChecked(false);
			female.setChecked(true);
		}

		passwordConfirmEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {	
				if (passwordConfirmEt.getText().toString().equals(newPasswordEt.getText().toString())) {
					Toast.makeText(context, "密码修改成功！", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(context, "两次输入密码不一致!", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

		});

		resetBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usernameText.setText(HttpThread.userInfo.username);
				if (HttpThread.userInfo.age.compareTo("null") != 0)
					ageText.setText(HttpThread.userInfo.age);
				if (HttpThread.userInfo.school.compareTo("null") != 0)
					schoolText.setText(HttpThread.userInfo.school);
				if (HttpThread.userInfo.interest.compareTo("null") != 0)
					interestText.setText(HttpThread.userInfo.interest);
				if (HttpThread.userInfo.gender.equals("1")) {
					male.setChecked(true);
					female.setChecked(false);
				}
				else {
					male.setChecked(false);
					female.setChecked(true);
				}
			}

		});

		newUserLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
				getAlbum.setType(IMAGE_TYPE);
				startActivityForResult(getAlbum, IMAGE_CODE);
			}

		});

		commitBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HttpThread.userInfo.username = usernameText.getText().toString();
				HttpThread.userInfo.age = ageText.getText().toString();
				HttpThread.userInfo.school = schoolText.getText().toString();
				HttpThread.userInfo.interest = interestText.getText().toString();
				HttpThread.userInfo.password = passwordConfirmEt.getText().toString();
				if (male.isChecked()) {
					HttpThread.userInfo.gender = "1";
				} else {
					HttpThread.userInfo.gender = "0";
				}

				new Thread() {

					@Override
					public void run() {
						updateUserInfoThread();
					}

				}.start();
			}

		});
	}

	private int uploadImage(FileInputStream fstream, String userId) {
		String urlStr = getActivity().getString(R.string.server_ip) + "/web/ReceiveImageServlet";
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
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
			DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"userId\"" + end);
			ds.writeBytes(end);
			ds.writeBytes(userId + "");
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + end);
			System.out.println("upload image username = " + userId);

			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"imgPath\"" + end);
			ds.writeBytes(end);
			ds.writeBytes("user_imgs");
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

	public void updateUserInfoThread() {
		try {
			String urlStr = getActivity().getString(R.string.server_ip) + "/web/UpdateUserInfoServlet";
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(50000);			
			OutputStream out = conn.getOutputStream();
			String content = "<?xml version='1.0' encoding='gbk'?><users>";
			content += "<user><username>" + HttpThread.userInfo.username + "</username><age>" + HttpThread.userInfo.age
					+ "</age><school>" + HttpThread.userInfo.school + "</school><interest>" + HttpThread.userInfo.interest
					+ "</interest><id>" + HttpThread.userInfo.id + "</id><gender>" + HttpThread.userInfo.gender
					+ "</gender><password>" + HttpThread.userInfo.password + "</password></user>";
			content += "</users>";
			System.out.println(content);
			out.write(content.getBytes());
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String str;
			StringBuffer sb = new StringBuffer();
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}

			Message msg = new Message();
			if (sb.toString().equals("success")) {
				Myself.updateUserInfoHandler.sendEmptyMessage(1);
				if (fstream != null && fstream.available() != 0) {
					new Thread(){
						public void run() {
							int resultCode = uploadImage(fstream, HttpThread.userInfo.id+"");	
							System.out.println("result code of CreateGroup: " + resultCode);
						}
					}.start();
				}
			} else {
				Myself.updateUserInfoHandler.sendEmptyMessage(0);
			}
			is.close();
		} catch (MalformedURLException e) {
			System.out.println("url error");
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
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
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = getActivity().managedQuery(originalUri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String path = cursor.getString(column_index);
				newUserLogo.setImageBitmap(bm);
				fstream = new FileInputStream(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
