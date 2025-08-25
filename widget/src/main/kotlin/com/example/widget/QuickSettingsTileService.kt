package com.voltai.widget

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QuickSettingsTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        // Toggle the tile state or launch an activity
        qsTile.state = if (qsTile.state == Tile.STATE_ACTIVE) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        qsTile.icon = Icon.createWithResource(this, android.R.drawable.ic_lock_power_off)
        qsTile.updateTile()

        // Example: Launch an activity
        // val intent = Intent(this, MainActivity::class.java)
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // startActivityAndCollapse(intent)
    }

    private fun updateTile() {
        qsTile?.let {
            it.state = Tile.STATE_INACTIVE // Default state
            it.label = "VoltAI Saver"
            it.icon = Icon.createWithResource(this, android.R.drawable.ic_lock_power_off)
            it.updateTile()
        }
    }
}
