package com.github.aayman93.chatapp.repositories

import com.github.aayman93.chatapp.data.models.ChatMessage
import com.github.aayman93.chatapp.data.models.Conversation
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.util.Constants.KEY_RECEIVER_ID
import com.github.aayman93.chatapp.util.Constants.KEY_SENDER_ID
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_CHAT
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_CONVERSATIONS
import com.github.aayman93.chatapp.util.Constants.KEY_COLLECTION_USERS
import com.github.aayman93.chatapp.util.Constants.KEY_CONVERSATION_DATE
import com.github.aayman93.chatapp.util.Constants.KEY_CONVERSATION_LAST_MESSAGE
import com.github.aayman93.chatapp.util.Resource
import com.github.aayman93.chatapp.util.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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

    suspend fun getCurrentUser(): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val user = firestore.collection(KEY_COLLECTION_USERS)
                    .document(auth.uid!!)
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
                    .whereEqualTo(KEY_SENDER_ID, userUid)
                    .whereEqualTo(KEY_RECEIVER_ID, receiverUid)

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
                    .whereEqualTo(KEY_SENDER_ID, senderUid)
                    .whereEqualTo(KEY_RECEIVER_ID, userUid)

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

    suspend fun sendMessage(
        message: String, receiverUid: String, conversationId: String?
    ): Resource<ChatMessage> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val messageId = UUID.randomUUID().toString()
                val senderId = auth.uid!!
                val convId = conversationId ?: createConversation(senderId, receiverUid)
                val chatMessage = ChatMessage(
                    messageId = messageId,
                    senderId = senderId,
                    receiverId = receiverUid,
                    message = message,
                    conversationId = convId
                )
                firestore.collection(KEY_COLLECTION_CHAT).document(messageId)
                    .set(chatMessage).await()
                updateConversation(convId, message, chatMessage.date)
                Resource.Success(chatMessage)
            }
        }
    }

    private suspend fun createConversation(senderId: String, receiverId: String): String {
        return withContext(Dispatchers.IO) {
            val conversationId = UUID.randomUUID().toString()
            val conversation = Conversation(
                conversationId = conversationId,
                senderId = senderId,
                receiverId = receiverId
            )
            firestore.collection(KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId).set(conversation).await()

            conversationId
        }
    }

    private suspend fun updateConversation(conversationId: String, message: String, date: Date) {
        withContext(Dispatchers.IO) {
            val updates = mapOf<String, Any>(
                KEY_CONVERSATION_LAST_MESSAGE to message,
                KEY_CONVERSATION_DATE to date
            )
            firestore.collection(KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(updates).await()
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun listenToStartedConversations(): Flow<List<Conversation>> {
        return withContext(Dispatchers.IO) {
            callbackFlow {
                val userUid = auth.uid!!
                val query = firestore.collection(KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(KEY_SENDER_ID, userUid)

                val subscription = query.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val startedConversations = mutableListOf<Conversation>()
                        snapshot.documents.mapNotNull { document ->
                            val conversation = document.toObject<Conversation>()!!
                            conversation.chatReceiverId = conversation.receiverId
                            firestore.collection(KEY_COLLECTION_USERS)
                                .document(conversation.receiverId)
                                .get()
                                .addOnSuccessListener {
                                    val user = it.toObject<User>()!!
                                    conversation.name = user.username
                                    conversation.imageUrl = user.profilePictureUrl
                                }
                            startedConversations.add(conversation)
                        }
                        trySend(startedConversations.toList())
                    }
                }

                awaitClose {
                    subscription.remove()
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun listenToReceivedConversations(): Flow<List<Conversation>> {
        return withContext(Dispatchers.IO) {
            callbackFlow {
                val userUid = auth.uid!!
                val query = firestore.collection(KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(KEY_RECEIVER_ID, userUid)

                val subscription = query.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val receivedConversations = mutableListOf<Conversation>()
                        snapshot.documents.mapNotNull { document ->
                            val conversation = document.toObject<Conversation>()!!
                            conversation.chatReceiverId = conversation.senderId
                            firestore.collection(KEY_COLLECTION_USERS)
                                .document(conversation.senderId)
                                .get()
                                .addOnSuccessListener {
                                    val user = it.toObject<User>()!!
                                    conversation.name = user.username
                                    conversation.imageUrl = user.profilePictureUrl
                                }
                            receivedConversations.add(conversation)
                        }
                        trySend(receivedConversations.toList())
                    }
                }

                awaitClose {
                    subscription.remove()
                }
            }
        }
    }
}