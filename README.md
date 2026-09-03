# Hotel Hop

Android app for browsing hotels in Egypt, saving favorites offline, and simulating a booking with cash or card.

**Package:** `com.task.hotelhop`  
**Language:** Kotlin  
**UI:** Jetpack Compose  
**Min SDK:** 24 · **Target SDK:** 36

---

## What it does

Hotel Hop is an offline-first hotel browser. Users can search and inspect hotels without an account. Signing in unlocks favorites and booking. The catalog is cached in Room so Home still works after the first successful fetch, including when the network drops.

The product flow is:

1. Native Android splash (app icon on black).
2. Onboarding (first launch only).
3. Login / Register, or **Continue as a guest**.
4. Home, Search, Favorites, Account (bottom navigation).
5. Hotel details → Checkout (signed-in users only).

---

## Features

### Guest browsing

- Login has **Continue as a guest**.
- Guests can open Home, Search, and hotel details.
- Favorites, Book stay, and similar actions show a dialog: sign in first, with a button to Login.
- Account shows **Sign in** instead of **Log out**.
- Favorites shows a guest empty state instead of leftover hearts.
- Closing Login without signing in keeps the guest session.
- Cold start: if guest or logged in, go to Home; otherwise Login.

### Auth

- Email / password login and register (Firebase Auth).
- Google Sign-In via Credential Manager + Firebase `GoogleAuthProvider`.
- Display name is taken from the profile, then email prefix, then “Guest”.
- Theme (light / dark / system) and language (English / Arabic) persist in DataStore.
- RTL follows Arabic.

### Home

- Cached hotels first, then a network refresh.
- **Popular stays:** top rated (up to 8).
- **Best price:** cheapest hotels that are **not** already in Popular.
- **Explore:** paged grid of the full cache.
- Pull to refresh and infinite scroll (`limit` / `offset`, page size 20).
- Room replace of the non-favorite cache is a **single transaction**, so Home does not flash an empty list while refreshing.

### Search

- Semantic search against the remote API.
- Filters: price (`Under $100`, `$100–$200`, `$200+`) and rating (`3+`, `4+`, `4.5+`).
- Query + filters apply to remote results.
- Filters only (empty query) apply to the cached hotel list.
- If remote search fails, results fall back to a local name/city match.

### Favorites

- Heart on cards and on details (signed-in only).
- Removing a favorite asks for confirmation first.
- Stored locally in Room (`isFavorite`). Survives offline.

### Hotel details

- Image pager with counter and dots.
- Amenities, about text, map link, book CTA.
- Book stay is blocked for guests.

### Checkout / booking

- Check-in and check-out cannot be in the past.
- Check-out must be after check-in (picker and validation).
- Rooms 1–10, nights, subtotal, 15% VAT, total.
- **Cash on arrival:** confirms immediately.
- **Pay with card:** Paymob Intention API + Unified Checkout WebView. No phone field in the UI.
- Success is a **full-screen** confirmation (reference + Done), not a dialog.

### Shell / UX

- Native splash (`Theme.SplashScreen`), then onboarding / login / home.
- Crisp vector launcher icon (hotel + pin). The marketing logo is not used in-app.
- Double-tap on a hotel card opens details once.
- Dark mode uses the dark surface for the scaffold and window, so the bottom nav does not leave a white strip on details.

---

## Architecture

Clean Architecture + MVI, wired with Koin.

```
presentation  →  domain  →  data
     UI           use cases      Room / DataStore / Ktor / Firebase
     ViewModels   repositories   DTOs + mappers
```

### Layers

| Layer | Responsibility |
| --- | --- |
| **Presentation** | Compose screens, MVI contracts (`UiState` / `UiEvent` / `UiEffect`), ViewModels |
| **Domain** | Entities (`Hotel`, `User`), repository interfaces, use cases, `AppException` |
| **Data** | Repository implementations, local/remote data sources, Room, DataStore, Ktor, Firebase, Paymob |

Screens do not talk to Firebase, Room, or Ktor directly. ViewModels call use cases. Use cases call repositories.

### MVI shape

Each feature follows the same trio:

- `*Contract.kt` — `UiState`, `UiEvent`, `UiEffect`
- `*ViewModel.kt` — `onEvent()`, state flow, one-shot effects
- `*Screen.kt` — Compose UI, `CollectEffect` for navigation / snackbars

