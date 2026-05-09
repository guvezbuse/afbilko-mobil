package com.example.afbilko;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Anasayfa_gonulluluk extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ChipGroup chipGroup;
    private Button btnBuAlandaAra;
    private FloatingActionButton btnGoogleMapsAc;
    private Polyline mevcutRota;

    private String sonMesafe = "";
    private String sonSure = "";

    private String mevcutKullaniciAd = "";
    private String mevcutKullaniciFoto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (!user.isEmailVerified()) {
                    new AlertDialog.Builder(this)
                            .setTitle("⚠️ Erişim Engellendi")
                            .setMessage("Gönüllülük sekmesine erişmek için e-postanızı doğrulamanız gerekmektedir.")
                            .setCancelable(false)
                            .setPositiveButton("Profile Git", (dialog, which) -> {
                                startActivity(new Intent(Anasayfa_gonulluluk.this, Anasayfa_profil.class));
                                finish();
                            })
                            .setNegativeButton("Kapat", (dialog, which) -> finish())
                            .show();
                }
            });
        }

        setContentView(R.layout.anasayfa_gonulluluk);
        db = FirebaseFirestore.getInstance();
        chipGroup = findViewById(R.id.chipGroupFiltre);
        btnBuAlandaAra = findViewById(R.id.btnBuAlandaAra);
        btnGoogleMapsAc = findViewById(R.id.btnGoogleMapsAc);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapGonullu);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        kullaniciBilgileriniOnYukle();

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mMap == null) return;
            talepleriGetir(getSecilenFiltre(checkedId), null);
            gizleRotaButonu();
        });

        btnBuAlandaAra.setOnClickListener(v -> {
            if (mMap == null) return;
            LatLng haritaMerkezi = mMap.getCameraPosition().target;
            cemberCiz(haritaMerkezi);
            talepleriGetir(getSecilenFiltre(chipGroup.getCheckedChipId()), haritaMerkezi);
            btnBuAlandaAra.setVisibility(View.GONE);
        });

        btnGoogleMapsAc.setOnClickListener(v -> navigasyonuBaslat());
    }

    private void kullaniciBilgileriniOnYukle() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            mevcutKullaniciAd = doc.getString("adSoyad");
                            mevcutKullaniciFoto = doc.getString("profilResmi");
                        }
                    });
        }
    }

    private void navigasyonuBaslat() {
        if (mevcutRota != null && !mevcutRota.getPoints().isEmpty()) {
            LatLng hedef = mevcutRota.getPoints().get(mevcutRota.getPoints().size() - 1);
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + hedef.latitude + "," + hedef.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }

    private void gizleRotaButonu() {
        if (mevcutRota != null) mevcutRota.remove();
        btnGoogleMapsAc.setVisibility(View.GONE);
    }

    private String getSecilenFiltre(int checkedId) {
        if (checkedId == R.id.chipGida) return "Gıda Yardımı";
        if (checkedId == R.id.chipSu) return "Su Yardımı";
        if (checkedId == R.id.chipIlac) return "İlaç/Tıbbi Yardım";
        if (checkedId == R.id.chipEnkaz) return "Enkaz Altındayım/Acil";
        if (checkedId == R.id.chipTahliye) return "Tahliye Talebi";
        if (checkedId == R.id.chipBarinma) return "Barınma Talebi";
        if (checkedId == R.id.chipDiger) return "Diğer Yardım Talepleri";
        return "";
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // --- YENİ GÜNCELLEME: KONUM BUTONUNA BASINCA SIFIRLAMA ---
        mMap.setOnMyLocationButtonClickListener(() -> {
            mMap.clear(); // Çemberi ve işaretçileri temizle
            gizleRotaButonu(); // Mevcut rotayı ve navigasyon butonunu gizle
            btnBuAlandaAra.setVisibility(View.VISIBLE); // Arama butonunu geri getir
            talepleriGetir("", null); // Tüm talepleri tekrar yükle
            return false; // False: Kamera konuma odaklanmaya devam eder
        });

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnInfoWindowClickListener(marker -> {
            DocumentSnapshot doc = (DocumentSnapshot) marker.getTag();
            if (doc == null) return;

            String[] secenekler = {"Yol Tarifi Çiz", "Yardım Ulaştı (Kapat)", "Asılsız/Trol Bildir"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(marker.getTitle());
            builder.setItems(secenekler, (dialog, which) -> {
                switch (which) {
                    case 0: yolTarifiAl(marker); break;
                    case 1: yardimiKapat(marker); break;
                    case 2: trolRaporla(doc, marker); break;
                }
            });
            builder.show();
        });

        LatLng bartin = new LatLng(41.63, 32.33);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bartin, 13f));
        talepleriGetir("", null);
    }

    private void yolTarifiAl(Marker marker) {
        Location myLocation = mMap.getMyLocation();
        if (myLocation != null) {
            LatLng origin = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            String url = getDirectionsUrl(origin, marker.getPosition());
            new FetchUrl().execute(url);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14f));
        }
    }

    private void trolRaporla(DocumentSnapshot doc, Marker marker) {
        String[] sebepler = {"Trol / Asılsız İçerik", "Konumda Kimse Yok", "Uygunsuz Dil"};
        new AlertDialog.Builder(this).setTitle("Asılsız Talebi Bildir").setItems(sebepler, (dialog, which) -> {
            Map<String, Object> report = new HashMap<>();
            report.put("sikayetEden", mAuth.getUid());
            report.put("sikayetEdilen", doc.getString("userId"));
            report.put("talepId", doc.getId());
            report.put("sebep", sebepler[which]);
            report.put("tarih", com.google.firebase.Timestamp.now());
            report.put("sikayetEdenAd", mevcutKullaniciAd);
            report.put("sikayetEdenFoto", mevcutKullaniciFoto);
            db.collection("Sikayetler").add(report).addOnSuccessListener(d -> {
                Toast.makeText(this, "Şikayet iletildi.", Toast.LENGTH_SHORT).show();
                marker.remove();
            });
        }).show();
    }

    private void yardimiKapat(Marker marker) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentSnapshot doc = (DocumentSnapshot) marker.getTag();
        if (currentUser == null || doc == null) return;
        doc.getReference().update("durum", "Tamamlandı", "gonulluId", currentUser.getUid())
                .addOnSuccessListener(aVoid -> {
                    marker.remove();
                    gizleRotaButonu();
                    Toast.makeText(Anasayfa_gonulluluk.this, "Yardım tamamlandı.", Toast.LENGTH_SHORT).show();
                });
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude + "&destination=" + dest.latitude + "," + dest.longitude + "&mode=driving&key=AIzaSyAD_pTfz48iSWMiJlY3WLoYCRMwDUvkZPI";
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... url) {
            String data = "";
            try { data = downloadUrl(url[0]); } catch (Exception e) { Log.e("API_ERROR", e.toString()); }
            return data;
        }
        @Override protected void onPostExecute(String result) { new ParserTask().execute(result); }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) { sb.append(line); }
            data = sb.toString();
            br.close();
        } catch (Exception e) { e.printStackTrace(); }
        finally {
            if (iStream != null) iStream.close();
            if (urlConnection != null) urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<LatLng>> {
        @Override protected List<LatLng> doInBackground(String... jsonData) {
            List<LatLng> routes = null;
            try {
                JSONObject jObject = new JSONObject(jsonData[0]);
                if (jObject.getString("status").equals("OK")) {
                    JSONObject leg = jObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                    sonMesafe = leg.getJSONObject("distance").getString("text");
                    sonSure = leg.getJSONObject("duration").getString("text");
                }
                routes = new DirectionsJSONParser().parse(jObject);
            } catch (Exception e) { e.printStackTrace(); }
            return routes;
        }
        @Override protected void onPostExecute(List<LatLng> points) {
            if (mevcutRota != null) mevcutRota.remove();
            if (points != null && !points.isEmpty()) {
                mevcutRota = mMap.addPolyline(new PolylineOptions().addAll(points).width(15).color(Color.BLUE).geodesic(true));
                btnGoogleMapsAc.setVisibility(View.VISIBLE);
                Toast.makeText(Anasayfa_gonulluluk.this, "Mesafe: " + sonMesafe + " - Süre: " + sonSure, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void talepleriGetir(String filtre, LatLng merkezKonum) {
        db.collection("YardimTalepleri").whereEqualTo("durum", "Beklemede").orderBy("tarih", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (mMap == null) return;
                    mMap.clear();
                    if (merkezKonum != null) cemberCiz(merkezKonum);
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("enlem");
                        Double lng = doc.getDouble("boylam");
                        String tur = doc.getString("yardimTuru");

                        if (lat == null || lng == null || tur == null) continue;

                        if (!filtre.isEmpty() && !tur.equals(filtre)) continue;

                        if (merkezKonum != null) {
                            float[] res = new float[1];
                            Location.distanceBetween(merkezKonum.latitude, merkezKonum.longitude, lat, lng, res);
                            if (res[0] > 50000) continue; // 50 km filtreleme
                        }

                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title(tur)
                                .snippet(doc.getString("aciklama"))
                                .icon(getMarkerIcon(tur)));

                        if (m != null) m.setTag(doc);
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", e.getMessage()));
    }

    private BitmapDescriptor getMarkerIcon(String tur) {
        String emoji = "📍";
        if (tur.contains("Gıda")) emoji = "🍞";
        else if (tur.contains("Su")) emoji = "💧";
        else if (tur.contains("İlaç")) emoji = "💊";
        else if (tur.contains("Tahliye")) emoji = "🚗";
        else if (tur.contains("Barınma")) emoji = "⛺";
        else if (tur.contains("Enkaz")) emoji = "🆘";
        else if (tur.contains("Diğer")) emoji = "📦";
        Bitmap bitmap = Bitmap.createBitmap(110, 110, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(); paint.setTextSize(90);
        canvas.drawText(emoji, 0, 90, paint);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void cemberCiz(LatLng merkez) {
        // 50 km yarıçaplı kırmızı çember
        mMap.addCircle(new CircleOptions().center(merkez).radius(50000).strokeColor(Color.RED).fillColor(0x22FF0000).strokeWidth(2));
    }
}