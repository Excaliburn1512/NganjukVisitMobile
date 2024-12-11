package com.polije.sem3.main_menu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import com.polije.sem3.R;

public class WelcomeScreen extends AppCompatActivity {
    private Handler handler = new Handler();
    private int sentenceIndex = 0;
    private int charIndex = 0;
    private Runnable typeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        TextView textView = findViewById(R.id.textwelcome);
        Button tombolPindah = findViewById(R.id.btnStart);

        // Array kalimat yang akan ditampilkan
        String[] sentences = {
                "KOTA NGANJUK",
                "KOTA ADIPURA",
                "KOTA ANGIN"
        };

        int durationPerCharacter = 150;
        int pauseBetweenSentences = 1000;

        typeText = new Runnable() {
            @Override
            public void run() {
                if (charIndex < sentences[sentenceIndex].length()) {
                    textView.setText(sentences[sentenceIndex].substring(0, charIndex + 1));
                    charIndex++;
                    handler.postDelayed(this, durationPerCharacter); // Tunda per karakter
                } else {
                    // Jika selesai mengetik, jeda sejenak sebelum melanjutkan ke kalimat berikutnya
                    handler.postDelayed(() -> {
                        charIndex = 0;
                        sentenceIndex = (sentenceIndex + 1) % sentences.length; // Pindah ke kalimat berikutnya
                        handler.post(typeText); // Lanjutkan mengetik
                    }, pauseBetweenSentences);
                }
            }
        };

        // Mulai animasi teks
        handler.post(typeText);
        tombolPindah.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(WelcomeScreen.this, Login.class);
                startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}