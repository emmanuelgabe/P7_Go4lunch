package com.emmanuel.go4lunch.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Workmate(
    val uid: String,
    var email: String,
    var name: String,
    var avatarURL: String,
    @ServerTimestamp
    val creationDate: Date? = null
)