# MVP Scaffold Skill

## Description
Generate a complete MVP (Model-View-Presenter) scaffold for a new ToneForge fragment following the project's architectural patterns.

## When to Use
- Creating a new feature fragment
- Refactoring legacy fragments to MVP pattern
- Adding new UI screens to the app

## Instructions

1. **Gather Requirements**
   - Ask user for feature name (e.g., "Equalizer", "Presets")
   - Identify required use cases and domain models
   - Determine if new repository methods are needed

2. **Create Contract Interface**
   - Define View interface with UI update methods
   - Define Presenter interface with user action handlers
   - Use existing contracts as reference (EffectsContract, TunerContract)
   - Place in appropriate package under `ui/`

3. **Implement Presenter**
   - Create presenter class implementing the contract
   - Inject required use cases and repositories via constructor
   - Implement business logic and state management
   - Add proper error handling and logging
   - Use coroutines for async operations
   - Follow naming convention: `*Presenter`

4. **Create Fragment**
   - Extend BaseFragment
   - Implement View contract interface
   - Set up view binding
   - Initialize presenter with dependencies
   - Implement lifecycle methods (onViewCreated, onDestroyView)
   - Add UI event listeners
   - Follow naming convention: `*FragmentRefactored`

5. **Create Layout XML**
   - Design UI following Material Design principles
   - Use ConstraintLayout for complex layouts
   - Add appropriate IDs for view binding
   - Follow naming convention: `fragment_*_refactored.xml`

6. **Update Navigation**
   - Add fragment to navigation graph if needed
   - Update NavigationController if custom navigation logic required
   - Add menu items or navigation triggers

7. **Add Unit Tests**
   - Create presenter test class
   - Mock view and dependencies
   - Test all presenter methods
   - Test error scenarios
   - Aim for >80% code coverage
   - Place in `app/src/test/java/`

8. **Documentation**
   - Add KDoc comments to contract interfaces
   - Document presenter methods
   - Add inline comments for complex logic
   - Update CLAUDE.md if needed

## Template Structure

```
ui/
├── contracts/
│   └── YourFeatureContract.kt
├── presenters/
│   └── YourFeaturePresenter.kt
└── fragments/
    └── YourFeatureFragmentRefactored.kt

res/layout/
└── fragment_your_feature_refactored.xml

test/java/.../ui/presenters/
└── YourFeaturePresenterTest.kt
```

## Expected Deliverables
- Complete MVP implementation
- Unit tests with good coverage
- Layout XML with proper styling
- Navigation integration
- Documentation
