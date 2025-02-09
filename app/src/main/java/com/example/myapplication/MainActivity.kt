@file:Suppress("DEPRECATION")
package com.example.myapplication

import android.Manifest
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider



@Suppress("NAME_SHADOWING")
class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var imageCapture: ImageCapture? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("401202928606-8k84hbsm8lbapohc7vqbr13lclnqf79f.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        // Initialize Firestore
        FirebaseApp.initializeApp(this)
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .build()

        firestore.firestoreSettings = settings

        Log.d("FirestoreInit", "Firestore has been initialized with offline support.")

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
        val context = LocalContext.current
        val selectedLanguage = remember { mutableStateOf(getSavedLanguage(context)) }

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task, navController)
        }

        NavHost(navController = navController, startDestination = "splash") {

            // Splash Screen
            composable("splash") { SplashScreen(navController) }

            // SignUp Page
            composable("signup") { SignUpScreen(navController) }

            // SignUp Page
            composable("login") { LoginScreen(navController,googleSignInLauncher = googleSignInLauncher) }

            // HomeScreen
            composable("home_screen") { HomeScreen(navController) }

            // ScanReceiptScreen
            composable("scan_receipt") { ScanReceiptScreen(navController) }

            // AddExpenseScreen
            composable("add_expense/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: "Unknown"
                AddExpenseScreen(navController, category)
            }

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

        // File picker launcher
        val context = LocalContext.current
        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val file = uriToFile(context, it) // Convert Uri to File
                file?.let { uploadImage(it, navController) }
            }
        }

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

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { captureImage(navController) },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Capture Image")
                        }

                        Button(
                            onClick = { filePickerLauncher.launch("image/*") },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Choose from Gallery")
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
            .url("https://platinum-tract-449212-d7.ew.r.appspot.com/api/ocr")
            .post(MultipartBody.Builder().setType(MultipartBody.FORM).addPart(body).build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("UploadImage", "OCR Extracted text: $responseBody")

                    // Extract only the "extracted_text" field from JSON response
                    val extractedText = try {
                        JSONObject(responseBody).getString("extracted_text")
                    } catch (e: JSONException) {
                        Log.e("UploadImage", "Failed to parse OCR response", e)
                        null
                    }

                    // Send extracted text to categorization API
                    extractedText?.let {
                        Log.d("UploadImage", "Sending extracted text to categorization API: $it")
                        categorizeReceipt(it, navController)
                    } ?: runOnUiThread {
                        Toast.makeText(applicationContext, "Error extracting text", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect to server", Toast.LENGTH_SHORT).show()
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

    private fun categorizeReceipt(extractedText: String, navController: NavController) {
        val json = JSONObject().apply {
            put("text", extractedText)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://platinum-tract-449212-d7.ew.r.appspot.com/api/categorize")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val category = response.body?.string()?.let {
                        JSONObject(it).getString("category")
                    } ?: "Others"

                    Log.d("Categorization", "Receipt categorized as: $category")

                    // ✅ Add expense to Firestore before navigating
                    saveExpenseToFirestore(category, extractedText, navController)
                } else {
                    Log.e("Categorization", "Failed to categorize receipt: ${response.message}")
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error categorizing receipt", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Categorization", "Failed to connect to categorization API", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)

        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: IOException) {
            Log.e("UriToFile", "Failed to copy URI to file: ${e.message}")
            null
        }
    }

    private fun saveExpenseToFirestore(category: String, extractedText: String, navController: NavController) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("Firestore", "User not logged in")
            return
        }

        val amount = extractAmountFromText(extractedText)  // Extracted total
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) // Matches "gio, 30 gen 2025"
        val date = dateFormat.format(Date())  // Formats the current date
        val timestamp = System.currentTimeMillis() // Ensures correct timestamp

        val expenseData = hashMapOf(
            "userId" to userId,
            "category" to category,
            "amount" to amount,  // **Ensures amount is a Double**
            "date" to date,  // **Stores date in localized format**
            "timestamp" to timestamp  // **Ensures correct epoch time**
        )

        db.collection("expenses")
            .add(expenseData)
            .addOnSuccessListener {
                Log.d("Firestore", "Expense added successfully!")

                runOnUiThread {
                    Toast.makeText(applicationContext, "Expense Added!", Toast.LENGTH_SHORT).show()

                    // ✅ Navigate to Transactions Screen after saving
                    Log.d("Navigation", "Navigating to transactions_screen")
                    navController.navigate("transactions_screen") {
                        popUpTo("transactions_screen") { inclusive = true }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding expense", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun extractAmountFromText(text: String): Double {
        val totalKeywords = listOf(
            "TOTALE", "TOTALE EURO", "TOTALE COMPLESSIVO",  // Italian
            "TOTAL", "TOTAL AMOUNT", "TOTAL PRICE",        // English
            "MONTANT TOTAL", "PRIX TOTAL",                 // French
            "GESAMTBETRAG", "SUMME",                        // German
            "TOTAL GENERAL"                                 // Spanish, Portuguese
        )

        // Regex to match amounts in formats like "69.00", "1,234.56", "1.234,56"
        val amountRegex = Regex("(\\d{1,3}(?:[.,]\\d{3})*[.,]\\d{2})")

        var bestAmount: Double? = null
        var bestPosition = Int.MAX_VALUE

        for (keyword in totalKeywords) {
            val pattern = Regex("$keyword\\s*(\\d{1,3}(?:[.,]\\d{3})*[.,]\\d{2})?", RegexOption.IGNORE_CASE)
            val match = pattern.find(text)

            if (match != null) {
                val keywordPosition = match.range.first

                // Check number AFTER the keyword
                match.groups[1]?.value?.let {
                    val amount = it.replace(",", ".").toDoubleOrNull()
                    if (amount != null) return amount  // Prioritize direct match
                }

                // If no number after, search the closest number BEFORE the keyword
                amountRegex.findAll(text).forEach { numberMatch ->
                    val numberPosition = numberMatch.range.first
                    val amount = numberMatch.value.replace(",", ".").toDoubleOrNull()

                    if (amount != null && numberPosition < keywordPosition && numberPosition < bestPosition) {
                        bestAmount = amount
                        bestPosition = numberPosition
                    }
                }
            }
        }

        // If no keyword match, fallback to last valid amount in the receipt
        if (bestAmount == null) {
            val lastAmountMatch = amountRegex.findAll(text).lastOrNull()?.value?.replace(",", ".")
            bestAmount = lastAmountMatch?.toDoubleOrNull()
        }

        return bestAmount ?: 0.0
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>, navController: NavController) {
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!, navController)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign in failed", e)
        }
    }



    private fun firebaseAuthWithGoogle(idToken: String, navController: NavController) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate("home_screen")
                } else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }





}