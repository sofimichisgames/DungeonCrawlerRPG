package com.dungeoncrawler

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.dungeoncrawler.game.GameEngine
import com.dungeoncrawler.ui.Renderer
import com.dungeoncrawler.ui.VirtualControls
import com.dungeoncrawler.ui.BitmapManager

// =============================================================================
// GameView.kt — SurfaceView con game loop propio en un hilo dedicado
// =============================================================================
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val engine   = GameEngine()
    private val bitmapManager = BitmapManager()
    private val renderer: Renderer
    private val controls: VirtualControls
    private var thread: GameThread? = null

    init {
        // Obtener dimensiones iniciales de la pantalla
        val dm  = context.resources.displayMetrics
        val sw  = dm.widthPixels
        val sh  = dm.heightPixels

        // Pre-load all item bitmaps before renderer initialization
        bitmapManager.preloadAllItems()

        renderer = Renderer(sw, sh, bitmapManager)
        controls = VirtualControls(sw, sh)

        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    // ------------------------------------------------------------------
    // SurfaceHolder callbacks
    // ------------------------------------------------------------------
    override fun surfaceCreated(holder: SurfaceHolder) {
        engine.newGame()
        thread = GameThread(holder).also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        renderer.resize(width, height)
        controls.resize(width, height)
        engine.needsRedraw = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread?.apply { running = false; join() }
        thread = null
    }

    // ------------------------------------------------------------------
    // Game Loop Thread
    // ------------------------------------------------------------------
    inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        @Volatile var running = true
        private val targetFps = 60L
        private val frameDurationMs = 1000L / targetFps
        private var lastHeldMoveTime = 0L
        private var heldInputStartTime = 0L
        private var lastHeldInputHash = 0
        private val heldMoveStartDelayMs = 1000L // Wait 1 sec before repeating
        private val heldMoveDelayMs = 500L // Rate limit held moves to 2 per second

        override fun run() {
            while (running) {
                val startMs = System.currentTimeMillis()

                // Apply continuous held input (e.g., held direction arrow)
                if (engine.state == Config.STATE_PLAYING) {
                    val heldInput = controls.getCurrentHeldInput()
                    if (heldInput != null) {
                        val inputHash = (heldInput.dx * 31 + heldInput.dy).hashCode() xor heldInput.wait.hashCode()
                        // Reset timer if input changed
                        if (inputHash != lastHeldInputHash) {
                            heldInputStartTime = startMs
                            lastHeldInputHash = inputHash
                        }
                        // Only trigger repeating moves after 1 sec of holding, then every 500ms
                        if (startMs - heldInputStartTime >= heldMoveStartDelayMs && startMs - lastHeldMoveTime >= heldMoveDelayMs) {
                            when {
                                heldInput.wait -> engine.playerWait()
                                heldInput.dx != 0 || heldInput.dy != 0 -> engine.playerMove(heldInput.dx, heldInput.dy)
                            }
                            lastHeldMoveTime = startMs
                        }
                    } else {
                        // Input released, reset state
                        heldInputStartTime = 0L
                        lastHeldInputHash = 0
                    }
                }

                if (engine.needsRedraw) {
                    var canvas: Canvas? = null
                    try {
                        canvas = surfaceHolder.lockCanvas()
                        if (canvas != null) {
                            synchronized(surfaceHolder) {
                                renderer.render(canvas, engine, controls)
                            }
                            engine.needsRedraw = false
                        }
                    } finally {
                        canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
                    }
                }

                val elapsed = System.currentTimeMillis() - startMs
                val sleep   = frameDurationMs - elapsed
                if (sleep > 0) sleep(sleep)
            }
        }
    }

    // ------------------------------------------------------------------
    // Touch input
    // ------------------------------------------------------------------
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val tx = event.x
        val ty = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (engine.state) {
                    Config.STATE_MENU, Config.STATE_GAME_OVER, Config.STATE_VICTORY -> {
                        engine.newGame()
                        engine.needsRedraw = true
                    }
                    Config.STATE_INVENTORY -> {
                        handleInventoryTouch(tx, ty)
                    }
                    Config.STATE_PLAYING -> {
                        val result = controls.handleTouch(tx, ty)
                        if (result != null && result.isMoveOrAction) {
                            when {
                                result.openInventory -> {
                                    engine.state = Config.STATE_INVENTORY
                                    engine.needsRedraw = true
                                }
                                result.pickUp   -> engine.playerPickUp()
                                result.descend  -> engine.playerDescend()
                                result.wait     -> engine.playerWait()
                                else -> engine.playerMove(result.dx, result.dy) // Move immediately for instant feedback
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (engine.state == Config.STATE_PLAYING) {
                    // Only update held input on move, not on initial down
                    controls.updateTouchMove(tx, ty)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                controls.releaseTouched()
                // Clear held input to prevent double move
                if (engine.state == Config.STATE_PLAYING) {
                    controls.releaseTouched()
                }
            }
        }
        return true
    }

    private fun handleInventoryTouch(tx: Float, ty: Float) {
        val sw = width.toFloat()
        val sh = height.toFloat()
        // Área de la ventana de inventario
        val ow = sw * 0.75f; val oh = sh * 0.80f
        val ox = (sw - ow) / 2f; val oy = (sh - oh) / 2f

        // Cerrar si toca fuera
        if (tx < ox || tx > ox + ow || ty < oy || ty > oy + oh) {
            engine.state = Config.STATE_PLAYING
            engine.needsRedraw = true
            return
        }

        // Detectar qué ítem se tocó usando grid
        val fs = oh * 0.045f
        val margin = ow * 0.03f
        val gridCols = 3
        val gridStartY = oy + fs * 4.0f
        val cellW = (ow - margin * 2) / gridCols
        val cellH = fs * 2.5f

        // Only process touches within grid area
        if (ty < gridStartY) {
            engine.needsRedraw = true
            return
        }

        // Calculate grid position
        val relX = tx - (ox + margin)
        val relY = ty - gridStartY

        if (relX < 0 || relY < 0) {
            engine.needsRedraw = true
            return
        }

        val col = (relX / cellW).toInt()
        val row = (relY / cellH).toInt()

        if (col >= 0 && col < gridCols) {
            val idx = row * gridCols + col
            if (idx in engine.player.inventory.indices) {
                engine.useInventoryItem(idx)
            }
        }
        engine.needsRedraw = true
    }

    // ------------------------------------------------------------------
    // Teclado físico (opcional, para emulador / teclado Bluetooth)
    // ------------------------------------------------------------------
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (engine.state) {
            Config.STATE_MENU, Config.STATE_GAME_OVER, Config.STATE_VICTORY -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE) {
                    engine.newGame(); engine.needsRedraw = true
                }
            }
            Config.STATE_PLAYING -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP,    KeyEvent.KEYCODE_W -> engine.playerMove(0, -1)
                    KeyEvent.KEYCODE_DPAD_DOWN,  KeyEvent.KEYCODE_S -> engine.playerMove(0,  1)
                    KeyEvent.KEYCODE_DPAD_LEFT,  KeyEvent.KEYCODE_A -> engine.playerMove(-1, 0)
                    KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> engine.playerMove( 1, 0)
                    KeyEvent.KEYCODE_SPACE   -> engine.playerWait()
                    KeyEvent.KEYCODE_G       -> engine.playerPickUp()
                    KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_COMMA -> engine.playerDescend()
                    KeyEvent.KEYCODE_I       -> { engine.state = Config.STATE_INVENTORY; engine.needsRedraw = true }
                    else -> return super.onKeyDown(keyCode, event)
                }
            }
            Config.STATE_INVENTORY -> {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                    engine.state = Config.STATE_PLAYING; engine.needsRedraw = true
                } else {
                    val idx = keyCode - KeyEvent.KEYCODE_A
                    if (idx in 0 until 26) engine.useInventoryItem(idx)
                }
            }
        }
        return true
    }
}
