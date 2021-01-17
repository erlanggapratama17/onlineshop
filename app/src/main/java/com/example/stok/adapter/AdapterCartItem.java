package com.example.stok.adapter;

import android.content.Context;
import android.print.PageRange;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stok.R;
import com.example.stok.activities.ShopDetailsActivity;
import com.example.stok.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HoldeCartItem>{

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HoldeCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HoldeCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoldeCartItem holder, final int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        final String id = modelCartItem.getId();
        String getpId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        final String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");
        holder.itemPriceEachTv.setText(""+ price);

        //handle remove click listener
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will create table
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Hapus dari troli...", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                //adjust subtotal
                double subTotalTanpaDiskon = ((ShopDetailsActivity)context).allTotalPrice;
                double subTotalAfterProductRemove = subTotalTanpaDiskon - Double.parseDouble(cost.replace("Rp", ""));
                ((ShopDetailsActivity)context).allTotalPrice = subTotalAfterProductRemove;
                ((ShopDetailsActivity)context).sTotalTv.setText("Rp" + String.format("%.2f", ((ShopDetailsActivity)context).allTotalPrice));

                //once subtotal is update
                double hargaPromo = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                double deliverFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("Rp", ""));

                //cek jika promo tersedia
                if (((ShopDetailsActivity)context).isPromocodeApplied){
                    //pakai
                    if (subTotalAfterProductRemove < Double.parseDouble(((ShopDetailsActivity)context).promoMinimumOrderPrice)){
                        //current order price is less then minimum
                        Toast.makeText(context, "Kode Ini Bisa Digunakan dengan minimal pembayaran: Rp"+((ShopDetailsActivity)context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity)context).discountTv.setText("Rp0");
                        ((ShopDetailsActivity)context).isPromocodeApplied = false;
                        //show new total after delivery feee
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rp" + String.format("0f", Double.parseDouble(String.format("0f" , subTotalAfterProductRemove + deliverFee))));
                    }
                    else {
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText(((ShopDetailsActivity)context).promoDescription);
                        //show new total price after total price
                        ((ShopDetailsActivity)context).isPromocodeApplied = true;
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rp" + String.format("0f", Double.parseDouble(String.format("0f" , subTotalAfterProductRemove + deliverFee - hargaPromo))));

                    }
                }
                else {
                    //tidak pakai
                    ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rp" + String.format("0f", Double.parseDouble(String.format("0f" , subTotalAfterProductRemove + deliverFee))));

                }


                //after removing item cart
                ((ShopDetailsActivity)context).cartCount();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    //view holder
    class HoldeCartItem extends RecyclerView.ViewHolder{

        //ui views row_cartitem.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HoldeCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }
}
