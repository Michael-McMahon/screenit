package com.mmcmahon.rtv2go.channels;

import java.util.Vector;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;
import com.mmcmahon.rtv2go.R.color;
import com.mmcmahon.rtv2go.VideoInterface;
import com.mmcmahon.rtv2go.dialogs.RemoveDialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.TransitionDrawable;

/**
 * Serves up views which display subreddit names, and search box.
 * At position 0 is the serach box which allows text entry for adding a new
 * subreddit.
 * 12/2013: REMOVING FEATURE EMPTY FILTERING FOR NOW
 * If empty subreddit filtering is initiated, this adapter will manage a check
 * and callback system for finding and removing any subreddits with no video 
 * postings.    
 *  
 * Summer 2012
 * @author Michael McMahon
 *
 */
public class ChannelsAdapter extends BaseAdapter
{
   //private static final int ANIM_DUR = 750;
   //public static final int SEARCH_WIDTH = 320;//dp

   private int selected = -1;//Index of selected channel
   //private View vSelected = null;
   final private ACTV_VideoPlayer actvCon;
   final private Vector<String> channels = new Vector<String>();
   final private LayoutInflater inflater;
   private View searchBox;
   final private RemoveDialog removePrompt;

   public ChannelsAdapter(Context c)
   {
      actvCon = (ACTV_VideoPlayer) c;
      inflater = (LayoutInflater) actvCon
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      // Create the search box view
      createSearchBox();
      // Create remove channel dialog
      removePrompt = new RemoveDialog(actvCon, this);
   }

   private void createSearchBox()
   {
       /*
      EditText searchText = new EditText(actvCon);
      searchText.setHint(R.string.topicSearch);
      searchText.setPadding(8, 8, 8, 8);
      searchText.setTextSize(18);
      searchText.setBackgroundResource(color.white); //R.color.IFACE_BG);
      searchText.setTextColor(actvCon.getResources().getColor(
              color.white));//10/2013 naaaww R.color.SEARCH_GREEN));// Search green color association!
      searchText.setHintTextColor(actvCon.getResources().getColor(
              color.white));//R.color.SEARCH_GREEN_OPQ));
      searchText.setCursorVisible(true);
      searchText.setLayoutParams(
                  new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                          LayoutParams.MATCH_PARENT));
      searchText.setSingleLine(true);
      //searchText.setMinHeight(50);
      //searchText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
      searchText.setImeActionLabel("Add", VideoInterface.ADD_ACTION);

        //2/2014 wrapping editext for center alignment
       searchBox = new LinearLayout(actvCon);
       searchBox.setLayoutParams(
               new LayoutParams(LayoutParams.WRAP_CONTENT
                       ,LayoutParams.MATCH_PARENT));
      searchBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
      searchBox.addView(searchText);
       */
      searchBox = inflater.inflate(R.layout.search_box, null);
   }

   private class OnChannelClick implements OnClickListener, OnLongClickListener
   {
      final private int channel;

      public OnChannelClick(int ci)
      {
         channel = ci;
      }

      public void onClick(View clicked)
      {
         if(channel == selected){
             return;
         }
         //UI will show view as selected
         ChannelsAdapter.this.setSelected(channel);
         actvCon.loadChannel((String)ChannelsAdapter.this.getItem(channel));
      }
      public boolean onLongClick(View v)
      {
         //Keep at least one channel
         if(channels.size() > 1)
         {
            actvCon.showRemove(true, removePrompt.setChannel(
                    (String)ChannelsAdapter.this.getItem(channel)));
         }
         return true;
      }
   }

   /*Number of Views*/
   public int getCount()
   {
      //Channel views and one extra search view
      return channels != null ? channels.size() + 1 : 1;
   }

   public Object getItem(int pos)
   {
      if(channels == null
              || channels.size() < pos
              || pos < 0)
      {
          return null;
      }
      else
      {
          return channels.get(pos);
      }
   }

   public long getItemId(int id)
   {
      return id;
   }

