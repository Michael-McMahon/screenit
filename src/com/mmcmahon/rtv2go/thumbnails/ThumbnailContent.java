package com.mmcmahon.rtv2go.thumbnails;

/**
 * A set of data pertaining to a video thumbnail. This class provides
 * a light weight object for passing around during thumbnail interaction.
 *
 * Summer 2012
 * @author Michael McMahon
 */
public class ThumbnailContent
{
   final private String url;
   final private String title;

   public ThumbnailContent(String u, String t)
   {
      url = u;
      title = t;
   }

   public String getUrl()
   {
      return url;
   }

   public String getTitle()
   {
      return title;
   }
}