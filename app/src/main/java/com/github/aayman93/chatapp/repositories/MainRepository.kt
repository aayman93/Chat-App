package com.github.aayman93.chatapp.repositories

import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_USERS
import com.github.aayman93.chatapp.util.Resource
import com.github.aayman93.chatapp.util.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityRetainedScoped
class MainRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getUsers(): Resource<List<User>> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val uid = auth.uid!!
                val users = firestore.collection(KEY_COLLECTION_USERS)
                    .get().await()
                    .toObjects<User>()
                    .filter { user -> user.uid != uid }
                    .sortedBy { user -> user.username }
                Resource.Success(users)
            }
        }
    }
}