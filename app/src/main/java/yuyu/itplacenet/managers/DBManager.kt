package yuyu.itplacenet.managers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import yuyu.itplacenet.models.User

class DBManager {

    private val db = FirebaseFirestore.getInstance()
    private val dbUsers = "users"


    fun getResultId(documentReference: DocumentReference): String {
        return documentReference.id
    }


    fun getUserData(userId: String): Task<DocumentSnapshot> {
        return db.collection(dbUsers).document(userId).get()
    }

    fun parseUserData(documentSnapshot: DocumentSnapshot): User? {
        return documentSnapshot.toObject(User::class.java)
    }

    fun setUserData(userId: String, user: User): Task<Void> {
        return db.collection(dbUsers).document(userId).set(user, SetOptions.merge())
    }

    fun addUser(user: User): Task<DocumentReference> {
        return db.collection(dbUsers).add(user)
    }

    fun updateUserData(userId: String, arr: Map<String, Any>): Task<Void> {
        return db.collection(dbUsers).document(userId).update(arr)
    }


    fun allUsersListener(
            addedCallback:    ((String,User) -> Unit)? = null,
            modifiedCallback: ((String,User) -> Unit)? = null,
            removedCallback:  ((String) -> Unit)? = null,
            failureCallback:  ((String) -> Unit)? = null
    ) : ListenerRegistration {
        return db.collection(dbUsers)
                .addSnapshotListener { snapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (e == null) {
                        var user: User
                        var userId: String

                        snapshots?.documentChanges?.forEach {
                            user = it.document.toObject(User::class.java)
                            userId = it.document.id

                            when( it.type ) {
                                DocumentChange.Type.ADDED -> {
                                    addedCallback?.invoke(userId, user)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    modifiedCallback?.invoke(userId, user)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    removedCallback?.invoke(userId)
                                }
                            }
                        }
                    } else {
                        failureCallback?.invoke(e.toString())
                    }
                }
    }
}