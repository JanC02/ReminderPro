# ReminderPro - Aplikacja przypomnień na Androida

Aplikacja mobilna na system Android napisana w języku Kotlin, która wysyła regularne przypomnienia w formie powiadomień.

## Funkcje

### ✅ Podstawowe funkcjonalności
- ➕ **Dodawanie przypomnień** - twórz własne przypomnienia z dowolną treścią
- 🗑️ **Usuwanie przypomnień** - usuń niepotrzebne przypomnienia
- 🔄 **Włączanie/wyłączanie** - tymczasowo wyłącz przypomnienia bez usuwania
- 🔔 **Powiadomienia** - otrzymuj powiadomienia w określonych interwałach

### ⏱️ Elastyczne ustawianie interwału
- 📊 **Suwak** - szybkie ustawienie interwału od 1 minuty do 24 godzin
- ⌨️ **Ręczne wpisanie** - precyzyjne określenie czasu (minuty, godziny, dni)
- 🎯 **Dowolne wartości** - pełna kontrola nad częstotliwością przypomnień

### ⚡ Panel szybkiego wyboru
Gotowe szablony przypomnień z jednym kliknięciem:
- 💧 **Pij wodę** - co 2 godziny
- 🧘 **Rozciąganie** - co 30 minut
- 🖥️ **Przerwa od ekranu** - co 20 minut
- 💊 **Witaminy** - codziennie
- 🏃 **Ćwiczenia** - co 4 godziny
- 🪑 **Postawa** - co 45 minut

## Technologie

### Architektura
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern**
- **Single Activity Architecture**

### Biblioteki i frameworki
- **Kotlin** - język programowania
- **Room Database** - lokalna baza danych SQLite
- **AlarmManager** - precyzyjne schedulowanie przypomnień
- **Material Design 3** - nowoczesny interfejs użytkownika
- **ViewBinding** - bezpieczne odwoływanie się do widoków
- **Coroutines** - asynchroniczne operacje
- **LiveData + ViewModel** - reaktywne UI

## Wymagania systemowe

- **Minimalna wersja Android**: 8.0 (API 26)
- **Docelowa wersja Android**: 14+ (API 34+)
- **Java/JDK**: OpenJDK 17 lub nowszy
- **Gradle**: 8.11.1 (Gradle Wrapper)

## Instalacja i uruchomienie

### Wymagane narzędzia
1. **Android Studio** (Ladybug | 2024.2.1 lub nowszy)
   - Pobierz ze strony: https://developer.android.com/studio

2. **Android SDK** (instalowane przez Android Studio)
   - Minimum API 26
   - Target API 34+

### Kroki instalacji

1. **Sklonuj lub pobierz projekt**
   ```bash
   cd /home/jach3366/Projekty/reminderpro
   ```

2. **Otwórz w Android Studio**
   - File → Open → wybierz folder projektu
   - Android Studio automatycznie pobierze zależności

3. **Skonfiguruj emulator lub urządzenie**
   - **Emulator**: Tools → Device Manager → Create Device
   - **Fizyczne urządzenie**: Włącz tryb deweloperski i debugowanie USB

4. **Zbuduj projekt**
   ```bash
   ./gradlew build
   ```

5. **Uruchom aplikację**
   - Kliknij zielony przycisk "Run" w Android Studio
   - Lub użyj komendy:
   ```bash
   ./gradlew installDebug
   ```

## Struktura projektu

