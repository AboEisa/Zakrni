// 1. GetAzkarUseCase.kt - Complete implementation
package com.zakrni.app.clean.domain.usecase

import com.zakrni.app.clean.domain.IRepo
import com.zakrni.app.clean.domain.models.CombinedAzkarResponse
import com.zakrni.app.clean.domain.models.DomainAzkarResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetAzkarUseCase @Inject constructor(
    private val repo: IRepo
) {

    suspend operator fun invoke(): Result<CombinedAzkarResponse> {
            return try {
                coroutineScope {
                    val morningDeferred = async { repo.getAzkarSabah() }
                    val eveningDeferred = async { repo.getAzkarMasaa() }
                    val sleepDeferred = async { repo.getAzkarNoom() }
                    val wakeUpDeferred = async { repo.getAzkarWake() }
                    val prayerDeferred = async { repo.getAzkarPostPlayer() }
                    val mosqueDeferred = async { repo.getAzkarMosque() }
                    val eatingDeferred = async { repo.getAzkarEating() }
                    val miscDeferred = async { repo.getAzkarMisc() }

                    // Await all results
                    val morningResult = morningDeferred.await()
                    val eveningResult = eveningDeferred.await()
                    val sleepResult = sleepDeferred.await()
                    val wakeUpResult = wakeUpDeferred.await()
                    val prayerResult = prayerDeferred.await()
                    val mosqueResult = mosqueDeferred.await()
                    val eatingResult = eatingDeferred.await()
                    val miscResult = miscDeferred.await()

                    // Combine all successful results
                    val combinedResponse = CombinedAzkarResponse(
                        morning = morningResult.getOrNull(),
                        evening = eveningResult.getOrNull(),
                        sleep = sleepResult.getOrNull(),
                        wakeUp = wakeUpResult.getOrNull(),
                        prayer = prayerResult.getOrNull(),
                        mosque = mosqueResult.getOrNull(),
                        eating = eatingResult.getOrNull(),
                        misc = miscResult.getOrNull(),
                        // Track any failures
                        failures = listOfNotNull(
                            morningResult.exceptionOrNull(),
                            eveningResult.exceptionOrNull(),
                            sleepResult.exceptionOrNull(),
                            wakeUpResult.exceptionOrNull(),
                            prayerResult.exceptionOrNull(),
                            mosqueResult.exceptionOrNull(),
                            eatingResult.exceptionOrNull(),
                            miscResult.exceptionOrNull()
                        )
                    )

                    Result.success(combinedResponse)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}





