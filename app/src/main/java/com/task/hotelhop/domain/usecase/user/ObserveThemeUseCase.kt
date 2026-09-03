package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.repo.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeUseCase(private val repository: UserRepository) {
    operator fun invoke(): Flow<Boolean?> {
        return repository.isDarkMode()
    }
}