package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.repo.UserRepository

class ChangeLanguageUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(languageCode: String) {
        repository.changeLanguage(languageCode)
    }
}