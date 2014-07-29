package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineAPI {

	public static final String REGISTER_TRIAL = "register_trial";
	public static final String ADD_PARTICIPANT = "add_participant";
	public static final String COMMAND_FAILURE = "failure";
	public static final String COMMAND_SUCCESS = "success";

	private static final Logger logger = LoggerFactory.getLogger(CommandLineAPI.class);
	private DataManager database;


	private static String stackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	public static void main(String[] args) {
		CommandLineAPI api = new CommandLineAPI();
		try {
			if (args.length == 0) {
				throw new BadCommandException("No parameters given.");
			}
			api.connect();

			if (args[0].equals(REGISTER_TRIAL)) {
				if (args.length != 3) {
					throw new BadCommandException("Usage: " +REGISTER_TRIAL +" trial_name definition_path");
				}
				api.registerTrial(args[1], args[2]);
			}

			if (args[0].equals(ADD_PARTICIPANT)) {
				if (args.length != 4) {
					throw new BadCommandException("Usage: " + ADD_PARTICIPANT +" trial_name user_identifier data_path");
				}
				Random r = new Random();
				api.addParticipant(args[1], args[2]+r.nextInt(), args[3]);
			}
		}
		catch (Exception e) {
			String s = stackTraceToString(e);
			logger.error(s);

			JSONObject json = new JSONObject();
			json.put("status", COMMAND_FAILURE);
			json.put("command", Arrays.toString(args));
			json.put("message", e.getClass().getSimpleName() +": " +e.getMessage());
			json.put("stacktrace", s);
			System.out.println(json.toString());
		}
		api.disconnect();
	}

	// mrt - don't really need an instance of this class......
	public void connect() throws PersistenceException {
		database = new DataManager("root", "", "randomisation", "127.0.0.1");
		database.connect();
	}

	public void disconnect() {
		database.disconnect();
	}

	/* study functions */
	public void registerTrial(String trialName, String definitionPath) throws PersistenceException, InvalidTrialException {
		//Trial t = TrialLoader.loadTrial(definitionPath);
		//System.out.println(t);

		if (database.getTrial(trialName) == null) {
			Trial trial = TrialLoader.loadTrial(definitionPath);
			trial.setName(trialName);
			database.registerTrial(trial);
			// mrt - do success message
		}
		else {
			throw new PersistenceException("Trial with name " +trialName +" already exists.");
		}
	}

	public void deleteTrial(String trialId) {
		logger.error("Deleting a trial is currently unsupported");
		// mrt - looks at existing database code and weeps
	}

	/* participant functions */
	public void addParticipant(String trialName, String participantIdentifier, String dataPath) throws AllocationException, PersistenceException, InvalidTrialException, FileNotFoundException {
		Trial trial = database.getTrial(trialName);
		if (trial == null) {
			throw new PersistenceException("No such trial: "+ trialName);
		}
		logger.debug(trial.toString());

		String data = new Scanner(new File(dataPath)).useDelimiter("\\A").next();
		// JSONObject json = new JSONObject(data);
		
		// // ignore the json data for now - fuck it!

		Participant participant = new Participant();
		participant.setIdentifier(participantIdentifier);
		participant.setData(data);

		// // mrt - seriously?????
		Arm cunt = trial.allocate(participant, database);
		// //String treatment = trial.getTreatmentName(cunt);
		System.out.println("Allocated to: " +cunt);
	}

	public void removeParticipant(String trialId, String participantId) {
		logger.error("Removing a participant is currently unsupported.");
	}

	public void getParticipantAllocation(String trialId, String participantId) {
		logger.info("getParticipantAllocation: "+ trialId +" " +participantId);
	}
}