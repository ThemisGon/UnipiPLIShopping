package com.example.unipiplishopping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
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

import java.util.ArrayList;
import java.util.List;

public class Products extends BaseActivity {

    Button product, product2, product3, product4, product5, product6, product7, product8, product9, product10;
    private DatabaseReference databaseRef;
    private List<Button> productButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout main = findViewById(R.id.main);
        // Εφαρμόζουμε τις αποθηκευμένες ρυθμίσεις
        UserPreferences.applySettings(this, new TextView[]{}, main);

        // Assign Buttons
        product = findViewById(R.id.product);
        product2 = findViewById(R.id.product2);
        product3 = findViewById(R.id.product3);
        product4 = findViewById(R.id.product4);
        product5 = findViewById(R.id.product5);
        product6 = findViewById(R.id.product6);
        product7 = findViewById(R.id.product7);
        product8 = findViewById(R.id.product8);
        product9 = findViewById(R.id.product9);
        product10 = findViewById(R.id.product10);

        // Add Buttons to List
        productButtons.add(product);
        productButtons.add(product2);
        productButtons.add(product3);
        productButtons.add(product4);
        productButtons.add(product5);
        productButtons.add(product6);
        productButtons.add(product7);
        productButtons.add(product8);
        productButtons.add(product9);
        productButtons.add(product10);

        // Connect to Firebase Database
        databaseRef = FirebaseDatabase.getInstance().getReference("Products");

        // Fetch product names and make buttons clickable
        fetchProductNames();
    }
    private void fetchProductNames() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    if (index < productButtons.size()) {
                        String productName = productSnapshot.child("title").getValue(String.class);
                        String productId = productSnapshot.getKey(); // Παίρνουμε το ID του προϊόντος

                        Button button = productButtons.get(index);
                        button.setText(productName);

                        // Set Click Listener for Each Button
                        button.setOnClickListener(v -> {
                            Intent intent = new Intent(Products.this, SingleProduct.class);
                            intent.putExtra("productId", productId);  // Περνάμε το ID του προϊόντος
                            intent.putExtra("productName", productName); // Περνάμε το όνομα του προϊόντος
                            startActivity(intent);
                        });

                        index++;
                    } else {
                        break; // Stop if there are more products than buttons
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Error loading product names", error.toException());
            }
        });
    }

    public void OrderBagfunction (View view){
        Intent intent = new Intent(Products.this,Order.class);
        startActivity(intent);
    }

    public void ProfileFunction (View view){
        Intent intent = new Intent(Products.this,Settings.class);
        startActivity(intent);
    }

}
