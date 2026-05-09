package com.example.afbilko;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;

import com.example.afbilko.Afetler.Deprem;
import com.example.afbilko.Afetler.Yangin;
import com.example.afbilko.Afetler.Sel;
import com.example.afbilko.Afetler.Heyelan;

public class Anasayfa_afetler extends AppCompatActivity {

    // CardView nesneleri
    CardView cardDeprem, cardYangin, cardSel, cardHeyelan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa_afetler); // Buradaki layout dosyanın adıyla aynı olmalı

        // CardView'ları bağlama
        cardDeprem = findViewById(R.id.cardDeprem);
        cardYangin = findViewById(R.id.cardYangin);
        cardSel = findViewById(R.id.cardSel);
        cardHeyelan = findViewById(R.id.cardHeyelan);

        // Card tıklama olayları
        cardDeprem.setOnClickListener(v -> {
            startActivity(new Intent(Anasayfa_afetler.this, Deprem.class));
        });

        cardYangin.setOnClickListener(v -> {
            startActivity(new Intent(Anasayfa_afetler.this, Yangin.class));
        });

        cardSel.setOnClickListener(v -> {
            startActivity(new Intent(Anasayfa_afetler.this, Sel.class));
        });

        cardHeyelan.setOnClickListener(v -> {
            startActivity(new Intent(Anasayfa_afetler.this, Heyelan.class));
        });
    }
}
