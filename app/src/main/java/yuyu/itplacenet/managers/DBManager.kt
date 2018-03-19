package yuyu.itplacenet.managers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import yuyu.itplacenet.models.User

class DBManager {

    private val db = FirebaseFirestore.getInstance()
    private val dbUsers = "users"

    fun getUserData( userId: String ) : Task<DocumentSnapshot> {
        return db.collection(dbUsers).document(userId).get()
    }

    fun parseUserData( documentSnapshot: DocumentSnapshot ) : User {
        return when( documentSnapshot.exists() ) {
            true  -> documentSnapshot.toObject(User::class.java)
            false -> User()
        }
    }

    fun setUserData( userId: String, user: User ) : Task<Void> {
        return db.collection(dbUsers).document(userId).set(user, SetOptions.merge())
    }

    fun addUser( user: User ) : Task<DocumentReference> {
        return db.collection(dbUsers).add(user)
    }

    fun getResultId( documentReference: DocumentReference ) : String {
        return documentReference.id
    }
}