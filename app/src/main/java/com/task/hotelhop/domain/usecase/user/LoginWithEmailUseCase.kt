package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.repo.UserRepository

class LoginWithEmailUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(email: String, pass: String): User {
        return repository.loginWithEmail(email, pass)
    }
}