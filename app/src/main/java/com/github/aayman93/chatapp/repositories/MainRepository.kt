package com.github.aayman93.chatapp.repositories

import com.github.aayman93.chatapp.data.models.ChatMessage
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.util.Constants.KEY_CHAT_RECEIVER_ID
import com.github.aayman93.chatapp.util.Constants.KEY_CHAT_SENDER_ID
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_CHAT
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_USERS
import com.github.aayman93.chatapp.util.Resource
import com.github.aayman93.chatapp.util.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
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

    suspend fun getUser(uid: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val user = firestore.collection(KEY_COLLECTION_USERS)
                    .document(uid)
                    .get().await().toObject<User>() ?: throw IllegalStateException()
                Resource.Success(user)
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun listenToSentMessages(receiverUid: String): Flow<List<ChatMessage>> {
        return withContext(Dispatchers.IO) {
            callbackFlow {
                val userUid = auth.uid!!
                val query = firestore.collection(KEY_COLLECTION_CHAT)
                    .whereEqualTo(KEY_CHAT_SENDER_ID, userUid)
                    .whereEqualTo(KEY_CHAT_RECEIVER_ID, receiverUid)

                val sentMessages = mutableListOf<ChatMessage>()
                val subscription = query.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        snapshot.documentChanges.mapNotNull { documentChange ->
                            if (documentChange.type == DocumentChange.Type.ADDED) {
                                val message = documentChange.document.toObject<ChatMessage>()
                                sentMessages.add(message)
                            }
                        }
                        trySend(sentMessages.toList())
                    }
                }

                awaitClose {
                    subscription.remove()
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun listenToReceivedMessages(senderUid: String): Flow<List<ChatMessage>> {
        return withContext(Dispatchers.IO) {
            callbackFlow {
                val userUid = auth.uid!!
                val sender = getUser(senderUid).data!!

                val query = firestore.collection(KEY_COLLECTION_CHAT)
                    .whereEqualTo(KEY_CHAT_SENDER_ID, senderUid)
                    .whereEqualTo(KEY_CHAT_RECEIVER_ID, userUid)

                val receivedMessages = mutableListOf<ChatMessage>()
                val subscription = query.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        snapshot.documentChanges.mapNotNull { documentChange ->
                            if (documentChange.type == DocumentChange.Type.ADDED) {
                                val message = documentChange.document.toObject<ChatMessage>()
                                message.isReceived = true
                                message.receiverImageUrl = sender.profilePictureUrl
                                receivedMessages.add(message)
                            }
                        }
                        trySend(receivedMessages.toList())
                    }
                }

                awaitClose {
                    subscription.remove()
                }
            }
        }
    }

    suspend fun sendMessage(message: String, receiverUid: String): Resource<ChatMessage> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val messageId = UUID.randomUUID().toString()
                val senderId = auth.uid!!
                val chatMessage = ChatMessage(
                    messageId = messageId,
                    senderId = senderId,
                    receiverId = receiverUid,
                    message = message
                )
                firestore.collection(KEY_COLLECTION_CHAT).document(messageId)
                    .set(chatMessage).await()
                Resource.Success(chatMessage)
            }
        }
    }
}