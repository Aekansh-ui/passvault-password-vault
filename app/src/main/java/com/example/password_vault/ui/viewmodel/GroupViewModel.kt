package com.example.password_vault.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.repository.VaultRepository
import com.example.password_vault.domain.model.AccountRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    repo: VaultRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    private val groupId: Long = savedState["groupId"] ?: 0L

    val accounts: StateFlow<List<AccountRow>> = repo.observeAccounts(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
