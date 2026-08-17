package com.example.contactimporter

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.contactimporter.data.CharacterMetadata
import com.example.contactimporter.data.ContactConflict
import com.example.contactimporter.data.ConflictResolution
import com.example.contactimporter.data.ImportResult
import com.example.contactimporter.data.ThemeConfig
import com.example.contactimporter.theme.ThemeEngine
import com.example.contactimporter.ui.AsyncUriImage
import com.example.contactimporter.ui.components.*
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
            val characters by viewModel.availableCharacters.collectAsStateWithLifecycle()
            val isDark = isSystemInDarkTheme()
            
            val activeCharacter = remember(themeConfig.characterId, characters) {
                characters.find { it.id == themeConfig.characterId }
            }
            
            val themeTokens = ThemeEngine.render(activeCharacter, themeConfig, isDark)
            
            CharacterThemeProvider(tokens = themeTokens, darkTheme = isDark) {
                HomeScreen(
                    viewModel = viewModel,
                    themeConfig = themeConfig
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    themeConfig: ThemeConfig
) {
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val characters by viewModel.availableCharacters.collectAsStateWithLifecycle()
    val loadError = viewModel.characterLoadError
    val context = LocalContext.current
    var showThemePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_CONTACTS] == true &&
            permissions[Manifest.permission.WRITE_CONTACTS] == true
        if (granted) {
            val state = viewModel.importState.value
            if (state is ImportState.ReadyToImport) {
                viewModel.startImport(state.entries)
            }
        } else {
            Toast.makeText(context, "Permissions required for contact sync", Toast.LENGTH_SHORT).show()
        }
    }

    val jsonPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.parseJsonFile(it) } }

    ThemedBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CONTACT",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = "IMPORTER",
                                modifier = Modifier.alpha(0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                            Switch(
                                checked = themeConfig.themeEnabled,
                                onCheckedChange = { viewModel.toggleTheme(it) },
                                modifier = Modifier.scale(0.8f),
                                    thumbContent = {
                                        if (themeConfig.themeEnabled) {
                                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { showThemePicker = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Open Theme Picker",
                                    tint = if (themeConfig.themeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                HeroSection(modifier = Modifier.fillMaxWidth().height(200.dp))

                Spacer(modifier = Modifier.height(16.dp))

                CharacterCarousel(
                    characters = characters,
                    loadError = loadError,
                    selectedId = themeConfig.characterId,
                    onSelect = { viewModel.setThemePreset(it) },
                    modifier = Modifier.weight(1f)
                )

                InteractionHub(
                    state = importState,
                    onSelectFile = { jsonPickerLauncher.launch(arrayOf("application/json")) },
                    onPasteJson = { viewModel.parseJsonString(it) },
                    onStartSync = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                        )
                    },
                    onAbort = { viewModel.resetState() },
                    onResolveConflict = { res, all -> viewModel.resolveConflict(res, all) },
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }

        if (showThemePicker) {
            ThemePickerSheet(
                characters = characters,
                loadError = loadError,
                currentConfig = themeConfig,
                onDismiss = { showThemePicker = false },
                onSelectCharacter = { viewModel.setThemePreset(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(
    characters: List<CharacterMetadata>,
    loadError: String?,
    currentConfig: ThemeConfig,
    onDismiss: () -> Unit,
    onSelectCharacter: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Choose Character Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            if (characters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loadError ?: "No characters loaded",
                        textAlign = TextAlign.Center,
                        color = if (loadError != null) MaterialTheme.colorScheme.error else Color.Unspecified,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(characters) { character ->
                        val isSelected = character.id == currentConfig.characterId
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = character.name, 
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
                                ) 
                            },
                            supportingContent = { Text(character.source, style = MaterialTheme.typography.bodySmall) },
                            leadingContent = {
                                Surface(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    AsyncUriImage(
                                        uriString = character.images.portrait,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            },
                            trailingContent = {
                                RadioButton(selected = isSelected, onClick = null)
                            },
                            modifier = Modifier
                                .clickable { onSelectCharacter(character.id) }
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent)
                        )
                    }
                }
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("APPLY & CLOSE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun HeroSection(modifier: Modifier = Modifier) {
    val tokens = LocalThemeTokens.current
    val heroPath = tokens.heroAssetPath
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp) // Increased height for premium feel
            .graphicsLayer {
                // Subtle parallax effect based on scroll or just static depth
                alpha = 1f
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (heroPath != null) {
            // Hero Image
            AsyncUriImage(
                uriString = heroPath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Soft fade at the bottom to blend into background
                        alpha = 1f
                    },
                contentScale = ContentScale.Fit
            )
            
            // Bottom Fade Overlay to blend hero into background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                tokens.background.copy(alpha = 1.0f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun CharacterCarousel(
    characters: List<CharacterMetadata>,
    loadError: String?,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (characters.isEmpty()) {
        Box(modifier = modifier.padding(horizontal = 48.dp), contentAlignment = Alignment.Center) {
            Text(
                text = loadError ?: "No characters loaded", 
                textAlign = TextAlign.Center,
                color = if (loadError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = characters.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
        pageCount = { characters.size }
    )

    LaunchedEffect(selectedId) {
        val index = characters.indexOfFirst { it.id == selectedId }
        if (index >= 0 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (characters.isNotEmpty() && pagerState.currentPage < characters.size) {
            val character = characters[pagerState.currentPage]
            if (character.id != selectedId) {
                onSelect(character.id)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 80.dp),
        modifier = modifier.fillMaxWidth()
    ) { page ->
        val character = characters[page]
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
        
        val scale = lerp(0.8f, 1.0f, 1f - pageOffset.coerceIn(0f, 1f))
        val alpha = lerp(0.4f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

        Column(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { onSelect(character.id) },
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncUriImage(
                    uriString = character.images.portrait,
                    contentDescription = character.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = character.source.uppercase(),
                modifier = Modifier.alpha(0.5f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun InteractionHub(
    state: ImportState,
    onSelectFile: () -> Unit,
    onPasteJson: (String) -> Unit,
    onStartSync: () -> Unit,
    onAbort: () -> Unit,
    onResolveConflict: (ConflictResolution, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var importMethod by remember { mutableStateOf(0) } // 0: File, 1: Paste
    var pastedJson by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    ThemedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state is ImportState.Idle) {
                TabRow(
                    selectedTabIndex = importMethod,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[importMethod]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(selected = importMethod == 0, onClick = { importMethod = 0 }, text = { Text("Upload File") })
                    Tab(selected = importMethod == 1, onClick = { importMethod = 1 }, text = { Text("Paste JSON") })
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(tween(400)) + scaleIn(initialScale = 0.92f) togetherWith
                    fadeOut(tween(400)) + scaleOut(targetScale = 0.92f)
                },
                label = "UIFlow"
            ) { targetState ->
                when (targetState) {
                    is ImportState.Idle -> {
                        if (importMethod == 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).alpha(0.2f),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Database Ready",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Select a student registry JSON file to begin local synchronization.",
                                    modifier = Modifier.alpha(0.6f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                ThemedButton(
                                    onClick = onSelectFile,
                                    modifier = Modifier.fillMaxWidth().height(64.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FileUpload, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = "SELECT JSON DATABASE", fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                OutlinedTextField(
                                    value = pastedJson,
                                    onValueChange = { pastedJson = it },
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    placeholder = { Text("Paste your contact JSON here...") },
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            val clip = clipboardManager.getText()?.text
                                            if (clip != null) pastedJson = clip
                                            else Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PASTE")
                                    }
                                    ThemedButton(
                                        onClick = { onPasteJson(pastedJson) },
                                        modifier = Modifier.weight(1f),
                                        enabled = pastedJson.isNotBlank()
                                    ) {
                                        Text("VALIDATE")
                                    }
                                }
                            }
                        }
                    }

                    is ImportState.Parsing -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ThemedProgress()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "VALIDATING SCHEMA", 
                                modifier = Modifier.alpha(0.5f),
                                style = MaterialTheme.typography.labelLarge, 
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    is ImportState.ReadyToImport -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${targetState.validCount} Contacts", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                            Text(
                                text = "INTEGRITY VERIFIED", 
                                modifier = Modifier.alpha(0.5f),
                                style = MaterialTheme.typography.labelMedium, 
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = onAbort, 
                                    modifier = Modifier.weight(1f).height(56.dp), 
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = "ABORT", fontWeight = FontWeight.Bold)
                                }
                                ThemedButton(
                                    onClick = onStartSync, 
                                    modifier = Modifier.weight(2f).height(56.dp)
                                ) {
                                    Text(text = "BEGIN SYNC", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    is ImportState.Importing -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val progressValue = if (targetState.total > 0) targetState.progress.toFloat() / targetState.total else 0f
                            LinearProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "SYNCING: ${targetState.progress} / ${targetState.total}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    is ImportState.Conflict -> {
                        ConflictResolutionUI(conflict = targetState.conflict, onResolve = onResolveConflict)
                    }

                    is ImportState.Finished -> {
                        SyncReport(result = targetState.result, onDone = onAbort)
                    }

                    is ImportState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SyncProblem, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "SYNC FAILURE", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                            Text(
                                text = targetState.message, 
                                modifier = Modifier.alpha(0.7f),
                                style = MaterialTheme.typography.bodyMedium, 
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ThemedButton(onClick = onAbort, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                Text(text = "TRY AGAIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConflictResolutionUI(
    conflict: ContactConflict,
    onResolve: (ConflictResolution, Boolean) -> Unit
) {
    var applyToAll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Conflict Detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(text = "Existing: ${conflict.existingName}", fontWeight = FontWeight.Bold)
                Text(text = conflict.existingNumber, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.6f))
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(text = "Incoming: ${conflict.incomingName}", fontWeight = FontWeight.Bold)
                Text(text = conflict.incomingNumber, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.6f))
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.clickable { applyToAll = !applyToAll }
        ) {
            Checkbox(checked = applyToAll, onCheckedChange = { applyToAll = it })
            Text(text = "Apply to all remaining conflicts", style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onResolve(ConflictResolution.SKIP, applyToAll) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "SKIP")
            }
            Button(
                onClick = { onResolve(ConflictResolution.OVERWRITE, applyToAll) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "OVERWRITE")
            }
        }
    }
}

@Composable
fun SyncReport(result: ImportResult, onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "SYNC SUCCESSFUL", 
            modifier = Modifier.alpha(0.4f),
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Black
        )
        Text(text = "Database Updated", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            SyncMetric("NEW", result.created.toString(), Color(0xFF4CAF50))
            SyncMetric("UPDATED", result.updated.toString(), Color(0xFF2196F3))
            SyncMetric("SKIPPED", result.skipped.toString(), Color(0xFFFF9800))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        ThemedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text(text = "FINISH", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun SyncMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = color)
        Text(
            text = label, 
            modifier = Modifier.alpha(0.5f),
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.ExtraBold
        )
    }
}
