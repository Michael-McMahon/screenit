package com.mmcmahon.rtv2go;

import android.content.Context;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mmcmahon.rtv2go.channels.ChannelsAdapter;
import com.mmcmahon.rtv2go.channels.HorizontalListView;
import com.mmcmahon.rtv2go.thumbnails.ThumbnailContent;
import com.mmcmahon.rtv2go.thumbnails.ThumbnailsAdapter;
import com.mmcmahon.rtv2go.video.VideoPlayer;
import com.mmcmahon.rtv2go.thumbnails.ThumbListView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The root view for just about every interactive element in the app.
 * Should appear as an overlay on top of the html video content. This view 
 * houses interfaces for interacting with the underlaying video player, such as
 *  subreddit selection, thumbnail browsing, vote casting, and much more...
 *  
 *  Summer 2012
 * @author Michael McMahon
 */
public class VideoInterface extends RelativeLayout
{
   //private final String TAG = "#VideoInterface#";
   private final int OUT_ANIM_MS = 500;// Duration of fade out when hiding interface
   private final int IN_ANIM_MS = 500;// Duration of fade out when showing interface
   private final int HIDE_DELAY = 5000;// Milliseconds to show interface
   private ACTV_VideoPlayer actvCon;
   private ImageButton searchBtn;
   private Gallery thumbnails;
   private VideoPlayer player;

  // private ChannelsAdapter chanAdapter;
   private HorizontalListView channelList;
   private EditText subredditSearch;
   private boolean hidden = false;
   private Timer hideTimer = new Timer();
   private ThumbnailsAdapter thumbAdapter;
   private TextView titleDisplay;
   private OnItemSelectedListener thumbSelected;
   
   public VideoInterface(Context context)
   {
      super(context);
   }

   public VideoInterface(Context context, AttributeSet attrs)
   {
      super(context, attrs);
   }

