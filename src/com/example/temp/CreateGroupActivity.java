package com.example.temp;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

public class CreateGroupActivity extends Activity {

	Button createBt;
	Button resetBt;
	Button returnBt;
	ImageView newGroupLogo;
	EditText groupNameText, startDateText, startTimeText, endDateText, endTimeText, locationText, schoolText,
			activityText;
	public static EditText memberText;
	public static String memberId;
	Button addGroupMemberBt;

	private final int START_DATE_DIALOG = 1;
	private final int START_TIME_DIALOG = 2;
	private final int END_DATE_DIALOG = 3;
	private final int END_TIME_DIALOG = 4;
	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0;
	FileInputStream fstream = null;
	public static Handler createGroupHandler = null;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);

		createBt = (Button) findViewById(R.id.createGroupInfoBt);
		returnBt = (Button) findViewById(R.id.returnGroupInfoBt);
		resetBt = (Button) findViewById(R.id.resetGroupInfoBt);
		addGroupMemberBt = (Button) findViewById(R.id.addGroupMembersBt);
		groupNameText = (EditText) findViewById(R.id.groupNameText);
		startDateText = (EditText) findViewById(R.id.startDateText);
		startTimeText = (EditText) findViewById(R.id.startTimeText);
		endDateText = (EditText) findViewById(R.id.endDateText);
		endTimeText = (EditText) findViewById(R.id.cendTimeText);
		locationText = (EditText) findViewById(R.id.locationText);
		schoolText = (EditText) findViewById(R.id.schoolText);
		activityText = (EditText) findViewById(R.id.activityText);
		memberText = (EditText) findViewById(R.id.memberText);
		newGroupLogo = (ImageView)findViewById(R.id.newGroupLogo);
		
		memberText.setText(HttpThread.userInfo.username);
		memberId = HttpThread.userInfo.id + "";
		
		context = this;
		createGroupHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String result = msg.getData().getString("result");
				System.out.println(result);
				Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
				
				try {
					if (result.compareTo("success!") == 0 && fstream != null && fstream.available() != 0) {
						new Thread(){
							public void run() {							
								int resultCode = uploadImage(fstream, groupNameText.getText().toString());	
								System.out.println("result code of CreateGroup: " + resultCode);
							}
						}.start();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};
		
		newGroupLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
				getAlbum.setType(IMAGE_TYPE);
				startActivityForResult(getAlbum, IMAGE_CODE);
			}
			
		});

		startDateText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(START_DATE_DIALOG);
			}

		});
		
		startTimeText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(START_TIME_DIALOG);
			}
			
		});
		
		endDateText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(END_DATE_DIALOG);
			}
			
		});
		
		endTimeText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(END_TIME_DIALOG);
			}
			
		});

		returnBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(CreateGroupActivity.this, MainActivity.class);
				newIntent.putExtra("id", 1);
				startActivity(newIntent);
			}

		});

		resetBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				groupNameText.setText("");
				startTimeText.setText("");
				startDateText.setText("");
				endDateText.setText("");
				endTimeText.setText("");
				locationText.setText("");
				schoolText.setText("");
				activityText.setText("");
				memberText.setText("");
			}

		});

		createBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupData tmp = new GroupData();
				tmp.groupName = groupNameText.getText().toString();
				tmp.freeTimeStart = startDateText.getText().toString() + " " + startTimeText.getText().toString();
				tmp.freeTimeStop = endDateText.getText().toString() + " " + endTimeText.getText().toString();
				tmp.location = locationText.getText().toString();
				tmp.school = schoolText.getText().toString();
				tmp.activity = activityText.getText().toString();
				tmp.groupmembers = memberId;
				
				new CreateGroupThreads(tmp, getString(R.string.server_ip)).start();				
			}

		});
		
		addGroupMemberBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(CreateGroupActivity.this, SearchMemberActivity.class);
				startActivity(newIntent);
			}
			
		});
	}
	
	private int uploadImage(FileInputStream fstream, String groupName) {
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
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"groupName\"" + end);
			ds.writeBytes(end);
			ds.writeBytes(groupName + "");
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + end);
			
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition:form-data;" + "name=\"imgPath\"" + end);
			ds.writeBytes(end);
			ds.writeBytes("logo_imgs");
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		
		Bitmap bm = null;
		ContentResolver resolver = getContentResolver();
		if (requestCode == IMAGE_CODE) {
			try {
				Uri originalUri = data.getData();	// 获得图像的URI
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
				newGroupLogo.setImageBitmap(bm);
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = managedQuery(originalUri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String path = cursor.getString(column_index);
				fstream = new FileInputStream(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		Calendar calendar = Calendar.getInstance(); // 用来获取时间和日期

		switch (id) {
		case START_DATE_DIALOG:
			DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					startDateText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
				}
			};
			dialog = new DatePickerDialog(this, startDateListener, 
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			break;
		case START_TIME_DIALOG:
			TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
				
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					startTimeText.setText(hourOfDay + ":" + minute);
				}
			};
			dialog = new TimePickerDialog(this, startTimeListener, 
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
			break;
		case END_DATE_DIALOG:
			DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					endDateText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
				}
			};
			dialog = new DatePickerDialog(this, endDateListener, 
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			break;
		case END_TIME_DIALOG:
			TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
				
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					endTimeText.setText(hourOfDay + ":" + minute);
				}
			};
			dialog = new TimePickerDialog(this, endTimeListener,
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
			break;
		default:
			break;
		}

		return dialog;
	}
}
