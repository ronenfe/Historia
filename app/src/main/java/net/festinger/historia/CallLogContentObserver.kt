package net.festinger.historia


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.location.Location
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.provider.CallLog
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.time.LocalTime
import java.util.TimeZone

internal class CallLogContentObserver(h: Handler?, private val context: Context) :
    ContentObserver(h) {
    private var mLastLocation: Location? = null
    private var mFusedLocationClient: FusedLocationProviderClient
    private var mContext = context
    private var lastTimeofCall = 0L
    private var lastTimeofUpdate = 0L
    private var threshold_time: Long = 1000
    // android.database.ContentObserver
    override fun deliverSelfNotifications(): Boolean {
        return true
    }
    @SuppressLint("MissingPermission")
    fun getCall(): Call?{
            val call: Call?
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
                call = Call(
                    logs.getString(numberIndex),
                    logs.getString(nameIndex),
                    logs.getLong(dateIndex),
                    logs.getLong(durationIndex),
                    logs.getInt(typeIndex),
                    mLastLocation,
                    context
                )
            } else {
                call = null
            }
            logs.close()
            return call
        }

    fun addCalendarEvent(call: Call) {
        try {
            var _id = -1
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.OWNER_ACCOUNT),
                CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )
            val idIndex = cursor?.getColumnIndex(CalendarContract.Calendars._ID)
            val displayNameIndex = cursor?.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    _id = idIndex?.let { cursor.getInt(it) }!!
                    if (displayNameIndex?.let { cursor.getString(it) } != "0") {
                        break
                    }
                }
                cursor.close()
            }
            if (_id != -1) {
                val eventValues = ContentValues()
                eventValues.put(CalendarContract.Events.CALENDAR_ID, _id)
                var text: String = call.type.toString() + " call "
                if (call.type == Call.Types.Outgoing && call.duration == 0L)
                {
                    text = "Unanswered call "
                }
                text += if (call.type == Call.Types.Outgoing) "to: " else "from: "
                if (call.name.isEmpty() == false) {
                    text = text + call.name + ", "
                }
                eventValues.put(CalendarContract.Events.TITLE, text + call.number)
                if (call.location != "Not Available") {
                    eventValues.put(CalendarContract.Events.EVENT_LOCATION, call.location)
                }
                eventValues.put(CalendarContract.Events.DESCRIPTION, "Duration: " + LocalTime.ofSecondOfDay(call.duration))
                eventValues.put(
                    CalendarContract.Events.DTSTART,
                    call.time
                )
                eventValues.put(
                    CalendarContract.Events.DTEND,
                        call.time + call.duration * 1000
                )
                eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

                context.contentResolver.insert(
                    CalendarContract.Events.CONTENT_URI,
                    eventValues
                )
            }

        } catch (e: Exception) {
            throw e
        }
    }

    override fun onChange(selfChange: Boolean) {
        lastTimeofCall = System.currentTimeMillis();
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val isLogEnabled = sharedPref.getBoolean("switch_preference_1", true)
        if (isLogEnabled && lastTimeofCall  > lastTimeofUpdate + threshold_time){
            var call = getCall()
            if (call != null) {
                addCalendarEvent(call);
            }
            lastTimeofUpdate = System.currentTimeMillis();
        }
    }

    // com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
    fun onConnectionFailed(connectionResult: ConnectionResult) {}

    init {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
    }
}