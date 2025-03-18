package com.example.unipiplishopping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Orders_view extends BaseActivity {

    private LinearLayout ordersContainer;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ordersContainer = findViewById(R.id.ordersContainer);
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        LinearLayout main = findViewById(R.id.main);

        // Εφαρμόζουμε τις αποθηκευμένες ρυθμίσεις
        UserPreferences.applySettings(this, new TextView[]{}, main);

        // Φόρτωση παραγγελιών από Firebase
        loadOrders();
    }

    private void loadOrders() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ordersContainer.removeAllViews();

                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        String fullName = orderSnapshot.child("fullName").getValue(String.class);
                        String timestamp = orderSnapshot.child("timestamp").getValue(String.class);
                        List<Product> products = new ArrayList<>();

                        for (DataSnapshot productSnapshot : orderSnapshot.child("products").getChildren()) {
                            String productId = productSnapshot.child("productId").getValue(String.class);
                            String title = productSnapshot.child("title").getValue(String.class);
                            String imageUrl = productSnapshot.child("imageUrl").getValue(String.class);
                            products.add(new Product(productId, title, imageUrl));
                        }

                        // Προβολή της παραγγελίας στο UI
                        displayOrder(fullName, timestamp, products);
                    }
                } else {
                    Toast.makeText(Orders_view.this, "No orders found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Error loading orders", error.toException());
            }
        });
    }

    private void displayOrder(String fullName, String timestamp, List<Product> products) {
        TextView orderHeader = new TextView(this);
        orderHeader.setText("Order by: " + fullName + "\nDate: " + timestamp);
        orderHeader.setTextSize(18);
        orderHeader.setPadding(10, 10, 10, 10);

        ordersContainer.addView(orderHeader);

        for (Product product : products) {
            LinearLayout productLayout = new LinearLayout(this);
            productLayout.setOrientation(LinearLayout.HORIZONTAL);
            productLayout.setPadding(10, 10, 10, 10);

            ImageView imageView = new ImageView(this);
            Picasso.get().load(product.getImageUrl()).into(imageView);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(150, 150);
            imgParams.setMargins(0, 0, 20, 0);
            imageView.setLayoutParams(imgParams);

            TextView textView = new TextView(this);
            textView.setText("Title: " + product.getTitle());
            textView.setTextSize(16);
            textView.setPadding(20, 0, 0, 0);

            productLayout.addView(imageView);
            productLayout.addView(textView);

            ordersContainer.addView(productLayout);
        }
    }
    public void OrderScreen(View view) {
        Intent intent = new Intent(Orders_view.this, Order.class);
        startActivity(intent);
    }

    public void ProductsPage(View view){
        Intent intent=new Intent(Orders_view.this,Products.class);
        startActivity(intent);
    }

    public void OrderBagfunction (View view){
        Intent intent = new Intent(Orders_view.this,Order.class);
        startActivity(intent);
    }

    public void ProfileFunction (View view){
        Intent intent = new Intent(Orders_view.this,Settings.class);
        startActivity(intent);
    }
}