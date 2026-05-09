""""""""""""""""""""""""""""""""""""package com.example.afbilko;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Anasayfa_toplanma extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline mevcutRota;
    private FloatingActionButton btnNavigasyonToplanma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa_toplanma);

        btnNavigasyonToplanma = findViewById(R.id.btnNavigasyonToplanma);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapToplanma);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (btnNavigasyonToplanma != null) {
            btnNavigasyonToplanma.setOnClickListener(v -> navigasyonuBaslat());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        mMap.setOnMarkerClickListener(marker -> {
            Location myLoc = mMap.getMyLocation();
            if (myLoc != null) {
                LatLng origin = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
                new DownloadTask().execute(getDirectionsUrl(origin, marker.getPosition()));
            } else {
                Toast.makeText(this, "Konumunuz henüz belirlenemedi.", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        ekleToplanmaAlanlari();
    }

    private void ekleToplanmaAlanlari() {
        BitmapDescriptor customIcon = getToplanmaIcon();
        List<ToplanmaData> alanlar = new ArrayList<>();

        // BARTIN BÖLGESİ
        alanlar.add(new ToplanmaData("Cumhuriyet Meydanı", 41.6358, 32.3375));
        alanlar.add(new ToplanmaData("Ömer Tepesi Spor Alanı", 41.6285, 32.3210));
        alanlar.add(new ToplanmaData("Bartın Belediye Önü", 41.6322, 32.3335));
        alanlar.add(new ToplanmaData("Yalı Boyu Sosyal Alan", 41.6395, 32.3315));

        // ANKARA BÖLGESİ (Test için)
        alanlar.add(new ToplanmaData("Ankara Gençlik Parkı", 39.9382, 32.8548));
        alanlar.add(new ToplanmaData("Kızılay Meydanı Güvenli Alan", 39.9208, 32.8541));

        // İSTANBUL BÖLGESİ (Test için)
        alanlar.add(new ToplanmaData("Beşiktaş Meydanı", 41.0422, 29.0075));
        alanlar.add(new ToplanmaData("Kadıköy Yoğurtçu Parkı", 40.9855, 29.0305));

        for (ToplanmaData data : alanlar) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(data.lat, data.lng))
                    .title(data.isim)
                    .snippet("Güvenli Toplanma Alanı")
                    .icon(customIcon));
        }

        // Başlangıç odağını Bartın olarak ayarla
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.63, 32.33), 13f));
    }

    // Basit bir veri taşıma sınıfı
    private static class ToplanmaData {
        String isim; double lat, lng;
        ToplanmaData(String isim, double lat, double lng) {
            this.isim = isim; this.lat = lat; this.lng = lng;
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "key=YOUR_API_KEY";
        return "https://maps.googleapis.com/maps/api/directions/json?" + str_origin + "&" + str_dest + "&mode=driving&" + key;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                URL myUrl = new URL(url[0]);
                HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                data = sb.toString();
                br.close();
            } catch (Exception e) { Log.e("API_ERROR", e.toString()); }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(String... jsonData) {
            try {
                JSONObject jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                return parser.parse(jObject);
            } catch (Exception e) {
                Log.e("PARSER_ERROR", e.toString());
                return null;
            }
        }
        @Override
        protected void onPostExecute(List<LatLng> points) {
            if (mevcutRota != null) mevcutRota.remove();
            if (points != null && !points.isEmpty()) {
                PolylineOptions lineOptions = new PolylineOptions()
                        .addAll(points)
                        .width(12)
                        .color(Color.BLUE)
                        .geodesic(true);
                mevcutRota = mMap.addPolyline(lineOptions);

                if (btnNavigasyonToplanma != null) {
                    btnNavigasyonToplanma.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private BitmapDescriptor getToplanmaIcon() {
        try {
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.toplanma);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 120, 120, false);
            return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        } catch (Exception e) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        }
    }
}