   public VideoInterface(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);
   }
   
   /* UI reactions to next or previous play button press. */
   private void onNextOrPrev(int pos)
   {
       /*
      AlphaAnimation fadeAnim = new AlphaAnimation((float) 1.0, (float) 0.3);
      fadeAnim.setDuration(ANIM_MS);
      fadeAnim.setRepeatMode(Animation.REVERSE);
      fadeAnim.setRepeatCount(1);
      iBtn.startAnimation(fadeAnim);*/
      thumbnails.onKeyDown(pos, new KeyEvent(KeyEvent.ACTION_UP, pos));
   }

   private void setupListeners()
   {
      //WebView Video Area (No interface)
      View videoArea = this.findViewById(R.id.lsifaceVideoArea);
      videoArea.setOnTouchListener(new OnTouchListener() {
          @Override
          public boolean onTouch(View view, MotionEvent motionEvent) {
              player.onTouchEvent(motionEvent);
              return false;
          }
      });

      // Next Button
      ImageButton nextBtn = (ImageButton) this.findViewById(R.id.lsifaceNext);
      nextBtn.setOnClickListener(new OnClickListener()
      {
         public void onClick(View v)
         {
            int pos = thumbnails.getSelectedItemPosition();//12/29/2013 actvCon.getPlayPos();
            int count = thumbAdapter.getCount();

            if (++pos < count)// Do nothing if positioned at last video
            {
               onNextOrPrev(KeyEvent.KEYCODE_DPAD_RIGHT);
            } else
            // At last video
            {
               ((Vibrator) actvCon.getSystemService(Context.VIBRATOR_SERVICE))
                     .vibrate(250);
               //thumbnails.setSelection(count - 1);
            }
         }
      });
      // Previous Button
      ImageButton prevBtn = (ImageButton) this.findViewById(R.id.lsifacePrev);
      prevBtn.setOnClickListener(new OnClickListener()
      {
         public void onClick(View v)
         {
            int pos = thumbnails.getSelectedItemPosition();//12/29/2013actvCon.getPlayPos();
            if (pos > 0)
            {
               onNextOrPrev(KeyEvent.KEYCODE_DPAD_LEFT);
            } else//Positioned at first thumbnail
            {
               ((Vibrator) actvCon.getSystemService(Context.VIBRATOR_SERVICE))
                     .vibrate(250);
            }
         }
      });
   }

   /**
    * This method is to be called once the video player view has been created.
    * This interface will hook up to the provided VideoPlayer view.
    */
   public void setupIface(VideoPlayer vp, ChannelsAdapter ca, ThumbnailsAdapter ta)
   {
      //Interface will pass touch events to videoplayer
      player = vp;
      actvCon = (ACTV_VideoPlayer) vp.getContext();

      //Setup channel search entry and button listeners
      subredditSearch = ca.getSearchBox();
      setupSearch();

      //Setup channels list
      channelList = (HorizontalListView) findViewById(R.id.lsifaceChannels);
      channelList.setAdapter(ca);

      //Setup thumbnails gallery
      thumbAdapter = ta;
      setupThumbnails();

      //Setup touch event listeners
      setupListeners();
   }

   public boolean onInterceptTouchEvent(MotionEvent ev)
   {
       //Don't react to touch, unless interface is showing
       if(hidden)
       {
           showInterface();
           return true;
       }
       //Restart the hide timer when interface is touched
       startHideTimer();
       return false;
   }

   private class OnSearchClick implements OnClickListener
   {
      public void onClick(View v)
      {
         // Show text entry if not already visible
         if (subredditSearch.getText().length() == 0)
         {
            channelList.scrollTo(0);
         } else// Attempt to find a subreddit with the name entered
         {
            addSearchChannel();
         }
      }
   }

   private void setupSearch()
   {
      searchBtn = (ImageButton) findViewById(R.id.sboxSearchBtn);
      searchBtn.setOnClickListener(new OnSearchClick());

       subredditSearch.setOnEditorActionListener( new TextView.OnEditorActionListener() {
           private final int ADD_ACTION =
                   actvCon.getResources().getInteger(R.integer.ADD_ACTION);
           @Override
           public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
               if(action == ADD_ACTION)
               {
                   return VideoInterface.this.handleKeyPress(KeyEvent.KEYCODE_ENTER);
               }
               return false;
           }
       });

      subredditSearch.setOnKeyListener(new OnKeyListener() {
          public boolean onKey(View v, int keyCode, KeyEvent event) {
              return VideoInterface.this.handleKeyPress(keyCode);
          }
      });

      subredditSearch.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

          }

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

          }

          @Override
          public void afterTextChanged(Editable editable) {
                //Show interface while typing
              if (VideoInterface.this.hidden) {
                  VideoInterface.this.showInterface();
              }
          }
      });
   }

    private boolean handleKeyPress(int keyCode)
    {
        boolean handled = false;

        switch(keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                break;
            case KeyEvent.KEYCODE_ENTER:
                hideSoftKeyboard();
                searchBtn.performClick();
                handled = true;
            default://Show interface while typing
                if (VideoInterface.this.hidden) {
                    VideoInterface.this.showInterface();
                }
        }
        return handled;
    }
   /* Read any input in search field and add it as a channel.*/
   private void addSearchChannel()
   {
      String search = subredditSearch.getText().toString();
      Animation spin = AnimationUtils.loadAnimation(actvCon,
            R.anim.clockwise_spin);
      // Show animation on search icon
      searchBtn.startAnimation(spin);

      // Load the new channel and add it to the list
      if (search.length() > 0)
      {
         //scrollChannelsTop();// Scroll list to show new channel entry
         String newChannel =
                 subredditSearch.getText().toString()
                         .replaceAll("[^a-zA-Z0-9_]*", "");
         //Reddit API does not respond to requests containing more than 21 characters
         if(newChannel.length() > 21)
         {
             newChannel = newChannel.substring(0, 21);
         }
         actvCon.pushChannel(newChannel);
         subredditSearch.setText("");
      }
   }

   /**
    * Called when the BACK button is pressed.
    */
   public void backPressed()
   {
      if(hidden)
      {
          showInterface();
      }
      else
      {
          hideInterface();
      }
   }
