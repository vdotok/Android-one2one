vdotok-Android-One2OneCall
===================
IDE Installation & Build Guide
==============================
Requirements
* Android Studio 4.1.2 or later (Stable Version)
* you can follow the link to download and install Android Stuiod
    * “https://developer.android.com/studio?gclid=Cj0KCQjwhr2FBhDbARIsACjwLo2fEHdB3l3eqRlhIvySYNx1-3XjDmuX1eSCbaCI7zU8FKHFkGBcVyMaAtSjEALw_wcB&gclsrc=aw.ds#downloads”
* Android SDKs 21(Lollipop) or later
    * For downloading Android SDKs
    * Open Android Studio
    * Goto file menu Tools and select “<b>SDK Manager</b>”
    * Checkmark api level 21 and onward 
    * click apply and download respective packages

Project Signup and Project ID
* Follow the link below register your self for chat server and get the project Id
* https://www.kuchtohoga.com/norgic/callSDK/

To Download Chat Lib
* Follow the link below and download “<b>norgic-callsdk-v1.0.0.aar</b>” file
* https://sdk.vdotok.com/Android-SDKs/

Code setup
* Open Android Studio
* Click on “<b>Get from Version Control</b>” or if you dont see the welcome page then go to file new and select "<b>Project From Version Control</b>"
* Select “<b>Repository URL</b>” from left menu
* Select “<b>Git</b>” from Version control dropdown menu
* Paste this URL "<b>https://github.com/vdotok/Android-one2one.git</b>" in URL section
* click on clone button and wait for build .gradle files you can see the progress on bottom of android studio
* let the android studio install the components, 
* Hurrraaaa you Just configure the project in android studio

Device Setting
* In order to connect you device with android studio you need to enable developer mode
* For enabling developer mode and usb debug you may follow the device specific steps
* you can follow the step described in link below to enable developer options and usb debugging
* https://developer.android.com/studio/debug/dev-options

Configure Lib
* From Android Studio file menu click on File->New->New Module-> Import .JAR/.AAR Package and click on next
* Select downloaded “<b>norgic-callsdk-v1.0.0.aar</b>” .AAR file and click Finish
* From Android Studio File Explorer  select project 
<img width="498" alt="123636395-41e08980-d836-11eb-8643-429d6e5510d5" src="https://user-images.githubusercontent.com/86282129/123811571-cb628b00-d90c-11eb-9584-b5a8f12957dc.png">

* And then go to vdotok-call -> app -> src -> build.gradle and past following line in Dependencies  section
* “<b>implementation project(path: ':norgic-callsdk-v1.0.0')</b>”

<img width="798" alt="Screenshot 2021-06-29 at 6 59 02 PM" src="https://user-images.githubusercontent.com/86282129/123811034-542cf700-d90c-11eb-8294-04a1a9d858c5.png">

* Click on “<b>sync now</b>” Button from top right corner
* From File explorer open chattApp -> app -> src -> main -> java -> com -> norgic -> vdotokchat -> utils -> ApplicationConstants replace “<b>SDK_PROJECT_ID</b>” with your own Project Id 

Build Project 
* Connect your phone with system in file sharing mode
* You can find your phone name in running devices list like describe in below image
* select your device and click on Play button
* After running some automated commands and building gradle your app will install on you connected device
<img width="1012" alt="Screenshot 2021-06-29 at 6 59 36 PM" src="https://user-images.githubusercontent.com/86282129/123811062-5bec9b80-d90c-11eb-96e1-ee50dee125c5.png">

