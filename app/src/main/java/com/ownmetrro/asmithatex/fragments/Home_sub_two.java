package com.ownmetrro.asmithatex.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ownmetrro.asmithatex.R;
import com.ownmetrro.asmithatex.adapters.ProductAdapter;
import com.ownmetrro.asmithatex.app.App;
import com.ownmetrro.asmithatex.constant.ConstantValues;
import com.ownmetrro.asmithatex.models.product_model.GetAllProducts;
import com.ownmetrro.asmithatex.models.product_model.ProductData;
import com.ownmetrro.asmithatex.models.product_model.ProductDetails;
import com.ownmetrro.asmithatex.network.APIClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class Home_sub_two  extends Fragment {
    String customerID;
    Boolean isHeaderVisible;
    Call<ProductData> networkCall;

    TextView emptyRecord, headerText;
    RecyclerView most_liked_recycler;

    ProductAdapter productAdapter;

    List<ProductDetails> mostLikedProductList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.f_products_horizontal, container, false);

        // Get the Boolean from Bundle Arguments
        isHeaderVisible = getArguments().getBoolean("isHeaderVisible");

        // Get the CustomerID from SharedPreferences
        customerID = this.getContext().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE).getString("userID", "");


        // Binding Layout Views
        emptyRecord = (TextView) rootView.findViewById(R.id.empty_record_text);
        headerText = (TextView) rootView.findViewById(R.id.products_horizontal_header);
        most_liked_recycler = (RecyclerView) rootView.findViewById(R.id.products_horizontal_recycler);

        // Hide some of the Views
        emptyRecord.setVisibility(View.GONE);

        // Check if Header must be Invisible or not
        if (!isHeaderVisible) {
            headerText.setVisibility(View.GONE);
        } else {
            headerText.setText("Mens Polo Shirt");
        }

        mostLikedProductList = new ArrayList<>();


        // RecyclerView has fixed Size
        most_liked_recycler.setHasFixedSize(true);
        most_liked_recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize the ProductAdapter for RecyclerView
        productAdapter = new ProductAdapter(getContext(), mostLikedProductList, true);

        // Set the Adapter and LayoutManager to the RecyclerView
        most_liked_recycler.setAdapter(productAdapter);

        // Request for Most Sold Products
        RequestsubonecatProducts();

        return rootView;
    }


    //*********** Adds Products returned from the Server to the MostLikedProductList ********//

    private void addProducts(ProductData productData) {

        // Add Products to mostLikedProductList
        mostLikedProductList.addAll(productData.getProductData());

        productAdapter.notifyDataSetChanged();
    }

    private void RequestsubonecatProducts() {

        GetAllProducts getAllProducts = new GetAllProducts();
        getAllProducts.setPageNumber(0);
        getAllProducts.setLanguageId(ConstantValues.LANGUAGE_ID);
        getAllProducts.setCustomersId(customerID);
        getAllProducts.setType("cat2");


        networkCall = APIClient.getInstance()
                .getAllProducts
                        (
                                getAllProducts
                        );

        networkCall.enqueue(new Callback<ProductData>() {
            @Override
            public void onResponse(Call<ProductData> call, retrofit2.Response<ProductData> response) {

                if (response.isSuccessful()) {

                    if (response.body().getSuccess().equalsIgnoreCase("1")) {
                        // Products have been returned. Add Products to the mostLikedProductList
                        addProducts(response.body());
                        emptyRecord.setVisibility(View.GONE);

                    }
                    else if (response.body().getSuccess().equalsIgnoreCase("0")) {
                        // Products haven't been returned
                        emptyRecord.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onFailure(Call<ProductData> call, Throwable t) {
                if (!call.isCanceled()) {
                    Toast.makeText(App.getContext(), "NetworkCallFailure : "+t, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onPause() {

        // Check if NetworkCall is being executed
        if (networkCall.isExecuted()){
            // Cancel the NetworkCall
            networkCall.cancel();
        }

        super.onPause();
    }

}
