# ToneForge Design System — HILAVA Dark Theme

Guia de cores, tipografia, espaçamentos e componentes para manter consistência visual no app.

---

## Cores

### Base

| Token | Hex | Uso |
|-------|-----|-----|
| Background | `#111827` | Fundo principal do app |
| Surface | `#1E2433` | Cards, containers |
| Elevated | `#2A2F3E` | Superfícies elevadas, botões |
| Muted | `#374151` | Tracks, backgrounds desabilitados |
| Nav Bar | `#1F2937` | Bottom navigation |
| Divider | `rgba(255,255,255,0.1)` | Divisores entre seções |

### Texto

| Token | Hex | Uso |
|-------|-----|-----|
| Primary | `#FFFFFF` | Títulos, texto principal |
| Secondary | `#9CA3AF` | Subtítulos, labels de seção |
| Tertiary | `#6B7280` | Texto muted, captions, tabs inativas |

### Acentos por Feature

| Feature | Hex | Cor |
|---------|-----|-----|
| Tuner | `#3B82F6` | Azul |
| Effects | `#F59E0B` | Âmbar |
| Looper | `#10B981` | Verde |
| Metronome | `#8B5CF6` | Roxo |
| Learning | `#F59E0B` | Amarelo/Âmbar |
| Recorder | `#EC4899` | Rosa |
| Settings | `#8E8E93` | Cinza |
| MIDI | `#06B6D4` | Ciano |
| Volume | `#10B981` | Verde |

### Semânticas

| Token | Hex | Uso |
|-------|-----|-----|
| Active/Selected | `#8B5CF6` | Tab ativa, seleção |
| Success | `#10B981` | Confirmação, positivo |
| Warning | `#F59E0B` | Alerta |
| Error | `#EF4444` | Erro, perigo |

---

## Tipografia

### Fontes

| Família | Uso |
|---------|-----|
| **Space Grotesk** (Bold 700) | Títulos display, valores de destaque |
| **Inter** (Regular 400, Medium 500, SemiBold 600) | Body, labels, UI geral |

### Escala de Tipos

| Elemento | Tamanho | Peso | Família | Extras |
|----------|---------|------|---------|--------|
| App Title | 36px | Bold 700 | Space Grotesk | — |
| Screen Title | 20px | Bold 700 | Space Grotesk | — |
| Stat Value | 20px | Bold 700 | Space Grotesk | — |
| Card Title | 16px | SemiBold 600 | Inter | — |
| Body Text | 14px | Regular 400 | Inter | line-height 20px |
| Caption | 12px | Regular 400 | Inter | — |
| Section Label | 11px | SemiBold 600 | Inter | uppercase, letter-spacing 0.08em, cor #9CA3AF |
| Badge Text | 11px | SemiBold 600 | Inter | — |
| Tab Label | 10px | Medium 500 | Inter | — |
| Stat Label | 10px | Regular 400 | Inter | — |

---

## Espaçamento

### Ritmo

| Valor | Uso |
|-------|-----|
| `4px` | Micro gap |
| `8px` | Element gap, inline (ícone + texto) |
| `12px` | Group gap (itens dentro de uma seção) |
| `16px` | Card padding, section gap (entre cards) |
| `24px` | Page horizontal padding |
| `40px` | Large section gap |

---

## Border Radius

| Valor | Uso |
|-------|-----|
| `4px` | Progress bars, badges |
| `12px` | Buttons, tags |
| `13px` | Toggles (pill shape) |
| `16px` | Cards |
| `20px` | Feature grid items |
| `24px` | CTA buttons, pills |

---

## Componentes

### Card

```
background: #1E2433
border-radius: 16px
padding: 16px
border: none
shadow: none
```

### Toggle / Switch

```
Dimensão: 44 × 26px
Thumb: 22 × 22px

Estado ON:
  background: cor de acento da feature
  thumb: #FFFFFF

Estado OFF:
  background: #374151
  thumb: #9CA3AF
```

### Bottom Navigation

```
height: 56px (+ safe area)
background: #1F2937
tabs: Home, Effects, Pedals, Tuner

Ativa:
  ícone + label: #8B5CF6

Inativa:
  ícone + label: #6B7280

Ícones: 22px, stroke 1.5px
Labels: 10px Inter Medium
```

### Status Bar

```
height: 38px
time: 14px Inter SemiBold, #FFFFFF
icons: #FFFFFF (signal, wifi, battery)
```

### Section Divider

```
height: 1px
color: rgba(255, 255, 255, 0.1)
```

### Progress Bar

```
height: 6px
track: #374151
fill: cor de acento
border-radius: 4px
```

### List Item

```
height: ~56px
layout: ícone (accent) | título + subtítulo | acessório (tempo, chevron)
divider entre itens
```

### Buttons

```
background: surface color com ripple
border-radius: 12px
text: 14px Inter Medium
```

---

## Ícones

| Propriedade | Valor |
|-------------|-------|
| Estilo | Stroke-based |
| Stroke width | 1.5px |
| Tamanho navegação | 22 × 22px |
| Tamanho cards/listas | 20 × 20px |
| Cor | Contextual (acento ou muted) |
