package phone.vishnu.sketchit.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

public class SketchView extends View {

    public static final float TOUCH_TOLERANCE = 2;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;

    public SketchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {

        paintScreen = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        pathMap = new HashMap<>();
        previousPointMap = new HashMap<>();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (int key : pathMap.keySet()) {
            canvas.drawPath(pathMap.get(key), paintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate();//Redraws the Screen

        return true;
    }

    private void touchMoved(MotionEvent event) {

        for (int i = 0; i < event.getPointerCount(); i++) {

            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (pathMap.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);

                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {

                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public void clear() {
        pathMap.clear();
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();

    }

    private void touchEnded(int pointerId) {

        Path path = pathMap.get(pointerId);
        bitmapCanvas.drawPath(path, paintLine);
        path.reset();

    }

    private void touchStarted(float x, float y, int pointerId) {

        Path path;
        Point point;

        if (pathMap.containsKey(pointerId)) {

            path = pathMap.get(pointerId);
            point = previousPointMap.get(pointerId);
        } else {
            path = new Path();
            pathMap.put(pointerId, path);
            point = new Point();
            previousPointMap.put(pointerId, point);

        }

        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;

    }

    public boolean isNull() {
        return pathMap.size() == 0 || null == previousPointMap;
        //TODO: This Is Not Working
    }
}
