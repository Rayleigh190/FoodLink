package com.example.wte;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

public class CookDetailsActivity extends AppCompatActivity {
    JSONObject object;
    TextView[] manualTextArray = new TextView[20];
    ImageView[] manualImgArray = new ImageView[20];
    TextView partsText; // 재료
    TextView patText; // 요리종류
    TextView wayText; // 조리방법
    TextView recipeText; // 요리이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_details);

        for(int i=1; i<21; i++){ // activity_cook_details.xml에서 각각 20개의 텍스트뷰와 이미지뷰를 찾아옴
            int getTextId = getResources().getIdentifier("manualText" + i, "id", "com.example.wte");
            int getImgId = getResources().getIdentifier("manualImg" + i, "id", "com.example.wte");
            manualTextArray[i-1] = findViewById(getTextId);
            manualImgArray[i-1] = findViewById(getImgId);
        }

        Intent intent = getIntent();
        try {
            object = new JSONObject(intent.getStringExtra("recipe")); // intent를 통해 데이터를 받아옴
        } catch (JSONException e) {
            e.printStackTrace();
        }
        partsText = findViewById(R.id.partsText);
        patText = findViewById(R.id.patText);
        wayText = findViewById(R.id.wayText);
        recipeText = findViewById(R.id.recipeText);
        try { // 요리이름, 재료, 종류, 방법 데이터 세팅
            recipeText.setText(object.getString("RCP_NM") + "\n");
            partsText.setText("재료 : " + object.getString("RCP_PARTS_DTLS").replace("\n", " ") + "\n");
            patText.setText("요리 종류 : " + object.getString("RCP_PAT2"));
            wayText.setText("조리 방법 : " + object.getString("RCP_WAY2") + "\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 레시피 메뉴얼 텍스트와 이미지를 세팅
        for(int i=1; i<21; i++){
            try {
                if (i<10){ // MANUAL01 ~ MANUAL09, MANUAL_IMG01 ~ MANUAL_IMG09 데이터 세팅
                    if (object.getString("MANUAL0" + i).toString().length() > 1) { // 데이터가 있을 경우 세팅
                        manualTextArray[i-1].setText(object.getString("MANUAL0" + i).replace("\n", " "));
                    } else { // 데이터가 없을 경우 뷰를 숨김
                        manualTextArray[i-1].setVisibility(View.GONE);
                    }
                    if (object.get("MANUAL_IMG0" + i).toString().length() > 1) { // 데이터가 있을 경우 세팅
                        Glide.with(CookDetailsActivity.this).load(object.get("MANUAL_IMG0" + i)).placeholder(R.drawable.ic_baseline_collections_24).error(R.drawable.food_logo3).into(manualImgArray[i-1]); // Glide로 이미지 표시하기
                    } else { // 데이터가 없을 경우 뷰를 숨김
                        manualImgArray[i-1].setVisibility(View.GONE);
                    }

                } else { // MANUAL10 ~ MANUAL20, MANUAL_IMG10 ~ MANUAL_IMG20 데이터 세팅
                    if (object.getString("MANUAL" + i).toString().length() > 1) { // 데이터가 있을 경우 세팅
                        manualTextArray[i-1].setText(object.getString("MANUAL" + i).replace("\n", " "));
                    } else { // 데이터가 없을 경우 뷰를 숨김
                        manualTextArray[i-1].setVisibility(View.GONE);
                    }
                    if (object.get("MANUAL_IMG" + i).toString().length() > 1) { // 데이터가 있을 경우 세팅
                        Glide.with(CookDetailsActivity.this).load(object.get("MANUAL_IMG" + i)).placeholder(R.drawable.ic_baseline_collections_24).error(R.drawable.food_logo3).into(manualImgArray[i-1]); // Glide로 이미지 표시하기
                    } else { // 데이터가 없을 경우 뷰를 숨김
                        manualImgArray[i-1].setVisibility(View.GONE);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}