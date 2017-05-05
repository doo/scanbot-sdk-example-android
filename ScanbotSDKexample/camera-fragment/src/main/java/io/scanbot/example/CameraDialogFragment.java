package io.scanbot.example;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.ui.PolygonView;

/**
 * {@link ScanbotCameraView} integrated in {@link DialogFragment} example
 */
public class CameraDialogFragment extends DialogFragment implements PictureCallback {
    private ScanbotCameraView cameraView;
    private ImageView resultView;

    /**
     * Create a new instance of CameraDialogFragment
     */
    static CameraDialogFragment newInstance() {
        return new CameraDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseView =  getActivity().getLayoutInflater().inflate(R.layout.scanbot_camera_view, container, false);

        cameraView = (ScanbotCameraView) baseView.findViewById(R.id.camera);
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.continuousFocus();
                    }
                });
            }
        });

        resultView = (ImageView) baseView.findViewById(R.id.result);

        ContourDetectorFrameHandler contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

        PolygonView polygonView = (PolygonView) baseView.findViewById(R.id.polygonView);
        contourDetectorFrameHandler.addResultHandler(polygonView);

        AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);

        cameraView.addPictureCallback(this);

        baseView.findViewById(R.id.snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture(false);
            }
        });

        baseView.findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {

            boolean flashEnabled = false;

            @Override
            public void onClick(View v) {
                cameraView.useFlash(!flashEnabled);
                flashEnabled = !flashEnabled;
            }
        });

        return baseView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    public void onPictureTaken(final byte[] image, int imageOrientation) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        resultView.post(new Runnable() {
            @Override
            public void run() {
                resultView.setImageBitmap(bitmap);
                cameraView.continuousFocus();
                cameraView.startPreview();
            }
        });
    }
}

