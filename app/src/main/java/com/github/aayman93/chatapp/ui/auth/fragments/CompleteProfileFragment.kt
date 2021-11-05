package com.github.aayman93.chatapp.ui.auth.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.RequestManager
import com.github.aayman93.chatapp.databinding.FragmentCompleteProfileBinding
import com.github.aayman93.chatapp.ui.auth.AuthViewModel
import com.github.aayman93.chatapp.ui.main.MainActivity
import com.github.aayman93.chatapp.util.Constants.DEFAULT_PROFILE_PICTURE_URL
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CompleteProfileFragment : Fragment() {

    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var glide: RequestManager

    private lateinit var cropContent: ActivityResultLauncher<Any?>
    private var currentImageUri: Uri? = null

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract) {
            it?.let { uri ->
                viewModel.setCurrentImageUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSave.isEnabled = false
        glide.load(DEFAULT_PROFILE_PICTURE_URL).into(binding.ivProfileImage)
        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            ivChooseImage.setOnClickListener {
                cropContent.launch(null)
            }

            buttonSave.setOnClickListener {
                currentImageUri?.let { uri ->
                    viewModel.completeProfile(uri)
                }
            }

            tvSkip.setOnClickListener { navigateToHome() }
        }
    }

    private fun subscribeToObservers() {
        viewModel.currentImageUri.observe(viewLifecycleOwner) { uri ->
            currentImageUri = uri
            binding.buttonSave.isEnabled = true
            glide.load(uri).into(binding.ivProfileImage)
        }

        viewModel.completeProfileStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                binding.buttonSave.isVisible = true
                snackbar(it)
            },
            onLoading = {
                binding.progressBar.isVisible = true
                binding.buttonSave.isInvisible = true
            }
        ) {
            binding.progressBar.isVisible = false
            navigateToHome()
        })
    }

    private fun navigateToHome() {
        Intent(requireContext(), MainActivity::class.java).also {
            startActivity(it)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}