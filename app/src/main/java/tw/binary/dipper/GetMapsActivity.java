package tw.binary.dipper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class GetMapsActivity extends ToolbarBaseActivity {

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final float MAPZOOM = 18.0f;
    private Marker mMarker;
    private Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!mapServiceOK()) {
            Toast.makeText(this, "Google Maps is ready", Toast.LENGTH_SHORT).show();
            setUpMapIfNeeded();
        }

        lat = Double.parseDouble(getIntent().getStringExtra("Lat"));
        lng = Double.parseDouble(getIntent().getStringExtra("Lng"));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_get_maps;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        //設定MAP長按可新增Marker
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng pLatLng) {
                //Remove 舊Marker
                if (mMarker != null)
                    mMarker.remove();
                lat = pLatLng.latitude;
                lng = pLatLng.longitude;
                gotoLocation(lat, lng, MAPZOOM);
            }
        });
        //設定Marker可拖拉
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker pMarker) {

            }

            @Override
            public void onMarkerDrag(Marker pMarker) {

            }

            @Override
            public void onMarkerDragEnd(Marker pMarker) {
                lat = pMarker.getPosition().latitude;
                lng = pMarker.getPosition().longitude;
                gotoLocation(lat, lng, MAPZOOM);
            }
        });

        //如果有座標先顯示
        gotoLocation(lat, lng, MAPZOOM);
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get current location
        Location location = locationManager.getLastKnownLocation(provider); // 設定定位資訊由 GPS提供

        if (location == null) {
            Toast.makeText(this, "Current location is not available", Toast.LENGTH_SHORT).show();
        } else {

            // mMap.setOnMarkerDragListener(this);
            lat = location.getLatitude();  // 取得經度
            lng = location.getLongitude(); // 取得緯度
            // 定位到現在位置
            gotoLocation(lat, lng, MAPZOOM);
        }
    }

    //檢查服務是否存在
    public boolean mapServiceOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can not connect to Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void gotoLocation(double lat, double lng, float pMAPZOOM) {
        LatLng ll = new LatLng(lat, lng);
        if (mMarker != null)
            mMarker.remove();
        mMarker = mMap.addMarker(new MarkerOptions().position(ll).title("現在位置").draggable(true));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, pMAPZOOM);
        mMap.moveCamera(cameraUpdate);
        showMarkerInfo(mMarker);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currentLocation:
                getLocation();
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //更新Parent Activity Lat Lng
    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        mIntent.putExtra("Lat", String.valueOf(lat));
        mIntent.putExtra("Lng", String.valueOf(lng));
        setResult(RESULT_OK, mIntent);
        super.onBackPressed();  //這是硬體按鍵，一定要放最後
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return super.checkCallingOrSelfUriPermission(uri, modeFlags);
    }

    private void showMarkerInfo(Marker pMarker) {

        pMarker.setSnippet("Lat:" + String.valueOf(pMarker.getPosition().latitude) +
                "  Lng:" + String.valueOf(pMarker.getPosition().longitude));
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = null;
        LatLng position = pMarker.getPosition();
        try {
            addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            pMarker.setTitle(address.getAddressLine(0));
            pMarker.showInfoWindow();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.get_map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
