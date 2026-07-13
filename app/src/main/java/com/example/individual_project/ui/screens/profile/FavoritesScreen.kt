package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val userRepository  : UserRepository,
    private val eventRepository : EventRepository,
    private val firebaseAuth    : FirebaseAuth
) : ViewModel() {

    private val uid: String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(UiState<List<Event>>())
    val state: StateFlow<UiState<List<Event>>> = _state.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        if (uid.isBlank()) {
            _state.value = UiState(error = "Not logged in")
            return
        }
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)

            val idsResult = userRepository.getFavoriteEventIds(uid)
            if (idsResult is Resource.Error) {
                _state.value = UiState(error = idsResult.message)
                return@launch
            }

            val ids = (idsResult as Resource.Success).data
            if (ids.isEmpty()) {
                _state.value = UiState(data = emptyList())
                return@launch
            }

            // Parallel event fetch
            val events = ids
                .map { id -> async { eventRepository.fetchEventDetails(id) } }
                .awaitAll()
                .filterIsInstance<Resource.Success<Event>>()
                .map { it.data }

            _state.value = UiState(data = events)
        }
    }

    fun removeFavorite(eventId: String) {
        viewModelScope.launch {
            userRepository.removeFavorite(uid, eventId)
            loadFavorites()
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel    : FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = TmNavyBlue,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> LoadingView()
                state.hasError  -> ErrorView(
                    message = state.error ?: "Failed to load favorites",
                    onRetry = { viewModel.loadFavorites() }
                )
                state.data != null -> {
                    val events = state.data!!
                    if (events.isEmpty()) {
                        EmptyState(
                            emoji       = "❤️",
                            title       = "No Favorites Yet",
                            subtitle    = "Events you favorite will appear here",
                            actionLabel = "Browse Events",
                            onAction    = { navController.navigate(Screen.Dashboard.route) }
                        )
                    } else {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(Spacing.screenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                            items(events, key = { it.id }) { event ->
                                FavoriteEventCard(
                                    event    = event,
                                    onClick  = {
                                        navController.navigate(
                                            Screen.EventDetail.createRoute(event.id)
                                        )
                                    },
                                    onRemove = { viewModel.removeFavorite(event.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(Spacing.md)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteEventCard(
    event   : Event,
    onClick : () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape           = MaterialTheme.shapes.large,
        color           = MaterialTheme.colorScheme.surface,
        tonalElevation  = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (event.imageUrl.isNotBlank()) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape    = MaterialTheme.shapes.medium,
                    color    = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AsyncImage(
                        model             = event.imageUrl,
                        contentDescription = event.title,
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md)
            ) {
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                if (event.venue.isNotBlank()) {
                    Text(
                        text     = "${event.venue}, ${event.city}".trimEnd(',', ' '),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (event.date.isNotBlank()) {
                    Text(
                        text  = event.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text       = PriceFormatter.format(event.price),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TmGold
                )
            }

            // Remove from favorites
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.FavoriteBorder, null,
                    tint     = TmError,
                    modifier = Modifier.size(Spacing.iconLg)
                )
            }
        }
    }
}
