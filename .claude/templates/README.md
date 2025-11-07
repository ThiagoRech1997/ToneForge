# ToneForge MVP Templates

This directory contains code templates for implementing MVP (Model-View-Presenter) pattern in ToneForge following Clean Architecture principles.

## Available Templates

### 1. `mvp-contract-template.kt`
Defines the Contract interface with View, Presenter, and Model interfaces.

**Usage:**
1. Copy template to your feature package
2. Replace `{FeatureName}` with your feature name (e.g., `Equalizer`)
3. Replace `{feature_name}` with lowercase version (e.g., `equalizer`)
4. Define view methods (what UI can display)
5. Define presenter methods (user actions and lifecycle)
6. Define model structure if needed

### 2. `presenter-template.kt`
Implements the Presenter with coroutines support and proper lifecycle management.

**Usage:**
1. Copy template to your feature package
2. Replace placeholders with your feature name
3. Inject required use cases and repositories via constructor
4. Implement feature-specific methods
5. Use provided async patterns for coroutines

**Key Features:**
- Coroutine scope management
- Proper view lifecycle handling
- Error handling patterns
- IO dispatcher for background work
- Main dispatcher for UI updates

### 3. `fragment-refactored-template.kt`
Implements the View interface in a Fragment with view binding.

**Usage:**
1. Copy template to your feature package
2. Replace placeholders with your feature name
3. Implement view interface methods
4. Setup UI components and listeners
5. Forward user actions to presenter

**Key Features:**
- View binding for type-safe views
- Proper lifecycle management
- Presenter attachment/detachment
- Loading/error/success states

### 4. `test-presenter-template.kt`
Comprehensive unit test template for Presenter testing.

**Usage:**
1. Copy to test source set
2. Replace placeholders with your feature name
3. Mock dependencies (use cases, repositories)
4. Write tests following Given-When-Then pattern
5. Cover happy path, error cases, and edge cases

**Test Categories:**
- Lifecycle tests
- Initialization tests
- Feature-specific tests
- Error handling tests
- Edge cases

### 5. `layout-fragment-template.xml`
Layout template with Material Design components and common patterns.

**Usage:**
1. Copy to res/layout/
2. Rename to `fragment_{feature_name}_refactored.xml`
3. Add your UI components
4. Follow Material Design guidelines
5. Ensure accessibility

**Included:**
- Loading indicator
- Main content layout
- Empty state view
- Proper constraints and spacing

## Quick Start Example

Let's create a "Profile" feature:

### 1. Create Contract (ProfileContract.kt)

```kotlin
package com.example.toneforge.ui.fragments.profile

interface ProfileContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun displayProfile(name: String, email: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun initialize()
        fun destroy()
        fun loadProfile()
        fun updateProfile(name: String, email: String)
    }
}
```

### 2. Create Presenter (ProfilePresenter.kt)

```kotlin
class ProfilePresenter(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ProfileContract.Presenter {

    private var view: ProfileContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadProfile() {
        presenterScope.launch {
            try {
                view?.showLoading()
                val profile = withContext(Dispatchers.IO) {
                    getUserProfileUseCase.execute()
                }
                view?.hideLoading()
                view?.displayProfile(profile.name, profile.email)
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load profile")
            }
        }
    }

    // ... other methods
}
```

### 3. Create Fragment (ProfileFragmentRefactored.kt)

```kotlin
class ProfileFragmentRefactored : BaseFragment(), ProfileContract.View {

    private var _binding: FragmentProfileRefactoredBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ProfileContract.Presenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = ProfilePresenter(
            GetUserProfileUseCase(),
            UpdateUserProfileUseCase()
        )
        presenter.attachView(this)

        setupListeners()
        presenter.initialize()
    }

    override fun displayProfile(name: String, email: String) {
        binding.nameTextView.text = name
        binding.emailTextView.text = email
    }

    // ... other methods
}
```

### 4. Write Tests (ProfilePresenterTest.kt)

```kotlin
@Test
fun `test loadProfile displays profile data`() = runTest {
    // Given
    val mockProfile = UserProfile("John", "john@example.com")
    `when`(mockGetUserProfileUseCase.execute()).thenReturn(mockProfile)

    // When
    presenter.loadProfile()
    advanceUntilIdle()

    // Then
    verify(mockView).showLoading()
    verify(mockView).hideLoading()
    verify(mockView).displayProfile("John", "john@example.com")
}
```

## Best Practices

### Contract Design
- Keep view methods simple and UI-focused
- Presenter methods should represent user actions or lifecycle events
- Avoid exposing implementation details in interfaces

### Presenter Implementation
- Use constructor injection for dependencies
- Always use coroutines for async operations
- Switch to Dispatchers.IO for repository/use case calls
- Handle errors gracefully and update view
- Detach view properly to prevent leaks

### Fragment Implementation
- Use view binding (never findViewById)
- Keep fragment code minimal (just UI updates)
- Forward all user actions to presenter
- Properly manage presenter lifecycle
- Handle configuration changes

### Testing
- Mock all dependencies
- Test both success and failure cases
- Test edge cases and null values
- Use Given-When-Then structure
- Verify view interactions
- Test with test coroutine dispatcher

## Common Patterns

### Loading Data on Init
```kotlin
override fun initialize() {
    loadData()
}
```

### Handling User Actions
```kotlin
override fun onSaveClicked(data: String) {
    if (validateData(data)) {
        saveData(data)
    } else {
        view?.showError("Invalid data")
    }
}
```

### Repository Calls
```kotlin
private fun loadData() {
    presenterScope.launch {
        try {
            view?.showLoading()
            val data = withContext(Dispatchers.IO) {
                repository.getData()
            }
            view?.hideLoading()
            view?.displayData(data)
        } catch (e: Exception) {
            view?.hideLoading()
            view?.showError(e.message ?: "Unknown error")
        }
    }
}
```

## Troubleshooting

### Presenter not updating view
- Ensure view is attached before calling methods
- Check coroutine scope hasn't been cancelled
- Verify you're on Main dispatcher when updating view

### Memory leaks
- Detach view in onDestroyView()
- Cancel coroutine scope in destroy()
- Clear binding in onDestroyView()

### Tests failing
- Use StandardTestDispatcher
- Call advanceUntilIdle() after async operations
- Mock all dependencies
- Set Dispatchers.Main in @Before

## Resources

- [ToneForge CLAUDE.md](../../CLAUDE.md) - Project overview
- [Android MVP Guide](https://github.com/android10/Android-CleanArchitecture)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [View Binding](https://developer.android.com/topic/libraries/view-binding)
