package com.example.afbilko;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Anasayfa_profil extends AppCompatActivity {

    private TextView textViewKullaniciAdi, textViewEmail, tvTalepSayisi, tvKarsilananSayisi;
    private ImageView imageViewProfilFoto;
    private LinearLayout containerGecmis;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ActivityResultLauncher<String> galleryLauncher;

    private String mevcutKullaniciAd = "";
    private String mevcutKullaniciFoto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(Anasayfa_profil.this, Anasayfa_giris_kayit.class));
            finish();
            return;
        }

        setContentView(R.layout.anasayfa_profil);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        textViewKullaniciAdi = findViewById(R.id.text_view_kullanici_adi);
        textViewEmail = findViewById(R.id.text_view_email);
        tvTalepSayisi = findViewById(R.id.tv_talep_sayisi);
        tvKarsilananSayisi = findViewById(R.id.tv_karsilanan_sayisi);
        imageViewProfilFoto = findViewById(R.id.image_view_profil_foto);
        containerGecmis = findViewById(R.id.containerGecmis);

        MaterialButton btnCikisYap = findViewById(R.id.btn_cikis_yap);
        MaterialButton btnBilgileriGuncelle = findViewById(R.id.btn_bilgileri_guncelle);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_profil);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        kullaniciBilgileriniYukle();
        istatistikleriYukle();
        gecmisiDinamikYukle();

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageViewProfilFoto.setImageURI(uri);
                resmiStorageYukle(uri);
            }
        });

        imageViewProfilFoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnBilgileriGuncelle.setOnClickListener(v -> {
            Intent intent = new Intent(Anasayfa_profil.this, Profil_bilgi_guncelle.class);
            startActivity(intent);
        });

        btnCikisYap.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Anasayfa_profil.this, Anasayfa_giris_kayit.class));
            finishAffinity();
        });
    }

    private void istatistikleriYukle() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        db.collection("YardimTalepleri").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snapshots -> tvTalepSayisi.setText(String.valueOf(snapshots.size())));
        db.collection("YardimTalepleri").whereEqualTo("gonulluId", uid).get()
                .addOnSuccessListener(snapshots -> tvKarsilananSayisi.setText(String.valueOf(snapshots.size())));
    }

    private void gecmisiDinamikYukle() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        db.collection("YardimTalepleri")
                .orderBy("tarih", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    containerGecmis.removeAllViews();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String olusturanId = doc.getString("userId");
                        String gonulluId = doc.getString("gonulluId");
                        if (uid.equals(olusturanId) || uid.equals(gonulluId)) {
                            ekleGecmisKart(doc, sdf, uid);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Hata: " + e.getMessage()));
    }

    private void ekleGecmisKart(DocumentSnapshot doc, SimpleDateFormat sdf, String uid) {
        String docId = doc.getId();
        String durum = doc.getString("durum");
        String olusturanId = doc.getString("userId");
        String gonulluId = doc.getString("gonulluId");

        boolean isBenimTalebim = uid.equals(olusturanId);
        boolean isBenKarsiladim = uid.equals(gonulluId);
        boolean isTamamlandi = "Tamamlandı".equals(durum);

        int arkaPlan, vurguRenk;
        String rolText;

        if (isBenKarsiladim) {
            arkaPlan = Color.parseColor("#E3F2FD");
            vurguRenk = Color.parseColor("#1565C0");
            rolText = "🔵 YARDIM ULAŞTIRDINIZ";
        } else if (isBenimTalebim && isTamamlandi) {
            arkaPlan = Color.parseColor("#E8F5E9");
            vurguRenk = Color.parseColor("#2E7D32");
            rolText = "✅ TALEBİNİZ KARŞILANDI";
        } else {
            arkaPlan = Color.parseColor("#FFEBEE");
            vurguRenk = Color.parseColor("#B71C1C");
            rolText = "🔴 TALEP ETTİNİZ (BEKLEMEDE)";
        }

        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 12, 16, 12);
        card.setLayoutParams(params);
        card.setRadius(20f);
        card.setCardElevation(8f);
        card.setCardBackgroundColor(arkaPlan);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 25, 35, 25);

        TextView tvBaslik = new TextView(this);
        tvBaslik.setText(doc.getString("yardimTuru"));
        tvBaslik.setTextSize(17);
        tvBaslik.setTypeface(null, Typeface.BOLD);
        tvBaslik.setTextColor(vurguRenk);

        TextView tvDetay = new TextView(this);
        String aciklama = doc.getString("aciklama");
        String adres = doc.getString("adres") != null ? doc.getString("adres") : "Konum bilgisi yok";
        String tarih = (doc.getTimestamp("tarih") != null) ? sdf.format(doc.getTimestamp("tarih").toDate()) : "";

        tvDetay.setText(String.format("%s\n📍 %s\n💬 %s\n%s", tarih, adres, aciklama, rolText));
        tvDetay.setTextColor(Color.parseColor("#424242"));
        tvDetay.setTextSize(14);
        tvDetay.setLineSpacing(1.1f, 1.1f);

        layout.addView(tvBaslik);
        layout.addView(tvDetay);

        // --- SİLME BUTONU (Sadece kendi bekleyen talepleri için) ---
        if (isBenimTalebim && !isTamamlandi) {
            MaterialButton btnSil = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnSil.setText("Talebi İptal Et");
            btnSil.setTextColor(Color.RED);
            btnSil.setStrokeColorResource(android.R.color.holo_red_light);
            btnSil.setIconResource(android.R.drawable.ic_menu_delete);
            btnSil.setIconTintResource(android.R.color.holo_red_light);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.gravity = Gravity.END;
            btnParams.topMargin = 10;
            btnSil.setLayoutParams(btnParams);

            btnSil.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Talebi Sil")
                        .setMessage("Bu yardım talebini silmek istediğinize emin misiniz?")
                        .setPositiveButton("Evet, Sil", (dialog, which) -> {
                            db.collection("YardimTalepleri").document(docId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Talep silindi.", Toast.LENGTH_SHORT).show();
                                        gecmisiDinamikYukle();
                                        istatistikleriYukle();
                                    });
                        })
                        .setNegativeButton("Vazgeç", null)
                        .show();
            });
            layout.addView(btnSil);
        }

        // --- SENARYO 1: YARDIM GELMEDİ RAPORU (Afetzede için) ---
        if (isBenimTalebim && isTamamlandi) {
            MaterialButton btnSikayetUlasmadi = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnSikayetUlasmadi.setText("YARDIM GELMEDİ!");
            btnSikayetUlasmadi.setTextColor(Color.parseColor("#D84315"));
            btnSikayetUlasmadi.setIconResource(android.R.drawable.ic_dialog_alert);

            LinearLayout.LayoutParams rpParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rpParams.gravity = Gravity.END;
            btnSikayetUlasmadi.setLayoutParams(rpParams);

            btnSikayetUlasmadi.setOnClickListener(v -> {
                Map<String, Object> report = new HashMap<>();
                report.put("sikayetEden", uid);
                report.put("sikayetEdilen", gonulluId);
                report.put("talepId", docId);
                report.put("sebep", "Yardım tamamlandı işaretlendi ama bana ulaşmadı.");
                report.put("tarih", com.google.firebase.Timestamp.now());
                report.put("sikayetEdenAd", mevcutKullaniciAd);
                report.put("sikayetEdenFoto", mevcutKullaniciFoto);

                db.collection("Sikayetler").add(report).addOnSuccessListener(d ->
                        Toast.makeText(this, "Şikayet iletildi.", Toast.LENGTH_SHORT).show());
            });
            layout.addView(btnSikayetUlasmadi);
        }

        // --- SENARYO 2: GENEL RAPOR ET (Sadece Kırmızı/Bekleyen Başka Talepler İçin) ---
        // isBenKarsiladim (Mavi kart) ise buton görünmeyecek.
        if (!isBenimTalebim && !isBenKarsiladim) {
            MaterialButton btnRapor = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnRapor.setText("Rapor Et");
            btnRapor.setIconResource(android.R.drawable.stat_notify_error);
            btnRapor.setTextColor(Color.DKGRAY);

            LinearLayout.LayoutParams rpParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rpParams.gravity = Gravity.END;
            btnRapor.setLayoutParams(rpParams);

            btnRapor.setOnClickListener(v -> sikayetDialogGoster(olusturanId, docId));
            layout.addView(btnRapor);
        }

        card.addView(layout);
        containerGecmis.addView(card);
    }

    private void sikayetDialogGoster(String sucluId, String talepId) {
        new AlertDialog.Builder(this)
                .setTitle("Trol Bildir")
                .setItems(new String[]{"Trol / Asılsız İçerik", "Uygunsuz Dil", "Yalan Beyan", "Diğer"}, (dialog, which) -> {
                    String[] sebepler = {"Trol / Asılsız İçerik", "Uygunsuz Dil", "Yalan Beyan", "Diğer"};
                    Map<String, Object> report = new HashMap<>();
                    report.put("sikayetEden", mAuth.getUid());
                    report.put("sikayetEdilen", sucluId);
                    report.put("talepId", talepId);
                    report.put("sebep", sebepler[which]);
                    report.put("tarih", com.google.firebase.Timestamp.now());
                    report.put("sikayetEdenAd", mevcutKullaniciAd);
                    report.put("sikayetEdenFoto", mevcutKullaniciFoto);

                    db.collection("Sikayetler").add(report)
                            .addOnSuccessListener(doc -> Toast.makeText(this, "Şikayet iletildi.", Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    private void kullaniciBilgileriniYukle() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String onayDurumu = currentUser.isEmailVerified() ? " (Onaylı ✅)" : " (Onaylanmadı ❌ - Tekrar Gönder)";
            textViewEmail.setText(currentUser.getEmail() + onayDurumu);

            if (!currentUser.isEmailVerified()) {
                textViewEmail.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Onay Maili")
                            .setMessage("Doğrulama mailini tekrar göndermek ister misiniz?")
                            .setPositiveButton("Evet", (d, w) -> {
                                currentUser.sendEmailVerification().addOnSuccessListener(a ->
                                        Toast.makeText(this, "Mail gönderildi.", Toast.LENGTH_SHORT).show());
                            }).show();
                });
            }

            db.collection("Users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            mevcutKullaniciAd = doc.getString("adSoyad");
                            mevcutKullaniciFoto = doc.getString("profilResmi");
                            if (mevcutKullaniciAd != null) textViewKullaniciAdi.setText(mevcutKullaniciAd);
                            if (mevcutKullaniciFoto != null && !mevcutKullaniciFoto.isEmpty()) {
                                Glide.with(Anasayfa_profil.this).load(mevcutKullaniciFoto).into(imageViewProfilFoto);
                            }
                        }
                    });
        }
    }

    private void resmiStorageYukle(Uri uri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        StorageReference fileRef = storageReference.child("profile_images/" + user.getUid() + ".jpg");
        fileRef.putFile(uri).addOnSuccessListener(task -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            db.collection("Users").document(user.getUid()).update("profilResmi", downloadUri.toString());
            Toast.makeText(Anasayfa_profil.this, "Fotoğraf buluta kaydedildi!", Toast.LENGTH_SHORT).show();
        }));
    }
}