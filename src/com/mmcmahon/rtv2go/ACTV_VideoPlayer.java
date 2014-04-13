package com.mmcmahon.rtv2go;

import com.mmcmahon.rtv2go.channels.ChannelsAdapter;
import com.mmcmahon.rtv2go.dialogs.InterfaceDialog;
import com.mmcmahon.rtv2go.dialogs.MenuDialog;
import com.mmcmahon.rtv2go.dialogs.RemoveDialog;
import com.mmcmahon.rtv2go.thumbnails.ThumbnailsAdapter;
import com.mmcmahon.rtv2go.video.VideoPlayer;
import com.mmcmahon.rtv2go.video.JsInterface;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Activity in this app which creates and configures all views.
 * This class serves as a central hub for communication between the major view
 * centers. These view centers are: the video player webview, the touch screen
 * interface overlay, and the MENU button screen. Summer 2012
 * 
 * @author Michael McMahon
 */
public class ACTV_VideoPlayer extends Activity
{
   //Shared Preferences keys
   private static final String SP_LAST_CHAN = "LAST";
   private static final String SP_DEF_CHAN = "DEF_CHAN";
   private static final String SP_DEF_CHAN_NAMES = "DEF_NAMES";
   private static final long HIDE_DELAY = 10000;//How long to show context bar for
   private static final String DLIM = "/";

   private final String TAG = "#ACTV_VideoPlayer#";

   //Extended WebView plays videos
   private VideoPlayer videoPlayer;
   //ViewGroup which manages the interface
   private VideoInterface videoIface;
   //Video player interface is presented in a transparent dialog
   private InterfaceDialog ifaceDialog;
   private SharedPreferences spChannels;
   //Manages the list of subreddit channels
   private ChannelsAdapter chanAdapter;
   private MenuDialog menuPopup;
   //Context bar will act as a basic title bar with just text
   private View contextBar;
   private TextView vidContext, chanContext;
   private String loadedChan;
   private Timer hideTimer = new Timer();

   public void onCreate(Bundle sis)
   {
      super.onCreate(sis);

       menuPopup = new MenuDialog(this);

      //Create channel related globals
       chanAdapter = new ChannelsAdapter(this);
       spChannels = getSharedPreferences(SP_DEF_CHAN, Context.MODE_PRIVATE);
       loadedChan = spChannels.getString(SP_LAST_CHAN, "All");
       loadChannelsAdapter();

       //Create thumbnails adapater
       ThumbnailsAdapter thumbAdapter = new ThumbnailsAdapter(this);
       //Create video player webview with javascript interface class
       JsInterface jsi = new JsInterface(this, thumbAdapter, loadedChan);
       videoPlayer = new VideoPlayer(this);
       videoPlayer.onCreate(jsi);

       //Main content is videoplayer
      setContentView(videoPlayer.getLayout());
       //Interface view will overlay video
      createInterface(videoPlayer, chanAdapter, thumbAdapter);
   }

