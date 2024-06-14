Grave Locator Mobile Application

Table of Contents
1. Introduction
2. Features
3. Modules
4. Installation
5. Usage
6. Development
7. Acknowledgements

1.Introduction
- The Grave Locator Mobile Application is designed to address the challenges of locating graves in cemeteries. It leverages augmented reality (AR) technology to provide users with a precise and efficient way to find graves, enhancing the experience of visiting cemeteries and reducing emotional stress.

2.Features
- Registration and Login: Secure user authentication system.
- Manage Deceased Data: Allows users and administrators to add, update, and manage data related to the deceased.
- Augmented Reality Navigation: Guides users to the exact location of graves using AR technology.
- User Profiles: Manage user information and preferences.
- Feedback System: Users can provide feedback on the accuracy and functionality of grave locations.

3.Modules
The application consists of two main modules:

Admin Module:
- Manage deceased data.
- Approve or reject data submissions.
- Oversee user activity and feedback.

Client Module:
- Register and log in to the application.
- Enter and search for deceased data.
- Use AR navigation to locate graves.
- Manage user profile and provide feedback.

4.Installation
Prerequisites
- Android device that supports ARCore (for navigation purposes)
- Internet connection
- Firebase project set up with Authentication, Realtime Database, and Storage
- Compatible IDE such as Android Studio

Steps
	1.Clone the Repository:
	- git clone https://github.com/AimanTahrim/grave-locator-mobile-application.git

	2.Navigate to the Project Directory:
	- cd grave-locator-mobile-application

	3.Open the Project in Android Studio:
	- Launch Android Studio.
	- Select "Open an existing Android Studio project".
	- Navigate to the grave-locator-mobile-application directory and open it.

	4.Configure Firebase:
	- Set up Firebase Authentication, Realtime Database, and Storage in your Firebase console.
	- Download the google-services.json file from your Firebase project and place it in the app directory of your project.

	5.Install Dependencies:
	- Android Studio will automatically install the required dependencies.

	6.Run the Application:
	- Connect your Android device to your computer.
	- Click the "Run" button in Android Studio to build and deploy the app to your device.

5.Usage
Open the Application: Launch the app on your mobile device.
Register/Login: Create an account or log in if you already have one.
Add Deceased Data: Enter details of the deceased to the database.
Locate Grave: Use the AR navigation feature to find the grave.
Update Profile: Manage your account settings and preferences.
Provide Feedback: Share your experience and suggest improvements.

6.Development
Tools and Technologies
Programming Languages: Kotlin, Java, and XML
Database: Google Firebase
Augmented Reality SDK: ARCore
APIs: Google Maps API
Architecture: Model-View-Controller (MVC)

7.Acknowledgements
I would like to extend my heartfelt appreciation to my supervisor, Dr. Nur Liyana Binti Sulaiman, for her guidance and support throughout this project. I am also grateful to my family, friends, and colleagues for their unwavering support and encouragement.