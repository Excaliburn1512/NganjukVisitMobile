package com.polije.sem3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.polije.sem3.databinding.ActivityDetailInformasiBinding;
import com.polije.sem3.databinding.ActivityMapJavaBinding;
import com.polije.sem3.model.UlasanModelAdapter;
import com.polije.sem3.model.WisataModel;
import com.polije.sem3.model.WisataModelAdapter;
import com.polije.sem3.response.DetailWisataResponse;
import com.polije.sem3.response.UlasanKirimResponse;
import com.polije.sem3.response.UlasanResponse;
import com.polije.sem3.response.WisataResponse;
import com.polije.sem3.retrofit.Client;
import com.polije.sem3.util.UsersUtil;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailInformasi extends AppCompatActivity implements MapListener, GpsStatus.Listener {

    private MapView mMap;
    private IMapController controller;
    private MyLocationNewOverlay mMyLocationOverlay;
    private Button btnLink;
    private WisataModel dataListWisata;
    private UlasanModelAdapter adapterUlasan;

    public static String ID_WISATA = "id";

    private String idSelected;
    private TextView emptyTextView;
    private String destination;
    private String getComment;

    private ActivityDetailInformasiBinding binding;

    private MapView mapView;

    private ItemizedIconOverlay<OverlayItem> itemizedIconOverlay;

    private boolean availablelinkmaps;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_informasi);

        idSelected = getIntent().getStringExtra(ID_WISATA);

        itemizedIconOverlay = new ItemizedIconOverlay<>(this, new ArrayList<>(), null);

        availablelinkmaps = true;

        emptyTextView = new TextView(DetailInformasi.this);

        binding = ActivityDetailInformasiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        );

        // kodingan retrofit get data
        Client.getInstance().detailwisata(idSelected).enqueue(new Callback<DetailWisataResponse>() {

            @Override
            public void onResponse(Call<DetailWisataResponse> call, Response<DetailWisataResponse> response) {
                if (response.body() != null && response.body().getStatus().equalsIgnoreCase("success")) {
                    dataListWisata = response.body().getData();
                    String coordinates = dataListWisata.getCoordinate();
                    String linkmaps = dataListWisata.getLinkmaps();

                    if (!coordinates.isEmpty()){
                        // Split the string by commas
                        String[] words = coordinates.split(",");
                        double firstCoordinates = Double.parseDouble(words[0].trim());  // Trim to remove leading and trailing whitespaces
                        double secondCoordinates = Double.parseDouble(words[1].trim());

                        mMap = binding.osmmap;
                        mMap.setTileSource(TileSourceFactory.MAPNIK);
                        mMap.getMapCenter();
                        mMap.setMultiTouchControls(true);
                        mMap.getLocalVisibleRect(new Rect());


                        GeoPoint startPoint = new GeoPoint(firstCoordinates, secondCoordinates);
                        OverlayItem overlayItem = new OverlayItem("Marker Title", "Marker Description", startPoint);
                        Drawable marker = getResources().getDrawable(R.drawable.locationpin); // Ganti dengan gambar marker Anda
                        marker.setBounds(0, 0, 10, 10);
                        overlayItem.setMarker(marker);

                        controller = mMap.getController();

                        controller.setCenter(startPoint);
                        controller.animateTo(startPoint);
                        controller.setZoom(16);

                        Log.e("TAG", "onCreate:in " + controller.zoomIn());
                        Log.e("TAG", "onCreate: out " + controller.zoomOut());

                        itemizedIconOverlay.addItem(overlayItem);
                        mMap.getOverlays().add(itemizedIconOverlay);
                        mMap.addMapListener(DetailInformasi.this);
                    }

                    if (linkmaps.isEmpty()) {
                        Toast.makeText(DetailInformasi.this, "Lokasi maps tidak tersedia", Toast.LENGTH_SHORT).show();
//                        destination = "Air+Terjun+Sedudo";
                        availablelinkmaps = false;
                    } else if (!linkmaps.isEmpty()) {
                        destination = linkmaps;
                    }

                    Glide.with(DetailInformasi.this).load(Client.IMG_DATA + dataListWisata.getGambar()).into(binding.imageView);
                    binding.namaWisata.setText(dataListWisata.getNama());
                    binding.deskripsiWisata.setText(dataListWisata.getDeskripsi());
                    binding.jamOperasional.setText(dataListWisata.getJadwal());
                    binding.hargaTiket.setText(dataListWisata.getHarga_tiket());
                    binding.alamatWisata.setText(dataListWisata.getAlamat());

//                    Toast.makeText(DetailInformasi.this, coordinates, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<DetailWisataResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(DetailInformasi.this, "ERROR -> " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("DetailInformasi", "error " + t.getMessage());
            }
        });

//        RecyclerView recyclerView = findViewById(R.id.recyclerviewUlasan);

        Client.getInstance().ulasan(idSelected).enqueue(new Callback<UlasanResponse>() {
            @Override
            public void onResponse(Call<UlasanResponse> call, Response<UlasanResponse> response) {
                if (response.body() != null && response.body().getStatus().equalsIgnoreCase("success")) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                        adapterUlasan = new UlasanModelAdapter(response.body().getData());
                        binding.recyclerviewUlasan.setAdapter(adapterUlasan);
                    } else {
                        emptyTextView.setText("Belum ada ulasan");
                        emptyTextView.setGravity(Gravity.CENTER);
                        binding.linearLayoutUlasan.setPadding(10, 100, 50, 100);
                        binding.linearLayoutUlasan.addView(emptyTextView);
                    }
                } else {
                    Toast.makeText(DetailInformasi.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UlasanResponse> call, Throwable t) {
                Toast.makeText(DetailInformasi.this, "Request Timeout", Toast.LENGTH_SHORT).show();
            }
        });

        mapView = findViewById(R.id.osmmap);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollviewLayout);

        // smooth scroll map
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Ketika pengguna menekan peta, nonaktifkan pengguliran ScrollView
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Ketika pengguna melepaskan peta, izinkan pengguliran ScrollView kembali
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        btnLink = findViewById(R.id.directToMaps);

        btnLink.setOnClickListener(v -> {

            if (availablelinkmaps){

//                destination = "Air+Terjun+Sedudo"; // Gantilah dengan nama atau alamat tujuan Anda
                String mapUri = "https://www.google.com/maps/search/?api=1&query=" + destination;
//                String mapUri = "https://maps.app.goo.gl/" + destination;

                Uri gmmIntentUri = Uri.parse(mapUri);

                // Buat intent untuk membuka Google Maps
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps"); // Hanya buka dengan aplikasi Google Maps

                // Periksa apakah aplikasi Google Maps terpasang
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    // Buka aplikasi Google Maps
                    startActivity(mapIntent);
                } else {
                    // Jika Google Maps tidak terpasang, Anda dapat menampilkan pesan kesalahan
                    Toast.makeText(getApplicationContext(), "Aplikasi Google Maps tidak tersedia.", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(DetailInformasi.this, "Lokasi maps tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        UsersUtil usersUtil = new UsersUtil(this);
        String idpengguna = usersUtil.getId();
        String fullnama = usersUtil.getFullName();

        binding.btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getComment = String.valueOf(binding.txtAddComment.getText());
                if(!getComment.isEmpty()) {
                    Client.getInstance().kirimulasan(idpengguna, fullnama, getComment, idSelected).enqueue(new Callback<UlasanKirimResponse>() {
                        @Override
                        public void onResponse(Call<UlasanKirimResponse> call, Response<UlasanKirimResponse> response) {
                            if (response.body() != null && response.body().getStatus().equalsIgnoreCase("success")) {
                                Client.getInstance().ulasan(idSelected).enqueue(new Callback<UlasanResponse>() {
                                    @Override
                                    public void onResponse(Call<UlasanResponse> call, Response<UlasanResponse> response) {
                                        if (response.body() != null && response.body().getStatus().equalsIgnoreCase("success")) {
                                            if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                                                adapterUlasan = new UlasanModelAdapter(response.body().getData());
                                                emptyTextView.setVisibility(View.GONE);
                                                binding.linearLayoutUlasan.setPadding(0,0,0,0);
                                                binding.recyclerviewUlasan.setAdapter(adapterUlasan);
                                            } else {
                                                // nothing
                                            }
                                        } else {
                                            Toast.makeText(DetailInformasi.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<UlasanResponse> call, Throwable t) {
                                        Toast.makeText(DetailInformasi.this, "Request Timeout", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(DetailInformasi.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                binding.txtAddComment.setText(null);
                            } else if (response.body() != null && response.body().getStatus().equalsIgnoreCase("fail")) {
                                Toast.makeText(DetailInformasi.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailInformasi.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UlasanKirimResponse> call, Throwable t) {
                            Toast.makeText(DetailInformasi.this, "Request Timeout", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(DetailInformasi.this, "Anda harus mengisi komentar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.backButtonDetail.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        Log.e("TAG", "onCreate:la " + event.getSource().getMapCenter().getLatitude());
        Log.e("TAG", "onCreate:lo " + event.getSource().getMapCenter().getLongitude());
        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        Log.e("TAG", "onZoom zoom level: " + event.getZoomLevel() + "   source:  " + event.getSource());
        return false;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        // Handle GPS status changes
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}