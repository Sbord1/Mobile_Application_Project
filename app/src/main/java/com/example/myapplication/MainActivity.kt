package com.example.myapplication

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme // Import the theme
import androidx.compose.material.*
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResult


@Suppress("NAME_SHADOWING")
class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize the executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            MyApplicationTheme {
                ExpenseTrackerApp() // Main app screen with navigation
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shutdown executor
        cameraExecutor.shutdown()
    }

    @Composable
    fun ExpenseTrackerApp() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "splash") {

            // Splash Screen
            composable("splash") { SplashScreen(navController) }

            // SignUp Page
            composable("signup") { SignUpScreen(navController) }

            // SignUp Page
            composable("login") { LoginScreen(navController) }

            // HomeScreen
            composable("home_screen") { HomeScreen(navController) }

            // ScanReceiptScreen
            composable("scan_receipt") { ScanReceiptScreen(navController) }

            // AddExpenseScreen
            composable("add_expense") { AddExpenseScreen(navController) }

            // Transactions Screen (Placeholder)
            composable("transactions_screen") { AllTransactionsScreen(navController) }

            // Statistics Screen (Placeholder)
            composable("statistics_screen") { StatisticsScreen(navController) }

            // Profile Screen (Placeholder)
            composable("profile_screen") { ProfileScreen(navController) }
        }
    }


    @Composable
    fun ScanReceiptScreen(navController: NavController) {
        var isCameraPermissionGranted by remember { mutableStateOf(false) }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                isCameraPermissionGranted = isGranted
            }
        )

        LaunchedEffect(Unit) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            Log.d("ScanReceiptScreen", "Camera permission requested")
        }

        Scaffold(
            topBar = {
                androidx.compose.material.TopAppBar(
                    title = { Text("Scan Receipt") }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (isCameraPermissionGranted) {
                    Log.d("ScanReceiptScreen", "Camera permission granted")
                    CameraPreviewView(
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.navigate("home_screen") }) {
                            Text("Go back")
                        }
                    }
                } else {
                    Text("Camera Permission Not Granted")
                }
            }
        }
    }



    @Composable
    fun CameraPreviewView(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        // Mutable states to manage preview and error
        var preview by remember { mutableStateOf<Preview?>(null) }
        var previewView by remember { mutableStateOf<PreviewView?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(cameraProviderFuture) {
            try {
                val cameraProvider = cameraProviderFuture.get()
                // Unbind any previous use cases
                cameraProvider.unbindAll()

                // Create a new Preview use case
                preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView?.surfaceProvider) }

                // Bind the Preview use case to the camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

                Log.d("CameraPreviewView", "Camera preview initialized successfully")
            } catch (exc: Exception) {
                Log.e("CameraPreviewView", "Failed to bind camera preview", exc)
                error = "Failed to initialize camera preview: ${exc.message}"
            }
        }

        // Render the PreviewView or error message
        if (error != null) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Unknown error", color = MaterialTheme.colors.error
                )
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        previewView = this // Set the mutable state to this PreviewView
                        Log.d("CameraPreviewView", "Surface provider set successfully")
                    }
                },
                modifier = modifier
            )
        }
    }
}
