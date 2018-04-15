package yuyu.itplacenet.models

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class FriendItem(val id: String,
                 var latLng: LatLng,
                 var name: String,
                 var snippet: String,
                 var photo: Bitmap)
: ClusterItem {

    override fun getPosition(): LatLng {
        return latLng
    }
}