    @Override
    protected void onResume()
    {
        super.onResume();
        ifaceDialog.show();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //ifaceDialog.hide();
        ifaceDialog.dismiss();
        menuPopup.dismiss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    private void loadChannelsAdapter()
    {
        int i, len;
        String[] chans;
        int initChan = -1;

        //Gets previous stored channels list
        String chanNames = spChannels.getString(SP_DEF_CHAN_NAMES, null);
        if (chanNames == null)
        {
            chanNames = createDefaultChannels();
        }

        //All Channels in one forwardSlash('/') delimited string
        //substring(1) skips first /
        chans = chanNames.substring(1).split("/", 0);

        for(i = 0, len = chans.length;
            i < len; i++)
        {
            chanAdapter.addChannel(chans[i]);
            //Set initally selected view
            if(chans[i].equals(loadedChan))
            {
                initChan = i;
            }
        }

        if(initChan != -1)
        {
            chanAdapter.setSelected(initChan);
        }
        else
        {
            chanAdapter.addChannel(loadedChan);
            chanAdapter.setSelected(len);
        }
        /*
        int len;
        len = list.length;
        while(len > 0)
        {
            len--;
            chanAdapter.addChannel(list[len]);
        }
        */
    }

    // Create new default channels list
    private String createDefaultChannels()
    {
        //Gets a prepackaged list of channels
        String chans[] = getResources().getStringArray(
                R.array.packaged_channels);
        int len = chans.length;

        //Save as the default channel set
        while(len > 0)
        {
            len--;
            spSaveChannel(chans[len]);
        }
        spChannels.edit().putString(SP_LAST_CHAN, chans[0]).commit();
        //Return the token delimited string list
        return spChannels.getString(SP_DEF_CHAN_NAMES, null);
    }

   private void createInterface(VideoPlayer vp, ChannelsAdapter ca, ThumbnailsAdapter ta)
   {
      ifaceDialog = new InterfaceDialog(this, vp, ca, ta);
      videoIface = ifaceDialog.getInterfaceView();
      contextBar = ifaceDialog.getContextBar();
      vidContext = (TextView)contextBar.findViewById(R.id.videoContext);
      chanContext = (TextView)contextBar.findViewById(R.id.channelContext);
   }

   /* Loads the channel name provided in the video player */
   public void loadChannel(String channel)
   {
      //Title bar will display channel name after load
      setLoadedChan("loading");
      loadedChan = channel;
      spChannels.edit().putString(SP_LAST_CHAN, channel).commit();
      //UI will clear out thumbs
      videoIface.clearThumbnails();
      //Webview starts ajax request
      videoPlayer.loadChannel(channel);
   }

   public void playVideo(int pos, String title)
   {
      videoPlayer.playVideo(pos);
      setLoadedVid(title);
   }

   /* Add a new channel to top of list and load it */
   public final void pushChannel(final String ch)
   {
      runOnUiThread(new Runnable() {
          @Override
          public void run() {
                  //pushChannel will put channel at top of list, then select and load it.
                  chanAdapter.pushChannel(ch);
              }
      });
   }

  /* Add a new channel to the shared preferences default list */
   public void spSaveChannel(String name)
   {
      // Get the current list
      String curr = spChannels.getString(SP_DEF_CHAN_NAMES, DLIM);
      // Add '/' delimited entry to front of list
      String dName = DLIM + name;
      if (!curr.contains(dName + DLIM))
      {
         spChannels.edit().putString(SP_DEF_CHAN_NAMES, dName + curr).commit();
      }
   }

   /* Remove a channel from the shared preferences default list */
   public void spDeleteChannel(String name)
   {
      String curr = spChannels.getString(SP_DEF_CHAN_NAMES, DLIM);
      curr = curr.replace(DLIM + name + DLIM, DLIM);
      spChannels.edit().putString(SP_DEF_CHAN_NAMES, curr).commit();
   }

   public void hideSoftKeyboard(final IBinder winToken)
   {
      /*
      Configuration config = getResources().getConfiguration();
      boolean wasShowing = // True if the softkeyboard was showing
      (config.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO)
            && (config.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_NO); TODO: ASSUMED that config should have UNDEFINED on devices which don't
       * have keyboards or which can't hide keyboards.*/
      final InputMethodManager imm =
           (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      runOnUiThread(new Runnable()
      {
         public void run()
         {
            imm.hideSoftInputFromWindow(winToken, 0);
         }
      });
   }

   public void promptNoVideos(final String chan)
   {
      runOnUiThread(new Runnable()
      {
         public void run()
         {
            videoIface.noVideosFound(chan);
         }
      });
      setLoadedChan(loadedChan);
   }

   /* No fading animation, just set visibility */
   public void showRemove(final boolean show, final RemoveDialog rd)
   {
      runOnUiThread(new Runnable()
      {
         public void run()
         {
            if (show)
            {
               ifaceDialog.hide();
               rd.show();
            } else
            {
               rd.hide();
               ifaceDialog.show();
            }
         }
      });
   }

   public void setLoadProgress(int p)
   {
      videoIface.setLoadProgress(p);
   }

   /**Called once webview is ready to play videos*/
   public void playerLoaded()
   {
        //Get last loaded channel from shared preferences
        pushChannel(spChannels.getString(SP_LAST_CHAN, "All"));
   }

   public void thumbsLoaded()
   {
      //Clears "loading..." from context bar
      setLoadedChan(loadedChan);
      runOnUiThread(new Runnable(){
          public void run()
          {
            videoIface.thumbsLoaded();
          }
      });
   }

   private void setLoadedVid(final String vidTitle)
   {
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               vidContext.setText(vidTitle);
           }
       });
   }

   private void setLoadedChan(final String chanTitle)
   {
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               chanContext.setText(chanTitle);
            }
        });
   }

   public void showContextBar()
   {
       contextBar.setVisibility(View.VISIBLE);
       startHideTimer();
   }
    public void hideContextBar()
    {
        // Remove any pending animations
        hideTimer.cancel();
        contextBar.setVisibility(View.GONE);
    }


    /* Called when the hide interface timer expires */
    class HideTask extends TimerTask
    {
        Activity act;
        View vContextBar;

        public HideTask(Activity a, View cb)
        {
            act = a;
            vContextBar = cb;
        }

        public void run()
        {

            act.runOnUiThread(
                    new Runnable() {
                        public void run() {
                            vContextBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void startHideTimer()
    {
        // Remove any pending animations
        hideTimer.cancel();
        //new Timer and TimerTask avoids illegal state
        hideTimer = new Timer();
        hideTimer.schedule(new HideTask(this, contextBar), HIDE_DELAY);
    }

    public void closeMenu()
    {
        if (menuPopup != null)
        {
            menuPopup.hide();
        }
        if(ifaceDialog != null)
        {
            ifaceDialog.show();
        }
    }

    public void openMenu()
    {
        if (menuPopup != null)
        {
            menuPopup.show();
        }
        if(ifaceDialog != null)
        {
            ifaceDialog.hide();
        }
    }

}
