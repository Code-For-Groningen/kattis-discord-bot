package nl.cfgroningen;

import nl.cfgroningen.API.API;
import nl.cfgroningen.bot.KattisBot;

public class BotStarter {
    public static void main(String[] args) {
        // Fetch the token from the environment
        final String token = System.getenv("DISCORD_TOKEN");
        final String universityUrl = System.getenv("FOR_UNIVERSITY");

        // Check if the token is set
        if (token == null) {
            System.err.println("DISCORD_TOKEN is not set");
            System.exit(1);
        }

        Thread t1 = new Thread() {
            public void run() {
                API.serveAPI(universityUrl);
            }
        };
        t1.start();

        // Create a new instance of the bot
        KattisBot bot = new KattisBot(token, universityUrl);
        bot.initialize();
    }
}
