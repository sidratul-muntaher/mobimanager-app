package com.iis.mobimanager2.ocr;


import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.iis.mobimanager2.ocr.camera.GraphicOverlay;
import com.iis.mobimanager2.ocr.camera.OcrListener;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    private final String PATTERN = "(?=(?:\\D*\\d){15})[0-9]*$";
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private OcrListener mListener;
    Pattern pattern;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, OcrListener mListener) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.mListener = mListener;
        pattern = Pattern.compile(PATTERN);
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public synchronized  void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        final SparseArray<TextBlock> items = detections.getDetectedItems();
        if (items == null || items.size() == 0) {
            return;
        }
        final List<String> imes = new ArrayList<>();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);
            final String value = item.getValue();
            if (value != null && !value.isEmpty()) {
                Matcher matcher = pattern.matcher(value);
                List<String> allMatches = getAllMatchStrings(matcher);
                if (allMatches.size() == 1) {
                    String ime = allMatches.get(0);
                    String validImei = value.replace(ime, "");
                    if (validImei.contains("1")) {
                        imes.add(0, ime);
                    } else if (validImei.contains("2")) {
                        imes.add(1, ime);
                    }
                }
            }
        }
        if (imes.size() == 2) {
            mListener.receiveDetections(imes.get(0), imes.get(1));
        }
    }

    private List<String> getAllMatchStrings(Matcher matcher) {
        List<String> allMatches = new ArrayList<>();
        while (matcher.find()) {
            allMatches.add(matcher.group());
        }
        return allMatches;
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
