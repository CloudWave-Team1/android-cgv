package com.shoppi.cloudwave;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class AuthActivity extends AppCompatActivity {

    private final String TAG = AuthActivity.class.getSimpleName();
    private static final String BASE_URL = "http://test-web-deploy-env.eu-central-1.elasticbeanstalk.com/"; // 여기에 실제 백엔드 주소를 적어주세요
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button signIn_button = findViewById(R.id.signIn_button); // 로그인 버튼
        Button signUp_button = findViewById(R.id.signUp_button); // 회원가입 버튼
        Button forgot_Password_button = findViewById(R.id.forgot_Password_button); // 비밀번호를 잊어버리셨나요?

        // 로그인이 되어있는지 확인
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());

                // 로그인이 되어있으면 MainActivity 로 이동
                if (userStateDetails.getUserState() == UserState.SIGNED_IN) {
                    Intent i = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        // 로그인 버튼
        signIn_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSignIn();
            }
        });

        // 회원가입 버튼
        signUp_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(AuthActivity.this, SignUpActivity.class);
                startActivity(i);
                finish();
            }

        });

        // 비밀번호를 잊어버리셨나요?
        forgot_Password_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(AuthActivity.this, ForgotActivity.class);
                startActivity(i);
                finish();
            }

        });
    }


    // 로그인 함수
    private void showSignIn() {

        // 아이디 비밀번호 순
        EditText login_id = findViewById(R.id.login_id);
        EditText login_paw = findViewById(R.id.login_paw);

        String username = login_id.getText().toString();
        String password = login_paw.getText().toString();


        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                        switch (signInResult.getSignInState()) {
                            case DONE:
                                Toast.makeText(getApplicationContext(), "Sign-in done.", Toast.LENGTH_SHORT).show();
                                fetchAndSendToken();
                                Intent i = new Intent(AuthActivity.this, MainActivity.class);
                                startActivity(i);
                                finish();
                                break;
                            case SMS_MFA:
                                Toast.makeText(getApplicationContext(), "Please confirm sign-in with SMS.", Toast.LENGTH_SHORT).show();
                                break;
                            case NEW_PASSWORD_REQUIRED:
                                Toast.makeText(getApplicationContext(), "Please confirm sign-in with new password.", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Unsupported sign-in confirmation: " + signInResult.getSignInState(), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Sign-in error", e);
            }
        });
    }


    private void fetchAndSendToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }
            String token = task.getResult();

            // 현재 로그인한 사용자의 아이디 가져오기
            String userId = AWSMobileClient.getInstance().getUsername();

            Intent orderActivityIntent = new Intent(AuthActivity.this, OrderActivity.class);
            orderActivityIntent.putExtra("USER_ID_KEY", userId);
            startActivity(orderActivityIntent);

            // Retrofit 초기화
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            TokenService tokenService = retrofit.create(TokenService.class);

            // 서버에 토큰 정보 전송
            tokenService.sendTokenInfo(userId, token).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(response.isSuccessful()) {
                        Log.d(TAG, "Token info sent successfully");
                    } else {
                        try {
                            Log.e(TAG, "Failed to send token info: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading errorBody", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Error sending token info", t);
                }
            });
        });
    }


    interface TokenService {
        @POST("register/token")
        Call<Void> sendTokenInfo(@Query("customerId") String customerId, @Query("token") String token);
    }


}