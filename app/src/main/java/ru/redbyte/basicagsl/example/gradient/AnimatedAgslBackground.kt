package ru.redbyte.basicagsl.example.gradient

import android.graphics.Paint
import android.graphics.RuntimeShader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive

private const val ANIMATED_GRADIENT_SHADER = """
uniform float2 u_resolution;
uniform float  u_time;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / u_resolution;

    // Немного волшебства синусов
    float t = u_time;

    float r = 0.5 + 0.5 * sin(3.0 * uv.x + t * 0.7);
    float g = 0.5 + 0.5 * sin(3.0 * uv.y + t * 1.1);
    float b = 0.5 + 0.5 * sin(3.0 * (uv.x + uv.y) + t * 0.9);

    return half4(r, g, b, 1.0);
}
"""

@Composable
fun AnimatedAgslBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val runtimeShader = remember {
        RuntimeShader(ANIMATED_GRADIENT_SHADER)
    }

    val paint = remember { Paint() }

    var timeSeconds by remember { mutableFloatStateOf(0f) }

    // Анимация времени — корутина, синхронизированная с кадром
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                timeSeconds = frameTimeNanos / 1_000_000_000f
            }
        }
    }

    Box(
        modifier = modifier
            .drawBehind {
                val width = size.width
                val height = size.height

                // Пробрасываем uniform-ы в шейдер
                runtimeShader.setFloatUniform("u_resolution", width, height)
                runtimeShader.setFloatUniform("u_time", timeSeconds)

                val frameworkPaint = paint.apply {
                    shader = runtimeShader
                }

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRect(
                        0f,
                        0f,
                        width,
                        height,
                        frameworkPaint
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun AgslGradientDemoScreen() {
    AnimatedAgslBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        // Любой контент поверх анимированного фона
        Text(
            text = "Hello, AGSL!",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = 28.sp
        )
    }
}
