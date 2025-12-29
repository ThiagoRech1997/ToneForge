# Refactor Legacy Fragment Skill

## Description
Systematically refactor a legacy monolithic fragment to the modern MVP architecture following ToneForge's established patterns.

## When to Use
- Converting old fragments to MVP pattern
- Modernizing legacy code
- Improving testability and maintainability
- Breaking down large monolithic classes

## Instructions

1. **Analyze Legacy Fragment**
   - Read the entire legacy fragment
   - Identify all responsibilities and features
   - Map out UI components and interactions
   - Note dependencies and side effects
   - Document current functionality

2. **Plan Refactoring Strategy**
   - Break down into logical concerns
   - Identify domain models needed
   - List required use cases
   - Plan repository interactions
   - Create refactoring checklist

3. **Create Domain Layer (if needed)**
   - Define domain models in `domain/models/`
   - Create use cases in `domain/usecases/`
   - Define repository interfaces in `domain/interfaces/`

4. **Implement Infrastructure Layer (if needed)**
   - Create repository implementations
   - Add adapters for external systems
   - Implement managers for complex logic

5. **Create MVP Components**
   - Define Contract interface (View + Presenter)
   - Implement Presenter with unit tests
   - Create RefactoredFragment implementing View
   - Design new layout XML

6. **Migrate Functionality**
   - Move business logic to Presenter
   - Move UI updates to View implementation
   - Extract reusable components
   - Refactor large methods into smaller ones
   - Remove code duplication

7. **Handle State Management**
   - Implement proper lifecycle handling
   - Save and restore view state
   - Handle configuration changes
   - Manage loading states

8. **Update Dependencies**
   - Update MainActivity fragment initialization
   - Update NavigationController
   - Update any direct references to old fragment
   - Update navigation graph

9. **Testing**
   - Write comprehensive presenter tests
   - Add integration tests
   - Test all user interactions
   - Verify state preservation
   - Test error scenarios

10. **Validation**
    - Compare functionality with legacy version
    - Ensure feature parity
    - Test all edge cases
    - Verify performance improvements

11. **Deprecate Legacy Code**
    - Mark old fragment as @Deprecated
    - Add migration notice in comments
    - Plan for eventual removal
    - Update documentation

12. **Documentation**
    - Document architectural decisions
    - Add KDoc to public APIs
    - Update CLAUDE.md refactoring status
    - Create migration guide if needed

## Refactoring Checklist

### Separation of Concerns
- [ ] Business logic moved to Presenter
- [ ] UI logic kept in Fragment/View
- [ ] Domain models properly defined
- [ ] Dependencies injected via constructor

### Code Quality
- [ ] No god classes or methods
- [ ] Proper error handling
- [ ] Logging for debugging
- [ ] No code duplication
- [ ] Clear naming conventions

### Testing
- [ ] Presenter unit tests (>80% coverage)
- [ ] Mock all dependencies
- [ ] Test error scenarios
- [ ] Integration tests for complex flows

### Android Best Practices
- [ ] Proper lifecycle handling
- [ ] No memory leaks
- [ ] View binding used
- [ ] Coroutines for async work
- [ ] Resource management

### Performance
- [ ] No UI thread blocking
- [ ] Efficient layouts
- [ ] Proper bitmap handling
- [ ] Reduced view hierarchy depth

## Expected Deliverables
- Fully refactored MVP implementation
- Comprehensive test suite
- Side-by-side functionality comparison
- Performance metrics (if applicable)
- Migration documentation
- Updated project documentation
