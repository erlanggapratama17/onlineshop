  package com.example.stok.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.stok.Constans;
import com.example.stok.R;
import com.example.stok.adapter.AdapterCartItem;
import com.example.stok.adapter.AdapterProductUser;
import com.example.stok.adapter.AdapterReview;
import com.example.stok.models.ModelCartItem;
import com.example.stok.models.ModelProduct;
import com.example.stok.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {


    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductsTv, cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn,reviewBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;


    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        reviewBtn = findViewById(R.id.reviewBtn);
        ratingBar = findViewById(R.id.ratingBar);

        //progres dilog init
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Mohon Tunggu");
        progressDialog.setCanceledOnTouchOutside(false);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        deleteCartData();
        cartCount();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart
                showCartDialog();

            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Pilih Kategori:")
                        .setItems(Constans.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = Constans.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("Semua")) {
                                    //load all
                                    loadShopProducts();
                                } else {
                                    //load filter
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop id
                Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid" , shopUid);
                startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list
                        ratingSum = 0;
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); //
                            ratingSum = ratingSum + rating;
                        }

                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {


        easyDB.deleteAllDataFromTable();
    }

    public void cartCount(){
        //keep it public
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //no item cart
            cartCountTv.setVisibility(View.GONE);
        }
        else {
            //have item in cart
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);//contrak with string
        }
    }

    public double allTotalPrice = 0.00;
    public TextView sTotalTv, dFeeTv, allTotalPriceTv,promoDescriptionTv,discountTv;
    public EditText promoCodeEt;
    public Button applyBtn;
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();
        //inflate
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);
        applyBtn = view.findViewById(R.id.applyBtn);
        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);


        if (isPromocodeApplied){
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Pakai");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);
        }
        else {
            //not applied
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
            applyBtn.setText("Gunakan");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all recored
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );

            cartItemList.add(modelCartItem);

        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set recyler view
        cartItemRv.setAdapter(adapterCartItem);

        if (isPromocodeApplied){
            hargaDenganDiskon();
        }
        else {
            priceWithOutDiscount();
        }

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myLatitude.equals("") || myLatitude.equals("null")|| myLongitude.equals("") || myLongitude.equals("null")){
                    //user dint enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Tolong Masukkan alamat di profile sebelum memesan.. ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (myPhone.equals("") || myPhone.equals("null")){
                    //user dint enter phone in profile
                    Toast.makeText(ShopDetailsActivity.this, "Tolong Masukkan alamat di profile sebelum memesan.. ", Toast.LENGTH_SHORT).show();
                    return;
            }
                if (cartItemList.size() == 0){
                    //cart list kosong
                    Toast.makeText(ShopDetailsActivity.this, "Tidak ada item di keranjang", Toast.LENGTH_SHORT).show();
                    return;
                }

                submitOrder();
            }

        });

        //validasi promo
        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String promotionCode = promoCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(promotionCode)){
                    Toast.makeText(ShopDetailsActivity.this, "Tolong Masukkan Kode ....", Toast.LENGTH_SHORT).show();
                }
                else {
                    cekKodeAvailability(promotionCode);
                }
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromocodeApplied = true;
                applyBtn.setText("Pakai");

                hargaDenganDiskon();
            }
        });

    }

    public boolean isPromocodeApplied = false;
    public String promoId, promoTimestamp, promoCode, promoDescription, promoExpDate, promoMinimumOrderPrice, promoPrice;
    private void cekKodeAvailability(String promotionCode){
        //proggres bar
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Mohon Tunggu");
        progressDialog.setMessage("Periksa Kode Promosi....");
        progressDialog.setCanceledOnTouchOutside(false);

        //promo tidak dipakai
        isPromocodeApplied = false;
        applyBtn.setText("Pakai");
        priceWithOutDiscount();

        //cek kode promo tersiedia
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //cek jika promo kode ada
                        if (snapshot.exists()){
                            //kode promo ada
                            progressDialog.dismiss();
                            for (DataSnapshot ds: snapshot.getChildren()){
                                promoId = ""+ds.child("id").getValue();
                                promoTimestamp = ""+ds.child("timestamp").getValue();
                                promoCode = ""+ds.child("promoCode").getValue();
                                promoTimestamp = ""+ds.child("timestamp").getValue();
                                promoDescription = ""+ds.child("description").getValue();
                                promoExpDate = ""+ds.child("expireDate").getValue();
                                promoMinimumOrderPrice = ""+ds.child("minimumOrderPrice").getValue();
                                promoPrice = ""+ds.child("promoPrice").getValue();

                                //cek kode expire atau tidak
                                cekKodeExpired();
                            }
                        }
                        else {
                            //masukan kode tidak ada
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this, "Kode Tidak Ada....", Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void cekKodeExpired() {
        //megambil tanggal benar
        Calendar calendar = Calendar.getInstance();
        int tahun = calendar.get(Calendar.YEAR);
        int bulan = calendar.get(Calendar.MONTH) + 1;
        int hari = calendar.get(Calendar.DAY_OF_MONTH);

        //concatenate date
        String todayDate = hari +"/"+ bulan +"/"+ tahun; //e.g

        //cek untuk kadaluarsa
        try {
            SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = sdFormat.parse(todayDate);
            Date expireDate = sdFormat.parse(promoExpDate);
            //compare date
            if (expireDate.compareTo(currentDate) > 0) {
                //tanggal 1 sesudah 2
                cekMinimalHargaPesanan();
            } else if (expireDate.compareTo(currentDate) < 0) {
                //tanggal 1 sebelum 2
                Toast.makeText(this, "Kode Promosi kamu expired"+promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            }
            else if (expireDate.compareTo(currentDate) == 0){
                //both dates are equals
                cekMinimalHargaPesanan();
            }
        }
        catch(Exception e){
                //jiika sesuatu terjadi kesalahan dalam kompare tanggal
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
            }
        }

    private void cekMinimalHargaPesanan() {
        if (Double.parseDouble(String.format("0f", allTotalPrice)) < Double.parseDouble(promoMinimumOrderPrice)){
            //current order price is less than minimum order price
            Toast.makeText(this, "Kode Ini Bisa Digunakan untuk memesan dengan minimum pembayaran: Rp"+ promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
        else {
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    //format tgl indonesia dd MMMM yyyy

    private void hargaDenganDiskon(){
        discountTv.setText("Rp"+promoPrice);
        dFeeTv.setText("Rp"+deliveryFee);
        sTotalTv.setText("Rp"+ String.format("0f", allTotalPrice));
        allTotalPriceTv.setText("Rp" + (allTotalPrice+ Double.parseDouble(deliveryFee.replace("Rp", "")) - Double.parseDouble(promoPrice)));
    }


    private void priceWithOutDiscount() {
        discountTv.setText("Rp");
        dFeeTv.setText("Rp"+deliveryFee);
        sTotalTv.setText("Rp"+ String.format("0f", allTotalPrice));
        allTotalPriceTv.setText("Rp" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("Rp", ""))));
    }

    private void submitOrder() {
        //show progres dialog
        progressDialog.setMessage("Membuat Order....");
        progressDialog.show();

        //untuk order dan waktu
        final String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("Rp", "");//remove rp


        //setup data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "InProgress");//in progress
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("deliveryFee", ""+deliveryFee);
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);

        if (isPromocodeApplied){
            //pakai
            hashMap.put("discount", ""+ promoPrice); //include promo
        }
        else {
            //promo tidak dipakai
            hashMap.put("discount", "0"); //include promo
        }

        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //order info
                        for ( int i=0; i<cartItemList.size(); i++){
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String name = cartItemList.get(i).getName();
                            String cost = cartItemList.get(i).getCost();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Pesanan selesai dibuat..", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr+=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," +shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = ""+dataSnapshot.child("name").getValue();
                shopName = ""+dataSnapshot.child("shopName").getValue();
                shopEmail = ""+dataSnapshot.child("email").getValue();
                shopPhone = ""+dataSnapshot.child("phone").getValue();
                shopLatitude = ""+dataSnapshot.child("latitude").getValue();
                shopAddress = ""+dataSnapshot.child("address").getValue();
                shopLongitude = ""+dataSnapshot.child("longitude").getValue();
                deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();

                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Onkos Kirim :"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (openCloseTv.equals("true")){
                    openCloseTv.setText("OPen");
                }
                else {
                    openCloseTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void loadShopProducts() {

        productsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        productsList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);

                        productsRv.setAdapter(adapterProductUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void  prepareNotificationMessage(String orderId){
        //dimana pengguna pesan ,kirim notifikasi
        String NOTIFICATION_TOPIC = "/topics/" +Constans.FCM_TOPIC; //hasrus sama dengan subskripbe user
        String NOTIFICATION_TITLE = "Order Baru" + orderId;
        String NOTIFICATION_MESSAGE = "Selamat...! Kamu dapat pesanan baru.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); //since we are logged in as buyer
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);//TO ALL WHO SUBCRIBE TO THIS TOPIC
            notificationJo.put("data", notificationBodyJo);//

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        //kirim request volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                 //after sending fcm start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start order
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put recuid header
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constans.FCM_KEY);

                return headers;
            }
        };
        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}