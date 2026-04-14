package com.hadify.NumberMerge2048

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		configureImmersiveMode()
		MobileAds.initialize(this)
		setContent {
			NumberMergeTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = Color.Transparent,
					contentColor = TextPrimary,
				) {
					AppBackgroundLayer {
						NumberMergeApp()
					}
				}
			}
		}
	}

	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (hasFocus) {
			hideSystemBars()
		}
	}

	private fun configureImmersiveMode() {
		WindowCompat.setDecorFitsSystemWindows(window, false)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			window.attributes = window.attributes.apply {
				layoutInDisplayCutoutMode =
					WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
			}
		}
		hideSystemBars()
	}

	private fun hideSystemBars() {
		val controller = WindowInsetsControllerCompat(window, window.decorView)
		controller.systemBarsBehavior =
			WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
		controller.hide(WindowInsetsCompat.Type.systemBars())
	}
}

@Composable
private fun NumberMergeTheme(content: @Composable () -> Unit) {
	val colors = darkColorScheme(
		primary = OrangePrimary,
		onPrimary = Color(0xFF1A1208),
		primaryContainer = Color(0xFF4A3521),
		onPrimaryContainer = Color(0xFFFEE9D5),
		secondary = GreenPrimary,
		onSecondary = Color(0xFF0F1E15),
		secondaryContainer = Color(0xFF22382C),
		onSecondaryContainer = Color(0xFFDDF8E5),
		background = AppBackground,
		onBackground = TextPrimary,
		surface = PanelBackground,
		onSurface = TextPrimary,
		tertiary = BlueAccent,
		onTertiary = Color(0xFF111D2D),
		error = Color(0xFFD65E5E),
		onError = Color.White,
	)

	val base = Typography()
	MaterialTheme(
		colorScheme = colors,
		typography = Typography(
			displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black),
			headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
			titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
			titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
			bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
			bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
			labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
		),
		content = content,
	)
}

@Composable
private fun AppBackgroundLayer(content: @Composable () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.verticalGradient(
					colors = listOf(
						Color(0xFF16141F),
						Color(0xFF121721),
						Color(0xFF17131C),
					)
				)
			),
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.radialGradient(
						colors = listOf(
							Color(0x33FFB347),
							Color.Transparent,
						),
						radius = 900f,
					)
				)
		)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.radialGradient(
						colors = listOf(
							Color(0x223A8DFF),
							Color.Transparent,
						),
						radius = 800f,
					)
				)
		)
		content()
	}
}

val AppBackground = Color(0xFF14131B)
val OrangePrimary = Color(0xFFFFA938)
val GreenPrimary = Color(0xFF63D883)
val BlueAccent = Color(0xFF69B3FF)
val PanelBackground = Color(0xFF23202C)
val PanelBackgroundSoft = Color(0xFF2A2633)
val PanelBorder = Color(0x40FFFFFF)
val TextPrimary = Color(0xFFF6F2E9)
val TextSecondary = Color(0xFFC0B7AA)
val BoardBackground = Color(0xFF2E2A38)
val EmptyTileColor = Color(0xFF3A3644)
val SelectionColor = Color(0xFFFFC65B)
val FrozenColor = Color(0xFF78D6FF)

fun readableTextOn(background: Color): Color {
	return if (background.luminance() >= 0.42f) Color(0xFF18110B) else Color.White
}
