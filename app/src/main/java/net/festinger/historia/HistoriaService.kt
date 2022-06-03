package net.festinger.historia

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.provider.CallLog
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.*


class HistoriaService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // android.app.Service
    override fun onCreate() {
        super.onCreate()
        getApplicationContext<Context>().contentResolver
            .registerContentObserver(
                CallLog.Calls.CONTENT_URI,
                true,
                CallLogContentObserver(Handler(), ApplicationProvider.getApplicationContext())
            )
    }

    // android.app.Service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

}
