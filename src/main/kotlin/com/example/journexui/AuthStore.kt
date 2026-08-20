package com.example.journexui

import java.util.prefs.Preferences

object AuthStore {
    private val prefs = Preferences.userRoot().node("journex")
    var token:String?
        get() = prefs.get("token", null)
        set(value) { if(value == null) prefs.remove("token") else prefs.put("token", value) }
}
