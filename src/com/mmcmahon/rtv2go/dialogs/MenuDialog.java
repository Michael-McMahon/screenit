package com.mmcmahon.rtv2go.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import com.mmcmahon.rtv2go.ACTV_VideoPlayer;
import com.mmcmahon.rtv2go.R;

/**
 * A dialog which houses menu type buttons, exit or logout, for instance.
 * 
 * Summer 2012
 * @author Michael McMahon
 */
public class MenuDialog extends Dialog
{
   private static final int VIB_DUR = 100;

   final private ACTV_VideoPlayer actvCon;
   final private TextView exit;

    public MenuDialog(Context context)
   {
      super(context, android.R.style.Theme_Translucent_NoTitleBar);
      setContentView(R.layout.menu_popup);
      actvCon = (ACTV_VideoPlayer) context;
      exit = (TextView)findViewById(R.id.menuExit);
      
      setListeners();
   }

   @Override
   public void onBackPressed()
   {
      actvCon.closeMenu();
   }


   private void setListeners()
   {
      exit.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View arg0)
         {
            ((Vibrator) actvCon.getSystemService(Context.VIBRATOR_SERVICE))
                  .vibrate(VIB_DUR);
            actvCon.finish();
         }
      });
   }
}
