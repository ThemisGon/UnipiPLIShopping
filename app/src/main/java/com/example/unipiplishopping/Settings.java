package com.example.unipiplishopping;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//Για την ειδοποίηση
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.os.Build;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//Για τις ειδοποιήσεις
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Settings extends BaseActivity implements LocationListener{

    TextView textView,userInfoName,userInfoEmail,sampleText ;
    private Button colorRed, colorBlue, colorGreen, french,spanish,english;
    private Button increaseTextSize, decreaseTextSize;
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private FirebaseAuth fAuth;
    private String userId;

    private LinearLayout main;

    LocationManager locationManager;

    private static final int LOCATION_PERMISSION_REQUEST = 123;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "nearby_product_channel";

    private List<StoreProduct> storeProducts; // Λίστα προϊόντων με τις τοποθεσίες των καταστημάτων

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Για τα notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        french = findViewById(R.id.french);
        spanish = findViewById(R.id.spanish);
        english = findViewById(R.id.english);
        userInfoEmail = findViewById(R.id.userInfoEmail);
        userInfoName = findViewById(R.id.userInfoName);
        textView =findViewById(R.id.textView);
        sampleText = findViewById(R.id.sampleText);
        colorRed = findViewById(R.id.colorRed);
        colorBlue = findViewById(R.id.colorBlue);
        colorGreen = findViewById(R.id.colorGreen);
        main = findViewById(R.id.main);
        increaseTextSize = findViewById(R.id.increaseTextSize);
        decreaseTextSize = findViewById(R.id.decreaseTextSize);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Φόρτωση αποθηκευμένων ρυθμίσεων
        UserPreferences.applySettings(this, new TextView[]{userInfoName, userInfoEmail, sampleText}, main);

        // Αλλαγή χρώματος
        colorRed.setOnClickListener(v -> {
            UserPreferences.saveBackgroundColor(this, "#D83F3F"); // Κόκκινο
            UserPreferences.applySettings(this, new TextView[]{userInfoName, userInfoEmail, sampleText}, main);
        });

        colorBlue.setOnClickListener(v -> {
            UserPreferences.saveBackgroundColor(this, "#3F74D8"); // Μπλε
            UserPreferences.applySettings(this, new TextView[]{userInfoName, userInfoEmail, sampleText}, main);
        });

        colorGreen.setOnClickListener(v -> {
            UserPreferences.saveBackgroundColor(this, "#82AC67"); // Πράσινο
            UserPreferences.applySettings(this, new TextView[]{userInfoName, userInfoEmail, sampleText}, main);
        });

        // Αλλαγή μεγέθους γραμματοσειράς
        increaseTextSize.setOnClickListener(v -> {
            float newSize = UserPreferences.getFontSize(this) + 2f;
            UserPreferences.saveFontSize(this, newSize);
            UserPreferences.applySettings(this, new TextView[]{userInfoName, userInfoEmail, sampleText}, main);
        });

        decreaseTextSize.setOnClickListener(v -> {
            float newSize = UserPreferences.getFontSize(this) - 2f;
            UserPreferences.saveFontSize(this, newSize);
            UserPreferences.applySettings(this, new TextView[]{userInfoName, userInfoEmail, sampleText}, main);
        });


        // SharedPreferences
        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);

        // Φόρτωση αποθηκευμένων ρυθμίσεων
        loadPreferences();

        // Listeners για αλλαγή χρώματος
        colorRed.setOnClickListener(v -> changeBackgroundColor("#D83F3F"));
        colorBlue.setOnClickListener(v -> changeBackgroundColor("#3F74D8"));
        colorGreen.setOnClickListener(v -> changeBackgroundColor("#82AC67"));

        french.setOnClickListener(v -> changeLanguage("fr"));
        spanish.setOnClickListener(v -> changeLanguage("es"));
        english.setOnClickListener(v -> changeLanguage("en"));

        // Listeners για αλλαγή μεγέθους γραμματοσειράς
        increaseTextSize.setOnClickListener(v -> changeFontSize(true));
        decreaseTextSize.setOnClickListener(v -> changeFontSize(false));

        // Firebase Auth για να πάρουμε το ID του χρήστη
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();

        if (user != null) {
            userId = user.getUid();
            loadUserData();
        } else {
            Toast.makeText(this, "Δεν βρέθηκε χρήστης!", Toast.LENGTH_SHORT).show();
        }

        createNotificationChannel();
        loadStoreProducts(); // Φορτώνουμε δεδομένα προϊόντων από τη βάση
        gps();
    }
    public void gps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        textView.setText(location.getLatitude() + "," + location.getLongitude());
        checkNearbyStores(location);

        // Δημιουργούμε νέο αντικείμενο τοποθεσίας
        Location fakeLocation = new Location("mock");
        fakeLocation.setLatitude(37.9755);
        fakeLocation.setLongitude(23.7345);

        Log.d("DEBUG", "Fake Location Set: " + fakeLocation.getLatitude() + ", " + fakeLocation.getLongitude());

        // Εμφανίζουμε την ψεύτικη τοποθεσία στο UI
        textView.setText(fakeLocation.getLatitude() + ", " + fakeLocation.getLongitude());

        // Καλούμε την αναζήτηση προϊόντων με τη νέα τοποθεσία
        checkNearbyStores(fakeLocation);
    }

    private void checkNearbyStores(Location userLocation) {
        double RADIUS = 50000.0; // 50km ακτίνα για δοκιμή
        Log.d("DEBUG", "Checking nearby stores for user at: " + userLocation.getLatitude() + ", " + userLocation.getLongitude());

        for (StoreProduct product : storeProducts) {
            Location storeLocation = new Location("");
            storeLocation.setLatitude(product.getLatitude());
            storeLocation.setLongitude(product.getLongitude());

            float distance = userLocation.distanceTo(storeLocation);
            Log.d("DEBUG", "Product: " + product.getTitle() + " Distance: " + distance + " meters");

            if (distance <= RADIUS) {
                Log.d("DEBUG", "Product " + product.getTitle() + " is within range! Sending notification.");
                sendNotification(String.valueOf(product.getProductId()));
                break;
            }
        }
    }

    private void sendNotification(String productId) {
        String firebaseProductId = "product" + productId; // Χρησιμοποιούμε το πλήρες productId από τη Firebase
        Log.d("DEBUG", "sendNotification() called for productId: " + firebaseProductId);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Products").child(firebaseProductId);
        Log.d("DEBUG", "Fetching product from Firebase path: Products/" + firebaseProductId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("DEBUG", "Product not found in Firebase! Product ID: " + firebaseProductId);
                    return;
                }

                Log.d("DEBUG", "Product found in Firebase. Preparing notification...");

                // Παίρνουμε το όνομα του προϊόντος
                String title = snapshot.child("title").getValue(String.class);
                if (title == null) title = "Νέο προϊόν κοντά σας!";

                Log.d("DEBUG", "Product title: " + title);

                // Δημιουργούμε το Intent για να ανοίξει το προϊόν
                Intent intent = new Intent(Settings.this, SingleProduct.class);
                intent.putExtra("productId", firebaseProductId);  // Στέλνουμε το πλήρες productId (π.χ. "product1")
                intent.putExtra("title", title);  // (Προαιρετικό, για να εμφανίζεται άμεσα)

                Log.d("DEBUG", "Sending notification with productId: " + firebaseProductId);

                int requestCode = (int) System.currentTimeMillis();
                PendingIntent pendingIntent = PendingIntent.getActivity(Settings.this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

                // Δημιουργία ειδοποίησης με το όνομα του προϊόντος
                NotificationCompat.Builder builder = new NotificationCompat.Builder(Settings.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.shoppingbag)
                        .setContentTitle(title)  //Εδώ προσθέτουμε το όνομα του προϊόντος
                        .setContentText("Το προϊόν \"" + title + "\" είναι κοντά σας!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Settings.this);
                if (ActivityCompat.checkSelfPermission(Settings.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("DEBUG", "Notification permission not granted!");
                    return;
                }

                Log.d("DEBUG", "Sending notification...");
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Firebase error: " + error.getMessage());
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Near Store Alerts";
            String description = "Ειδοποιήσεις για κοντινά προϊόντα";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void loadStoreProducts() {
        storeProducts = new ArrayList<>();

        // Αναφορά στη βάση δεδομένων Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Products");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeProducts.clear(); // Καθαρίζουμε τη λίστα πριν την ενημέρωση

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    String productId = productSnapshot.child("productId").getValue(String.class);
                    String title = productSnapshot.child("title").getValue(String.class);
                    String description = productSnapshot.child("description").getValue(String.class);
                    double price = productSnapshot.child("price").getValue(Double.class);
                    double latitude = productSnapshot.child("storeLocation").child("latitude").getValue(Double.class);
                    double longitude = productSnapshot.child("storeLocation").child("longitude").getValue(Double.class);
                    String imageUrl = productSnapshot.child("imageUrl").getValue(String.class);

                    // Δημιουργούμε το αντικείμενο StoreProduct και το προσθέτουμε στη λίστα
                    StoreProduct product = new StoreProduct(productId, title, description, price, latitude, longitude, imageUrl);
                    storeProducts.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Σε περίπτωση αποτυχίας φόρτωσης
                System.out.println("Firebase Database Error: " + error.getMessage());
            }
        });
    }

    private void loadUserData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Ενημέρωση UI
                    userInfoName.setText(fullName);
                    userInfoEmail.setText(email);

                    Log.d("DEBUG", "User loaded: " + fullName + ", " + email);
                } else {
                    Log.e("DEBUG", "User data not found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("DEBUG", "Firebase error: " + error.getMessage());
            }
        });
    }

    //Φόρτωση αποθηκευμένων προτιμήσεων
    private void loadPreferences() {
        String savedColor = sharedPreferences.getString("background_color", "#4CAF50");
        main.setBackgroundColor(Color.parseColor(savedColor));

        float savedFontSize = sharedPreferences.getFloat("font_size", 18f);
        sampleText.setTextSize(savedFontSize);
    }

    //Αλλαγή Background
    private void changeBackgroundColor(String color) {
        main.setBackgroundColor(Color.parseColor(color));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("background_color", color);
        editor.apply();
    }

    //Αλλαγή Μεγέθους Γραμματοσειράς
    private void changeFontSize(boolean increase) {
        float currentSize = sampleText.getTextSize() / getResources().getDisplayMetrics().scaledDensity;

        if (increase) {
            currentSize += 2; // Αύξηση κατά 2sp
        } else {
            currentSize -= 2; // Μείωση κατά 2sp
        }

        sampleText.setTextSize(currentSize);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("font_size", currentSize);
        editor.apply();
    }

    private void changeLanguage(String langCode) {
        LocaleHelper.setLocale(this, langCode);
        recreate();  // Επαναφόρτωση του activity για να εφαρμοστεί η αλλαγή
    }

    public void OrderBagfunction (View view){
        Intent intent = new Intent(Settings.this,Order.class);
        startActivity(intent);
    }

    public void ProfileFunction (View view){
        Intent intent = new Intent(Settings.this,Settings.class);
        startActivity(intent);
    }

    public void ProductsPage(View view){
        Intent intent=new Intent(Settings.this,Products.class);
        startActivity(intent);
    }
}