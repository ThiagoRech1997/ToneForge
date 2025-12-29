package com.example.toneforge.ui.fragments.{feature_name}

/**
 * Contract defining the MVP interfaces for {FeatureName} feature.
 *
 * This contract follows ToneForge's Clean Architecture and MVP pattern.
 * - View: Handles UI updates and user interactions
 * - Presenter: Coordinates business logic and updates view
 * - Model: Represents the data/state for this feature
 */
interface {FeatureName}Contract {

    /**
     * View interface - Implemented by {FeatureName}FragmentRefactored
     *
     * Responsibilities:
     * - Display data provided by presenter
     * - Forward user interactions to presenter
     * - Handle UI state (loading, error, success)
     * - NO business logic
     */
    interface View {
        /**
         * Display loading state to user
         */
        fun showLoading()

        /**
         * Hide loading state
         */
        fun hideLoading()

        /**
         * Display error message to user
         * @param message Error message to display
         */
        fun showError(message: String)

        /**
         * Display success message to user
         * @param message Success message to display
         */
        fun showSuccess(message: String)

        // TODO: Add view-specific methods here
        // Example:
        // fun displayData(data: YourDataModel)
        // fun updateUI(state: YourState)
    }

    /**
     * Presenter interface - Implemented by {FeatureName}Presenter
     *
     * Responsibilities:
     * - Coordinate use cases and business logic
     * - Transform data for view
     * - Handle user actions
     * - Manage view lifecycle
     */
    interface Presenter {
        /**
         * Attach view to presenter
         * Call this in onViewCreated()
         */
        fun attachView(view: View)

        /**
         * Detach view from presenter
         * Call this in onDestroyView()
         */
        fun detachView()

        /**
         * Initialize presenter and load initial data
         */
        fun initialize()

        /**
         * Clean up resources when presenter is destroyed
         */
        fun destroy()

        // TODO: Add presenter-specific methods here
        // Example:
        // fun onUserAction()
        // fun loadData()
        // fun saveData(data: YourDataModel)
    }

    /**
     * Model interface - Represents the data/state
     *
     * This can be a data class or interface depending on complexity.
     * For simple cases, you might not need this and can use domain models directly.
     */
    interface Model {
        // TODO: Define model structure if needed
        // Example:
        // data class State(
        //     val isLoading: Boolean = false,
        //     val data: YourDataModel? = null,
        //     val error: String? = null
        // )
    }
}
