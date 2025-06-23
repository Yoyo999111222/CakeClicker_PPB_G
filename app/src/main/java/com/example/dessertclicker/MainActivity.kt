package com.example.dessertclicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import com.example.dessertclicker.ui.theme.DessertClickerTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(false) }
            DessertClickerTheme(useDarkTheme = darkMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    DessertClickerApp(
                        desserts = Datasource.dessertList,
                        darkMode = darkMode,
                        toggleTheme = { darkMode = !darkMode }
                    )
                }
            }
        }
    }
}

fun determineDessertToShow(desserts: List<Dessert>, dessertsSold: Int): Dessert {
    var dessertToShow = desserts.first()
    for (dessert in desserts) {
        if (dessertsSold >= dessert.startProductionAmount) {
            dessertToShow = dessert
        } else break
    }
    return dessertToShow
}

private fun shareSoldDessertsInformation(context: Context, dessertsSold: Int, revenue: Int) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, dessertsSold, revenue))
        type = "text/plain"
    }
    try {
        ContextCompat.startActivity(context, Intent.createChooser(sendIntent, null), null)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.sharing_not_available), Toast.LENGTH_LONG).show()
    }
}

@Composable
fun DessertClickerApp(
    desserts: List<Dessert>,
    darkMode: Boolean,
    toggleTheme: () -> Unit
) {
    var revenue by rememberSaveable { mutableStateOf(0) }
    var dessertsSold by rememberSaveable { mutableStateOf(0) }
    val currentDessertIndex by rememberSaveable { mutableStateOf(0) }
    var currentDessertPrice by rememberSaveable {
        mutableStateOf(desserts[currentDessertIndex].price)
    }
    var currentDessertImageId by rememberSaveable {
        mutableStateOf(desserts[currentDessertIndex].imageId)
    }
    var level by rememberSaveable { mutableStateOf(1) }
    val nextLevelThreshold = level * 10

    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        topBar = {
            DessertClickerAppBar(
                onShareButtonClicked = {
                    shareSoldDessertsInformation(context, dessertsSold, revenue)
                },
                onToggleTheme = toggleTheme,
                isDarkMode = darkMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateStartPadding(layoutDirection),
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(layoutDirection),
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        DessertClickerScreen(
            revenue = revenue,
            dessertsSold = dessertsSold,
            dessertImageId = currentDessertImageId,
            level = level,
            progress = (dessertsSold % nextLevelThreshold) / nextLevelThreshold.toFloat(),
            onDessertClicked = {
                revenue += currentDessertPrice
                dessertsSold++
                if (dessertsSold >= nextLevelThreshold) level++
                val dessertToShow = determineDessertToShow(desserts, dessertsSold)
                currentDessertImageId = dessertToShow.imageId
                currentDessertPrice = dessertToShow.price
            },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun DessertClickerAppBar(
    onShareButtonClicked: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge
        )
        Row {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onPrimary
                )

            }
            IconButton(onClick = onShareButtonClicked) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.share),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun DessertClickerScreen(
    revenue: Int,
    dessertsSold: Int,
    level: Int,
    progress: Float,
    dessertImageId: Int,
    onDessertClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 200),
        label = "imageScale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bakery_back),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(dessertImageId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                        .scale(animatedScale)
                        .clickable {
                            scale = 1.2f
                            onDessertClicked()
                            scale = 1f
                        },
                    contentScale = ContentScale.Crop
                )
            }
            LevelInfo(level = level, progress = progress)
            TransactionInfo(
                revenue = revenue,
                dessertsSold = dessertsSold,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun LevelInfo(level: Int, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Level $level", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .progressSemantics(progress)
        )
    }
}

@Composable
fun TransactionInfo(revenue: Int, dessertsSold: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(stringResource(R.string.dessert_sold))
            Text(dessertsSold.toString())
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(stringResource(R.string.total_revenue))
            Text("$${revenue}")
        }
    }
}

@Preview
@Composable
fun PreviewDessertClickerApp() {
    DessertClickerTheme {
        DessertClickerApp(
            desserts = listOf(Dessert(R.drawable.cupcake, 5, 0)),
            darkMode = false,
            toggleTheme = {}
        )
    }
}
