package tw.binary.dipper.consumer;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import tw.binary.dipper.DrawerBaseActivity;
import tw.binary.dipper.R;
import tw.binary.dipper.util.MyUtils;
import tw.binary.dipper.util.WeatherUtils;

public class WeatherActivity extends DrawerBaseActivity implements View.OnClickListener
        , WeatherUtils.OnWeatherForcastListener {
    private static Context mContext;
    private GoogleMap mMap;
    private static float MAPZOOM = 8f;
    private Marker mMarker = null;
    private Double lat, lng;
    private ImageView[] imageDay = new ImageView[5];   //氣象圖
    private TextView[] tvDay = new TextView[5];   //星期？
    private TextView[] tvDayH = new TextView[5];  //最高溫
    private TextView[] tvDayL = new TextView[5];  //最低溫
    private WeatherUtils weatherUtils = new WeatherUtils(this);
    private HashMap<String, String> zhTWweek = new HashMap<>();
    private HashMap<String, Integer> iconMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        setUpMapIfNeeded(); //設定GoogleMap
        if (savedInstanceState != null) {
            lat = savedInstanceState.getDouble("Lat");
            lng = savedInstanceState.getDouble("Lng");
        } else {
            LatLng latLng = getLocation();
            if (latLng == null) {

                //暫定
                lat = 24.804179;
                lng = 120.961529;
            } else {
                lat = latLng.latitude;
                lng = latLng.longitude;
            }
        }
        gotoLocation(lat, lng, MAPZOOM);
        weatherUtils.getForcast(lat, lng);

        findViewId();
        initData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("Lat", lat);
        outState.putDouble("Lng", lng);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_weather;
    }

    private void initData() {
        zhTWweek.put("Sun", "日");
        zhTWweek.put("Mon", "一");
        zhTWweek.put("Tue", "二");
        zhTWweek.put("Wed", "三");
        zhTWweek.put("Thu", "四");
        zhTWweek.put("Fri", "五");
        zhTWweek.put("Sat", "六");

        iconMap.put("Sunny", R.drawable.weather_sunny_day);
        iconMap.put("Partly Cloudy", R.drawable.weather_partly_cloudy_day);
        iconMap.put("Isolated Thunderstorms", R.drawable.weather_isolated_thunderstorms);
        iconMap.put("Showers", R.drawable.weather_rain);
        iconMap.put("Rain", R.drawable.weather_rain);
        iconMap.put("Rain/Thunder", R.drawable.weather_isolated_thunderstorms);
        iconMap.put("Light Rain", R.drawable.weather_light_rain);
        iconMap.put("PM Rain", R.drawable.weather_rain);
        iconMap.put("PM Light Rain", R.drawable.weather_light_rain);
        iconMap.put("AM Showers", R.drawable.weather_am_showers);
        iconMap.put("PM Showers", R.drawable.weather_am_showers);
        iconMap.put("PM Thunderstorms", R.drawable.weather_isolated_thunderstorms);
        iconMap.put("PM Thundershowers", R.drawable.weather_thunder_storms);
        iconMap.put("Thunderstorms", R.drawable.weather_isolated_thunderstorms);
        iconMap.put("Thundershowers", R.drawable.weather_isolated_thunderstorms);
    }

    protected void updateUi() {
    }

    private void findViewId() {
        weatherUtils.setOnWeatherForcastListener(this);

        imageDay[0] = (ImageView) findViewById(R.id.imageDay0);
        imageDay[1] = (ImageView) findViewById(R.id.imageDay1);
        imageDay[2] = (ImageView) findViewById(R.id.imageDay2);
        imageDay[3] = (ImageView) findViewById(R.id.imageDay3);
        imageDay[4] = (ImageView) findViewById(R.id.imageDay4);

        tvDay[0] = (TextView) findViewById(R.id.tvDay0);
        tvDay[1] = (TextView) findViewById(R.id.tvDay1);
        tvDay[2] = (TextView) findViewById(R.id.tvDay2);
        tvDay[3] = (TextView) findViewById(R.id.tvDay3);
        tvDay[4] = (TextView) findViewById(R.id.tvDay4);

        tvDayH[0] = (TextView) findViewById(R.id.tvDay0H);
        tvDayH[1] = (TextView) findViewById(R.id.tvDay1H);
        tvDayH[2] = (TextView) findViewById(R.id.tvDay2H);
        tvDayH[3] = (TextView) findViewById(R.id.tvDay3H);
        tvDayH[4] = (TextView) findViewById(R.id.tvDay4H);
        tvDayL[0] = (TextView) findViewById(R.id.tvDay0L);
        tvDayL[1] = (TextView) findViewById(R.id.tvDay1L);
        tvDayL[2] = (TextView) findViewById(R.id.tvDay2L);
        tvDayL[3] = (TextView) findViewById(R.id.tvDay3L);
        tvDayL[4] = (TextView) findViewById(R.id.tvDay4L);

    }

    //有這一行AcctionBar才會顯示
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_WEATHER;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.setPadding(0, 750, 0, 0);    //My Location 的位置
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        //設定MAP長按可新增Marker
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng pLatLng) {
                //Remove 舊Marker
                if (mMarker != null)
                    mMarker.remove();
                lat = pLatLng.latitude;
                lng = pLatLng.longitude;
                MAPZOOM = mMap.getCameraPosition().zoom;
                gotoLocation(lat, lng, MAPZOOM);
                weatherUtils.getForcast(lat, lng);
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
                MAPZOOM = mMap.getCameraPosition().zoom;
                gotoLocation(lat, lng, MAPZOOM);
                weatherUtils.getForcast(lat, lng);
            }
        });

    }

    private LatLng getLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get current location
        Location location = locationManager.getLastKnownLocation(provider); // 設定定位資訊由 GPS提供

        if (location == null) {
            Toast.makeText(mContext, "Current location is not available", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
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

    private void showMarkerInfo(Marker pMarker) {
        pMarker.setSnippet("Lat:" + String.valueOf(pMarker.getPosition().latitude) +
                "  Lng:" + String.valueOf(pMarker.getPosition().longitude));
        Geocoder geocoder = new Geocoder(mContext);
        List<Address> addressList;
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        //Check by Id 暫時沒用到
        switch (v.getId()) {
            case R.id.btSave:

                break;
            default:
        }
        //Check by Tag
        /*
        switch (v.getTag().toString()) {
            case "btAdd":
                startResourceDetail(this,null);
                break;
        }*/
    }

    public boolean isAuthNeeded() {
        return false;
    }   //不須登入


    @Override
    public void forcastResult(JSONArray result) {

        if (result == null) return;
        try {
            for (int i = 0; i < 5; i++) {
                JSONObject jsonObject = result.getJSONObject(i);
                if (jsonObject == null) return;

                imageDay[i].setImageResource(iconMap.get(jsonObject.getString("text")) == null ?
                        R.drawable.weather_cloudy : iconMap.get(jsonObject.getString("text")));

                tvDay[i].setText(zhTWweek.get(jsonObject.getString("day")));
                tvDayH[i].setText("↑ " + String.valueOf(MyUtils.tempertureF2C(jsonObject.getInt("high"))) + "°");
                tvDayL[i].setText("↓ " + String.valueOf(MyUtils.tempertureF2C(jsonObject.getInt("low"))) + "°");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
