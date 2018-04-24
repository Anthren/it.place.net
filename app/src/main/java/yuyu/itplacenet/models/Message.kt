package yuyu.itplacenet.models

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.MessageContentType
import java.util.Date


class Message @JvmOverloads constructor(private val id: String,
                                        private val user: Author,
                                        private var text: String?,
                                        private var createdAt: Date? = Date()
) : IMessage,
        MessageContentType.Image,
        MessageContentType {

    private var image: Image? = null
    var voice: Voice? = null

    val status: String
        get() = "Sent"

    override fun getId(): String {
        return id
    }

    override fun getText(): String? {
        return text
    }

    override fun getCreatedAt(): Date? {
        return createdAt
    }

    override fun getUser(): Author {
        return this.user
    }

    override fun getImageUrl(): String? {
        return image?.url
    }

    fun setText(text: String) {
        this.text = text
    }

    fun setCreatedAt(createdAt: Date) {
        this.createdAt = createdAt
    }

    fun setImage(image: Image) {
        this.image = image
    }

    class Image(val url: String)

    class Voice(val url: String, val duration: Int)
}
