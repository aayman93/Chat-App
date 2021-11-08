package com.github.aayman93.chatapp.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.github.aayman93.chatapp.R
import com.github.aayman93.chatapp.databinding.FragmentHomeBinding
import com.github.aayman93.chatapp.ui.auth.AuthActivity
import com.github.aayman93.chatapp.ui.main.adapters.ConversationsAdapter
import com.github.aayman93.chatapp.ui.main.viewmodels.HomeViewModel
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter

    @Inject
    lateinit var glide: RequestManager

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.conversationsRecycler.adapter = conversationsAdapter
        viewModel.getCurrentUserDetails()
        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            fab.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToUsersFragment()
                )
            }

            ivMore.setOnClickListener { v ->
                PopupMenu(requireContext(), v).apply {
                    setOnMenuItemClickListener(this@HomeFragment)
                    inflate(R.menu.menu_popup)
                    show()
                }
            }

            conversationsAdapter.setOnConversationClickListener { conversation ->
                val id = conversation.chatReceiverId!!
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToChatFragment(id)
                )
            }
        }
    }

    private fun subscribeToObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { user ->
            glide.load(user.profilePictureUrl).into(binding.ivProfileImage)
        })
        viewModel.conversations.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                binding.progressBar.isVisible = true
                binding.conversationsRecycler.isVisible = false
            }
        ) { conversations ->
            binding.progressBar.isVisible = false
            if (conversations.isNotEmpty()) {
                binding.conversationsRecycler.isVisible = true
                conversationsAdapter.submitList(conversations)
            }
        })
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                Intent(requireContext(), AuthActivity::class.java).also {
                    startActivity(it)
                    requireActivity().finish()
                }
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}