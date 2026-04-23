package com.cat_together.meta.utils

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

class BitmapParcelable(private val bitmap: Bitmap) : Parcelable {

    fun getBitmap(): Bitmap = bitmap

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        bitmap.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BitmapParcelable> {
        override fun createFromParcel(parcel: Parcel): BitmapParcelable {
            val bitmap = Bitmap.CREATOR.createFromParcel(parcel)
            return BitmapParcelable(bitmap)
        }

        override fun newArray(size: Int): Array<BitmapParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
