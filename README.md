# E-Voting System

An Android application for secure electronic voting, built with Kotlin and Firebase.

## Overview
Brief description — A mobile e-voting 
platform allowing verified voters to cast ballots in elections, with an admin 
panel for managing elections and candidates.

##  Tech Stack
- **Language:** Kotlin
- **Platform:** Android (min SDK __, target SDK __)
- **Backend:** Firebase Authentication, Cloud Firestore
  

##  Features
- User authentication (email/password + Google Sign-In)
- Voter verification by admin
- One-person-one-vote enforcement via Firestore transactions
- Real-time results
- Admin dashboard for managing elections/candidates
- [add/remove based on what you've actually built]

##  Security
- Firestore Security Rules enforce authentication, role-based access, and 
  prevent double voting or unauthorized privilege escalation
- Votes are cast via atomic transactions to prevent race conditions
- API key restricted to the app's signed package + SHA-1 fingerprint
  


##  Setup Instructions
1. Clone the repository
2. Open in Android Studio
3. Add your own `google-services.json` to the `app/` folder (get this from 
   Firebase Console → Project Settings → download for Android app), OR use 
   the one provided if sharing privately with the team
4. Sync Gradle and run

## 👥 Team
- 180413 RUTH NDUNGE 
- 189576 Manuel Githua
- 191646 Trevor Kimani
- 191178 Emmanuel Dixon
  
