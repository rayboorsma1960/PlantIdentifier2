package com.example.plantidentifier

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlantIdentifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var imageUri by remember { mutableStateOf<Uri?>(null) }
                    val identificationResult by remember { mutableStateOf("") }

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
                            identifyPlant(uri)
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

    private fun identifyPlant(imageUri: Uri) {
        Log.d("PlantIdentifier", "Identifying plant from URI: $imageUri")
        // TODO: Implement plant identification logic here
        Toast.makeText(this, "Plant identification will be implemented here.", Toast.LENGTH_SHORT).show()
    }
}