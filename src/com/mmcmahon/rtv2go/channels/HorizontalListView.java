package com.mmcmahon.rtv2go.channels;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;
import android.util.Log;

/**
 * A horizontally scrolling list. 
 * Obtained and authored here: https://github.com/dinocore1/DevsmartLib-Android
 * Minor addition of runReqLayout class.
 */
public class HorizontalListView extends AdapterView<ChannelsAdapter>
{
   private ChannelsAdapter mAdapter;
   private int mLeftViewIndex = -1;
   private int mRightViewIndex = 0;
   private int mCurrentX;
   private int mNextX;
   private int mMaxX = Integer.MAX_VALUE;
   private int mDisplayOffset = 0;
   private Scroller mScroller;
   private GestureDetector mGesture;
   final private Queue<View> mRemovedViewQueue = new LinkedList<View>();
   private boolean mDataChanged = true;//false; 2/1 First load hides search
   final private Runnable runReqLayout = new Runnable()
   {
      public void run()
      {
         requestLayout();
      }
   };

   public HorizontalListView(Context context, AttributeSet attrs)
   {
      super(context, attrs);

       //MM 1/2014 Will set x value of scroll position, to reflect hidden search box
       //getViewTreeObserver().addOnGlobalLayoutListener(xCorrect);
       initView();
    }

   private synchronized void initView()
   {
       /*  mLeftViewIndex = 0;
       mRightViewIndex = 0;
       */
       mLeftViewIndex = -1;
       mRightViewIndex = 0;
       mCurrentX = 0;
       mDisplayOffset = 0;
      mNextX = 0;
      mMaxX = Integer.MAX_VALUE;
      mScroller = new Scroller(getContext());
      mGesture = new GestureDetector(getContext(), mOnGesture);
   }

   final private DataSetObserver mDataObserver = new DataSetObserver()
   {
      @Override
      public void onChanged()
      {
         synchronized (HorizontalListView.this)
         {
            mDataChanged = true;
         }
         invalidate();
         requestLayout();
      }

      @Override
      public void onInvalidated()
      {
         reset();
         invalidate();
         requestLayout();
      }
   };

   @Override
   public ChannelsAdapter getAdapter()
   {
      return mAdapter;
   }

   @Override
   public View getSelectedView()
   {
      // TODO: implement
      return null;
   }

    @Override
   public void setAdapter(ChannelsAdapter adapter)
   {
      if (mAdapter != null)
      {
         mAdapter.unregisterDataSetObserver(mDataObserver);
      }
      mAdapter = adapter;
      mAdapter.registerDataSetObserver(mDataObserver);

      reset();
   }

   private synchronized void reset()
   {
      initView();
      removeAllViewsInLayout();
      requestLayout();
   }

   @Override
   public void setSelection(int position)
   {
      // Log.e(TAG, "Thumb pos is now " + position);
      // TODO: implement
   }

