package com.github.aayman93.chatapp.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.aayman93.chatapp.databinding.FragmentUsersBinding
import com.github.aayman93.chatapp.ui.main.adapters.UsersAdapter
import com.github.aayman93.chatapp.ui.main.viewmodels.UsersViewModel
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()

    @Inject
    lateinit var usersAdapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.usersRecycler.adapter = usersAdapter
        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            buttonBack.setOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun subscribeToObservers() {
        viewModel.users.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                binding.progressBar.isVisible = true
                binding.usersRecycler.isVisible = false
            }
        ) { users ->
            binding.progressBar.isVisible = false
            if (users.isNotEmpty()) {
                binding.usersRecycler.isVisible = true
                usersAdapter.submitList(users)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}