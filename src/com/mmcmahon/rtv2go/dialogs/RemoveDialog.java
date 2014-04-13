package com.mmcmahon.rtv2go.dialogs;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;
import com.mmcmahon.rtv2go.channels.ChannelsAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

/**
 * Screen overlay for asking the user to remove subreddits saved to Shared
 * Preferences. May be configured to show a single subreddit or a list of
 * subreddits.
 * 
 * @author Michael McMahon
 */
public class RemoveDialog extends Dialog
{
   final private View yes, no;
   final private ChannelsAdapter chanAdapter;
   private String chan;// Single channel to be removed
   final private TextView prompt;
   final private Resources res;// Has string resources
   final private ACTV_VideoPlayer actvCon;

   public RemoveDialog(Context context, ChannelsAdapter ca)
   {
      super(context, android.R.style.Theme_Translucent_NoTitleBar);

      actvCon = (ACTV_VideoPlayer) context;
      chanAdapter = ca;
      setContentView(R.layout.remove_popup);
      yes = findViewById(R.id.removeYes);
      no = findViewById(R.id.removeNo);
      prompt = (TextView) findViewById(R.id.removeMsg);
      res = context.getResources();
      setListeners();
   }

   private void setPrompt(String chName)
   {
      String msg = String.format(res.getString(R.string.remove_prompt), chName);
      prompt.setText(msg);
   }

   public RemoveDialog setChannel(final String c)
   {
      chan = c;
      actvCon.runOnUiThread(new Runnable()
      {
         public void run()
         {
            setPrompt(chan);
         }
      });
      return this;
   }

   private void setListeners()
   {
      yes.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View arg0)
         {
            chanAdapter.removeChannel(chan);
            actvCon.showRemove(false, RemoveDialog.this);
         }
      });

      no.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View arg0)
         {
            RemoveDialog.this.hide();
            actvCon.showRemove(false, RemoveDialog.this);
         }
      });
   }

}
