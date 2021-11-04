package com.github.aayman93.chatapp.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.aayman93.chatapp.databinding.FragmentLoginBinding
import com.github.aayman93.chatapp.ui.auth.AuthViewModel
import com.github.aayman93.chatapp.ui.main.MainActivity
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            buttonLogin.setOnClickListener {
                viewModel.login(
                    inputEmail.text.toString(),
                    inputPassword.text.toString()
                )
            }

            tvRegister.setOnClickListener {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                )
            }
        }
    }

    private fun subscribeToObservers() {
        viewModel.loginStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                binding.buttonLogin.isVisible = true
                snackbar(it)
            },
            onLoading = {
                binding.progressBar.isVisible = true
                binding.buttonLogin.isInvisible = true
            }
        ) {
            binding.progressBar.isVisible = false
            Intent(requireContext(), MainActivity::class.java).also {
                startActivity(it)
                requireActivity().finish()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}