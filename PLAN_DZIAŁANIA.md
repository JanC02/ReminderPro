# Plan Działania - Aplikacja ReminderPro na Androida

## 1. Informacje o środowisku
- **Java/JDK**: OpenJDK 21.0.9 (kompatybilne z Android)
- **System**: Linux (Fedora)
- **Gradle**: Będzie używany Gradle Wrapper (część projektu)
- **Android SDK**: Wymagane do instalacji (przez Android Studio lub narzędzia CLI)

## 2. Wymagania funkcjonalne aplikacji

### 2.1 Podstawowe funkcje
- Dodawanie nowych przypomnień
- Usuwanie istniejących przypomnień
- Wyłączanie/włączanie przypomnień bez usuwania
- Wyświetlanie powiadomień w określonych interwałach

### 2.2 Ustawianie interwału przypomnień
- Ręczne wpisywanie wartości (liczba + jednostka czasu: minuty/godziny/dni)
- Suwak do szybkiego wyboru interwału
- Obsługa różnych jednostek czasowych

### 2.3 Panel szybkiego wyboru
- Predefiniowane szablony przypomnień (np. "Pij wodę co 2h", "Rozciąganie co 30min")
- Szybkie dodawanie popularnych przypomnień jednym kliknięciem

## 3. Architektura aplikacji

