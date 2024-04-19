package net.festinger.historia

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.festinger.historia.HistoriaService

class HistoriaReceiver : BroadcastReceiver() {
    // android.content.BroadcastReceiver
    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, HistoriaService::class.java))
    }
}