package com.example.stok;

import android.widget.Filter;

import com.example.stok.adapter.AdapterOrderShop;
import com.example.stok.adapter.AdapterProductSeller;
import com.example.stok.models.ModelOrderShop;
import com.example.stok.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data
        if (constraint != null && constraint.length() > 0){
            //cari filed tidak kosong

            //change to upper case
            constraint = constraint.toString().toUpperCase();
            //store filtered list
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check dan cari berdasarkan title
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //add filter data
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //cari field kosong
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>) results.values;
        adapter.notifyDataSetChanged();
    }
}
