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

// =============================================================================
// GameView.kt — SurfaceView con game loop propio en un hilo dedicado
// =============================================================================
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val engine   = GameEngine()
    private val renderer: Renderer
    private val controls: VirtualControls
    private var thread: GameThread? = null

    init {
        // Obtener dimensiones iniciales de la pantalla
        val dm  = context.resources.displayMetrics
        val sw  = dm.widthPixels
        val sh  = dm.heightPixels
        renderer = Renderer(sw, sh)
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

        override fun run() {
            while (running) {
                val startMs = System.currentTimeMillis()

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
        if (event.action != MotionEvent.ACTION_DOWN) return true

        val tx = event.x
        val ty = event.y

        when (engine.state) {
            Config.STATE_MENU, Config.STATE_GAME_OVER, Config.STATE_VICTORY -> {
                engine.newGame()
                engine.needsRedraw = true
                return true
            }
            Config.STATE_INVENTORY -> {
                handleInventoryTouch(tx, ty)
                return true
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
                        else            -> engine.playerMove(result.dx, result.dy)
                    }
                }
            }
        }
        return true
    }

    private fun handleInventoryTouch(tx: Float, ty: Float) {
        val sw = width.toFloat()
        val sh = height.toFloat()
        // Área de la ventana de inventario
        val ow = sw * 0.55f; val oh = sh * 0.75f
        val ox = (sw - ow) / 2f; val oy = (sh - oh) / 2f

        // Cerrar si toca fuera
        if (tx < ox || tx > ox + ow || ty < oy || ty > oy + oh) {
            engine.state = Config.STATE_PLAYING
            engine.needsRedraw = true
            return
        }

        // Detectar qué ítem se tocó por posición vertical
        val fs    = oh * 0.05f
        val startY = oy + fs * 4.3f
        val lineH  = fs * 1.25f
        val idx = ((ty - startY) / lineH).toInt()
        if (idx in engine.player.inventory.indices) {
            engine.useInventoryItem(idx)
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
