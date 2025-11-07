# New Feature Implementation Checklist

Use this checklist when implementing new features to ensure completeness and quality.

## 📋 Planning Phase

- [ ] Feature requirements clearly defined
- [ ] User stories/use cases documented
- [ ] UI/UX mockups reviewed (if applicable)
- [ ] Technical approach discussed and approved
- [ ] Impact on existing features assessed
- [ ] Dependencies identified

## 🏗️ Architecture Design

### Domain Layer
- [ ] Use case(s) created with single responsibility
- [ ] Domain models defined (data classes)
- [ ] Repository interface(s) defined
- [ ] Business logic properly encapsulated
- [ ] No Android framework dependencies

### Infrastructure Layer
- [ ] Repository implementation(s) created
- [ ] Adapters created if integrating external systems
- [ ] Data sources implemented (local/remote)
- [ ] Error handling implemented
- [ ] Logging added at appropriate levels

### UI Layer
- [ ] Contract interface defined (View, Presenter, Model)
- [ ] Presenter implements business logic coordination
- [ ] Fragment/Activity handles UI only
- [ ] Proper lifecycle management
- [ ] Navigation integration

## 💻 Implementation

### Code Quality
- [ ] Follows existing code conventions
- [ ] Proper error handling throughout
- [ ] Logging at appropriate levels
- [ ] No hardcoded strings (use resources)
- [ ] Constants properly defined
- [ ] No code duplication

### Performance
- [ ] No blocking operations on UI thread
- [ ] Efficient algorithms used
- [ ] Resources properly managed
- [ ] Memory leaks prevented
- [ ] Database queries optimized (if applicable)

### Security
- [ ] Input validation implemented
- [ ] Permissions handled correctly
- [ ] No sensitive data in logs
- [ ] Secure storage for sensitive data
- [ ] File operations are secure

## 🎨 UI/UX (if applicable)

- [ ] Follows Material Design guidelines
- [ ] Consistent with existing app design
- [ ] Responsive on different screen sizes
- [ ] Accessibility implemented (content descriptions)
- [ ] Loading states handled
- [ ] Error states provide clear feedback
- [ ] Empty states handled gracefully

## 🧪 Testing

### Unit Tests
- [ ] Presenter tests written
- [ ] Use case tests written
- [ ] Repository tests written (with mocks)
- [ ] Edge cases covered
- [ ] Error scenarios tested
- [ ] Target coverage achieved (85%+ for critical code)

### Integration Tests
- [ ] Cross-layer integration tested if applicable
- [ ] Database operations tested
- [ ] File operations tested

### UI Tests
- [ ] Critical user flows tested with Espresso
- [ ] Navigation tested
- [ ] User interactions tested

### Manual Testing
- [ ] Feature tested on device
- [ ] Different screen sizes tested
- [ ] Rotation/configuration changes tested
- [ ] Background/foreground transitions tested
- [ ] Low memory scenarios tested

## 📚 Documentation

- [ ] Code comments for complex logic
- [ ] Public APIs documented (KDoc)
- [ ] CLAUDE.md updated if needed
- [ ] README.md updated if user-facing
- [ ] Migration guide if breaking changes

## 🔍 Pre-Merge Validation

### Build & Tests
- [ ] `./gradlew clean build` succeeds
- [ ] `./gradlew test` passes
- [ ] `./gradlew lint` shows no critical issues
- [ ] `./scripts/functional-validation.sh` passes
- [ ] Test coverage report reviewed

### Code Review
- [ ] Self-review completed
- [ ] PR description clearly explains changes
- [ ] Screenshots/videos included for UI changes
- [ ] Breaking changes highlighted
- [ ] Reviewers assigned

## 🚀 Post-Merge

- [ ] Feature flag removed (if applicable)
- [ ] Monitoring in place for critical features
- [ ] User documentation updated
- [ ] Release notes prepared
- [ ] Stakeholders notified

## 📝 Notes

Use this space to track feature-specific considerations or deviations from standard process.
