package com.example.ml.flowerclassification;

import org.tensorflow.lite.Interpreter;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteClassifier {

    private Interpreter interpreter;
    private int imageSize = 150;  // Kích thước ảnh đầu vào của mô hình (150x150)

    // Khởi tạo interpreter với mô hình TFLite
    public TFLiteClassifier(AssetManager assetManager, String modelPath) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
    }

    // Tải mô hình từ thư mục assets
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Xử lý ảnh đầu vào từ Bitmap thành ByteBuffer
    private ByteBuffer preprocessImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[imageSize * imageSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixel : intValues) {
            byteBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);  // Red
            byteBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);   // Green
            byteBuffer.putFloat((pixel & 0xFF) / 255.0f);          // Blue
        }
        return byteBuffer;
    }

    // Chạy suy luận (inference)
//    public float[] runInference(Bitmap bitmap) {
//        ByteBuffer inputBuffer = preprocessImage(bitmap);
//        float[][] output = new float[1][5];  // Mô hình trả về xác suất cho 5 lớp
//        interpreter.run(inputBuffer, output);
//        return output[0];
//    }

    public float[] runInference(Bitmap bitmap) {
        ByteBuffer inputBuffer = preprocessImage(bitmap);
        // Đảm bảo rằng kích thước ở đây khớp với số lượng lớp mà mô hình trả về
        float[][] output = new float[1][5];  // Thay đổi 5 thành số lớp của bạn
        interpreter.run(inputBuffer, output);
        return output[0];
    }
    public void close() {
        interpreter.close();
    }
    private int getPredictedClass(float[] results) {
        int maxIndex = 0;
        float maxValue = results[0];

        // Kiểm tra kích thước của mảng trước khi truy cập
        if (results.length == 0) return maxIndex; // Trả về 0 nếu không có kết quả

        for (int i = 1; i < results.length; i++) {
            if (results[i] > maxValue) {
                maxValue = results[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

}
