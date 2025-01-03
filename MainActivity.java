import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView statusTextView;
    private static final String TAG = "DeviceSecurityCheck";

    // Define a set of "risky" permissions
    private static final Set<String> RISKY_PERMISSIONS = new HashSet<>();

    static {
        RISKY_PERMISSIONS.add(Manifest.permission.CAMERA);
        RISKY_PERMISSIONS.add(Manifest.permission.RECORD_AUDIO);
        RISKY_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        RISKY_PERMISSIONS.add(Manifest.permission.READ_CONTACTS);
        RISKY_PERMISSIONS.add(Manifest.permission.SEND_SMS);
        RISKY_PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        RISKY_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Add more as needed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = findViewById(R.id.scanButton);
        statusTextView = findViewById(R.id.statusTextView);

        scanButton.setOnClickListener(view -> startSecurityScan());

        requestPermissions();
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.PACKAGE_USAGE_STATS);
        }
        for (String riskyPermission : RISKY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, riskyPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(riskyPermission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    private void startSecurityScan() {
        statusTextView.setText("Scanning device...");
        Log.d(TAG, "Starting scan...");

        boolean isRooted = checkRootAccess();
        boolean isDebuggerConnected = checkDebugger();
        Map<String, List<String>> riskyAppsWithPermissions = checkInstalledApps();
        List<String> runningProcesses = getRunningProcesses();

        StringBuilder result = new StringBuilder();

        if (isRooted) {
            result.append("\nWarning: Device appears to be rooted!");
        }

        if (isDebuggerConnected) {
            result.append("\nWarning: Debugger detected!");
        }

        if (!riskyAppsWithPermissions.isEmpty()) {
            result.append("\nFound apps with potentially risky permissions:\n");
            for (Map.Entry<String, List<String>> entry : riskyAppsWithPermissions.entrySet()) {
                result.append("\nApp: ").append(entry.getKey()).append("\nPermissions:").append(entry.getValue().toString()).append("\n");
            }
        }

        if (!runningProcesses.isEmpty()) {
            result.append("\nCurrently running processes:\n");
            for (String process : runningProcesses) {
                result.append("\n- ").append(process);
            }
        }

        if (result.length() == 0) {
            result.append("No immediate security issues detected.");
        }

        statusTextView.setText(result.toString());
    }

    private boolean checkRootAccess() {
        String[] paths = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                Log.w(TAG, "Root access detected: " + path);
                return true;
            }
        }

        try {
            Process process = Runtime.getRuntime().exec("which su");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean checkDebugger() {
        return android.os.Debug.isDebuggerConnected();
    }

    private Map<String, List<String>> checkInstalledApps() {
        Map<String, List<String>> riskyApps = new HashMap<>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo packageInfo : packages) {
            List<String> appRiskyPermissions = new ArrayList<>();
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    if (RISKY_PERMISSIONS.contains(permission)) {
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // Filter out system apps
                                appRiskyPermissions.add(permission);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!appRiskyPermissions.isEmpty()) {
                    riskyApps.put(packageInfo.applicationInfo.loadLabel(pm).toString(), appRiskyPermissions);
                }
            }
        }
        return riskyApps;
    }

    private List<String> getRunningProcesses() {
        List<String> runningProcesses = new ArrayList<>();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                runningProcesses.add(info.processName);
            }
        }
        return runningProcesses;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions are required for full functionality", Toast.LENGTH_SHORT).show();
                    return; // Exit if any permission is denied
                }
            }
        }
    }
}
