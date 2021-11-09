package com.github.aayman93.chatapp.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.github.aayman93.chatapp.R
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.databinding.FragmentEditProfileBinding
import com.github.aayman93.chatapp.ui.main.viewmodels.EditProfileViewModel
import com.github.aayman93.chatapp.util.EventObserver
import com.github.aayman93.chatapp.util.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    private lateinit var currentUser: User
    private var selectedImageUri: Uri? = null

    @Inject
    lateinit var glide: RequestManager

    private lateinit var cropContent: ActivityResultLauncher<Any?>

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
                viewModel.setSelectedImageUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            buttonBack.setOnClickListener {
                findNavController().navigateUp()
            }

            ivChooseImage.setOnClickListener {
                cropContent.launch(null)
            }

            buttonSave.setOnClickListener {
                viewModel.updateProfile(
                    currentUser,
                    inputUsername.text.toString(),
                    selectedImageUri
                )
            }
        }
    }

    private fun subscribeToObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.progressBar.isVisible = false
                snackbar(it)
                if (viewModel.isFirstLoad) {
                    findNavController().navigateUp()
                }
            },
            onLoading = { binding.progressBar.isVisible = true }
        ) { user ->
            binding.progressBar.isVisible = false
            currentUser = user

            if (selectedImageUri == null) {
                glide.load(user.profilePictureUrl).into(binding.ivProfileImage)
            }
            if (viewModel.isFirstLoad) {
                binding.inputUsername.setText(user.username)
                viewModel.isFirstLoad = false
            }
        })

        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            selectedImageUri = uri
            glide.load(uri).into(binding.ivProfileImage)
        }

        viewModel.updateProfileStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                binding.saveProgressBar.isVisible = false
                binding.buttonSave.isVisible = true
                snackbar(it)
            },
            onLoading = {
                binding.saveProgressBar.isVisible = true
                binding.buttonSave.isVisible = false
            }
        ) {
            binding.saveProgressBar.isVisible = false
            binding.buttonSave.isVisible = true
            viewModel.getUser()
            if (!viewModel.hasBeenHandled) {
                snackbar(getString(R.string.successfully_updated_profile))
                viewModel.hasBeenHandled = true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}