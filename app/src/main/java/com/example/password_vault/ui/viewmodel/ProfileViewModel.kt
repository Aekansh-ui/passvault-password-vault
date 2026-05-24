package com.example.password_vault.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.repository.ProfileRepository
import com.example.password_vault.domain.model.ProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository
) : ViewModel() {

    val profile: StateFlow<ProfileData> = profileRepo.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileData("", "", null))

    val editName = MutableStateFlow("")
    val editImageUri = MutableStateFlow<Uri?>(null)
    val isSaving = MutableStateFlow(false)

    fun prepareEdit(current: ProfileData) {
        editName.value = current.displayName
        editImageUri.value = null
    }

    fun saveProfile() {
        viewModelScope.launch {
            isSaving.value = true
            try {
                profileRepo.saveProfile(editName.value, "", editImageUri.value)
            } finally {
                isSaving.value = false
            }
        }
    }

    fun loadProfileImageBytes(): ByteArray? {
        val path = profile.value.imagePath ?: return null
        return profileRepo.loadDecryptedImageBytes(path)
    }
}
