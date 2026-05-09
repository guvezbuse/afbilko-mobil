package com.example.afbilko;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Anasayfa_giris_kayit extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton btnGirisYap;
    private MaterialButton btnKayitOl;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- OTURUM VE BAN KONTROLÜ (Zaten giriş yapılmış mı?) ---
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserStatusAndNavigate(currentUser);
            return;
        }

        setContentView(R.layout.anasayfa_giris_kayit);

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        btnGirisYap = findViewById(R.id.btn_giris_yap);
        btnKayitOl = findViewById(R.id.btn_kayit_ol);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_giris);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // --- GİRİŞ YAP BUTONU ---
        btnGirisYap.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                editTextEmail.setError("E-posta boş bırakılamaz.");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                editTextPassword.setError("Şifre boş bırakılamaz.");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserStatusAndNavigate(user);
                            }
                        } else {
                            Toast.makeText(Anasayfa_giris_kayit.this, "Giriş başarısız: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // --- KAYIT OL BUTONU ---
        btnKayitOl.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                editTextEmail.setError("E-posta boş bırakılamaz.");
                return;
            }

            if (!isStrongPassword(password)) {
                editTextPassword.setError("Şifre en az 8 karakter olmalı; büyük/küçük harf, rakam ve özel karakter (. @ # $ % ^ & + = !) içermelidir.");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                user.sendEmailVerification();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("email", email);
                                userMap.put("userId", uid);
                                userMap.put("adSoyad", "");
                                userMap.put("status", "ACTIVE"); // Yeni kullanıcılar varsayılan olarak aktif

                                db.collection("Users").document(uid)
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Anasayfa_giris_kayit.this, "Kayıt başarılı! Doğrulama e-postası gönderildi.", Toast.LENGTH_LONG).show();

                                            Intent mainIntent = new Intent(Anasayfa_giris_kayit.this, MainActivity.class);
                                            startActivity(mainIntent);

                                            Intent profilIntent = new Intent(Anasayfa_giris_kayit.this, Anasayfa_profil.class);
                                            startActivity(profilIntent);

                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Veritabanı hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(Anasayfa_giris_kayit.this, "Kayıt başarısız: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // --- KRİTİK: BAN VE ASKIYA ALMA KONTROL METODU ---
    private void checkUserStatusAndNavigate(FirebaseUser user) {
        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");

                        if ("BANNED".equals(status)) {
                            mAuth.signOut();
                            Toast.makeText(Anasayfa_giris_kayit.this, "Hesabınız kural ihlalleri nedeniyle KALICI olarak engellenmiştir.", Toast.LENGTH_LONG).show();
                        } else if ("SUSPENDED".equals(status)) {
                            mAuth.signOut();
                            Toast.makeText(Anasayfa_giris_kayit.this, "Hesabınız şüpheli faaliyetler nedeniyle geçici olarak ASKIYA alınmıştır.", Toast.LENGTH_LONG).show();
                        } else {
                            // Durum normalse (ACTIVE veya null ise) giriş yap
                            Toast.makeText(Anasayfa_giris_kayit.this, "Giriş başarılı!", Toast.LENGTH_SHORT).show();

                            Intent mainIntent = new Intent(Anasayfa_giris_kayit.this, MainActivity.class);
                            startActivity(mainIntent);

                            Intent profilIntent = new Intent(Anasayfa_giris_kayit.this, Anasayfa_profil.class);
                            startActivity(profilIntent);

                            finish();
                        }
                    } else {
                        // Kullanıcı dokümanı yoksa (Hatalı durum)
                        mAuth.signOut();
                        Toast.makeText(this, "Kullanıcı profili bulunamadı.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Bağlantı hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isStrongPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.])(?=\\S+$).{8,}$";
        return password != null && password.matches(passwordPattern);
    }
}