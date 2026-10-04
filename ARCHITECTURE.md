# DynamiQR Android — Architecture

Native Java port of the React Native app. Package root: `com.dynamiqr.android`.

## Layer map

```
app/
├── DynamiQRApplication.java     # DI root: AuthManager, repositories, ApiService
├── MainActivity.java              # Shell: AppHeader + fragments + bottom nav
│
├── core/
│   ├── base/                      # BaseActivity, BaseFragment (ViewBinding)
│   ├── factory/                   # ViewModelFactory
│   └── utils/                     # QrTypeProvider, ColorProvider, QrEncoder
│
├── data/
│   ├── api/                       # RetrofitClient, ApiService
│   ├── local/                     # AuthManager (token + user prefs)
│   ├── models/                    # Gson DTOs matching backend
│   └── repository/                # AuthRepository, QrRepository
│
├── features/                      # One package per screen / flow
│   ├── auth/                      # Login, Register, VerifyEmail
│   ├── dashboard/                 # My Codes list
│   ├── generator/                 # 3-step QR wizard
│   ├── scanner/                   # CameraX + ML Kit
│   └── learn/                     # Educational content
│
└── ui/
    ├── components/                # Reusable views (AppHeader, ScreenPageHeader, …)
    └── adapters/                  # RecyclerView adapters
```

## MVVM rules

| Layer | Responsibility |
|-------|----------------|
| **Fragment / Activity** | ViewBinding, observe LiveData, forward clicks |
| **ViewModel** | UI state, calls Repository, survives rotation |
| **Repository** | Single source for API; no Android context |
| **ApiService** | Retrofit interface only |

Never call `ApiService` directly from UI — always go through a Repository.

## RN parity reference

| RN path | Android |
|---------|---------|
| `src/screens/MyCodesScreen.js` | `features/dashboard/DashboardFragment` |
| `src/screens/QrGeneratorScreen.js` | `features/generator/GeneratorFragment` |
| `src/components/AppHeader.js` | `ui/components/AppHeaderView` |
| `src/navigation/AppTabBar.js` | `activity_main.xml` custom bottom bar |
| `src/components/auth/AuthScreenLayout.js` | `ui/components/AuthScreenLayout` |

Reference project (read-only): `C:/Users/User/Desktop/DynamiQR/mobile`
