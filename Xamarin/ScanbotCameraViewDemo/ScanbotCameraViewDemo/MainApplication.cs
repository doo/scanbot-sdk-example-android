using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;

namespace ScanbotSDKExample.Xamarine
{
	[Application]
	public class MainApplication : Application
	{
		public MainApplication(IntPtr javaReference, JniHandleOwnership transfer) : base(javaReference, transfer)
		{ }

		public override void OnCreate()
		{
			new Net.Doo.Snap.ScanbotSDKInitializer().Initialize(this);
			base.OnCreate();
		}
	}
}
