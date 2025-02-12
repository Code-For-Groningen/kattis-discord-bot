package nl.cfgroningen;

import nl.cfgroningen.api.API;
import nl.cfgroningen.bot.KattisBot;

public class BotStarter {
    public static void main(String[] args) {
        // Fetch the token from the environment
        final String token = System.getenv("DISCORD_TOKEN");
        final String universityUrl = System.getenv("FOR_UNIVERSITY");
        final int port = Integer.parseInt(System.getenv("PORT"));

        // Check if the token is set
        if (token == null) {
            System.err.println("DISCORD_TOKEN is not set");
            System.exit(1);
        }

        // Create a new instance of the bot
        KattisBot bot = new KattisBot(token, universityUrl);

        API api = new API(port, bot.getDataManager());

        Thread t1 = new Thread(api::serveAPI);
        t1.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down API");
            t1.interrupt();
        }));

        bot.initialize();
    }
}
