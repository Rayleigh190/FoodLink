package com.example.wte;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

    String[] sortItems = {"정확도순", "거리순"}; // 리스트뷰 정렬 종류
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
        setApiURL(); // 새로운 좌표로 api url 세팅
        sendAddressRequest(); // 좌표에 해당하는 주소 요청

        // GPS 및 위치 관련
        final ProgressDialog dialog = new ProgressDialog(RestaurantActivity.this); // 진행 다이얼로그 생성
        dialog.setMessage("위치 검색 중...");
        dialog.show();
        Log.d("Build.MODEL", android.os.Build.MODEL);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) { // gps 좌표가 업데이트 됐을 때 위도, 경도 변수 업데이트
                if (westernmost<location.getLongitude() && location.getLongitude()<easternmost &&
                        southernmost<location.getLatitude() && location.getLatitude()<northernmost) { // 대한민국 안의 좌표 일때만
                    lat = Double.toString(location.getLatitude()); // 위도
                    lon = Double.toString(location.getLongitude()); // 경도
                    Log.d("gps", "lat: "+lat);
                    Log.d("gps", "lon: "+lon);
                    setApiURL(); // 새로운 좌표로 api url 세팅
                    url = kakaoAPI;
                    Log.d("api", "kakaoAPI"+kakaoAPI);
                    Log.d("api", "gpsKakaoAPI"+gpsKakaoAPI);
                    sendAddressRequest(); // 현재 좌표에 해당하는 주소 요청
                    dialog.dismiss();
                    // GPS_PROVIDER로 부터 좌표를 받으면 locationManager에서 locationListener 제거
                    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {locationManager.removeUpdates(locationListener); }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한 허용 확인
            return;
        }
        if (!android.os.Build.MODEL.contains("sdk")){ // 기기가 애뮬레이터가 아닐 경우에는 NETWORK_PROVIDER도 사용
            // NETWORK_PROVIDER로 부터 1초마다 현재 좌표를 받아옴
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 100, locationListener);
        }
        // 기기가 애뮬레이터 일 경우 GPS_PROVIDER만 사용
        // GPS_PROVIDER로 부터 1초마다 현재 좌표를 받아옴
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener);

        // 식당 검색 관련
        restSearchView = findViewById(R.id.restSearchesView);
        restSearchView.setSubmitButtonEnabled(true);

        restSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // 검색 버튼을 눌렀을 경우
                TextView textInfo = findViewById(R.id.textInfo);
                textInfo.setVisibility(View.GONE); // 안내 문구 완전히 숨기기
                restSearchView.clearFocus(); // 키보드 내리기
                sendRestRequest(query); // 검색어에 해당하는 식당 정보 요청
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
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item); // 미리 정의된 레이아웃 사용
        spinner.setAdapter(adapter); // 스피너 객체에다가 어댑터를 넣기
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 스피너가 선택 됐을 때
                if (position == 0){ // "정확도순"이 선택 되었을 때
                    url = kakaoAPI; // api 요청 url 변경
                    CharSequence query = restSearchView.getQuery();
                    if (query.length() > 0) { // SearchView에 검색어가 입력 됐을 경우
                        sendRestRequest(query); // 검색어에 해당하는 식당 정보 요청
                    }
                } else { // "거리순"이 선택 되었을 때
                    url = gpsKakaoAPI; // api 요청 url 변경
                    CharSequence query = restSearchView.getQuery();
                    if (query.length() > 0) { // SearchView에 검색어가 입력 됐을 경우
                        sendRestRequest(query); // 검색어에 해당하는 식당 정보 요청
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
                // 리스트뷰 아이템이 클릭 되었을 때 해당 장소 url 정보와 함께 RestaurantDetailsActivity로 이동
                Intent intent = new Intent(getApplicationContext(), RestaurantDetailsActivity.class);
                JSONObject object = null;
                String resUrl = null;
                try {
                    object = array.getJSONObject(i);
                    resUrl = object.getString("place_url"); // 클릭된 아이템 장소 url을 가져옴
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("resUrl", resUrl); // url 정보를 intent에 넣음
                startActivity(intent); // RestaurantDetailsActivity 실행
            }
        });
    }

    // API URL 세팅
    public void setApiURL() {
        // "정확도순" 요청 api
        kakaoAPI = "https://dapi.kakao.com/v2/local/search/keyword.json?y=" + lat + "&x=" + lon + "&query=";
        // "거리순" 요청 api
        gpsKakaoAPI = "https://dapi.kakao.com/v2/local/search/keyword.json?y=" + lat + "&x=" + lon + "&radius=20000&sort=distance&query=";
    }

    // 식당 정보 요청 메소드
    public void sendRestRequest(CharSequence query) {

        String reqUrl = url + query; // 요청 url에 검색어 삽입
        Log.d("api", "reqUrl: "+reqUrl);

        final ProgressDialog dialog = new ProgressDialog(RestaurantActivity.this); // 진행 다이얼로그 생성
        dialog.setMessage("식당 검색 중...");
        dialog.show();

        // reqUrl로 데이터 요청
        StringRequest request = new StringRequest(Request.Method.GET, reqUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // 요청에 대해 응답이 왔을 때
                dialog.dismiss(); // 다이얼로그 끝냄
                try {
                    JSONObject res = new JSONObject(response); // response를 JSONObject로 생성
                    array = res.getJSONArray("documents"); // 식당들을 JSONArray에 저장
                    items.clear();
                    for(int i=0; i<array.length();i++){ // 검색 결과 식당들이 들어있는 array에서 데이터를 하나씩 꺼내 리스트뷰 아이템에 적용
                        JSONObject obj=array.getJSONObject(i);
                        // JSONObject로 부터 식당 이름, 카테고리, 번호, 주소, 거리 데이터를 꺼내 items에 세팅
                        items.add(new RestaurantActivity.Item(obj.getString("place_name"), obj.getString("category_name"), obj.getString("phone"), obj.getString("road_address_name"), obj.getString("distance")));
                    }
                    ItemAdapter adapter=new ItemAdapter(RestaurantActivity.this);
                    ListView listView=(ListView)findViewById(R.id.restListView);
                    listView.setAdapter(adapter);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { // 에러 발생시
            @Override
            public void onErrorResponse(VolleyError error) { error.getMessage(); }
        }) {
            // kakao API 사용자 인증 관련
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String authKey = "KakaoAK ###"; // 외부 유출 금지
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", authKey); // 헤더에 삽입
                return headers;
            }
        };
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    }

    // 현재 위치 주소 요청 메소드
    public void sendAddressRequest() {
        // gps 좌표 -> 주소(지번, 도로명)
        // 요청 apu url
        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x="+lon+"&y="+lat+"&input_coord=WGS84";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // 요청에 대한 응답이 왔을 때
                JSONObject res = null; // response를 JSONObject로 생성
                TextView gpsAddressText = findViewById(R.id.gpsAddressText);

                try {
                    res = new JSONObject(response);
                    addArray = res.getJSONArray("documents"); // JSONArray에 저장
                    JSONObject obj = addArray.getJSONObject(0);
                    if (!obj.isNull("road_address")) { // 도로명 주소가 있을 경우
                        gpsAddressText.setText(obj.getJSONObject("road_address").getString("address_name"));
                    } else { // 도로명 주소가 없으면 지번 주소 사용
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                // kakao API 사용자 인증 관련
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
        String restNameText; // 식당 이름
        String restCategoryText; // 식당 카테고리
        String phoneText; // 식당 번호
        String addressText; // 식당 주소
        String distanceText; // 식당 거리
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
        public ItemAdapter(Context context) { // list_restaurant_item.xml을 items 레이아웃으로 적용
            super(context, R.layout.list_restaurant_item, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 각 레이아웃 뷰에 텍스트 및 이미지 적용
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