package com.zakrni.app.clean.domain.usecase

import com.zakrni.app.clean.domain.IRepo
import com.zakrni.app.clean.domain.models.DomainHisnDuasResponse
import com.zakrni.app.clean.domain.models.DomainHisnSection
import javax.inject.Inject

class GetDuasUseCase @Inject constructor(
    private val repo: IRepo
) {
    /**
     * Get all available dua sections
     */
    suspend fun getSections(): Result<List<DomainHisnSection>> {
        return repo.getHisnSections()
    }

    /**
     * Get duas for a specific section
     */
    suspend fun getDuas(
        section: String? = null,
        query: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): Result<DomainHisnDuasResponse> {
        return repo.getHisnDuas(section, query, page, limit)
    }

    /**
     * Get all duas (paginated)
     */
    suspend fun getAllDuas(page: Int = 1, limit: Int = 50): Result<DomainHisnDuasResponse> {
        return repo.getHisnDuas(null, null, page, limit)
    }

    /**
     * Search duas by text
     */
    suspend fun searchDuas(query: String, page: Int = 1, limit: Int = 50): Result<DomainHisnDuasResponse> {
        return repo.getHisnDuas(null, query, page, limit)
    }
}