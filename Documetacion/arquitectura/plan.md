# Front-End Architecture Plan

## Folder Structure

```
src/
├── components/          # Reusable UI components
├── pages/              # Page-level components
├── services/           # API communication services (using fetch)
├── hooks/              # Custom React hooks
├── utils/              # Utility functions
├── styles/             # Stylesheets
│   └── global.css      # Global styles with Tailwind import
├── assets/             # Static assets (images, icons)
├── App.jsx             # Root component
├── main.jsx            # Entry point
└── index.css           # Minimal global styles (if needed)
```

## Tailwind CSS Configuration

- Use Tailwind CSS v3 for better configuration options
- Install and configure with tailwind.config.js
- Import Tailwind in global.css
- Remove existing styles from index.css and move to global.css if needed

## API Services Structure

- Use native fetch API instead of axios
- Create base API utility in services/api.js
- Create service modules for each backend module:
  - services/authService.js (Security)
  - services/userService.js
  - services/employeeService.js (HumanResources)
  - services/newsService.js
  - services/organizationService.js
  - services/scheduleService.js
  - services/notificationService.js

## Next Steps

1. Reinstall Tailwind CSS v3
2. Configure tailwind.config.js
3. Move Tailwind import to styles/global.css
4. Clean up index.css
5. Create API service files with fetch
6. Create basic page components
7. Set up routing (React Router)