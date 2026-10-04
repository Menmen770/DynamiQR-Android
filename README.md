# DynamiQR — Android (Java)

אפליקציית Android מקורית (Java) ל־**DynamiQR**: יצירה, שמירה, סריקה וניהול של קודי QR דינמיים וסטטיים, בעברית (RTL) ובאנגלית.

האפליקציה מתחברת ל־**אותו Backend** של פרויקט DynamiQR הראשי (Express + MongoDB). יצירת תמונת ה־QR נעשית **בשרת** (`POST /api/generate-qr`), לא במכשיר.

**קישור לפרויקט השרת (Backend):**  
https://github.com/Menmen770/DynamiQR

---

## דרישות הקורס — מה מומש כאן

| דרישה | מימוש |
|--------|--------|
| מסד מקומי באנדרואיד (Room) | טבלת `course_users` ב־SQLite — ת.ז., טלפון, תאריך לידה |
| CRUD לפרופיל כולל מחיקה | עריכה ומחיקת חשבון מקומי מתפריט החשבון |
| משתמשים חדשים באנדרואיד בלי שינוי סכמת Mongo | מודל **Hybrid** (ראו למטה) |
| Navigation Component | `nav_graph_main.xml` + `NavHost` ב־`MainActivity` |
| תרגום HE / EN | `res/values/strings.xml` + `res/values-en/strings.xml` בלבד |
| סיבוב מסך | רק ב־Login / Register / לשונית המדריך (Learn) |
| מצב כהה | `Theme.MaterialComponents.DayNight` + מתג בתפריט |
| היסטוריית Git | קומיטים מסודרים בריפו זה |

---

## Room מול Mongo — מה נשמר איפה?

האפליקציה **לא** משנה את מסד ה־Mongo של הפרויקט המשותף. במקום זה יש הפרדה ברורה:

### Room (במכשיר בלבד) — דרישת הקורס

- קובץ DB מקומי: `dynamiqr_course_local.db`
- ישות: `LocalUserEntity` / טבלה `course_users`
- שדות קורס: **תעודת זהות**, **טלפון**, **תאריך לידה**, שם, אימייל, hash סיסמה מקומי
- נשאר על המכשיר; ניתן **למחוק** דרך מחיקת החשבון באפליקציה
- **לא** מסונכרן לשרת / Mongo

### MongoDB (בשרת DynamiQR) — לוגיקת המוצר

- משתמשים ל־JWT / שמירת QR / סטטיסטיקות / תיקיות
- קודים שמורים, סגנון QR, הפניות דינמיות (`/r/:slug`)
- האנדרואיד קורא ל־API הקיים בלבד — **בלי שינוי סכמה בשרת**

### מודל Hybrid (למה שניהם?)

1. **הרשמה / התחברות** מול ה־Backend → JWT לעבודה עם QR (כמו באתר / RN).
2. **במקביל** נשמר פרופיל קורס ב־Room (ת.ז. / טלפון / DOB) על אותו אימייל.
3. כך ה־Dashboard וה־QR עובדים מול השרת, ודרישות הקורס על Room מתקיימות בלי לגעת ב־Mongo.

```
┌─────────────────────┐         ┌──────────────────────────┐
│  Android app        │  JWT    │  DynamiQR Backend        │
│  Generator/Dashboard├────────►│  Express + MongoDB       │
│                     │         │  Users, Saved QRs, stats │
│  Room (course DB)   │  local  │                          │
│  ת.ז. / phone / DOB │◄─only───┤  (לא משתנה לקורס)        │
└─────────────────────┘         └──────────────────────────┘
```

---

## הרצת ה־Backend (חובה כדי שהאפליקציה תעבוד)

האפליקציה הזו מכילה **רק את לקוח האנדרואיד**. השרת נמצא בפרויקט הנפרד:

**https://github.com/Menmen770/DynamiQR** → תיקיית `backend/`

### 1. הורדה

```bash
git clone https://github.com/Menmen770/DynamiQR.git
cd DynamiQR/backend
```

### 2. דרישות מערכת

- Node.js 18+
- MongoDB מקומי **או** MongoDB Atlas
- npm

### 3. הגדרת `.env`

```bash
cp .env.example .env
```

ערכים מינימליים לדוגמה:

```env
MONGO_URI=mongodb://127.0.0.1:27017/dynamiqr
PORT=5000
NODE_ENV=development
SESSION_SECRET=replace_with_strong_secret
JWT_SECRET=replace_with_jwt_secret
FRONTEND_URL=http://localhost:5173
BACKEND_URL=http://localhost:5000
```

(SMTP / OAuth אופציונליים לבדיקה בסיסית של QR.)

### 4. התקנה והרצה

```bash
npm install
npm run dev
```

השרת אמור לענות ב־`http://localhost:5000` (או ה־IP של המחשב ברשת).

### 5. חיבור האפליקציה לשרת

ב־`app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://YOUR_LAN_IP:5000/\"")
```

- **אמולטור Android:** לרוב `http://10.0.2.2:5000/`
- **מכשיר פיזי:** IP המחשב ברשת המקומית, למשל `http://192.168.1.x:5000/`  
  (המכשיר והמחשב באותה Wi‑Fi; Firewall מאפשר פורט 5000)

אחרי שינוי — **Sync Gradle** והרצה מחדש.

> `localhost` במכשיר **לא** מצביע על המחשב שלכם — חובה IP / `10.0.2.2`.

---

## הרצת האפליקציה (Android Studio)

1. פתיחת התיקייה הזו ב־Android Studio.
2. Sync Gradle.
3. וידוא שה־Backend רץ ו־`API_BASE_URL` נכון.
4. Run על אמולטור / מכשיר (API 24+).

Package: `com.dynamiqr.android`

---

## מבנה כללי

```
app/src/main/java/com/dynamiqr/android/
├── features/          # auth, dashboard, generator, scanner, learn
├── data/
│   ├── api/           # Retrofit → Backend
│   ├── local/db/      # Room — course users
│   └── repository/
├── core/              # utils, base, LocaleHelper
└── ui/                # adapters, components
```

Navigation: `app/src/main/res/navigation/nav_graph_main.xml`

---

## צ׳ק־ליסט לבודק / מרצה

1. Clone של הריפו הזה + Clone של [DynamiQR](https://github.com/Menmen770/DynamiQR).
2. הרצת `backend` עם Mongo + `.env`.
3. עדכון `API_BASE_URL` באנדרואיד.
4. הרשמה / התחברות → יצירת QR → שמירה לדשבורד.
5. פרופיל: עריכת ת.ז./טלפון/תאריך לידה ב־Room; מחיקת חשבון מקומית.
6. החלפת שפה HE↔EN; מצב כהה; סיבוב ב־Login/Register/Learn.

---

## רישיון / הקשר אקדמי

פרויקט קורס Android — לקוח Native Java מעל Backend קיים של DynamiQR, עם תוספת Room מקומית לדרישות הקורס בלבד.
