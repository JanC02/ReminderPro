# Instrukcja uruchomienia projektu ReminderPro

## ✅ Co zostało zaimplementowane

Projekt został w pełni zaimplementowany zgodnie z planem. Zawiera:

### 1. Struktura projektu
- ✅ Kompletna struktura katalogów Android
- ✅ Konfiguracja Gradle (build.gradle.kts, settings.gradle.kts)
- ✅ AndroidManifest.xml z wszystkimi uprawnieniami

### 2. Warstwa danych
- ✅ Model `Reminder` z Room Entity
- ✅ `ReminderDao` - interfejs dostępu do bazy
- ✅ `ReminderDatabase` - baza danych Room (Singleton)
- ✅ `ReminderRepository` - warstwa abstrakcji

### 3. Interfejs użytkownika
- ✅ `MainActivity` - główny ekran
- ✅ `ReminderAdapter` - adapter listy przypomnień
- ✅ `AddReminderDialog` - dialog dodawania z suwakiem i ręcznym wpisywaniem
- ✅ `QuickSelectBottomSheet` - panel szybkiego wyboru
- ✅ Layouty XML dla wszystkich ekranów
- ✅ Material Design 3 z dark mode

### 4. ViewModel
- ✅ `ReminderViewModel` - zarządzanie stanem i danymi

### 5. System powiadomień
- ✅ `NotificationHelper` - tworzenie i zarządzanie powiadomieniami
- ✅ `ReminderScheduler` - schedulowanie z AlarmManager
- ✅ `AlarmReceiver` - odbieranie alarmów
- ✅ `BootReceiver` - restart po restarcie systemu

### 6. Zasoby
- ✅ strings.xml (po polsku)
- ✅ colors.xml (Material Design 3 palette)
- ✅ themes.xml (light i dark theme)
- ✅ Ikony SVG (notifications, timer, water, stretch, itp.)

## 📋 Następne kroki

### Krok 1: Zainstaluj Android Studio

1. Pobierz Android Studio z: https://developer.android.com/studio
2. Zainstaluj zgodnie z instrukcjami dla Fedora Linux
3. Podczas pierwszego uruchomienia zainstaluj:
   - Android SDK
   - Android SDK Platform (API 34+)
   - Android Virtual Device (emulator)

### Krok 2: Otwórz projekt

```bash
# Przejdź do katalogu projektu
cd /home/jach3366/Projekty/reminderpro

# Uruchom Android Studio i otwórz ten folder
# File > Open > wybierz /home/jach3366/Projekty/reminderpro
```

### Krok 3: Zsynchronizuj projekt

Po otwarciu w Android Studio:
1. Kliknij "Sync Project with Gradle Files" (ikona słonia w górnym pasku)
2. Poczekaj aż Gradle pobierze wszystkie zależności (może potrwać kilka minut przy pierwszym uruchomieniu)

### Krok 4: Skonfiguruj urządzenie/emulator

**Opcja A: Emulator (łatwiejsze)**
1. W Android Studio: Tools → Device Manager
2. Kliknij "Create Device"
3. Wybierz urządzenie (np. Pixel 6)
4. Wybierz system Android 13 lub nowszy (API 33+)
5. Kliknij "Finish"

**Opcja B: Fizyczne urządzenie (rekomendowane dla testowania powiadomień)**
1. Na telefonie: Ustawienia → O telefonie → stuknij 7x w "Numer kompilacji"
2. Ustawienia → Opcje deweloperskie → włącz "Debugowanie USB"
3. Podłącz telefon USB do komputera
4. Zaakceptuj dialog o debugowaniu USB na telefonie

### Krok 5: Uruchom aplikację

1. Wybierz urządzenie/emulator z listy rozwijanej w górnym pasku
2. Kliknij zielony przycisk "Run" (▶️) lub naciśnij Shift+F10
3. Poczekaj aż aplikacja się zbuduje i zainstaluje

## 🔧 Kompilacja z terminala (opcjonalna)

Jeśli wolisz terminal:

```bash
# Przejdź do katalogu projektu
cd /home/jach3366/Projekty/reminderpro

# Zbuduj projekt
./gradlew build

# Zainstaluj na podłączonym urządzeniu
./gradlew installDebug

# Lub zbuduj APK
./gradlew assembleDebug
# APK będzie w: app/build/outputs/apk/debug/app-debug.apk
```

## ⚠️ Możliwe problemy i rozwiązania

### Problem 1: "SDK location not found"
**Rozwiązanie:**
Utwórz plik `local.properties` w katalogu projektu:
```
sdk.dir=/home/jach3366/Android/Sdk
```
(Dostosuj ścieżkę do swojej instalacji Android SDK)

### Problem 2: Brak Gradle Wrapper JAR
**Rozwiązanie:**
W Android Studio:
1. File → Settings → Build, Execution, Deployment → Build Tools → Gradle
2. Zaznacz "Use Gradle from: 'wrapper'"
3. Kliknij "Download Gradle" jeśli pojawi się opcja

Lub z terminala (jeśli masz zainstalowane gradle globalnie):
```bash
gradle wrapper --gradle-version 8.11.1
```

### Problem 3: Błędy związane z KSP
**Rozwiązanie:**
Upewnij się, że wersje Kotlin i KSP są kompatybilne:
- Kotlin: 2.1.0
- KSP: 2.1.0-1.0.29

### Problem 4: ViewBinding nie działa
**Rozwiązanie:**
1. Build → Clean Project
2. Build → Rebuild Project

## 📱 Testowanie aplikacji

Po uruchomieniu:

1. **Test dodawania przypomnienia**
   - Kliknij FAB (+)
   - Wypełnij formularz
   - Zapisz

2. **Test szybkiego wyboru**
   - Kliknij "Szybki wybór"
   - Wybierz jeden z szablonów

3. **Test powiadomień**
   - Dodaj przypomnienie z interwałem 1 minuta
   - Zaczekaj 1 minutę
   - Powinno pojawić się powiadomienie

4. **Test włącz/wyłącz**
   - Przełącz switch przy przypomnieniu
   - Sprawdź czy powiadomienia się zatrzymują/wznawiają

5. **Test usuwania**
   - Kliknij "Usuń" przy przypomnieniu
   - Potwierdź w dialogu

## 🎯 Dodatkowe konfiguracje (opcjonalne)

### Podpisywanie APK dla release
Aby utworzyć podpisane APK:

1. Build → Generate Signed Bundle / APK
2. Wybierz APK
3. Utwórz nowy keystore lub użyj istniejącego
4. Wybierz release build type
5. Kliknij Finish

### Optymalizacja dla produkcji

Edytuj `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true  // Włącz ProGuard
        isShrinkResources = true  // Usuń nieużywane zasoby
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## 📚 Przydatne komendy

```bash
# Sprawdź podłączone urządzenia
adb devices

# Zobacz logi aplikacji
adb logcat | grep ReminderPro

# Odinstaluj aplikację
adb uninstall com.reminderpro

# Wyczyść dane aplikacji
adb shell pm clear com.reminderpro

# Instaluj APK bezpośrednio
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🎉 Gotowe!

Twoja aplikacja ReminderPro jest gotowa do uruchomienia!

Jeśli masz pytania lub napotkasz problemy, sprawdź:
- README.md - szczegółowa dokumentacja
- PLAN_DZIAŁANIA.md - plan implementacji
- Kod źródłowy - każda klasa ma komentarze wyjaśniające
