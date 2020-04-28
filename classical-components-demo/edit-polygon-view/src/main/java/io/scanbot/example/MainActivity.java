package io.scanbot.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import io.scanbot.sdk.ScanbotSDK;

public class MainActivity extends AppCompatActivity {

    private EditPolygonImageView editPolygonView;
    private MagnifierView magnifierView;
    private Bitmap originalBitmap;
    private ImageView resultImageView;
    private Button cropButton;
    private Button rotateButton;
    private Button backButton;
    private int rotationDegrees = 0;
    private long lastRotationEventTs = 0L;
    private ScanbotSDK scanbotSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanbotSDK = new ScanbotSDK(this);
        getSupportActionBar().hide();

        editPolygonView = findViewById(R.id.polygonView);

        magnifierView = findViewById(R.id.magnifier);

        resultImageView = findViewById(R.id.resultImageView);
        resultImageView.setVisibility(View.GONE);

        cropButton = findViewById(R.id.cropButton);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });

        rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotatePreview();
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton.setVisibility(View.GONE);
                resultImageView.setVisibility(View.GONE);

                editPolygonView.setVisibility(View.VISIBLE);
                cropButton.setVisibility(View.VISIBLE);
                rotateButton.setVisibility(View.VISIBLE);
            }
        });

        new InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    private Bitmap loadBitmapFromAssets(final String filePath) {
        try {
            final InputStream is = this.getAssets().open(filePath);
            return BitmapFactory.decodeStream(is);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap resizeForPreview(final Bitmap bitmap) {
        final float maxW = 1000, maxH = 1000;
        final float oldWidth = bitmap.getWidth();
        final float oldHeight = bitmap.getHeight();
        final float scaleFactor = (oldWidth > oldHeight ? (maxW / oldWidth) : (maxH / oldHeight));
        final int scaledWidth = Math.round(oldWidth * scaleFactor);
        final int scaledHeight = Math.round(oldHeight * scaleFactor);
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);
    }

    private void rotatePreview() {
        if ((System.currentTimeMillis() - lastRotationEventTs) < 350) {
            return;
        }
        rotationDegrees += 90;
        editPolygonView.rotateClockwise(); // rotates only the preview image
        lastRotationEventTs = System.currentTimeMillis();
    }

    private void crop() {
        final ContourDetector detector = scanbotSDK.contourDetector();
        detector.detect(originalBitmap);
        List<Operation> operations = new ArrayList<>();
        operations.add(new CropOperation(editPolygonView.getPolygon()));
        // crop & warp image by selected polygon (editPolygonView.getPolygon())
        Bitmap documentImage = scanbotSDK.imageProcessor().process(originalBitmap, operations, false);


        if (rotationDegrees > 0) {
            // rotate the final cropped image result based on current rotation value:
            final Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            documentImage = Bitmap.createBitmap(documentImage, 0, 0, documentImage.getWidth(), documentImage.getHeight(), matrix, true);
        }

        editPolygonView.setVisibility(View.GONE);
        cropButton.setVisibility(View.GONE);
        rotateButton.setVisibility(View.GONE);

        resultImageView.setImageBitmap(resizeForPreview(documentImage));
        resultImageView.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
    }


    // We use AsyncTask only for simplicity here. Avoid using it in your production app due to memory leaks, etc!
    class InitImageViewTask extends AsyncTask<Void, Void, InitImageResult> {
        private Bitmap previewBitmap;

        @Override
        protected InitImageResult doInBackground(Void... params) {
            originalBitmap = loadBitmapFromAssets("demo_image.jpg");
            previewBitmap = resizeForPreview(originalBitmap);

            final ContourDetector detector = new ScanbotSDK(MainActivity.this).contourDetector();
            final DetectionResult detectionResult = detector.detect(originalBitmap);
            Pair<List<Line2D>, List<Line2D>> linesPair = null;
            List<PointF> polygon = new ArrayList<>(EditPolygonImageView.DEFAULT_POLYGON);
            switch (detectionResult) {
                case OK:
                case OK_BUT_BAD_ANGLES:
                case OK_BUT_TOO_SMALL:
                case OK_BUT_BAD_ASPECT_RATIO:
                    linesPair = new Pair<>(detector.getHorizontalLines(), detector.getVerticalLines());
                    polygon = detector.getPolygonF();
                    break;
            }

            return new InitImageResult(linesPair, polygon);
        }

        @Override
        protected void onPostExecute(final InitImageResult initImageResult) {
            editPolygonView.setImageBitmap(previewBitmap);
            magnifierView.setupMagnifier(editPolygonView);

            // set detected polygon and lines into EditPolygonImageView
            editPolygonView.setPolygon(initImageResult.polygon);
            if (initImageResult.linesPair != null) {
                editPolygonView.setLines(initImageResult.linesPair.first, initImageResult.linesPair.second);
            }
        }
    }

    class InitImageResult {
        final Pair<List<Line2D>, List<Line2D>> linesPair;
        final List<PointF> polygon;

        InitImageResult(final Pair<List<Line2D>, List<Line2D>> linesPair, final List<PointF> polygon) {
            this.linesPair = linesPair;
            this.polygon = polygon;
        }
    }

}
