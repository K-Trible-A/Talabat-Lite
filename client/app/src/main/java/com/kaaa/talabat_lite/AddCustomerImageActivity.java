package com.kaaa.talabat_lite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class AddCustomerImageActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private Bitmap selectedImage;
    // ActivityResultLauncher for picking an image
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imgPreview.setImageBitmap(selectedImage);
                        imgPreview.setVisibility(ImageView.VISIBLE);
                    } catch (IOException e) {
                        runOnUiThread(() -> Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Find views
        setContentView(R.layout.activity_add_customer_image);
        // Find views
        imgPreview = findViewById(R.id.img_preview);
        Button btnUploadImage = findViewById(R.id.btn_upload_image);
        Button btnSaveItem = findViewById(R.id.btn_save_item);

        // Upload image functionality
        btnUploadImage.setOnClickListener(v -> openImagePicker());
        // Save item functionality
        btnSaveItem.setOnClickListener(view -> new Thread(this::saveItem).start());
    }

    // Open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    // Save item
    private void saveItem() {
        // Save the item to a database or send it to the server
        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(globals.ADD_CUSTOMER_IMAGE);
            socketHelper.getInstance().sendInt(globals.userId);
            socketHelper.getInstance().sendImg(selectedImage);
            socketHelper.getInstance().close();

            // Send result back to MerchantHomeFragment
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent); // Indicating success
            runOnUiThread(() -> Toast.makeText(this, "picture saved successfully!", Toast.LENGTH_SHORT).show());
            Intent backIntent = new Intent(AddCustomerImageActivity.this,CustomerActivity.class);
            startActivity(backIntent);
            finish(); // Finish the activity after saving
        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(this, "Error saving the item", Toast.LENGTH_SHORT).show());
            setResult(RESULT_CANCELED); // Indicate failure to the caller
            Intent backIntent = new Intent(AddCustomerImageActivity.this,CustomerActivity.class);
            startActivity(backIntent);
            finish();
        }
    }
}