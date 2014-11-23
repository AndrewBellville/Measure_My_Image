package com.example.andrew.measuremyimage;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.security.PublicKey;

/**
 * Created by Andrew on 11/10/2014.
 */
public class ImageProcessing {

    public class Pixel{
        int x = 0;
        int y = 0;

        public Pixel()
        {

        }

        public Pixel(int aX, int aY)
        {
            this.setX(aX);
            this.setY(aY);
        }

        public  Pixel(Pixel aCopy)
        {
            this.setX(aCopy.getX());
            this.setY(aCopy.getY());
        }

        public int getX() {
            return x;
        }
        public void setX(int x) {
            this.x = x;
        }
        public int getY() {
            return y;
        }
        public void setY(int y) {
            this.y = y;
        }
    }

    private static final String LOG = "ImageProcessing";
    private Bitmap bitmap;
    int[] pixels;
    private int imageHeight;
    private int imageWidth;

    public ImageProcessing(Bitmap aBitmap)
    {
        bitmap = aBitmap;
        imageHeight = bitmap.getHeight();
        imageWidth = bitmap.getWidth();
    }

    public int[] ProcessPixels(int aX, int aY)
    {
        Log.e(LOG, "Entering: ProcessPixels");
        pixels = new int[imageHeight*imageWidth];
        bitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        Pixel touchPoint = new Pixel(aX,aY);
        Pixel startPixel = new Pixel();
        Pixel currentPixel = new Pixel();

        //If edge is found try to trace the object
        if(FindFirstEdge(touchPoint,startPixel)) {

            currentPixel.setX(startPixel.getX());
            currentPixel.setY(startPixel.getY());

            do {
                if (!findNextPixel(currentPixel)) {
                    break;
                }
                pixels[currentPixel.getX() + (currentPixel.getY() * imageWidth)] = Color.rgb(100, 255, 0);
                //Log.e(LOG, "Next X[" + Integer.toString(currentPixel.getX()) + "] Y[" + Integer.toString(currentPixel.getY()) + "]");
            } while (!startPixel.equals(currentPixel));
        }

        return pixels;
    }

    private boolean FindFirstEdge(Pixel aTouchPoint, Pixel aStartPixel)
    {
        int aX = aTouchPoint.getX();
        int aY = aTouchPoint.getY();

        // look to the right from point touched until first edge is found
        for (aX++;aX < imageWidth - 1; aX++) {
            if(PixelComparison(new Pixel(aX,aY),new Pixel(aX-1,aY))) {
                pixels[aX + (aY * imageWidth)] = Color.rgb(100, 255, 0);
                Log.e(LOG,"Edge found X["+ Integer.toString(aX) + "] Y["+ Integer.toString(aY) +"]");

                aStartPixel.setX(aX);
                aStartPixel.setY(aY);
                return true;
            }
        }

        return false;
    }

