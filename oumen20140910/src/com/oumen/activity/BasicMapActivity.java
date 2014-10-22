package com.oumen.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.model.LatLng;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.message.ActivityBean;
import com.oumen.android.BaseActivity;

/**
 * 基本地图展示
 * 
 */
public class BasicMapActivity extends BaseActivity {
	public static final float MAP_ENLARGER_LEVEL = 14.0f;
	private ActivityBean amuse;

	private TitleBar titleBar;
	private Button ivBack;

	private MapView mapView;
	private BaiduMap baiduMap;
	private Marker markerA;

	private Button addressButton;

	private BitmapDescriptor bitmapDescrible;
	private InfoWindow mInfoWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.amusement_map);
		init();
		amuse = (ActivityBean) getIntent().getParcelableExtra("amusementmap");
		if (amuse != null) {
			double lat = Double.parseDouble(amuse.getLat());
			double lng = Double.parseDouble(amuse.getLng());
			LatLng location = new LatLng(lat, lng);
			OverlayOptions option = new MarkerOptions().position(location).icon(bitmapDescrible).zIndex(5);
			markerA = (Marker) (baiduMap.addOverlay(option));
			baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(location));
			addressButton.setText(amuse.getAddress());
		}

		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void init() {
		// 返回按钮
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.getTitle().setText(R.string.nav_activitymap_title);
		titleBar.getRightButton().setVisibility(View.GONE);
		ivBack = titleBar.getLeftButton();

		mapView = (MapView) findViewById(R.id.amusedetail_map);
		baiduMap = mapView.getMap();
		
		MapStatusUpdate status = MapStatusUpdateFactory.zoomTo(MAP_ENLARGER_LEVEL);
		baiduMap.setMapStatus(status);
		baiduMap.setOnMarkerClickListener(mapClickListener);

		bitmapDescrible = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);

		addressButton = new Button(BasicMapActivity.this);
		addressButton.setBackgroundResource(R.drawable.popup);

	}

	private final OnMarkerClickListener mapClickListener = new OnMarkerClickListener() {

		@Override
		public boolean onMarkerClick(Marker marker) {

			final LatLng ll = marker.getPosition();
			Point p = baiduMap.getProjection().toScreenLocation(ll);
			p.y -= 47;
			LatLng llInfo = baiduMap.getProjection().fromScreenLocation(p);
			OnInfoWindowClickListener listener = null;

			if (marker == markerA) {
				listener = new OnInfoWindowClickListener() {
					public void onInfoWindowClick() {
						baiduMap.hideInfoWindow();
					}
				};
			}
//			mInfoWindow = new InfoWindow(addressButton, llInfo, listener);
			mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(addressButton), llInfo, -47, listener);
			baiduMap.showInfoWindow(mInfoWindow);
			return true;
		}
	};

	@Override
	public void onPause() {
		mapView.onPause();
		super.onPause();
	};

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		mapView.onDestroy();
		bitmapDescrible.recycle();
		super.onDestroy();
	}

}
