package ru.brikster.chatty.chat.message.transform.stage.early.moderation;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.api.chat.message.strategy.stage.EarlyMessageTransformStrategy;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.ModerationConfig;
import ru.brikster.chatty.config.type.ModerationConfig.CapsModerationConfig;

import javax.inject.Inject;

public final class CapsModerationMessageTransformStrategy implements EarlyMessageTransformStrategy {

    private final int percent;
    private final int length;
    private final boolean useBlock;

    @Inject private BukkitAudiences audiences;

    @Inject private MessagesConfig messages;
    @Inject private ModerationConfig moderationConfig;

    private CapsModerationMessageTransformStrategy() {
        CapsModerationConfig config = moderationConfig.getCaps();
        this.useBlock = config.isBlock();
        this.percent = config.getPercent();
        this.length = config.getLength();
    }

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        String message = context.getMessage();

        if (message.length() >= length
                && calculateUppercasePercent(message) >= percent) {
            message = message.toLowerCase();

            audiences.player(context.getSender()).sendMessage(messages.getCapsFound());

            if (useBlock) {
                return MessageTransformResultBuilder.<String>fromContext(context)
                        .withMessage(message)
                        .withCancelled()
                        .build();
            } else {
                return MessageTransformResultBuilder.<String>fromContext(context)
                        .withMessage(message)
                        .build();
            }
        }

        return MessageTransformResultBuilder.<String>fromContext(context).build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    private int calculateUppercasePercent(String message) {
        int totalLength = 0;
        int capsLength = 0;
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                totalLength++;
                if (c == Character.toUpperCase(c) && (Character.toLowerCase(c) != Character.toUpperCase(c))) {
                    capsLength++;
                }
            }
        }
        return (int) ((double) capsLength / (double) totalLength * 100);
    }

}