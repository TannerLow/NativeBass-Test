import jouvieje.bass.structures.BASS_DEVICEINFO;

import java.util.ArrayList;
import java.util.List;

import static jouvieje.bass.Bass.BASS_Free;
import static jouvieje.bass.Bass.BASS_GetDeviceInfo;
import static jouvieje.bass.Bass.BASS_Init;
import static jouvieje.bass.Bass.BASS_SetDevice;

public class DeviceManager {
    public static List<DeviceItem> getDevices(int initFlags) {
        List<DeviceItem> devices = new ArrayList<>();
        BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
        for(int c = 1; BASS_GetDeviceInfo(c, info); c++) {
            String text = info.getName();

            //device 1 = 1st real device
            /* Check if the device supports 3D */
            if(!BASS_Init(c, 44100, initFlags, null, null)) {
                continue;	// could not initialize device
            }
            closeDevice();

            devices.add(new DeviceItem(text, c));
        }
        info.release();

        return devices;
    }

    public static boolean setupDevice(int deviceId, int initFlags) {
        if(!BASS_Init(deviceId, 44100, initFlags, null, null)) {
            return false; // unable to initialize device
        }

        if(!BASS_SetDevice(deviceId)) {
            closeDevice();
            return false; // unable to set device
        }

        return true;
    }

    public static void closeDevice() {
        BASS_Free();
    }
}
