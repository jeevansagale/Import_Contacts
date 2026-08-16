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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.contactimporter.data.ImportResult
import com.example.contactimporter.theme.AppTheme
import com.example.contactimporter.ui.AsyncUriImage

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle(
                initialValue = AppTheme("default", null, null)
            )
            
            AppContent(
                viewModel = viewModel,
                themeSettings = themeSettings
            )
        }
    }
}

@Composable
fun AppContent(
    viewModel: MainViewModel,
    themeSettings: AppTheme
) {
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher for READ/WRITE CONTACTS permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_CONTACTS] == true &&
            permissions[Manifest.permission.WRITE_CONTACTS] == true) {
            // Permissions granted, can proceed with import if state is Ready
            if (importState is ImportState.ReadyToImport) {
                viewModel.startImport((importState as ImportState.ReadyToImport).contacts)
            }
        } else {
            Toast.makeText(context, "Contacts permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    // JSON file picker
    val jsonPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.parseJsonFile(it) } }

    // Image pickers
    val bgPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> viewModel.setBackgroundUri(uri) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> viewModel.setLogoUri(uri) }

    // Dynamic Theme Colors based on preset
    val (primaryColor, bgColor) = when (themeSettings.presetId) {
        "dark" -> Color(0xFFBB86FC) to Color(0xFF121212)
        "blue" -> Color(0xFF2196F3) to Color(0xFFBBDEFB)
        "pink" -> Color(0xFFE91E63) to Color(0xFFFCE4EC)
        else -> Color(0xFF6200EE) to Color(0xFFFFFFFF) // default light
    }

    MaterialTheme(
        colorScheme = if (themeSettings.presetId == "dark") darkColorScheme(primary = primaryColor) 
                      else lightColorScheme(primary = primaryColor, background = bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Background Image
            if (themeSettings.backgroundUri != null) {
                AsyncUriImage(
                    uriString = themeSettings.backgroundUri,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize().alpha(0.3f), // slight transparency
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                if (themeSettings.logoUri != null) {
                    Box(modifier = Modifier.size(100.dp).padding(8.dp)) {
                        AsyncUriImage(
                            uriString = themeSettings.logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Text("Contact Importer", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Settings Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Appearance Settings", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { bgPickerLauncher.launch(arrayOf("image/*")) }) { Text("Set Background") }
                            Button(onClick = { logoPickerLauncher.launch(arrayOf("image/*")) }) { Text("Set Logo") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePresetChip("Light", "default", themeSettings.presetId) { viewModel.setThemePreset(it) }
                            ThemePresetChip("Dark", "dark", themeSettings.presetId) { viewModel.setThemePreset(it) }
                            ThemePresetChip("Blue", "blue", themeSettings.presetId) { viewModel.setThemePreset(it) }
                            ThemePresetChip("Pink", "pink", themeSettings.presetId) { viewModel.setThemePreset(it) }
                        }
                    }
                }

                // Main Import Action Card
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp).fillMaxSize(), 
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (val state = importState) {
                            is ImportState.Idle -> {
                                Text("Select a JSON file containing your contacts.")
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { jsonPickerLauncher.launch(arrayOf("application/json")) }) {
                                    Text("Pick JSON File")
                                }
                            }
                            is ImportState.Parsing -> {
                                CircularProgressIndicator()
                                Text("Parsing...")
                            }
                            is ImportState.ReadyToImport -> {
                                Text("Found ${state.contacts.size} contacts.", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                                    )
                                }) {
                                    Text("Start Import")
                                }
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.resetState() }) {
                                    Text("Cancel")
                                }
                            }
                            is ImportState.Importing -> {
                                CircularProgressIndicator(progress = { if (state.total > 0) state.progress.toFloat() / state.total else 0f })
                                Text("Importing: ${state.progress} / ${state.total}")
                            }
                            is ImportState.Finished -> {
                                SummaryView(state.result)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { viewModel.resetState() }) {
                                    Text("Done")
                                }
                            }
                            is ImportState.Error -> {
                                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { viewModel.resetState() }) {
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemePresetChip(label: String, id: String, currentId: String, onClick: (String) -> Unit) {
    val isSelected = currentId == id
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick(id) }
    ) {
        Text(
            text = label, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SummaryView(result: ImportResult) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Import Complete!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text("Total Processed: ${result.totalContacts}")
        Text("Successfully Imported: ${result.importedContacts}", color = Color(0xFF4CAF50))
        Text("Skipped (Duplicates/Empty): ${result.duplicatesSkipped}", color = Color(0xFFFF9800))
        if (result.errors > 0) {
            Text("Errors: ${result.errors}", color = MaterialTheme.colorScheme.error)
        }
    }
}