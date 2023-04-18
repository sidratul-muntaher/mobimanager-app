package com.iis.mobimanager2.utils;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.iis.mobimanager2.activity.MainActivity;

public class DeviceAdminHelper extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context,"Admin Enabled",Toast.LENGTH_SHORT).show();
    }
    /**
     * Called on the new profile when managed profile provisioning has completed. Managed profile
     * provisioning is the process of setting up the device so that it has a separate profile which
     * is managed by the mobile device management(mdm) application that triggered the provisioning.
     * Note that the managed profile is not fully visible until it is enabled.
     */
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // EnableProfileActivity is launched with the newly set up profile.
        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }

    /**
     * Generates a {@link ComponentName} that is used throughout the app.
     * @return a {@link ComponentName}
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(),
                DeviceAdminHelper.class);
    }
}
