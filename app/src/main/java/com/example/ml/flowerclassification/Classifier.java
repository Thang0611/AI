package com.example.ml.flowerclassification;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Classifier {

    private final Interpreter interpreter;
    private final ByteBuffer inputImage;

    private static final int DIM_BATCH_SIZE = 1; //batch size
    public static final int DIM_IMG_SIZE_X = 64; //image width
    public static final int DIM_IMG_SIZE_Y = 64; //image height
    private static final int DIM_PIXEL_SIZE = 3; //image channels

    private final int[] imagePixels = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
    private static final int CLASSES = 2; //flower classes = final Dense layer neurons
    private float[][] outputArray = new float[DIM_BATCH_SIZE][CLASSES]; //output probability array

    public Classifier(Activity activity) throws IOException {
        interpreter = new Interpreter(loadModelFile(activity));
        inputImage =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * DIM_IMG_SIZE_X
                                * DIM_IMG_SIZE_Y
                                * DIM_PIXEL_SIZE
                                *4);
        inputImage.order(ByteOrder.nativeOrder());
    }

    //loads the .tflite file from assets folder
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("flower.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //prime method where classification actually happens
    public int classify(Bitmap bitmap) {
        preprocess(bitmap);
        runInference();
        return postprocess();
    }

    private void preprocess(Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
    }

    //converting the bitmap image to bytebuffer
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (inputImage == null) {
            return;
        }
        inputImage.rewind();
        bitmap.getPixels(imagePixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = imagePixels[pixel++];
                inputImage.putFloat(convertToGreyScale(val));
            }
        }
    }

    //preprocess step of converting the image to grayscale
    private float convertToGreyScale(int color) {
        float r = ((color >> 16) & 0xFF);
        float g = ((color >> 8) & 0xFF);
        float b = ((color) & 0xFF);

        int grayscaleValue = (int) (0.299f * r + 0.587f * g + 0.114f * b);
        float preprocessedValue = grayscaleValue / 255.0f;
        return preprocessedValue;
    }

    //run methods takes the input image and the outputArray contains list of probabilities
    private void runInference() {
        interpreter.run(inputImage, outputArray);
    }

    //postprocess returns the index with maximum probability in the output array
    private int postprocess() {
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < outputArray[0].length; i++) {
            if (outputArray[0][i] > maxProb) {
                maxProb = outputArray[0][i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}

