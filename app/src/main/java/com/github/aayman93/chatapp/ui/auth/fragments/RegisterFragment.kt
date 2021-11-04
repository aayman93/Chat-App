package com.github.aayman93.chatapp.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.aayman93.chatapp.databinding.FragmentRegisterBinding
import com.github.aayman93.chatapp.ui.auth.AuthViewModel
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            buttonRegister.setOnClickListener {
                viewModel.register(
                    inputUsername.text.toString(),
                    inputEmail.text.toString(),
                    inputPassword.text.toString(),
                    inputConfirmPassword.text.toString()
                )
            }
        }
    }

    private fun subscribeToObservers() {
        viewModel.registerStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                binding.buttonRegister.isVisible = true
                snackbar(it)
            },
            onLoading = {
                binding.progressBar.isVisible = true
                binding.buttonRegister.isInvisible = true
            }
        ) {
            binding.progressBar.isVisible = false
            binding.buttonRegister.isVisible = true
            snackbar("Successfully Registered!")
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}