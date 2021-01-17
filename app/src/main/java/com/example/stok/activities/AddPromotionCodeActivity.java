package com.example.stok.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stok.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodeActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText promoCodeEt,promoDescriptionEt,promoPriceEt,minimumOrderPriceEt;
    private TextView expireDateTv, titleTv;
    private Button addBtn;

    FirebaseAuth firebaseAuth;
    //progress dialog
    ProgressDialog progressDialog;

    private String promoId;

    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);

        backBtn = findViewById(R.id.backBtn);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumOrderPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        addBtn = findViewById(R.id.addBtn);
        titleTv = findViewById(R.id.titleTv);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        //inin progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Mohon Tunggu...");
        progressDialog.setCanceledOnTouchOutside(false);

        //get promo id from intent
        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null){
            //came here from adapter to update record
            promoId = intent.getStringExtra("promoId");

            titleTv.setText("Update Kode Promosi");
            addBtn.setText("Update");

            isUpdating = true;

            loadPromoInfo(); //load promo info
        }
        else {
            //came from promo cedes list activity to add
            titleTv.setText("Tambah Kode Promosi");
            addBtn.setText("Tambah");

            isUpdating = false;
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickDialog();
            }
        });

        //add promo to fire base
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                inputData();
            }
        });
    }

    private void loadPromoInfo() {
        //bd path to promo code
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get info of promo code
                        String id = ""+ snapshot.child("id").getValue();
                        String timestamp = ""+ snapshot.child("timestamp").getValue();
                        String description = ""+ snapshot.child("description").getValue();
                        String promoCode = ""+ snapshot.child("promoCode").getValue();
                        String promoPrice = ""+ snapshot.child("promoPrice").getValue();
                        String minimumOrderPrice = ""+ snapshot.child("minimumOrderPrice").getValue();
                        String expireDate = ""+ snapshot.child("expireDate").getValue();

                        //set data
                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumOrderPriceEt.setText(minimumOrderPrice);
                        expireDateTv.setText(expireDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void datePickDialog() {

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMounth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        //date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                DecimalFormat mformat = new DecimalFormat("00");
                String pDay = mformat.format(dayOfMonth);
                String pMounth = mformat.format(monthOfYear);
                String pYear = ""+ year;
                String pDate = pDay + "/" + pMounth+"/"+pYear;

                expireDateTv.setText(pDate);
            }
        },mYear, mMounth, mDay);

        datePickerDialog.show();
        //DISABLE POST Date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;
    private void inputData(){
        //input data
        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();

        //validate
        if (TextUtils.isEmpty(promoCode)){
            Toast.makeText(this, "Masukkan Kode Diskon", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Masukkan Deskripsi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(promoPrice)){
            Toast.makeText(this, "Masukkan Harga Promo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(minimumOrderPrice)){
            Toast.makeText(this, "Masukkan Minimal Harga Belanja", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(expireDate)){
            Toast.makeText(this, "Masukkan tanggal expired", Toast.LENGTH_SHORT).show();
            return;
        }


        if (isUpdating){
             //update database
            updateToDb();
        }
        else {
            //add data
            addDataToDb();
        }

    }

    private void updateToDb() {
        progressDialog.setMessage("Mengubah Kode Promosi");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("description", ""+description);
        hashMap.put("promoCode", ""+promoCode);
        hashMap.put("promoPrice", ""+promoPrice);
        hashMap.put("minimumOrderPrice", ""+minimumOrderPrice);
        hashMap.put("expireDate", "" +expireDate);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      //updated
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Update....", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed update
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDataToDb() {
        //all field entered
        progressDialog.setMessage("Menambah Kode Promosi");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        //setup data to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("description", ""+description);
        hashMap.put("promoCode", ""+promoCode);
        hashMap.put("promoPrice", ""+promoPrice);
        hashMap.put("minimumOrderPrice", ""+minimumOrderPrice);
        hashMap.put("expireDate", "" +expireDate);

        //init db reference user = current user = promotion = promoid = promo data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       //code added
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Kode Promosi Ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //adding code fail
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}