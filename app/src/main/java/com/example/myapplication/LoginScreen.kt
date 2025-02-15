@file:Suppress("DEPRECATION")
package com.example.myapplication

import android.content.Context
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher


@Composable
fun LoginScreen(navController: NavController, googleSignInLauncher: ActivityResultLauncher<Intent>) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Scaffold(
        topBar = { LoginTopBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Welcome Text
            Text(
                text = "Welcome To Expense Tracker",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(62.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, color = Color.Black), // Ensure text is visible
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.Black
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
                    disabledContainerColor = Color.Black
                )
            )

            // Log in buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
            ) {

                // Log In Button
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            signInUser(email, password, navController, context)
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter both email and password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Log in",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Google Sign-In Button
                Button(
                    onClick = {
                        val googleSignInClient = GoogleSignIn.getClient(
                            context,
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("401202928606-8k84hbsm8lbapohc7vqbr13lclnqf79f.apps.googleusercontent.com")
                                .requestEmail()
                                .build()
                        )

                        // Sign out to force account selection
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google", color = Color.Black)
                }
            }


            // Bottom Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    append("Don't have an account? ")
                    pushStringAnnotation(tag = "SIGN_UP", annotation = "sign_up_screen")
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                            color = Color(0xFF3E6B60),
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Sign Up")
                    }
                    pop()
                }

                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {

                        navController.navigate("signup")  // Navigates to Sign Up screen
                    }
                )
            }
        }
    }
}

fun signInUser(email: String, password: String, navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // User is logged in, navigate to home screen
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                navController.navigate("home_screen")
            } else {
                // If login fails, show an error message
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}


@Composable
fun LoginTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Login Top Bar Background",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
