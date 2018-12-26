# Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances

##### Note - Although this app has been tested on multiple android devices and is working fine, changes are still being made to this app as it is further refined.


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
6. An additional facility is provided to the Riders to call the Drivers, they just made the request to.
7. If the Driver accepts the request of the Rider, he is taken to the Navigation Mode, which shows the Driver on the Map, the shortest path to the Rider, the distance across this path and the expected time to cover this distance.

### Screenshots of the App:

##### Common to both Riders and Drivers:

1. Signing in using Phone Number:

![Signing in using Phone Number](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/screenshots/Both/IMG_20181225_190019.jpg)

2. OTP Verification:

3. Driver/Rider profile selection option on first time account creation:

![Driver/Rider profile selection option on first time account creation](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/screenshots/Rider/Screenshot_2018-12-21-19-42-59-761_com.project.sih.ambulancebookingapplication.png)

##### Rider:

2. Make a request to some Ambulance:

![Make a request to some Ambulance](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/screenshots/Rider/IMG_20181225_185829.jpg)

3. Driver accepting the request notification:

![Driver accepting the request notification](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/screenshots/Rider/IMG_20181225_185643.jpg)

4. Call the Ambulance Driver:

![Call the Ambulance Driver](https://github.com/dmast3r/Ambulance-Booking-Application-a-Taxi-Booking-App-or-an-Ola-Uber-Clone-for-Ambulances/blob/master/screenshots/Rider/IMG_20181225_185320.jpg)

### How to import this project:

### Changes to be made to convert the app into a generic Taxi-Booking-App
