package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // General padding
    ) {
        // SignUpBar Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter) // Aligns the SignUpBar at the top
        ) {
            SignUpBar()
        }

        // Welcome Text Box (slightly above SignUpBar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 280.dp)
                .padding(horizontal = 16.dp)
                .background(Color.White, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome To Expense Tracker",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp) // Padding inside the box
            )
        }

        // Sign Up Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 380.dp) // Adjusted to make space for the SignUpBar and welcome text
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp) // Adjust padding to control spacing
                    .height(62.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, color = Color.Black),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.Black // Text color for input
                )
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp) // Adjust padding to control spacing
                    .height(62.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, color = Color.Black),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.Black // Text color for input
                )
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(62.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, color = Color.Black),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.Black // Text color for input
                )
            )

            // Sign Up Button
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                        signUpUser(username, password, email, navController)
                    } else {
                        Toast.makeText(
                            navController.context,
                            "Please enter required data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Bottom Text (Already Have an Account?)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    append("Already Have an Account? ")
                    pushStringAnnotation(tag = "LOG_IN", annotation = "login_screen")
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                            color = Color(0xFF3E6B60),
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Log In")
                    }
                    pop()
                }

                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}

// Function to handle user sign-up with Firebase
fun signUpUser(username: String, password: String, email: String, navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance() // Firestore database reference

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid // Ensure user exists before proceeding

                if (userId != null) {
                    val userData = hashMapOf(
                        "uid" to userId,
                        "email" to email,
                        "username" to username
                    )

                    // Save the username in Firestore under "users" collection
                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "User data saved successfully: $userData")

                            // Verify that Firestore has stored the data correctly
                            db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        Log.d("Firestore", "Verified Firestore write: ${document.data}")
                                        Toast.makeText(navController.context, "Registration successful", Toast.LENGTH_SHORT).show()

                                        // Navigate only after data is confirmed saved
                                        navController.navigate("home_screen")
                                    } else {
                                        Log.e("Firestore", "Document does not exist after write!")
                                        Toast.makeText(navController.context, "Firestore write verification failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error retrieving document: ${e.message}")
                                    Toast.makeText(navController.context, "Error verifying Firestore write", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Failed to save user data: ${e.message}")
                            Toast.makeText(navController.context, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("Auth", "User ID is null after registration")
                    Toast.makeText(navController.context, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("Auth", "Registration failed: ${task.exception?.message}")
                Toast.makeText(navController.context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}





// SignUpBar Component (Background Image)
@Composable
fun SignUpBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Adjusted height to match layout needs
    ) {
        Image(
            painter = painterResource(id = R.drawable.login), // Replace with your PNG file name
            contentDescription = "Sign Up Bar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
