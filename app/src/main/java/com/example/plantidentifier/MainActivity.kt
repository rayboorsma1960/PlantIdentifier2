package com.example.plantidentifier

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.example.plantidentifier.ui.theme.PlantIdentifierTheme
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var generativeModel: GenerativeModel

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Gemini API
        generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = "AIzaSyAOApzWL2G8uTtaY9z4rMHeIx6Jk7ZYx8Y" // Replace with your actual API key
        )

        setContent {
            PlantIdentifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var imageUri by remember { mutableStateOf<Uri?>(null) }
                    var identificationResult by remember { mutableStateOf("") }

                    val permissionState = rememberPermissionState(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            Manifest.permission.READ_MEDIA_IMAGES
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )

                    LaunchedEffect(Unit) {
                        Log.d("PlantIdentifier", "Initial permission state: ${permissionState.status}")
                    }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        Log.d("PlantIdentifier", "Image selected: $uri")
                        imageUri = uri
                        if (uri != null) {
                            identifyPlant(uri) { result ->
                                identificationResult = result
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected plant image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(16.dp)
                            )
                        }

                        Button(
                            onClick = {
                                Log.d("PlantIdentifier", "Upload button clicked")
                                when {
                                    permissionState.status.isGranted -> {
                                        Log.d("PlantIdentifier", "Permission is granted, launching image picker")
                                        launcher.launch("image/*")
                                    }
                                    permissionState.status.shouldShowRationale -> {
                                        Log.d("PlantIdentifier", "Should show permission rationale")
                                        // Show rationale if needed
                                    }
                                    else -> {
                                        Log.d("PlantIdentifier", "Requesting permission")
                                        permissionState.launchPermissionRequest()
                                    }
                                }
                            }
                        ) {
                            Text("Upload Image")
                        }

                        if (identificationResult.isNotEmpty()) {
                            Text(
                                text = identificationResult,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun identifyPlant(imageUri: Uri, onResult: (String) -> Unit) {
        Log.d("PlantIdentifier", "Identifying plant from URI: $imageUri")
        lifecycleScope.launch {
            try {
                val bitmap = contentResolver.openInputStream(imageUri)?.use {
                    BitmapFactory.decodeStream(it)
                }

                bitmap?.let {
                    val prompt = "Identify this plant and provide its name and some important information about it."
                    val response = generativeModel.generateContent(
                        content {
                            image(bitmap)
                            text(prompt)
                        }
                    )

                    onResult(response.text ?: "Unable to identify the plant.")
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }
}