package de.yard.threed.flightgear.testutil;

import de.yard.threed.flightgear.core.simgear.sound.SGSampleGroup;
import de.yard.threed.flightgear.core.simgear.sound.SGSoundSample;

import java.util.List;
import java.util.Map;

import static de.yard.threed.flightgear.core.flightgear.main.FGGlobals.globals;
import static org.junit.jupiter.api.Assertions.*;

public class SoundAssertions {

    public static void validateBasicSound(String vehicleSoundGroup) {

        Map<String, SGSampleGroup> sampleGroups = globals.sgSoundMgr.getSampleGroups();
        assertEquals(3, sampleGroups.size());
        assertEquals("atc", sampleGroups.get("atc")._refname);
        assertEquals("avionics", sampleGroups.get("avionics")._refname);
        assertEquals(vehicleSoundGroup, sampleGroups.get(vehicleSoundGroup)._refname);
    }

    public static void validateBluebirdSound(boolean shouldPlayRumble, int expectedPlayTrigger) {

        SGSoundSample rumbleSoundSample = globals.sgSoundMgr.getSampleGroups().get("bluebird-sound.xml")._samples.get("rumble");
        assertNotNull(rumbleSoundSample);
        if (shouldPlayRumble){
            assertTrue(rumbleSoundSample.is_playing());
            assertTrue(rumbleSoundSample.isEffectivelyPlaying());
            assertTrue(rumbleSoundSample.is_valid_source());

        }else {

            assertFalse(rumbleSoundSample.is_playing());
            assertFalse(rumbleSoundSample.isEffectivelyPlaying());
            assertFalse(rumbleSoundSample.is_valid_source());
        }
        assertEquals(expectedPlayTrigger, rumbleSoundSample.playedAudio);
    }

    public static void validateC172pSound(boolean shouldPlayEngine, int expectedPlayTrigger) {

        SGSoundSample engineSoundSample = globals.sgSoundMgr.getSampleGroups().get("c172-sound.xml")._samples.get("engine");
        assertNotNull(engineSoundSample);
        if (shouldPlayEngine){
            assertTrue(engineSoundSample.is_playing());
            assertTrue(engineSoundSample.isEffectivelyPlaying());
            assertTrue(engineSoundSample.is_valid_source());

        }else {

            assertFalse(engineSoundSample.is_playing());
            assertFalse(engineSoundSample.isEffectivelyPlaying());
            assertFalse(engineSoundSample.is_valid_source());
        }
        assertEquals(expectedPlayTrigger, engineSoundSample.playedAudio);
    }
}
