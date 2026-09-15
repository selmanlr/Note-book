package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.NotebookViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        NotebookApp()
      }
    }
  }
}

@Composable
fun NotebookApp(
  viewModel: NotebookViewModel = viewModel()
) {
  val navController = rememberNavController()

  Surface(modifier = Modifier.fillMaxSize()) {
    NavHost(
      navController = navController,
      startDestination = "home"
    ) {
      composable("home") {
        HomeScreen(
          viewModel = viewModel,
          onNavigateToEditor = { noteId, isChecklist ->
            navController.navigate("editor/$noteId?isChecklist=$isChecklist")
          }
        )
      }

      composable(
        route = "editor/{noteId}?isChecklist={isChecklist}",
        arguments = listOf(
          navArgument("noteId") {
            type = NavType.LongType
            defaultValue = 0L
          },
          navArgument("isChecklist") {
            type = NavType.BoolType
            defaultValue = false
          }
        )
      ) { backStackEntry ->
        val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
        val isChecklist = backStackEntry.arguments?.getBoolean("isChecklist") ?: false
        NoteEditorScreen(
          noteId = noteId,
          initialIsChecklist = isChecklist,
          viewModel = viewModel,
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }
    }
  }
}

