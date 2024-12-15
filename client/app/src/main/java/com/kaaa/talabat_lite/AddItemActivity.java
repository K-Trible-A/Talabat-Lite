package com.kaaa.talabat_lite;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AddItemActivity extends AppCompatActivity {

    private EditText etItemName, etItemDescription, etItemPrice;
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
        setContentView(R.layout.activity_add_item);

        // Find views
        etItemName = findViewById(R.id.et_item_name);
        etItemDescription = findViewById(R.id.et_item_description);
        etItemPrice = findViewById(R.id.et_item_price);
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
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    // Prepare the multipart request body

    public void uploadImage(Bitmap bitmap) {
        try {
            // URL of the server endpoint
            URL url = new URL(globals.serverURL + "/uploadImage/" + globals.userId);
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
        } catch (Exception e) {
            Log.e("ImageUploader", "Error uploading image");
        }
    }
    // Save item
    private void saveItem() {
        String name = etItemName.getText().toString().trim();
        String description = etItemDescription.getText().toString().trim();
        String price = etItemPrice.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || selectedImage == null) {
            showToast("Please fill all fields and upload an image");
            return;
        }

        uploadImage(selectedImage);

        try{
            URL server = new URL(globals.serverURL + "/add_item/" + globals.userId);
            // Create the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("itemName", name);
            jsonPayload.put("itemDescription", description);
            jsonPayload.put("itemPrice", price);

            // Open a connection to the server
            HttpURLConnection conn = (HttpURLConnection) server.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true); // To send a body
            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            // Get the response code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showToast("Item added successfully");
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                Intent outIntent = new Intent(this, MerchantActivity.class);
                outIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(outIntent);
                finish();
            }
            else {
                showToast("Uploading error");
                Log.i("AddItem", conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (IOException e) {
            setResult(RESULT_CANCELED); // Indicate failure to the caller
            Intent backIntent = new Intent(AddItemActivity.this,MerchantActivity.class);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(backIntent);
            finish();
            showToast("Error saving the item");
        } catch (JSONException e) {
            Log.e("AddItem", "Json error");
            setResult(RESULT_CANCELED); // Indicate failure to the caller
            Intent backIntent = new Intent(AddItemActivity.this,MerchantActivity.class);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(backIntent);
            finish();
            showToast("Error saving the item");
        }
    }
}