   private void addAndMeasureChild(final View child, int viewPos)
   {
      LayoutParams params = child.getLayoutParams();
      if (params == null)
      {
         params = new LayoutParams(LayoutParams.WRAP_CONTENT,
               LayoutParams.MATCH_PARENT);
      }

      addViewInLayout(child, viewPos, params, true);
      child.measure(
            MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

       /*2/2014
      // Make this parent view wrap to child view size
      params = this.getLayoutParams();
      params.height = child.getMeasuredHeight();
      this.setLayoutParams(params);
       */
   }

   @Override
   protected synchronized void onLayout(boolean changed, int left, int top,
         int right, int bottom)
   {
      super.onLayout(changed, left, top, right, bottom);

      // Log.e("onLayout", changed+"=changed. "+mCurrentX);
      if (mAdapter == null)
      {
         return;
      }

     //  Log.e("onLayout", mDataChanged+"=mDataChanged. "+mCurrentX);
      if (mDataChanged)
      {
         int oldCurrentX = mCurrentX;
         initView();
          mNextX = oldCurrentX;

          //MM 1/2014 Will hide search box if one or more channels present
          if(getChildCount() == 0 && mAdapter.getCount() > 1)
          {
            //  Log.e("onLayout","hiding search");
              mLeftViewIndex = 0;//Skip the search view
              mRightViewIndex = 1;//Start at first channel view
              View search = mAdapter.getView(0,null,null);
              addAndMeasureChild(search, 0);//Add search
              addAndMeasureChild(mAdapter.getView(1,null,null), 1);
              mNextX = search.getMeasuredWidth();
              mCurrentX = mNextX;
              //MM 1/2014 Will set x value of scroll position, to reflect hidden search box
              //getViewTreeObserver().addOnGlobalLayoutListener(xCorrect);
          }

          removeAllViewsInLayout();


         mDataChanged = false;
      }

      if (mScroller.computeScrollOffset())
      {
         mNextX = mScroller.getCurrX();
      }

      if (mNextX <= 0)
      {
         mNextX = 0;
         mScroller.forceFinished(true);
      }
      if (mNextX >= mMaxX)
      {
         mNextX = mMaxX;
         mScroller.forceFinished(true);
      }
      int dx = mCurrentX - mNextX;

     //  Log.e("###", "left: "+mLeftViewIndex+" right: "+mRightViewIndex+" current: "+mCurrentX+" offset="+mDisplayOffset+" dx: "+dx);
      removeNonVisibleItems(dx);
      fillList(dx);
      positionItems(dx);
     //  Log.e("filled", "left: "+mLeftViewIndex+" right: "+mRightViewIndex+" current: "+mCurrentX+" offset="+mDisplayOffset+" dx: "+dx);

       mCurrentX = mNextX;

      if (!mScroller.isFinished())
      {
         post(runReqLayout);
      }
   }

   private void fillList(final int dx)
   {
      int edge = 0;
      View child = getChildAt(getChildCount() - 1);
      if (child != null)
      {
         edge = child.getRight();
      }
      fillListRight(edge, dx);

      edge = 0;
      child = getChildAt(0);
      if (child != null)
      {
         edge = child.getLeft();
      }
      fillListLeft(edge, dx);
   }

   private void fillListRight(int rightEdge, final int dx)
   {
      while (rightEdge + dx < getWidth()
            && mRightViewIndex < mAdapter.getCount())
      {

         View child = mAdapter.getView(mRightViewIndex,
               mRemovedViewQueue.poll(), this);
         addAndMeasureChild(child, -1);
         rightEdge += child.getMeasuredWidth();

         if (mRightViewIndex == mAdapter.getCount() - 1)
         {
            mMaxX = mCurrentX + rightEdge - getWidth();
         }

         if (mMaxX < 0)
         {
            mMaxX = 0;
         }
         mRightViewIndex++;
      }
   }

   private void fillListLeft(int leftEdge, final int dx)
   {
      while (leftEdge + dx > 0 && mLeftViewIndex >= 0)
      //while (mLeftViewIndex >= 0)
      {
         View child = mAdapter.getView(mLeftViewIndex,
               mRemovedViewQueue.poll(), this);
         addAndMeasureChild(child, 0);
         leftEdge -= child.getMeasuredWidth();
         mLeftViewIndex--;
         mDisplayOffset -= child.getMeasuredWidth();
      }
   }

   private void removeNonVisibleItems(final int dx)
   {
      View child = getChildAt(0);
      while (child != null && child.getRight() + dx <= 0)
      {
         mDisplayOffset += child.getMeasuredWidth();
         mRemovedViewQueue.offer(child);
         removeViewInLayout(child);
         mLeftViewIndex++;
         child = getChildAt(0);
      }

      child = getChildAt(getChildCount() - 1);
      while (child != null && child.getLeft() + dx >= getWidth())
      {
         mRemovedViewQueue.offer(child);
         removeViewInLayout(child);
         mRightViewIndex--;
         child = getChildAt(getChildCount() - 1);
      }
   }

   private void positionItems(final int dx)
   {
      if (getChildCount() > 0)
      {
         mDisplayOffset += dx;
         int left = mDisplayOffset;
         for (int i = 0; i < getChildCount(); i++)
         {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
            left += childWidth;
         }
      }
   }

   public synchronized void scrollTo(int x)
   {
      mScroller.startScroll(mNextX, 0, x - mNextX, 0);
      requestLayout();
   }

   /* Down events will go to children, but if corresponding up event is a
    * scroll gesture, the down event gets canceled.
    */
   @Override
   public boolean dispatchTouchEvent(MotionEvent ev)
   {
      if(mGesture.onTouchEvent(ev))
      {
          //Up result was gesture, so cancel down for children
          ev.setAction(MotionEvent.ACTION_CANCEL);
      }
      return super.dispatchTouchEvent(ev);
   }

   void onFling(float velocityX)
   {
      synchronized (HorizontalListView.this)
      {
         mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
      }
      requestLayout();
   }

   boolean onDown()
   {
      //Consider this a 'stop scrolling' gesture, no down event for child
      if(mScroller.computeScrollOffset())
      {
        mScroller.forceFinished(true);
        return true;
      }
      //Child can receive down when not scrolling
      return false;
   }

   final private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
   {
      @Override
      public boolean onDown(MotionEvent e)
      {
          return HorizontalListView.this.onDown();
      }

      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY)
      {
         HorizontalListView.this.onFling(velocityX);
         return true;
      }

      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY)
      {
         synchronized (HorizontalListView.this)
         {
            mNextX += (int) distanceX;
         }
         requestLayout();

         return true;
      }

       @Override
       public boolean onSingleTapUp(MotionEvent e)
       {
           //Up event is a single tap, send to child
           return false;
       }

       @Override
       public void onLongPress(MotionEvent e) {
           //Send long press to children
       }
   };
}