---
name: Pantera Fitness Core
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#3a3939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#e0c0b1'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#a78b7d'
  outline-variant: '#584236'
  surface-tint: '#ffb68e'
  primary: '#ffb68e'
  on-primary: '#542200'
  primary-container: '#ff7a1a'
  on-primary-container: '#5e2700'
  inverse-primary: '#9c4500'
  secondary: '#c8c6c5'
  on-secondary: '#313030'
  secondary-container: '#474746'
  on-secondary-container: '#b7b4b4'
  tertiary: '#8aceff'
  on-tertiary: '#00344e'
  tertiary-container: '#00aaf2'
  on-tertiary-container: '#003b57'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdbca'
  primary-fixed-dim: '#ffb68e'
  on-primary-fixed: '#331200'
  on-primary-fixed-variant: '#773300'
  secondary-fixed: '#e5e2e1'
  secondary-fixed-dim: '#c8c6c5'
  on-secondary-fixed: '#1c1b1b'
  on-secondary-fixed-variant: '#474746'
  tertiary-fixed: '#c9e6ff'
  tertiary-fixed-dim: '#8aceff'
  on-tertiary-fixed: '#001e2f'
  on-tertiary-fixed-variant: '#004c6e'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
typography:
  display-lg:
    fontFamily: Anybody
    fontSize: 48px
    fontWeight: '800'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Anybody
    fontSize: 36px
    fontWeight: '800'
    lineHeight: '1.1'
  headline-md:
    fontFamily: Anybody
    fontSize: 24px
    fontWeight: '700'
    lineHeight: '1.2'
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  label-bold:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '700'
    lineHeight: '1'
    letterSpacing: 0.05em
spacing:
  container-max: 1200px
  gutter: 1.5rem
  margin-mobile: 1rem
  margin-desktop: 2.5rem
  auth-card-width: 440px
  metric-gap: 1rem
---

## Brand & Style
The design system embodies a high-performance, aggressive, and focused athletic aesthetic. It targets dedicated athletes and fitness enthusiasts who value precision and intensity. The visual language is rooted in **Modern Minimalism** mixed with **High-Contrast / Bold** elements. 

The interface relies on deep blacks and dark grays to eliminate distractions, using a vibrant orange accent to draw the eye toward primary actions and progress metrics. The emotional response should be one of motivation, raw power, and technical excellence.

**Slogan:** "Entrená con propósito"
**Isotype:** A bold "PF" centered on a vibrant orange background square.

## Colors
The palette is engineered for a "Lights Out" environment, maximizing the pop of the orange accent.

- **Background (`#070707`)**: The foundation of the UI, providing a pure, infinite depth.
- **Surface (`#171717`)**: Used for cards, containers, and elevated sections to create subtle separation.
- **Primary Accent (`#ff7a1a`)**: Reserved for the brand mark, primary calls to action, and active state indicators.
- **Status Colors**: High-saturation tones for alerts, validation, and password strength meters to ensure readability against the dark background.

## Typography
The typography system uses a dual-font approach to balance character with utility.

- **Headlines (Anybody)**: A variable, expressive font that conveys movement and strength. Use heavy weights (700-800) for section headers and brand statements.
- **Body & Labels (Hanken Grotesk)**: A sharp, contemporary grotesque that ensures high legibility for metrics, workout instructions, and form inputs.
- **Styling Note**: Headlines should often use slight negative letter-spacing to enhance the "dense" and "powerful" feel of the brand.

## Layout & Spacing
The layout follows a strict 8px rhythmic grid. 

- **Fluid Grid**: Desktop layouts utilize a 12-column fluid grid.
- **Metric Grid**: Specialized dashboard views use a CSS Grid-based `metric-grid` with auto-fit columns (minimum width of 160px) to showcase performance data.
- **Mobile Auth**: Authentications screens on mobile are full-screen, removing all card-margins to maximize focus. Vertical spacing is increased to push primary buttons to the bottom thumb-zone.
- **Safe Areas**: Ensure all bottom-fixed elements account for device home-indicators using `env(safe-area-inset-bottom)`.

## Elevation & Depth
This design system avoids traditional soft shadows in favor of **Tonal Layering** and **High-Contrast Outlines**.

- **Level 0**: Background (`#070707`).
- **Level 1**: Surface (`#171717`). Used for the `.auth-card` and `.metric-item`.
- **Level 2**: Active Surface. A slightly lighter gray (`#252525`) or a 1px solid border using the primary orange at 20% opacity.
- **Borders**: Use 1px solid borders (`#262626`) to define structure without adding visual bulk.

## Shapes
The design system utilizes **Sharp** edges (0px) for all primary components. This choice reinforces the "Pantera" persona—aggressive, precise, and uncompromising. 

- **Exceptions**: The `.brand-mark` (PF Isotype) remains a perfect square. 
- **Interactive Elements**: Buttons, inputs, and cards must maintain 90-degree corners to distinguish the brand from the softer, rounded aesthetics of lifestyle fitness apps.

## Components
Consistent styling tokens for the core component library:

- **.brand-mark**: A square container with background-color `--primary-color`. The "PF" text inside is black (`#070707`), bold, and center-aligned.
- **.auth-card**: On desktop, a fixed-width container with `#171717` background. On mobile, it expands to `100vh` and `100vw` with internal padding of `2rem`.
- **Buttons**:
    - **Primary**: Background `--primary-color`, text `#070707`, weight 700, uppercase.
    - **Secondary**: Border 1px solid `#262626`, background transparent, text white.
- **Input Fields**: Background `#070707`, border 1px solid `#262626`, placeholder color `#555`. On focus, the border changes to `--primary-color`.
- **Metric Grid**: A container utilizing `display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 1rem;`.
- **Status Indicators**:
    - **Password Strength**: A 4-segment bar. Colors transition from `--color-red` to `--color-yellow` to `--color-green` based on entropy.
    - **Alerts**: Minimalist banners with a left-edge accent border in the respective status color.