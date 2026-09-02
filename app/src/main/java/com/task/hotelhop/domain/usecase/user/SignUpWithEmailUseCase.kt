package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.repo.UserRepository

class SignUpWithEmailUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(
        fName: String,
        lName: String,
        email: String,
        pass: String,
        gender: String
    ): User {
        return repository.signUpWithEmail(fName, lName, email, pass, gender)
    }
}