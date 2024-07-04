package com.berkan.marketurunotomasyonu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.berkan.marketurunotomasyonu.databinding.ActivityUrunDetayBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.security.auth.callback.PasswordCallback;

public class UrunDetay extends AppCompatActivity {
    public ActivityUrunDetayBinding binding;
    ActivityResultLauncher<Intent> galeriLauncher;
    ActivityResultLauncher<String> izinLauncher;
    Bitmap secilenGorsel;
    int gelenid;
    String gelisSebebi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urun_detay);
        binding = ActivityUrunDetayBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Intent gelenVeri = getIntent();
        gelisSebebi=gelenVeri.getStringExtra("gelisSebebi");
        gelenid=gelenVeri.getIntExtra("id",-1);
        if (gelisSebebi.matches("urunGoster")){
            try {
                SQLiteDatabase veritabani =this.openOrCreateDatabase("market_db",MODE_PRIVATE,null);
                Cursor imlec = veritabani.rawQuery("SELECT * FROM urunler WHERE id="+gelenid,null);
                int idX=imlec.getColumnIndex("id");
                int urunAdiX=imlec.getColumnIndex("urunAdi");
                int urunFiyatiX=imlec.getColumnIndex("urunFiyati");
                int stokAdetiX=imlec.getColumnIndex("stokAdeti");
                int gorselX=imlec.getColumnIndex("gorsel");
                while (imlec.moveToNext()){
                    binding.urunAdiText.setText(imlec.getString(urunAdiX));
                    binding.fiyatText.setText(imlec.getString(urunFiyatiX));
                    binding.stokText.setText(imlec.getString(stokAdetiX));
                    byte[] gorselDizisi=imlec.getBlob(gorselX);
                    Bitmap olusanGorsel= BitmapFactory.decodeByteArray(gorselDizisi,0,gorselDizisi.length);
                    binding.gorsel.setImageBitmap(olusanGorsel);
                }
                binding.button3.setVisibility(View.INVISIBLE);


            }catch (Exception e){
                Toast.makeText(this,"Bir Hata oluştu", Toast.LENGTH_LONG).show();
            }

        }else {
            UrunEkleme();
            binding.button3.setVisibility(View.VISIBLE);
        }



    }
    public void UrunEkleme(){
        galeriLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode()==RESULT_OK){
                    Intent galeridenGelen= o.getData();
                    if (galeridenGelen!=null){
                        Uri gorselData = galeridenGelen.getData();
                        try {
                            if (Build.VERSION.SDK_INT>=28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),gorselData);
                                secilenGorsel = ImageDecoder.decodeBitmap(source);
                                binding.gorsel.setImageBitmap(secilenGorsel);

                            }else {
                                secilenGorsel = MediaStore.Images.Media.getBitmap(getContentResolver(),gorselData);
                                binding.gorsel.setImageBitmap(secilenGorsel);

                            }
                        }
                        catch (Exception e){
                            Toast.makeText(UrunDetay.this, "Görsel Seçiminde Hata Oluştu", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        izinLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o ==true){
                    //İZİN VERİLDİ
                    Intent galeriyeGit = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galeriLauncher.launch(galeriyeGit);
                }else {
                    //İZİN VERİLMEDİ
                    Toast.makeText(UrunDetay.this,"Galeri izini Vermelisiniz",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
  public void kaydet(View view){
        String urunAdi=binding.urunAdiText.getText().toString();
        int fiyat = Integer.parseInt(binding.fiyatText.getText().toString());
        int stok = Integer.parseInt(binding.stokText.getText().toString());

        Bitmap kucukResim = resimKucult(secilenGorsel,300);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      kucukResim.compress(Bitmap.CompressFormat.PNG,50,outputStream);
      byte[] resimDizisi = outputStream.toByteArray();

      try {
          SQLiteDatabase veritabani = this.openOrCreateDatabase("market_db",MODE_PRIVATE,null);
          String sql = "INSERT INTO urunler(urunAdi,UrunFiyati,StokAdeti,gorsel) VALUES (?,?,?,?)";
          SQLiteStatement sqlDurum =veritabani.compileStatement(sql);
          sqlDurum.bindString(1,urunAdi);
          sqlDurum.bindLong(2,fiyat);
          sqlDurum.bindLong(3,stok);
          sqlDurum.bindBlob(4,resimDizisi);
          sqlDurum.execute();
          Toast.makeText(UrunDetay.this,"Ürün Başarıyla Kaydedildi",Toast.LENGTH_SHORT).show();

          Intent urunlereGit= new Intent(UrunDetay.this, Urunler.class);
          urunlereGit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(urunlereGit);

      }catch (Exception e){
          Toast.makeText(UrunDetay.this,"Bir Hata Oluştu",Toast.LENGTH_SHORT).show();
      }

  }
  public void resimSec(View view){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                Snackbar.make(view,"Galeri izni vermeniz gerekmektedir",Snackbar.LENGTH_INDEFINITE).setAction("izin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        izinLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }
                }).show();

            }
            else {
                Intent galeriyeGit = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galeriLauncher.launch(galeriyeGit);

            }

        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                Snackbar.make(view,"Galeri izni vermeniz gerekmektedir",Snackbar.LENGTH_INDEFINITE).setAction("izin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        izinLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }
            else {
                Intent galeriyeGit = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galeriLauncher.launch(galeriyeGit);
            }

        }

  }

    public Bitmap resimKucult(Bitmap gorsel, int maximumBoyut) {
        int genislik = gorsel.getWidth();
        int yukseklik = gorsel.getHeight();
        float oran = (float) genislik / (float) yukseklik;

        if (genislik > yukseklik) {
            // Yatay resim
            genislik = maximumBoyut;
            yukseklik = (int) (genislik / oran);
        } else {
            // Dikey resim
            yukseklik = maximumBoyut;
            genislik = (int) (yukseklik * oran);
        }

        return Bitmap.createScaledBitmap(gorsel, genislik, yukseklik, true);
    }


}