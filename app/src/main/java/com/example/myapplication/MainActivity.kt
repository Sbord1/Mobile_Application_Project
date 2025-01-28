package com.example.myapplication

import android.Manifest
import android.content.Context
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
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageCapture.Builder
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.widget.Toast



@Suppress("NAME_SHADOWING")
class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var imageCapture: ImageCapture? = null


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
    fun ScanReceiptScreen(navController: androidx.navigation.NavController) {
        var isCameraPermissionGranted by remember { mutableStateOf(false) }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                isCameraPermissionGranted = isGranted
            }
        )

        // Request camera permission on first launch
        LaunchedEffect(Unit) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            Log.d("ScanReceiptScreen", "Camera permission requested")
        }

        Scaffold(
            topBar = { androidx.compose.material.TopAppBar(title = { Text("Scan Receipt") }) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (isCameraPermissionGranted) {
                    Log.d("ScanReceiptScreen", "Camera permission granted")
                    CameraPreviewView(modifier = Modifier.fillMaxSize())

                    Button(
                        onClick = { captureImage(navController) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text("Capture Image")
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
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        var previewView by remember { mutableStateOf<PreviewView?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(cameraProviderFuture) {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build() // Initialize ImageCapture

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                Log.d("CameraPreviewView", "Camera preview initialized successfully")
            } catch (exc: Exception) {
                Log.e("CameraPreviewView", "Failed to bind camera preview", exc)
                error = "Failed to initialize camera preview: ${exc.message}"
            }
        }

        if (error != null) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Unknown error", color = MaterialTheme.colors.error)
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        previewView = this
                    }
                },
                modifier = modifier
            )
        }
    }

    private fun uploadImage(imageFile: File, navController: NavController) {
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
        val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://platinum-tract-449212-d7.ew.r.appspot.com//api/ocr")
            .post(MultipartBody.Builder().setType(MultipartBody.FORM).addPart(body).build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the OCR result (Extracted text)
                    val extractedText = response.body?.string()
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Extracted text: $extractedText",
                            Toast.LENGTH_LONG
                        ).show()

                        navController.navigate("add_expense")
                    }
                } else {
                    // Handle the error
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Error uploading image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to connect to server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun captureImage(navController: NavController) {
        val imageCapture = imageCapture ?: return

        // Create time-stamped name and MediaStore entry.
        val outputFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "receipt_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(outputFile)
                    Log.d("CaptureImage", "Photo capture succeeded: $savedUri")

                    // Upload the captured image
                    uploadImage(outputFile, navController)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CaptureImage", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(
                        baseContext,
                        "Failed to capture image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}