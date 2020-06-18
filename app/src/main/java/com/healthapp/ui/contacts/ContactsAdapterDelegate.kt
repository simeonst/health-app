package com.healthapp.ui.contacts

import com.healthapp.Contact

interface ContactsAdapterDelegate {
    fun onContactSelected(contact: Contact)
}