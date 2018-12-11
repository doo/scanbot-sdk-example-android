package io.scanbot.example;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        editPolygonView = (EditPolygonImageView) findViewById(R.id.polygonView);
        editPolygonView.setImageResource(R.drawable.test_receipt);
        originalBitmap = ((BitmapDrawable) editPolygonView.getDrawable()).getBitmap();

        magnifierView = (MagnifierView) findViewById(R.id.magnifier);
        // MagifierView should be set up every time when editPolygonView is set with new image
        magnifierView.setupMagnifier(editPolygonView);

        resultImageView = (ImageView) findViewById(R.id.resultImageView);
        resultImageView.setVisibility(View.GONE);

        cropButton = (Button) findViewById(R.id.cropButton);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });

        rotateButton = (Button) findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotatePreview();
            }
        });

        backButton = (Button) findViewById(R.id.backButton);
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

        new InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor(), originalBitmap);
    }

    private void rotatePreview() {
        if ((System.currentTimeMillis() - lastRotationEventTs) < 350) {
            return;
        }
        rotationDegrees += 90;
        editPolygonView.rotateClockwise(); // only rotates the preview image (animated)
        lastRotationEventTs = System.currentTimeMillis();
    }

    private void crop() {
        // crop & warp image by selected polygon (editPolygonView.getPolygon())
        Bitmap documentImage = new ContourDetector().processImageF(
                originalBitmap, editPolygonView.getPolygon(), ContourDetector.IMAGE_FILTER_NONE);

        if (rotationDegrees > 0) {
            // rotate the final cropped image result based on current rotation value:
            final Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            documentImage = Bitmap.createBitmap(documentImage, 0, 0, documentImage.getWidth(), documentImage.getHeight(), matrix, true);
        }

        editPolygonView.setVisibility(View.GONE);
        cropButton.setVisibility(View.GONE);
        rotateButton.setVisibility(View.GONE);

        resultImageView.setImageBitmap(documentImage);
        resultImageView.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
    }

    /**
     * Detects horizontal and vertical lines and polygon of the given bitmap image.
     * Initializes EditPolygonImageView with detected lines and polygon.
     */
    class InitImageViewTask extends AsyncTask<Bitmap, Void, InitImageResult> {

        @Override
        protected InitImageResult doInBackground(Bitmap... params) {
            Bitmap image = params[0];
            ContourDetector detector = new ContourDetector();
            final DetectionResult detectionResult = detector.detect(image);
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
