package com.mmcmahon.rtv2go.video;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;
import com.mmcmahon.rtv2go.thumbnails.ThumbnailsAdapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.JavascriptInterface;

/**
 *  An interface for this app to communicate with javascript. Handles non-channel related tasks. 
 *  
 *  Summer 2012
 *  Michael McMahon
 */
public class JsInterface
{
   private static final String TAG = "#JsInterface#";
   final private ACTV_VideoPlayer actvCon;
   private int width = 0, height = 0;
   final private Toast tShow;
   final private TextView tMsg;
   private boolean initLoad = true;
   private boolean playerCreated;
   final private ThumbnailsAdapter tnsAdapter;
   final private String initChannel;
   //The maximum number of videos to be listed
   final static public int LIST_LIMIT = 100;

    public JsInterface(Context c, ThumbnailsAdapter ta, String chan1)
   {
      actvCon = (ACTV_VideoPlayer) c;
      tnsAdapter = ta;
      initChannel = chan1;
       tShow = new Toast(actvCon);
       tShow.setGravity(Gravity.CENTER, 0, 0);
       tMsg = new TextView(actvCon);
       tMsg.setBackgroundResource(R.color.IFACE_BG);
       tMsg.setTextColor(Color.WHITE);
       tMsg.setTypeface(Typeface.DEFAULT_BOLD);
   }

   @JavascriptInterface
   public String getInitialChannel()
   {
       return initChannel;
   }

   public void setSize(int h, int w)
   {
      height = h;
      width = w;
   }

   @JavascriptInterface
   public int getHeight()
   {
      return height;
   }

   @JavascriptInterface
   public int getWidth()
   {
      return width;
   }

   @JavascriptInterface
   public int getListLimit()
   {
       return LIST_LIMIT;
   }

   @JavascriptInterface
   public void androidLog(String msg)
   {
      Log.e(TAG, "SEENIT JS CONSOLE: " + msg);
   }

    /** This method invoked after JS has called clearThumbs, and made one or
     * more calls to addThumbnail
     */
    @JavascriptInterface
    public void thumbsLoaded()
   {
       actvCon.thumbsLoaded();
       //Automatically cue first video on initial channel load
       if(initLoad)
       {
           initLoad = false;
           actvCon.playVideo(0, tnsAdapter.getThumbTitle(0));
       }
   }

    @JavascriptInterface
   public void playerReady()
   {
        playerCreated = true;
        actvCon.playerLoaded();
   }

   public boolean isPlayerCreated()
   {
       return playerCreated;
   }

    @JavascriptInterface
   public void clearThumbnails()
   {
       tnsAdapter.clearAllThumbs();
   }

    @JavascriptInterface
   public void addThumbnail(String url, String title)
   {
       tnsAdapter.addThumbnail(url, title);
   }

   /**
    * Called when tv.js has found no videos while attempting to load a channel.
    */
   @JavascriptInterface
   public void promptNoVideos(String chan)
   {
      actvCon.promptNoVideos(chan);
   }

    //Quick toast
    @JavascriptInterface
    public void toastMsgS(final String msg)
    {
        actvCon.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tShow.cancel();
                tMsg.setText(msg);
                tShow.setDuration(Toast.LENGTH_SHORT);
                tShow.setView(tMsg);
                tShow.show();
            }
        });
    }

    //Long toast
    @JavascriptInterface
    public void toastMsgL(final String msg)
    {
        actvCon.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tShow.cancel();
                tMsg.setText(msg);
                tShow.setDuration(Toast.LENGTH_LONG);
                tShow.setView(tMsg);
                tShow.show();
            }
        });
    }
}
