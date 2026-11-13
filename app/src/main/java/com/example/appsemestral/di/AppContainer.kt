package com.example.appsemestral.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appsemestral.data.local.WimDatabase
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.data.repository.EvaluacionRepository
import com.example.appsemestral.data.repository.HistorialRepository
import com.example.appsemestral.viewmodel.CandidataFormViewModel
import com.example.appsemestral.viewmodel.CandidatasViewModel
import com.example.appsemestral.viewmodel.EvaluacionViewModel
import com.example.appsemestral.viewmodel.DetalleEvaluacionesViewModel
import com.example.appsemestral.viewmodel.HistorialViewModel
import com.example.appsemestral.viewmodel.ResultadosViewModel
import com.example.appsemestral.viewmodel.SesionViewModel

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    val database: WimDatabase = WimDatabase.obtener(appContext)
    val historialRepository = HistorialRepository(database.historialDao())
    val candidataRepository = CandidataRepository(database, database.candidataDao(), historialRepository)
    val evaluacionRepository = EvaluacionRepository(database, database.candidataDao(), database.criterioDao(), database.evaluacionDao(), historialRepository)
    val viewModelFactory: ViewModelProvider.Factory = AppViewModelProvider(this)
}

class AppViewModelProvider(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vm: ViewModel = when {
            modelClass.isAssignableFrom(SesionViewModel::class.java) -> SesionViewModel(container.historialRepository)
            modelClass.isAssignableFrom(CandidatasViewModel::class.java) -> CandidatasViewModel(container.candidataRepository)
            modelClass.isAssignableFrom(CandidataFormViewModel::class.java) -> CandidataFormViewModel(container.appContext, container.candidataRepository)
            modelClass.isAssignableFrom(EvaluacionViewModel::class.java) -> EvaluacionViewModel(container.evaluacionRepository, container.appContext)
            modelClass.isAssignableFrom(DetalleEvaluacionesViewModel::class.java) -> DetalleEvaluacionesViewModel(container.evaluacionRepository, container.database.criterioDao(), container.database.puntajeCriterioDao())
            modelClass.isAssignableFrom(HistorialViewModel::class.java) -> HistorialViewModel(container.historialRepository)
            modelClass.isAssignableFrom(ResultadosViewModel::class.java) -> ResultadosViewModel(container.candidataRepository, container.evaluacionRepository)
            else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
        }
        return vm as T
    }
}
