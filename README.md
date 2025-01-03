# Device Security Checker (Android)

This Android application performs an on-device security check by analyzing installed apps, their requested permissions, and the device's security state. It aims to provide users with insights into potential vulnerabilities or suspicious activity on their devices.

## Features

* **On-Device Analysis:** Performs security checks locally without requiring network connectivity for core functionality.
* **Risky Permission Detection:** Identifies installed apps that request permissions considered potentially risky (e.g., camera, microphone, location, storage).
* **Root Access Detection:** Detects if the device is rooted by checking for common root indicators and the presence of the `su` binary.
* **Debugger Detection:** Alerts the user if a debugger is connected to the device.
* **Running Process Monitoring:** Lists currently running processes to help identify suspicious activity.
* **Clear Reporting:** Provides detailed results, including flagged apps with their permissions, detected root access, debugger connections, and running processes.
* **System App Filtering:** Excludes system apps from the analysis to focus on user-installed applications.

## How it Works

1. **Permission Scan:** The app iterates through installed user applications and flags those requesting risky permissions.
2. **Root Detection:** Checks for common indicators of root access, such as the presence of the `su` binary or root-only directories.
3. **Debugger Check:** Monitors whether the app is being debugged, which could indicate tampering or surveillance.
4. **Process Monitoring:** Retrieves and lists active processes on the device for user review.

## Permissions

The app requests the following permissions:

* `PACKAGE_USAGE_STATS`: Required to access the list of installed applications on the device. Users must manually grant this permission through system settings.
* Other permissions: Depending on the features, additional permissions such as `READ_EXTERNAL_STORAGE` may be required.

## Building and Running

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
    ```
2. **Open in Android Studio:** Open the project in Android Studio.
3. **Build the APK:** Go to *Build* > *Build Bundle(s)/APK(s)* > *Build APK(s)*.
4. **Install on Device:** Transfer the generated APK file to your Android device and install it.

## Code Structure

* `MainActivity.java`: Contains the main application logic, including permission handling, security checks, and reporting.
* `activity_main.xml`: Defines the user interface layout.

## Potentially Risky Permissions

The following permissions are currently considered risky by the app:

* `android.permission.CAMERA`
* `android.permission.RECORD_AUDIO`
* `android.permission.ACCESS_FINE_LOCATION`
* `android.permission.READ_CONTACTS`
* `android.permission.SEND_SMS`
* `android.permission.READ_EXTERNAL_STORAGE`
* `android.permission.WRITE_EXTERNAL_STORAGE`

This list can be easily updated by modifying the `RISKY_PERMISSIONS` set in `MainActivity.java`.

## Limitations

* **Basic Analysis:** The app performs basic checks and does not include malware scanning or advanced threat analysis.
* **Permission Context:** The app does not evaluate the context in which permissions are used, which may affect its assessment.
* **No Real-Time Monitoring:** The app performs a one-time scan and does not provide ongoing monitoring.

## Future Enhancements

* **File System Analysis:** Add checks for suspicious files and directories.
* **Device Settings Check:** Identify insecure settings (e.g., developer mode, unknown sources enabled).
* **Enhanced Reporting:** Improve the user interface and provide more detailed explanations of detected issues.
* **Network Traffic Analysis:** Monitor network activity for potential data leaks.

## Disclaimer

This app is intended for informational purposes only. It does not guarantee the complete security of your device. Use it as a tool to gain insights into your device's security posture and take appropriate precautions. The developers are not responsible for any damages or losses resulting from the use of this app.
```
