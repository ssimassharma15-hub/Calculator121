package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.outlined.KeyboardBackspace
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class KeyType {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS
}

data class CalcKey(
    val label: String,
    val type: KeyType,
    val testTag: String
)

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val history by viewModel.history.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    // Cyber slate theme background with dynamic diagonal gradients
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF030712)  // Gray 950
        )
    )

    Box(
        modifier = modifier
            .background(bgGradient)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALCULATOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFF6366F1) // Indigo accent
                )

                Row {
                    IconButton(
                        onClick = { showHistory = !showHistory },
                        modifier = Modifier.testTag("toggle_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Show History",
                            tint = if (showHistory) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Display & History Panel Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Current Expression
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expression.ifEmpty { "0" },
                            style = TextStyleForExpression(expression),
                            color = Color.White,
                            textAlign = TextAlign.End,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize()
                                .testTag("expression_text")
                        )

                        if (expression.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onKeyPress("⌫") },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .testTag("key_backspace")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardBackspace,
                                    contentDescription = "Backspace",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    // Live Preview Result
                    AnimatedVisibility(
                        visible = previewResult.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Text(
                            text = previewResult,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF94A3B8), // Slate 400
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("preview_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Overlay History Pane
                androidx.compose.animation.AnimatedVisibility(
                    visible = showHistory,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    HistoryPane(
                        history = history,
                        onItemClick = { item ->
                            // Put either expression or result back in
                            viewModel.onKeyPress("AC")
                            item.expression.forEach { viewModel.onKeyPress(it.toString()) }
                            showHistory = false
                        },
                        onClearClick = { viewModel.clearHistory() },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

            // Keypad Layout (Bounded Max Width for Tablet Support)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030712)) // Deep Gray background for keys
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .align(Alignment.Center)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val keys = listOf(
                        listOf(
                            CalcKey("AC", KeyType.ACTION, "key_clear"),
                            CalcKey("(", KeyType.ACTION, "key_paren_open"),
                            CalcKey(")", KeyType.ACTION, "key_paren_close"),
                            CalcKey("÷", KeyType.OPERATOR, "key_divide")
                        ),
                        listOf(
                            CalcKey("7", KeyType.NUMBER, "key_7"),
                            CalcKey("8", KeyType.NUMBER, "key_8"),
                            CalcKey("9", KeyType.NUMBER, "key_9"),
                            CalcKey("×", KeyType.OPERATOR, "key_multiply")
                        ),
                        listOf(
                            CalcKey("4", KeyType.NUMBER, "key_4"),
                            CalcKey("5", KeyType.NUMBER, "key_5"),
                            CalcKey("6", KeyType.NUMBER, "key_6"),
                            CalcKey("-", KeyType.OPERATOR, "key_subtract")
                        ),
                        listOf(
                            CalcKey("1", KeyType.NUMBER, "key_1"),
                            CalcKey("2", KeyType.NUMBER, "key_2"),
                            CalcKey("3", KeyType.NUMBER, "key_3"),
                            CalcKey("+", KeyType.OPERATOR, "key_add")
                        ),
                        listOf(
                            CalcKey("+/-", KeyType.ACTION, "key_sign"),
                            CalcKey("0", KeyType.NUMBER, "key_0"),
                            CalcKey(".", KeyType.NUMBER, "key_dot"),
                            CalcKey("=", KeyType.EQUALS, "key_equals")
                        )
                    )

                    keys.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowKeys.forEach { key ->
                                CalculatorButton(
                                    key = key,
                                    onClick = { viewModel.onKeyPress(key.label) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextStyleForExpression(expr: String): androidx.compose.ui.text.TextStyle {
    val length = expr.length
    val fontSize = when {
        length > 25 -> 24.sp
        length > 15 -> 32.sp
        else -> 46.sp
    }
    return MaterialTheme.typography.headlineLarge.copy(
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = fontSize * 1.2f,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun CalculatorButton(
    key: CalcKey,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (key.type) {
        KeyType.NUMBER -> Color(0xFF1E293B) // Dark slate
        KeyType.OPERATOR -> Color(0xFF312E81) // Deep Indigo
        KeyType.ACTION -> Color(0xFF334155) // Slate 700
        KeyType.EQUALS -> Color(0xFF6366F1) // Bright Indigo Accent
    }

    val contentColor = when (key.type) {
        KeyType.OPERATOR -> Color(0xFF818CF8) // Indigo 400 glow
        KeyType.ACTION -> Color(0xFFE2E8F0) // Off white
        KeyType.EQUALS -> Color.White
        else -> Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .testTag(key.testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key.label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (key.label.length > 2) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            color = contentColor
        )
    }
}

@Composable
fun HistoryPane(
    history: List<HistoryItem>,
    onItemClick: (HistoryItem) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Scroll to top on first render or when history updates
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculation History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = onClearClick,
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No calculations yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable { onItemClick(item) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.expression,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "= ${item.result}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981), // Green result
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
