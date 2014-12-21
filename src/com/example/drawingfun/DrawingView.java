package com.example.drawingfun;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.TypedValue;

public class DrawingView extends View implements SensorEventListener {
	private boolean drawAcceleration = false;
	
	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	private float brushSize, lastBrushSize;
	private boolean erase = false;
	
	private float lastX, lastY, lastZ;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private long lastUpdateTime = 0;

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
	}

	private void setupDrawing() {
		//get drawing area setup for interaction  
		drawPath = new Path();
		drawPaint = new Paint();

		// set initial color
		drawPaint.setColor(paintColor);
		
		// set initial path properties
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);

		canvasPaint = new Paint(Paint.DITHER_FLAG);
		
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		//view given size
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//draw view
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//detect user touch     
		float touchX = event.getX();
		float touchY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			drawAcceleration = true;
			lastX = touchX;
			lastY = touchY;
			drawPath.moveTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			//drawPath.lineTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_UP:
			drawAcceleration = false;
			drawCanvas.drawPath(drawPath, drawPaint);
			drawPath.reset();
			break;
		default:
			return false;
		}

		invalidate();
		return true;
	}

	public void setColor(String newColor){
		//set color     
		invalidate();

		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}
	
	public void setBrushSize(float newSize) {
		//update size
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
		brushSize = pixelAmount;
		drawPaint.setStrokeWidth(brushSize);	
	}
	
	public void setLastBrushSize(float lastSize) {
	    lastBrushSize = lastSize;
	}
	
	public float getLastBrushSize() {
	    return lastBrushSize;
	}
	
	public void setErase(boolean isErase) {
		//set erase true or false  
		erase = isErase;
		
		if (erase)
			drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else 
			drawPaint.setXfermode(null);
	}
	
	public void startNew() {
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    invalidate();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		long lastTime = lastUpdateTime;
		lastUpdateTime = System.currentTimeMillis();
		
		double timeDelta = (lastUpdateTime - lastTime) / 100.00;
		
		double deltaX = (0.5 * event.values[0]) * Math.pow(timeDelta, 0);
		double deltaY = (0.5 * event.values[1]) * Math.pow(timeDelta, 0);
		double deltaZ = (0.5 * event.values[2]) * Math.pow(timeDelta, 0);
		
		lastX -= deltaX * 10;
		lastY += deltaY * 10;
		lastZ += deltaZ * 10;
		
		System.out.println(timeDelta);
		
		if(drawAcceleration) { 
			drawPath.lineTo(lastX, lastY);
			drawCanvas.drawPath(drawPath, drawPaint);
			invalidate();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	public void setupSensorManager(SensorManager sensorManager) {
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			// success! we have an accelerometer
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			// fail! we don't have an accelerometer
			System.out.println("Error: No accelerometer.");
		}
	}

}
