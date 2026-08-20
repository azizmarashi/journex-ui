# Journex UI

Compose Multiplatform/Desktop client for the Journex Spring Boot API.

## Baseline preserved
- Kotlin JVM 2.0.21
- JetBrains Compose plugin 1.7.3
- Kotlin Compose plugin 2.0.21
- Retrofit 2.11.0
- Gson + Scalars converters
- kotlinx-coroutines-swing 1.8.1
- Desktop distributions: DMG, MSI, DEB

## Implemented
- JWT login/register/logout with local token persistence
- Dashboard and current-user profile
- Profile update and password change
- Strategy list/create/update/delete/trash/restore
- Checklist list/create/update/delete/trash/restore
- Checklist item management and answer API integration
- Trade list/open/close/delete/trash/restore
- Trade risk/journal API integration in network layer
- Pagination models and all documented REST endpoints
- Material 3 desktop navigation and responsive content layouts

## Backend
The default API URL is `http://localhost:9090/` in `RetrofitClient.kt`.

Change `BASE_URL` there if the Spring Boot server is running elsewhere.
