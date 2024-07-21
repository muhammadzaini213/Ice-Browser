package com.ibndev.icebrowser.floatingparts.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class IntersititialAdLoader {
    private InterstitialAd mInterstitialAd;

    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";


    public void loadOpenAppAd(Context context, Activity activity) {
        AdRequest adRequest3 = new AdRequest.Builder().build();
        InterstitialAd.load(context, "ca-app-pub-9202355295382068/9866101635", adRequest3,
//        InterstitialAd.load(context, AD_UNIT_ID, adRequest3,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        showOpenAppAd(activity);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public void showOpenAppAd(Activity activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
        }
    }


}

