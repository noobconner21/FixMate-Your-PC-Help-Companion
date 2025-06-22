
# 💻 FixMate: Your PC Help Companion

Struggling with PC or laptop problems? **FixMate** is here to help. We provide a reliable, easy-to-use platform to diagnose and fix common computer issues, connect with a supportive community, and find local repair services.  
Our goal is to empower users to solve technical problems confidently and avoid costly repairs.

---

## 🌟 Features

FixMate offers a comprehensive set of features:

### 🔐 User Authentication
- Secure sign-up and login using **Firebase Authentication**.

### 📰 Global Post Feed
- Browse posts from all users.
- **Image Carousel**: Horizontally swipable `ViewPager2` with rounded corners.
- **Read More / Less**: Toggle long post descriptions.
- Displays **comment and react counts**.

### 🔎 Post Search
- Search posts by **title**, **description**, or **author name**.

### 🔄 Pull-to-Refresh
- Swipe to refresh the feed for new content.

### 📝 User's Own Posts
- View, edit, and delete your own posts.

### ➕ Post Creation
- Create posts with **titles**, **descriptions**, and up to **three images**.

### 💬 Comment System
- View and add comments in a dedicated dialog for each post.

### 🔔 Notifications
- Get notified when someone comments on your post.

### 👤 User Profile Management
- Edit profile details (name, picture).
- View app info and link to developer’s GitHub.
- Secure logout.

### ⏳ Custom Loading Dialogs
- Clear feedback during network operations.

### 🕒 Robust Date Formatting
- Timestamps displayed in a human-readable format.

### 📍 Location Services *(If Implemented)*
- Map to find nearby PC repair shops (requires `MapFragment`).

---

## 🛠️ Technologies Used

### Front-end
- Android Studio
- Java
- XML for UI
- **AndroidX Libraries**: AppCompat, Material Components, RecyclerView, ViewPager2, SwipeRefreshLayout, Biometric, Lifecycle

### Back-end
- *(Specify your backend, e.g., Node.js with Express)*

### Database
- *(Specify your database, e.g., Firebase Firestore, MongoDB, etc.)*

### APIs & Libraries
- **Retrofit2** – Type-safe HTTP client
- **Glide** – Efficient image loading
- **Firebase Authentication**
- **Google Maps SDK** *(If implemented)*
- **Google Play Services Location & Places API** *(If implemented)*
- **CircleImageView** – For circular image displays

---

## 🚀 Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/noobconner21/FixMate-Your-PC-Help-Companion
   cd FixMate
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **"Open an existing project"**
   - Navigate to the cloned `FixMate` directory

3. **SDK Setup**
   - Install **Android SDK Platform 34** (API Level 34)
   - Ensure `minSdkVersion = 23`, `targetSdkVersion = 34` in `app/build.gradle.kts`

4. **Gradle Sync**
   - Allow Android Studio to sync Gradle and download dependencies

5. **Firebase Configuration**
   - Add your `google-services.json` file to the `app/` directory
   - Configure Firebase in the [Firebase Console](https://console.firebase.google.com/)

6. **Google Maps API Key** *(If applicable)*
   - Add your key in `AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_API_KEY_HERE"/>
     ```

7. **Run the Application**
   - Connect a USB-debugging enabled device or use an emulator
   - Click ▶️ **Run 'app'**

---

## 📱 Usage

1. **Launch the App** – Splash screen appears on start.

2. **Authentication**
   - **Register** – Tap *"Click To Register"*
   - **Login** – Tap *"Click To Login"*

3. **Main Navigation**
   - `Home` – Global feed, image carousels, comments
   - `Posts` – Your own posts (edit/delete)
   - `Add` – Create new post
   - `Notification` – View activity notifications
   - `Map` – *(If implemented)* PC repair shop map
   - `Profile` – Tap your profile pic or icon to:
     - Edit profile
     - View About
     - Visit GitHub
     - Logout

---

## 🤝 Contributing

Contributions are welcome!  
Feel free to open an issue or pull request in the [GitHub repository]([https://github.com/noobconner21](https://github.com/noobconner21/FixMate-Your-PC-Help-Companion)).

---

## 📄 License

This project is licensed under the **MIT License**.  
*(You may replace this with "All Rights Reserved" or another license if needed.)*

---

## 📧 Contact

**Developer:** Lakshan  
**GitHub:** [SSLABLK](https://github.com/noobconner21)
