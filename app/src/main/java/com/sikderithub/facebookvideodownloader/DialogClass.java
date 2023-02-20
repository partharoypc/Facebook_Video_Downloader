package com.sikderithub.facebookvideodownloader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class DialogClass extends Dialog implements View.OnClickListener {

    public Activity activity;
    public Button exit, cancel;

    public DialogClass(@NonNull Context context) {
        super(context);

        this.activity = (Activity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.exit_dialog);

        exit = findViewById(R.id.btn_exit);
        cancel = findViewById(R.id.btn_cancel);

        exit.setOnClickListener(this);
        cancel.setOnClickListener(this);

        new Thread(){
            @Override
            public void run() {
                MobileAds.initialize(activity);
                final AdLoader adLoader = new AdLoader.Builder(activity, activity.getString(R.string.admob_native_ad_id))
                        .forNativeAd(nativeAd -> {
                            NativeAdView nativeAdView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad_layout, null);
                            mapUnifiedNativeAdToLayout(nativeAd, nativeAdView);

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(()->{
                                RelativeLayout nativeAdLayout = findViewById(R.id.ad_native);
                                nativeAdLayout.removeAllViews();
                                nativeAdLayout.addView(nativeAdView);


                            });

                        }).build();

                new Handler(Looper.getMainLooper()).post(()->{
                    adLoader.loadAd(new AdRequest.Builder().build());
                });


            }
        }.start();

    }

    public void mapUnifiedNativeAdToLayout(NativeAd adFromGoogle, NativeAdView myAdView) {
        MediaView mediaView = myAdView.findViewById(R.id.ad_media);
        myAdView.setMediaView(mediaView);

        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_title));
        myAdView.setBodyView(myAdView.findViewById(R.id.ad_details));
        myAdView.setCallToActionView(myAdView.findViewById(R.id.btn_action));
        myAdView.setIconView(myAdView.findViewById(R.id.ad_icon));

        ((TextView) myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());

        if (adFromGoogle.getBody() == null) {
            myAdView.getBodyView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getBodyView()).setText(adFromGoogle.getBody());
        }

        if (adFromGoogle.getCallToAction() == null) {
            myAdView.getCallToActionView().setVisibility(View.GONE);
        } else {
            ((Button) myAdView.getCallToActionView()).setText(adFromGoogle.getCallToAction());
        }

        if (adFromGoogle.getIcon() == null) {
            myAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) myAdView.getIconView()).setImageDrawable(adFromGoogle.getIcon().getDrawable());
        }
        myAdView.setNativeAd(adFromGoogle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit:
                activity.finish();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }
}
