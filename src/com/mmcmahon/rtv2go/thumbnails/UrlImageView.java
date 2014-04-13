package com.mmcmahon.rtv2go.thumbnails;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;

class UrlImageView extends ImageView
{
    private String url;

    UrlImageView(Context context)
    {
        super(context);
    }
    UrlImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    UrlImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public synchronized void setUrl(String u)
    {
        url = u;
    }

    public synchronized String getUrl()
    {
        return url;
    }
}