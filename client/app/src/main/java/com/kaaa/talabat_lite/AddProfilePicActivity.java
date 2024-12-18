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

public class AddProfilePicActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_add_profile_pic);

        // Find views

        imgPreview = findViewById(R.id.profile_img_preview);
        Button btnUploadImage = findViewById(R.id.btn_upload_profile_image);
        Button btnSaveImage = findViewById(R.id.btn_save_image);

        // Upload image functionality
        btnUploadImage.setOnClickListener(v -> openImagePicker());
        // Save image functionality
        btnSaveImage.setOnClickListener(view -> {
            if (selectedImage == null) {
                showToast("Please select an image before saving.");
                return;
            }
            new Thread(() -> uploadProfileImage(selectedImage)).start();
        });
    }

    // Open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    // Prepare the multipart request body

    public void uploadProfileImage(Bitmap bitmap) {
        try {
            // URL of the server endpoint
            URL url = new URL(globals.serverURL + "/uploadProfileImage/" + globals.userId);
            // Encode the Bitmap to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
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
            Log.i("ImageUPload", String.valueOf(responseCode));
            showToast("Item added successfully");
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            Intent outIntent = new Intent(this, MerchantActivity.class);
            outIntent.putExtra("profile",true);
            outIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(outIntent);
            finish();
        } catch (Exception e) {
            Log.e("ImageUploader", "Error uploading image");
            setResult(RESULT_CANCELED); // Indicate failure to the caller
            Intent outIntent = new Intent(AddProfilePicActivity.this,MerchantActivity.class);
            outIntent.putExtra("profile",true);
            outIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(outIntent);
            finish();
            showToast("Error saving the image");
        }
    }
}
