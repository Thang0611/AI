package com.example.ml;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ml.flowerclassification.TFLiteClassifier;

import java.io.IOException;

public class ImageClassificationActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewResult;
    private TFLiteClassifier tfliteClassifier;
    String TAG="NHT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_classification);

        imageView = findViewById(R.id.imageView);
        textViewResult = findViewById(R.id.textViewResult);
        Button buttonBack = findViewById(R.id.buttonBack);

        // Nhận dữ liệu từ Intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        Uri imageUri = Uri.parse(imageUriString);
        Log.i(TAG, "onCreate2: "+imageUri);
        try {
            // Tải Bitmap từ URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Kiểm tra xem bitmap có phải là null không
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.i(TAG, "onCreate: "+bitmap);
                // Khởi tạo TFLiteClassifier
                tfliteClassifier = new TFLiteClassifier(getAssets(), "model.tflite");

                // Phân loại ảnh và hiển thị kết quả
                float[] results = tfliteClassifier.runInference(bitmap);
                Log.i(TAG, "onCreate: "+results);
                int predictedClass = getPredictedClass(results);
                Log.i(TAG, "onCreate: "+predictedClass);
                String[] flowerNames = {"Hoa Hồng", "Hoa Cúc", "Hoa Lan", "Hoa Huệ", "Hoa Ly"};
//                textViewResult.setText("Loài hoa dự đoán: " + flowerNames[predictedClass]);
            } else {
                textViewResult.setText("Không thể tải ảnh.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            textViewResult.setText("Lỗi khi tải ảnh: " + e.getMessage());
        }

        // Thiết lập sự kiện cho nút quay lại
        buttonBack.setOnClickListener(v -> finish());
    }

    private int getPredictedClass(float[] results) {
        int maxIndex = 0;
        float maxValue = results[0];
        for (int i = 1; i < results.length; i++) {
            if (results[i] > maxValue) {
                maxValue = results[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tfliteClassifier != null) {
            tfliteClassifier.close();
        }
    }
}
