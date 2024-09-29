package nl.cfgroningen.command;

import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.interaction.ModalInteraction;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import nl.cfgroningen.bot.KattisBot;

public class LinkCommand extends GenericCommand {
    public LinkCommand(KattisBot bot) {
        super(bot);
    }

    @Override
    public SlashCommandBuilder getCommandDefinition() {
        return SlashCommand.with("link", "Link your Kattis account to your Discord account!");
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public void register() {
        this.bot.getClient().addModalSubmitListener(event -> {
            ModalInteraction interaction = event.getModalInteraction();
            if (interaction.getCustomId().equals("kattisLinkAccount")) {
                String kattisUrl = interaction.getTextInputValueByCustomId("kattisUrl").orElse("");

                if (!kattisUrl.startsWith("https://open.kattis.com/users/") && !kattisUrl.startsWith("/users/")) {
                    interaction.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .addEmbed(this.error("Invalid URL",
                                    "The URL you provided is not a valid Kattis profile URL!"))
                            .respond();
                    return;
                }

                if (kattisUrl.startsWith("/users/")) {
                    kattisUrl = "https://open.kattis.com" + kattisUrl;
                }

                kattisUrl = kattisUrl.replace("com//users", "com/users");

                interaction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .addEmbed(this.success("Linking complete!", "Your Kattis account has been linked!"))
                        .respond();

                bot.getData().getCachedData().getDiscordIdToKattisProfileUrl().put(interaction.getUser().getId(),
                        kattisUrl);
                bot.getData().save();
            }
        });
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        interaction.respondWithModal(
                "kattisLinkAccount", "Paste your Kattis account URL here!",
                ActionRow.of(
                        TextInput.create(TextInputStyle.SHORT, "kattisUrl",
                                "Profile URL (/users/profile)")))
                .join();
        System.out.println("running!!!");
    }
}
