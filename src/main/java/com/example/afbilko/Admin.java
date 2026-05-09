package com.example.afbilko;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Admin extends AppCompatActivity {

    private LinearLayout containerSikayetler;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        db = FirebaseFirestore.getInstance();
        containerSikayetler = findViewById(R.id.containerSikayetler);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_admin);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        sikayetleriYukle();
    }

    private void sikayetleriYukle() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        db.collection("Sikayetler")
                .orderBy("tarih", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    containerSikayetler.removeAllViews();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ekleSikayetKarti(doc, sdf);
                    }
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Henüz şikayet bulunmuyor.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void ekleSikayetKarti(DocumentSnapshot doc, SimpleDateFormat sdf) {
        String sikayetId = doc.getId();
        String sikayetEdilenId = doc.getString("sikayetEdilen");
        String sikayetEdenId = doc.getString("sikayetEden");
        String sebep = doc.getString("sebep");
        String tarih = (doc.getTimestamp("tarih") != null) ? sdf.format(doc.getTimestamp("tarih").toDate()) : "";

        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 16, 16, 16);
        card.setLayoutParams(params);
        card.setRadius(20f);
        card.setCardElevation(10f);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        // --- ŞİKAYET BİLGİSİ ---
        TextView tvSikayetBaslik = new TextView(this);
        tvSikayetBaslik.setText("🚨 ŞİKAYET RAPORU");
        tvSikayetBaslik.setTextColor(Color.RED);
        tvSikayetBaslik.setTypeface(null, Typeface.BOLD);
        tvSikayetBaslik.setTextSize(16);

        TextView tvSikayetDetay = new TextView(this);
        tvSikayetDetay.setText("Neden: " + sebep + "\nTarih: " + tarih + "\nEdilen ID: " + sikayetEdilenId);
        tvSikayetDetay.setPadding(0, 10, 0, 20);

        layout.addView(tvSikayetBaslik);
        layout.addView(tvSikayetDetay);

        // --- ŞİKAYET EDEN KİŞİ BİLGİLERİ (DINAMIK) ---
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setGravity(Gravity.CENTER_VERTICAL);
        userLayout.setPadding(0, 20, 0, 20);

        ImageView imgProfil = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(120, 120);
        imgParams.setMargins(0, 0, 20, 0);
        imgProfil.setLayoutParams(imgParams);

        TextView tvUserInfo = new TextView(this);
        tvUserInfo.setText("Şikayet Eden Yükleniyor...");

        userLayout.addView(imgProfil);
        userLayout.addView(tvUserInfo);
        layout.addView(userLayout);

        // Veritabanından Şikayet Edenin Detaylarını Çek [cite: 64, 91]
        db.collection("Users").document(sikayetEdenId).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String ad = userDoc.getString("adSoyad");
                String foto = userDoc.getString("profilResmi");
                String mail = userDoc.getString("email");

                // Kayıt tarihi ve istatistik simülasyonu
                tvUserInfo.setText(ad + " (" + mail + ")\nŞikayet Eden ID: " + sikayetEdenId);
                if (foto != null && !foto.isEmpty()) {
                    Glide.with(this).load(foto).circleCrop().into(imgProfil);
                } else {
                    imgProfil.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        });

        // --- ADMIN İŞLEM BUTONLARI ---
        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionLayout.setGravity(Gravity.CENTER);

        // 1. BANLA BUTONU (Kalıcı)
        MaterialButton btnBan = new MaterialButton(this);
        btnBan.setText("KALICI BAN");
        btnBan.setBackgroundColor(Color.BLACK);
        btnBan.setTextColor(Color.WHITE);
        btnBan.setOnClickListener(v -> kullaniciIslemYap(sikayetEdilenId, "BANNED", sikayetId));

        // 2. DONDUR BUTONU (Askıya Al)
        MaterialButton btnSuspend = new MaterialButton(this);
        btnSuspend.setText("ASKIYA AL");
        btnSuspend.setBackgroundColor(Color.parseColor("#FF9800"));
        btnSuspend.setOnClickListener(v -> kullaniciIslemYap(sikayetEdilenId, "SUSPENDED", sikayetId));

        // 3. SİL/ARŞİVLE
        MaterialButton btnSil = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnSil.setText("KAPAT");
        btnSil.setOnClickListener(v -> db.collection("Sikayetler").document(sikayetId).delete().addOnSuccessListener(a -> sikayetleriYukle()));

        actionLayout.addView(btnBan);
        actionLayout.addView(btnSuspend);
        actionLayout.addView(btnSil);
        layout.addView(actionLayout);

        card.addView(layout);
        containerSikayetler.addView(card);
    }

    private void kullaniciIslemYap(String userId, String yeniDurum, String sikayetDocId) {
        // Kullanıcının durumunu güncelle
        db.collection("Users").document(userId).update("status", yeniDurum)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "İşlem Başarılı: " + yeniDurum, Toast.LENGTH_SHORT).show();

                    // Kullanıcıya mail gönderme taslağını aç
                    db.collection("Users").document(userId).get().addOnSuccessListener(doc -> {
                        String mail = doc.getString("email");
                        if (mail != null) {
                            mailGonder(mail, yeniDurum);
                        }
                    });

                    // Şikayeti temizle
                    db.collection("Sikayetler").document(sikayetDocId).delete().addOnSuccessListener(a -> sikayetleriYukle());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void mailGonder(String aliciMail, String durum) {
        String konu = "AFBİLKO - Hesap Durumu Hakkında Bilgilendirme";
        String mesaj = (durum.equals("BANNED")) ?
                "Sayın Kullanıcı, AFBİLKO platformu üzerindeki kural ihlalleriniz nedeniyle hesabınız KALICI olarak engellenmiştir." :
                "Sayın Kullanıcı, hesabınız şüpheli faaliyetler nedeniyle geçici olarak ASKIYA alınmıştır.";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{aliciMail});
        intent.putExtra(Intent.EXTRA_SUBJECT, konu);
        intent.putExtra(Intent.EXTRA_TEXT, mesaj);

        try {
            startActivity(Intent.createChooser(intent, "Bilgilendirme Maili Gönder..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Mail uygulaması bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }
}