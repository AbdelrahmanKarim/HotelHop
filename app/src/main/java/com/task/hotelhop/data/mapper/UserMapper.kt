package com.task.hotelhop.data.mapper

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.exception.AppException

 fun mapFirebaseUserToDomain(
    firebaseUser: com.google.firebase.auth.FirebaseUser?,
    fallbackName: String = "Guest"
): User {
    if (firebaseUser == null) throw AppException.UnknownException()

    val rawName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: fallbackName
    val names = rawName.split(" ")
    val fName = names.firstOrNull() ?: "Guest"
    val lName = if (names.size > 1) names.last() else ""

    return User(
        id = firebaseUser.uid,
        firstName = fName,
        lastName = lName,
        email = firebaseUser.email ?: "",
        gender = "Not Specified"
    )
}