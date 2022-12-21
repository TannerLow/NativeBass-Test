import jouvieje.bass.structures.BASS_CHANNELINFO;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.BufferUtils;

import java.nio.FloatBuffer;

import static jouvieje.bass.Bass.BASS_ChannelBytes2Seconds;
import static jouvieje.bass.Bass.BASS_ChannelGetAttribute;
import static jouvieje.bass.Bass.BASS_ChannelGetInfo;
import static jouvieje.bass.Bass.BASS_ChannelGetLength;
import static jouvieje.bass.Bass.BASS_ChannelPlay;
import static jouvieje.bass.Bass.BASS_ChannelSetAttribute;
import static jouvieje.bass.Bass.BASS_ChannelStop;
import static jouvieje.bass.Bass.BASS_ErrorGetCode;
import static jouvieje.bass.Bass.BASS_FX_TempoCreate;
import static jouvieje.bass.Bass.BASS_GetDevice;
import static jouvieje.bass.Bass.BASS_StreamCreateFile;
import static jouvieje.bass.Bass.BASS_StreamFree;
import static jouvieje.bass.defines.BASS_ATTRIB.BASS_ATTRIB_VOL;
import static jouvieje.bass.defines.BASS_FX.BASS_FX_FREESOURCE;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_DECODE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_PRESCAN;
import static jouvieje.bass.enumerations.BASS_ATTRIB.BASS_ATTRIB_TEMPO;
import static jouvieje.bass.enumerations.BASS_ATTRIB.BASS_ATTRIB_TEMPO_FREQ;
import static jouvieje.bass.enumerations.BASS_ATTRIB_TEMPO_OPTION.BASS_ATTRIB_TEMPO_OPTION_OVERLAP_MS;
import static jouvieje.bass.enumerations.BASS_ATTRIB_TEMPO_OPTION.BASS_ATTRIB_TEMPO_OPTION_SEQUENCE_MS;
import static jouvieje.bass.enumerations.BASS_ATTRIB_TEMPO_OPTION.BASS_ATTRIB_TEMPO_OPTION_USE_QUICKALGO;

public class MP3Player {

    private HSTREAM stream = null;
    private boolean isPlaying = false;

    private double length = -1;
    private int frequency = -1;

    public boolean createStream(final String filename) {
        if(BASS_GetDevice() == -1) {
            return false; // device not initialized
        }

        int flags = BASS_STREAM_PRESCAN | BASS_STREAM_DECODE;
        stream = BASS_StreamCreateFile(false, filename, 0, 0, flags);

        if(stream != null) {
            length = _getLength() * 1000;
            frequency = _getFrequency();

            // https://github.com/Quaver/Wobble/blob/833db7546afb603a835a9b7eecefec2290da0d0a/Wobble/Audio/Tracks/AudioTrack.cs#L403
            stream = BASS_FX_TempoCreate(stream.asInt(), BASS_FX_FREESOURCE);
            System.out.println("tempo_stream_create: " + BASS_ErrorGetCode());

            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO_OPTION_USE_QUICKALGO.asInt(), 1);
            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO_OPTION_OVERLAP_MS.asInt(), 4);
            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO_OPTION_SEQUENCE_MS.asInt(), 30);
        }

        return stream != null;
    }

    public void freeStream() {
        if(stream != null) {
            BASS_StreamFree(stream);
            stream = null;
            length = -1;
            frequency = -1;
        }
    }

    public BASS_CHANNELINFO getInfo() {
        BASS_CHANNELINFO channelInfo = null;

        if(stream != null) {
            channelInfo = BASS_CHANNELINFO.allocate();
            if(!BASS_ChannelGetInfo(stream.asInt(), channelInfo)) {
                System.out.println("get_info_err: " + BASS_ErrorGetCode());
                channelInfo.release();
                channelInfo = null;
            }
        }

        return channelInfo;
    }

    public double _getLength() {
        double length = -1;

        if(stream != null) {
            long bytes = BASS_ChannelGetLength(stream.asInt(), BASS_POS_BYTE);
            System.out.println("get_length_err: " + BASS_ErrorGetCode());
            if(bytes != -1) {
                length = BASS_ChannelBytes2Seconds(stream.asInt(), bytes);
                System.out.println("byte_2_seconds_err: " + BASS_ErrorGetCode());
            }
        }

        return length;
    }

    public double getLength() {
        return length;
    }

    public int _getFrequency() {
        int frequency = -1;

        if(stream != null) {
            BASS_CHANNELINFO channelInfo = BASS_CHANNELINFO.allocate();
            BASS_ChannelGetInfo(stream.asInt(), channelInfo);
            System.out.println("get_freq_err: " + BASS_ErrorGetCode());
            frequency = channelInfo.getFreq();
            channelInfo.release();
        }

        return frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean play() {
        if(stream != null) {
            System.out.println("pre_play_err: " + BASS_ErrorGetCode());
            isPlaying = BASS_ChannelPlay(stream.asInt(), false);
            System.out.println("play_err: " + BASS_ErrorGetCode());
        }
        return isPlaying;
    }

    public void stop() {
        if(isPlaying && stream != null) {
            BASS_ChannelStop(stream.asInt());
        }
        isPlaying = false;
    }

    public float getVolume() {
        FloatBuffer volume = BufferUtils.newFloatBuffer(1);
        if(!BASS_ChannelGetAttribute(stream.asInt(), BASS_ATTRIB_VOL, volume)) {
            System.out.println("get_volume_err: " + BASS_ErrorGetCode());
            return -1.f;
        }
        return volume.get();
    }

    public boolean setVolume(float volume) {
        if(volume > 1.f) volume = 1.f;

        boolean isSuccessful = false;
        if(stream != null) {
            isSuccessful = BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_VOL, volume);
            System.out.println("set_volume_err: " + BASS_ErrorGetCode());
        }
        return isSuccessful;
    }

    public boolean applyRate(float rate, boolean adjustPitch) {
        if (!adjustPitch)
        {
            // When pitching is enabled, adjust rate using frequency.
            return
            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO.asInt(), 0) &&
            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO_FREQ.asInt(), frequency * rate);
        }
        else
        {
            // When pitching is disabled, adjust rate using tempo.
            return
            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO_FREQ.asInt(), frequency) &&
            BASS_ChannelSetAttribute(stream.asInt(), BASS_ATTRIB_TEMPO.asInt(), rate * 100 - 100);
        }
    }
}
