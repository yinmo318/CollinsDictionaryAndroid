package me.konyaco.collinsdictionary.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.konyaco.collinsdictionary.android.data.DictionaryRepository
import me.konyaco.collinsdictionary.android.domain.LookupResult
import me.konyaco.collinsdictionary.android.domain.ResultSource
import me.konyaco.collinsdictionary.android.storage.CacheStore

data class DictionaryUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val result: LookupResult? = null,
    val errorMessage: String? = null,
)

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DictionaryRepository(
        cacheStore = CacheStore(File(application.filesDir, "dictionary-cache")),
    )

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState = _uiState.asStateFlow()

    private var lookupJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search(query: String = uiState.value.query) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return

        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    query = normalizedQuery,
                    isLoading = true,
                    errorMessage = null,
                )
            }

            repository.lookup(normalizedQuery)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.readableMessage(),
                        )
                    }
                }
                .collect { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = result.source != ResultSource.NETWORK,
                            result = result,
                            errorMessage = null,
                        )
                    }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun reportError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun Throwable.readableMessage(): String {
        return message?.takeIf { it.isNotBlank() } ?: "Unknown error"
    }
}
