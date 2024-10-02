package com.example.ml.flowerclassification;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ml.ImageClassificationActivity;
import com.example.ml.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity_1 extends AppCompatActivity {
    TFLiteClassifier tfliteClassifier;
    private static final int PICK_IMAGE = 1; // Mã yêu cầu cho Gallery
    private static final int TAKE_PHOTO = 2; // Mã yêu cầu cho Camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);

        try {
            tfliteClassifier = new TFLiteClassifier(getAssets(), "model.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button buttonOpenGallery = findViewById(R.id.button_open_gallery);
        Button buttonOpenCamera = findViewById(R.id.button_open_camera);

        // Thiết lập sự kiện cho nút mở Gallery
        buttonOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Thiết lập sự kiện cho nút mở Camera
        buttonOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    // Mở Gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    // Mở Camera
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO);
        }
    }

    // Xử lý kết quả trả về từ Gallery hoặc Camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                // Gọi hàm xử lý ảnh từ Gallery
                handleSelectedImage(selectedImageUri);
            } else if (requestCode == TAKE_PHOTO && data != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                // Gọi hàm xử lý ảnh từ Camera
                handleCapturedImage(imageBitmap);
            }
        }
    }

    // Xử lý ảnh được chọn từ Gallery
    private void handleSelectedImage(Uri imageUri) {
        // Chuyển đổi Uri thành Bitmap và xử lý
        // Gọi hàm xử lý inference tại đây
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//            // Resize ảnh và chuẩn bị cho inference
//            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
//            // Gọi hàm để thực hiện suy luận với mô hình TFLite
//            float[] result = tfliteClassifier.runInference(resizedBitmap);
//            // Xử lý kết quả tại đây
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//            Intent intent = new Intent(this, ImageClassificationActivity.class);
//            intent.putExtra("imageBitmap", bitmap);
//            startActivity(intent);
        Log.i("NHT", "onCreate1: "+imageUri);
            Intent intent = new Intent(this, ImageClassificationActivity.class);
            intent.putExtra("imageUri", imageUri.toString()); // Chuyển đổi URI thành String
            startActivity(intent);

    }

    // Xử lý ảnh được chụp từ Camera
    private void handleCapturedImage(Bitmap imageBitmap) {
        // Xử lý ảnh Bitmap và gọi hàm inference
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 150, 150, true);
        // Gọi hàm để thực hiện suy luận với mô hình TFLite
        float[] result = tfliteClassifier.runInference(resizedBitmap);
        // Xử lý kết quả tại đây
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}