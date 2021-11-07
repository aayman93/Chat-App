package com.github.aayman93.chatapp.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.aayman93.chatapp.databinding.FragmentChatBinding
import com.github.aayman93.chatapp.ui.main.adapters.ChatAdapter
import com.github.aayman93.chatapp.ui.main.viewmodels.ChatViewModel
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.hideKeyboard
import com.github.aayman93.chatapp.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val args: ChatFragmentArgs by navArgs()

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var receiverUid: String

    @Inject
    lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiverUid = args.uid
        binding.chatRecycler.adapter = chatAdapter
        binding.buttonSend.isEnabled = false

        viewModel.getUserDetails(receiverUid)
        viewModel.getMessages(receiverUid)

        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            buttonBack.setOnClickListener {
                findNavController().navigateUp()
            }

            buttonSend.setOnClickListener { v ->
                viewModel.sendMessage(
                    inputMessage.text.toString(),
                    receiverUid
                )
                inputMessage.text?.clear()
                v.hideKeyboard()
            }

            inputMessage.addTextChangedListener { text ->
                buttonSend.isEnabled = text.toString().isNotEmpty()
            }
        }
    }

    private fun subscribeToObservers() {
        viewModel.user.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { user ->
            binding.tvReceiverName.text = user.username
        })

        viewModel.messages.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                binding.progressBar.isVisible = true
                binding.chatRecycler.isVisible = false
            }
        ) { messages ->
            binding.progressBar.isVisible = false
            if (messages.isNotEmpty()) {
                binding.chatRecycler.isVisible = true
                chatAdapter.submitList(messages)
                binding.chatRecycler.smoothScrollToPosition(chatAdapter.currentList.size)
            }
        })

        viewModel.sendMessageStatus.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) {
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}