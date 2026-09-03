package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.repo.UserRepository

class GetUserDetailsUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): User? {
        return repository.getCurrentUser()
    }
}