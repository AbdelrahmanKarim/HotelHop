package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.repo.UserRepository

class SignInWithGoogleUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(idToken: String): User {
        return repository.signInWithGoogle(idToken)
    }
}