# JihunPage Project Plan

## Overview

JihunPage is a personal introduction website built with React.

The React frontend will be completed first.  
A Spring Boot backend will be added late.

## Pages

### 1. Home

A personal introduction and portfolio page.

Main sections:

- Introduction
- Education and experience
- Skills
- Projects
- Contact information

Route:

```text
/
```

### 2. Signup

A signup page based on the React form created during class.

Main features:

- Form state management
- Empty field validation
- Regular expression validation
- Future Spring Boot signup API integration

Route:

```text
/signup
```

### 3. Login

A page where registered users can log in.

Main features:

- Login form
- Input validation
- Future session-based login
- Login status handling
- Logout

Route:

```text
/login
```

### 4. Guestbook

A page where visitors can leave short messages.

Main features:

- Display guestbook posts
- Create guestbook posts
- Delete posts written by the logged-in user
- Use temporary frontend data before backend integration

Route:

```text
/guestbook
```

### 5. Gallery

A gallery containing photos from my working holiday in Japan.

Initial features:

- Display working holiday photos
- Use a responsive grid layout
- Keep image sizes consistent
- Add a simple hover effect

Future features:

- Photo titles and descriptions
- Photo detail view
- Comments for each photo

Route:

```text
/gallery
```

## Initial Frontend Scope

- Create five pages with React Router
- Create a common navigation bar
- Build a responsive layout with Bootstrap
- Manage form data with React state
- Validate signup and login forms
- Display gallery photos using arrays and `map()`
- Use temporary data for the guestbook

## Future Backend Scope

- Spring Boot
- Spring Data JPA
- Member signup API
- Password hashing
- Session-based login
- Login status API
- Logout API
- Guestbook CRUD
- Gallery comment CRUD

## Development Order

1. Set up React Router and common navigation
2. Create the Home page
3. Create the Gallery page
4. Create the Signup page
5. Create the Login page
6. Create the Guestbook page
7. Add the Spring Boot backend
8. Implement session-based authentication
9. Connect the guestbook APIs
10. Add gallery comments

## Out of Scope

The following technologies will not be included in this project:

- JWT
- Redis
- OAuth login
- Cloud deployment

JWT and Redis will be studied and implemented in the final team project.
