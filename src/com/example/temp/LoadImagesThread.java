package com.example.temp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;


public class LoadImagesThread extends Thread {

	private String imgUrl;
	private int imgCount;
	private int width;
	private int height;
	
	public LoadImagesThread(String url, int count, int width, int height) {
		imgCount = count;
		this.width = width;
		this.height = height;
		imgUrl = url + "/web/imgs/";
	}
	
	@Override
	public void run() {
		for (int i = 0; i < imgCount; i++) {
			try {
				URL url = new URL(imgUrl + i + ".png");
				URLConnection conn = url.openConnection();
				conn.setReadTimeout(10000);
				conn.connect();
				InputStream is = conn.getInputStream();
				Bitmap tmp = BitmapFactory.decodeStream(is);
				Matrix matrix = new Matrix();
				float scale = (float)(width) / tmp.getWidth();
				int targetHeight = (int)(height / scale);
				matrix.postScale(scale, scale);
				System.out.println(scale + "");
				System.out.println("height:" + height + " width:" + width);
				System.out.println("height0:" + tmp.getHeight() + " width0:" + tmp.getWidth());
				if (targetHeight >= height) {
					System.out.println("height0:" + tmp.getHeight() + " width0:" + tmp.getWidth());
					Bitmap resizeBmp = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), matrix, true);
					Home.imgs[i] = new BitmapDrawable(resizeBmp);
				}
				else {
					System.out.println("height1:" + ((tmp.getHeight() - targetHeight) / 2) + " height2:" + (tmp.getHeight() - (tmp.getHeight() - targetHeight) / 2));
					Bitmap resizeBmp = Bitmap.createBitmap(tmp, 0, (tmp.getHeight() - targetHeight) / 2, 
							tmp.getWidth(), tmp.getHeight() - (tmp.getHeight() - targetHeight) / 2, matrix, true);
					Home.imgs[i] = new BitmapDrawable(resizeBmp);
				}
				is.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Home.flag = 1;
	
		Home.homeHandler.sendEmptyMessage(1);
	}
	
}
