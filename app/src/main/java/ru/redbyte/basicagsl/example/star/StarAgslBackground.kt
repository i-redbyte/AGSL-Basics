package ru.redbyte.basicagsl.example.star

import android.graphics.Paint
import android.graphics.RuntimeShader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val STAR_SHADER = """
uniform float2 u_resolution;
uniform float2 u_center;
uniform float  u_active;
uniform float  u_phase;

const float PI = 3.14159265;

float sdStar5(float2 p, float rOuter, float rInnerRatio) {
    const float2 k1 = float2(0.809016994375, -0.587785252292);
    const float2 k2 = float2(-k1.x, k1.y);

    p.x = abs(p.x);
    p -= 2.0 * max(dot(k1, p), 0.0) * k1;
    p -= 2.0 * max(dot(k2, p), 0.0) * k2;
    p.x = abs(p.x);

    p.y -= rOuter;

    float rf = rInnerRatio;
    float2 ba = rf * rOuter * float2(-k1.y, k1.x) - float2(0.0, 1.0);
    float h = clamp(dot(p, ba) / dot(ba, ba), 0.0, 1.0);

    float d = length(p - ba * h);
    float s = sign(p.y * ba.x - p.x * ba.y);
    return d * s;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / u_resolution;

    float3 bg = float3(0.05, 0.07, 0.12);

    if (u_active < 0.5) {
        return half4(bg, 1.0);
    }

    float2 p = uv - u_center;
    float scale = 0.22;
    p /= scale;

    float dStar = sdStar5(p, 1.0, 0.35);

    float starMask = smoothstep(0.01, 0.0, dStar);

    float phase = clamp(u_phase, 0.0, 1.0);
    float3 colA = float3(0.85, 0.65, 0.10);
    float3 colB = float3(0.90, 0.10, 0.10);
    float3 starColor = mix(colA, colB, phase);

    float radial = 1.0 - clamp(length(p) / 1.0, 0.0, 1.0);
    starColor *= (0.9 + 0.1 * radial);

    float3 color = mix(bg, starColor, starMask);

    return half4(color, 1.0);
}
"""


@Composable
fun StarAgslBackground(
    progress: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val runtimeShader = remember { RuntimeShader(STAR_SHADER) }
    val paint = remember { Paint() }

    Box(
        modifier = modifier
            .drawBehind {
                val width = size.width
                val height = size.height

                runtimeShader.setFloatUniform("u_resolution", width, height)

                // Прогресс полёта [0, 1]
                val t = progress.coerceIn(0f, 1f)
                val u = 2f * t - 1f // [-1, 1]

                // Горизонталь: почти от левого до правого края (0.05..0.95)
                val xNorm = 0.5f + 0.45f * u

                // Парабола повыше: в центре выше, по краям ниже
                val yNorm = 0.8f - 0.45f * (1f - u * u)

                runtimeShader.setFloatUniform("u_center", xNorm, yNorm)
                runtimeShader.setFloatUniform("u_active", if (isActive) 1f else 0f)

                // Фаза цвета
                runtimeShader.setFloatUniform("u_phase", t)
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
fun AgslStarDemoScreen() {
    var isFlying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Анимация прогресса полёта звезды
    LaunchedEffect(isFlying) {
        if (!isFlying) return@LaunchedEffect

        // Время полёта
        val durationSeconds = 3.95f

        var startTime = 0L
        withFrameNanos { now ->
            startTime = now
        }

        while (isFlying) {
            withFrameNanos { now ->
                val dt = (now - startTime) / 1_000_000_000f
                val p = (dt / durationSeconds).coerceIn(0f, 1f)
                progress = p

                if (dt >= durationSeconds) {
                    isFlying = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        StarAgslBackground(
            progress = progress,
            isActive = isFlying,
            modifier = Modifier.matchParentSize()
        )

        Button(
            onClick = {
                if (!isFlying) {
                    progress = 0f
                    isFlying = true
                }
            },
            enabled = !isFlying,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = if (isFlying) "Звезда в полёте..." else "Запустить звезду",
                fontSize = 16.sp
            )
        }
    }
}
