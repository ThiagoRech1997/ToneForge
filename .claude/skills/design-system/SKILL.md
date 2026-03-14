# Design System Skill

## Description
Apply the ToneForge HILAVA Dark Theme design system when creating or modifying UI components, layouts, and screens. Ensures visual consistency across the entire app.

## When to Use
- When creating new fragments, layouts, or XML resources
- When designing screens in Paper MCP
- When reviewing UI code for design consistency
- When adding new features that need UI components
- When the user asks about colors, fonts, spacing, or component styles

## Instructions

Follow the HILAVA Dark Theme specifications below for all UI work.

### Color Palette

#### Base Colors
| Token | Hex | Usage |
|-------|-----|-------|
| Background | `#111827` | App background, artboard fill |
| Surface | `#1E2433` | Cards, containers, input fields |
| Elevated | `#2A2F3E` | Elevated surfaces, active buttons |
| Muted | `#374151` | Disabled backgrounds, tracks, toggle OFF |
| Nav Bar | `#1F2937` | Bottom navigation background |
| Divider | `rgba(255,255,255,0.1)` | Section dividers (1dp) |

#### Text Colors
| Token | Hex | Usage |
|-------|-----|-------|
| Primary | `#FFFFFF` | Headings, primary text |
| Secondary | `#9CA3AF` | Subtitles, section labels |
| Tertiary | `#6B7280` | Muted text, captions, inactive tabs |

#### Feature Accent Colors
Each feature has a unique accent color used for icons, toggles, highlights, and active states:

| Feature | Hex | Color |
|---------|-----|-------|
| Tuner | `#3B82F6` | Blue |
| Effects | `#F59E0B` | Amber |
| Looper | `#10B981` | Green |
| Metronome | `#8B5CF6` | Purple |
| Learning | `#F59E0B` | Yellow/Amber |
| Recorder | `#EC4899` | Pink |
| Settings | `#8E8E93` | Gray |
| MIDI | `#06B6D4` | Cyan |
| Volume | `#10B981` | Green |

#### Semantic Colors
| Token | Hex | Usage |
|-------|-----|-------|
| Active/Selected | `#8B5CF6` | Active tab, selection state |
| Success | `#10B981` | Positive confirmation |
| Warning | `#F59E0B` | Alert, caution |
| Error | `#EF4444` | Error, danger, destructive |

### Typography

#### Font Families
- **Space Grotesk Bold (700)** — Display headings, stat values, app title
- **Inter** — All body/UI text (Regular 400, Medium 500, SemiBold 600)

#### Type Scale
| Element | Size | Weight | Family | Extras |
|---------|------|--------|--------|--------|
| App Title | 36px | Bold 700 | Space Grotesk | — |
| Screen Title | 20px | Bold 700 | Space Grotesk | — |
| Stat Value | 20px | Bold 700 | Space Grotesk | — |
| Card Title | 16px | SemiBold 600 | Inter | — |
| Body Text | 14px | Regular 400 | Inter | line-height 20px |
| Caption | 12px | Regular 400 | Inter | — |
| Section Label | 11px | SemiBold 600 | Inter | uppercase, letter-spacing 0.08em, color #9CA3AF |
| Badge Text | 11px | SemiBold 600 | Inter | — |
| Tab Label | 10px | Medium 500 | Inter | — |
| Stat Label | 10px | Regular 400 | Inter | — |

#### Android XML Mapping
- Space Grotesk → `@font/space_grotesk_bold`
- Inter Regular → `@font/inter_regular` or `sans-serif`
- Inter Medium → `@font/inter_medium` or `sans-serif-medium`
- Inter SemiBold → `@font/inter_semibold`

### Spacing

| Value | Usage |
|-------|-------|
| `4dp` | Micro gap |
| `8dp` | Element gap, inline icon+text |
| `12dp` | Group gap (items within a section) |
| `16dp` | Card padding, section gap between cards |
| `24dp` | Page horizontal padding |
| `40dp` | Large section gap |

### Border Radius

| Value | Usage |
|-------|-------|
| `4dp` | Progress bars, badges |
| `12dp` | Buttons, tags |
| `13dp` | Toggles (pill shape) |
| `16dp` | Cards |
| `20dp` | Feature grid items |
| `24dp` | CTA buttons, pills |

### Component Specifications

#### Card
```xml
background: @color/hilava_surface (#1E2433)
cornerRadius: 16dp
padding: 16dp
elevation: 0dp (no shadow)
border: none
```

#### Toggle / Switch
```
Size: 44 × 26dp
Thumb: 22 × 22dp

ON state:
  track: feature accent color
  thumb: #FFFFFF

OFF state:
  track: #374151
  thumb: #9CA3AF
```

#### Bottom Navigation
```
height: 56dp (+ safe area inset)
background: #1F2937
tabs: Home, Effects, Pedals, Tuner

Active tab:
  icon + label: #8B5CF6

Inactive tab:
  icon + label: #6B7280

Icons: 22dp, stroke-based, 1.5dp stroke
Labels: 10sp Inter Medium
```

#### Status Bar
```
height: 38dp
time: 14sp Inter SemiBold, #FFFFFF
icons: #FFFFFF
```

#### Progress Bar
```
height: 6dp
track: #374151
fill: feature accent color
borderRadius: 4dp
```

#### Section Divider
```
height: 1dp
color: rgba(255, 255, 255, 0.1)
```

#### List Item Row
```
height: ~56dp
layout: icon (accent) | title + subtitle | accessory (time, chevron)
divider between items (1dp, 10% white)
```

### Icon Style
- Stroke-based, 1.5dp stroke width
- Navigation: 22 × 22dp
- Cards/lists: 20 × 20dp
- Color: contextual (accent for active, #6B7280 for inactive)

### Android Resource Colors
When creating XML layouts, use color resources defined in `res/values/colors.xml`:
```xml
@color/hilava_background     → #111827
@color/hilava_surface         → #1E2433
@color/hilava_surface_elevated → #2A2F3E
@color/hilava_text_primary    → #FFFFFF
@color/hilava_text_secondary  → #9CA3AF
@color/hilava_text_tertiary   → #6B7280
@color/hilava_nav_background  → #1F2937
@color/hilava_divider         → #1AFFFFFF
```

## Expected Deliverables
- UI code that matches the design system specifications
- Consistent use of the color palette, typography, and spacing
- Components that follow the documented patterns
- No hardcoded values — use resource references when available
