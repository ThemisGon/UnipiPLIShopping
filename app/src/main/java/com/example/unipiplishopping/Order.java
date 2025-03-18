package com.example.unipiplishopping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order extends BaseActivity {

    private LinearLayout ordersContainer;
    private Button sendOrder;
    private List<Product> cartList;
    private DatabaseReference ordersRef;
    private String fullName;  // Όνομα πελάτη από SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ordersContainer = findViewById(R.id.ordersContainer);
        sendOrder = findViewById(R.id.OrderSend);

        LinearLayout main = findViewById(R.id.main);
        // Εφαρμόζουμε τις αποθηκευμένες ρυθμίσεις
        UserPreferences.applySettings(this, new TextView[]{}, main);

        // Firebase reference
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        // Φόρτωση ονόματος πελάτη από SharedPreferences
        loadCustomerInfo();

        // Φόρτωση προϊόντων από SharedPreferences
        loadCartItems();

        // Εμφάνιση προϊόντων
        displayCartItems();

        // Όταν πατάει το κουμπί "Send Order", αποστέλλεται η παραγγελία
        sendOrder.setOnClickListener(v -> sendOrderToDatabase());
    }

    private void loadCustomerInfo() {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        fullName = prefs.getString("fullName", "Unknown User");
    }
    private void loadCartItems() {
        SharedPreferences prefs = getSharedPreferences("cart", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("cartList", null);
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        cartList = json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    private void displayCartItems() {
        ordersContainer.removeAllViews();
        for (Product product : cartList) {
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
            textView.setTextSize(18);
            textView.setPadding(20, 0, 0, 0);

            productLayout.addView(imageView);
            productLayout.addView(textView);

            ordersContainer.addView(productLayout);
        }
    }

    private void sendOrderToDatabase() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Δημιουργία timestamp για την παραγγελία
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Δημιουργία μοναδικού Order ID
        String orderId = ordersRef.push().getKey();

        if (orderId == null) {
            Toast.makeText(this, "Error generating order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Δημιουργία δομής παραγγελίας
        OrderData orderData = new OrderData(fullName, timestamp, cartList);

        // Αποθήκευση παραγγελίας στη Firebase
        ordersRef.child(orderId).setValue(orderData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Order Sent!", Toast.LENGTH_SHORT).show();

                // Καθαρισμός του καλαθιού μετά την παραγγελία
                clearCart();

                // Επιστροφή στα προϊόντα
                Intent intent = new Intent(Order.this, Products.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to send order. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCart() {
        SharedPreferences prefs = getSharedPreferences("cart", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("cartList");
        editor.apply();
    }

    public void ProductsPage(View view){
        Intent intent=new Intent(Order.this,Products.class);
        startActivity(intent);
    }

    // Μοντέλο δεδομένων για αποθήκευση παραγγελίας
    public static class OrderData {
        public String fullName;
        public String timestamp;
        public List<Product> products;

        public OrderData() {}

        public OrderData(String fullName, String timestamp, List<Product> products) {
            this.fullName = fullName;
            this.timestamp = timestamp;
            this.products = products;
        }
    }

    public void SeeOrders(View view) {
        Intent intent = new Intent(Order.this, Orders_view.class);
        startActivity(intent);
    }

    public void OrderBagfunction (View view){
        Intent intent = new Intent(Order.this,Order.class);
        startActivity(intent);
    }

    public void ProfileFunction (View view){
        Intent intent = new Intent(Order.this,Settings.class);
        startActivity(intent);
    }
}