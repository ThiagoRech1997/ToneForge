package com.example.toneforge.ui.fragments.{feature_name}

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.toneforge.databinding.Fragment{FeatureName}RefactoredBinding
import com.example.toneforge.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar

/**
 * {FeatureName} Fragment following MVP pattern and Clean Architecture.
 *
 * This fragment implements the View interface from {FeatureName}Contract.
 *
 * Responsibilities:
 * - Display data provided by presenter
 * - Forward user interactions to presenter
 * - Handle UI state changes
 * - NO business logic (all logic in presenter)
 */
class {FeatureName}FragmentRefactored : BaseFragment(), {FeatureName}Contract.View {

    companion object {
        private const val TAG = "{FeatureName}FragmentRefactored"

        /**
         * Factory method to create new instance
         * Use this instead of constructor for fragment creation
         */
        fun newInstance(): {FeatureName}FragmentRefactored {
            return {FeatureName}FragmentRefactored()
        }
    }

    // View binding for type-safe view access
    private var _binding: Fragment{FeatureName}RefactoredBinding? = null
    private val binding get() = _binding!!

    // Presenter instance
    // TODO: Inject dependencies via factory or dependency injection framework
    private lateinit var presenter: {FeatureName}Contract.Presenter

    /**
     * Create and inflate the fragment view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Fragment{FeatureName}RefactoredBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Initialize presenter and setup UI
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize presenter with dependencies
        // For now, creating directly. Consider using factory or DI.
        presenter = {FeatureName}Presenter(
            // TODO: Inject use cases and repositories
            // yourUseCase = YourUseCase(),
            // yourRepository = YourRepository()
        )

        // Attach view to presenter
        presenter.attachView(this)

        // Setup UI components
        setupUI()
        setupListeners()

        // Initialize presenter (loads initial data)
        presenter.initialize()
    }

    /**
     * Setup UI components
     */
    private fun setupUI() {
        // TODO: Initialize UI components
        // Example:
        // binding.recyclerView.apply {
        //     layoutManager = LinearLayoutManager(requireContext())
        //     adapter = yourAdapter
        // }
    }

    /**
     * Setup click listeners and user interaction handlers
     */
    private fun setupListeners() {
        // TODO: Setup listeners for user interactions
        // Example:
        // binding.button.setOnClickListener {
        //     presenter.onButtonClicked()
        // }

        // binding.editText.addTextChangedListener { text ->
        //     presenter.onTextChanged(text.toString())
        // }
    }

    /**
     * Clean up when view is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()

        // Detach view from presenter
        presenter.detachView()

        // Clean up binding to prevent memory leaks
        _binding = null
    }

    /**
     * Clean up presenter when fragment is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()

        // Only destroy presenter if activity is finishing or fragment is being removed
        if (requireActivity().isFinishing || isRemoving) {
            presenter.destroy()
        }
    }

    // ===========================================
    // View Interface Implementation
    // ===========================================

    /**
     * Show loading indicator to user
     */
    override fun showLoading() {
        // TODO: Show loading indicator
        // Example:
        // binding.progressBar.visibility = View.VISIBLE
        // binding.contentLayout.visibility = View.GONE
    }

    /**
     * Hide loading indicator
     */
    override fun hideLoading() {
        // TODO: Hide loading indicator
        // Example:
        // binding.progressBar.visibility = View.GONE
        // binding.contentLayout.visibility = View.VISIBLE
    }

    /**
     * Show error message to user
     */
    override fun showError(message: String) {
        // Use Snackbar for non-critical errors (user can dismiss)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

        // Or use Toast for simpler messages
        // Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show success message to user
     */
    override fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    // TODO: Implement view-specific methods from contract
    // Example:
    // override fun displayData(data: YourDataModel) {
    //     binding.textView.text = data.title
    //     binding.imageView.setImageResource(data.imageResId)
    // }

    // override fun updateUI(state: YourState) {
    //     when (state) {
    //         is YourState.Loading -> showLoading()
    //         is YourState.Success -> {
    //             hideLoading()
    //             displayData(state.data)
    //         }
    //         is YourState.Error -> {
    //             hideLoading()
    //             showError(state.message)
    //         }
    //     }
    // }
}
