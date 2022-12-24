package com.example.wte;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class RestaurantDetailsActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        Intent intent = getIntent();
        String resUrl = intent.getStringExtra("resUrl"); // intent를 통해 식당 url 데이터를 받아옴

        webView = (WebView) findViewById(R.id.restWebView);
        // 웹뷰 관련 설정들
        webView.setWebViewClient(new WebViewClient()); // 새 창 띄우기 않기
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setLoadWithOverviewMode(true); // WebView 화면크기에 맞추도록 설정
        webView.getSettings().setUseWideViewPort(true); // wide viewport 설정
        webView.getSettings().setSupportZoom(false); // 줌 설정 여부
        webView.getSettings().setBuiltInZoomControls(false); // 줌 확대/축소 버튼 여부
        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용 여부
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // javascript가 window.open()을 사용할 수 있도록 설정
        webView.getSettings().setSupportMultipleWindows(true); // 멀티 윈도우 사용 여부
        webView.getSettings().setDomStorageEnabled(true); // 로컬 스토리지 사용 여부
        // 웹뷰에서 GPS 권한 허용
        webView.getSettings().setGeolocationEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });

        webView.loadUrl(resUrl); // 웹페이지 호출
    }
    @Override
    public void onBackPressed() { // 뒤로가기 키 눌렀을 경우
        if(webView.canGoBack()){ // 뒤로 갈 페이지가 있으면
            // 뒤로 이동
            webView.goBack();
        }else{
            // 없으면 이전 액티비티로 이동
            super.onBackPressed();
        }
    }
}