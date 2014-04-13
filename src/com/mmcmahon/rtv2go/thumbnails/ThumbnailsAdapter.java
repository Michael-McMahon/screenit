package com.mmcmahon.rtv2go.thumbnails;

import java.util.Vector;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Manages a gallery of thumbnail video previews for the current channel. 
 * Thumbsnails are stored in a LRU cache, which can be configured to use ++/-- 
 * memory.
 *   
 * Summer 2012
 * @author Michael McMahon
 */
public class ThumbnailsAdapter extends BaseAdapter
{
   public static int THUMB_HEIGHT = 150, THUMB_WIDTH = 250;
   public final static String SHOW_LOADING = "showloadingkey";
   //private static final String TAG = null;
   private final ACTV_VideoPlayer actvCon;
   private final BitmapCache bitmapCache;
   private Vector<ThumbnailContent> thumbData = new Vector<ThumbnailContent>();

   public ThumbnailsAdapter(ACTV_VideoPlayer c)
   {
      actvCon = c;
      configThumbSize();

      // 2nd param is the portion of memory to use for cache
      bitmapCache = BitmapCache.createBitmapCache(c, 8);
   }

   public void clearAllThumbs()
   {
       thumbData.removeAllElements();
       //bitmapCache.clearRequests();
   }

   /**
    * Determine the screen resolution, and set an appropriate thumbnail size.
    */
   @TargetApi(13)
   private void configThumbSize()
   {
      Display disp = actvCon.getWindowManager().getDefaultDisplay();
      Point res = new Point();
      //Get the screen's dimensions
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
      {
         disp.getSize(res);//API 13 call
      }
      else
      {
         res.x = disp.getWidth();
         res.y = disp.getHeight();
      }
      //Set thumbnail size
      THUMB_WIDTH = res.x / 3;
      THUMB_HEIGHT = res.y /3;
   }

   public int getCount()
   {
      return thumbData.size();
   }

   /**
    * Get the thumbnail content for the i'th postion in the gallery. Will return
    * data for an advertisement, if the position is an ad space.
    */
   public Object getItem(int i)
   {
      int thumbSize = thumbData.size();

      if (thumbSize == 0 || i >= thumbSize)
      {
          //Returns loading placeholder
         return new ThumbnailContent(SHOW_LOADING, "loading...");
      }

      return thumbData.get(i);
   }

   public long getItemId(int id)
   {
      return id;
   }

   public View getView(final int pos, View v, ViewGroup gal)
   {
      ThumbnailContent content;
      final String imgUrl;
      final UrlImageView imageView;// = (UrlImageView) v;

      // Create a new view to hold a thumbnail
      //if (imageView == null)      {
         imageView = new UrlImageView(actvCon);
         imageView.setLayoutParams(// Use gallery layoutparams
               new Gallery.LayoutParams(THUMB_WIDTH, THUMB_HEIGHT));
         imageView.setBackgroundResource(R.drawable.thumb_bg);
         imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         imageView.setAlpha(127);
      //}

      imgUrl = thumbData.get(pos).getUrl();
       //Log.e("getView",pos+":  next "+imgUrl+",  prev "+imageView.getUrl() );
      imageView.setUrl(imgUrl);

      if (imgUrl != null)
      {
         actvCon.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 bitmapCache.getBitmap(imageView, imgUrl, imgUrl);// Use url as key
             }
         });
      }
      return imageView;
   }

   public void addThumbnail(final String url, final String title)
   {
      // Notify UI Thread of new thumbnail
      ((Activity) actvCon).runOnUiThread(new Runnable()
      {
         public void run()
         {
            thumbData.add(new ThumbnailContent(url, title));
            notifyDataSetChanged();
         }
      });
   }

   public String getThumbTitle(int pos)
   {
       return ((ThumbnailContent)getItem(pos)).getTitle();
   }
}
