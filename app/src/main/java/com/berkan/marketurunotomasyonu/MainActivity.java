package com.berkan.marketurunotomasyonu;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.berkan.marketurunotomasyonu.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        try {
            SQLiteDatabase veritabani = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            veritabani.execSQL("CREATE TABLE IF NOT EXISTS urunler(id INTEGER PRIMARY KEY AUTOINCREMENT, urunAdi VARCHAR(50), urunFiyati INTEGER, stokAdeti INTEGER, gorsel BLOB)");
            veritabani.execSQL("CREATE TABLE IF NOT EXISTS kullanicilar(id INTEGER PRIMARY KEY AUTOINCREMENT, kullaniciAdi VARCHAR(50), sifre VARCHAR(50))");
        } catch (Exception e) {
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show();
        }

        adminKaydet();
    }

    public void adminKaydet() {
        try {
            SQLiteDatabase veritabani = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            Cursor imlec = veritabani.rawQuery("SELECT * FROM kullanicilar WHERE kullaniciAdi IN ('admin', 'admin2')", null);

            if (imlec.getCount() == 0) {
                veritabani.execSQL("INSERT INTO kullanicilar (kullaniciAdi, sifre) VALUES ('admin', '123456')");
                veritabani.execSQL("INSERT INTO kullanicilar (kullaniciAdi, sifre) VALUES ('admin2', '123456')");
            }
            imlec.close();
        } catch (Exception e) {
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    public void girisYap(View view) {
        String kullaniciAdi = binding.kullaniciAdiText.getText().toString();
        String sifre = binding.sifreText.getText().toString();
        if (kullaniciAdi.isEmpty() || sifre.isEmpty()) {
            Toast.makeText(this, "Kullanici Adi yada Şifre Boş Geçilmez", Toast.LENGTH_SHORT).show();
        } else {
            try {
                SQLiteDatabase veritabani = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
                Cursor imlec = veritabani.rawQuery("SELECT * FROM kullanicilar WHERE kullaniciAdi = ? AND sifre = ?", new String[]{kullaniciAdi, sifre});
                if (imlec.moveToFirst()) {
                    Intent giris = new Intent(MainActivity.this, Urunler.class);
                    startActivity(giris);
                } else {
                    Toast.makeText(this, "Kullanıcı Adı Bulunamadı", Toast.LENGTH_SHORT).show();
                }
                imlec.close();
            } catch (Exception e) {
                Toast.makeText(this, "Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
