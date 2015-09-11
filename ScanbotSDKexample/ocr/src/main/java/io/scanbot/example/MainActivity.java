package io.scanbot.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.entity.Blob;
import net.doo.snap.entity.Document;
import net.doo.snap.entity.Language;
import net.doo.snap.entity.OcrStatus;
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
import java.util.Collection;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_REQUEST = 100;
    private static final String IMAGE_TYPE = "image/*";

    private DocumentProcessor documentProcessor;
    private PageFactory pageFactory;
    private DocumentDraftExtractor documentDraftExtractor;
    private Cleaner cleaner;
    private BlobManager blobManager;
    private BlobFactory blobFactory;

    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<Blob> engOcrBlobs;
                try {
                    engOcrBlobs = blobFactory.ocrLanguageBlobs(Language.ENG);
                    for (Blob blob : engOcrBlobs) {
                        if (!blobManager.isBlobAvailable(blob)) {
                            Toast.makeText(MainActivity.this, "Download OCR data first!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                openGallery();
            }
        });
        findViewById(R.id.downloadOcrData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadOcrData();
            }
        });
        progressView = findViewById(R.id.progressBar);
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

    private void downloadOcrData() {
        Collection<Blob> engOcrBlobs = null;
        try {
            /*
            In example OCR will be performed only for english language documents.
            But you can use all supported languages in net.doo.snap.entity.Language enum.
             */
            engOcrBlobs = blobFactory.ocrLanguageBlobs(Language.ENG);
            for (Blob blob : engOcrBlobs) {
                if (!blobManager.isBlobAvailable(blob)) {
                    progressView.setVisibility(View.VISIBLE);
                    new MainActivity.DownloadOCRDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, "OCR data is downloaded! Try to scan some document...", Toast.LENGTH_LONG).show();
    }

    private void initDependencies() {
        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        documentProcessor = scanbotSDK.documentProcessor();
        pageFactory = scanbotSDK.pageFactory();
        documentDraftExtractor = scanbotSDK.documentDraftExtractor();
        cleaner = scanbotSDK.cleaner();
        blobManager = scanbotSDK.blobManager();
        blobFactory = scanbotSDK.blobFactory();
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
                        /*
                        Set OCR status for document processor.
                        OcrStatus.PENDING - OCR well be performed only if preference PreferencesConstants.PERFORM_OCR is true
                        and PreferencesConstants.OCR_ONLY_WHILE_CHARGING is false (or true and device is charging).

                        OcrStatus.PENDING_FORCED - OCR will be performed. Ignores all preferences flags.

                        OcrStatus.PENDING_ON_CHARGER - OCR will be performed only if device is charging. Ignores all preferences flags.
                        */
                        draft.getDocument().setOcrStatus(OcrStatus.PENDING);

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
            return detector.processImageF(bitmap, polygon, ContourDetector.IMAGE_FILTER_BINARIZED);
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
                DocumentProcessingResult result = documentProcessingResults.get(0);
                Log.i("Scanbot SDK OCR example", "First document content:\n" + result.getDocument().getOcrText());
                openDocument(result);
            }
        }

    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class DownloadOCRDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Collection<Blob> engOcrBlobs = blobFactory.ocrLanguageBlobs(Language.ENG);
                for (Blob blob : engOcrBlobs) {
                    blobManager.fetch(blob, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressView.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "English language OCR data is downloading! Try to scan some document when OCR data will be downloaded...", Toast.LENGTH_LONG).show();
        }
    }

}