/*
   private void scrollChannelsTop()
   {
      channelList.scrollTo(ChannelsAdapter.SEARCH_WIDTH);
   }
*/
   /**Notify this interface that thumbnail images have loaded*/
   public void thumbsLoaded()
   {
      if(hidden)
      {
          showInterface();
      }

      //Make sure first thumbnail is now selected
      thumbSelected
            .onItemSelected(thumbnails,
                  thumbnails.getChildAt(0),
                    0, thumbAdapter.getItemId(0));
   }

   private class OnThumbSelected implements OnItemSelectedListener
   {
      private ImageView selectedImgView = null;
      final android.widget.Gallery.LayoutParams bigLps;
      android.widget.Gallery.LayoutParams oldLps;

      public OnThumbSelected()
      {
         int tw = ThumbnailsAdapter.THUMB_WIDTH;
         int th = ThumbnailsAdapter.THUMB_HEIGHT;
         bigLps = new Gallery.LayoutParams(tw + (tw/4), th + (th/4));
      }

      public void onItemSelected(AdapterView<?> av, View v, int pos, long id)
      {
         // Update the title display
         ThumbnailContent tc = (ThumbnailContent) thumbAdapter.getItem(pos);

         if (titleDisplay != null)
         {
            titleDisplay.setText(tc.getTitle());
         }

         if (selectedImgView != null)// If a view has previously been
                                     // highlighted...
         {
            selectedImgView.setAlpha(127);// Return previous selection to
                                          // non-highlighted state
            selectedImgView.setLayoutParams(oldLps);
         }
         if (v != null)
         {
            // Update to latest selection
            selectedImgView = (ImageView) v;
            oldLps = (android.widget.Gallery.LayoutParams) selectedImgView
                  .getLayoutParams();
            // Highlight new selection
            selectedImgView.setAlpha(255);
            selectedImgView.setLayoutParams(bigLps);
            //selectedImgView.invalidate();
         } else
         {
            selectedImgView = null;
         }
      }

      public void onNothingSelected(AdapterView<?> av)
      {// Set title to first video
         onItemSelected(av, null, 0, 0);
      }
   }

   /**
    * Add a scrolling list of video thumbnail images from the current channel.
    */
   private void setupThumbnails()
   {
      // Get thumbnail views
      thumbnails = (Gallery) findViewById(R.id.lsifaceThumbnails);
      titleDisplay = (TextView) findViewById(R.id.lsifaceThumbTitle);
      thumbnails.setAdapter(thumbAdapter);
      
      // On thumbnail click...
      thumbnails.setOnItemClickListener(new OnItemClickListener()
      {
         public void onItemClick(AdapterView<?> av, View v, int pos, long id)
         {// Listener sends call to playVideo for the index of the thumbnail
          // clicked on
            actvCon.playVideo(pos,
                    ((ThumbnailsAdapter) av.getAdapter()).getThumbTitle(pos));
         }
      });

      // On thumbnail highlighted...
      thumbSelected = new OnThumbSelected();
      thumbnails.setOnItemSelectedListener(thumbSelected);
      //thumbnails.setCallbackDuringFling(false);
   }

   /**
    * Called when no videos for found in a channel. Set title bar to show alert.
    */
   public void noVideosFound(String chan)
   {
      //"No videos found in <subreddit>."
      titleDisplay.setText(
              actvCon.getResources().getString(R.string.emptyVideosPrompt)
                + " " + chan + ".");
   }

   private void showInterface()
   {
       // Flip hidden state
       hidden = false;
       // Run animation to make interface visible
       AlphaAnimation showAnim =
               new AlphaAnimation((float)0.0, (float)1.0);
       showAnim.setFillAfter(true);
       showAnim.setDuration(IN_ANIM_MS);
       this.startAnimation(showAnim);
       //Start count down to auto-hide
       startHideTimer();
       actvCon.hideContextBar();
   }

    private void hideInterface()
    {
        // Flip hidden state
        hidden = true;
        // Run animation to make interface invisible
        AlphaAnimation hideAnim =
                new AlphaAnimation((float)1.0, (float)0.0);
        hideAnim.setFillAfter(true);
        hideAnim.setDuration(OUT_ANIM_MS);
        this.startAnimation(hideAnim);
        actvCon.showContextBar();
    }

   /* Called when the hide interface timer expires */
   private class HideTask extends TimerTask
   {
      public void run()
      {
         //VideoInterface.this.subredditSearch.h
         actvCon.runOnUiThread(new Runnable()
         {
            public void run()
            {
               if (!hidden)
               {
                  VideoInterface.this.hideInterface();
               }
            }
         });
      }
   }

   /* Schedule this interface to hide after a delay */
   private void startHideTimer()
   {
      // Remove any pending animations
      hideTimer.cancel();
       //new Timer and TimerTask avoids illegal state
      hideTimer = new Timer();
      hideTimer.schedule(new HideTask(), HIDE_DELAY);
   }

   private void hideSoftKeyboard()
   {
      actvCon.hideSoftKeyboard(getWindowToken());
   }

   public void setLoadProgress(int p)
   {
      titleDisplay.setText("loading... %"+ p);
   }

   public void clearThumbnails()
   {
       actvCon.runOnUiThread(new Runnable()
       {
           public void run()
           {
               // Make thumbnails list empty
               thumbAdapter.clearAllThumbs();//addThumbnails();
               setLoadProgress(0);
           }
        });
   }
}
