package com.healthapp.ui.meetings

import com.healthapp.Meeting

interface MeetingsDelegate {
    fun onContactRemoved(meeting: Meeting)
    fun onContactAdded()
    fun getShareableContent(): String
}