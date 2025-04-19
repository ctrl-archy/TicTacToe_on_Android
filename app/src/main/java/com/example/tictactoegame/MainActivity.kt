package com.example.tictactoegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tictactoegame.ui.theme.TicTacToeGameTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    enum class Screen {
        NameInput,
        ModeSelection,
        Game
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicTacToeGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TicTacToeGame(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {

// globālās vērtības spēles izsekošanai, tas ieķļauj :
    //esošo ekrānu
    var currentScreen by remember { mutableStateOf(MainActivity.Screen.NameInput) }
    // spēlētāja ievadīto vārdu
    var playerName by remember { mutableStateOf("") }
    // spēles režīmu (PvC vai PvP)
    var gameMode by remember { mutableStateOf("") }

    // spēles lauks
    var board by remember { mutableStateOf(List(9) { "" }) }
    // esošais spēlētājs (X simbols sāk)
    var currentPlayer by remember { mutableStateOf("X") }
    // esošais uzvarētājs
    var winner by remember { mutableStateOf<String?>(null) }
    // uzvarētāja logs
    var showResultDialog by remember { mutableStateOf(false) }

    // spēles atsākšanas funkcija, atbrīvo sarakstu, atsāk spēlētāja gājienu no X un noņem iepriekšējo uzvarētāja paziņojumu
    fun resetGame() {
        board = List(9) { "" }
        currentPlayer = "X"
        winner = null
        showResultDialog = false
    }
/*
Pārbauda vai kāda no uzvarošām kombinācijām tiek izpildīta

    0 1 2
    3 4 5
    6 7 8

 */
    val checkWinner: () -> String? = checkWinner@{
        val combos = listOf(
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            listOf(0, 4, 8),
            listOf(2, 4, 6)
        )

        // salīdzina vai visas trīs vērtības uzvarošā kombinācijā ir vienādas
        for ((a, b, c) in combos) {
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return@checkWinner board[a]
            }
        }
        if (board.all { it.isNotEmpty() }) return@checkWinner "Draw"
        null
    }

    // Datora gājiens
    LaunchedEffect(currentPlayer, gameMode, winner) {
        // izpilda gājienu, kad ir PvC režīms un O simbola gājiens
        if (gameMode == "PvC" && currentPlayer == "O" && winner == null) {

            // taimeris, lai tiktu simulēta domāšana
            delay(500)

            // izvēlas jebkuru brīvu laukumu
            val empty = board.mapIndexed { i, v -> if (v.isEmpty()) i else null }.filterNotNull()
            if (empty.isNotEmpty()) {
                val move = empty.random()
                board = board.toMutableList().apply { this[move] = "O" }
                // pārbaude, vai spēle ir noslēgusies
                winner = checkWinner()
                if (winner == null) {
                    currentPlayer = "X"
                } else {
                    showResultDialog = true
                }
            }
        }
    }

    // trīs dažādi ekrāni aplikācijai
    when (currentScreen) {
        // Vārda ievade, kas tiek izpildīta, kā "pop-up" paziņojums, lai vērstu lietotāja uzmanību
        MainActivity.Screen.NameInput -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Enter your name") },
                text = {
                    TextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = { Text("Name") }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // maza pārbaude, lai vārds nebūtu tukšs un tad noved uz spēles režīma izvēli
                            if (playerName.isNotBlank()) {
                                currentScreen = MainActivity.Screen.ModeSelection
                            }
                        }
                    ) {
                        Text("Next")
                    }
                }
            )
        }
        // spēles režīma "spēletājs pret spēlētājs" vai "spēlētājs pret dators"
        MainActivity.Screen.ModeSelection -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // specifiska sasveicināšanās ar lietotāju
                Text("Welcome, $playerName!", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        gameMode = "PvP"
                        resetGame()
                        currentScreen = MainActivity.Screen.Game
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play PvP")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        gameMode = "PvC"
                        resetGame()
                        currentScreen = MainActivity.Screen.Game
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play PvC")
                }
                Spacer(modifier = Modifier.height(16.dp))
                // iespēja nomainīt/izlabot lietotāja vārdu
                Button(
                    onClick = {
                        resetGame()
                        playerName = ""
                        currentScreen = MainActivity.Screen.NameInput
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change name")
                }
            }
        }
        // spēles logs
        MainActivity.Screen.Game -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    // iespēja atgriezties, lai nomainītu spēles režīmu
                    Button(onClick = {
                        currentScreen = MainActivity.Screen.ModeSelection
                        resetGame()
                    }) {
                        Text("Back to Menu")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // gājiena izsekošana lietotājam
                Text(
                    text = when {
                        winner == null -> if (gameMode == "PvP") {
                            "$currentPlayer's turn"
                        } else if (currentPlayer == "X") {
                            "$playerName's turn"
                        } else "CPU's turn"
                        else -> ""
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                for (row in 0..2) {
                    Row {
                        for (col in 0..2) {
                            // spēles lauka izveide
                            val index = row * 3 + col
                            Button(
                                // spēles lauciņu pieraksts
                                onClick = {
                                    if (board[index].isEmpty() && winner == null &&
                                        (gameMode == "PvP" || currentPlayer == "X")
                                    ) {
                                        board = board.toMutableList().apply { this[index] = currentPlayer }
                                        winner = checkWinner()
                                        if (winner == null) {
                                            currentPlayer = if (currentPlayer == "X") "O" else "X"
                                        } else {
                                            showResultDialog = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp),
                                enabled = board[index].isEmpty() && winner == null &&
                                        (gameMode == "PvP" || currentPlayer == "X")
                            ) {
                                Text(board[index], style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    // "pop-up" paziņojums par uzvarošo spēlētāju
    if (showResultDialog && winner != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = {
                Text(
                    when (winner) {
                        "Draw" -> "It's a draw!"
                        // X ir pirmā spēlētāja gājiens tādējādi, ja uzvarošais simbols ir X, ir uzvarējs lietotājs PvC režīmā vai PvP režīmā tas kurš lietoja X simbolu
                        "X" -> if (gameMode == "PvC") "$playerName wins!" else "Player X wins!"
                        // O ir otrā spēlētāja gājiens, PvC gadījumā tas ir dators un PvP režīmā tas ir simbola O spēlētājs
                        "O" -> if (gameMode == "PvC") "CPU wins!" else "Player O wins!"
                        else -> ""
                    }
                )
            },
            // turpat arī poga, lai spēli sāktu no jauna vai atkal dotu iespēju atgriezties uz režīma izvēles logu
            confirmButton = {
                Button(onClick = {
                    resetGame()
                    showResultDialog = false
                }) {
                    Text("Play Again")
                }
            }
        )
    }

}



