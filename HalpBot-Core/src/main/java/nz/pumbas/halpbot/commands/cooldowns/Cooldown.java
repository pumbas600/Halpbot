package nz.pumbas.halpbot.commands.cooldowns;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class Cooldown
{
    private final long startTimeMs;
    private final long durationMs;

    public Cooldown(long duration, TimeUnit timeUnit) {
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }

    public boolean hasFinished() {
        return System.currentTimeMillis() - this.startTimeMs > this.durationMs;
    }

    public long getRemainingTime() {
        return this.durationMs - (System.currentTimeMillis() - this.startTimeMs);
    }

    public MessageEmbed getRemainingTimeEmbed() {
        double remainingTimeSeconds = this.getRemainingTime() / 1000D;

        return new EmbedBuilder()
            .setTitle("Please wait, you're on cooldown")
            .setDescription(String.format("%.2fs Remaining", remainingTimeSeconds))
            .setColor(Color.BLUE)
            .build();
    }
}