Example: `HomeContract` + `HomeViewModel` + `HomeScreen`.

### Dependency injection

Koin modules in `di/AppModule.kt`:

- `networkModule` — `FirebaseAuth`, Ktor `HttpClient`, `HotelApiService`, `PaymobApiService`
- `localModule` — Room `HotelHopDatabase`, DAO, DataStore
- `dataSourceModule` — local/remote user and hotel sources
- `repositoryModule` — `UserRepository`, `HotelRepository`, `PaymentRepository`
- `useCaseModule` — factories per use case
- `viewModelModule` — `viewModelOf(...)` for every screen ViewModel

---

## Package map

```
com.task.hotelhop
├── HotelHopApp.kt              # Koin start
├── MainActivity.kt             # native splash + setContent
├── di/AppModule.kt
├── domain/
│   ├── entity/                 # Hotel, User, PaymobCheckoutSession
│   ├── exception/AppException.kt
│   ├── repo/                   # HotelRepository, UserRepository, PaymentRepository
│   └── usecase/{user,hotel,payment}/
├── data/
│   ├── datasource/{user,hotel}/
│   ├── local/{dao,db,entity,converters}/
│   ├── remote/{dto,service}/
│   ├── mapper/
│   └── repo/
└── presentation/
    ├── main/                   # theme, language, start destination
    ├── navigation/             # Screen routes, bottom bar
    ├── design_system/          # colors, typography, shared components
    ├── login / register / on_boarding
    ├── home / search / favorite / account
    ├── hotel_details / checkout
    └── util/                   # UiText, navigation throttle, dates
```

---

## Screens and navigation

| Route | Who can open it | Notes |
| --- | --- | --- |
| Native splash | Everyone | System splash only. No Compose splash screen. |
| `onboarding` | First launch | Skip / get started → Login |
| `login` | Guests and signed-out users | Email, Google, continue as guest |
| `register` | From login | Email signup with display name |
| `home` | Guest or signed in | Cached + paged catalog |
| `search` | Guest or signed in | Semantic search + filters |
| `favorites` | Both (guest sees CTA) | Room favorites |
| `account` | Both | Theme, language, sign in / log out |
| `hotel_details/{id}` | Guest or signed in | Book gated |
| `checkout/{id}` | Signed in | Cash or Paymob card |

Bottom tabs: Home, Search, Favorites, Account. Details and checkout hide the bar.

Start destination is resolved in `MainViewModel` before the splash is dismissed:

- Not onboarded → Onboarding
- Logged in **or** guest → Home
- Else → Login

---

## Data, cache, and pagination

### Hotels

Remote (`HotelApiService`, Ktor):

| Call | Endpoint | Params |
| --- | --- | --- |
| List | `GET {BASE_URL}/hotels` | `countryCode=EG`, `limit`, `offset`, `X-API-Key` |
| Details | `GET {BASE_URL}/hotel` | `hotelId` |
| Search | `GET {BASE_URL}/hotels/semantic-search` | `query`, `limit` |

Local: Room table `hotels`. Favorites are a flag on the same rows.

**Refresh (Home, offset 0)**

1. Fetch the first page from the API.
2. Read current favorite IDs.
3. In one Room transaction: delete non-favorites, insert the new page (favorites preserved).
4. UI observes `getHotels()` and never applies an empty emit while a refresh is running.

**Next pages** upsert into Room (`OnConflictStrategy.REPLACE`) and append to the Explore grid.

**Details** prefer the network, then the cached row if offline.

### User preferences

DataStore (`settings`):

- logged in
- guest
- theme
- language
- onboarding seen
- cached user profile (id, names, email, gender)

Login / Google / signup set logged in and clear guest. Logout clears both. Continue as guest sets guest and clears the cached user.

---

## Payments (Paymob)

Card checkout is a **simulation path** using Paymob Intention + Unified Checkout.

1. App creates an intention (amount in cents, hotel name, booking reference).
2. Opens `PaymobCheckoutActivity` (WebView).
3. Redirect `https://hotelhop.app/paymob/result` is intercepted.
4. Success → full-screen confirmation with `HH-XXXXXXXX`.

Keys are read from `local.properties` into `BuildConfig`. They are **not** committed.

Cash bookings skip Paymob and generate the same style of reference locally.

VAT is 15% of `pricePerNight × nights × rooms`.

