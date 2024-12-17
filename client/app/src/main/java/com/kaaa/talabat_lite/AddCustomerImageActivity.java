package com.kaaa.talabat_lite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private void saveItem() {
        try {
            // URL of the server endpoint
            URL url = new URL(globals.serverURL + "/customer/uploadImage/" + globals.userId);
            // Encode the Bitmap to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] bitmapData = byteArrayOutputStream.toByteArray();
            // Open a connection to the server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setFixedLengthStreamingMode(bitmapData.length);
            // Write the data
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bitmapData);
            outputStream.close();
            // Get response code (for debugging)
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                // Send result back to CustomerHomeFragment
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent); // Indicating success
                runOnUiThread(() -> Toast.makeText(this, "picture saved successfully!", Toast.LENGTH_SHORT).show());
                Intent backIntent = new Intent(AddCustomerImageActivity.this,CustomerActivity.class);
                startActivity(backIntent);
                finish(); // Finish the activity after saving
            }
            else{
                runOnUiThread(() -> Toast.makeText(this, "Error saving the item", Toast.LENGTH_SHORT).show());
                setResult(RESULT_CANCELED); // Indicate failure to the caller
                Intent backIntent = new Intent(AddCustomerImageActivity.this,CustomerActivity.class);
                startActivity(backIntent);
                finish();
            }
        } catch (Exception e) {
            Log.e("ImageUploader", "Error uploading image");
        }
    }
}