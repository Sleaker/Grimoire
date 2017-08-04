package net.bemacized.grimoire;

import net.bemacized.grimoire.database.DBManager;
import net.bemacized.grimoire.eventhandlers.MainChatProcessor;
import net.bemacized.grimoire.model.controllers.*;
import net.bemacized.grimoire.model.controllers.PricingManager;
import net.bemacized.grimoire.model.models.imageproviders.ImageProvider;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Grimoire {

	private final static Logger LOG = Logger.getLogger(Grimoire.class.getName());
	private static Grimoire instance;

	public static Grimoire getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		new Grimoire(System.getenv("BOT_TOKEN"));
	}

	// Model Controllers
	private Cards cards;
	private Sets sets;
	private Tokens tokens;
	private ComprehensiveRules comprehensiveRules;
	private Definitions definitions;
	private InfractionProcedureGuide infractionProcedureGuide;
	private TournamentRules tournamentRules;
	private PricingManager pricingManager;
	private ImageProviders imageProviders;

	private JDA discord;
	private DBManager dbManager;

	private Grimoire(String bot_token) {
		instance = this;

		// Verify existence of token
		if (bot_token == null) {
			LOG.severe("No discord bot token was set in the BOT_TOKEN environment variable! Quitting...");
			System.exit(1);
		}

		// Connect to Mongo database
		int MONGO_PORT = -1;
		try {
			MONGO_PORT = Integer.parseInt(System.getenv("MONGO_PORT"));
		} catch (Exception ignored) {
		}
		this.dbManager = new DBManager(
				System.getenv("MONGO_HOST"),
				MONGO_PORT,
				System.getenv("MONGO_DB"),
				System.getenv("MONGO_USER"),
				System.getenv("MONGO_PASSWORD")
		);

		// Setup image providers
		this.imageProviders = new ImageProviders();

		// Load sets and cards
		MTGJSON mtgjson = new MTGJSON();
		this.cards = new Cards(mtgjson);
		this.sets = new Sets(mtgjson);
		mtgjson.load();

		// Load tokens
		this.tokens = new Tokens();
		this.tokens.load();

		// Load definitions
		this.definitions = new Definitions();
		this.definitions.load();

		// Load comprehensive rules
		this.comprehensiveRules = new ComprehensiveRules();
		this.comprehensiveRules.load();

		// Load tournament rules
		this.tournamentRules = new TournamentRules();
		this.tournamentRules.load();

		// Load infraction procedure guide
		this.infractionProcedureGuide = new InfractionProcedureGuide();
		this.infractionProcedureGuide.load();

		// Instantiate pricing manager
		this.pricingManager = new PricingManager();
		this.pricingManager.init();

		// Log in to Discord
		try {
			LOG.info("Logging in to Discord...");
			discord = new JDABuilder(AccountType.BOT)
					.setAutoReconnect(true)
					.setToken(bot_token)
					.buildBlocking();
			LOG.info("Discord login complete.");
		} catch (LoginException e) {
			LOG.log(Level.SEVERE, "Could not log in to Discord. Quitting...", e);
			System.exit(1);
		} catch (RateLimitedException e) {
			LOG.log(Level.SEVERE, "Walked into a rate limit while logging in. Please try again later. Quitting...", e);
			System.exit(1);
		} catch (InterruptedException e) {
			LOG.log(Level.SEVERE, "Login procedure was interrupted. Quitting...", e);
			System.exit(1);
		}

		// Register EventHandlers
		discord.addEventListener(new MainChatProcessor());
	}

	public JDA getDiscord() {
		return discord;
	}

	public DBManager getDBManager() {
		return dbManager;
	}

	public PricingManager getPricingManager() {
		return pricingManager;
	}

	public Tokens getTokens() {
		return tokens;
	}

	public ComprehensiveRules getComprehensiveRules() {
		return comprehensiveRules;
	}

	public Definitions getDefinitions() {
		return definitions;
	}

	public InfractionProcedureGuide getInfractionProcedureGuide() {
		return infractionProcedureGuide;
	}

	public TournamentRules getTournamentRules() {
		return tournamentRules;
	}

	public Cards getCards() {
		return cards;
	}

	public Sets getSets() {
		return sets;
	}

	public ImageProviders getImageProviders() {
		return imageProviders;
	}
}