    private boolean findNextPixel(Pixel aPixel)
    {
        int maxPixelSpread = 10;
        int pixelSpread = 1;

        while(pixelSpread <= maxPixelSpread) {

            //look right check its  not last location we where at and verify its  still an edge
            if (!HasBeenSeen(new Pixel(aPixel.getX() + pixelSpread, aPixel.getY())) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()),new Pixel( aPixel.getX() + pixelSpread, aPixel.getY()))) {
                //Log.e(LOG, "right");
                aPixel.setX(aPixel.getX() + pixelSpread);
                return true;
            }
            //look up right check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX() + pixelSpread, aPixel.getY() - pixelSpread)) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()),new Pixel( aPixel.getX() + pixelSpread, aPixel.getY() - pixelSpread))) {
                //Log.e(LOG, "up right");
                aPixel.setX(aPixel.getX() + pixelSpread);
                aPixel.setY(aPixel.getY() - pixelSpread);
                return true;
            }
            //look up check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX(), aPixel.getY() - pixelSpread)) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()),new Pixel( aPixel.getX(), aPixel.getY() - pixelSpread))) {
                // Log.e(LOG, "up");
                aPixel.setY(aPixel.getY() - pixelSpread);
                return true;
            }
            //look up left check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX() - pixelSpread, aPixel.getY() - pixelSpread)) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()),new Pixel( aPixel.getX() - pixelSpread, aPixel.getY() - pixelSpread))) {
                // Log.e(LOG, "up left");
                aPixel.setX(aPixel.getX() - pixelSpread);
                aPixel.setY(aPixel.getY() - pixelSpread);
                return true;
            }
            //look left check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX() - pixelSpread, aPixel.getY())) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()), new Pixel(aPixel.getX() - pixelSpread, aPixel.getY()))) {
                //Log.e(LOG, "left");
                aPixel.setX(aPixel.getX() - pixelSpread);
                return true;
            }
            //look down left check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX() - pixelSpread, aPixel.getY() + pixelSpread)) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()), new Pixel(aPixel.getX() - pixelSpread, aPixel.getY() + pixelSpread))) {
                //Log.e(LOG, "down left");
                aPixel.setX(aPixel.getX() - pixelSpread);
                aPixel.setY(aPixel.getY() + pixelSpread);
                return true;
            }
            //look down check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX(), aPixel.getY() + pixelSpread)) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()),new Pixel( aPixel.getX(), aPixel.getY() + pixelSpread))) {
                // Log.e(LOG, "down");
                aPixel.setY(aPixel.getY() + pixelSpread);
                return true;
            }
            //look down right check its  not last location we where at and verify its  still an edge
            else if (!HasBeenSeen(new Pixel(aPixel.getX() + pixelSpread, aPixel.getY() + pixelSpread)) && PixelComparison(new Pixel(aPixel.getX(), aPixel.getY()),new Pixel( aPixel.getX() + pixelSpread, aPixel.getY() + pixelSpread))) {
                //Log.e(LOG, "down right");
                aPixel.setX(aPixel.getX() + pixelSpread);
                aPixel.setY(aPixel.getY() + pixelSpread);
                return true;
            }

            pixelSpread++;
        }
        return false;
    }

    private boolean PixelComparison(Pixel aPixel, Pixel bPixel)
    {
        //Log.e(LOG, "Entering: PixelComparison");

        if(!IsValidPixel(aPixel)) return false;
        if(!IsValidPixel(bPixel)) return false;

        // Pixel location and color
        int touchedRGB = bitmap.getPixel(aPixel.getX(), aPixel.getY());
        int redValue = Color.red(touchedRGB);
        int blueValue = Color.blue(touchedRGB);
        int greenValue = Color.green(touchedRGB);
        // Left Pixel location and color
        touchedRGB = bitmap.getPixel(bPixel.getX(), bPixel.getY());
        int redValue2 = Color.red(touchedRGB);
        int blueValue2 = Color.blue(touchedRGB);
        int greenValue2 = Color.green(touchedRGB);
        // diff color
        int newRedValue = Math.abs(redValue-redValue2);
        int newBlueValue = Math.abs(blueValue-blueValue2);
        int newGreenValue = Math.abs(greenValue-greenValue2);

        // New color is difference between pixel and left neighbor
        if(newRedValue + newBlueValue + newGreenValue > 25) {
            //Log.e(LOG,"Edge found X["+ Integer.toString(aX) + "] Y["+ Integer.toString(aY) +"]");
            return true;
        }
        return  false;
    }

    private boolean HasBeenSeen(Pixel aPixel)
    {
        if(!IsValidPixel(aPixel)) return false;

        if (pixels[aPixel.getX() + (aPixel.getY() * imageWidth)] == Color.rgb(100, 255, 0)) return true;

        return false;
    }

    private boolean IsValidPixel(Pixel aPixel)
    {
        if(aPixel.getX() < 0 || aPixel.getX() > imageWidth-1 || aPixel.getY() < 0  || aPixel.getY() > imageHeight-1) return false;

        return true;
    }

    public int[] GenericEdgeDetection (Bitmap aBitmap)
    {
        int[] pixels = new int[aBitmap.getHeight()*aBitmap.getWidth()];
        aBitmap.getPixels(pixels, 0, aBitmap.getWidth(), 0, 0, aBitmap.getWidth(), aBitmap.getHeight());

        for (int x = 1;x < aBitmap.getWidth(); x++) {
            for (int ly = 0; ly < aBitmap.getHeight(); ly++ ) {
                // Pixel location and color
                int touchedRGB = aBitmap.getPixel(x, ly);
                int redValue = Color.red(touchedRGB);
                int blueValue = Color.blue(touchedRGB);
                int greenValue = Color.green(touchedRGB);
                // Left Pixel location and color
                touchedRGB = aBitmap.getPixel(x-1, ly);
                int redValue2 = Color.red(touchedRGB);
                int blueValue2 = Color.blue(touchedRGB);
                int greenValue2 = Color.green(touchedRGB);
                // diff color
                int newRedValue = Math.abs(redValue-redValue2);
                int newBlueValue = Math.abs(blueValue-blueValue2);
                int newGreenValue = Math.abs(greenValue-greenValue2);

                // New color is difference between pixel and left neighbor
                if(newRedValue + newBlueValue + newGreenValue > 20) {
                    //Log.e(LOG,"touched color: " + Integer.toString(newRedValue + newBlueValue + newGreenValue));
                    pixels[x + (ly * aBitmap.getWidth())] = Color.rgb(100, 255, 0);
                }
            }
        }

        for (int ly = 1;ly < aBitmap.getHeight(); ly++) {
            for (int x = 0; x < aBitmap.getWidth(); x++ ) {
                // Pixel location and color
                int touchedRGB = aBitmap.getPixel(x, ly);
                int redValue = Color.red(touchedRGB);
                int blueValue = Color.blue(touchedRGB);
                int greenValue = Color.green(touchedRGB);
                // Left Pixel location and color
                touchedRGB = aBitmap.getPixel(x, ly-1);
                int redValue2 = Color.red(touchedRGB);
                int blueValue2 = Color.blue(touchedRGB);
                int greenValue2 = Color.green(touchedRGB);
                // diff color
                int newRedValue = Math.abs(redValue-redValue2);
                int newBlueValue = Math.abs(blueValue-blueValue2);
                int newGreenValue = Math.abs(greenValue-greenValue2);

                // New color is difference between pixel and left neighbor
                if(newRedValue + newBlueValue + newGreenValue > 20) {
                    //Log.e(LOG,"touched color: " + Integer.toString(newRedValue + newBlueValue + newGreenValue));
                    pixels[x + (ly * aBitmap.getWidth())] = Color.rgb(100, 255, 0);
                }
            }
        }



        return pixels;
    }

}
