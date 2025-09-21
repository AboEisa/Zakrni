import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainRecitersResponse
import javax.inject.Inject

// domain/usecase/GetRecitersUseCase.kt
class GetRecitersUseCase @Inject constructor(
    private val repository: IRepo
) {
    suspend fun execute(rewaya: String? = null): Result<DomainRecitersResponse> {
        return repository.getReciters(rewaya)
    }
}