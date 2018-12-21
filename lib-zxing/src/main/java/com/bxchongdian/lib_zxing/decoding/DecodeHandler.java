/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bxchongdian.lib_zxing.decoding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.bxchongdian.lib_zxing.R;
import com.bxchongdian.lib_zxing.activity.CaptureFragment;
import com.bxchongdian.lib_zxing.camera.CameraManager;
import com.bxchongdian.lib_zxing.camera.PlanarYUVLuminanceSource;

import java.util.Hashtable;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureFragment   fragment;
    private final MultiFormatReader multiFormatReader;

    DecodeHandler(CaptureFragment fragment, Hashtable<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.fragment = fragment;
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (message.what == R.id.quit) {
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;

        //modify here
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        final PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }
//        if (rawResult != null) {
//            // Don't log the barcode contents for security.
//            long end = System.currentTimeMillis();
//            Log.d(TAG, "Found barcode in " + (end - start) + " ms");
//            if (fragment.getHandler() != null) {
//                float point1X = rawResult.getResultPoints()[0].getX();
//                float point1Y = rawResult.getResultPoints()[0].getY();
//                float point2X = rawResult.getResultPoints()[1].getX();
//                float point2Y = rawResult.getResultPoints()[1].getY();
//                int len = (int) Math.sqrt(Math.abs(point1X - point2X) * Math.abs(point1X - point2X) + Math.abs(point1Y - point2Y) * Math.abs(point1Y - point2Y));
//                if (frameRect != null) {
//                    int frameWidth = frameRect.right - frameRect.left;
//                    Camera camera = activity.getCameraManager().getCameraNotStatic();
//                    Camera.Parameters parameters = camera.getParameters();
//                    final int maxZoom = parameters.getMaxZoom();
//                    int zoom = parameters.getZoom();
//                    if (parameters.isZoomSupported()) {
//                        if (len <= frameWidth / 4) {
//                            if (zoom == 0) {
//                                zoom = maxZoom / 3;
//                            } else {
//                                zoom = zoom + 10;
//                            }
//                            if (zoom > maxZoom) {
//                                zoom = maxZoom;
//                            }
//                            parameters.setZoom(zoom);
//                            camera.setParameters(parameters);
//                            final Result finalRawResult = rawResult;
//                            postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, finalRawResult);
//                                    Bundle bundle = new Bundle();
//                                    bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
//                                    message.setData(bundle);
//                                    message.sendToTarget();
//                                }
//                            }, 1000);
//
//                        } else {
//                            Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, rawResult);
//                            Bundle bundle = new Bundle();
//                            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
//                            message.setData(bundle);
//                            message.sendToTarget();
//                        }
//                    }
//                } else {
//                    Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, rawResult);
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
//                    message.setData(bundle);
//                    message.sendToTarget();
//                }
//            }
//        } else {
//            if (fragment.getHandler() != null) {
//                Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed);
//                message.sendToTarget();
//            }
//        }
        if (rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
            message.setData(bundle);
            //Log.d(TAG, "Sending decode succeeded message...");
            message.sendToTarget();
        } else {
            Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }

}
