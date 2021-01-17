package com.example.stok.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.stok.R;
import com.example.stok.adapter.AdapterPromotionShop;
import com.example.stok.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesActivity extends AppCompatActivity {

    private ImageButton backBtn, addPromoBtn, filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        backBtn = findViewById(R.id.backBtn);
        addPromoBtn = findViewById(R.id.addPromoBtn);
        filteredTv = findViewById(R.id.filteredTv);
        filterBtn = findViewById(R.id.filterBtn);
        promoRv = findViewById(R.id.promoRv);

        //init firebase auth to current user
        firebaseAuth = FirebaseAuth.getInstance();
        loadAllPromoCodes();

        //handle click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addPromoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(PromotionCodesActivity.this,AddPromotionCodeActivity.class));
            }
        });


        //handle filter
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });


    }

    private void filterDialog() {
        //options to display
        String[] options = {"All", "Expired", "Belum Expired"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Kode Promosi")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which==0){
                            //all clicked
                            filteredTv.setText("Semua Kode Promosi");
                            loadAllPromoCodes();
                        }
                        else if (which==1){
                            //expired
                            filteredTv.setText("Kode Promosi Expired");
                            loadExpiredPromoCodes();
                        }
                        else if (which==2){
                            //belum expired
                            filteredTv.setText("Kode Promosi Belum Expired");
                            loadNoExpiredPromoCodes();
                        }
                    }
                })
                .show();
    }

    private void loadAllPromoCodes(){
        //init list
        promotionArrayList = new ArrayList<>();

        //db reference users - current user - promotions - codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            //add to  list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //set adapter to recyleview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadExpiredPromoCodes() {
        //get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int mounth = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day +"/"+ mounth +"/"+ year;

        //init list
        promotionArrayList = new ArrayList<>();

        //db reference users - current user - promotions - codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            //check for expired
                            try {
                                SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                                Date currentDate = sdFormat.parse(todayDate);
                                Date expireDate = sdFormat.parse(expDate);
                                if (expireDate.compareTo(currentDate) > 0){
                                    //date 1 after date 2
                                }
                                else if (expireDate.compareTo(currentDate) < 0){
                                    //both date equals
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) == 0) {
                                    //both date equals
                                }
                            }
                            catch (Exception e){

                            }

                        }
                        //setup adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //set adapter to recyleview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNoExpiredPromoCodes() {
//get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int mounth = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day +"/"+ mounth +"/"+ year;

        //init list
        promotionArrayList = new ArrayList<>();

        //db reference users - current user - promotions - codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            //check for expired
                            try {
                                SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                                Date currentDate = sdFormat.parse(todayDate);
                                Date expireDate = sdFormat.parse(expDate);
                                if (expireDate.compareTo(currentDate) > 0){
                                    //date 1 after date 2
                                    //both date equals
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) < 0){

                                }
                                else if (expireDate.compareTo(currentDate) == 0) {
                                    //both date equals
                                    promotionArrayList.add(modelPromotion);
                                }
                            }
                            catch (Exception e){

                            }

                        }
                        //setup adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //set adapter to recyleview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}