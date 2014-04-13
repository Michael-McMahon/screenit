package com.mmcmahon.rtv2go.video;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * This extended webview can run html5 videos.
 * 
 * Source obtained here:
 * https://code.google.com/p/html5webview/
 */
public class HTML5WebView extends WebView {
	
	private Context 							mContext;
	private MyWebChromeClient					mWebChromeClient;
	private View								mCustomView;
	private FrameLayout							mCustomViewContainer;
	private WebChromeClient.CustomViewCallback 	mCustomViewCallback;
	private FrameLayout							mLayout;

	    
	private void init(Context context) {
        FrameLayout mContentView;
        FrameLayout mBrowserFrameLayout;

		mContext = context;		
		Activity a = (Activity) mContext;

		mLayout = new FrameLayout(context);
        mLayout.setLayoutParams(COVER_SCREEN_PARAMS);

		mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(a).inflate(R.layout.html5_custom_screen, null);
		mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
		mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);

		mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

		mWebChromeClient = new MyWebChromeClient();
	    setWebChromeClient(mWebChromeClient);

	    setWebViewClient(new MyWebViewClient());

	    // Configure the webview
	    WebSettings s = getSettings();
	    s.setJavaScriptEnabled(true);
        //s.setUseWideViewPort(true);
        //s.setLoadWithOverviewMode(true);

	    // enable Web Storage: localStorage, sessionStorage
	    s.setDomStorageEnabled(true);
	    
	    mContentView.addView(this);
	}

	public HTML5WebView(Context context) {
		super(context);
		init(context);
	}

	public HTML5WebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public FrameLayout getLayout() {
		return mLayout;
	}
	
    public boolean inCustomView() {
		return (mCustomView != null);
	}
    
    public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if ((mCustomView == null) && canGoBack()){
    			goBack();
    			return true;
    		}
    	}
    	return super.onKeyDown(keyCode, event);
    }

    private class MyWebChromeClient extends WebChromeClient {
		private Bitmap 		mDefaultVideoPoster;
		private View 		mVideoProgressView;

        @Override
        public void onProgressChanged (WebView view, int newProgress)
        {
            ((ACTV_VideoPlayer)mContext).setLoadProgress(newProgress);
        }

    	@Override
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
		{
	        HTML5WebView.this.setVisibility(View.GONE);
	        
	        // if a view already exists then immediately terminate the new one

	         if (mCustomView != null) {
	            callback.onCustomViewHidden();
	            return;
	        }
	       /*
            //If view already exists, hide it and show the new one
            if(mCustomView != null)
            {
                mCustomViewCallback.onCustomViewHidden();
            }
*/
	        mCustomViewContainer.addView(view);
	        mCustomView = view;
	        mCustomViewCallback = callback;
	        mCustomViewContainer.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onHideCustomView() {
			if (mCustomView == null)
				return;	       
			
			// Hide the custom view.
			mCustomView.setVisibility(View.GONE);
			
			// Remove the custom view from its container.
			mCustomViewContainer.removeView(mCustomView);
			mCustomView = null;
			mCustomViewContainer.setVisibility(View.GONE);
			mCustomViewCallback.onCustomViewHidden();
			
			HTML5WebView.this.setVisibility(View.VISIBLE);
		}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
			if (mDefaultVideoPoster == null) {
				mDefaultVideoPoster = BitmapFactory.decodeResource(
						getResources(), R.drawable.default_video_poster);
		    }
			return mDefaultVideoPoster;
		}
		
		@Override
		public View getVideoLoadingProgressView() {
	        //Log.e(TAG, "Video Progress request");
            if (mVideoProgressView == null) {
	            LayoutInflater inflater = LayoutInflater.from(mContext);
	            mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
	        }
	        return mVideoProgressView; 
		}
    }
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	// don't override URL so that stuff within iframe can work properly
	        // view.loadUrl(url);
	        return false;
	    }
	}
	
	static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
        new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
}