---

## Design system

Custom theme in `presentation/design_system`:

- Brand colors: primary `#39BADF`, secondary `#FF7C38`
- Surfaces: light `#F9FBFB` / dark `#121212`
- Typeface: Rubik
- Shared pieces: `HotelCard`, `HotelHopButton`, `HotelHopTextField`, `HotelHopAlertDialog`, `HotelHopSnackbarHost`, `HotelHopEmptyState`, login-required and unfavorite dialogs

Material 3 color scheme is mapped from these tokens so date pickers, chips, and ripples match the brand.

English (`values/strings.xml`) and Arabic (`values-ar/strings.xml`). Language is applied with `AppCompatDelegate` locales (`locales_config.xml`).

---

## Project setup

### Requirements

- Android Studio (recent AGP 9.x / JDK 11+)
- A device or emulator, API 24+
- Firebase project with the Android app `com.task.hotelhop`
- Hotel catalog API key and base URL
- Optional: Paymob keys for card checkout
- Optional: Google Sign-In (Web client ID + SHA fingerprints)

### Files that stay local (gitignored)

- `local.properties` — SDK path and secrets
- `app/google-services.json` — Firebase config (download from Console)

### `local.properties`

```properties
sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

lite_api_key=YOUR_HOTEL_API_KEY
lite_base_url=YOUR_HOTEL_API_BASE_URL

google_web_client_id=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com

paymob_secret_key=
paymob_public_key=
paymob_base_url=https://accept.paymob.com
paymob_currency=EGP
paymob_integration_id_card=5399135
```

Never commit this file.

### Google Sign-In

Other apps on a phone can use Google; Hotel Hop still needs **its own** OAuth clients.

1. Firebase Console → project settings → Android app `com.task.hotelhop`.
2. Add the **debug** keystore fingerprints:

```
SHA-1:   FF:3A:E8:63:89:48:60:85:4A:48:2E:3D:7D:56:E2:B3:C7:15:9A:8A
SHA-256: 6E:F2:5F:BF:F7:C8:64:94:51:64:24:C9:05:1C:42:E7:DA:23:DD:C2:58:AA:4B:C1:08:7F:7A:C8:FB:D1:B2:D9
```

3. Authentication → Sign-in method → enable **Google**.
4. Download a new `google-services.json` into `app/` (it must contain `oauth_client` entries, not `[]`).
5. Set `google_web_client_id` to the **Web** client (type 3). The app also reads `default_web_client_id` if the plugin generates it.

Release builds need the release keystore SHA-1 as well.

---

## Build and run

```bash
./gradlew :app:assembleDebug
```

On Windows:

```bat
gradlew.bat :app:assembleDebug
```

Then Run from Android Studio, or install `app/build/outputs/apk/debug/app-debug.apk`.

After changing the launcher icon, uninstall the old build once so the home-screen icon refreshes.

---

## Testing

Unit-test stack (already in Gradle): JUnit 4, MockK, Turbine, Ktor mock, Room testing, Koin test, Coroutines test.

Instrumentation: Espresso + Compose UI test.

There is no requirement to run a full suite for local feature work; add tests next to the use case or ViewModel you change.

---

## Conventions

- One feature = contract + ViewModel + screen, then wire nav/DI.
- New user-facing copy goes in **both** `values/strings.xml` and `values-ar/strings.xml`.
- Secrets only in `local.properties` / `google-services.json`.
- Default integration branch is **`develop`**. Feature branches: `feature/<name>`.
- Commit style: `feat(scope): short why`.

Recent feature slices (merge order into `develop`):

`app-shell` → `splash` → `onboarding` → `login` → `register` → `home` → `search` → `favorites` → `hotel-details` → `checkout` → `account` → `guest-mode`

---

## Guest vs signed-in (quick matrix)

| Action | Guest | Signed in |
| --- | --- | --- |
| Browse Home / Search / details | Yes | Yes |
| Add favorite | Login dialog | Yes |
| Remove favorite | Login dialog | Confirm dialog, then remove |
| Book stay | Login dialog | Checkout |
| Account primary button | Sign in | Log out |
| Cold start | Home | Home |

---

## License / author

Course / portfolio project by **Abdelrahman**. Repository: [AbdelrahmanKarim/HotelHop](https://github.com/AbdelrahmanKarim/HotelHop).
