package com.example.afbilko;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Anasayfa_chatbot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvTypingStatus; // Yeni tanımlama
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa_chatbot);

        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvTypingStatus = findViewById(R.id.tvTypingStatus); // XML bağlantısı

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addMessage("Merhaba! Ben AFBİLKO Asistan. Size nasıl yardımcı olabilirim?", false, false);

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                addMessage(msg, true, false);
                etMessage.setText("");

                // 1. "Yazıyor..." yazısını göster (Instagram tarzı)
                tvTypingStatus.setVisibility(View.VISIBLE);

                // 2. Gecikme başlat (1.2 saniye bekletelim daha doğal dursun)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    // 3. Yazıyı gizle ve cevabı ekle
                    tvTypingStatus.setVisibility(View.GONE);
                    generateResponse(msg);

                }, 1200);
            }
        });
    }

    private void addMessage(String text, boolean isUser, boolean isEmergency) {
        messageList.add(new ChatMessage(text, isUser, isEmergency));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void generateResponse(String msg) {
        String reply = "Bu konuda henüz sistemimde bir bilgi tanımlı değil. Lütfen daha spesifik bir kelime deneyin (örn: 'navigasyon', 'kalp masajı', 'gaz sızıntısı').";
        String lower = msg.toLowerCase();
        boolean isEmergency = false;

        // --- 1. UYGULAMA KULLANIM REHBERİ (TEKNİK DESTEK) ---
        if (lower.contains("nasıl kullanılır") || lower.contains("rehber") || lower.contains("yardım")) {
            reply = "AFBİLKO Kullanım Rehberi:\n" +
                    "1. **Talepler:** Haritadan yardım ihtiyaçlarını görün.\n" +
                    "2. **Toplanma:** Size en yakın güvenli alanları bulun.\n" +
                    "3. **Gönüllülük:** Yardım ulaştırmak için rota çizebilirsiniz.\n" +
                    "4. **Neredeyim:** Konumunuzu SMS ile paylaşabilirsiniz.\n" +
                    "5. **Chatbot:** Afetler hakkında bana soru sorabilirsiniz.";
        }
        else if (lower.contains("navigasyon") || lower.contains("yol tarifi") || lower.contains("nasıl giderim")) {
            reply = "Yol tarifi almak için haritadaki bir ikona tıklayın, 'Yol Tarifi Al' butonuna basın. Ardından sağ altta beliren Google Haritalar (FAB) butonuna dokunarak navigasyonu başlatın.";
        }
        else if (lower.contains("mesaj") || lower.contains("sms") || lower.contains("konum gönder") || lower.contains("konumum")) {
            reply = "Konumunuzu paylaşmak için 'Neredeyim' sayfasına gidin. 'Konum Gönder' butonuna basarak rehberinizdeki kişilere enlem/boylam bilgilerinizi hızlıca SMS atabilirsiniz.";
        }
        else if (lower.contains("filtre") || lower.contains("gıda") || lower.contains("su") || lower.contains("ilaç")) {
            reply = "Filtreleme yapmak için 'Gönüllülük' sayfasının üstündeki Chip butonlarını (Gıda, Su vb.) kullanın. Harita sadece seçtiğiniz ihtiyaçları gösterecektir.";
        }

        // --- 2. HAYATİ TEHLİKE VE KRİTİK ACİL DURUMLAR ---
        else if (lower.contains("enkaz") || lower.contains("altındayım") || lower.contains("sıkıştım") || lower.contains("göçük")) {
            reply = "PANİK YAPMAYIN. 1) Nefesinizi kontrol edin. 2) Ritmik şekilde (3 kez) sert cisimlere vurun. 3) Dışarıdan ses gelene kadar bağırmayın. 4) Tozdan korunmak için ağzınızı kapatın.";
            isEmergency = true;
        }
        else if (lower.contains("kanama") || lower.contains("yaralandım") || lower.contains("turnike")) {
            reply = "İLK YARDIM: Temiz bir bezle yaranın üzerine bastırın. Kan durmuyorsa kalp seviyesinden yukarı kaldırın. Bilginiz varsa turnike uygulayın ve 112'yi arayın.";
            isEmergency = true;
        }
        else if (lower.contains("kalp masajı") || lower.contains("nefes almıyor")) {
            reply = "ACİL: 112'yi arayın. Hastayı sert zemine yatırın. Göğüs kemiğinin ortasına dakikada 100-120 bası olacak şekilde ritmik baskı uygulayın. Bırakmayın!";
            isEmergency = true;
        }

        // --- 3. DOĞAL AFETLER VE HAZIRLIK ---
        else if (lower.contains("deprem") || lower.contains("sarsıntı")) {
            if (lower.contains("hazırlık") || lower.contains("önce")) {
                reply = "HAZIRLIK: Eşyaları sabitleyin. Deprem sigortanızı yaptırın. Aile tahliye planı yapın ve afet çantanızı yatağınızın yakınında tutun.";
            } else {
                reply = "ANINDA: ÇÖK-KAPAN-TUTUN yapın. Merdiven ve asansörden uzak durun. Sarsıntı bitince gaz/elektrik vanalarını kapatıp toplanma alanına gidin.";
            }
        }
        else if (lower.contains("yangın") || lower.contains("itfaiye") || lower.contains("duman")) {
            reply = "YANGIN: 112'yi arayın. Duman varsa yere yakın ilerleyin (aşağıda temiz hava bulunur). Islak bezle ağzınızı kapatın. Kapıları kapatarak ilerleyin.";
            isEmergency = true;
        }
        else if (lower.contains("sel") || lower.contains("su baskını")) {
            reply = "SEL: Yüksek yerlere çıkın. Sel suyu içinde yürümeyin veya araç kullanmayın. Elektrik kaynaklarından ve kablolardan uzak durun.";
        }
        else if (lower.contains("çanta") || lower.contains("malzeme")) {
            reply = "AFET ÇANTASI: 72 saat yetecek su, kuru gıda, ilk yardım kiti, fener, radyo, düdük, nakit para ve önemli evrak fotokopileri bulunmalıdır.";
        }

        // --- 4. ÖZEL DURUMLAR ---
        else if (lower.contains("psikoloji") || lower.contains("korku") || lower.contains("panik")) {
            reply = "Korkmanız normal bir tepkidir. Derin nefes alın. Güvendiğiniz kişilerle iletişimde kalın. AFAD psikososyal destek ekipleri sahada olacaktır.";
        }
        else if (lower.contains("çocuk") || lower.contains("bebek")) {
            reply = "ÇOCUKLARLA İLETİŞİM: Onlara dürüst ama sakin olun. Oyun oynayarak vakit geçirin ve güvende olduklarını hissettirin.";
        }
        else if (lower.contains("merhaba") || lower.contains("selam") || lower.contains("kimsin")) {
            reply = "Merhaba! Ben AFBİLKO Dijital Asistanı. Size nasıl yardımcı olabilirim?";
        }

        addMessage(reply, false, isEmergency);
    }
}