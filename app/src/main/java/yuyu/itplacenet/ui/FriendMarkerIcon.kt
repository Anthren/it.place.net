package yuyu.itplacenet.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import yuyu.itplacenet.R

abstract class FriendMarkerIcon( activity: Activity ) {

    private val iconLayout = activity.layoutInflater.inflate(R.layout.friend_marker_icon, null, false)

    val photoView1: ImageView = iconLayout.findViewById(R.id.friend_photo_1)
    val photoView2: ImageView = iconLayout.findViewById(R.id.friend_photo_2)
    val photoView3: ImageView = iconLayout.findViewById(R.id.friend_photo_3)
    val additionalCount: TextView = iconLayout.findViewById(R.id.additional_count)

    fun makeIcon(): Bitmap {
        val width  = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        iconLayout.measure(width, height)
        iconLayout.layout(0, 0, iconLayout.measuredWidth, iconLayout.measuredHeight)

        val screenshot = Bitmap.createBitmap(iconLayout.measuredWidth, iconLayout.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        iconLayout.draw(canvas)
        return screenshot
    }

}

class FriendMarkerIconBuilder( activity: Activity,
                               private val photo: Bitmap
) : FriendMarkerIcon(activity) {

    fun build() : Bitmap {
        photoView1.setImageBitmap(photo)
        photoView2.visibility = View.GONE
        photoView3.visibility = View.GONE
        return this.makeIcon()
    }
}

// TODO: Как-то странно, что класс FriendClusterIconBuilder наследуется от FriendMarkerIcon
// судя по названию, это как будто бы разные сущности
class FriendClusterIconBuilder( activity: Activity,
                                private val photos: ArrayList<Bitmap>
) : FriendMarkerIcon(activity) {

    fun build() : Bitmap {
        photoView1.setImageBitmap(photos[0])

        when {
            photos.size > 1 -> {
                photoView2.setImageBitmap(photos[1])
                photoView2.visibility = View.VISIBLE
            }
            else -> {
                photoView2.visibility = View.GONE
            }
        }

        val other = photos.size - 2
        when {
            other > 1 -> {
                additionalCount.text = "+${other.toString()}"
                additionalCount.visibility = View.VISIBLE
                photoView3.visibility = View.VISIBLE
            }
            other == 1 -> {
                photoView3.setImageBitmap(photos[2])
                photoView3.visibility = View.VISIBLE
                additionalCount.visibility = View.GONE
            }
            else -> {
                photoView3.visibility = View.GONE
                additionalCount.visibility = View.GONE
            }
        }

        return this.makeIcon()
    }
}