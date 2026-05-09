package com.example.afbilko;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Anasayfa_talep_olustur extends AppCompatActivity {

    private CardView cardGida, cardSu, cardIlac, cardTahliye, cardBarinma, cardDiger, cardEnkaz;
    private TextInputEditText etTalepDetay;
    private ImageButton btnSesleYaz;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;

    private double enlem = 0.0;
    private double boylam = 0.0;
    private String mevcutAdres = "Konum bekleniyor...";
    private static final int SPEECH_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa_talep_olustur);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etTalepDetay = findViewById(R.id.etTalepDetay);
        btnSesleYaz = findViewById(R.id.btnSesleYaz);

        cardGida = findViewById(R.id.cardGida);
        cardSu = findViewById(R.id.cardSu);
        cardIlac = findViewById(R.id.cardIlac);
        cardTahliye = findViewById(R.id.cardTahliye);
        cardBarinma = findViewById(R.id.cardBarinma);
        cardDiger = findViewById(R.id.cardDiger);
        cardEnkaz = findViewById(R.id.cardEnkaz);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_talep);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        konumuAl();

        btnSesleYaz.setOnClickListener(v -> sesliGirisBaslat());

        // KRİTİK: Her kart tıklamasında mail onay kontrolü yapılır
        cardGida.setOnClickListener(v -> dogrulamaKontroluIleGonder("Gıda Yardımı"));
        cardSu.setOnClickListener(v -> dogrulamaKontroluIleGonder("Su Yardımı"));
        cardIlac.setOnClickListener(v -> dogrulamaKontroluIleGonder("İlaç/Tıbbi Yardım"));
        cardTahliye.setOnClickListener(v -> dogrulamaKontroluIleGonder("Tahliye Talebi"));
        cardBarinma.setOnClickListener(v -> dogrulamaKontroluIleGonder("Barınma Talebi"));
        cardDiger.setOnClickListener(v -> dogrulamaKontroluIleGonder("Diğer Yardım Talepleri"));
        cardEnkaz.setOnClickListener(v -> dogrulamaKontroluIleGonder("Enkaz Altındayım/Acil"));
    }

    private void dogrulamaKontroluIleGonder(String tur) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // 1. Durum: Giriş yapılmış, onay kontrol ediliyor
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    talepGonder(tur);
                } else {
                    Toast.makeText(this, "⚠️ Önce e-posta doğrulaması yapmanız gerekmektedir.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // 2. Durum: Giriş YAPILMAMIŞ (user null ise burası çalışır)
            Toast.makeText(this, "⚠️ Talep oluşturmak için önce giriş yapmalısınız.", Toast.LENGTH_LONG).show();

            // Kullanıcıyı direkt giriş sayfasına gönderelim ki işi kolaylaşsın
            Intent intent = new Intent(this, Anasayfa_giris_kayit.class);
            startActivity(intent);
        }
    }

    private void sesliGirisBaslat() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "İhtiyacınızı söyleyin...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Cihazınız sesli girişi desteklemiyor.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etTalepDetay.setText(result.get(0));
            }
        }
    }

    private void talepGonder(String yardimTuru) {
        String aciklama = etTalepDetay.getText().toString().trim();
        if (aciklama.isEmpty()) {
            etTalepDetay.setError("Lütfen bir açıklama yazın.");
            return;
        }
        if (enlem == 0.0 || boylam == 0.0) {
            Toast.makeText(this, "Konum bekleniyor...", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> talep = new HashMap<>();
        talep.put("userId", mAuth.getCurrentUser().getUid());
        talep.put("kullaniciEmail", mAuth.getCurrentUser().getEmail());
        talep.put("yardimTuru", yardimTuru);
        talep.put("aciklama", aciklama);
        talep.put("enlem", enlem);
        talep.put("boylam", boylam);
        talep.put("adres", mevcutAdres);
        talep.put("durum", "Beklemede");
        talep.put("tarih", com.google.firebase.Timestamp.now());

        db.collection("YardimTalepleri").add(talep)
                .addOnSuccessListener(d -> {
                    Toast.makeText(this, "Talebiniz iletildi.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void konumuAl() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            enlem = location.getLatitude();
                            boylam = location.getLongitude();
                            new Thread(() -> {
                                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                                try {
                                    List<Address> adresler = geocoder.getFromLocation(enlem, boylam, 1);
                                    if (adresler != null && !adresler.isEmpty()) {
                                        mevcutAdres = adresler.get(0).getAddressLine(0);
                                    }
                                } catch (IOException e) { mevcutAdres = "Koordinat işaretlendi"; }
                            }).start();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }
}