package com.example.wte;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RestaurantActivity extends AppCompatActivity {
    private static final double easternmost = 131.87222222; // 대한민국 극동
    private static final double westernmost = 125.06666667; // 극서
    private static final double northernmost = 38.45000000; // 극북
    private static final double southernmost = 33.10000000; // 극남

    String[] sortItems = {"정확도순", "거리순"};
    SearchView restSearchView;
    ListView restListView;
    JSONArray array;
    JSONArray addArray;
    // 위도, 경도
    String lat = "";
    String lon = "";
    // 식당 요청 API 주소
    String kakaoAPI = "";
    String gpsKakaoAPI = "";
    String url = "";
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        // 초기 좌표 - 전남대 공과대학 7호관
        lat = "35.178215357025955";
        lon = "126.90922309044905";
        setApiURL();

        // GPS 및 위치 관련
        final ProgressDialog dialog = new ProgressDialog(RestaurantActivity.this); // 진행 다이얼로그 생성
        dialog.setMessage("위치 검색 중...");
        dialog.show();
        Log.d("build MODEL", android.os.Build.MODEL);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) { // gps 좌표가 업데이트 됐을 때 위도, 경도 변수 업데이트
                if (westernmost<location.getLongitude() && location.getLongitude()<easternmost &&
                        southernmost<location.getLatitude() && location.getLatitude()<northernmost) { // 대한민국 안의 좌표 일때만
                    lat = Double.toString(location.getLatitude());
                    lon = Double.toString(location.getLongitude());
                    Log.d("gps", "lat: "+lat);
                    Log.d("gps", "lon: "+lon);
                    setApiURL();
                    url = kakaoAPI;
                    Log.d("uuu", "kakaoAPI"+kakaoAPI);
                    Log.d("uuu", "gpsKakaoAPI"+gpsKakaoAPI);
                    sendAddressRequest();
                    dialog.dismiss();
                    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {locationManager.removeUpdates(locationListener); }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한 허용 확인
            return;
        }
        if (!android.os.Build.MODEL.contains("sdk")){ // 기기가 애뮬레이터가 아닐 경우에는 NETWORK_PROVIDER도 사용
            Log.d("Build.MODEL : ", android.os.Build.MODEL);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 100, locationListener);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener);

        // 식당 검색 관련
        restSearchView = findViewById(R.id.restSearchesView);
        restSearchView.setSubmitButtonEnabled(true);

        restSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // 검색 버튼을 눌렀을 경우
                TextView textInfo = findViewById(R.id.textInfo);
                textInfo.setVisibility(View.GONE); // 안내 문구 완전히 숨기기
                restSearchView.clearFocus();
                sendRestRequest(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        if (AppHelper.requestQueue != null) { // RequestQueue 생성
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        // 식당 리스트 정렬 방법 관련
        Spinner spinner = findViewById(R.id.sortSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item); //미리 정의된 레이아웃 사용
        spinner.setAdapter(adapter); // 스피너 객체에다가 어댑터를 넣기
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 스피너가 선택 됐을 때
                if (position == 0){
                    url = kakaoAPI;
                    sendAddressRequest();
                    CharSequence query = restSearchView.getQuery();
                    if (query.length() > 0) {
                        sendRestRequest(query);
                    }
                } else {
                    url = gpsKakaoAPI;
                    sendAddressRequest();
                    CharSequence query = restSearchView.getQuery();
                    if (query.length() > 0) {
                        sendRestRequest(query);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 리스트뷰 클릭 관련
        restListView = findViewById(R.id.restListView);
        restListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), RestaurantDetailsActivity.class);
                JSONObject object = null;
                String resUrl = null;
                try {
                    object = array.getJSONObject(i);
                    resUrl = object.getString("place_url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("resUrl", resUrl);
                startActivity(intent);
            }
        });
    }

    public void setApiURL() { // API URL 세팅
        kakaoAPI = "https://dapi.kakao.com/v2/local/search/keyword.json?y=" + lat + "&x=" + lon + "&query=";
        gpsKakaoAPI = "https://dapi.kakao.com/v2/local/search/keyword.json?y=" + lat + "&x=" + lon + "&radius=20000&sort=distance&query=";
    }

    public void sendRestRequest(CharSequence query) { // 식당 요청 메소드

        String reqUrl = url + query;
        Log.d("uuu", "reqUrl"+reqUrl);

        final ProgressDialog dialog = new ProgressDialog(RestaurantActivity.this); // 진행 다이얼로그 생성
        dialog.setMessage("식당 검색 중...");
        dialog.show();

        // url로 데이터 요청
        StringRequest request = new StringRequest(Request.Method.GET, reqUrl, new Response.Listener<String>() {
            //응답을 잘 받았을 경우 아래 메소드가 자동으로 호출
            @Override
            public void onResponse(String response) {
                dialog.dismiss(); // 다이얼로그 끝냄
                try {
                    JSONObject res = new JSONObject(response); // response를 JSONObject로 생성
                    array = res.getJSONArray("documents"); // 식당들을 JSONArray에 저장
                    items.clear();
                    for(int i=0; i<array.length();i++){ // 검색 결과 식당들이 들어있는 array에서 데이터를 하나씩 꺼내 리스트뷰 아이템에 적용
                        JSONObject obj=array.getJSONObject(i);
                        items.add(new RestaurantActivity.Item(obj.getString("place_name"), obj.getString("category_name"), obj.getString("phone"), obj.getString("road_address_name"), obj.getString("distance")));
                    }
                    ItemAdapter adapter=new ItemAdapter(RestaurantActivity.this);
                    ListView listView=(ListView)findViewById(R.id.restListView);
                    listView.setAdapter(adapter);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
            @Override
            public void onErrorResponse(VolleyError error) { error.getMessage(); }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { // API 인증
                String authKey = "KakaoAK ###"; // 외부 유출 금지
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", authKey);
                return headers;
            }
        };
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);

    }

    // 현재 위치 주소 요청
    public void sendAddressRequest() {
        // gps 좌표 -> 주소(지번, 도로명)
        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x="+lon+"&y="+lat+"&input_coord=WGS84";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
            @Override
            public void onResponse(String response) {
                JSONObject res = null; // response를 JSONObject로 생성
                TextView gpsAddressText = findViewById(R.id.gpsAddressText);

                try {
                    res = new JSONObject(response);
                    addArray = res.getJSONArray("documents"); // JSONArray에 저장
                    JSONObject obj = addArray.getJSONObject(0);
                    if (!obj.isNull("road_address")) { // 도로명 주소가 있을 경우
                        gpsAddressText.setText(obj.getJSONObject("road_address").getString("address_name"));
                    } else {
                        gpsAddressText.setText(obj.getJSONObject("address").getString("address_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
            @Override
            public void onErrorResponse(VolleyError error) { error.getMessage(); }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { // API 인증
                String authKey = "KakaoAK ###"; // 외부 유출 금지
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", authKey);
                return headers;
            }
        };
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    }

    // 리스트뷰에 아이템을 뿌리기 위한 코드

    class Item{ // 리스트 아이템 클래스 정의
        String restNameText;
        String restCategoryText;
        String phoneText;
        String addressText;
        String distanceText;
        Item(String restNameText, String restCategoryText, String phoneText, String addressText, String distanceText){
            this.restNameText = restNameText;
            this.restCategoryText = restCategoryText;
            this.phoneText = phoneText;
            this.addressText = addressText;
            this.distanceText = distanceText;
        }
    }

    ArrayList<RestaurantActivity.Item> items = new ArrayList<RestaurantActivity.Item>(); // ArrayList 생성
    class ItemAdapter extends ArrayAdapter { // ArrayAdapter 클래스 확장
        public ItemAdapter(Context context) { // list_recipe_item.xml을 items 레이아웃으로 적용
            super(context, R.layout.list_restaurant_item, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) { // 각 레이아웃 뷰에 텍스트 및 이미지 적용
            View view = convertView;
            if(view == null){
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_restaurant_item, null);
            }
            TextView restNameText = view.findViewById(R.id.restNameText);
            TextView restCategoryText = view.findViewById(R.id.restCategoryText);
            TextView phoneText = view.findViewById(R.id.phoneText);
            TextView addressText = view.findViewById(R.id.addressText);
            TextView distanceText = view.findViewById(R.id.distanceText);

            restNameText.setText(items.get(position).restNameText);
            restCategoryText.setText(items.get(position).restCategoryText);
            phoneText.setText(items.get(position).phoneText);
            addressText.setText(items.get(position).addressText);
            distanceText.setText(items.get(position).distanceText+"m");
            return view;
        }
    }

}