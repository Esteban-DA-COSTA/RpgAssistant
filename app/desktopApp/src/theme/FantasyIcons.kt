package com.estebandacosta.jdrassistant.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Icônes du thème Fantasy, tracées depuis les chemins SVG de la maquette. */
object FantasyIcons {
    val sword: ImageVector = icon("sword", 24f) {
        stroke("M12 3 V21 M6 8 H18 M8 21 H16", Color(0xFF2A1C08), 2f)
        fill("M12 3 L9.5 7 H14.5 Z", Color(0xFF2A1C08))
    }

    val star: ImageVector = icon("star", 48f) {
        fill("M24 4 L27.5 19 L42 24 L27.5 29 L24 44 L20.5 29 L6 24 L20.5 19 Z", Color(0xEBFFF0CC), Color(0xFF5A1A0C), 1.4f)
    }

    val book: ImageVector = icon("book", 32f) {
        stroke("M16 9 C12 6 7 6 3 7 V25 C7 24 12 24 16 27 C20 24 25 24 29 25 V7 C25 6 20 6 16 9 Z M16 9 V27", Color(0xFFF2FAFF), 1.8f)
    }

    val folder: ImageVector = icon("folder", 24f) {
        stroke("M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2Z", Color(0xFFF2FFF6), 1.9f)
    }

    val check: ImageVector = icon("check", 24f) {
        stroke("M5 12.5l4.5 4.5L19 7.5", Color(0xFFF2FFF5), 2.2f)
    }

    val copy: ImageVector = icon("copy", 24f) {
        stroke(
            "M9.5 8h8A1.5 1.5 0 0 1 19 9.5v8a1.5 1.5 0 0 1-1.5 1.5h-8A1.5 1.5 0 0 1 8 17.5v-8A1.5 1.5 0 0 1 9.5 8Z M5 15.5V6.5A1.5 1.5 0 0 1 6.5 5h9",
            Color(0xFF2E1E06),
            2f,
        )
    }

    /** Tracé neutre, à teinter à l'affichage. */
    val modules: ImageVector = icon("modules", 24f) {
        stroke("M3.5 4h4v16h-4Z M9.5 4h4v16h-4Z M15.5 5.2l3.4-0.9 3.6 15-3.4 0.9Z", Color.White, 1.6f)
    }

    /** Tracé neutre, à teinter à l'affichage. */
    val chevronLeft: ImageVector = icon("chevronLeft", 24f) {
        stroke("M15 6l-6 6 6 6", Color.White, 1.8f)
    }

    /** Tracé neutre, à teinter à l'affichage. */
    val chevronRight: ImageVector = icon("chevronRight", 24f) {
        stroke("M9 6l6 6-6 6", Color.White, 1.8f)
    }

    /** Tracé neutre, à teinter à l'affichage. */
    val close: ImageVector = icon("close", 24f) {
        stroke("M6 6l12 12M18 6L6 18", Color.White, 2f)
    }
}

private fun icon(name: String, viewport: Float, paths: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(name, viewport.dp, viewport.dp, viewport, viewport).apply(paths).build()

private fun ImageVector.Builder.stroke(path: String, color: Color, width: Float) {
    addPath(
        pathData = addPathNodes(path),
        stroke = SolidColor(color),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}

private fun ImageVector.Builder.fill(path: String, color: Color, strokeColor: Color? = null, strokeWidth: Float = 0f) {
    addPath(
        pathData = addPathNodes(path),
        fill = SolidColor(color),
        stroke = strokeColor?.let(::SolidColor),
        strokeLineWidth = strokeWidth,
        strokeLineJoin = StrokeJoin.Round,
    )
}
