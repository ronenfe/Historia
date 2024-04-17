package net.festinger.historia


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.provider.CallLog
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.FusedLocationApi
import java.util.*

internal class CallLogContentObserver(h: Handler?, private val context: Context) :
    ContentObserver(h) {
    private var lastCallStringSN = ""
    private var mLastLocation: Location? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mContext = context
    // android.database.ContentObserver
    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    val lastCall: CallStrings?
        @SuppressLint("MissingPermission")
        get() {
            val callStrings: CallStrings?
            val strFields = arrayOf( CallLog.Calls.NUMBER, CallLog.Calls.TYPE,  CallLog.Calls.CACHED_NAME,  CallLog.Calls.DATE,  CallLog.Calls.DURATION,  CallLog.Calls.TYPE)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    "android.permission.READ_CALL_LOG"
                ) != 0
            ) {
                ActivityCompat.requestPermissions(
                    (context as Activity),
                    arrayOf("android.permission.READ_CALL_LOG"),
                    1
                )
            }
            val logs = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI.buildUpon().appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                    .build(),
                strFields,
                null,
                null,
                "date DESC"
            )
            val numberIndex = logs!!.getColumnIndex( CallLog.Calls.NUMBER)
            val nameIndex = logs.getColumnIndex( CallLog.Calls.CACHED_NAME)
            val typeIndex = logs.getColumnIndex( CallLog.Calls.TYPE)
            val dateIndex = logs.getColumnIndex( CallLog.Calls.DATE)
            val durationIndex = logs.getColumnIndex( CallLog.Calls.DURATION)
            if (logs.moveToNext()) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        "android.permission.ACCESS_FINE_LOCATION"
                    ) != 0
                ) {
                    ActivityCompat.requestPermissions(
                        (context as Activity),
                        arrayOf("android.permission.ACCESS_FINE_LOCATION"),
                        1
                    )
                }
                if (ActivityCompat.checkSelfPermission(
                        context,
                        "android.permission.ACCESS_COARSE_LOCATION"
                    ) != 0
                ) {
                    ActivityCompat.requestPermissions(
                        (context as Activity),
                        arrayOf("android.permission.ACCESS_COARSE_LOCATION"),
                        1
                    )
                }
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        mLastLocation = location
                    }
                callStrings = CallStrings(
                    logs.getString(numberIndex),
                    logs.getString(nameIndex),
                    logs.getLong(dateIndex),
                    logs.getString(durationIndex),
                    logs.getInt(typeIndex),
                    mLastLocation,
                    context
                )
            } else {
                callStrings = null
            }
            logs.close()
            return callStrings
        }

    fun addCalendarEvent(callStrings: CallStrings) {
        try {
            var _id = "-1"
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.ACCOUNT_NAME, CalendarContract.Events.CALENDAR_DISPLAY_NAME, CalendarContract.Events.OWNER_ACCOUNT),
                null,
                null,
                null
            )
            while (cursor!!.moveToNext()) {
                _id = cursor.getString(0)
                if (cursor.getString(2) != "0") {
                    break
                }
            }
            cursor.close()
            if (_id.compareTo("-1") != 0) {
                val eventValues = ContentValues()
                eventValues.put(CalendarContract.Events.CALENDAR_ID, Integer.valueOf(_id.toInt()))
                var addString: String = callStrings.strings[4].toString() + " call "
                if (callStrings.strings[4] == "Outgoing" && callStrings.strings[3].equals("00:00")
                ) {
                    addString = "Unanswered call "
                }
                var addString2 = addString + if (callStrings.strings[4] == "Outgoing") "to: " else "from: "
                if (callStrings.strings[1] != "Unknown") {
                    addString2 = addString2 + callStrings.strings[1].toString() + ", "
                }
                eventValues.put(CalendarContract.Events.TITLE, addString2 + callStrings.strings[0])
                if (callStrings.strings[5] != "Not Available") {
                    eventValues.put(CalendarContract.Events.EVENT_LOCATION, callStrings.strings[5])
                }
                eventValues.put(CalendarContract.Events.DESCRIPTION, "Duration: " + callStrings.strings[3])
                eventValues.put(
                    CalendarContract.Events.DTSTART,
                    callStrings.strings[6]?.toLong() ?:  0
                )
                eventValues.put(
                    CalendarContract.Events.DTEND,
                        (callStrings.strings[6]?.toLong() ?: 0) + callStrings.duration * 1000
                )
                eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE)

                context.contentResolver.insert(
                    CalendarContract.Events.CONTENT_URI,
                    eventValues
                )
            }

        } catch (e: Exception) {
            throw e
        }
    }

    // android.database.ContentObserver
    override fun onChange(selfChange: Boolean) {
        var callStrings: CallStrings
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val isLogEnabled = sharedPref.getBoolean("switch_preference_1", true)
        if (isLogEnabled){
            callStrings = lastCall!!
            if (callStrings != null && callStrings.strings[6] != this.lastCallStringSN ) {
                this.lastCallStringSN = callStrings.strings[6]!!;
                addCalendarEvent(callStrings);
            }
        }
    }

    // com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
    fun onConnectionFailed(connectionResult: ConnectionResult) {}

    init {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
    }
}