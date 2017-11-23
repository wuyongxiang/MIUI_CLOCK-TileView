package com.xiangzi.miui_clock;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;



public class TileView extends View {
	private Camera mCamera;
	private Matrix mMatrix;
	private int mCenterX;
	private int mCenterY;

	private float[] mCurrentRotate;
	private float mCurrentDepth;

	private int mMaxDepth;
	private int mMaxRotateDegree;

	private Scroller mScroller;

	public TileView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mMaxDepth = 100;
		mMaxRotateDegree = Math.abs(15);

		mCamera = new Camera();
		mMatrix = new Matrix();
		mScroller = new Scroller(context);
		mCurrentRotate = new float[2];
	}

	public TileView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.borderlessButtonStyle);
	}
	int startX,startY;
	private Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what){
				case 0:
					CurrentRotate currentRotate = (CurrentRotate) msg.obj;
					Log.i("currentRotate",currentRotate.toString());
					mScroller.startScroll(currentRotate.startX,currentRotate.startY,currentRotate.endX,currentRotate.endY);
					int d = (int) Math.sqrt(currentRotate.endX*currentRotate.endX+currentRotate.endY*currentRotate.endY);
					if(d>0){
						Message msg2 = new Message();
						msg2.what = 0;
						msg2.obj = new CurrentRotate(currentRotate.endX,currentRotate.endY,
								(currentRotate.startX != 0) ?( currentRotate.endX*(currentRotate.endX/currentRotate.startX)*0.8f):0,
								(currentRotate.startY != 0) ? ( currentRotate.endY*(currentRotate.endY/currentRotate.startY)*0.8f):0);
						handler.sendMessageDelayed(msg2,50);
					}else{
						Message msg2 = new Message();
						msg2.what = 1;
						msg2.obj = new CurrentRotate(currentRotate.endX,currentRotate.endY,-currentRotate.endX,-currentRotate.endY);

						handler.sendMessageDelayed(msg2,50);
					}
					break;
				case 1:
					CurrentRotate currentRotate2 = (CurrentRotate) msg.obj;
					mScroller.startScroll(currentRotate2.startX,currentRotate2.startY,currentRotate2.endX,currentRotate2.endY);
					break;
			}


		}
	};

	public TileView(Context context) {
		this(context, null);
	}

	public TileView(Context context,int defStyle) {
		this(context, null,defStyle);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int centerX = mCenterX;
		final int centerY = mCenterY;
		final float x = event.getX();
		final float y = event.getY();
		final float[] currentRotate = mCurrentRotate;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				final float[] rotate = new float[2];
				computeRotate(rotate, x, y, centerX, centerY);
				mScroller.startScroll(currentRotate[0], currentRotate[1], rotate[0]
						- currentRotate[0], rotate[1] - currentRotate[1]);
				break;
			case MotionEvent.ACTION_MOVE:
				computeRotate(currentRotate, x, y, centerX, centerY);
				applyRotate(currentRotate[0], currentRotate[1], mCurrentDepth);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
			case MotionEvent.ACTION_UP:
				Message msg = new Message();
				msg.what = 0;
				msg.obj = new CurrentRotate(currentRotate[0], currentRotate[1], (- currentRotate[0]*2*0.4f), (- currentRotate[1]*2*0.4f));
				handler.sendMessage(msg);
				break;
		}
		invalidate();
		requestLayout();
		return super.onTouchEvent(event);
	}



	@Override
	public void computeScroll() {
		super.computeScroll();
		final float[] currentRotate = mCurrentRotate;

		if (mScroller.computeScrollOffset()) {
			currentRotate[0] = mScroller.getCurrX();
			currentRotate[1] = mScroller.getCurrY();
			applyRotate(currentRotate[0], currentRotate[1], mCurrentDepth);
			invalidate();
			requestLayout();
		}
	}

	protected void computeRotate(float[] rotate, float x, float y, int centerX,
								 int centerY) {
		final int maxRotateDegree = mMaxRotateDegree;
		rotate[0] = (int) Math.min(Math.max(-(y - centerY) * maxRotateDegree
				/ centerY, -maxRotateDegree), maxRotateDegree);
		rotate[1] = (int) Math.min(Math.max((x - centerX) * maxRotateDegree
				/ centerX, -maxRotateDegree), maxRotateDegree);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCenterX = w / 2;
		mCenterY = h / 2;
	}

	@SuppressLint("NewApi") private void applyRotate(float x, float y, float depth) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setRotationX(x);
			setRotationY(y);

			float scale = depthToScale(depth);
			setScaleX(scale);
			setScaleY(scale);
		} else {
			final Camera camera = mCamera;
			final Matrix matrix = mMatrix;
			camera.save();
			camera.translate(0.0f, 0.0f, depth);
			camera.rotateY(y);
			camera.rotateX(x);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-mCenterX, -mCenterY);
			matrix.postTranslate(mCenterX, mCenterY);
		}
	}

	private float depthToScale(float depth) {
		if (depth <= 0) {
			return 1;
		}

		return (1000 - depth) / 1000;
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.concat(mMatrix);
		super.draw(canvas);
		canvas.restore();
	}

	public int getMaxDepth() {
		return mMaxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.mMaxDepth = maxDepth;
	}

	public int getMaxRotateDegree() {
		return mMaxRotateDegree;
	}

	public void setMaxRotateDegree(int maxRotateDegree) {
		this.mMaxRotateDegree = maxRotateDegree;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.maxDepth = mMaxDepth;
		savedState.maxRotateDegree = mMaxRotateDegree;
		return savedState;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mMaxDepth = savedState.maxDepth;
		mMaxRotateDegree = savedState.maxRotateDegree;
	}

	/**
	 * Base class for save the state of this view.
	 *
	 * @author Tank
	 *
	 */
	static class SavedState extends BaseSavedState {
		public int maxDepth;
		public int maxRotateDegree;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			maxDepth = in.readInt();
			maxRotateDegree = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(maxDepth);
			out.writeInt(maxRotateDegree);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
	class CurrentRotate{
		float startX;
		float startY;
		float endX;
		float endY;

		public CurrentRotate(float startX, float startY, float endX, float endY) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}

		@Override
		public String toString() {
			return "CurrentRotate{" +
					"startX=" + startX +
					", startY=" + startY +
					", endX=" + endX +
					", endY=" + endY +
					'}';
		}
	}
	class LooperThread extends Thread {
		public Handler mHandler;

		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					// process incoming messages here
				}
			};

			Looper.loop();
		}
	}
}