# 🌍 AI Translator - Multilingual Android App 

## 📌 Project Overview
AI Translator is a real-time multilingual translation app for Android, allowing users to translate text, speech, and camera-captured text into multiple languages. The app utilizes OpenAI GPT, Camera OCR, and Text-to-Speech (TTS) for an interactive and fully localized translation experience. It adapts to the system language, provides live chat translation, and supports language-based sorting for a seamless user experience.

## 🎨 Figma Design
🔗 [Figma Prototype](https://www.figma.com/design/LuauTcANEWA5ANi4GgqvaM/Translator-App?node-id=0-1&t=VU6aw3y1WoJsYRAr-1)

## 📱 Features & Technologies Used
### Android & Jetpack Components
- **MVVM Architecture** – Clean architecture with ViewModel, LiveData, and Repository pattern
- **ConstraintLayout & XML-based UI** – Modern UI with responsiveness and dark mode support
- **Kotlin Coroutines & Flow** – Asynchronous programming for smooth UI updates

### 🧠 AI-Powered Translation & Voice Features
- **OpenAI GPT API (`gpt-4o-mini`)** – AI-powered text translation
- **Text-to-Speech (TTS)** – Reads translations aloud in the selected language
- **Speech Recognition** – Converts spoken words into text for real-time chat

### 📷 Camera OCR & Image Translation
- **Google ML OCR** – Extracts text from images for instant translation

### 🌍 Language & Localization
- **Dynamic Language Switching** – Matches the user's system language automatically
- **Sorted Language Selection** – Displays language names in the correct system locale
- **Over 10 Translated UI Languages** – Includes English, Spanish, French, German, Italian, Hindi, Japanese, Greek, Polish, Danish, Norwegian, and more!

### 🔗 Networking & API Integration
- **Retrofit** – Fetches translations from OpenAI’s API
- **Gson** – Parses JSON responses in Retrofit API calls

### 🔄 User Preferences & Data Handling
- **SharedPreferences** – Saves user language settings and preferences
- **Android Permissions** – Handles Microphone, Camera, and Internet for full app functionality

### 🎨 UI Enhancements
- **Lottie Animations** – Smooth, engaging UI interactions
- **Material Design** – Sleek, modern, and fully responsive across screen sizes

---

## 📌 Setup & Requirements

### 🔧 Prerequisites
- **Android Studio Giraffe+** (Latest version recommended)
- **Minimum SDK**: API 24 (Android 7.0, Nougat)

### 📜 Permissions Required:
- 🎤 **Microphone** – For voice input translation  
- 📷 **Camera** – For image-to-text OCR translation  
- 🌐 **Internet** – For OpenAI API calls  

## 📂 Installation Steps
#### 1️⃣ Clone the repository:
```sh
git clone https://github.com/jhueyy/ai-translator.git
```
#### 2️⃣ Open in Android Studio and let Gradle sync.
#### 3️⃣ Set up OpenAI API key:
- Locate **`SAMPLE-OpenAIService.kt`** in `/src/main/java/csc436/aitranslator/`
- Rename the file from **`SAMPLE-OpenAIService.kt`** to **`OpenAIService.kt`**
- Open the file and replace:
  ```kotlin
  "Authorization" to "Bearer KEY GOES HERE"
  ```
  with:
  ```kotlin
  "Authorization" to "Bearer sk-proj-..."
  ```
- **Never share your API key publicly**.
#### 4️⃣ Build and run the app on a real device or emulator.

### 📩 Contact
- **Jake Huey** – [jahuey@calpoly.edu](mailto:jahuey@calpoly.edu)  
- **Cristian Castro** – [ccastroo@calpoly.edu](mailto:ccastroo@calpoly.edu)  
