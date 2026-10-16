package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppHeader
import com.example.ui.dialogs.NotificationCenterBottomSheet
import com.example.ui.screens.AdminHomeScreen
import com.example.ui.screens.TeacherHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.UserRole
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: SchoolViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNotificationCenter by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppHeader(
                    currentRole = uiState.currentRole,
                    onRoleChange = { viewModel.setRole(it) },
                    selectedSemester = uiState.selectedSemester,
                    onSemesterSelect = { viewModel.selectSemester(it) },
                    allGroups = uiState.allGroups,
                    selectedGroup = uiState.selectedGroup,
                    onGroupSelect = { viewModel.selectGroup(it) },
                    unreadNotifications = uiState.unreadNotificationsCount,
                    onOpenNotifications = { showNotificationCenter = true }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isWideScreen) Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            else Modifier
                        )
                ) {
                    // Interactive Feedback Banner
                    AnimatedVisibility(
                        visible = uiState.userFeedbackMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.userFeedbackMessage?.let { msg ->
                            FeedbackBanner(
                                message = msg,
                                isSuccess = uiState.isSuccessFeedback,
                                onDismiss = { viewModel.clearFeedback() }
                            )
                        }
                    }

                    // Role Screen Routing
                    when (uiState.currentRole) {
                        UserRole.ADMINISTRATOR -> AdminHomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        UserRole.TEACHER -> TeacherHomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showNotificationCenter) {
        NotificationCenterBottomSheet(
            notifications = uiState.notifications,
            onDismiss = { showNotificationCenter = false },
            onMarkAsRead = { viewModel.markNotificationAsRead(it) }
        )
    }
}

@Composable
fun FeedbackBanner(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        delay(4000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF16A34A) else Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSuccess) Color(0xFF166534) else Color(0xFF991B1B),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = if (isSuccess) Color(0xFF166534) else Color(0xFF991B1B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
