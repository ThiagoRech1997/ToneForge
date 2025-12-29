# ToneForge Claude Skills

This directory contains specialized skills for Claude Code when working on the ToneForge project.

## Available Skills

### 🎵 Audio & DSP

- **audio-test** - Run comprehensive audio tests including native C++ tests, JNI validation, and audio processing verification
- **cpp-effect** - Add a new audio effect to the native audio engine with full Android integration
- **debug-native** - Debug and troubleshoot native C++ audio processing issues using NDK tools

### 🏗️ Architecture & Development

- **mvp-scaffold** - Generate a complete MVP scaffold for new fragments following ToneForge patterns
- **refactor-legacy** - Systematically refactor legacy fragments to modern MVP architecture

### 🔒 Security & Quality

- **security-audit** - Perform comprehensive security audit focusing on Android best practices
- **release-prep** - Complete release preparation checklist and automation

## How to Use Skills

Skills are invoked during conversations with Claude Code. You can use them in several ways:

### Direct Invocation
```
Use the audio-test skill
```

### As Part of Workflow
```
I need to add a tremolo effect to the app
```
Claude will automatically suggest using the `cpp-effect` skill.

### For Complex Tasks
```
Refactor the old SettingsFragment to MVP
```
Claude will use the `refactor-legacy` skill to guide the process.

## Skill Categories

### Development Workflow
- Creating new features (mvp-scaffold)
- Refactoring code (refactor-legacy)
- Adding effects (cpp-effect)

### Quality Assurance
- Testing audio (audio-test)
- Security review (security-audit)
- Release preparation (release-prep)

### Debugging
- Native debugging (debug-native)

## Skill Structure

Each skill follows the Anthropic standard structure:

```
.claude/skills/
└── skill-name/
    └── SKILL.md       # Main skill definition
```

## Custom Skills

You can create custom skills by following this structure:

1. Create a directory: `.claude/skills/your-skill-name/`
2. Create `SKILL.md` inside the directory
3. Include the following sections:
   - **Description** - What the skill does
   - **When to Use** - Scenarios where the skill is helpful
   - **Instructions** - Step-by-step process
   - **Expected Deliverables** - What should be produced

## Integration with Agents

Skills work alongside the specialized agents in `.claude/agents/`:

- **android-architecture-reviewer** - Reviews architectural compliance
- **android-qa-engineer** - Creates and reviews tests
- **android-ui-designer** - Creates UI components
- **audio-dsp-engineer** - Works on native audio processing
- **toneforge-advanced-features** - Implements MIDI, presets, automation
- **toneforge-utility-developer** - Works on tuner, metronome, looper

Skills provide high-level workflows, while agents provide specialized execution.

## Contributing

When adding new skills:

1. Create directory: `.claude/skills/skill-name/`
2. Create file: `SKILL.md` inside the directory
3. Use clear, descriptive names (lowercase-with-hyphens)
4. Follow the established template structure
5. Include practical examples
6. Add to this README
7. Test the skill with real scenarios

## Best Practices

- Use skills for repeatable, multi-step processes
- Keep instructions clear and actionable
- Include code examples and templates
- Reference existing project patterns
- Provide checklists for complex tasks
- Document expected outcomes
