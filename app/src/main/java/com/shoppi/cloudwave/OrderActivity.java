package com.shoppi.cloudwave;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://test-web-deploy-env.eu-central-1.elasticbeanstalk.com/";
    private Button orderButton;
    private Button combo1;
    private Button combo2;
    public String combo_name;
    public double combo_cost;
    public int combo_quantity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        orderButton = findViewById(R.id.orderButton);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrder();
            }
        });

        combo1 = findViewById(R.id.combo1);
        combo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                combo_name = "스몰세트 팝콘(M)1 + 탄산음료(M)1";
                combo_quantity = 1;
                combo_cost = 7000.0;

            }
        });

        combo2 = findViewById(R.id.combo2);
        combo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                combo_name = "라지세트 팝콘(L)1 + 탄산음료(L)1";
                combo_quantity = 1;
                combo_cost = 15000.0;
            }
        });
    }

    // set api context
    private void createOrder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OrderApiService orderApiService = retrofit.create(OrderApiService.class);

        // 주문 정보 생성
        Order order = new Order();
        // Intent에서 USER_ID_KEY로 저장된 값을 가져옵니다.
        String userId = getIntent().getStringExtra("USER_ID_KEY");
        // 값을 확인하려면 Log나 Toast를 사용할 수 있습니다.
        Log.d("OrderActivity", "Received User ID: " + userId);

        order.setCustomerId(userId);
        order.setFoodItem(combo_name);
        order.setQuantity(combo_quantity);
        order.setTotalPrice(combo_cost);
        Log.d("OrderActivity", "Received User ID: " + combo_name);
        Log.d("OrderActivity", "Received User ID: " + combo_quantity);
        Log.d("OrderActivity", "Received User ID: " + combo_cost);

        Call<Order> call = orderApiService.placeOrder(order);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 응답 받았을 경우 처리
                    Order savedOrder = response.body();
                    Toast.makeText(OrderActivity.this, "주문이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 에러 처리
                    Toast.makeText(OrderActivity.this, "주문 요청이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                // 네트워크 실패 처리
                Toast.makeText(OrderActivity.this, "네트워크 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



