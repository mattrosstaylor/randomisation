package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineAPI {

	private static final Logger logger = LoggerFactory.getLogger(CommandLineAPI.class);
	private DBManager database;


	public static void main(String[] args) {

		CommandLineAPI api = new CommandLineAPI();
		try {
			api.connectDatabase();
			api.registerTrial("test_trial.txt");

			api.disconnectDatabase();
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void connectDatabase() throws SQLException {
		database = new DBManager("root", "", "randomisation", "127.0.0.1");
		// mrt - returning true or false for critical failure cases? 
		// what the fuck is this? C coding????
		if (database.connect()) {
			if (!database.checkTablesExist()) {
				database.createTables();
				logger.info("Created database.");
			}
		}
		else {
			// this already got "logged" in the connect method
			throw new Error("FUUUUUCK YOOOOOOOU");
		}
	}

	public void disconnectDatabase() {
		database.disconnect();
	}

	/* study functions */
	public void registerTrial(String definitionPath) {
		logger.info("Register trial: " +definitionPath);

		try {
			TrialDefinition trial = TrialLoader.loadTrial(definitionPath);
		}
		catch (InvalidTrialException e) {
			logger.error(e.getMessage());
		}
	}

	public void deleteTrial(String trialId) {
		logger.error("Deleting a trial is currently unsupported");
	}

	public void showStudyInfo(String trialId) {
		logger.info("showTrialInfo: " +trialId);	
	}

	/* participant functions */
	public void addParticipant(String trialId, String participantId, String data) {
		logger.info("addParticipant: " +trialId +" " +participantId);
	}

	public void removeParticipant(String trialId, String participantId) {
		logger.error("Removing a participant is currently unsupported.");
	}

	public void getParticipantAllocation(String trialId, String participantId) {
		logger.info("getParticipantAllocation: "+ trialId +" " +participantId);
	}
}