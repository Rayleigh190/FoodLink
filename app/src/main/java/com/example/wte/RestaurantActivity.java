package com.example.wte;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class RestaurantActivity extends AppCompatActivity {
    String[] sortItems = {"정확도순", "거리순"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        Spinner spinner = findViewById(R.id.sortSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item); //미리 정의된 레이아웃 사용
        spinner.setAdapter(adapter); // 스피너 객체에다가 어댑터를 넣기
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 스피너가 선택 됐을 때
//                textView.setText(items[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { // 아무것도 선택되지 않은 상태일 때
//                textView.setText("선택: ");
            }
        });

    }
}