package com.mmcmahon.rtv2go.dialogs;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;
import com.mmcmahon.rtv2go.VideoInterface;
import com.mmcmahon.rtv2go.channels.ChannelsAdapter;
import com.mmcmahon.rtv2go.thumbnails.ThumbnailsAdapter;
import com.mmcmahon.rtv2go.video.VideoPlayer;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

/**
 * A dialog which allows the interface to overlay the html video content.
 * 
 * Summer 2012
 * @author Michael McMahon
 */
public class InterfaceDialog extends Dialog
{
   final private VideoInterface lsiface;
   final private ACTV_VideoPlayer actvCon;
   final private View contextBar;

   public InterfaceDialog(Context context, VideoPlayer vp, ChannelsAdapter ca, ThumbnailsAdapter ta)
   {
      super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
      actvCon = (ACTV_VideoPlayer) context;
      
      setContentView(R.layout.lsiface2);
      contextBar = findViewById(R.id.contextBar);
      lsiface = (VideoInterface) findViewById(R.id.lsifaceClickables);
      lsiface.setupIface(vp, ca, ta);
   }

   @Override
   public void onBackPressed()
   {
       actvCon.openMenu();
       /* 4/2014 back button now brings up menu
       * Dedicated Menu buttons are a thing of the past!
        * /
        /*
      if (lsiface != null)
      {
         lsiface.backPressed();
      }*/
   }

   @Override
   public boolean onKeyDown(int keycode, KeyEvent event)
   {
      switch (keycode)
      {
      case KeyEvent.KEYCODE_MENU:
         actvCon.openMenu();
         return true;
      case KeyEvent.KEYCODE_BACK:
         onBackPressed();
         return true;
      default:
         return false;
      }
   }

   public VideoInterface getInterfaceView()
   {
      return lsiface;
   }
   public View getContextBar() {return contextBar;}
}
