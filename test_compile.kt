import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.runtime.Composable

@Composable
fun Test() {
    val state = rememberTextFieldState()
    OutlinedTextField(state = state, lineLimits = TextFieldLineLimits.SingleLine)
}