   public View getView(final int pos, View v, ViewGroup list)
   {
      int chPos = pos - 1; // Offset by one; Search box view
      // If requesting the left most view, return the search box
      if (chPos == -1)
      {
         //searchBox.requestFocus();
         return searchBox;
      }
      // Else, return channel name view
      String name = channels.get(chPos);
      View row;
      TextView text;
      OnChannelClick occ;

      if (v == null || v == searchBox)// Create a new textview
      {
         row = inflater.inflate(R.layout.channel_view, null);

      } else
      {
          // Recycled view
          row = v;
      }

      text = (TextView) row.findViewById(R.id.channel_text);
      text.setText(name);
      occ = new OnChannelClick(chPos);
      row.setOnClickListener(occ);
      row.setOnLongClickListener(occ);

      //Selected channel gets highlighted background
      if(chPos == selected)
      {
          row.setBackgroundResource(R.color.white);
          text.setTextColor(Color.BLACK);
          text.setTypeface(Typeface.DEFAULT_BOLD);
          //vSelected = row;
      }
      else
      {
          row.setBackgroundResource(0);
          text.setTextColor(Color.WHITE);
          text.setTypeface(Typeface.DEFAULT);
      }
      return row;
   }

   private void removeDefault(final String channel)
   {
      actvCon.spDeleteChannel(channel);

         final String message = channel + " "
               + actvCon.getResources().getString(R.string.not_def_chan);
         actvCon.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast confirm = Toast.makeText(actvCon, message, Toast.LENGTH_LONG);
                 confirm.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                 confirm.show();
                 ChannelsAdapter.this.notifyDataSetChanged();         }
         });
   }

   public void removeChannel(final String ch)
   {
      final int rmPos = channels.indexOf(ch);

       if(rmPos < 0)
      {
           return;
      }

       //Removing currently selected channel
       if(rmPos == selected)
       {
           if(channels.size()-1 == selected)
           {
               //Selected is last: load channel to left of selected
               actvCon.loadChannel(
                       (String)ChannelsAdapter.this.getItem(selected-1));
               selected--;
           }
           else
           {
               //Load channel to right of selected
               actvCon.loadChannel(
                       (String)ChannelsAdapter.this.getItem(selected+1));
           }
       }

      ((Activity) actvCon).runOnUiThread(new Runnable()
      {
          public void run()
          {
              if(rmPos < selected)
              {
                  //selected index shifts left
                  selected--;
              }
              channels.remove(rmPos);
              ChannelsAdapter.this.notifyDataSetChanged();
          }
      });

       removeDefault(ch);
   }

    /**
     * Adds a new channel to the top of the list. New channel will be loaded
     * and appear as the selected channel in UI.
     * @param channel Channel to add, select, and load
     */
    public void pushChannel(String channel)
    {
        int i = channels.indexOf(channel);
        if(i < 0)
        {
            addChannel(channel, 0);
            //Store the new channel in saved preferences
            actvCon.spSaveChannel(channel);
            i = 0;
        }
        setSelected(i);
        actvCon.loadChannel(channel);
    }

   public void addChannel(String name)
   {
      addChannel(name, channels.size());
   }

   private void addChannel(final String ch, final int i)
   {
      if (!channels.contains(ch))
      {
         // Notify UI Thread of new entry
         ((Activity) actvCon).runOnUiThread(new Runnable() {
             public void run() {
                 channels.insertElementAt(ch, i);
                 ChannelsAdapter.this.notifyDataSetChanged();
             }
         });
      }
   }

   /* Returns the EditText for subreddit searching */
   public EditText getSearchBox()
   {
      return (EditText)searchBox.findViewById(R.id.searchEdit);
   }

   /*Run Bright to Dark animation*/
    /*
   private void bgFromGreen(View v)
   {
       TransitionDrawable bgAnim = (TransitionDrawable) actvCon
               .getResources().getDrawable(R.anim.search_add);
       v.setBackgroundDrawable(bgAnim);
       bgAnim.setCrossFadeEnabled(true);
       bgAnim.startTransition(ANIM_DUR);
   }
    */

    /* Will set a list item's background to a bright color. Only ONE list item
     * should be highlighted at a time.
    */
    public void setSelected(final int i)
    {
        //Run dark to bright animation
        /*
        TransitionDrawable bgAnim = (TransitionDrawable) actvCon
                .getResources().getDrawable(R.anim.search_add);
        v.setBackgroundDrawable(bgAnim);
        bgAnim.setCrossFadeEnabled(true);
        bgAnim.reverseTransition(ANIM_DUR);
        */

        //Refresh the (de)selected views, +1 offset for search view
        actvCon.runOnUiThread(new Runnable() {
            public void run() {
                selected = i;
                //bgFromGreen(vSelected);
                ChannelsAdapter.this.notifyDataSetChanged();
            }
        });
    }
}
