package com.gmail.justinxvopro.introbot.audio;

import static java.nio.ByteOrder.BIG_ENDIAN;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

public class StereoPcmAudioMixer {
	private final AudioDataFormat format = StandardAudioDataFormats.DISCORD_PCM_S16_BE;
	private final int[] mixBuffer = new int[format.chunkSampleCount * 2];
	private final byte[] outputBuffer = new byte[format.totalSampleCount() * 4];
	private final ShortBuffer wrappedOutput = ByteBuffer.wrap(outputBuffer).order(BIG_ENDIAN).asShortBuffer();;
	private final Multiplier previousMultiplier = new Multiplier();
	private final Multiplier currentMultiplier = new Multiplier();
	private byte[] onlyFrame = null;
	private boolean isEmpty = true;

	public void reset() {
		isEmpty = true;
		onlyFrame = null;
	}

	public void add(AudioFrame frame) {
		if (frame != null) {
			byte[] data = frame.getData();

			if (isEmpty) {
				isEmpty = false;
				onlyFrame = data;
			} else {
				if (onlyFrame != null) {
					ShortBuffer inputBuffer = ByteBuffer.wrap(onlyFrame).order(BIG_ENDIAN).asShortBuffer();

					for (int i = 0; i < mixBuffer.length; i++) {
						mixBuffer[i] = inputBuffer.get(i);
					}

					onlyFrame = null;
				}

				ShortBuffer inputBuffer = ByteBuffer.wrap(data).order(BIG_ENDIAN).asShortBuffer();

				for (int i = 0; i < mixBuffer.length; i++) {
					mixBuffer[i] += inputBuffer.get(i);
				}
			}
		}
	}

	public byte[] get() {
		if (isEmpty) {
			previousMultiplier.reset();
			return null;
		} else if (onlyFrame != null) {
			previousMultiplier.reset();
			return onlyFrame;
		}

		updateMultiplier();

		if (!currentMultiplier.identity || !previousMultiplier.identity) {
			for (int i = 0; i < 10; i++) {
				float gradientMultiplier = (currentMultiplier.value * i + previousMultiplier.value * (10 - i)) * 0.1f;
				wrappedOutput.put(i, (short) (gradientMultiplier * mixBuffer[i]));
			}

			for (int i = 10; i < mixBuffer.length; i++) {
				wrappedOutput.put(i, (short) (currentMultiplier.value * mixBuffer[i]));
			}

			previousMultiplier.identity = currentMultiplier.identity;
			previousMultiplier.value = currentMultiplier.value;
		} else {
			for (int i = 0; i < mixBuffer.length; i++) {
				wrappedOutput.put(i, (short) mixBuffer[i]);
			}
		}

		return outputBuffer;
	}

	private void updateMultiplier() {
		int peak = 0;

		if (!isEmpty) {
			for (int value : mixBuffer) {
				peak = Math.max(peak, Math.abs(value));
			}
		}

		if (peak > 32767) {
			currentMultiplier.identity = false;
			currentMultiplier.value = 32767.0f / peak;
		} else {
			currentMultiplier.identity = true;
			currentMultiplier.value = 1.0f;
		}
	}

	private static class Multiplier {
		private boolean identity = true;
		private float value = 1.0f;

		private void reset() {
			identity = true;
			value = 1.0f;
		}
	}
}
