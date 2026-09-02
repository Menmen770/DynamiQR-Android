# תוכנית פיתוח High-Fidelity וארכיטקטורה מקצועית (Java/XML) - שלב ה-Pixel Perfect

המטרה היא להביא את האפליקציה לרמה המקצועית והעיצובית המדויקת של ה-React Native, תוך ארגון מחדש של הקוד לפי סטנדרטים גבוהים של פיתוח אנדרואיד (Feature-based Packaging).

## User Review Required

> [!IMPORTANT]
> - **ארכיטקטורה:** כל הפרויקט יאורגן מחדש תחת חבילות ברורות (`core`, `data`, `features`, `ui`).
> - **דיוק ב-Assets:** כל 18 הסטיקרים, 14 הלוגואים ו-6 צורות ה-QR יוצגו עם תמונות ה-Preview המקוריות שלהם.
> - **דף Learn QR:** יתווסף דף "מה זה QR" המלא הכולל את ה-Workflow Timeline וטבלאות ההשוואה.
> - **UX/UI:** נוסיף צללים, אנימציות לחיצה (Ripple) ומרווחים מדויקים כדי להבטיח תחושת Premium.

## Proposed Changes

### [Phase 1: Architecture & Complete Auth]

ארגון מחדש של התיקיות ומימוש המסכים החסרים במערכת האימות:
- **Architecture:** העברת כל הקבצים למבנה: `core/`, `data/`, `features/auth`, `features/dashboard` וכו'.
- **RegisterScreen:** מסך הרשמה מלא עם ולידציה.
- **VerifyEmailScreen:** ממשק קוד OTP מעוצב עם טיימר ובקשת קוד חוזר.
- **Base Components:** שדרוג ה-`BaseActivity` וה-`BaseFragment` לניהול טעינה ושגיאות בצורה אחידה.

### [Phase 2: Advanced Dashboard & Marketing]

הוספת ה"בשר" לדף הראשי:
- **Folders Logic:** מימוש מלא של סינון ותצוגת תיקיות.
- **Promotional Cards:** שילוב באנרים שיווקיים ("Upgrade to Premium").
- **Edit/Stats Modals:** בניית הדיאלוגים המתקדמים לעריכת יעד וצפייה בנתוני סריקה.

### [Phase 3: The Ultimate QR Generator]

שדרוג המנוע והממשק לרמה הגבוהה ביותר:
- **Gradient Picker:** מימוש בחירת צבעים הדרגתית.
- **Precise Sticker Placement:** חישוב מיקומים (Sticker Rects) לכל 18 הפריימים.
- **Image Export:** מנוע שמירת תמונה לגלריה.

## Verification Plan

### Manual Verification
1. **ויזואלי:** השוואה של 1:1 מול ה-React Native בכל מסך.
2. **ארכיטקטורה:** וודא שכל הקוד יושב בתיקיות ה-Features הנכונות.
3. **פונקציונליות:** הרשמה מלאה כולל אימות קוד מול השרת.
