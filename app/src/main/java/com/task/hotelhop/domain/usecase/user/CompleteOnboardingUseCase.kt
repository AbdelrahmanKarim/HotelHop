package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.repo.UserRepository

class CompleteOnboardingUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() {
        repository.completeOnboarding()
    }
}