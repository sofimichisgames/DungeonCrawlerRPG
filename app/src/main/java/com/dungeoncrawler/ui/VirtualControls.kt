package com.dungeoncrawler.ui

data class TouchResult(
    val dx: Int = 0,
    val dy: Int = 0,
    val openInventory: Boolean = false,
    val pickUp: Boolean = false,
    val descend: Boolean = false,
    val wait: Boolean = false
) {
    val isMoveOrAction get() = dx != 0 || dy != 0 || openInventory || pickUp || descend || wait
}

class VirtualControls(var sw: Int = 1080, var sh: Int = 1920) {
    fun resize(width: Int, height: Int) {
        sw = width; sh = height
    }
    
    fun handleTouch(tx: Float, ty: Float): TouchResult? {
        return null
    }
}
