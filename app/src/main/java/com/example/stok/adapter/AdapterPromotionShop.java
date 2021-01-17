package com.example.stok.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stok.R;
import com.example.stok.activities.AddPromotionCodeActivity;
import com.example.stok.models.ModelPromotion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterPromotionShop extends RecyclerView.Adapter<AdapterPromotionShop.HolderPromotionShop> {

    private Context context;
    private ArrayList<ModelPromotion> promotionArrayList;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public AdapterPromotionShop(Context context, ArrayList<ModelPromotion> promotionArrayList) {
        this.context = context;
        this.promotionArrayList = promotionArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Mohon Tunggu...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       //inflate layout row_promotion_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_promotion_shop, parent, false);

        return new HolderPromotionShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPromotionShop holder, int position) {

        //get data
        final ModelPromotion modelPromotion = promotionArrayList.get(position);
        String id = modelPromotion.getId();
        String timestamp = modelPromotion.getTimestamp();
        String description = modelPromotion.getDescription();
        String promoCode = modelPromotion.getPromoCode();
        String promoPrice = modelPromotion.getPromoPrice();
        String expireDate = modelPromotion.getExpireDate();
        String minimumOrderPrice = modelPromotion.getMinimumOrderPrice();

        //set data
        holder.descriptionTv.setText(description);
        holder.promoPriceTv.setText(promoPrice);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);
        holder.promoCodeTv.setText("Kode" +promoCode);
        holder.expireDateTv.setText("Gunakan Sebelum :" +expireDate);


        //handle click, show Edit/Delete dialog
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeleteDialog(modelPromotion);
            }
        });

    }

    private void editDeleteDialog(final ModelPromotion modelPromotion) {
        //options to display in dialog
        String[] options = {"Ganti", "Hapus"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pilih Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if (which==0){
                           //edit clicked
                           editPromoCode(modelPromotion);
                       }
                       else if (which==1){
                           //delete clicked
                           deletePromoCode(modelPromotion);
                       }
                    }
                })
                .show();
    }

    private void deletePromoCode(ModelPromotion modelPromotion) {
        //progress bar....
        progressDialog.setMessage("Hapus Kode Promosi...");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(modelPromotion.getId())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //deleted
                        progressDialog.dismiss();
                        Toast.makeText(context, "Hapuss....", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failture deleted
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editPromoCode(ModelPromotion modelPromotion) {
        //start and padd data to AddpromotionCodeActivity to edit
        Intent intent = new Intent(context, AddPromotionCodeActivity.class);
        intent.putExtra("promoId", modelPromotion.getId());//will use id to update promo code
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return promotionArrayList.size();
    }

    //vies holder
    class HolderPromotionShop extends RecyclerView.ViewHolder{

        //views of row_promotion_shop.xml
        private ImageView iconIv;
        private TextView promoCodeTv, promoPriceTv, minimumOrderPriceTv, expireDateTv,descriptionTv;

        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);

            iconIv = itemView.findViewById(R.id.iconIv);
            promoCodeTv = itemView.findViewById(R.id.promoCodeTv);
            promoPriceTv = itemView.findViewById(R.id.promoPriceTv);
            minimumOrderPriceTv = itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv = itemView.findViewById(R.id.expireDateTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);


        }
    }
}
