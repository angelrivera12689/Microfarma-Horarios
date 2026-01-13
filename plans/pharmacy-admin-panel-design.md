# Pharmacy Administrative Panel Design Specification

## Overview
This design transforms the existing administrative panel for Microfarma into a modern, elegant interface that conveys health, confidence, cleanliness, and order. The aesthetic prioritizes usability for daily operations while maintaining a professional and trustworthy appearance.

## Color Palette
- **Primary Green**: `#10B981` (Emerald-500) - Represents health and vitality
- **Secondary Blue**: `#3B82F6` (Blue-500) - Conveys trust and professionalism
- **Accent Teal**: `#14B8A6` (Teal-500) - For highlights and active states
- **Neutral Whites**: `#FFFFFF`, `#F8FAFC` - Clean backgrounds
- **Gray Scale**: `#64748B` (Slate-500), `#94A3B8` (Slate-400), `#CBD5E1` (Slate-300)
- **Background**: `#F1F5F9` (Slate-100) - Soft, clean base

## Typography
- **Primary Font**: Inter (or system sans-serif)
- **Weights**: Regular (400), Medium (500), Bold (600), Extra Bold (700)
- **Sizes**:
  - Headlines: 24px-36px
  - Body: 14px-16px
  - Small text: 12px-13px
- **Line Heights**: 1.4-1.6 for readability

## Layout Structure

### Overall Layout
- **Sidebar Width**: 280px (increased for better readability)
- **Main Content**: Max-width 1200px, centered
- **Header Height**: 64px
- **Spacing**: 24px base unit, 16px for tight spaces

### Sidebar Design
- **Background**: White with subtle shadow
- **Logo Section**:
  - Pharmacy cross icon in teal gradient
  - "Microfarma" in bold, "Panel Administrativo" in smaller text
- **Navigation**:
  - Section headers with health-related icons:
    - 🔒 Security (lock)
    - 👥 Human Resources (people with heart)
    - 🏥 Organization (building with cross)
    - 📅 Schedules (calendar with clock)
    - 📰 News (newspaper)
    - 📧 Notifications (envelope with bell)
  - Hover states: Soft green background
  - Active states: Teal accent
- **Colors**: Text in slate-700, hover in slate-900

### Header Design
- **Background**: White with subtle border
- **Logo**: Consistent with sidebar
- **User Section**:
  - Avatar with teal gradient background
  - Welcome message
  - Status indicator: Green dot with "Sistema Activo"
- **Logout**: Teal button with hover effects

### Dashboard Home

#### Decorative Elements
- **Subtle Patterns**: Replace red circles with:
  - Leaf motifs in very low opacity (2-3%)
  - Medical cross silhouettes
  - Wave patterns suggesting cleanliness

#### Statistics Cards
- **Background**: White with rounded corners (12px radius)
- **Border**: Subtle slate-200
- **Icons**: Health-themed SVGs in colored backgrounds:
  - Users: People icon in teal-50 background
  - Employees: Stethoscope icon in green-50
  - Shifts: Clock icon in blue-50
  - Notifications: Bell icon in amber-50
- **Typography**: Bold numbers, descriptive text in slate-600
- **Hover**: Subtle shadow increase

#### Weekly Schedule
- **Header**: Calendar icon in slate-100 circle
- **Grid**: 7 columns, colored based on status:
  - Active: Green-50 background, green text
  - Partial: Teal-50 background, teal text
  - Closed: Slate-50 background, slate text
- **Legend**: Small dots with labels

## Interactive Elements
- **Buttons**: Rounded corners, teal/green gradients
- **Hover States**: Smooth transitions (200ms)
- **Focus States**: Accessible outlines
- **Loading States**: Spinning indicators in teal

## Icons
Use Heroicons or similar for consistency:
- Health-related: stethoscope, pill, syringe, heart
- Standard: user, calendar, bell, etc.

## Responsive Design
- **Mobile**: Collapsible sidebar, stacked cards
- **Tablet**: Adjusted grid layouts
- **Desktop**: Full layout with hover effects

## Accessibility
- **Contrast**: WCAG AA compliant
- **Keyboard Navigation**: Full support
- **Screen Readers**: Proper ARIA labels
- **Color Blind**: Not relying solely on color for information

## Implementation Notes
- Use Tailwind CSS for styling
- Maintain existing component structure
- Gradual color replacement from red to green/teal
- Add subtle animations for modern feel
- Ensure all icons are SVG for scalability

This design maintains functionality while creating a trustworthy, health-focused aesthetic perfect for a pharmacy administrative system.