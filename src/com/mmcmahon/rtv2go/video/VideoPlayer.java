package com.mmcmahon.rtv2go.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * A WebView which loads html+javasript derived from reddit.tv. Provides an API
 * for java classes to interact with the tv.js script.
 * 
 * Summer 2012
 * 
 * @author Michael McMahon
 */
public class VideoPlayer extends HTML5WebView
{
   // private final String TAG = "#VideoPlayer#";
   // Load modified reddit.tv source
   private static final String RTV2GO_HTML_LOC = "file:///android_asset/rtv-video/index.html";
   private JsInterface jsi;

   public VideoPlayer(Context context)
   {
      super(context);
   }

   public VideoPlayer(Context context, AttributeSet attrs)
   {
      super(context, attrs);
   }

   public VideoPlayer(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);
   }

    /* Not an override! */
    public void onCreate(JsInterface j)
    {
        jsi = j;
        addJavascriptInterface(jsi, "rtv2go");
        loadUrl(RTV2GO_HTML_LOC);
    }

   @Override
   protected void onSizeChanged(int nW, int nH, int oW, int oH)
   {
      super.onSizeChanged(nW, nH, oW, oH);

       /**Race condition:
        *   Javascript loads before call to onSizeChanged:
        *       Javascript requests size: gets default size.
        *       onSizeChanged called: JS gets size.
        *   onSizeChanged called before Javascript loads:
        *       JS gets size: Exception thrown
        *       JSinterface stores size
        *       JS loads and requests size: gets stored size from interface.
        */

      jsi.setSize(nH, nW);
      setSize(nH, nW);
      //Initialize youtube player with screen dimensions
      //ytInit(nW, nH);
   }

   @Override
   public boolean onKeyDown(int keycode, KeyEvent event)
   {
      return false;
   }

    /*
   public void ytInit(int w, int h)
   {
      // Javascript will size embeded videos to fit device's screen
      jsi.setHeight(h);//this.getLayout().getHeight());
      jsi.setWidth(w);//this.getLayout().getWidth());

      if(jsi.isPlayerCreated())
      {
        loadUrl("javascript:createPlayer()");
      }
      requestFocus();
   }
*/
    /*
   @Override
   // Override to prevent scrolling
   protected void onScrollChanged(int ch, int cv, int oh, int ov)
   {
      scrollTo(0, 0);// Don't scroll
      super.onScrollChanged(0, 0, 0, 0);
   }
*/

   @Override
  public boolean onTouchEvent(MotionEvent e)
   {
       loadUrl("javascript:playVideo()");
       return true;
   }

   /******************************
    * app ---> tv.js API *
    ****************************** 
    * The methods below are the apps medium for manipulating the webview's
    * content. This API lets the app call javascript functions in tv.js. Results
    * are sent back through the two interface classes set in vpInit().
    */
   public void playVideo(int i)
   {
      if(inCustomView())
      {
          hideCustomView();
      }
      loadUrl("javascript:loadVideo(" + i + ")");
   }

   public void loadChannel(String channel)
   {
      loadUrl("javascript:loadChannel(\"" + channel + "\")");
   }

   public void setSize(int h, int w)
   {
      loadUrl("javascript:setPlayerSize("+h+","+w+");");
   }
}
