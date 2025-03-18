package com.example.unipiplishopping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.squareup.picasso.Picasso; // Προσθήκη της βιβλιοθήκης Picasso


import com.google.gson.Gson;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com.example.unipiplishopping.Product;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SingleProduct extends BaseActivity {

    //Οι μεταβλητές που θα περάσουμε με Sharred Prederences στην Order
    private String id;
    private String title;
    private String imageUrl;

    TextView productId1,productTitle,productDescription,productPrice,productRelease;
    ImageView productImage;

    Button Basketbutton,Orderbutton;
    private DatabaseReference databaseRef;

    private String productName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Παίρνουμε το `Intent`
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("productId")) {
            Log.e("DEBUG", "No Intent or productId found!");
            Toast.makeText(this, "No product data received!", Toast.LENGTH_SHORT).show();
            finish();  // Κλείνουμε την οθόνη αν δεν υπάρχει προϊόν
            return;
        }

        // Διαβάζουμε το `productId`
        String productId = intent.getStringExtra("productId");
        Log.d("DEBUG", "Loading product details for productId: " + productId);

        // Προσπαθούμε να διαβάσουμε το `title`
        String productTitleFromIntent = intent.getStringExtra("title");

        if (productTitleFromIntent == null) {
            Log.w("DEBUG", "No product title in Intent. Fetching from Firebase...");
            loadProductDetails(productId);
        } else {
            Log.d("DEBUG", "Product title received from Intent: " + productTitleFromIntent);
            // Ενημερώνουμε την οθόνη κατευθείαν με το `title` από την ειδοποίηση
            //TextView productTitleView = findViewById(R.id.productTitle);
            //productTitleView.setText(productTitleFromIntent);
        }

        productId1 = findViewById(R.id.productId);
        productTitle = findViewById(R.id.productTitle);
        productDescription = findViewById(R.id.productDescription);
        productPrice = findViewById(R.id.productPrice);
        productRelease = findViewById(R.id.productRelease);
        productImage = findViewById(R.id.productImage);

        LinearLayout main = findViewById(R.id.main);
        // Εφαρμόζουμε τις αποθηκευμένες ρυθμίσεις
        UserPreferences.applySettings(this, new TextView[]{productId1,productTitle,productDescription,productPrice,productRelease}, main);

        Basketbutton = findViewById(R.id.Basketbutton);
        Orderbutton = findViewById(R.id.Orderbutton);


        Log.d("DEBUG", "Fetching product from Firebase path: Products/" + productId);
        databaseRef = FirebaseDatabase.getInstance().getReference("Products");

        // Ανάκτηση δεδομένων προϊόντος
        loadProductDetails(productId);

    }
    private void loadProductDetails(String productId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Products").child(productId);

        Log.d("DEBUG", "Fetching product from Firebase path: Products/" + productId);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("DEBUG", "Product not found in Firebase! Product ID: " + productId);
                    Toast.makeText(SingleProduct.this, "Product not found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Αποθηκεύουμε τις τιμές στις global μεταβλητές
                id = snapshot.getKey();
                title = snapshot.child("title").getValue(String.class);
                String description = snapshot.child("description").getValue(String.class);
                Double price = snapshot.child("price").getValue(Double.class);
                String releaseDate = snapshot.child("releaseDate").getValue(String.class);
                imageUrl = snapshot.child("imageUrl").getValue(String.class);

                // Έλεγχος αν υπάρχουν null τιμές
                if (title == null) title = "Unknown Title";
                if (description == null) description = "No description available";
                if (price == null) price = 0.0;
                if (releaseDate == null) releaseDate = "Unknown Date";
                if (imageUrl == null) imageUrl = "";

                Log.d("DEBUG", "Product loaded: " + title + ", price: " + price);

                // Ενημερώνουμε το UI
                productId1.setText("ID: " + id);
                productTitle.setText("Title: " + title);
                productDescription.setText("Description: " + description);
                productPrice.setText("Price: $" + price);
                productRelease.setText("Release: " + releaseDate);

                //Φόρτωση εικόνας με Picasso
                if (!imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(productImage);
                } else {
                    productImage.setImageResource(R.drawable.baseline_email_24);
                }

                //Ενεργοποιούμε το κουμπί μόνο αν όλα τα δεδομένα είναι σωστά
                if (id != null && title != null && imageUrl != null) {
                    Basketbutton.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Error loading product details", error.toException());
            }
        });
    }




    public void addToCart(View view) {
        if (id == null || title == null || imageUrl == null) {
            Log.e("DEBUG", "Product data not loaded yet! id: " + id + ", title: " + title);
            Toast.makeText(this, "Product data not loaded yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("cart", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();

        // Ανάκτηση αποθηκευμένης λίστας προϊόντων
        String json = prefs.getString("cartList", null);
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        List<Product> cartList = json == null ? new ArrayList<>() : gson.fromJson(json, type);

        // Προσθήκη του νέου προϊόντος στη λίστα
        cartList.add(new Product(id, title, imageUrl));

        // Αποθήκευση της ενημερωμένης λίστας στο SharedPreferences
        editor.putString("cartList", gson.toJson(cartList));
        editor.apply();

        Toast.makeText(this, "Added to Basket!", Toast.LENGTH_SHORT).show();

        // Επιστροφή στη λίστα προϊόντων
        Intent intent = new Intent(SingleProduct.this, Products.class);
        startActivity(intent);
    }


    public void ProductsPage(View view){
        Intent intent=new Intent(SingleProduct.this,Products.class);
        startActivity(intent);
    }

    public void OrderPage(View view){
        Intent intent=new Intent(SingleProduct.this,Order.class);
        startActivity(intent);
    }

    public void OrderBagfunction (View view){
        Intent intent = new Intent(SingleProduct.this,Order.class);
        startActivity(intent);
    }

    public void ProfileFunction (View view){
        Intent intent = new Intent(SingleProduct.this,Settings.class);
        startActivity(intent);
    }

}