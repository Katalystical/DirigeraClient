package de.dvdgeisler.iot.dirigera.client.api.model.device.speaker;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.dvdgeisler.iot.dirigera.client.api.model.device.DeviceStateAttributes;
import de.dvdgeisler.iot.dirigera.client.api.model.music.MusicPlaybackState;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakerStateAttributes extends DeviceStateAttributes {
  public MusicPlaybackState playback;
  public LocalDateTime playbackLastChangedTimestamp;
  public SpeakerAudioAttributes playbackAudio;
  public SpeakerPlaybackPosition playbackPosition;
  public Integer volume;
  public Boolean isMuted;
}
