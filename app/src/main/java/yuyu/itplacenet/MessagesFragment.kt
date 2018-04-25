package yuyu.itplacenet

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.*
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import yuyu.itplacenet.fixtures.MessagesFixtures
import yuyu.itplacenet.models.Message
import java.util.*

class MessagesFragment : Fragment(),
        MessagesListAdapter.OnMessageClickListener<Message>,
        MessagesListAdapter.OnMessageLongClickListener<Message>,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessagesListAdapter.OnLoadMoreListener {

    private val TOTAL_MESSAGES_COUNT = 100

    private lateinit var messagesList: MessagesList
    private lateinit var messagesAdapter: MessagesListAdapter<Message>
    private lateinit var imageLoader: ImageLoader

    private lateinit var dialogId: String

    private val senderId = "0"
    private var lastLoadedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageLoader = ImageLoader { imageView, url -> Picasso.get().load(url).into(imageView) }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_messages, container, false)

        messagesList = rootView.findViewById(R.id.messagesList) as MessagesList
        initAdapter()

        val input = rootView.findViewById(R.id.input) as MessageInput
        input.setInputListener(this)
        input.setAttachmentsListener(this)

        return rootView
    }

    override fun onStart() {
        super.onStart()
        dialogId = arguments!!.getString("id")
        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true)
    }

    private fun initAdapter() {
        val holdersConfig = MessageHolders()
                .setIncomingTextLayout(R.layout.item_incoming_text_message)
                .setOutcomingTextLayout(R.layout.item_outcoming_text_message)
                .setIncomingImageLayout(R.layout.item_incoming_image_message)
                .setOutcomingImageLayout(R.layout.item_outcoming_image_message)

        messagesAdapter = MessagesListAdapter(senderId, holdersConfig, imageLoader)
        messagesAdapter.setOnMessageLongClickListener(this)
        messagesAdapter.setLoadMoreListener(this)
        messagesList.setAdapter(messagesAdapter)
    }

    override fun onSubmit(input: CharSequence): Boolean {
        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(input.toString()), true)
        return true
    }

    override fun onAddAttachments() {
        messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true)
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages()
        }
    }

    private fun loadMessages() {
        Handler().postDelayed({
            val messages = MessagesFixtures.getMessages(lastLoadedDate)
            lastLoadedDate = messages[messages.size - 1].createdAt
            messagesAdapter.addToEnd(messages, false)
        }, 1000)
    }

    override fun onMessageClick(message: Message) {
    }

    override fun onMessageLongClick(message: Message) {
    }

}
