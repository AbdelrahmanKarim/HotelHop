package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.repo.UserRepository

class ToggleThemeUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(isDark: Boolean) {
        repository.toggleTheme(isDark)
    }
}