package com.example.afbilko;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Anasayfa_neredeyim extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvAdres;
    private MaterialButton btnKonumuPaylas;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private String mevcutAdres = "";
    private double mevcutEnlem = 0.0;
    private double mevcutBoylam = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa_neredeyim);

        tvAdres = findViewById(R.id.tv_adres);
        btnKonumuPaylas = findViewById(R.id.btn_konumu_paylas);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_neredeyim);

        toolbar.setNavigationOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Paylaş butonunun güncellenmiş hali
        btnKonumuPaylas.setOnClickListener(v -> {
            if (mevcutEnlem != 0.0 && mevcutBoylam != 0.0) {
                // Google Maps Linki oluşturma
                String mapsLink = "https://www.google.com/maps?q=" + mevcutEnlem + "," + mevcutBoylam;

                String paylasilacakMetin = "AFBİLKO - Şu anki konumum:\n\n" +
                        "📍 Adres: " + mevcutAdres + "\n\n" +
                        "🗺️ Haritada Gör: " + mapsLink;

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, paylasilacakMetin);
                startActivity(Intent.createChooser(shareIntent, "Konumu Paylaş"));
            } else {
                Toast.makeText(this, "Konum bilgisi henüz hazır değil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        konumIzniniKontrolEtVeKonumuAl();
    }

    private void konumIzniniKontrolEtVeKonumuAl() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            konumuAl();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void konumuAl() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mevcutEnlem = location.getLatitude();
                mevcutBoylam = location.getLongitude();

                LatLng mevcutKonum = new LatLng(mevcutEnlem, mevcutBoylam);
                mMap.addMarker(new MarkerOptions().position(mevcutKonum).title("Buradasınız"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mevcutKonum, 16));

                adresiAl(mevcutKonum);
            } else {
                tvAdres.setText("Konum bulunamadı. GPS açık mı?");
            }
        });
    }

    private void adresiAl(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> adresler = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (adresler != null && !adresler.isEmpty()) {
                mevcutAdres = adresler.get(0).getAddressLine(0);
                tvAdres.setText(mevcutAdres);
            } else {
                mevcutAdres = "Adres bulunamadı.";
                tvAdres.setText(mevcutAdres);
            }
        } catch (IOException e) {
            mevcutAdres = "Adres hatası oluştu.";
            tvAdres.setText(mevcutAdres);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            konumuAl();
        }
    }
}