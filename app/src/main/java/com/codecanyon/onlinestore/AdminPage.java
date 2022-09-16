package com.codecanyon.onlinestore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.codecanyon.onlinestore.admin.AddCategory;
import com.codecanyon.onlinestore.admin.AddProduct;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AdminPage extends AppCompatActivity {

    AppCompatButton addProduct, addCategory, addImage;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    private CircularProgressIndicator pd;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);
        
        addProduct = findViewById(R.id.addProduct);
        addCategory = findViewById(R.id.addCategory);
        addImage = findViewById(R.id.addImage);
        pd = findViewById(R.id.pd);
        back = findViewById(R.id.back);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        addProduct.setOnClickListener(view -> {
            startActivity(new Intent(AdminPage.this, AddProduct.class));
        });

        addCategory.setOnClickListener(view -> {
            startActivity(new Intent(AdminPage.this, AddCategory.class));
        });

        addImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/png");
            startActivityForResult(intent, 100);
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(AdminPage.this, LoginPage.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = data.getData();
        uploadImage();
    }

    private void uploadImage() {
        pd.setVisibility(View.VISIBLE);
        final String randomKey = UUID.randomUUID().toString();
        StorageReference reference = storageReference.child("ProductImages/" + randomKey);
        reference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    pd.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Image Uploaded to Firebase Storage",
                            Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    pd.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Image cannot be uploaded",
                            Snackbar.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pd.setProgress((int)progress, true);
                    }
                });

    }
}