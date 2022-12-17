package com.example.wte;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btn_cook; // 해 먹기 버튼
    private Button btn_restaurant; // 사 먹기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_cook = findViewById(R.id.btn_cook);
        btn_cook.setOnClickListener(new View.OnClickListener() { // CookActivity로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CookActivity.class);
                startActivity(intent);
            }
        });

        btn_restaurant = findViewById(R.id.btn_restaurant);
        btn_restaurant.setOnClickListener(new View.OnClickListener() { // RestaurantActivity로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RestaurantActivity.class);
                startActivity(intent);
            }
        });
    }
}