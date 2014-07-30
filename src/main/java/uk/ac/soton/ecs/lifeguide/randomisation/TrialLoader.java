package uk.ac.soton.ecs.lifeguide.randomisation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;

/**
 * A class which provides methods to create {@link TrialDefinition} objects from
 * specification text files.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class TrialLoader{

	// Pre-defined tokens (i.e. the specification file's syntax).
	private static final String STRATEGY_TYPE_TOKEN = "method";
	private static final String STRATA_TOKEN = "stratify";
	private static final String CLUSTER_TOKEN = "cluster";
	private static final String LIMIT_TOKEN = "limit";
	private static final String ATTRIBUTE_TOKEN = "group";
	private static final String ARMS_TOKEN = "arms";
	private static final String WEIGHT_TOKEN = "weight";
	private static final String ATTR_WEIGHT_TOKEN = "priority";
	private static final String DEFAULT_ARM_TOKEN = "default";
	private static final String COMMENT_TOKEN = "#";

	private static final String STRATEGY_CLASS_PACKAGE = "uk.ac.soton.ecs.lifeguide.randomisation.";

	private static final int DEFAULT_ARM_WEIGHT = 1;

	/**
	 * Constructs a {@link TrialDefinition} object based on a specification file.
	 * Throws InvalidTrialException objects, which contain an error message specifying
	 * why the input specification file is invalid, including line numbers for easier
	 * correction of syntax errors.
	 *
	 * @param filePath The path to the specification file.
	 * @return The constructed TrialDefinition object, containing the loaded set of attributes,
	 *         treatments and parameters.
	 * @throws InvalidTrialException Contains a message with a human readable description of the problem,
	 *                               including line number (where possible).
	 */
	public static Trial loadTrial(String filePath) throws InvalidTrialException {
		String trialName = ParserUtils.getAlphanumericFileName(filePath);
		String defaultArm = null;
		List<Attribute> attributes = new ArrayList<Attribute>();
		List<Arm> treatments = new ArrayList<Arm>();
		List<String> groupingFactors = new ArrayList<String>();
		List<String> clusterFactors = new ArrayList<String>();
		HashMap<String, Integer> weights = new HashMap<String, Integer>();
		HashMap<String, Integer> attrWeights = new HashMap<String, Integer>();
		HashMap<String, Integer> limitStatements = new HashMap<String, Integer>();

		HashMap<String, Double> strategyParams = new HashMap<String, Double>();
		String strategyName = "";
		Class<? extends Strategy> strategyClass = null;

		int lineNum = 0;
		int groupingLineNum = 0;
		int clusterLineNum = 0;
		int defaultArmLineNum = 0;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = "";

			// Track whether any groups have yet been specified (e.g. a Group or Value keyword).
			boolean groupFlag = false;

			// Valued attributes run across multiple lines. Temp variables for the value's name, and list of range options.
			String currentName = "";
			List<Grouping> ranges = new ArrayList<Grouping>();

			while ((line = reader.readLine()) != null) {
				// Read until EOF, track line number, remove whitespace.
				++lineNum;
				line = line.trim();

				// Remove mid-line comments.
				int commentIndex = line.indexOf(COMMENT_TOKEN);
				if (commentIndex >= 0)
					line = line.substring(0, commentIndex).trim();

				// Ignore empty lines.
				if (line.length() < 1)
					continue;

				// Use the first token on the line to decide on an action.
				String lineToken = ParserUtils.toAlphaNumeric(ParserUtils.getTokenAt(line, " ", 0));
				lineToken = lineToken.toLowerCase(); // mrt - force to lower toLowerCase
				
				// mrt - this used to be a switch statement
				if (lineToken.equals(STRATEGY_TYPE_TOKEN)) {
					strategyName = ParserUtils.getTokenAt(line, " ", 1);
				}
				else if (lineToken.equals(STRATA_TOKEN)) {
					// Record the grouping factors for later.
					groupingFactors = ParserUtils.tokenise(line, " ", 1);
					groupingLineNum = lineNum;
					// Remove additional punctuation (e.g. commas).
					for(int i = 0; i < groupingFactors.size(); ++i)
						groupingFactors.set(i, ParserUtils.toAlphaNumeric(groupingFactors.get(i)));
				}
				else if (lineToken.equals(CLUSTER_TOKEN)) {
					// Record the cluster factors for later.
					clusterFactors = ParserUtils.tokenise(line, " ", 1);
					clusterLineNum = lineNum;
				}
				else if (lineToken.equals(LIMIT_TOKEN)) {
					// Track the limit statements, and which line they appeared on.
					limitStatements.put(line, lineNum);
				}
				else if (lineToken.equals(DEFAULT_ARM_TOKEN)) {
					defaultArm = ParserUtils.getTokenAt(line, " ", 1);
					defaultArmLineNum = lineNum;
				}
				else if (lineToken.equals(ATTRIBUTE_TOKEN)) {
					groupFlag = true;
					// If we were tracking a value (i.e. multi-line with answer options), store it now.
					if (ranges.size() > 0) {
						attributes.add(new Attribute(currentName, ranges, 1, false));
						ranges = new ArrayList<Grouping>();
					}

					List<String> lineTokens = ParserUtils.tokenise(line, " ");
					String errorMsg = "Invalid group format. Usage is \"Group: <name>\" or \"Group: <name> <number of groups>\".";
							
					if(lineTokens.size() == 3){
						// Store the 'easy' attribute of the form: Group [name] [number of groups]
						String attrName = ParserUtils.getTokenAt(line, " ", 1);
						int numGroups = 0;
						try {
							numGroups = Integer.parseInt(ParserUtils.getTokenAt(line, " ", 2));
						} catch (NumberFormatException e) {
							throw new InvalidTrialException(errorMsg, lineNum);
						}
						attributes.add(new Attribute(attrName, numGroups, 1, false));
					} else if(lineTokens.size() == 2){
						currentName = ParserUtils.getTokenAt(line, " ", 1);
					} else {
						throw new InvalidTrialException(errorMsg, lineNum);
					}
				}
				else if (lineToken.equals(ARMS_TOKEN)) {
					// Tokenise the list of treatment arms, set them up as Arm objects. Weights assigned later.
					List<String> treatmentNames = ParserUtils.tokenise(line, " ", 1);
					for (String name : treatmentNames)
						treatments.add(new Arm(ParserUtils.toAlphaNumeric(name), DEFAULT_ARM_WEIGHT));
				}
				else if (lineToken.equals(WEIGHT_TOKEN)) {
					// Store the weight statement and line number for later use.
					weights.put(line, lineNum);
				}
				else if (lineToken.equals(ATTR_WEIGHT_TOKEN)) {
					attrWeights.put(line, lineNum);
				}
				else {
					// No attributes specified yet, so this line is a custom parameter.
					if (!groupFlag) {
						int splitIndex = line.lastIndexOf(" ");
						if(splitIndex < 1){
							String errorMsg = "Parameters must be in the format [param name]: [param value], where value is a number.";
							throw new InvalidTrialException(errorMsg, lineNum);
						}
						String paramName = ParserUtils.toAlphaNumeric(line.substring(0, splitIndex));
						String paramValue = ParserUtils.toAlphaNumeric(line.substring(splitIndex + 1));
						if (paramName.equals("") || paramValue.equals("")) {
							String errorMsg = "Invalid parameter specification. Usage is  \"[Parameter name]: [value]\"";
							throw new InvalidTrialException(errorMsg, lineNum);
						}
						try{
							strategyParams.put(paramName, Double.parseDouble(paramValue));
						} catch(NumberFormatException e){
							String errorMsg = "Parameters must be in the format [param name]: [param value], where value is a number.";
							throw new InvalidTrialException(errorMsg, lineNum);
						}
					}
					else {
						// Line is not a custom parameter, try to interpret it as a group range.
						String errorMsg = "";
						try {
							if (line.charAt(0) == '<') {
								// Upper bound, implicitly strict <, not <=.
								errorMsg = "Range formatting error. Format: <[num]";
								double limit = Double.parseDouble(ParserUtils.toDecimal(line));
								ranges.add(new Grouping(line, -Double.MAX_VALUE, limit));
							} else if (line.charAt(0) == '>') {
								// Lower bound, implicitly >=.
								errorMsg = "Range formatting error. Format: >[num]";
								double limit = Double.parseDouble(ParserUtils.toDecimal(line));
								ranges.add(new Grouping(line, limit, Double.MAX_VALUE));
							} else if (line.contains(" to ")) {
								// Range
								errorMsg = "Range formatting error. Format: [num] to [num]";
								double lowerLimit = Double.parseDouble(ParserUtils.getTokenAt(line, " to ", 0));
								double upperLimit = Double.parseDouble(ParserUtils.getTokenAt(line, " to ", 1));
								if (lowerLimit > upperLimit) {
									errorMsg = "Range error. Ensure: lower bound <= upper bound.";
									throw new InvalidTrialException(errorMsg, lineNum);
								}
								ranges.add(new Grouping(line, lowerLimit, upperLimit));
							} else {
								errorMsg = "Value error. Formats: [num], [num] to [num], <[num], or >[num]";
								double val = Double.parseDouble(line);
								ranges.add(new Grouping(line, val, val));
							}
						} catch (NumberFormatException e) {
							throw new InvalidTrialException(errorMsg, lineNum);
						}
					}
				}
				
			}

			// Store the final attribute.
			if (ranges.size() > 0) {
				attributes.add(new Attribute(currentName, ranges, 1, false));
			}

			reader.close();

		} catch (FileNotFoundException e) {
			throw new InvalidTrialException("No such trial specification file found.", lineNum);
		} catch (IOException e) {
			throw new InvalidTrialException("An error occurred while loading the trial:\n" + e.getMessage(), lineNum);
		}


		// ====================================================================================
		// Apply grouping limit statements.
		// ====================================================================================
		for (String limitString : limitStatements.keySet()) {
			String tName = ParserUtils.getTokenAt(limitString, " ", 1);
			boolean found = false;
			// Find the named treatment.
			for (Arm treatment : treatments) {
				if (treatment.getName() != null && treatment.getName().equals(tName)) {
					try {
						// Assign the limit to the treatment.
						int tLimit = Integer.parseInt(ParserUtils.getTokenAt(limitString, " ", 2));
						treatment.setMaxParticipants(tLimit);
						found = true;
						break;
					} catch (NumberFormatException e) {
						String errorMsg = "Incorrect number formatting. Usage is \"Limit [attribute name] [limit]\"";
						throw new InvalidTrialException(errorMsg, limitStatements.get(limitString));
					}
				}
			}
			// Throw an exception if the treatment does not exist.
			if (!found) {
				String errorMsg = "Limit requested on '" + tName + "', but no such treatment arm exists.";
				throw new InvalidTrialException(errorMsg, limitStatements.get(limitString));
			}
		}


		// ====================================================================================
		// Apply treatment weight statements.
		// ====================================================================================
		for (String weightString : weights.keySet()) {
			String weightName = ParserUtils.getTokenAt(weightString, " ", 1);
			boolean found = false;
			for (Arm treatment : treatments) {
				if (treatment.getName() != null && treatment.getName().equals(weightName)) {
					try{
						treatment.setWeight(Integer.parseInt(ParserUtils.getTokenAt(weightString, " ", 2)));
					} catch(NumberFormatException e){
						String errorMsg = "Invalid number formatting. Usage is \"Weight: [arm name] [weight]\"";
						throw new InvalidTrialException(errorMsg, weights.get(weightString));
					}
					found = true;
					break;
				}
			}
			if(!found){
				String errorMsg = "Weight requested on '" + weightName + "', but no such treatment arm exists.";
				throw new InvalidTrialException(errorMsg, weights.get(weightString));
			}
		}
		
		// ====================================================================================
		// Apply attribute minimisation weights.
		// ====================================================================================
		for(String weightString: attrWeights.keySet()){
			String attrName = ParserUtils.getTokenAt(weightString, " ", 1);
			boolean found = false;
			for (Attribute attribute : attributes) {
				if (attribute.getName() != null && attribute.getName().equals(attrName)) {
					try{
						attribute.setWeight(Double.parseDouble(ParserUtils.getTokenAt(weightString, " ", 2)));
					} catch(NumberFormatException e){
						String errorMsg = "Invalid number formatting. Usage is \"Priority: [attribute name] [priority]\"";
						throw new InvalidTrialException(errorMsg, weights.get(weightString));
					}
					found = true;
					break;
				}
			}
			if (!found) {
				String errorMsg = "Priority requested for '" + attrName + "', but no such attribute exists.";
				throw new InvalidTrialException(errorMsg, weights.get(weightString));
			}
		}


		// ====================================================================================
		// Set up grouping factors, throw an error if no attribute exists which matches the grouping factor.
		// ====================================================================================
		for (String groupName : groupingFactors) {
			boolean found = false;
			for (Attribute attr : attributes) {
				if (attr.getName() != null && attr.getName().equals(groupName)) {
					attr.setGroupingFactor(true);
					found = true;
				}
			}
			if (!found) {
				String errorMsg = "Stratification requested on '" + groupName + "', but no such attribute exists.";
				throw new InvalidTrialException(errorMsg, groupingLineNum);
			}
		}


		// ====================================================================================
		// Set up cluster factors, throw an error if no attribute exists which matches the cluster factor.
		// ====================================================================================
		List<Integer> indexList = new ArrayList<Integer>();
		for (String clusterName : clusterFactors) {
			boolean found = false;
			for (int i = 0; i < attributes.size(); ++i) {
				if (attributes.get(i).getName().equals(clusterName)) {
					indexList.add(i);
					found = true;
				}
			}
			if (!found) {
				String errorMsg = "Clustering requested on '" + clusterName + "', but no such attribute exists.";
				throw new InvalidTrialException(errorMsg, clusterLineNum);
			}
		}
		int[] clusterIndices = new int[indexList.size()];
		for (int i = 0; i < indexList.size(); ++i)
			clusterIndices[i] = indexList.get(i);

		// ====================================================================================
		// Set up default treatment arm, throw an error if the specified default arm does not exist.
		// ====================================================================================
		if (defaultArm != null) {
			boolean defaultFound = false;
			for (Arm treatment : treatments) {
				if (treatment.getName().equals(defaultArm))
					defaultFound = true;
			}
			if (!defaultFound) {
				String errMsg = "Default treatment arm '" + defaultArm + "' specified, but no such treatment arm exists.";
				throw new InvalidTrialException(errMsg, defaultArmLineNum);
			}
		}

		// ====================================================================================
		// Check all required parameters are present, and all present parameters are required.
		// ====================================================================================
		List<String> requiredParams = Strategy.getRequiredParameters(strategyClass);

		for (String userParam : strategyParams.keySet()) {
			boolean found = false;
			for (String stratParam : requiredParams) {
				if (userParam.equals(stratParam))
					found = true;
			}
			if (!found) {
				String errMsg = "Parameter '" + userParam + "' has been specified, but is not required by this allocation strategy.";
				throw new InvalidTrialException(errMsg, lineNum);
			}
		}

		// ====================================================================================
		// Final sanity checks.
		// ====================================================================================

		// Strategy correctness.
		if (strategyName == "") {
			throw new InvalidTrialException("No allocation method chosen! Usage is \"Method: [method name]\".", lineNum);
		}

		// Arm presence.
		if (treatments.size() == 0) {
			throw new InvalidTrialException("No trial arms specified! Usage is \"Arms: arm1 arm2 arm3 ...\"", lineNum);
		}

		// Attributes have no null/empty names, all have at least one group, and there are no duplicate names.
		List<String> attrNames = new ArrayList<String>();
		for (Attribute attr : attributes) {
			if (!attr.isValid())
				throw new InvalidTrialException("Invalid attribute. Attributes must have a name, and at least one group.", lineNum);
			if (attrNames.contains(attr.getName()))
				throw new InvalidTrialException("Duplicate attribute name: " + attr.getName(), lineNum);
			attrNames.add(attr.getName());
		}

		Trial tDef = new Trial(trialName, strategyName, strategyParams, attributes, treatments, clusterIndices);
		tDef.setDefaultArm(defaultArm);

		// Check that the trial complies with any extra checks needed for its choice of allocation strategy.
		try{
			Strategy.checkValidTrial(tDef);
		} catch(InvalidTrialException e){
			e.setLineNumber(lineNum);
			throw e;
		}
		
		
		return tDef;
	}

}
