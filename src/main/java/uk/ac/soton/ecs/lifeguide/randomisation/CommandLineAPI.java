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
	public static final String GET_ALLOCATION = "get_allocation";
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
		JSONObject json = new JSONObject();
		json.put("command", Arrays.toString(args));

		CommandLineAPI api = new CommandLineAPI();
		try {
			String result = null;

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
				if (args.length == 3) {
					result = api.addParticipant(args[1], args[2], null);
				} 
				else if (args.length == 4) {
					result = api.addParticipant(args[1], args[2], args[3]);
				}
				else {
					throw new BadCommandException("Usage: " + ADD_PARTICIPANT +" trial_name participant_identifier [data_path]");
				}
				json.put("allocation", result);
				json.put("message", args[2] +" was allocated to " +result +" in " +args[1]);
			}
			if (args[0].equals(GET_ALLOCATION)) {
				if (args.length != 3) {
					throw new BadCommandException("Usage: " +GET_ALLOCATION +" trial_name participant_identifier");
				}
				else {
					result = api.getParticipantAllocation(args[1],args[2]);
					json.put("allocation", result);
					json.put("message", args[2] +" is allocated to " +result +" in " +args[1]);
				}
			}

			json.put("status", COMMAND_SUCCESS);

		}
		catch (Exception e) {
			String s = stackTraceToString(e);
			logger.error(s);
			json.put("status", COMMAND_FAILURE);
			json.put("message", e.getClass().getSimpleName() +": " +e.getMessage());
			json.put("stacktrace", s);
		}
		api.disconnect();
		System.out.println(json.toString());
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
	public String registerTrial(String trialName, String definitionPath) throws PersistenceException, InvalidTrialException {
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
		return "Registered Trial with name " +trialName;
	}

	public String deleteTrial(String trialId) {
		logger.error("Deleting a trial is currently unsupported");
		// mrt - looks at existing database code and weeps
		return "...";
	}

	/* participant functions */
	public String addParticipant(String trialName, String participantIdentifier, String dataPath) throws AllocationException, PersistenceException, InvalidTrialException, FileNotFoundException {
		Trial trial = database.getTrial(trialName);
		if (trial == null) {
			throw new PersistenceException("No such trial: "+ trialName);
		}

		Participant participant = database.getParticipant(trialName, participantIdentifier);
		if (participant != null) {
			throw new AllocationException(participantIdentifier +" has already been allocated.");
		}

		String data;

		if (dataPath != null) {
			data = new Scanner(new File(dataPath)).useDelimiter("\\A").next();
		}
		else {
			data = null;
		}

		participant = new Participant();
		participant.setIdentifier(participantIdentifier);
		participant.setData(data);
	
		Arm allocatedArm = trial.allocate(participant, database);

		return allocatedArm.getName();
	}

	public String removeParticipant(String trialId, String participantId) {
		logger.error("Removing a participant is currently unsupported.");
		return "...";
	}

	public String getParticipantAllocation(String trialId, String participantId) {
		Participant p = database.getParticipant(trialId, participantId);
		return p.getAllocatedArm().getName();
	}
}