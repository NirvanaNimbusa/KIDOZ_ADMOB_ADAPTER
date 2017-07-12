package com.kidoz.mediation.admob.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.kidoz.sdk.api.interfaces.SDKEventListener;
import com.kidoz.sdk.api.ui_views.kidoz_banner.KidozBannerListener;
import com.kidoz.sdk.api.ui_views.new_kidoz_banner.BANNER_POSITION;
import com.kidoz.sdk.api.ui_views.new_kidoz_banner.KidozBannerView;

/**
 * Created by orikam on 07/06/2017.
 */

public class KidozAdMobMediationBannerAdapter implements CustomEventBanner
{
    private static final String TAG = "KidozAdMobMediationBannerAdapter";
    private static final BANNER_POSITION DEFAULT_BANNER_POSITION = BANNER_POSITION.BOTTOM;

    private CustomEventBannerListener mCustomEventBannerListener;
    private KidozManager mKidozManager;

    public KidozAdMobMediationBannerAdapter()
    {
        mKidozManager = new KidozManager();
    }

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle)
    {
        mCustomEventBannerListener = customEventBannerListener;

        //Kidoz requires Activity context to run.
        if (!(context instanceof Activity)){
            mCustomEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            Log.d(TAG, "Kidoz | requestBannerAd with non Activity context");
            return;
        }

        Log.d("ahmed", "KidozBannerAdapter | requestBannerAd called");

        //Kidoz must be initialized before an ad can be requested
        if (!mKidozManager.getIsKidozInitialized())
        {
            Log.d("ahmed", "KidozBannerAdapter | kidoz not init, initializing first");
            initKidoz((Activity) context);
        } else {
            Log.d("ahmed", "KidozBannerAdapter | kidoz already init");
            continueRequestBannerAd((Activity) context);
        }
    }

    private void initKidoz(final Activity activity)
    {
        mKidozManager.initKidozSDK(activity, new SDKEventListener()
        {
            @Override
            public void onInitSuccess()
            {
                continueRequestBannerAd(activity);
            }

            @Override
            public void onInitError(String error)
            {
                mCustomEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                Log.d(TAG, "Kidoz | onInitError: " + error);
            }
        });
    }

    private void continueRequestBannerAd(Activity activity)
    {
        Log.d("ahmed", "KidozBannerAdapter | kidoz continueRequestBannerAd");
        if (mKidozManager.getBanner() == null)
        {
            Log.d("ahmed", "kidozBannerAdapter | banner not set up, calling view load.");
            setupKidozBanner(activity);
        }

        Log.d("ahmed", "KidozBannerAdapter | continueRequestBannerAd | calling load()");
        mKidozManager.getBanner().setLayoutWithoutShowing();
        mKidozManager.getBanner().load();
    }

    private void setupKidozBanner(final Activity activity)
    {
        Log.d("ahmed", "kidozBannerAdapter | kidozBannerView == null, calling view creation. START");
        mKidozManager.setupKidozBanner(activity, DEFAULT_BANNER_POSITION, new KidozBannerListener()
        {
            @Override
            public void onBannerReady()
            {
                //ask banner not to insert itself to view hierarchy, admob will do that in onAdLoaded(...)
                final KidozBannerView kbv = mKidozManager.getBanner();
                kbv.setBackgroundColor(Color.TRANSPARENT);

                kbv.show();
            }

            @Override
            public void onBannerViewAdded()
            {
                mCustomEventBannerListener.onAdLoaded(mKidozManager.getBanner());
                mCustomEventBannerListener.onAdOpened();
            }

            @Override
            public void onBannerError(String errorMsg)
            {
                Log.e(TAG, "onBannerError: " + errorMsg);
                mCustomEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
            }

            @Override
            public void onBannerClose()
            {
                mCustomEventBannerListener.onAdClosed();
            }
        });
        Log.d("ahmed", "kidozBannerAdapter | kidozBannerView == null, calling view creation. END");
    }

    @Override
    public void onDestroy()
    {
    }

    @Override
    public void onPause()
    {
    }

    @Override
    public void onResume()
    {
    }

}