```
app/
├── src/main/
│   ├── java/com/reminderpro/
│   │   ├── MainActivity.kt                 # Główna aktywność
│   │   ├── data/                          # Warstwa danych
│   │   │   ├── Reminder.kt                # Model przypomnienia
│   │   │   ├── ReminderDao.kt             # Data Access Object
│   │   │   ├── ReminderDatabase.kt        # Baza danych Room
│   │   │   └── ReminderRepository.kt      # Repository
│   │   ├── ui/                            # Komponenty UI
│   │   │   ├── ReminderAdapter.kt         # Adapter listy
│   │   │   ├── AddReminderDialog.kt       # Dialog dodawania
│   │   │   ├── QuickSelectBottomSheet.kt  # Panel szybkiego wyboru
│   │   │   └── QuickReminderAdapter.kt    # Adapter panelu
│   │   ├── viewmodel/
│   │   │   └── ReminderViewModel.kt       # ViewModel
│   │   ├── workers/
│   │   │   └── ReminderScheduler.kt       # Scheduler alarmów
│   │   ├── receivers/
│   │   │   ├── AlarmReceiver.kt           # Odbieranie alarmów
│   │   │   └── BootReceiver.kt            # Restart po reboot
│   │   └── notifications/
│   │       └── NotificationHelper.kt      # Zarządzanie powiadomieniami
│   ├── res/                               # Zasoby
│   │   ├── layout/                        # Layouty XML
│   │   ├── drawable/                      # Ikony
│   │   ├── values/                        # Stringi, kolory, style
│   │   └── mipmap/                        # Ikony launchera
│   └── AndroidManifest.xml                # Manifest aplikacji
└── build.gradle.kts                       # Konfiguracja Gradle
```

## Uprawnienia

Aplikacja wymaga następujących uprawnień:
- **POST_NOTIFICATIONS** - wysyłanie powiadomień (Android 13+)
- **SCHEDULE_EXACT_ALARM** - precyzyjne alarmy
- **RECEIVE_BOOT_COMPLETED** - restart po restarcie systemu
- **VIBRATE** - wibracje przy powiadomieniach

## Jak używać

1. **Dodanie przypomnienia**
   - Kliknij przycisk ➕ (FAB)
   - Wpisz tytuł i treść przypomnienia
   - Ustaw interwał suwakiem lub wpisz ręcznie
   - Kliknij "Zapisz"

2. **Szybki wybór**
   - Kliknij przycisk "Szybki wybór"
   - Wybierz jeden z gotowych szablonów
   - Przypomnienie zostanie automatycznie dodane

3. **Zarządzanie**
   - **Wyłącz/włącz** - przełącznik przy każdym przypomnieniu
   - **Usuń** - przycisk "Usuń" na karcie przypomnienia

## Funkcje zaawansowane

### Dokładne alarmy
Aplikacja używa `AlarmManager.setExactAndAllowWhileIdle()` dla precyzyjnych przypomnień, które działają nawet gdy telefon jest w trybie oszczędzania energii.

### Restart po restarcie systemu
`BootReceiver` automatycznie restartuje wszystkie aktywne przypomnienia po restarcie urządzenia.

### Tryb ciemny
Aplikacja automatycznie dostosowuje się do ustawień systemowych (light/dark mode).

## Możliwe rozszerzenia

Funkcje, które można dodać w przyszłości:
- 📝 Edycja istniejących przypomnień
- 📁 Kategoryzacja i grupowanie
- 📊 Statystyki i historia
- 🔄 Eksport/import danych
- 📱 Widget na ekran główny
- 🎨 Personalizacja kolorów i ikon
- 🔊 Własne dźwięki powiadomień

## Rozwiązywanie problemów

### Problem: Powiadomienia się nie wyświetlają
**Rozwiązanie:**
1. Sprawdź uprawnienia w ustawieniach telefonu
2. Wyłącz optymalizację baterii dla aplikacji
3. Zezwól na dokładne alarmy w ustawieniach systemu

### Problem: Przypomnienia przestają działać po restarcie
**Rozwiązanie:**
- Upewnij się, że aplikacja ma uprawnienie `RECEIVE_BOOT_COMPLETED`

### Problem: Błąd kompilacji w Android Studio
**Rozwiązanie:**
1. File → Invalidate Caches → Invalidate and Restart
2. Upewnij się, że używasz odpowiedniej wersji Gradle i SDK

## Licencja

Ten projekt został stworzony jako aplikacja edukacyjna/osobista.

## Autor

Projekt stworzony przy użyciu Claude Code.

## Kontakt

W razie pytań lub problemów, otwórz issue w repozytorium projektu.
