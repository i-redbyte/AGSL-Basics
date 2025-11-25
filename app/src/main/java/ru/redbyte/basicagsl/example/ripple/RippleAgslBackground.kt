package ru.redbyte.basicagsl.example.ripple

import android.graphics.Paint
import android.graphics.RuntimeShader
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp

private const val RIPPLE_SHADER = """
uniform float2 u_resolution;
uniform float2 u_touch;
uniform float  u_time;
uniform float  u_active; // 0.0 если волны нет, 1.0 если есть

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / u_resolution;

    // Центр волны в нормализованных координатах
    float2 center = u_touch / u_resolution;

    float dist = distance(uv, center);

    // Параметры волны
    float speed = 2.0;
    float frequency = 20.0;
    float maxRadius = 0.5;

    float t = u_time * speed;

    // Базовая волна: cos по расстоянию - времени
    float wave = 0.5 + 0.5 * cos(frequency * (dist - t));

    // Маска радиуса — волна затухает с расстоянием
    float radiusMask = smoothstep(maxRadius, 0.0, dist);

    // Временная маска — со временем волна исчезает
    float timeMask = smoothstep(0.0, 1.0, 1.0 - t);

    float intensity = wave * radiusMask * timeMask * u_active;

    // Фон
    float3 baseColor = float3(0.1, 0.12, 0.18);

    // Цвет волны
    float3 rippleColor = float3(0.2, 0.6, 1.0) * intensity;

    float3 finalColor = baseColor + rippleColor;

    return half4(finalColor, 1.0);
}
"""

@Composable
fun RippleAgslBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val runtimeShader = remember { RuntimeShader(RIPPLE_SHADER) }
    val paint = remember {
        Paint().apply {
            shader = runtimeShader
        }
    }

    var timeFromTap by remember { mutableFloatStateOf(0f) }
    var lastTap by remember { mutableStateOf(Offset.Zero) }
    var isActive by remember { mutableStateOf(false) }

    // Анимация времени волны
    LaunchedEffect(isActive) {
        if (!isActive) return@LaunchedEffect

        val maxDurationSeconds = 1.5f
        var startTime = 0L

        withFrameNanos { now ->
            startTime = now
        }

        while (isActive) {
            withFrameNanos { now ->
                val dt = (now - startTime) / 1_000_000_000f
                timeFromTap = dt
                if (dt > maxDurationSeconds) {
                    isActive = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    lastTap = offset
                    timeFromTap = 0f
                    isActive = true
                }
            }
            .drawBehind {
                val width = size.width
                val height = size.height

                runtimeShader.setFloatUniform("u_resolution", width, height)
                runtimeShader.setFloatUniform("u_touch", lastTap.x, lastTap.y)
                runtimeShader.setFloatUniform("u_time", timeFromTap)
                runtimeShader.setFloatUniform("u_active", if (isActive) 1f else 0f)

                paint.apply {
                    shader = runtimeShader
                }

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRect(
                        0f,
                        0f,
                        width,
                        height,
                        paint
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun AgslRippleDemoScreen() {
    RippleAgslBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Тапни по экрану",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = 20.sp
        )
    }
}

