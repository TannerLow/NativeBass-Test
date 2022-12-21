import jouvieje.bass.BassInit;
import jouvieje.bass.structures.HSTREAM;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Scanner;

import static jouvieje.bass.Bass.BASS_ChannelPlay;
import static jouvieje.bass.Bass.BASS_ChannelStop;
import static jouvieje.bass.Bass.BASS_ErrorGetCode;
import static jouvieje.bass.Bass.BASS_FX_GetVersion;
import static jouvieje.bass.Bass.BASS_FX_TempoCreate;
import static jouvieje.bass.Bass.BASS_Free;
import static jouvieje.bass.Bass.BASS_GetVersion;
import static jouvieje.bass.Bass.BASS_Init;
import static jouvieje.bass.Bass.BASS_SetDevice;
import static jouvieje.bass.Bass.BASS_StreamCreateFile;
import static jouvieje.bass.Bass.BASS_StreamFree;
import static jouvieje.bass.defines.BASS_FX.BASS_FX_FREESOURCE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_DECODE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_PRESCAN;
import static org.jouvieje.libloader.LibLoader.getPlatform;
import static org.jouvieje.libloader.LibLoader.isPlatform64Bits;

public class Main {

    private static String getProperty(final String prop) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() { public Object run() {
            return System.getProperty(prop);
        }});
    }

    public static void main(String[] args) {
        System.out.println(getProperty("java.library.path"));

        System.out.println(getPlatform());
        System.out.println(isPlatform64Bits());
        BassInit.loadLibraries();
        System.out.println("BASS FX loaded: " + BassInit.isPluginFxLoaded());
        System.out.println(BassInit.BASSVERSION());
        System.out.println("Bass    version: " + String.format("0x%08X", BASS_GetVersion()));
        System.out.println("Bass FX version: " + String.format("0x%08X", BASS_FX_GetVersion()));

        Runner.run();
    }
}