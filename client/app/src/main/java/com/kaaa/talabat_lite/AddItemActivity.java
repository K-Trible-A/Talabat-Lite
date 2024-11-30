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

import java.io.IOException;

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
                        runOnUiThread(()->Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show());
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

    // Save item
    private void saveItem() {
        String name = etItemName.getText().toString().trim();
        String description = etItemDescription.getText().toString().trim();
        String price = etItemPrice.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || selectedImage == null) {
            runOnUiThread(()->Toast.makeText(this, "Please fill all fields and upload an image", Toast.LENGTH_SHORT).show());
            return;
        }

        // Save the item to a database or send it to the server
        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(globals.ADD_ITEM);
            Log.i("USERID", String.valueOf(globals.userId));
            socketHelper.getInstance().sendInt(globals.userId);
            socketHelper.getInstance().sendString(name);
            socketHelper.getInstance().sendString(description);
            socketHelper.getInstance().sendFloat(Float.parseFloat(price));
            socketHelper.getInstance().sendImg(selectedImage);

            socketHelper.getInstance().close();

            runOnUiThread(() -> {
                Toast.makeText(this, "Item saved successfully!", Toast.LENGTH_SHORT).show();
                // Clear the form
                etItemName.setText("");
                etItemDescription.setText("");
                etItemPrice.setText("");
                imgPreview.setImageBitmap(null);
                imgPreview.setVisibility(ImageView.GONE);
                selectedImage = null;
            });
        }
        catch (IOException e){
            runOnUiThread(() -> Toast.makeText(this, "Error saving the item", Toast.LENGTH_SHORT).show());
        }
    }
}
