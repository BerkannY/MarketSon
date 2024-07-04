package com.berkan.marketurunotomasyonu;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.berkan.marketurunotomasyonu.databinding.ActivityUrunlerBinding;

import java.util.ArrayList;

public class Urunler extends AppCompatActivity {
    public ActivityUrunlerBinding binding;

    ArrayList<Integer> idler = new ArrayList<>();
    ArrayList<String> urunAdlari = new ArrayList<>();
    ArrayList<String> yazilacakIfadeler = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUrunlerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        try {
            SQLiteDatabase veritabani = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            Cursor imlec = veritabani.rawQuery("SELECT * FROM urunler", null);
            int idX = imlec.getColumnIndex("id");
            int urunAdiX = imlec.getColumnIndex("urunAdi");
            while (imlec.moveToNext()) {
                idler.add(imlec.getInt(idX));
                urunAdlari.add(imlec.getString(urunAdiX));
                yazilacakIfadeler.add(imlec.getInt(idX) + "-" + imlec.getString(urunAdiX));
            }
            ArrayAdapter<String> adaptor = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, yazilacakIfadeler);
            binding.urunler.setAdapter(adaptor);
            imlec.close();

            binding.urunler.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String secilen = yazilacakIfadeler.get(position);
                    String[] parcala = secilen.split("-");
                    int gosterilecekUrun = Integer.parseInt(parcala[0]);
                    Intent detayaGit = new Intent(Urunler.this, UrunDetay.class);
                    detayaGit.putExtra("id", gosterilecekUrun);
                    detayaGit.putExtra("gelisSebebi", "urunGoster");
                    startActivity(detayaGit);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    public void yeniUrun(View view) {
        Intent urunekle = new Intent(this, UrunDetay.class);
        urunekle.putExtra("gelisSebebi", "urunEkle");
        startActivity(urunekle);
    }
}
