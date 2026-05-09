package com.example.afbilko;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Anasayfa_hakkimizda extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bu satır, hazırladığın anasayfa_hakkimizda.xml dosyasını ekrana bağlar
        setContentView(R.layout.anasayfa_hakkimizda);

        // Geri butonunu XML'den sildiğin için burada herhangi bir
        // findViewById veya setOnClickListener işlemine gerek yoktur.
        // Kullanıcı telefonun geri tuşuna bastığında Android otomatik olarak bu sayfayı kapatır.
    }
}
