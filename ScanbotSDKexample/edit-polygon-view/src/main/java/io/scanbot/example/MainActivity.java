package io.scanbot.example;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;
import net.doo.snap.util.DrawMagnifierListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements DrawMagnifierListener {
    private static final String POLYGON = "polygon";

    private MagnifierView magnifierView;
    private EditPolygonImageView editPolygonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        magnifierView = (MagnifierView) findViewById(R.id.magnifier);
        editPolygonView = (EditPolygonImageView) findViewById(R.id.polygonView);
        findViewById(R.id.get_polygon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call editPolygonView.getPolygon() to get new selected polygon.
                Log.i("EDIT_POLYGON_EXAMPLE", editPolygonView.getPolygon().toString());
            }
        });

        editPolygonView.setImageResource(R.drawable.test_receipt);
        // MagifierView should be set up every time when editPolygonView is set with new image
        magnifierView.setupMagnifier(editPolygonView);

        setPolygon(savedInstanceState);

        new DetectLines().executeOnExecutor(Executors.newSingleThreadExecutor(), ((BitmapDrawable) editPolygonView.getDrawable()).getBitmap());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(POLYGON, (ArrayList<PointF>) editPolygonView.getPolygon());
    }

    @Override
    public void drawMagnifier(PointF zoomPoint) {
        magnifierView.drawMagnifier(zoomPoint);
    }

    @Override
    public void eraseMagnifier() {
        magnifierView.eraseMagnifier();
    }

    private void setPolygon(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            editPolygonView.setPolygon(EditPolygonImageView.DEFAULT_POLYGON);
            return;
        }

        ArrayList<PointF> polygon = savedInstanceState.getParcelableArrayList(POLYGON);
        editPolygonView.setPolygon(polygon);
    }

    /**
     * Detects horizontal and vertical lines of the polygon
     */
    class DetectLines extends AsyncTask<Bitmap, Void, Pair<List<Line2D>, List<Line2D>>> {

        @Override
        protected Pair<List<Line2D>, List<Line2D>> doInBackground(Bitmap... params) {
            Bitmap image = params[0];
            ContourDetector detector = new ContourDetector();
            final DetectionResult detectionResult = detector.detect(image);
            switch (detectionResult) {
                case OK:
                case OK_BUT_BAD_ANGLES:
                case OK_BUT_TOO_SMALL:
                case OK_BUT_BAD_ASPECT_RATIO:
                    return new Pair<>(detector.getHorizontalLines(), detector.getVerticalLines());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Pair<List<Line2D>, List<Line2D>> listListPair) {
            super.onPostExecute(listListPair);
            if (listListPair != null) {
                editPolygonView.setLines(listListPair.first, listListPair.second);
            }
        }
    }
}
