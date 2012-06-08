package nl.sison.xmpp.dao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
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
		Schema schema = new Schema(26, "nl.sison.xmpp.dao");

		Entity message = addMessage(schema);
		Entity buddy = addBuddy(schema);
		Entity connection = addConnectionConfiguration(schema);
		
		
		// many buddies to one connection
		Property connectionIdProperty = buddy.addLongProperty("connectionId").notNull().getProperty();
		buddy.addToOne(connection, connectionIdProperty);
		
		// many messages to one buddy
		Property buddyIdProperty = message.addLongProperty("buddyId").notNull().getProperty();
		message.addToOne(buddy, buddyIdProperty);		
	
		DaoGenerator generator = new DaoGenerator();
		generator.generateAll(schema, "../xmppclient/src-dao-gen");
		generator.generateAll(schema, "../xmppclient_redone_with_fragments/src-dao-gen");
	}

	private static Entity addMessage(Schema schema) {
		Entity message = schema.addEntity("MessageEntity");
		message.addIdProperty();
		message.addStringProperty("sender_jid").notNull(); // !
		message.addStringProperty("receiver_jid").notNull(); // !
		message.addStringProperty("content").notNull(); // !
		message.addDateProperty("received_date").notNull(); // !
		message.addBooleanProperty("delivered");
		message.addStringProperty("thread");
		
		return message;
	}

	/**
	 * IDEA - afdeling code en bijbehorende alias - sorteren op...
	 */

	public static Entity addBuddy(Schema schema) {
		Entity buddy = schema.addEntity("BuddyEntity");
		buddy.addIdProperty();
		buddy.addStringProperty("partial_jid").notNull(); // !
		buddy.addStringProperty("last_seen_resource"); // !
		buddy.addStringProperty("nickname"); // TODO settable by user
		buddy.addBooleanProperty("vibrate"); // TODO settable by user
		buddy.addStringProperty("presence_status"); // gone to lunch
		buddy.addStringProperty("presence_mode");
		buddy.addStringProperty("presence_type");
		buddy.addDateProperty("last_chat_date");
		buddy.addDateProperty("last_seen_online_date");
		buddy.addBooleanProperty("isAvailable");
		buddy.addBooleanProperty("isAway");

		return buddy;
	}

	public static Entity addConnectionConfiguration(Schema schema) {
		Entity connection = schema.addEntity("ConnectionConfigurationEntity");
		connection.addIdProperty();
		connection.addStringProperty("label").notNull().unique();
		connection.addStringProperty("port").notNull(); // NOTE: you could consider turning this into an int...
		connection.addStringProperty("server").notNull(); // where to connect
		connection.addStringProperty("domain"); // xmpp jid domain
		connection.addStringProperty("username").notNull();
		connection.addStringProperty("password").notNull();
		connection.addStringProperty("resource").notNull();
		connection.addBooleanProperty("encrypted").notNull(); // TLS/SSL encryption is
															// broken
		connection.addBooleanProperty("compressed").notNull();
		connection.addBooleanProperty("saslauthenticated").notNull(); // sasl
																	// authentication
																	// is broken
		connection.addIntProperty("connection_success").notNull();
//		settings.addStringProperty("provider_reflection_injection"); // TODO use
																		// reflection
																		// to
																		// inject
																		// behaviour
																		// in
																		// the
																		// connection
																		// process
		return connection;
	}
}
