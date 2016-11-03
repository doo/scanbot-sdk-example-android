using Android.App;
using Android.Widget;
using Android.OS;
using Net.Doo.Snap.Camera;
using Net.Doo.Snap.UI;
using System;
using Android.Graphics;
using Java.Lang;

namespace ScanbotCameraViewDemo
{
	[Activity(Label = "ScanbotCameraViewDemo", MainLauncher = true, Icon = "@mipmap/icon")]
	public class MainActivity : Activity, IPictureCallback
	{

		ScanbotCameraView cameraView;
		ImageView resultView;

		bool flashEnabled = false;

		protected override void OnCreate(Bundle savedInstanceState)
		{
			base.OnCreate(savedInstanceState);

			// Set our view from the "main" layout resource
			SetContentView(Resource.Layout.Main);

			cameraView = FindViewById<ScanbotCameraView>(Resource.Id.camera);
			resultView = FindViewById<ImageView>(Resource.Id.result);

			ContourDetectorFrameHandler contourDetectorFrameHandler = ContourDetectorFrameHandler.Attach(cameraView);

			PolygonView polygonView = FindViewById<PolygonView>(Resource.Id.polygonView);
			contourDetectorFrameHandler.AddResultHandler(polygonView);

			AutoSnappingController.Attach(cameraView, contourDetectorFrameHandler);

			cameraView.AddPictureCallback(this);

			FindViewById(Resource.Id.snap).Click += delegate
			{
				cameraView.TakePicture(false);
			};

			FindViewById(Resource.Id.flash).Click += delegate
			{
				cameraView.UseFlash(!flashEnabled);
				flashEnabled = !flashEnabled;
			};

		}

		protected override void OnResume()
		{
			base.OnResume();
			cameraView.OnResume();
		}

		protected override void OnPause()
		{
			base.OnPause();
			cameraView.OnPause();
		}

		public void OnPictureTaken(byte[] image, int imageOrientation)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.InSampleSize = 8;

			Bitmap bitmap = BitmapFactory.DecodeByteArray(image, 0, image.Length, options);

			resultView.Post(new ResultHandelr(resultView, cameraView, bitmap));
		}

		public class ResultHandelr : Java.Lang.Object, Java.Lang.IRunnable
		{
			Bitmap bitmap;
			ScanbotCameraView cameraView;
			ImageView resultView;

			public ResultHandelr(ImageView resultView, ScanbotCameraView cameraView, Bitmap bitmap)
			{
				this.resultView = resultView;
				this.cameraView = cameraView;
				this.bitmap = bitmap;
			}

			public void Run()
			{
				resultView.SetImageBitmap(bitmap);
				cameraView.StartPreview();
			}
		}
	}
}

