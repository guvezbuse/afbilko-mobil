package com.example.afbilko;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Profil_bilgi_guncelle extends AppCompatActivity {

    private TextInputEditText editTextAdSoyad;
    private TextInputEditText editTextYeniEmail;
    private TextInputEditText editTextYeniSifre;
    private TextInputEditText editTextMevcutSifre;
    private MaterialButton btnKaydet;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profil_bilgi_guncelle);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextAdSoyad = findViewById(R.id.edit_text_ad_soyad);
        editTextYeniEmail = findViewById(R.id.edit_text_yeni_email);
        editTextYeniSifre = findViewById(R.id.edit_text_yeni_sifre);
        editTextMevcutSifre = findViewById(R.id.edit_text_mevcut_sifre);
        btnKaydet = findViewById(R.id.btn_kaydet);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_guncelle);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        btnKaydet.setOnClickListener(v -> {
            String adSoyad = editTextAdSoyad.getText().toString().trim();
            String yeniEmail = editTextYeniEmail.getText().toString().trim();
            String yeniSifre = editTextYeniSifre.getText().toString().trim();
            String mevcutSifre = editTextMevcutSifre.getText().toString().trim();

            if (TextUtils.isEmpty(adSoyad) && TextUtils.isEmpty(yeniEmail) && TextUtils.isEmpty(yeniSifre)) {
                Toast.makeText(this, "Güncellemek için en az bir alanı doldurun.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(mevcutSifre)) {
                editTextMevcutSifre.setError("Değişiklik için mevcut şifrenizi girmelisiniz.");
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            // KİMLİK DOĞRULAMA (Re-authentication)
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), mevcutSifre);
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {

                    // 1. AD SOYAD GÜNCELLEME
                    if (!TextUtils.isEmpty(adSoyad)) {
                        db.collection("Users").document(user.getUid())
                                .update("adSoyad", adSoyad)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ad soyad güncellendi.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("adSoyad", adSoyad);
                                    db.collection("Users").document(user.getUid()).set(data, SetOptions.merge());
                                });
                    }

                    // 2. E-POSTA GÜNCELLEME
                    if (!TextUtils.isEmpty(yeniEmail)) {
                        user.verifyBeforeUpdateEmail(yeniEmail).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Doğrulama e-postası gönderildi.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    // 3. ŞİFRE GÜNCELLEME
                    if (!TextUtils.isEmpty(yeniSifre)) {
                        if (!isStrongPassword(yeniSifre)) {
                            editTextYeniSifre.setError("Şifre en az 8 karakter olmalı; büyük/küçük harf, rakam ve özel karakter içermelidir.");
                            Toast.makeText(this, "Şifre kriterleri karşılamıyor!", Toast.LENGTH_LONG).show();
                            editTextYeniSifre.requestFocus();
                            return; // Kriter uymuyorsa metottan çık, alttaki işlemleri yapma
                        }

                        user.updatePassword(yeniSifre).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Şifre başarıyla güncellendi.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    Toast.makeText(this, "İşlemler başarıyla başlatıldı.", Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(this::finish, 1500);

                } else {
                    editTextMevcutSifre.setError("Mevcut şifreniz yanlış.");
                }
            });
        });
    }

    private boolean isStrongPassword(String password) {
        // Regex: En az 8 karakter, 1 büyük harf, 1 küçük harf, 1 rakam ve 1 özel karakter
        // Nokta (.) karakteri de özel karakter listesine eklendi
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.])(?=\\S+$).{8,}$";
        return password != null && password.matches(passwordPattern);
    }
}