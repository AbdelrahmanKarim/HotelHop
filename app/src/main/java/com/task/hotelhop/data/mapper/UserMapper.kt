package com.task.hotelhop.data.mapper

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.exception.AppException

fun mapFirebaseUserToDomain(
    firebaseUser: com.google.firebase.auth.FirebaseUser?,
    fallbackName: String? = null
): User {
    if (firebaseUser == null) throw AppException.UnknownException()

    val emailPrefix = firebaseUser.email
        ?.substringBefore("@")
        ?.replace('.', ' ')
        ?.replace('_', ' ')
        ?.trim()
        .orEmpty()

    val rawName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
        ?: fallbackName?.takeIf { it.isNotBlank() }
        ?: emailPrefix

    val names = rawName.split(" ").filter { it.isNotBlank() }
    val fName = names.firstOrNull().orEmpty()
    val lName = if (names.size > 1) names.drop(1).joinToString(" ") else ""

    return User(
        id = firebaseUser.uid,
        firstName = fName,
        lastName = lName,
        email = firebaseUser.email ?: "",
        gender = "Not Specified"
    )
}
