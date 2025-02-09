package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import android.content.SharedPreferences
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    var username by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }  // ✅ Zoom dialog state
    var showLanguageDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember { mutableStateOf(getSavedLanguage(context)) }

    // Fetch user data and profile picture when screen loads
    LaunchedEffect(userId) {
        fetchUserData { fetchedUsername, fetchedEmail ->
            username = fetchedUsername
            email = fetchedEmail
        }
        profilePictureUri = getSavedProfilePictureUri(context, userId)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileTopBar()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 150.dp)
            ) {
                // ✅ Profile Picture (Zoom on Click)
                Surface(
                    shape = CircleShape,
                    color = Color.LightGray,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { showZoomDialog = true } // ✅ Opens Zoom Dialog

                ) {
                    if (profilePictureUri != null) {
                        val bitmap = BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(profilePictureUri!!)
                        )
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (profilePictureUri != null) {
                    TextButton(
                        onClick = {
                            deleteProfilePicture(context, userId)
                            profilePictureUri = null
                            Toast.makeText(context, "🗑️ Picture deleted!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Delete Picture", color = Color.Red)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = username ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = email ?: "", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                ProfileMenuOption(icon = R.drawable.ic_profile, label = "Account info", onClick = {})
                ProfileMenuOption(icon = R.drawable.ic_settings, label = "Settings", onClick = { showEditDialog = true })
                ProfileMenuOption(icon = R.drawable.ic_wallet, label = "Change language", onClick = { showLanguageDialog = true })
                ProfileMenuOption(icon = R.drawable.ic_logout, label = "Logout", isDestructive = true) {
                    navController.navigate("login")
                }
            }
        }
    }

    // ✅ Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentUsername = username ?: "",
            currentProfilePictureUri = profilePictureUri,
            onDismiss = { showEditDialog = false },
            onSave = { newUsername, newProfilePictureUri ->
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("username", newUsername)
                    .addOnSuccessListener {
                        Toast.makeText(context, "✅ Username updated!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "❌ Failed to update username.", Toast.LENGTH_SHORT).show()
                    }

                newProfilePictureUri?.let {
                    saveProfilePictureLocally(context, it, userId)
                    profilePictureUri = it
                }

                username = newUsername
            }
        )
    }

    // ✅ Zoom Dialog for Profile Picture
    if (showZoomDialog) {
        ZoomedImageDialog(imageUri = profilePictureUri, onDismiss = { showZoomDialog = false })
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { newLanguage ->
                saveLanguagePreference(context, newLanguage)
                selectedLanguage = newLanguage
                showLanguageDialog = false
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentProfilePictureUri: Uri?,
    onDismiss: () -> Unit,
    onSave: (String, Uri?) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }
    var newProfilePictureUri by remember { mutableStateOf(currentProfilePictureUri) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newProfilePictureUri = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .wrapContentHeight(),
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    trailingIcon = {
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                                contentDescription = "Select Image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = Color(0xFF018786),
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = {
                        onSave(newUsername, newProfilePictureUri)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6200EE)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Save",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    )
}



@Composable
fun ProfileMenuOption(
    icon: Int,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit // Pass a lambda to handle the click
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(horizontal = 12.dp)
            .offset(y = (-20).dp)
            .clickable { onClick() }, // Add the clickable modifier
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = if (isDestructive) Color.Red else Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (isDestructive) Color.Red else Color.Black
        )
    }
}


@Composable
fun ZoomedImageDialog(imageUri: Uri?, onDismiss: () -> Unit) {
    val context = LocalContext.current

    if (imageUri != null) {
        val bitmap = remember(imageUri) {
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = null,          // ❌ No title needed for zoom
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() }, // ✅ Dismiss on click
                    contentAlignment = Alignment.Center
                ) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Zoomed Profile Picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(20.dp)
                        )
                    }
                }
            },
            buttons = { /* No buttons needed */ },  // ✅ Empty buttons block
            backgroundColor = Color.Black,
            modifier = Modifier.fillMaxSize()
        )
    }
}





@Composable
fun ProfileTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .zIndex(0f)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_tophome),
            contentDescription = "AppBar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Overlaying Content (Text)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}

fun fetchUserData(onUserDataFetched: (String, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return // Get logged-in user's UID

    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    userRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val username = document.getString("username") ?: "User"
                val email = document.getString("email") ?: "user@example.com"
                onUserDataFetched(username, email) // Pass username and email to the Composable
            } else {
                Log.e("Firestore", "User document does not exist")
                onUserDataFetched("User", "user@example.com") // Default values if not found
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching user data: ${e.message}")
            onUserDataFetched("User", "user@example.com") // Default values on failure
        }
}

fun saveProfilePictureLocally(context: Context, uri: Uri, userId: String) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "profile_picture_$userId.jpg") // Unique filename per user
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getSavedProfilePictureUri(context: Context, userId: String): Uri? {
    val file = File(context.filesDir, "profile_picture_$userId.jpg") // Load based on userId
    return if (file.exists()) Uri.fromFile(file) else null
}

fun deleteProfilePicture(context: Context, userId: String) {
    val file = File(context.filesDir, "profile_picture_$userId.jpg")
    if (file.exists()) {
        file.delete()
    }
}

fun saveLanguagePreference(context: Context, language: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("language", language)
        apply()
    }
}

fun getSavedLanguage(context: Context): String {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("language", "en") ?: "en"  // Default to English
}


@Composable
fun LanguageSelectionDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column {
                LanguageOption("English", "en", selectedLanguage, onLanguageSelected)
                LanguageOption("Italiano", "it", selectedLanguage, onLanguageSelected)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LanguageOption(
    label: String,
    languageCode: String,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageSelected(languageCode) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedLanguage == languageCode,
            onClick = { onLanguageSelected(languageCode) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}


@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=780dp,dpi=408", // Approximate dimensions for Huawei P20 Pro
    showSystemUi = true // Shows the status and navigation bars in preview
)
@Composable
fun PreviewProfileScreen() {
    val dummyNavController = rememberNavController()
    ProfileScreen(navController = dummyNavController)
}
