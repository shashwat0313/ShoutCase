package com.example.shoutcase
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.shoutcase.ui.theme.ShoutCaseTheme
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("button-settings", Context.MODE_PRIVATE)

        setContent {
            ShoutCaseTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Greeting("Android", modifier = Modifier.absolutePadding(16.dp, 0.dp, 0.dp, 0.dp))
                        SettingsScreen()
                    }

                }
            }
        }
    }
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SettingsScreen() {
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current // Get the keyboard controller

        // State variables with proper types
        var buttonText by remember { mutableStateOf(sharedPreferences.getString(getString(R.string.BUTTON_TEXT), "!!!") ?: "!!!") }
        var addExclamation by remember { mutableStateOf(sharedPreferences.getBoolean(getString(R.string.BOOLEAN_EXCLAMATION), false)) }

        var buttonSizeInput by remember { mutableStateOf(sharedPreferences.getInt(getString(R.string.BUTTON_SIZE), 56).toString()) }
        var buttonTextSizeInput by remember { mutableStateOf(sharedPreferences.getInt(getString(R.string.BUTTON_TEXT_SIZE), 24).toString()) }

        var buttonColorInput by remember { mutableStateOf(String.format("#%06X", 0xFFFFFF and sharedPreferences.getInt(getString(R.string.BUTTON_COLOR), Color.RED))) }
        var buttonTextColorInput by remember { mutableStateOf(String.format("#%06X", 0xFFFFFF and sharedPreferences.getInt(getString(R.string.BUTTON_TEXT_COLOR), Color.BLACK))) }

        var text by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = {
                        // Hide keyboard and clear focus
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    indication = null, // Remove the ripple effect
                    interactionSource = remember { MutableInteractionSource() } // Remove the ripple effect
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Try it!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = text,
                    textStyle = TextStyle(fontSize = 15.sp),
                    onValueChange = { text = it },
                    placeholder = { Text(text = "Write Something and select some from it", fontSize = 15.sp) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Button Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(18.dp))

                // Button Text Input
                TextField(
                    value = buttonText,
                    onValueChange = { buttonText = it },
                    label = { Text("Button Text") },
                    placeholder = { Text(buttonText) }
                )

                // Exclamation Toggle
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Exclamation Mark?")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = addExclamation,
                        onCheckedChange = { addExclamation = it }
                    )
                }

                // Button Size Input
                TextField(
                    value = buttonSizeInput,
                    onValueChange = { input -> buttonSizeInput = input },
                    label = { Text("Button Size (dp)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button Text Size Input
                TextField(
                    value = buttonTextSizeInput,
                    onValueChange = { input -> buttonTextSizeInput = input },
                    label = { Text("Button Text Size (sp)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button Color Input
                TextField(
                    value = buttonColorInput,
                    onValueChange = { input -> buttonColorInput = input },
                    label = { Text("Button Color (Hex)") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button Text Color Input
                TextField(
                    value = buttonTextColorInput,
                    onValueChange = { input -> buttonTextColorInput = input },
                    label = { Text("Button Text Color (Hex)") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(onClick = {
                    // Validate and convert inputs before saving
                    val buttonSize = buttonSizeInput.toIntOrNull() ?: 56
                    val buttonTextSize = buttonTextSizeInput.toIntOrNull() ?: 24

                    val buttonColor = try {
                        Color.parseColor(buttonColorInput)
                    } catch (e: IllegalArgumentException) {
                        Color.RED // Default color if parsing fails
                    }

                    val buttonTextColor = try {
                        Color.parseColor(buttonTextColorInput)
                    } catch (e: IllegalArgumentException) {
                        Color.BLACK // Default color if parsing fails
                    }

                    saveSettings(
                        buttonText,
                        addExclamation,
                        buttonSize,
                        buttonColor,
                        buttonTextColor,
                        buttonTextSize
                    )
                    restartService()
                }) {
                    Text("Save Settings")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    openAccessibilitySettings(context)
                }) {
                    Text("Open Accessibility Settings")
                }
            }
        }
    }

    private fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }

    private fun saveSettings(
        buttonText: String,
        exclamation: Boolean,
        buttonSize: Int,
        buttonColor: Int,
        buttonTextColor: Int,
        buttonTextSize: Int
    ) {
        with(sharedPreferences.edit()) {
            putString(getString(R.string.BUTTON_TEXT), buttonText)
            putBoolean(getString(R.string.BOOLEAN_EXCLAMATION), exclamation)
            putInt(getString(R.string.BUTTON_SIZE), buttonSize)
            putInt(getString(R.string.BUTTON_COLOR), buttonColor)
            putInt(getString(R.string.BUTTON_TEXT_COLOR), buttonTextColor)
            putInt(getString(R.string.BUTTON_TEXT_SIZE), buttonTextSize)
            apply()
        }
    }



    private fun saveSettings(
        newButtonText: String,
        newExclamation: Boolean,
        newButtonSize: Int?,
        newButtonColor: Int?,
        newButtonTextColor: Int?,
        newButtonTextSize: Int?
    ) {
        with(sharedPreferences.edit()) {
            // Retain old values if the new ones are empty or null
            putString("button-text", if (newButtonText.isNotEmpty()) newButtonText else sharedPreferences.getString("button-text", "!!!"))
            putBoolean("exclamation?", newExclamation)
            putInt("button-size", newButtonSize ?: sharedPreferences.getInt("button-size", 56))
            putInt("button-color", newButtonColor ?: sharedPreferences.getInt("button-color", Color.RED))
            putInt("button-text-color", newButtonTextColor ?: sharedPreferences.getInt("button-text-color", Color.BLACK))
            putInt("button-text-size", newButtonTextSize ?: sharedPreferences.getInt("button-text-size", 24))

            apply()
        }
    }

    private fun restartService(){
        val intent : Intent = Intent(this, shoutCaseService::class.java)

        stopService(intent)
        Log.i("restart service", "service stopped ")
        startService(intent)
        Log.i("restart service", "service started")
    }
//    END OF CLASS
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Shouty Text!",
            modifier = modifier,
        fontSize = 60.sp, // Set the font size to 24 sp (scaled pixels)
        fontWeight = FontWeight.Bold // Set the font weight to bold
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShoutCaseTheme {
        Greeting("Android")
    }
}
