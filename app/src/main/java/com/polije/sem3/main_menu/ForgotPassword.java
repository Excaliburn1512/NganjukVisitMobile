package com.polije.sem3.main_menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.polije.sem3.R;
import com.polije.sem3.model.Verification;
import com.polije.sem3.response.VerificationResponse;
import com.polije.sem3.network.Client;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends AppCompatActivity {

    private AppCompatImageButton btnBack;
    private Button btnSubmit;
    private EditText txtEmail;
    private String emailUser;
    private ProgressDialog progressDialog;
    private ImageView airplane;

    // model data
    private Verification verification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(ForgotPassword.this);
        progressDialog.setTitle("Proses Permintaan OTP...");
        progressDialog.setMessage("Harap Tunggu");
        progressDialog.setCancelable(false);

        // Bind views
        airplane = findViewById(R.id.airplane);
        btnBack = findViewById(R.id.backButton);
        btnSubmit = findViewById(R.id.btnSubmitOTP);
        txtEmail = findViewById(R.id.txtemails);
        emailUser = "";

        // Back Button click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPassword.this, Login.class);
                startActivity(intent);
            }
        });

        // Submit Button click listener
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailUser = txtEmail.getText().toString().trim();

                // Validate email input
                if (TextUtils.isEmpty(emailUser)) {
                    Toast.makeText(ForgotPassword.this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(airplane, "rotation", 0f, 360f);
                rotateAnimator.setDuration(500);
                rotateAnimator.start();
                animasipesawat();
                Client.getInstance().sendmailotp(emailUser).enqueue(new Callback<VerificationResponse>() {
                    @Override
                    public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                        progressDialog.dismiss();
                        if (response.body() != null && response.body().getStatus().equalsIgnoreCase("true")) {
                            String otp = response.body().getData().getOtp();
                            long startMillis = 1677000000000L;
                            long endMillis = startMillis + 600000;
                            String endMillisString = String.valueOf(endMillis);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ForgotPassword.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(ForgotPassword.this, OtpVerification.class);
                            intent.putExtra(OtpVerification.EMAIL_USER, emailUser);
                            intent.putExtra(OtpVerification.OTP_USER, otp);
                            intent.putExtra(OtpVerification.END_MILLIS, endMillis);
                            startActivity(intent);
                        } else {
                            animasiKembali();
                            Toast.makeText(ForgotPassword.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VerificationResponse> call, Throwable t) {
                        animasiKembali();
                        Log.e("ForgotPassword", "Error: " + t.getMessage(), t);
                        runOnUiThread(() -> {
                            if (t instanceof SocketTimeoutException) {
                                Toast.makeText(ForgotPassword.this, "Koneksi timeout. Periksa koneksi internet Anda.", Toast.LENGTH_SHORT).show();
                            } else if (t instanceof UnknownHostException) {
                                Toast.makeText(ForgotPassword.this, "Tidak dapat terhubung ke server. Periksa koneksi internet.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ForgotPassword.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
    public void animasipesawat(){
        ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(airplane, "translationX", 0f, 1000f);
        translateXAnimator.setDuration(3000);ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(airplane, "translationY", 0f, -1000f);
        translateYAnimator.setDuration(3000);
        translateXAnimator.start();
        translateYAnimator.start();
    }
    private void animasiKembali() {
        ObjectAnimator rotateBackAnimator = ObjectAnimator.ofFloat(airplane, "rotation", 360f, 0f);
        rotateBackAnimator.setDuration(500);
        ObjectAnimator translateBackX = ObjectAnimator.ofFloat(airplane, "translationX", airplane.getTranslationX(), 0f);
        translateBackX.setDuration(3000);
        ObjectAnimator translateBackY = ObjectAnimator.ofFloat(airplane, "translationY", airplane.getTranslationY(), 0f);
        translateBackY.setDuration(3000);
        rotateBackAnimator.start();
        translateBackX.start();
        translateBackY.start();
    }
}
