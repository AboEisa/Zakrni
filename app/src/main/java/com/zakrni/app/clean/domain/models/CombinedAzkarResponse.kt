package com.zakrni.app.clean.domain.models

data class CombinedAzkarResponse(
    val morning: DomainAzkarResponse? = null,
    val evening: DomainAzkarResponse? = null,
    val sleep: DomainAzkarResponse? = null,
    val wakeUp: DomainAzkarResponse? = null,
    val prayer: DomainAzkarResponse? = null,
    val mosque: DomainAzkarResponse? = null,
    val eating: DomainAzkarResponse? = null,
    val misc: DomainAzkarResponse? = null,
    val failures: List<Throwable> = emptyList()
) {
    fun hasAnyData(): Boolean {
        return morning != null || evening != null || sleep != null ||
                wakeUp != null || prayer != null || mosque != null ||
                eating != null || misc != null
    }

}
