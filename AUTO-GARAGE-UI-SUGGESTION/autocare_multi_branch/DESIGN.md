---
name: AutoCare Multi-Branch
colors:
  surface: '#f8f9ff'
  surface-dim: '#ccdbf4'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e6eeff'
  surface-container-high: '#dde9ff'
  surface-container-highest: '#d5e3fd'
  on-surface: '#0d1c2f'
  on-surface-variant: '#444651'
  inverse-surface: '#233144'
  inverse-on-surface: '#ebf1ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#9d4300'
  on-secondary: '#ffffff'
  secondary-container: '#fd761a'
  on-secondary-container: '#5c2400'
  tertiary: '#002e44'
  on-tertiary: '#ffffff'
  tertiary-container: '#004565'
  on-tertiary-container: '#36b6fb'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#ffdbca'
  secondary-fixed-dim: '#ffb690'
  on-secondary-fixed: '#341100'
  on-secondary-fixed-variant: '#783200'
  tertiary-fixed: '#c9e6ff'
  tertiary-fixed-dim: '#89ceff'
  on-tertiary-fixed: '#001e2f'
  on-tertiary-fixed-variant: '#004c6e'
  background: '#f8f9ff'
  on-background: '#0d1c2f'
  surface-variant: '#d5e3fd'
  success-green: '#16A34A'
  danger-red: '#DC2626'
  warning-amber: '#F59E0B'
  industrial-slate: '#334155'
  surface-gray: '#F8FAFC'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.3'
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: '1.3'
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.4'
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1.2'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
  container-max: 1440px
---

## Brand & Style

The design system is engineered to project a high degree of **professionalism, technical expertise, and reliability**. It serves a dual purpose: providing an efficient, data-dense tool for garage operations while maintaining a welcoming and transparent interface for customers.

The visual style is **Corporate / Modern** with subtle **Industrial** influences. It prioritizes clarity and efficiency through:
- **High-Density Utility:** For administrative and technical roles, the layout maximizes information density without sacrificing legibility.
- **Trust-Based Aesthetics:** A clean, SaaS-inspired look for web interfaces that feels established and secure.
- **Tactile Technicality:** For the technician mobile app, the design shifts toward high-contrast, touch-optimized elements suitable for fast-paced garage environments.
- **Approachable Hospitality:** For the customer app, the design uses softer transitions and more generous white space to reduce the anxiety often associated with car repairs.

## Colors

The palette is anchored by **Deep Blue**, signifying institutional trust and mechanical precision. **Action Orange** is used sparingly but decisively for primary call-to-actions and urgent status indicators.

- **Primary (Deep Blue):** Used for navigation, headers, and primary branding elements.
- **Secondary (Orange):** Reserved for high-priority actions (e.g., "Create Quote," "Book Now") and critical alerts.
- **Support Colors:** **Teal/Blue** is used in the Front Desk app to create a friendlier, service-oriented atmosphere. **Slate Blue** acts as the foundation for the Technician’s industrial theme.
- **Functional Colors:** Standardized Green (Success), Red (Errors/Urgent), and Amber (Warnings) must be used consistently across all five platforms to denote repair statuses.
- **Neutral Strategy:** Backgrounds should primarily be White (`#FFFFFF`) or light Gray (`#F8FAFC`). For the Technician App, a dark-mode variation uses `industrial-slate` as the primary surface color to minimize the visibility of smudges and glare in shop settings.

## Typography

This design system utilizes **Inter** for its exceptional legibility in data-heavy environments and its neutral, modern tone.

- **Headers:** Use Bold or Semi-Bold weights to create a clear hierarchy. For the Admin and Manager dashboards, tight line heights are preferred to keep information compact.
- **Body Text:** Standardize on 16px for desktop and 14px for dense tables. In the Mobile Technician app, increase contrast and use 16px minimum for body text to ensure readability in variable lighting.
- **Labels:** Uppercase labels with slight letter spacing are used for table headers and section titles to differentiate them from actionable content.
- **Numeric Data:** In financial reports and quotes, use tabular figures (monospaced numbers) to ensure columns of prices and quantities align perfectly.

## Layout & Spacing

The layout system is built on a **4px baseline grid** to ensure mathematical consistency across all components.

### Web Dashboards (Admin, Manager, Front Desk)
- **Grid:** 12-column fluid grid.
- **Navigation:** Fixed 240px left sidebar for Admin/Manager; Top-nav for Front Desk to allow for full-width chat and table views.
- **Density:** High. Content areas use 24px padding, while table cells use 12px vertical padding.

### Mobile Apps (Technician, Customer)
- **Margins:** 16px side margins.
- **Touch Targets:** Minimum 48x48px for all interactive elements.
- **Reflow:** Single column vertical stack. Use "Bottom Sheets" for complex forms (like adding parts) to maintain context without navigating away from the task.

## Elevation & Depth

Visual hierarchy is established through a combination of **Tonal Layering** and **Ambient Shadows**.

- **Surfaces:** Use a light gray background (`#F8FAFC`) with white cards (`#FFFFFF`) to create immediate separation.
- **Shadows:** Soft, diffused shadows are used for cards and modals.
  - *Low Elevation:* 0px 2px 4px rgba(0, 0, 0, 0.05) (Standard cards).
  - *High Elevation:* 0px 10px 20px rgba(0, 0, 0, 0.1) (Modals, dropdowns).
- **Technician App:** Elevation is conveyed through "Lume" borders—thin, 1px borders that are slightly lighter than the background color—instead of heavy shadows, providing clarity in high-glare environments.
- **Glassmorphism:** Reserved exclusively for the Customer Mobile App's top navigation or "floating" AI chatbot button to provide a modern, premium feel.

## Shapes

The shape language balances industrial precision with modern software friendliness.

- **Standard Radius:** 8px (Rounded) for buttons, input fields, and small cards.
- **Large Radius:** 16px (Rounded-lg) for main dashboard containers and mobile cards.
- **Status Badges:** Use a pill-shape (full rounding) to clearly distinguish them from buttons.
- **Consistent Enclosure:** Every data group (e.g., a customer's vehicle info or a line item in an invoice) must be contained within a rounded card to maintain the "organized" feel requested.

## Components

### Buttons
- **Primary:** Solid Deep Blue or Orange with white text. 8px border radius.
- **Secondary:** Outlined with 1.5px stroke in Primary color.
- **Ghost:** No background/border, used for "Cancel" or less frequent actions.

### Input Fields
- **Desktop:** 40px height, 1px border (`#E2E8F0`). Label sits above the field.
- **Mobile:** 52px height for better thumb-target, floating labels to save vertical space.

### Tables (Critical for Admin/Manager)
- **Header:** Sticky header with light gray background.
- **Rows:** Alternating subtle zebra striping or 1px bottom border. 
- **Actions:** Grouped at the far right of the row, using "More" vertical ellipsis menus to save horizontal space.

### Cards
- **Web:** White background, 1px border or soft shadow, 16px internal padding.
- **Mobile:** Large surface area, prominent image/icon on the left, primary info in bold.

### Status Badges
- **Status Map:**
  - *Pending:* Yellow Background, Dark Amber Text.
  - *In Progress:* Blue Background, Deep Blue Text.
  - *Completed:* Green Background, Dark Green Text.
  - *Cancelled:* Red Background, Dark Red Text.

### Specialized Components
- **Vehicle Checklist:** Interactive SVG or image of a car chassis where technicians can tap specific "hotspots" to mark dents or scratches.
- **Timeline Stepper:** Vertical line with dots used in the Customer App to show real-time repair progress.