package geek.fan.multipleimageview.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import geek.fan.multipleimageview.R;

/**
 * Created by fan on 16/3/16.
 */
public class MultipleImageView extends View {

    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<Bitmap> mBitmaps = new ArrayList<>();
    private Bitmap mPlaceHolder;
    private int mHorizontalSpace = 10;
    private int mVerticalSpace = 10;
    private int mRadius = 0;
    private int mColumns = 3;
    private int mRows = 1;
    private int mMaxImageWidth = 0;
    private int mImageWidth = 0;
    private int mMinImageWidth = 0;
    private boolean mIsStaggered = true;
    private Matrix matrix = new Matrix();
    final Paint paint = new Paint();
    private boolean isLoading = false;
    private OnClickItemListener onClickItemListener;
    private MotionEvent mEventDown;
    private int mDown;
    private List<WeakReference<SimpleTarget<Bitmap>>> targetList = new ArrayList<>();


    public MultipleImageView(Context context) {
        this(context, null);
    }

    public MultipleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultipleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultipleImageView);
            for (int i = 0; i < a.getIndexCount(); i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.MultipleImageView_mivHorizontalSpace:
                        mHorizontalSpace = a.getDimensionPixelSize(attr, mHorizontalSpace);
                        break;
                    case R.styleable.MultipleImageView_mivVerticalSpace:
                        mVerticalSpace = a.getDimensionPixelSize(attr, mVerticalSpace);
                        break;
                    case R.styleable.MultipleImageView_mivRadius:
                        mRadius = a.getDimensionPixelSize(attr, mRadius);
                        break;
                    case R.styleable.MultipleImageView_mivStaggeredMode:
                        mIsStaggered = a.getBoolean(attr, mIsStaggered);
                        break;
                }
            }
        }

        paint.setAntiAlias(true);
        mPlaceHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_photo);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isLoading) {
            loadBitmap(0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mImageUrls.isEmpty()) {
            int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            mRows = (int) Math.ceil(mImageUrls.size() * 1f / 3);
            if (!mIsStaggered) {
                mImageWidth = (width - (mColumns - 1) * mHorizontalSpace - getPaddingLeft() - getPaddingRight()) / mColumns;
                height = mImageWidth * mRows + (mRows - 1) * mVerticalSpace + getPaddingTop() + getPaddingBottom();
            } else {
                mMaxImageWidth = width - getPaddingLeft() - getPaddingRight();
                mImageWidth = (width - mHorizontalSpace - getPaddingLeft() - getPaddingRight()) / 2;
                mMinImageWidth = (width - mHorizontalSpace - getPaddingLeft() - getPaddingRight()) / 3;
                switch (mImageUrls.size()) {
                    case 1:
                        height = mMaxImageWidth + getPaddingTop() + getPaddingBottom();
                        break;
                    case 2:
                        height = mImageWidth + getPaddingTop() + getPaddingBottom();
                        break;
                    case 3:
                        height = mMaxImageWidth + mVerticalSpace + mImageWidth + getPaddingTop() + getPaddingBottom();
                        break;
                    case 4:
                        height = mImageWidth * 2 + mVerticalSpace + getPaddingTop() + getPaddingBottom();
                        break;
                    case 5:
                        height = mImageWidth + mMinImageWidth + mVerticalSpace + getPaddingTop() + getPaddingBottom();
                        break;
                    case 6:
                        height = mMinImageWidth * 2 + mVerticalSpace * 2 + getPaddingTop() + getPaddingBottom();
                        break;
                    case 7:
                        height = mMinImageWidth + mImageWidth * 2 + mVerticalSpace * 2 + getPaddingTop() + getPaddingBottom();
                        break;
                    case 8:
                        height = mMinImageWidth * 2 + mImageWidth + mVerticalSpace * 2 + getPaddingTop() + getPaddingBottom();
                        break;
                    case 9:
                        height = mMinImageWidth * 3 + mVerticalSpace * 3 + getPaddingBottom() + getPaddingTop();
                        break;
                }
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mImageUrls.isEmpty()) {
            switch (mImageUrls.size()) {
                case 1:
                    drawBitmap(canvas, 0, 0, mMaxImageWidth, 0, 0);
                    break;
                case 2:
                    drawBitmap(canvas, 0, 0, mImageWidth, 0, 0);
                    drawBitmap(canvas, 0, 1, mImageWidth, 0, 1);
                    break;
                case 3:
                    drawBitmap(canvas, 0, 0, mMaxImageWidth, 0, 0);
                    drawBitmap(canvas, 1, 0, mImageWidth, mMaxImageWidth, 1);
                    drawBitmap(canvas, 1, 1, mImageWidth, mMaxImageWidth, 2);
                    break;
                case 4:
                    for (int row = 0; row < 2; row++) {
                        for (int column = 0; column < 2; column++) {
                            drawBitmap(canvas, row, column, mImageWidth, row * mImageWidth, row * 2 + column);
                        }
                    }
                    break;
                case 5:
                    for (int column = 0; column < 2; column++) {
                        drawBitmap(canvas, 0, column, mImageWidth, 0, column + 1);
                    }
                    for (int column = 0; column < 3; column++) {
                        drawBitmap(canvas, 1, column, mMinImageWidth, mImageWidth, 2 + column);
                    }
                    break;
                case 6:
                    for (int row = 0; row < 2; row++) {
                        for (int column = 0; column < 3; column++) {
                            drawBitmap(canvas, row, column, mMinImageWidth, row * mMinImageWidth, row * 3 + column);
                        }
                    }
                    break;
                case 7:
                    for (int column = 0; column < 2; column++) {
                        drawBitmap(canvas, 0, column, mImageWidth, 0, column);
                    }
                    for (int column = 0; column < 3; column++) {
                        drawBitmap(canvas, 1, column, mMinImageWidth, mImageWidth, 2 + column);
                    }
                    for (int column = 0; column < 2; column++) {
                        drawBitmap(canvas, 2, column, mImageWidth, mImageWidth + mMinImageWidth, 5 + column);
                    }
                    break;
                case 8:
                    for (int column = 0; column < 2; column++) {
                        drawBitmap(canvas, 0, column, mImageWidth, 0, column);
                    }
                    for (int row = 1; row < 3; row++) {
                        for (int column = 0; column < 3; column++) {
                            drawBitmap(canvas, row, column, mMinImageWidth, mImageWidth + mMinImageWidth * (row - 1), 2 + (row - 1) * 3 + column);
                        }
                    }
                    break;
                /*case 9:
                    for (int row = 0; row < 3; row++) {
                        for (int column = 0; column < 3; column++) {
                            drawBitmap(canvas, row, column, mMinImageWidth);
                        }
                    }
                    break;*/
            }
        }
    }

    private void drawBitmap(Canvas canvas, int row, int column, int imageWidth, int perImageWidth, int i) {
        Bitmap bitmap;
        if (i >= mImageUrls.size()) {
            return;
        } else if (i < mBitmaps.size()) {
            bitmap = mBitmaps.get(i);
        } else {
            bitmap = mPlaceHolder;
            Log.d("drawBitmap", "row:" + row + "column:" + "pos:" + i + "-mPlaceHolder");
        }

        float left = getPaddingLeft() + column * mHorizontalSpace + column * imageWidth;
        float top = getPaddingTop() + row * mVerticalSpace + perImageWidth;
        if (mImageUrls.size() == 6) {
            Log.e("onDraw", "left=" + left + "top=" + top);
        }

        float scale;
        float dx = 0, dy = 0;

        int dwidth = bitmap.getWidth();
        int dheight = bitmap.getHeight();
        int vwidth = imageWidth;
        int vheight = imageWidth;
        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight;
            dx = (vwidth - dwidth * scale) * 0.5f;//scale后中心点
        } else {
            scale = (float) vwidth / (float) dwidth;
            dy = (vheight - dheight * scale) * 0.5f;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate(left + Math.round(dx), top + Math.round(dy));

        BitmapShader mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapShader.setLocalMatrix(matrix);
        paint.setShader(mBitmapShader);
        RectF rectF = new RectF(left, top, left + imageWidth, top + imageWidth);
        canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isClickItem = false;
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mEventDown = MotionEvent.obtain(event);
                mDown = getClickItem(mEventDown);
                isClickItem = mDown > -1;
                break;
            case MotionEvent.ACTION_UP:
                if (mEventDown != null) {
                    float distance = (float) Math.sqrt(Math.pow((event.getX() - mEventDown.getX()), 2) + Math.pow((event.getY() - mEventDown.getY()), 2));
                    if (distance < ViewConfiguration.getTouchSlop()) {
                        int iUp = getClickItem(event);
                        if (mDown == iUp && iUp > -1) {
                            isClickItem = true;
                            if (onClickItemListener != null) {
                                onClickItemListener.onClick(iUp, mImageUrls);
                            }
                        }
                    }
                }
                break;
        }
        return isClickItem ? true : super.onTouchEvent(event);
    }

    private int getClickItem(MotionEvent event) {
        int i = -1;
        float result = (event.getX() - getPaddingLeft() * 1f) / (mImageWidth + mHorizontalSpace);
        if (result < 0 || result >= mColumns) {
            return i;
        }
        int column = (int) result;

        result = (event.getY() - getPaddingTop() * 1f) / (mImageWidth + mVerticalSpace);
        if (result < 0 || result >= mRows) {
            return i;
        }
        int row = (int) result;

        i = row * mColumns + column;
        return i < mImageUrls.size() ? i : -1;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /*for (WeakReference<SimpleTarget<Bitmap>> weakTarget : targetList) {
            if (weakTarget.get() != null) {
                Log.e("onDetachedFromWindow", "Glide.clear:" + weakTarget.get().toString());
                Glide.clear(weakTarget.get());
            }
        }*/
        Glide.clear(glideTarget);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public interface OnClickItemListener {
        void onClick(int i, ArrayList<String> urls);
    }

    private void loadBitmap(int i) {
        if (mImageUrls.isEmpty()) {
            return;
        }
        isLoading = true;
        WeakReference<SimpleTarget<Bitmap>> weakTarget;
        if (targetList.isEmpty() || targetList.size() < i + 1) {
            weakTarget = new WeakReference<SimpleTarget<Bitmap>>(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mBitmaps.add(resource);
                    //invalidate();

                    int bSize = mBitmaps.size();
                    refresh(bSize - 1);
                    if (bSize < mImageUrls.size()) {
                        loadBitmap(bSize);
                    } else {
                        isLoading = false;
                    }
                }
            });
            targetList.add(i, weakTarget);
        } else {
            weakTarget = targetList.get(i);
        }
        Glide.with(getContext()).load(mImageUrls.get(i)).asBitmap().dontAnimate().dontTransform().into(glideTarget);
    }

    public void setImageUrls(List<String> imageUrls) {
        this.mImageUrls.clear();
        this.mImageUrls.addAll(imageUrls);
        mBitmaps.clear();
        targetList.clear();
        isLoading = false;
        requestLayout();
    }


    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    private SimpleTarget<Bitmap> glideTarget = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            mBitmaps.add(resource);
            //invalidate();

            int bSize = mBitmaps.size();
            refresh(bSize - 1);
            if (bSize < mImageUrls.size()) {
                loadBitmap(bSize);
            } else {
                isLoading = false;
            }
        }
    };

    private void refresh(int pos) {
        switch (mImageUrls.size()) {
            case 1:
                invalidate();
                break;
            case 2:
                setImageRect(0, pos, mImageWidth, 0);
                break;
            case 3:
                if (pos == 0) {
                    setImageRect(0, 0, mMaxImageWidth, 0);
                } else {
                    setImageRect(1, pos - 1, mImageWidth, mMaxImageWidth);
                }
                break;
            case 4:
                setImageRect(pos / 2, pos % 2, mImageWidth, pos / 2 * mImageWidth);
                break;
            case 5:
                if (pos == 0 || pos == 1) {
                    setImageRect(0, pos, mImageWidth, 0);
                } else {
                    setImageRect(1, pos - 2, mMinImageWidth, mImageWidth);
                }
                break;
            case 6:
                setImageRect(pos / 3, pos % 3, mMinImageWidth, pos / 3 * mMinImageWidth);
                break;
        }
    }

    private void setImageRect(int row, int column, int imageWidth, int perImageWidth) {

        int left = getPaddingLeft() + column * mHorizontalSpace + column * imageWidth;
        int top = getPaddingTop() + row * mVerticalSpace + perImageWidth;
        invalidate(left, top, left + imageWidth, top + imageWidth);
        if (mImageUrls.size() == 6) {
            Log.e("setImageRect", "left=" + left + "top=" + top);
        }

    }
}
