package com.example.toneforge.ui.fragments.{feature_name}

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presenter for {FeatureName} feature following MVP pattern.
 *
 * Responsibilities:
 * - Coordinate use cases and business logic
 * - Transform domain data for view presentation
 * - Handle user actions and update view accordingly
 * - Manage coroutines for async operations
 *
 * @param {useCaseParam} Use case for {description}
 */
class {FeatureName}Presenter(
    // TODO: Inject use cases and repositories via constructor
    // Example:
    // private val yourUseCase: YourUseCase,
    // private val yourRepository: YourRepository
) : {FeatureName}Contract.Presenter {

    companion object {
        private const val TAG = "{FeatureName}Presenter"
    }

    private var view: {FeatureName}Contract.View? = null

    // Coroutine scope for async operations
    // Uses SupervisorJob so child coroutine failures don't cancel siblings
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Attach view to presenter
     * Called when fragment view is created
     */
    override fun attachView(view: {FeatureName}Contract.View) {
        this.view = view
        Log.d(TAG, "View attached")
    }

    /**
     * Detach view from presenter
     * Called when fragment view is destroyed
     */
    override fun detachView() {
        this.view = null
        Log.d(TAG, "View detached")
    }

    /**
     * Initialize presenter and load initial data
     * Called from fragment's onViewCreated
     */
    override fun initialize() {
        Log.d(TAG, "Initializing presenter")
        // TODO: Load initial data
        // loadInitialData()
    }

    /**
     * Clean up resources when presenter is destroyed
     * Cancel all running coroutines
     */
    override fun destroy() {
        Log.d(TAG, "Destroying presenter")
        presenterScope.cancel()
    }

    // TODO: Implement feature-specific methods

    /**
     * Template method for loading data with proper error handling
     * Copy and modify this pattern for your use cases
     */
    private fun loadDataExample() {
        presenterScope.launch {
            try {
                view?.showLoading()

                // Switch to IO dispatcher for repository/use case calls
                val result = withContext(Dispatchers.IO) {
                    // TODO: Call your use case or repository
                    // yourUseCase.execute()
                    // or
                    // yourRepository.getData()
                    "Example Result"
                }

                // Back on Main dispatcher to update view
                view?.hideLoading()

                // TODO: Process result and update view
                // view?.displayData(result)

                Log.d(TAG, "Data loaded successfully")

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load data: ${e.message}")
                Log.e(TAG, "Error loading data", e)
            }
        }
    }

    /**
     * Template method for saving data with proper error handling
     * Copy and modify this pattern for your use cases
     */
    private fun saveDataExample(data: String) {
        presenterScope.launch {
            try {
                view?.showLoading()

                // Switch to IO dispatcher for repository/use case calls
                withContext(Dispatchers.IO) {
                    // TODO: Call your use case or repository
                    // yourUseCase.execute(data)
                    // or
                    // yourRepository.saveData(data)
                }

                // Back on Main dispatcher to update view
                view?.hideLoading()
                view?.showSuccess("Data saved successfully")

                Log.d(TAG, "Data saved successfully")

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to save data: ${e.message}")
                Log.e(TAG, "Error saving data", e)
            }
        }
    }

    /**
     * Template method for handling user actions
     * Copy and modify this pattern for your user interactions
     */
    private fun onUserActionExample() {
        Log.d(TAG, "User action triggered")

        // TODO: Validate input if needed

        // TODO: Call appropriate use case or repository method
        // loadDataExample()
        // or
        // saveDataExample("data")
    }
}
