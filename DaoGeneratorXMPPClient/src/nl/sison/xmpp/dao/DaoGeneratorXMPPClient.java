package nl.sison.xmpp.dao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Generates entities and DAOs for the project XMPPClient.
 * 
 * Run it as a Java application (not Android).
 * 
 * @author Jasm Sison
 */
public class DaoGeneratorXMPPClient {

	public static void main(String[] args) throws Exception {
		Schema schema = new Schema(9, "nl.sison.xmpp.dao");

		addMessage(schema);
		addBuddy(schema);
		addConnectionConfiguration(schema);

		new DaoGenerator().generateAll(schema, "../xmppclient/src-dao-gen");
	}

	private static void addMessage(Schema schema) {
		Entity message = schema.addEntity("MessageEntity");
		message.addIdProperty();
		message.addStringProperty("sender_jid").notNull();
		message.addStringProperty("receiver_jid").notNull();
		message.addStringProperty("content").notNull();
		message.addDateProperty("received_date");
		message.addBooleanProperty("delivered");
		message.addBooleanProperty("processed");
	}

	/**
	 * IDEA - afdeling code en bijbehorende alias - sorteren op...
	 */

	public static void addBuddy(Schema schema) {
		Entity buddy = schema.addEntity("BuddyEntity");
		buddy.addIdProperty();
		buddy.addStringProperty("partial_jid").notNull();
		buddy.addStringProperty("resource").notNull();
		buddy.addStringProperty("nickname").notNull();
		buddy.addStringProperty("presence").notNull();
		buddy.addStringProperty("custom_away_string").notNull();
		buddy.addDateProperty("last_seen_date");
	}

	public static void addConnectionConfiguration(Schema schema) {
		Entity settings = schema.addEntity("ConnectionConfigurationEntity");
		settings.addIdProperty();
		settings.addStringProperty("label").notNull().unique();
		settings.addStringProperty("port").notNull();
		settings.addStringProperty("server").notNull(); // where to connect
		settings.addStringProperty("domain"); // xmpp jid domain
		settings.addStringProperty("username").notNull();
		settings.addStringProperty("password").notNull();
//		settings.addStringProperty("jid").notNull();
		settings.addStringProperty("resource").notNull();
		settings.addBooleanProperty("encrypted").notNull(); // encryption is broken
		settings.addBooleanProperty("compressed").notNull(); 
		settings.addBooleanProperty("saslauthenticated").notNull(); // sasl authentication is broken
		settings.addIntProperty("connection_success").notNull();
	}
}
