package com.example.wte;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CookActivity extends AppCompatActivity {
    ListView listView;
    SearchView searchView;
    JSONArray array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { // 리스트뷰 아이템이 클릭 되었을때 해당 아이템 데이터를 CookDetailsActivity 넘겨줌
                Intent intent = new Intent(getApplicationContext(), CookDetailsActivity.class);
                JSONObject object = null;
                try {
                    object = array.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("recipe", String.valueOf(object));
                startActivity(intent);
            }
        });

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // 검색버튼을 눌렀을 경우
                TextView textInfo = findViewById(R.id.textInfo);
                textInfo.setVisibility(View.GONE); // 안내 문구 완전히 숨기기
                searchView.clearFocus(); // 키보드 포커스 제거
                sendRequest(query); // 입력한 검색어로 레시피 검색 요청을 보냄
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        if(AppHelper.requestQueue != null) { // RequestQueue 생성
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

    }

    public void sendRequest(String query) { // 레시피 요청 메소드
        String food = query;
        // 레시피 요청 API 주소
        String url = "https://openapi.foodsafetykorea.go.kr/api/###/COOKRCP01/json/1/10/RCP_NM=" + food;

        final ProgressDialog dialog = new ProgressDialog(CookActivity.this); // 진행 다이얼로그 생성
        dialog.setMessage("레시피 검색 중...");
        dialog.show();

        // url로 데이터 요청
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            //응답을 잘 받았을 경우 아래 메소드가 자동으로 호출
            @Override
            public void onResponse(String response) {
                dialog.dismiss(); // 다이얼로그 끝냄
                try {
                    JSONObject res = new JSONObject(response); // response를 JSONObject로 생성
                    JSONObject cookRcp = res.getJSONObject("COOKRCP01");
                    array = cookRcp.getJSONArray("row"); // 레시피들을 JSONArray에 저장
                    items.clear();
                    for(int i=0; i<array.length();i++){ // 검색 결과 레시피들이 들어있는 array에서 데이터를 하나씩 꺼내 리스트뷰 아이템에 적용
                        JSONObject obj=array.getJSONObject(i);
                        items.add(new CookActivity.Item(obj.getString("RCP_NM"), obj.getString("ATT_FILE_NO_MAIN"), obj.getString("RCP_PAT2")));
                    }
                    ItemAdapter adapter=new ItemAdapter(CookActivity.this);
                    ListView listView=(ListView)findViewById(R.id.listView);
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
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                return params;
            }
        };
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);

    }

// 리스트뷰에 아이템을 뿌리기 위한 코드

    class Item{ // 리스트 아이템 클래스 정의
        String recipe_name;
        String recipe_img_url;
        String recipe_type;
        Item(String recipe_name, String imageUrl, String recipe_type){
            this.recipe_name = recipe_name;
            this.recipe_img_url = imageUrl;
            this.recipe_type = recipe_type;
        }
    }

    ArrayList<Item> items = new ArrayList<Item>(); // ArrayList 생성
    class ItemAdapter extends ArrayAdapter { // ArrayAdapter 클래스 확장
        public ItemAdapter(Context context) { // list_recipe_item.xml을 items 레이아웃으로 적용
            super(context, R.layout.list_recipe_item, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) { // 각 레이아웃 뷰에 텍스트 및 이미지 적용
            View view = convertView;
            if(view == null){
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_recipe_item, null);
            }
            TextView recipe_name = view.findViewById(R.id.recipe_name);
            ImageView recipe_img = view.findViewById(R.id.recipe_img);
            TextView recipe_type = view.findViewById(R.id.recipe_type);
            recipe_name.setText(items.get(position).recipe_name);
            recipe_type.setText(items.get(position).recipe_type);
            Glide.with(CookActivity.this).load(items.get(position).recipe_img_url).placeholder(R.drawable.ic_baseline_collections_24).error(R.drawable.food_logo1).into(recipe_img); // Glide로 이미지 표시하기
            return view;
        }
    }

}

// Volley RequestQueue를 사용하기 위한 클래스
class AppHelper {
    public static RequestQueue requestQueue;
}