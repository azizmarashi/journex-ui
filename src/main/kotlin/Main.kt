import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.example.journexui.AuthStore
import com.example.journexui.ui.AppScreen
import com.example.journexui.ui.AuthScreens
import com.example.journexui.network.RetrofitClient
import kotlinx.coroutines.launch

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Journex", state = rememberWindowState(width = 1400.dp, height = 900.dp)) {
        MaterialTheme {
            Surface {
                var token by remember { mutableStateOf(AuthStore.token) }
                val scope = rememberCoroutineScope()
                if (token == null) AuthScreens(
                    onLogin = { AuthStore.token = it; token = it },
                    onRegister = {}
                ) else AppScreen(
                    token = token!!,
                    onLogout = { scope.launch { try { RetrofitClient.api.logout("Bearer $token") } catch (_: Exception) {} finally { AuthStore.token = null; token = null } } }
                )
            }
        }
    }
}
