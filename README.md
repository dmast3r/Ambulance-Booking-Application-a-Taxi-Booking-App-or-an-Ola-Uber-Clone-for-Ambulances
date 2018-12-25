# Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances

##### Note - Although this app has been tested on multiple android devices and is working fine, changes are still being made to this app as it is further refined. Make a pull request to contribute or [Download the APK from here](https://drive.google.com/file/d/1v5nG8eZZLXAVLXuEjwbvRaTuPRCeIDBH/view?usp=sharing).


### Index
1. [Features](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/README.md#features).
2. [Screenshots of the App](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/README.md#screenshots-of-the-app).
3. [How to import this project](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/README.md#how-to-import-this-project).
4. [Changes to be made to convert the app into a generic Taxi-Booking-App](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/README.md#changes-to-be-made-to-conver-the-app-into-a-generic-taxi-booking-app).

### Features:
1. Account creation for Riders and Drivers using Phone Number.
2. OTP verification on login using Firebase UI Phone Auth.
3. Uploading of images of Ambulance Registration Certificate and Driver's License on Cloud, for the authorities to verify the authenticity of Ambulance Service. The App uses Firebase Cloud Store for this purpose.
4. Firebase Realtime Database provides the facility to store and fetch Riders and Drivers information. For the Riders, it provides Real-Time location of all the available ambulances and their details(such as, if it has Life Support System or not).
5. The booking request of a Rider is forwarded to the respective Driver using Firebase Cloud Messaging, a push notification is generated on the Driver's side informing him of the request. A similar push notification is generated on the Rider's side when the Driver accepts or rejects the request.
6. An additional facility is provided to the Riders to call the Drivers, they just made a request to.

### Screenshots of the App:

##### Common to both Riders and Drivers:

1. Signing in using Phone Number:
![Signing in using Phone Number](https://drive.google.com/file/d/1TPxVzjfUTyz91_Ew1KIvMt9mPVs03rJT/view?usp=sharing)

### How to import this project:

### Changes to be made to convert the app into a generic Taxi-Booking-App
