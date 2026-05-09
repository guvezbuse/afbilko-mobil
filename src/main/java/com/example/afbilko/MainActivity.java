package com.example.afbilko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // XML bileşenleri
    CardView btnYardim, btnAfetler, btnGonulluluk, btnToplanma, btnNeredeyim, btnTalepOlustur, btnAdminPaneli;
    MaterialButton btnGiris_kayit, btnProfil;
    FloatingActionButton btnChatbot;
    TextView btnHakkimizda;

    private FirebaseAuth mAuth;

    // Admin UID
    private final String ADMIN_UID = "YRHA3YMVp4UeKtq1R6gd3YZ5xdw1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // ID Bağlantıları
        btnYardim = findViewById(R.id.btnYardim);
        btnAfetler = findViewById(R.id.btnAfetler);
        btnGonulluluk = findViewById(R.id.btnGonulluluk);
        btnToplanma = findViewById(R.id.btnToplanma);
        btnTalepOlustur = findViewById(R.id.btnTalepOlustur);
        btnNeredeyim = findViewById(R.id.btnNeredeyim);
        btnAdminPaneli = findViewById(R.id.btnAdminPaneli);

        btnGiris_kayit = findViewById(R.id.btnGiris_kayit);
        btnProfil = findViewById(R.id.btnProfil);
        btnChatbot = findViewById(R.id.btnChatbot);
        btnHakkimizda = findViewById(R.id.btnHakkimizda);

        // --- ADMİN KONTROLÜ ---
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(ADMIN_UID)) {
            btnAdminPaneli.setVisibility(View.VISIBLE);
            btnAdminPaneli.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, Admin.class));
            });
        } else {
            btnAdminPaneli.setVisibility(View.GONE);
        }

        // --- TIKLAMA OLAYLARI ---
        btnYardim.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_yardim.class)));
        btnAfetler.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_afetler.class)));
        btnGonulluluk.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_gonulluluk.class)));
        btnToplanma.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_toplanma.class)));
        btnTalepOlustur.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_talep_olustur.class)));
        btnNeredeyim.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_neredeyim.class)));
        btnProfil.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_profil.class)));
        btnGiris_kayit.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_giris_kayit.class)));
        btnChatbot.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_chatbot.class)));
        btnHakkimizda.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Anasayfa_hakkimizda.class)));
    }
}