package com.github.aayman93.chatapp.repositories

import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_USERS
import com.github.aayman93.chatapp.util.Resource
import com.github.aayman93.chatapp.util.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val usersCollection = firestore.collection(KEY_COLLECTION_USERS)

    suspend fun register(username: String, email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid!!
                val user = User(uid, username, email)
                usersCollection.document(uid).set(user).await()
                Resource.Success(result)
            }
        }
    }

    suspend fun login(email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Resource.Success(result)
            }
        }
    }

}