### 3.1 Struktura projektu
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/reminderpro/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── Reminder.kt (model danych)
│   │   │   │   ├── ReminderDatabase.kt (Room database)
│   │   │   │   ├── ReminderDao.kt (Data Access Object)
│   │   │   │   └── ReminderRepository.kt
│   │   │   ├── ui/
│   │   │   │   ├── ReminderAdapter.kt (RecyclerView adapter)
│   │   │   │   ├── AddReminderDialog.kt
│   │   │   │   └── QuickSelectFragment.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── ReminderViewModel.kt
│   │   │   ├── workers/
│   │   │   │   └── ReminderWorker.kt (WorkManager)
│   │   │   └── notifications/
│   │   │       └── NotificationHelper.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   └── build.gradle
```

### 3.2 Technologie i biblioteki

#### Podstawowe
- **Kotlin** - język programowania
- **Android SDK** - minimalna wersja: API 26 (Android 8.0), docelowa: API 34+

#### Persistence (przechowywanie danych)
- **Room Database** - lokalna baza danych SQLite
- SharedPreferences - ustawienia aplikacji

#### Background Work (zadania w tle)
- **WorkManager** - zarządzanie okresowymi zadaniami w tle
- **AlarmManager** - precyzyjne alarmy (dla dokładnych interwałów)

#### UI/UX
- **Material Design 3** - nowoczesny design
- **RecyclerView** - lista przypomnień
- **ViewBinding** - bezpieczne odwoływanie się do widoków
- **ViewModel + LiveData** - architektura MVVM

#### Powiadomienia
- **NotificationCompat** - kompatybilne powiadomienia
- **NotificationChannel** - kanały powiadomień (Android 8.0+)

## 4. Etapy implementacji

### Etap 1: Konfiguracja projektu
1. Utworzenie nowego projektu Android w Kotlinie
2. Konfiguracja build.gradle:
   - Dodanie zależności (Room, WorkManager, Material Design)
   - Ustawienie minSdkVersion: 26, targetSdkVersion: 34
   - Włączenie ViewBinding
3. Konfiguracja uprawnień w AndroidManifest.xml:
   - POST_NOTIFICATIONS (Android 13+)
   - SCHEDULE_EXACT_ALARM (dla precyzyjnych alarmów)
   - RECEIVE_BOOT_COMPLETED (restart przypomnień po restarcie)

### Etap 2: Model danych i baza
1. Utworzenie klasy `Reminder` (data class):
   - id: Long
   - title: String
   - message: String
   - intervalMinutes: Int
   - isEnabled: Boolean
   - createdAt: Long
   - lastTriggered: Long?
2. Konfiguracja Room Database:
   - ReminderDao (insert, update, delete, getAll, getById)
   - ReminderDatabase (singleton)
   - ReminderRepository (warstwa abstrakcji)

### Etap 3: UI - Ekran główny
1. Layout głównego ekranu (activity_main.xml):
   - RecyclerView dla listy przypomnień
   - FloatingActionButton (FAB) do dodawania
   - Przycisk do panelu szybkiego wyboru
2. ReminderAdapter:
   - Wyświetlanie przypomnienia (tytuł, interwał, status)
   - Przełącznik włącz/wyłącz
   - Przycisk usuwania
3. ReminderViewModel:
   - LiveData z listą przypomnień
   - Metody: addReminder, deleteReminder, toggleReminder

### Etap 4: Dodawanie przypomnień
1. Dialog/Activity dodawania przypomnienia:
   - Pole tekstowe: tytuł
   - Pole tekstowe: treść przypomnienia
   - SeekBar (suwak) dla interwału:
     - Zakres: 1-1440 minut (1min - 24h)
     - Wyświetlanie aktualnej wartości
   - EditText dla ręcznego wpisania:
     - Pole liczbowe
     - Spinner/Dropdown: minuty/godziny/dni
   - Przycisk "Zapisz"
2. Walidacja danych
3. Zapis do bazy danych

### Etap 5: Panel szybkiego wyboru
1. Fragment/Activity z predefiniowanymi szablonami:
   - "Pij wodę" - co 2 godziny
   - "Rozciąganie" - co 30 minut
   - "Przerwa od ekranu" - co 20 minut
   - "Witaminy" - codziennie rano
   - "Ćwiczenia" - co 4 godziny
   - "Nawodnienie skóry" - co 3 godziny
2. GridLayout lub RecyclerView z kartami
3. Kliknięcie = szybkie dodanie z domyślnymi wartościami

### Etap 6: System powiadomień
1. NotificationHelper:
   - Tworzenie kanału powiadomień
   - Budowanie i wyświetlanie powiadomień
   - Akcje w powiadomieniach (opcjonalnie: "Wyłącz", "Odłóż")
2. Styl powiadomień:
   - Ikona aplikacji
   - Tytuł i treść
   - Priorytet (ważne przypomnienia)
   - Dźwięk i wibracje

### Etap 7: Planowanie zadań w tle
1. ReminderWorker (WorkManager):
   - Sprawdzanie aktywnych przypomnień
   - Obliczanie czy minął czas od ostatniego powiadomienia
   - Wywoływanie NotificationHelper
2. Schedulowanie okresowych zadań:
   - PeriodicWorkRequest (co 15 minut minimum)
   - Alternatywnie: OneTimeWorkRequest z delays dla każdego przypomnienia
3. AlarmManager dla precyzyjnych alarmów:
   - Ustawienie dokładnego czasu powiadomienia
   - BroadcastReceiver do obsługi alarmów
   - Rescheduling po restarcie urządzenia

### Etap 8: Zarządzanie cyklem życia
1. BootReceiver:
   - Restart wszystkich aktywnych przypomnień po restarcie systemu
2. Obsługa uprawnień:
   - Request notification permission (Android 13+)
   - Request exact alarm permission
3. Optymalizacja baterii:
   - Wyjaśnienie użytkownikowi potrzeby wyłączenia optymalizacji

### Etap 9: UI/UX Improvements
1. Material Design 3 styling:
   - Kolory, typografia, kształty
   - Dark mode support
2. Animacje:
   - Dodawanie/usuwanie elementów z listy
   - Przejścia między ekranami
3. Empty state:
   - Komunikat gdy brak przypomnień
   - Grafika zachęcająca do dodania pierwszego

### Etap 10: Testowanie i debugowanie
1. Testy jednostkowe:
   - Repository
   - ViewModel
2. Testy UI (opcjonalnie):
   - Espresso tests
3. Testy manualne:
   - Dodawanie/usuwanie/wyłączanie
   - Weryfikacja powiadomień
   - Test po restarcie urządzenia
   - Test różnych interwałów

### Etap 11: Optymalizacje i funkcje dodatkowe
1. Edycja istniejących przypomnień
2. Kategoryzacja przypomnień
3. Statystyki (ile razy przypomnienie zostało wyświetlone)
4. Eksport/import danych
5. Widget na ekran główny

## 5. Potencjalne wyzwania i rozwiązania

### Wyzwanie 1: Ograniczenia systemu Android w tle
**Problem**: Android ogranicza działanie aplikacji w tle (Doze mode, App Standby)
**Rozwiązanie**:
- Użycie WorkManager z constraints
- AlarmManager z setExactAndAllowWhileIdle()
- Prośba o wyłączenie optymalizacji baterii dla aplikacji

### Wyzwanie 2: Dokładność interwałów
**Problem**: WorkManager ma minimum 15-minutowy interwał
**Rozwiązanie**:
- Dla interwałów < 15 min: użycie AlarmManager
- Dla interwałów >= 15 min: WorkManager

### Wyzwanie 3: Uprawnienia na Android 13+
**Problem**: Wymagane runtime permission dla powiadomień
**Rozwiązanie**:
- Runtime permission request
- Wyjaśnienie użytkownikowi dlaczego to jest potrzebne
- Graceful degradation jeśli odmówi

### Wyzwanie 4: Restart urządzenia
**Problem**: Zaplanowane zadania są czyszczone po restarcie
**Rozwiązanie**:
- RECEIVE_BOOT_COMPLETED permission
- BootReceiver do reinicjalizacji wszystkich przypomnień

## 6. Narzędzia potrzebne do rozwoju

### Wymagane
1. **Android Studio** (Ladybug | 2024.2.1 lub nowszy)
   - Zawiera: Android SDK, Emulator, ADB, Gradle
   - Download: https://developer.android.com/studio

2. **Java Development Kit**
   - ✅ OpenJDK 21.0.9 już zainstalowane

### Opcjonalne
1. **Fizyczne urządzenie Android** (rekomendowane do testowania powiadomień)
2. **Scrcpy** - narzędzie do mirror urządzenia na komputer
3. **Git** - kontrola wersji

## 7. Komendy do rozpoczęcia (po instalacji Android Studio)

```bash
# Utworzenie projektu przez Android Studio GUI lub:
# Użycie Android Studio do utworzenia nowego projektu:
# File > New > New Project > Empty Views Activity
# Name: ReminderPro
# Package: com.reminderpro
# Language: Kotlin
# Minimum SDK: API 26 (Android 8.0)

# Po utworzeniu projektu, build:
./gradlew build

# Uruchomienie na emulatorze/urządzeniu:
./gradlew installDebug
```

## 8. Przewidywany czas implementacji

- **Etapy 1-2** (Setup + Database): 2-3 godziny
- **Etapy 3-4** (UI podstawowe + Dodawanie): 3-4 godziny
- **Etap 5** (Panel szybkiego wyboru): 1-2 godziny
- **Etapy 6-7** (Powiadomienia + Background): 4-5 godzin
- **Etapy 8-9** (Lifecycle + UI polish): 2-3 godziny
- **Etap 10** (Testowanie): 2-3 godziny
- **Etap 11** (Dodatkowe funkcje): opcjonalnie

**Łącznie**: ~15-20 godzin dla pełnej funkcjonalności

## 9. Następne kroki

1. Zainstalować Android Studio z https://developer.android.com/studio
2. Skonfigurować emulator lub podłączyć fizyczne urządzenie
3. Utworzyć nowy projekt Android w Kotlinie
4. Rozpocząć implementację od Etapu 1

---

**Uwaga**: Ten plan jest elastyczny i może być modyfikowany w trakcie implementacji w zależności od napotkanych wyzwań i dodatkowych wymagań.
