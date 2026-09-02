# תוכנית פיתוח רכיבים מתקדמת (Java/XML) - שחזור נאמן למקור

המטרה היא להשלים את הפער בין האפליקציה הבסיסית הנוכחית לבין המורכבות המלאה של פרויקט ה-React Native, תוך שימוש ברכיבים מותאמים אישית (Custom Views) ו-XML Layouts מורכבים.

## User Review Required

> [!IMPORTANT]
> - אני אבנה מחדש את מערכת ה-Auth כך שתכלול את ה-`AuthScreenLayout` המקורי (לוגו מותאם, כותרות דינמיות וניווט עליון).
> - ה-Dashboard ישודרג לכלול את מערכת התיקיות (Folders) והסינונים המתקדמים באמצעות `BottomSheetDialogFragment`.
> - תהליך יצירת ה-QR ישודרג לשימוש ב-`ViewPager2` או מערכת צעדים מבוססת Fragments כדי לתמוך ב-`QrTypeSelector` המורכב וב-`StylePanel`.

## Proposed Changes

### [UI Components - Custom Views]

#### [NEW] `ScreenPageHeader.java` & `layout_screen_page_header.xml`
- רכיב כותרת ממורכזת עם כותרת וסב-כותרת, בשימוש בכל המסכים.

#### [NEW] `AuthScreenLayout.java`
- Layout בסיס למסכי התחברות הכולל טיפול במקלדת (KeyboardAvoidingView), לוגו המותג, וכפתורי הגדרות עליונים.

### [Authentication Overhaul]

#### [MODIFY] [activity_login.xml](file:///C:/Users/User/Desktop/DynamiQR%20mobile/app/src/main/res/layout/activity_login.xml)
- שדרוג העיצוב כך שישתמש ב-`AuthScreenLayout`.
- הוספת כפתור `GoogleSignInButton` המעוצב.
- הוספת `AuthLegalFooter` עם קישורים לתנאי שימוש ופרטיות.

### [Dashboard Enhancements]

#### [NEW] `MyCodesFolderSheet.java` & `layout_folder_sheet.xml`
- מימוש ה-Sheet שעולה מלמטה עם רשימת תיקיות, מונה קודים וסינון סטטוס (פעיל/לא פעיל).

#### [NEW] `SimplePromptModal.java`
- דיאלוג מעוצב ליצירת תיקייה חדשה או שינוי שם.

### [Advanced QR Generator]

#### [NEW] `QrTypeSelectorView.java`
- רכיב בחירת סוג קוד המחולק ל"ראשי" ו"עוד" (More), עם אנימציית LinearGradient (באמצעות Drawable).

#### [NEW] `QrStylePanel.java`
- ממשק הטאבים (Color, Shape, Logo, Sticker) להתאמה אישית של ה-QR.

#### [NEW] `QrPreviewComposite.java`
- תצוגה מקדימה מורכבת המשלבת את ה-QR עם ה-Sticker הנבחר והרקע.

## Verification Plan

### Manual Verification
1. **Auth:** וודא שהלוגו מופיע בגודל הנכון והמקלדת לא מסתירה את השדות.
2. **Folders:** פתח את ה-Sheet בדאשבורד, וודא שהסינון עובד והתיקיות נטענות.
3. **Generator:** מעבר בין סוגי קוד שונים ולוודא שהעיצוב משתנה בהתאם ב-Preview.
