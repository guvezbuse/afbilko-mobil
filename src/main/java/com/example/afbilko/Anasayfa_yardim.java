package com.example.afbilko;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.afbilko.Yardim.Afbilko_nedir;
import com.example.afbilko.Yardim.Afet_bilinci;
import com.example.afbilko.Yardim.Afet_cantasi;
import com.example.afbilko.Yardim.Gonulluluk;
import com.example.afbilko.Yardim.Psikolojik_destek;
import com.example.afbilko.Yardim.Yardimlasma;

public class Anasayfa_yardim extends AppCompatActivity {

    // CardView nesneleri
    CardView cardAfbilkoNedir, cardAfetBilinci, cardAfetCantasi, cardYardimlasma, cardGonulluluk, cardPsikolojikDestek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa_yardim); // Yardim activity'nin XML dosyası

        // CardView'ları bağlama
        cardAfbilkoNedir = findViewById(R.id.cardAfbilkoNedir);
        cardAfetBilinci = findViewById(R.id.cardAfetBilinci);
        cardAfetCantasi = findViewById(R.id.cardAfetCantasi);
        cardYardimlasma = findViewById(R.id.cardYardimlasma);
        cardGonulluluk = findViewById(R.id.cardGonulluluk);
        cardPsikolojikDestek = findViewById(R.id.cardPsikolojikDestek);

        // Card tıklama olayları
        cardAfbilkoNedir.setOnClickListener(v ->
                startActivity(new Intent(Anasayfa_yardim.this, Afbilko_nedir.class)));

        cardAfetBilinci.setOnClickListener(v ->
                startActivity(new Intent(Anasayfa_yardim.this, Afet_bilinci.class)));

        cardAfetCantasi.setOnClickListener(v ->
                startActivity(new Intent(Anasayfa_yardim.this, Afet_cantasi.class)));

        cardYardimlasma.setOnClickListener(v ->
                startActivity(new Intent(Anasayfa_yardim.this, Yardimlasma.class)));

        cardGonulluluk.setOnClickListener(v ->
                startActivity(new Intent(Anasayfa_yardim.this, Gonulluluk.class)));

        cardPsikolojikDestek.setOnClickListener(v ->
                startActivity(new Intent(Anasayfa_yardim.this, Psikolojik_destek.class)));
    }
}
