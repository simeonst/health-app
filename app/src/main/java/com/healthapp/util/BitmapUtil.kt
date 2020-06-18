package com.healthapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import timber.log.Timber

fun View.takeViewScreenShot(scaleSize: Float = 1f): Bitmap {
    if (width <= 0 || height <= 0) {
        Timber.e("Taking a screenshot of a view $this with height: $height and width: $width")
    }
    val w = if (width > 0) width else 4
    val h = if (height > 0) height else 4
    val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    draw(c)
    return if (scaleSize == 1f) b else Bitmap.createScaledBitmap(b, (w * scaleSize).toInt(), (h * scaleSize).toInt(), true)
}

fun View.takeRootScreenShot(scaleSize: Float = 1f): Bitmap {
    return rootView.findViewById<View>(android.R.id.content).takeViewScreenShot(scaleSize)
}

fun View.takeDecorScreenShot(scaleSize: Float = 1f): Bitmap {
    return rootView.takeViewScreenShot(scaleSize)
}


//Had to use standard android lib as the androidx lib is failing for API Lvl 22 on RenderScript#create(context: Context) method
fun Bitmap.blurBitmap(radius: Float, appContext: Context, rsIn: RenderScript? = null, bsIn: ScriptIntrinsicBlur? = null): Bitmap {

    //no need to blur...
    if (radius < 0) {
        return this
    }

    //Let's create an empty bitmap with the same size of the bitmap we want to blur
    val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    //Instantiate a new Renderscript
    val rs = rsIn ?: RenderScript.create(appContext)

    //Create an Intrinsic Blur Script using the Renderscript
    val blurScript = bsIn ?: ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

    //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
    val allIn = Allocation.createFromBitmap(rs, this)
    val allOut = Allocation.createFromBitmap(rs, outBitmap)

    //Set the radius of the blur (accommodate for blur radii > 25)
    blurScript.setRadius(if (radius > 25) 25f else radius)

    //Perform the Renderscript
    blurScript.setInput(allIn)
    blurScript.forEach(allOut)

    //Copy the final bitmap created by the out Allocation to the outBitmap
    allOut.copyTo(outBitmap)

    //recycle the original bitmap
    recycle()

    //After finishing everything, we destroy the Renderscript.
    rs.destroy()

    if (radius > 25) {
        //recursively blur
        return outBitmap.blurBitmap(radius - 25f, appContext, rs, blurScript)
    }

    return outBitmap
}