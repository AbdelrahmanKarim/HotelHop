package com.task.hotelhop.domain.usecase.user

import com.task.hotelhop.domain.repo.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingUseCase(private val repository: UserRepository) {
    operator fun invoke(): Flow<Boolean> {
        return repository.hasSeenOnboarding()
    }
}
