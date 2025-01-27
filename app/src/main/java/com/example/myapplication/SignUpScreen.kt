package com.example.myapplication

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth


@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = { SignUpBar() }
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
                color = MaterialTheme.colorScheme.primary, // Custom green color to match the design
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp) // Adjust padding to control spacing
                    .height(56.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, color = Color.Black), // Ensure text is visible
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
                    .height(48.dp),
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
                    // Attempt to sign up with Firebase Authentication
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        signUpUser(username, password, navController)
                    } else {
                        Toast.makeText(
                            navController.context,
                            "Please enter both email and password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp), // Rounded corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White, // Text color
                    fontSize = 14.sp,
                )
            }


            // Bottom Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    append("Already Have Account? ")
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
                        // Handle click action for the "Log In" part
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}

fun signUpUser(username: String, password: String, navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    // Register the user with email and password
    auth.createUserWithEmailAndPassword(username, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // User is successfully registered, navigate to home screen
                Toast.makeText(navController.context, "Registration successful", Toast.LENGTH_SHORT).show()
                navController.navigate("home_screen") // Change to your home screen
            } else {
                // If registration fails, show an error message
                Toast.makeText(navController.context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}


@Composable
fun SignUpBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
    ) {
        // Background Image (PNG)
        Image(
            painter = painterResource(id = R.drawable.login), // Replace with your PNG file name
            contentDescription = "Login Top Bar Background",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop // Adjust this to match your design
        )
    }
}

// Preview function
@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    // Use a mock NavController for the preview
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}
