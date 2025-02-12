package nl.cfgroningen.command;

import java.awt.Color;

import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import lombok.AllArgsConstructor;
import nl.cfgroningen.bot.KattisBot;

@AllArgsConstructor
public abstract class GenericCommand {

    protected KattisBot bot;

    public void register() {
    }

    public abstract SlashCommandBuilder getCommandDefinition();

    public abstract String getName();

    public abstract void execute(SlashCommandInteraction interaction);

    protected boolean hasPermission(User user, Server server) {
        return true;
    }

    protected EmbedBuilder error(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.RED);
    }

    protected EmbedBuilder success(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.GREEN);
    }

    protected EmbedBuilder info(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.BLUE);
    }

    protected void respond(EmbedBuilder embed, SlashCommandInteraction interaction) {
        interaction.createImmediateResponder()
                .addEmbed(embed)
                .respond();
    }

    protected void respondEphemeral(EmbedBuilder embed, SlashCommandInteraction interaction) {
        interaction.createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .addEmbed(embed)
                .respond();
    }

    protected EmbedBuilder getNoSubcommandSpecified() {
        return error("No subcommand specified!", "Please specify a subcommand!");
    }

    protected EmbedBuilder getIllegalValueForParameter(String parameter) {
        return error("Illegal value for parameter " + parameter,
                "Please specify a valid value for the parameter " + parameter);
    }

    protected EmbedBuilder getNoPermission() {
        return error("No Permission", "You do not have permission to use this command");
    }
}
