package com.emm.mybest.features.history.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.usecase.history.WeightTrendPoint
import com.emm.mybest.ui.components.HEmptyState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme

private val CHART_HEIGHT = 180.dp

@Composable
fun WeightTrendChart(
    points: List<WeightTrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        HEmptyState(
            title = "Sin registros de peso",
            description = "Registra tu peso para ver tu evolución.",
            icon = Icons.AutoMirrored.Rounded.ShowChart,
            modifier = modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT),
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries { series(points.map { it.weight.toDouble() }) }
        }
    }

    val vicoTheme = remember(primaryColor, onSurfaceVariantColor, outlineVariantColor) {
        VicoTheme(
            candlestickCartesianLayerColors = VicoTheme.CandlestickCartesianLayerColors(
                bullish = primaryColor,
                neutral = onSurfaceVariantColor,
                bearish = onSurfaceVariantColor,
            ),
            columnCartesianLayerColors = listOf(primaryColor),
            lineCartesianLayerColors = listOf(primaryColor),
            pieChartColors = listOf(primaryColor),
            lineColor = outlineVariantColor,
            textColor = onSurfaceVariantColor,
        )
    }

    ProvideVicoTheme(theme = vicoTheme) {
        WeightTrendChartContent(
            primaryColor = primaryColor,
            onSurfaceVariantColor = onSurfaceVariantColor,
            outlineVariantColor = outlineVariantColor,
            modelProducer = modelProducer,
            modifier = modifier,
        )
    }
}

@Composable
private fun WeightTrendChartContent(
    primaryColor: Color,
    onSurfaceVariantColor: Color,
    outlineVariantColor: Color,
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    val line = remember(primaryColor) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(primaryColor)),
            stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
        )
    }

    val labelComponent = rememberAxisLabelComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariantColor),
    )

    val axisLine = rememberAxisLineComponent(
        fill = Fill(outlineVariantColor),
        thickness = 1.dp,
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(line),
            ),
            startAxis = VerticalAxis.rememberStart(
                label = labelComponent,
                line = axisLine,
                tick = null,
                guideline = null,
                itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { _ -> 3 }) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = labelComponent,
                line = axisLine,
                tick = null,
                guideline = null,
                itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT),
    )
}
