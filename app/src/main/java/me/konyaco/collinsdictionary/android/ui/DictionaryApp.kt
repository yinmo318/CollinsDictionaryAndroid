package me.konyaco.collinsdictionary.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.konyaco.collinsdictionary.android.audio.PronunciationPlayer
import me.konyaco.collinsdictionary.android.domain.CobuildDictionarySection
import me.konyaco.collinsdictionary.android.domain.Definition
import me.konyaco.collinsdictionary.android.domain.DefinitionEntry
import me.konyaco.collinsdictionary.android.domain.ExampleSentence
import me.konyaco.collinsdictionary.android.domain.LookupResult
import me.konyaco.collinsdictionary.android.domain.ResultSource
import me.konyaco.collinsdictionary.android.domain.WordForm

@Composable
fun CollinsDictionaryApp(
    viewModel: DictionaryViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pronunciationPlayer = remember { PronunciationPlayer() }

    DisposableEffect(pronunciationPlayer) {
        onDispose { pronunciationPlayer.release() }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    DictionaryScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = { viewModel.search() },
        onAlternativeClick = { word ->
            viewModel.onQueryChange(word)
            viewModel.search(word)
        },
        onPlaySound = { url ->
            pronunciationPlayer.play(
                url = url,
                onError = { error ->
                    viewModel.reportError(error.message ?: "Could not play pronunciation audio")
                },
            )
        },
    )
}

@Composable
private fun DictionaryScreen(
    state: DictionaryUiState,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onAlternativeClick: (String) -> Unit,
    onPlaySound: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Collins Dictionary",
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
            )
            Spacer(Modifier.height(16.dp))
            SearchBar(
                query = state.query,
                isLoading = state.isLoading,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
            )
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            } else {
                Spacer(Modifier.height(12.dp))
            }

            when (val result = state.result) {
                is LookupResult.Entry -> EntryResult(
                    result = result,
                    onPlaySound = onPlaySound,
                )
                is LookupResult.NotFound -> NotFoundResult(
                    result = result,
                    onAlternativeClick = onAlternativeClick,
                )
                null -> EmptyState()
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = query,
            onValueChange = onQueryChange,
            enabled = !isLoading,
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            placeholder = { Text("Search an English word") },
        )
        Button(
            onClick = onSearch,
            enabled = query.isNotBlank() && !isLoading,
        ) {
            Text("Search")
        }
    }
}

@Composable
private fun EntryResult(
    result: LookupResult.Entry,
    onPlaySound: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SourceLine(result.source)
        }
        items(result.word.cobuildDictionary.sections) { section ->
            SectionContent(
                section = section,
                onPlaySound = onPlaySound,
            )
        }
        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SourceLine(source: ResultSource) {
    val text = when (source) {
        ResultSource.CACHE -> "Showing cached result while refreshing"
        ResultSource.NETWORK -> "Updated from Collins Online Dictionary"
    }
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
        fontSize = 13.sp,
    )
}

@Composable
private fun SectionContent(
    section: CobuildDictionarySection,
    onPlaySound: (String) -> Unit,
) {
    SelectionContainer {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            WordHeader(section, onPlaySound)
            if (section.forms.isNotEmpty()) {
                WordForms(section.forms)
            }
            DefinitionList(section.definitionEntries)
        }
    }
}

@Composable
private fun WordHeader(
    section: CobuildDictionarySection,
    onPlaySound: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = section.word,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            section.pronunciation.ipa?.let { ipa ->
                Text(
                    text = "/$ipa/",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f),
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                )
            }
            section.pronunciation.soundUrl?.let { soundUrl ->
                IconButton(onClick = { onPlaySound(soundUrl) }) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Play pronunciation",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        section.frequency?.let { frequency ->
            WordFrequency(frequency)
        }
    }
}

@Composable
private fun WordFrequency(frequency: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(5) { index ->
            val alpha = if (index < frequency) 1f else 0.22f
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
private fun WordForms(forms: List<WordForm>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel("Word forms")
        forms.forEach { form ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = form.description.ifBlank { "form" },
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
                    fontSize = 15.sp,
                )
                Text(
                    text = form.spell,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun DefinitionList(entries: List<DefinitionEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionLabel("Definitions")
        entries.forEach { entry ->
            DefinitionEntry(entry)
        }
    }
}

@Composable
private fun DefinitionEntry(entry: DefinitionEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${entry.index}.",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = entry.type.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            )
        }

        DefinitionBlock(entry.definition)
        entry.extraDefinitions.forEach { definition ->
            DefinitionBlock(definition)
        }
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DefinitionBlock(definition: Definition) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            modifier = Modifier.padding(start = 18.dp),
            text = definition.text,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            fontSize = 16.sp,
            lineHeight = 23.sp,
        )
        definition.examples.forEach { example ->
            ExampleSentence(example)
        }
        if (definition.synonyms.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(start = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                definition.synonyms.forEach { synonym ->
                    AssistChip(
                        onClick = {},
                        label = { Text(synonym) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExampleSentence(example: ExampleSentence) {
    Row(
        modifier = Modifier.padding(start = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            Modifier
                .padding(top = 10.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)),
        )
        Column {
            Text(
                text = example.sentence,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                lineHeight = 23.sp,
            )
            example.grammarPattern?.let { pattern ->
                Text(
                    text = pattern,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotFoundResult(
    result: LookupResult.NotFound,
    onAlternativeClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "No entry found for \"${result.query}\"",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
            )
        }

        SourceLine(result.source)

        if (result.alternatives.isNotEmpty()) {
            Text(
                text = "Similar words",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 14.sp,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                result.alternatives.forEach { alternative ->
                    AssistChip(
                        onClick = { onAlternativeClick(alternative) },
                        label = { Text(alternative) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 32.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Search a word to view COBUILD definitions.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Results are cached locally and refreshed from Collins when online.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 5.dp, height = 12.dp)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
        )
    }
}
