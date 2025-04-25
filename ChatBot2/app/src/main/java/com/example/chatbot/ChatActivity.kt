package com.example.chatbot

import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatbot.ui.theme.ChatBotTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatBotTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen() {
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Hello! I'm Ellie ✨") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7B1FA2))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true // para que los mensajes nuevos aparezcan abajo
        ) {
            items(messages.reversed()) { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            IconButton(onClick = {
                val input = userInput
                messages.add("You: $input")
                sendMessageToServer(input) { reply ->
                    messages.add("Ellie: $reply")
                }
                userInput = ""
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
fun decodeUnicodeEscapes(text: String): String {
    return text.replace("\\u2019", "’")
        .replace("\\u00ed", "í")
        .replace("\\u00e1", "á")
        .replace("\\u00e9", "é")
        .replace("\\u00f3", "ó")
        .replace("\\u00fa", "ú")
        .replace("\\u00f1", "ñ")
        .replace("\\u00bf", "¿")
        .replace("\\u00a1", "¡")
        .replace("\\u0022", "\"")
}

fun sendMessageToServer(message: String, onResult: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("http://192.168.100.12:5000/chat")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val jsonInput = """{"message":"$message"}"""
            val outputWriter = OutputStreamWriter(connection.outputStream)
            outputWriter.write(jsonInput)
            outputWriter.flush()

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val rawReply = Regex("\"response\":\\s*\"(.*?)\"").find(response)?.groupValues?.get(1) ?: "Sin respuesta"
            val reply = decodeUnicodeEscapes(rawReply).replace("\\n", "\n")

            withContext(Dispatchers.Main) {
                onResult(reply)
            }

            connection.disconnect()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult("Error: ${e.message}")
            }
        }
    }
}
