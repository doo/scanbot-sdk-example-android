package io.scanbot.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.entity.Blob;
import net.doo.snap.entity.Document;
import net.doo.snap.entity.Language;
import net.doo.snap.entity.OcrStatus;
import net.doo.snap.entity.Page;
import net.doo.snap.entity.SnappingDraft;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.persistence.PageFactory;
import net.doo.snap.persistence.cleanup.Cleaner;
import net.doo.snap.process.DocumentProcessingResult;
import net.doo.snap.process.DocumentProcessor;
import net.doo.snap.process.OcrResult;
import net.doo.snap.process.TextRecognition;
import net.doo.snap.process.draft.DocumentDraftExtractor;
import net.doo.snap.process.util.DocumentDraft;
import net.doo.snap.util.FileChooserUtils;
import net.doo.snap.util.bitmap.BitmapUtils;
import net.doo.snap.util.thread.MimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.ocr.OpticalCharacterRecognizer;
import io.scanbot.sdk.persistence.PageFileStorage;
import io.scanbot.sdk.process.ImageFilterType;
import io.scanbot.sdk.process.PDFPageSize;
import io.scanbot.sdk.process.PageProcessor;


public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE_REQUEST = 100;
    private static final String IMAGE_TYPE = "image/*";

    private View progressView;
    private OpticalCharacterRecognizer opticalCharacterRecognizer;
    private PageFileStorage pageFileStorage;
    private PageProcessor pageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();

        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
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

        new RecognizeTextWithPDFTask(imageUri).execute();

// Alternative OCR examples:
//        new RecognizeTextWithoutPDFTask(imageUri).execute();

        progressView.setVisibility(View.VISIBLE);
    }

    private void initDependencies() {
        ScanbotSDK scanbotSDK = new ScanbotSDK(this);
        opticalCharacterRecognizer = scanbotSDK.ocrRecogniser();
        pageFileStorage = scanbotSDK.getPageFileStorage();
        pageProcessor = scanbotSDK.pageProcessor();
    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class RecognizeTextWithoutPDFTask extends AsyncTask<Void, Void, OcrResult> {

        private final Uri imageUri;

        private RecognizeTextWithoutPDFTask(Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected OcrResult doInBackground(Void... voids) {
            try {
                Bitmap bitmap = loadImage();

                String newPageId = pageFileStorage.add(bitmap);
                io.scanbot.sdk.persistence.Page page = new io.scanbot.sdk.persistence.Page(newPageId, Collections.<PointF>emptyList(), DetectionResult.OK, ImageFilterType.BINARIZED);
                io.scanbot.sdk.persistence.Page processedPage = pageProcessor.detectDocument(page);
                List<io.scanbot.sdk.persistence.Page> pages = new ArrayList<>();
                pages.add(processedPage);

                return opticalCharacterRecognizer.recognizeTextFromPages(pages);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        protected void onPostExecute(OcrResult ocrResult) {
            progressView.setVisibility(View.GONE);

            if (ocrResult != null) {
                Log.i("Scanbot SDK OCR example", "Recognized page content:\n" + ocrResult.recognizedText);
            }
        }

    }

    /*
    This AsyncTask is used here only for the sake of example. Please, try to avoid usage of
    AsyncTasks in your application
     */
    private class RecognizeTextWithPDFTask extends AsyncTask<Void, Void, OcrResult> {

        private final Uri imageUri;

        private RecognizeTextWithPDFTask(Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected OcrResult doInBackground(Void... voids) {
            try {
                Bitmap bitmap = loadImage();
                String newPageId = pageFileStorage.add(bitmap);
                io.scanbot.sdk.persistence.Page page = new io.scanbot.sdk.persistence.Page(newPageId, Collections.<PointF>emptyList(), DetectionResult.OK, ImageFilterType.BINARIZED);
                io.scanbot.sdk.persistence.Page processedPage = pageProcessor.detectDocument(page);
                List<io.scanbot.sdk.persistence.Page> pages = new ArrayList<>();
                pages.add(processedPage);

                return opticalCharacterRecognizer.recognizeTextWithPdfFromPages(pages, PDFPageSize.A4);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        protected void onPostExecute(OcrResult ocrResult) {
            progressView.setVisibility(View.GONE);

            if (ocrResult != null) {
                Log.i("Scanbot SDK OCR example", "Recognized page content:\n" + ocrResult.recognizedText);
                Log.i("Scanbot SDK OCR example", "Generated document path:\n" + ocrResult.sandwichedPdfDocumentFile.getPath());
            }
        }

    }
}
