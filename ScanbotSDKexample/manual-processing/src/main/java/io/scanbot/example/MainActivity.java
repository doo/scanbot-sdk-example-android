package io.scanbot.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.entity.Document;
import net.doo.snap.entity.Page;
import net.doo.snap.entity.SnappingDraft;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.persistence.PageFactory;
import net.doo.snap.persistence.cleanup.Cleaner;
import net.doo.snap.process.DocumentProcessingResult;
import net.doo.snap.process.DocumentProcessor;
import net.doo.snap.process.draft.DocumentDraftExtractor;
import net.doo.snap.process.util.DocumentDraft;
import net.doo.snap.util.FileChooserUtils;
import net.doo.snap.util.bitmap.BitmapUtils;
import net.doo.snap.util.thread.MimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_REQUEST = 100;
    private static final String IMAGE_TYPE = "image/*";

    private PageFactory pageFactory;
    private DocumentDraftExtractor documentDraftExtractor;
    private DocumentProcessor documentProcessor;
    private Cleaner cleaner;

    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDependencies();

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        progressView = findViewById(R.id.progressBar);
    }

    private void initializeDependencies() {
        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        pageFactory = scanbotSDK.pageFactory();
        documentDraftExtractor = scanbotSDK.documentDraftExtractor();
        documentProcessor = scanbotSDK.documentProcessor();
        cleaner = scanbotSDK.cleaner();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType(IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        startActivityForResult(
                Intent.createChooser(intent, "Select picture"),
                SELECT_PICTURE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode != SELECT_PICTURE_REQUEST || resultCode != RESULT_OK) {
            return;
        }

        Uri imageUri = intent.getData();

        if (imageUri == null) {
            return;
        }

        new ProcessDocumentTask(imageUri).execute();
        progressView.setVisibility(View.VISIBLE);
    }

    private void openDocument(DocumentProcessingResult documentProcessingResult) {
        Document document = documentProcessingResult.getDocument();
        File documentFile = documentProcessingResult.getDocumentFile();

        Intent openIntent = new Intent();
        openIntent.setAction(Intent.ACTION_VIEW);
        openIntent.setDataAndType(
                Uri.fromFile(documentFile),
                MimeUtils.getMimeByName(document.getName())
        );

        if (openIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(openIntent);
        } else {
            Toast.makeText(MainActivity.this, "Error while opening the document", Toast.LENGTH_LONG).show();
        }
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class ProcessDocumentTask extends AsyncTask<Void, Void, List<DocumentProcessingResult>> {

        private final Uri imageUri;
        private final int screenWidth;
        private final int screenHeight;

        private ProcessDocumentTask(Uri imageUri) {
            this.imageUri = imageUri;

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }

        @Override
        protected List<DocumentProcessingResult> doInBackground(Void... voids) {
            List<DocumentProcessingResult> results = new ArrayList<>();

            try {
                Bitmap bitmap = loadImage();
                Bitmap result = applyFilters(bitmap);

                Page page = pageFactory.buildPage(result, screenWidth, screenHeight).page;
                SnappingDraft snappingDraft = new SnappingDraft(page);
                DocumentDraft[] drafts = documentDraftExtractor.extract(snappingDraft);

                for (DocumentDraft draft : drafts) {
                    try {
                        results.add(documentProcessor.processDocument(draft));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                cleaner.cleanUp();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return results;
        }

        private Bitmap applyFilters(Bitmap bitmap) {
            ContourDetector detector = new ContourDetector();
            detector.detect(bitmap);
            List<PointF> polygon = detector.getPolygonF();

            /*
             * This operation crops original bitmap and creates a new one. Old bitmap is recycled
             * and can't be used anymore. If that's not what you need, use processImageF() instead
             */
            return detector.processImageAndRelease(bitmap, polygon, ContourDetector.IMAGE_FILTER_GRAY);
        }

        private Bitmap loadImage() throws IOException {
            String imagePath = FileChooserUtils.getPath(MainActivity.this, imageUri);
            Bitmap bitmap = BitmapUtils.decodeQuietly(imagePath, null);

            if (bitmap == null) {
                throw new IOException("Bitmap is null");
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(List<DocumentProcessingResult> documentProcessingResults) {
            progressView.setVisibility(View.GONE);

            //open first document
            if (documentProcessingResults.size() > 0) {
                openDocument(documentProcessingResults.get(0));
            }
        }

    }

}
