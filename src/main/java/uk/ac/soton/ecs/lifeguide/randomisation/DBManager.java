//tutorial at http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
//set up at http://db.apache.org/derby/papers/DerbyTut/ij_intro.html

package uk.ac.soton.ecs.lifeguide.randomisation;

import com.mysql.jdbc.Driver; // mrt - lololololol
import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;


public class DBManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

    private Connection conn = null;
    private HashMap<String, String> db_schema = new HashMap<String, String>();

    private static final String PROTOCOL = "jdbc:mysql:";
    private String PW = "";
    private String UN = "root";
    private String DB_SERVER_IP = "localhost";
    private String DB_NAME = "randomisation";

    private static final String STRATEGY_CLASS_PACKAGE = "uk.ac.soton.ecs.lifeguide.randomisation.";

    private LifeGuideAPI lifeGuideAPI;

    /**
     * @param uname        Database username
     * @param pw           Database password for the given username
     * @param dbname       Database name which to connect to
     * @param db_server_ip IP address of the server hosting the database.
     */
    public DBManager(String uname, String pw, String dbname, String db_server_ip) {
        PW = pw;
        UN = uname;
        DB_SERVER_IP = db_server_ip;
        DB_NAME = dbname;

        initHashMap();
    }

    /**
     * Initiates a {@link HashMap} object with the required tables that the DBManager needs and will operate on. The HashMap is of the form TableName => SchemaDescription, where both are strings.
     */
    private void initHashMap() {
        db_schema.put("INTERVENTION",
                " (id INT AUTO_INCREMENT PRIMARY KEY, trial_name VARCHAR(255) UNIQUE NOT NULL, strategy_id VARCHAR(255) NOT NULL, cluster_factors VARCHAR(255) NOT NULL)");

        db_schema.put("RESPONSE",
                " (id INT AUTO_INCREMENT PRIMARY KEY, value FLOAT NOT NULL, parameter_name VARCHAR(255) NOT NULL, participant_id INT NOT NULL, time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, trial_definition_id INT NOT NULL )");

        /*db_schema.put("STRATEGY",
                "(name VARCHAR(255) PRIMARY KEY, class_name VARCHAR(255) )"); */

        db_schema.put("PARTICIPANT",
                " (id INT AUTO_INCREMENT PRIMARY KEY, given_id INT, treatment_id INT )");

        db_schema.put("ATTRIBUTE",
                " (id INT AUTO_INCREMENT PRIMARY KEY, trial_definition_id INT NOT NULL , attr_name VARCHAR(255) NOT NULL, weight FLOAT NOT NULL, grouping_factor VARCHAR(255), num_groups INT )");

        db_schema.put("GROUPS",
                " (id INT AUTO_INCREMENT PRIMARY KEY,  attribute_id INT NOT NULL , name VARCHAR(255) NOT NULL, range_min FLOAT NOT NULL, range_max FLOAT NOT NULL )");

        db_schema.put("TREATMENT",
                " (id INT AUTO_INCREMENT PRIMARY KEY, trial_definition_id INT NOT NULL ,  name VARCHAR(255) NOT NULL, weight INT NOT NULL, max_participants INT NOT NULL, participant_limit VARCHAR(255) NOT NULL )");

        db_schema.put("INTERVENTION_PARAMS",
                " (id INT AUTO_INCREMENT PRIMARY KEY, trial_definition_id INT NOT NULL, name VARCHAR(255) NOT NULL,  value FLOAT NOT NULL )");

        db_schema.put("STRATEGY_STATISTICS",
                " (id INT AUTO_INCREMENT PRIMARY KEY, statistic_name VARCHAR(255) NOT NULL, value FLOAT NOT NULL, trial_definition_id INT NOT NULL)");
    }


    /**
     * A convenience method for getting the id of a particular {@link TrialDefinition} object, given its name.
     *
     * @param trialName The name of the trial definition
     * @return An integer representing the id key of this trial definition in the database
     * @throws SQLException
     */
    public int getTrialDefinitionId(String trialName) throws SQLException {
        PreparedStatement pst = conn.prepareStatement("SELECT id FROM INTERVENTION WHERE trial_name = ?");
        pst.setString(1, trialName);
        pst.executeQuery();

        ResultSet rs = pst.getResultSet();
        if (rs != null && rs.next()) {

            return rs.getInt("id");
        }

        return -1;
    }

    /**
     * Checks to see that the required tables exist in the database.
     *
     * @return Returns true if the required schema exists in the database
     * @throws SQLException
     */
    private boolean checkTablesExist() throws SQLException {
        DatabaseMetaData dbmdata = conn.getMetaData();
        ResultSet res = dbmdata.getTables(null, null, null, new String[]{"TABLE"});
        HashSet<String> tables = new HashSet<String>();
        while (res.next()) {
            tables.add(res.getString("TABLE_NAME"));
        }
        return tables.containsAll(db_schema.keySet());
    }

    /**
     * Creates the tables required for the module to work.
     *
     * @throws SQLException
     */
    private void createTables() throws SQLException {
        for (String s : db_schema.keySet()) {
            if (createTable(s, db_schema.get(s))) {
                logger.debug("Created table " + s);
            }
        }
    }

    /**
     * @param tblName Name of the table to be created
     * @param specs   DB Engine specific SQL code for the creation of the table
     * @return Returns true if the table was successfully created.
     * @throws SQLException
     */
    private boolean createTable(String tblName, String specs) throws SQLException {
        Statement statement = conn.createStatement();
        return statement.execute("CREATE TABLE " + tblName + specs);
    }

    /**
     * Connects to the database.
     */
    public void connect() throws PersistenceException {
        try {
            new Driver(); // mrt - this is maaaaagic and you have to do this in order to register the Driver
            conn = DriverManager.getConnection(PROTOCOL + "//" + DB_SERVER_IP + "/" + DB_NAME, UN, PW);

            // initialise databse
            if (!checkTablesExist()) {
                createTables();
                logger.info("Created database.");
            }
        } 
        catch (SQLException e) {
            throw new PersistenceException("Error initialising database.", e);
        }
    }

    /**
     * Wrapper for calling the {@link #closeDB()}  method.
     *
     * @throws PersistenceException
     */
    public void disconnect() throws PersistenceException {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Error closing database", e);
        }
    }

    /**
     * Registers a new TrialDefinition object in the database.
     *
     * @param trialDefinition The trial definition to be registered.
     * @throws PersistenceException
     */
    public void registerTrial(TrialDefinition trialDefinition) throws PersistenceException {
        try {
            PreparedStatement trialInsertStmt = conn.prepareStatement("INSERT INTO INTERVENTION(trial_name, strategy_id, cluster_factors) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement insertGroupStmt = conn.prepareStatement("INSERT INTO GROUPS(attribute_id , name, range_min, range_max) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement insertAttributeStmt = conn.prepareStatement("INSERT INTO ATTRIBUTE(trial_definition_id , attr_name, weight, grouping_factor, num_groups) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement insertTreatmentStmt = conn.prepareStatement("INSERT INTO TREATMENT(name, weight , max_Participants, participant_limit, trial_definition_id) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement insertParams = conn.prepareStatement("INSERT INTO INTERVENTION_PARAMS( name , value, trial_definition_id ) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement insertStatistics = conn.prepareStatement("INSERT INTO STRATEGY_STATISTICS( statistic_name, value, trial_definition_id) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);

            trialInsertStmt.clearParameters();
            trialInsertStmt.setString(1, trialDefinition.getTrialName());
            trialInsertStmt.setString(2, trialDefinition.getStrategyID());
            trialInsertStmt.setString(3, joinArray(trialDefinition.getClusterIndices(), ","));
            trialInsertStmt.executeUpdate();

            ResultSet generatedIds = null;
            long generated_id = -1;
            int trial_id = -1;

            generatedIds = trialInsertStmt.getGeneratedKeys();
            if (!generatedIds.next()) {
                throw new PersistenceException("Could not access AUTO_INCREMENT values"); // mrt - this is probably unreachable code
            }

            generated_id = generatedIds.getLong(1);
            trial_id = (int) generated_id;

            for (Attribute attr : trialDefinition.getAttributes()) {
                insertAttributeStmt.clearParameters();
                insertAttributeStmt.setInt(1, (int) trial_id);
                insertAttributeStmt.setString(2, attr.getAttributeName());
                insertAttributeStmt.setFloat(3, attr.getWeight());
                insertAttributeStmt.setString(4, Boolean.toString(attr.isGroupingFactor()));
                insertAttributeStmt.setInt(5, attr.getGroupCount());
                insertAttributeStmt.executeUpdate();

                generatedIds = insertAttributeStmt.getGeneratedKeys();

                if (!generatedIds.next()) {
                    throw new PersistenceException("Could not access AUTO_INCREMENT values"); // mrt - this is probably unreachable code
                }
                generated_id = generatedIds.getLong(1);

                for (Group g : attr.getRanges()) {
                    insertGroupStmt.clearParameters();
                    insertGroupStmt.setInt(1, (int) generated_id);
                    insertGroupStmt.setString(2, g.getName());
                    insertGroupStmt.setFloat(3, g.getRangeMin());
                    insertGroupStmt.setFloat(4, g.getRangeMax());
                    insertGroupStmt.executeUpdate();
                }
            }

            Map<String, Float> m = Strategy.getStoredParameters(trialDefinition.getStrategy(), trialDefinition);
            Map<String, Float> m2 = trialDefinition.getStrategyParams();
            for (String s : m.keySet()) {
                if (m2.containsKey(s)) {
                    m.put(s, m2.get(s));
                }

                insertStatistics.clearParameters();
                insertStatistics.setString(1, s);
                insertStatistics.setFloat(2, m.get(s));
                insertStatistics.setInt(3, (int) trial_id);
                insertStatistics.executeUpdate();
            }

            for (Treatment t : trialDefinition.getTreatments()) {
                insertTreatmentStmt.clearParameters();
                insertTreatmentStmt.setString(1, t.getName());
                insertTreatmentStmt.setInt(2, t.getWeight());
                insertTreatmentStmt.setInt(3, t.getParticipantLimit());
                insertTreatmentStmt.setString(4, Boolean.toString(t.hasParticipantLimit()));
                insertTreatmentStmt.setInt(5, trial_id);
                insertTreatmentStmt.executeUpdate();
            }

            for (String name : trialDefinition.getStrategyParams().keySet()) {
                insertParams.clearParameters();
                insertParams.setString(1, name);
                insertParams.setFloat(2, trialDefinition.getStrategyParam(name));
                insertParams.setInt(3, (int) trial_id);
                insertParams.executeUpdate();
            }
        }

        // mrt - this needs more testing before I can really determine what the failure cases are
        catch (SQLIntegrityConstraintViolationException e) {
            throw new PersistenceException("Could not register trial", e);
        } 
        catch (SQLException e) {
            throw new PersistenceException("Could not register trial", e);
        }
    }

    /**
     * Checks the database to determine whether a trial with the given name exists.
     *
     * @param trialName
     * @return Returns true if the trial definition exists.
     */
    public boolean trialExists(String trialName) {
        try {
            PreparedStatement st = conn.prepareStatement("SELECT trial_name FROM INTERVENTION WHERE trial_name=?");
            st.setString(1, trialName);
            st.executeQuery();

            ResultSet rs = st.getResultSet();

            if (rs != null && rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace(); // mrt - this will probably never happen....
        }
        return false;
    }

    /**
     * Retrieves the number of participants in a given trial, specified by the {@link TrialDefinition} parameter,
     * who the provided response values. <code>attrName.length</code> should be equal to <code>val.length</code>.
     *
     * @param trialDefinition
     * @param args
     * @return number of participants matching
     */
    public int getCount(TrialDefinition trialDefinition, Map<String, Integer> args) {

        int count = 0;
        if (args.keySet().size() < 1) {
            return 0;
        }

        try {
            int listLength = args.keySet().size();
            String[] attrName = new String[listLength];
            int i = 0;
            for (String s : args.keySet()) {
                attrName[i++] = s;
            }

            String qString = "SELECT participant_id, parameter_name, value ";
            qString += "FROM RESPONSE WHERE trial_definition_id = ?  AND ( parameter_name = ? ";
            for (i = 1; i < listLength; i++) {
                qString += "OR parameter_name = ? ";
            }
            qString += ") ORDER BY participant_id ";

            PreparedStatement ps = conn.prepareStatement(qString);
            ps.setInt(1, getTrialDefinitionId(trialDefinition.getTrialName()));

            for (i = 0; i < listLength; i++) {
                ps.setString(i + 2, attrName[i]);
            }

            ps.executeQuery();

            ResultSet rs = ps.getResultSet();
            int tempCount = 0;
            int currPart = -1;
            if (rs != null) {
                while (rs.next()) {
                    if (currPart != rs.getInt("participant_id")) {
                        currPart = rs.getInt("participant_id");
                        tempCount = 0;
                    }

                    if (rs.getFloat("value") == args.get(rs.getString("parameter_name"))) {
                        tempCount++;
                    }

                    count += tempCount / listLength;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Returns a {@link StrategyStatistics} object that corresponds to this participant's treatment arm in this trial.
     *
     * @param trialDefinition The trial object for which the statics should be returned
     * @return The strategyStatistics object corresponding to this participant's treatment group for this trial.
     */
    public Statistics getStrategyStatistics(TrialDefinition trialDefinition) throws SQLException {
        Statistics strategyStats = new StrategyStatistics();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT statistic_name,value FROM STRATEGY_STATISTICS WHERE trial_definition_id = ?");
            ps.setInt(1, getTrialDefinitionId(trialDefinition.getTrialName()));
            ps.executeQuery();

            ResultSet rs = ps.getResultSet();

            while (rs != null && rs.next()) {
                strategyStats.putStatistic(rs.getString("statistic_name"), rs.getFloat("value"));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw e;
        }
        //logger.debug("map size is "+strategyStats.getAllNames().toString());
        return strategyStats;
    }

    /**
     * Update the statistics for this treatment arm, given the new patient.
     *
     * @param trialDefinition    The trial object for which the statics should be updated
     * @param participant        The enumeration index of the stratified group within the trial definitions
     *                           for which the statics should be updated.
     * @param strategyStatistics The strategy statics which are to be updated on the data base.
     * @param treatment          The treatment allocation arm that the patient have been assigned to.
     * @return Returns true if operation was successful.
     */
    public boolean update(TrialDefinition trialDefinition, Participant participant,
                          Statistics strategyStatistics, int treatment) throws SQLException {
        try {
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE STRATEGY_STATISTICS SET value = ? WHERE statistic_name = ? AND trial_definition_id = ? ");
            PreparedStatement selectStmt = conn.prepareStatement("SELECT id FROM TREATMENT WHERE trial_definition_id = ? AND name = ?");
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO PARTICIPANT(given_id, treatment_id) VALUES(?,?)");

            int trial_definition_id = getTrialDefinitionId(trialDefinition.getTrialName());

            selectStmt.setInt(1, trial_definition_id);
            selectStmt.setString(2, trialDefinition.getTreatments().get(treatment).getName());
            selectStmt.executeQuery();

            ResultSet rs = null;

            rs = selectStmt.getResultSet();
            if (!rs.next()) {
                return false;
            }

            int treatmentId = rs.getInt("id");
            insertStmt.setInt(1, participant.getId());
            insertStmt.setInt(2, treatmentId);
            insertStmt.executeUpdate();

            for (String s : strategyStatistics.getAllNames()) {
                updateStmt.clearParameters();
                updateStmt.setString(2, s);
                updateStmt.setFloat(1, strategyStatistics.getStatistic(s));
                updateStmt.setInt(3, trial_definition_id);
                updateStmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw e;
        }

    }

    public void setLifeGuideAPI(LifeGuideAPI lifeGuideAPI) {
        this.lifeGuideAPI = lifeGuideAPI;
    }

    /**
     * Return the details of the participant for the trial.
     *
     * @param id The numerical id of the participant required.
     * @return A participant object
     */
    public Participant getParticipant(int id) {
        return lifeGuideAPI.getParticipant(id);
    }

    /**
     * Queries the database for a given trial definition object. If found, the object is reconstructed and returned.
     *
     * @param name The name of the trial definition.
     * @return Returns a {@link TrialDefinition} object, if it exists in the database.
     */
    public TrialDefinition getTrialDefinition(String trialName) throws PersistenceException, InvalidTrialException {
        try {
            TrialDefinition definition = null;
            PreparedStatement definitionStmt = conn.prepareStatement("SELECT id,strategy_id,cluster_factors FROM INTERVENTION WHERE trial_name = ?");
            definitionStmt.setString(1, trialName);
            definitionStmt.executeQuery();

            ResultSet results = definitionStmt.getResultSet();
            if (results != null && results.next()) {
                int trialId = results.getInt("id");

                ArrayList<Treatment> treatments = getTreatments(trialId);
                ArrayList<Attribute> attributes = getAttributes(trialId);
                HashMap<String, Float> definitionParameters = getParameters(trialId);

                String[] cFString = results.getString("cluster_factors").split(",");
                int[] clusterFactors = new int[cFString.length];
                for (int i = 0; i < cFString.length; i++) {
                    clusterFactors[i] = Integer.parseInt(cFString[i]);
                }

                String strategyName = results.getString("strategy_id");
                Class<? extends Strategy> c;

                try {
                    c  = Class.forName(STRATEGY_CLASS_PACKAGE+strategyName).asSubclass(Strategy.class);
                }
                catch (ClassNotFoundException e) {
                    throw new InvalidTrialException("Allocation method not found: " + strategyName);
                }
                catch (ClassCastException e) {
                    throw new InvalidTrialException("Allocation method not found: " + strategyName);
                }

                definition = new TrialDefinition(
                        trialName,
                        c,
                        results.getString("strategy_id"),
                        definitionParameters,
                        attributes,
                        treatments,
                        clusterFactors);
            }
            return definition;
        }
        catch (SQLException e) {
            throw new PersistenceException("Could not load trial: " +trialName, e);
        }
    }

    /**
     * Returns the names of all the trial definitions currently registered in the database.
     *
     * @return Returns a {@link ArrayList} object with the names of the trial definition in the database.
     */
    public Set<String> getTrialDefinitionNames() {
        HashSet<String> names = new HashSet<String>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT trial_name FROM INTERVENTION");
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            while (rs != null && rs.next()) {
                names.add(rs.getString("trial_name"));
            }
        } catch (SQLException e) {
        }
        return names;
    }

    /**
     * Returns the names of all registered trials.
     *
     * @param trial_definition_id The name of the trial
     * @return An {@link ArrayList<String>} with the names of all registered trials.
     * @throws SQLException
     */
    private ArrayList<Treatment> getTreatments(int trial_definition_id) throws SQLException {
        PreparedStatement treatmentStmt = conn.prepareStatement("SELECT name,weight,max_participants,participant_limit FROM TREATMENT WHERE trial_definition_id = ?");
        treatmentStmt.setInt(1, trial_definition_id);
        treatmentStmt.execute();
        ArrayList<Treatment> treatments = new ArrayList<Treatment>();

        ResultSet treatmentResults = treatmentStmt.getResultSet();
        while (treatmentResults != null && treatmentResults.next()) {
            treatments.add(new Treatment(
                    treatmentResults.getString("name"),
                    treatmentResults.getInt("weight"),
                    treatmentResults.getInt("max_participants")));
        }

        return treatments;
    }

    /**
     * @param trialId
     * @return All the parameters specified at creation time for the given trial.
     * @throws SQLException
     */
    private HashMap<String, Float> getParameters(int trialId) throws SQLException {
        PreparedStatement paramStmt = conn.prepareStatement("SELECT name,value FROM INTERVENTION_PARAMS WHERE trial_definition_id = ?");
        paramStmt.setInt(1, trialId);
        paramStmt.execute();

        HashMap<String, Float> response = new HashMap<String, Float>();

        ResultSet group = paramStmt.getResultSet();
        while (group != null && group.next()) {
            response.put(group.getString("name"), group.getFloat("value"));
        }

        return response;
    }

    /**
     * Gets the attributes of a {@link TrialDefinition} for a given id. This is the id in the INTERVENTION table.
     *
     * @param trialId ID number of the trial whose parameters to fetch
     * @return A list of {@link Attribute} objects.
     * @throws SQLException
     */
    private ArrayList<Attribute> getAttributes(int trialId) throws SQLException {
        Attribute attr = null;
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        PreparedStatement attrStmt = conn.prepareStatement("SELECT id,attr_name,num_groups,weight, grouping_factor FROM ATTRIBUTE WHERE trial_definition_id=?");
        attrStmt.setInt(1, trialId);
        attrStmt.execute();

        ResultSet attrRes = attrStmt.getResultSet();
        while (attrRes != null && attrRes.next()) {
            attr = new Attribute(attrRes.getString("attr_name"),
                    attrRes.getInt("num_groups"),
                    attrRes.getFloat("weight"),
                    attrRes.getBoolean("grouping_factor"));
            attr.setRanges(getGroup(attrRes.getInt("id")));
            attrs.add(attr);
        }
        return attrs;
    }

    /**
     * Fetches all the Group objects for a given Attribute object from the database.
     *
     * @param attrIndex The id of the parent Attribute element for this Group object.
     * @return A list og {@link Group} objects.
     * @throws SQLException
     */
    private ArrayList<Group> getGroup(int attrIndex) throws SQLException {
        PreparedStatement grStmt = conn.prepareStatement("SELECT name,range_min,range_max FROM GROUPS WHERE attribute_id=?");
        grStmt.setInt(1, attrIndex);
        grStmt.executeQuery();
        ArrayList<Group> groups = new ArrayList<Group>();

        ResultSet group = grStmt.getResultSet();
        while (group != null && group.next()) {
            groups.add(new Group(group.getString("name"),
                    group.getInt("range_min"),
                    group.getInt("range_max")));
        }

        return groups;
    }

    /**
     * Register a new response to a question in the database.
     *
     * @param val           The value of a response
     * @param trialId       The id of the trial
     * @param paramName     The name of the parameter to which the response corresponds
     * @param participantId The id of the participant who answered
     * @return Returns the number of rows affected .
     * @throws SQLException
     */
    public int registerResponse(float val, int trialId, String paramName, int participantId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO RESPONSE (value, parameter_name, participant_id, trial_definition_id ) VALUES (?,?,?,?)");
        ps.setFloat(1, val);
        ps.setString(2, paramName);
        ps.setInt(3, participantId);
        ps.setInt(4, trialId);
        ps.executeUpdate();

        return ps.getUpdateCount();
    }

    /**
     * Why is this not built in java ? This turns int a = [1,2,3] to String b = "1,2,3";
     *
     * @param arr       Array of ints to join
     * @param delimiter The delimiter which will separate the fields.
     * @return A String of the joined array elements.
     */
    public String joinArray(int[] arr, String delimiter) {
        String res = "";
        if (arr.length > 0) {
            res = arr[0] + "";
            for (int i = 1; i < arr.length; i++) {
                res += delimiter + arr[i];
            }
        }
        return res;
    }
}
