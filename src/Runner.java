import jouvieje.bass.BassInit;
import jouvieje.bass.structures.BASS_CHANNELINFO;

import java.util.List;
import java.util.Scanner;

import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_MP3;

public class Runner {
    public static void run() {
        BassInit.loadLibraries();
        System.out.println(BassInit.BASSVERSION());

        List<DeviceItem> deviceList = DeviceManager.getDevices(0);
        for (DeviceItem deviceItem : deviceList) {
            System.out.println(deviceItem.device + " - " + deviceItem.name);
        }

        final Scanner scanner = new Scanner(System.in);
        System.out.print("Select an audio device >>> ");
        int deviceId = scanner.nextInt();

        final int deviceInitFlags = 0;
        if(!DeviceManager.setupDevice(deviceId, deviceInitFlags)) {
            System.out.println("Failed to setup device. TERMINATING");
            return;
        }

        final MP3Player mp3Player = new MP3Player();
        if(!mp3Player.createStream("res/audio.mp3")) {
            DeviceManager.closeDevice();
            System.out.println("Failed to open stream");
        }
        mp3Player.applyRate(1.5f, true);
        BASS_CHANNELINFO channelInfo = mp3Player.getInfo();
        if(channelInfo != null) {
            final String isMP3 = (channelInfo.getChannelType() == BASS_CTYPE_STREAM_MP3) ? "TRUE" : "FALSE";
            System.out.println("Filename : " + channelInfo.getFilename());
            System.out.println("Frequency: " + channelInfo.getFreq());
            System.out.println("isMP3    : " + isMP3);
            System.out.println("Length   : " + mp3Player.getLength());
            System.out.println("Frequency: " + mp3Player.getFrequency());
            channelInfo.release();

            System.out.println("Volume: " + mp3Player.getVolume());
            mp3Player.setVolume(0.05f);
            System.out.println("Volume: " + mp3Player.getVolume());

            System.out.println(mp3Player.play());

            System.out.println("Press 1 and then Enter to stop music");
            scanner.next();
            scanner.nextLine();

            mp3Player.stop();
        }
        mp3Player.freeStream();

        System.out.println("--- END ---");
    }
}
