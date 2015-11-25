package com.example.temp;



import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.ViewSwitcher.ViewFactory;

public class Home extends Fragment implements ViewFactory, OnTouchListener, OnRefreshListener {
	public static int imgCount = 4;
	private static ImageSwitcher mImageSwitcher;
	//����ĸ��ö�����ͼƬ
	public static BitmapDrawable[] imgs;
	public static int flag = -1;
	private static int currentPosition = 0; // ��ǰѡ��ͼ���id��
	private float downX; // ���µ��X����
	private LinearLayout linearLayout;
	private ImageView[] tips; // �������
	private RefreshListView groupListView;
	private SearchView searchView;
	private static GroupListAdapter gla = null;
	
	public static Handler homeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				mImageSwitcher.setImageDrawable(imgs[currentPosition]);
				break;
			case 2:
				gla.notifyDataSetChanged();
				break;
			case 3:
				gla.notifyDataSetChanged();
				gla.loadImgs();
				break;
			}				
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// �����Ļ�ܶ�
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density; // ��Ļ�ܶȣ����ر�����0.75/1.0/1.5/2.0��
		int densityDPI = dm.densityDpi; // ��Ļ�ܶ�(ÿ�����أ� 120/160/240/320)
		float xdpi = dm.xdpi;
		float ydpi = dm.ydpi;
		int screenWidthDip = dm.widthPixels; // ��Ļ��ȣ���dipΪ��λ
		int screenHeightDip = dm.heightPixels;
		int screenWidth = (int) (screenWidthDip * density + 0.5);
		int screenHeight = (int) (screenHeightDip * density + 0.5); // ������Ϊ��λ
		System.out.println("width:" + screenWidth + "px height:" + screenHeight);

		if (flag == -1) {
			imgs = new BitmapDrawable[imgCount];
			//����ĸ��ö�����ͼƬ
			new LoadImagesThread(getString(R.string.server_ip), imgCount, screenWidth, (int) screenWidth * 3 / 7)
					.start();
		}				

		mImageSwitcher = (ImageSwitcher) getActivity().findViewById(R.id.imageSwitcher1);

		linearLayout = (LinearLayout) getActivity().findViewById(R.id.viewGroup);

		// init dots
		//�����ĸ��ö�����ͼƬ
		tips = new ImageView[imgs.length];
		for (int i = 0; i < imgs.length; i++) {
			ImageView mImageView = new ImageView(this.getActivity());
			tips[i] = mImageView;
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(5, 5);
			layoutParams.rightMargin = 3;
			layoutParams.leftMargin = 3;
			mImageView.setBackgroundResource(R.drawable.dot_normal);
			linearLayout.addView(mImageView, layoutParams);
		}

		currentPosition = 0;
		mImageSwitcher.setFactory(this); // ����Factory
		mImageSwitcher.setOnTouchListener(this);
		if (currentPosition < imgs.length && imgs[currentPosition] != null) 
			mImageSwitcher.setImageDrawable(imgs[currentPosition]);
		else
			mImageSwitcher.setImageResource(R.drawable.empty_photo);
		System.out.println("success" + flag);
		tips[currentPosition].setBackgroundResource(R.drawable.dot_focus);

		
		groupListView = (RefreshListView) getActivity().findViewById(R.id.groupListView);
		gla = new GroupListAdapter(this.getActivity(), getString(R.string.server_ip));
		groupListView.setAdapter(gla);
		groupListView.setOnRefreshListener(this);

		searchView = (SearchView) getActivity().findViewById(R.id.searchView1);
		searchView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSearchActivity();
			}

		});

		
		groupListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startItemDetail(position - 1);
			}
		});
	}

	public void startItemDetail(int position) {
		if (position >= 0 && gla.groupDataList.size() > 0 && position < gla.groupDataList.size()) {
			Intent itemDetail = new Intent(this.getActivity(), ListItemDetailActivity.class);
			GroupData item = GroupListAdapter.groupDataList.get(position);
			itemDetail.putExtra("position", position);
			itemDetail.putExtra("simple", item);
			startActivity(itemDetail);
		}
	}

	//search
	public void startSearchActivity() {
		Intent searchIntent = new Intent(this.getActivity(), SearchActivity.class);
		startActivity(searchIntent);
	}


	//�ö�����ͼƬ����Ӧ����
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			downX = event.getX();
			break;
		}
		case MotionEvent.ACTION_UP: {
			float lastX = event.getX();
			// ̧���ʱ��X������ڰ��µ�ʱ�����ʾ��һ��ͼƬ
			if (lastX > downX) {
				mImageSwitcher
						.setInAnimation(AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.left_in));
				mImageSwitcher.setOutAnimation(
						AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.right_out));
				tips[currentPosition].setBackgroundResource(R.drawable.dot_normal);
				currentPosition = (currentPosition - 1 + imgs.length) % imgs.length;
				tips[currentPosition].setBackgroundResource(R.drawable.dot_focus);
				mImageSwitcher.setImageDrawable(imgs[currentPosition]);
			}
			if (lastX < downX) {
				mImageSwitcher
						.setInAnimation(AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.right_in));
				mImageSwitcher
						.setOutAnimation(AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.left_out));
				tips[currentPosition].setBackgroundResource(R.drawable.dot_normal);
				currentPosition = (currentPosition + 1) % imgs.length;
				tips[currentPosition].setBackgroundResource(R.drawable.dot_focus);
				mImageSwitcher.setImageDrawable(imgs[currentPosition]);
			}
			break;
		}
		}
		return true;
	}

	@Override
	public View makeView() {
		// TODO Auto-generated method stub
		return new ImageView(this.getActivity());
	}

	@Override
	//����ˢ��
	public void onDownPullRefresh() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(1000);
				gla.refreshData();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				gla.notifyDataSetChanged();
				groupListView.hideHeaderView();
			}

		}.execute(new Void[] {});

	}

	@Override
	//��������
	public void onLoadingMore() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				gla.addABatch();

				System.out.println(gla.groupDataList.size() + " " + gla.img.size());
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				gla.notifyDataSetChanged();
				groupListView.hideFooterView();
			}

		}.execute(new Void[] {});

	}
}
