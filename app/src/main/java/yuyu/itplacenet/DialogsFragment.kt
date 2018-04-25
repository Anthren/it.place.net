package yuyu.itplacenet

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import yuyu.itplacenet.fixtures.DialogsFixtures
import yuyu.itplacenet.models.Dialog
import yuyu.itplacenet.models.Message
import yuyu.itplacenet.utils.toast


class DialogsFragment : Fragment(),
        DialogsListAdapter.OnDialogClickListener<Dialog>,
        DialogsListAdapter.OnDialogLongClickListener<Dialog> {

    private lateinit var imageLoader: ImageLoader
    private lateinit var dialogsAdapter: DialogsListAdapter<Dialog>
    private lateinit var dialogsListView: DialogsList

    private var listener: OnDialogSelectListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageLoader = ImageLoader { imageView, url -> Picasso.get().load(url).into(imageView) }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_dialogs, container, false)
        dialogsListView = rootView.findViewById(R.id.dialogsList)

        initAdapter()

        return rootView
    }

    private fun initAdapter() {
        dialogsAdapter = DialogsListAdapter(R.layout.item_dialog, imageLoader)
        dialogsAdapter.setItems(DialogsFixtures.getDialogs())

        dialogsAdapter.setOnDialogClickListener(this)
        dialogsAdapter.setOnDialogLongClickListener(this)

        dialogsListView.setAdapter(dialogsAdapter)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDialogSelectListener) {
            listener = context
        }
    }

    interface OnDialogSelectListener {
        fun onDialogSelect(dialog: Dialog)
    }

    override fun onDialogClick(dialog: Dialog) {
        listener?.onDialogSelect(dialog)
    }

    override fun onDialogLongClick(dialog: Dialog) {
        toast(dialog.dialogName)
    }

    //for example
    private fun onNewMessage(dialogId: String, message: Message) {
        val isUpdated = dialogsAdapter.updateDialogWithMessage(dialogId, message)
        if (!isUpdated) {
            //Dialog with this ID doesn't exist, so you can create new Dialog or update all dialogs list
        }
    }

    //for example
    private fun onNewDialog(dialog: Dialog) {
        dialogsAdapter.addItem(dialog)
    }

}
