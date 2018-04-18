package yuyu.itplacenet.models

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class FriendItem(val id: String,
                 val latLng: LatLng,
                 val name: String,
                 val snippet: String,
                 val photo: Bitmap,
                 val photoHash: String)
: ClusterItem {

    override fun getPosition(): LatLng {
        return latLng
    }
}