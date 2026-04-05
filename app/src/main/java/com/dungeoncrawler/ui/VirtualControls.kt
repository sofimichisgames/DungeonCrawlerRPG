package com.dungeoncrawler.ui

// =============================================================================
// VirtualControls.kt — D-pad táctil y botones de acción
// =============================================================================
class VirtualControls(sw: Int, sh: Int) {

    data class TouchResult(
        val isMoveOrAction: Boolean = false,
        val dx: Int = 0,
        val dy: Int = 0,
        val openInventory: Boolean = false,
        val pickUp: Boolean = false,
        val descend: Boolean = false,
        val wait: Boolean = false
    )

    // Zonas de botones (se actualizan con resize)
    private var screenW = sw.toFloat()
    private var screenH = sh.toFloat()

    fun resize(w: Int, h: Int) {
        screenW = w.toFloat()
        screenH = h.toFloat()
    }

    fun handleTouch(tx: Float, ty: Float): TouchResult? {
        // El D-pad ocupa el tercio inferior izquierdo de la pantalla
        // Los botones de acción están en el tercio inferior derecho
        val padSize = screenW * 0.38f
        val btnSize = padSize / 3f

        val padLeft  = screenW * 0.02f
        val padBottom = screenH - screenH * 0.02f
        val padTop   = padBottom - padSize

        // Centro del D-pad
        val cx = padLeft + padSize / 2f
        val cy = padTop  + padSize / 2f

        // Zona D-pad
        if (tx >= padLeft && tx <= padLeft + padSize &&
            ty >= padTop  && ty <= padBottom) {

            val relX = tx - cx
            val relY = ty - cy
            val threshold = btnSize * 0.6f

            return when {
                // Centro = esperar
                Math.abs(relX) < threshold && Math.abs(relY) < threshold ->
                    TouchResult(isMoveOrAction = true, wait = true)
                // Vertical domina
                Math.abs(relY) > Math.abs(relX) ->
                    TouchResult(isMoveOrAction = true, dy = if (relY < 0) -1 else 1)
                // Horizontal domina
                else ->
                    TouchResult(isMoveOrAction = true, dx = if (relX < 0) -1 else 1)
            }
        }

        // Botones de acción (derecha inferior)
        val actRight  = screenW - screenW * 0.02f
        val actBottom = screenH - screenH * 0.02f
        val actTop    = actBottom - padSize
        val actLeft   = actRight - padSize

        if (tx >= actLeft && tx <= actRight &&
            ty >= actTop  && ty <= actBottom) {

            val col = ((tx - actLeft) / btnSize).toInt().coerceIn(0, 2)
            val row = ((ty - actTop)  / btnSize).toInt().coerceIn(0, 2)

            return when {
                // Fila superior: [INV] [,] [G]
                row == 0 && col == 0 -> TouchResult(isMoveOrAction = true, openInventory = true)
                row == 0 && col == 1 -> TouchResult(isMoveOrAction = true, descend = true)
                row == 0 && col == 2 -> TouchResult(isMoveOrAction = true, pickUp = true)
                // Fila media-baja: diagonales y espera extra
                row == 1 && col == 0 -> TouchResult(isMoveOrAction = true, dx = -1, dy = -1)
                row == 1 && col == 1 -> TouchResult(isMoveOrAction = true, wait = true)
                row == 1 && col == 2 -> TouchResult(isMoveOrAction = true, dx =  1, dy = -1)
                row == 2 && col == 0 -> TouchResult(isMoveOrAction = true, dx = -1, dy =  1)
                row == 2 && col == 2 -> TouchResult(isMoveOrAction = true, dx =  1, dy =  1)
                else -> null
            }
        }

        return null
    }

    /** Dimensiones de la zona de juego (mapa) — excluyendo controles y HUD */
    fun mapAreaBounds(screenW: Float, screenH: Float): FloatArray {
        // Reservamos 40% inferior para controles, y HUD_HEIGHT arriba
        val hudH = screenH * 0.08f
        val ctrlH = screenH * 0.42f
        return floatArrayOf(0f, hudH, screenW, screenH - ctrlH)
    }
}
