package uk.ac.soton.ecs.lifeguide.randomisation;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;

/**
 * An object that provides basic data retrieval and storage capabilities for {@link TrialDefinition}
 * and {@link Strategy}. The interface allows abstraction of the {@link Strategy} interaction with the actual
 * method of storage and retrieval of the data.
 *
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see Strategy
 * @see TrialDefinition
 * @see Participant
 * @since 1.7
 */

public interface DBConnector {

	/**
	 * Attempts to instantiates a connection to the data resource.
	 * A useful abstraction for remote storage such as data bases or access trough the web.
	 *
	 * @throws PersistenceException if there was a problem connecting
	 */
	public void connect() throws PersistenceException;

	/**
	 * Attempts to close the connection to the data resource.
	 * Can also be used for releasing any resources required for the connection.
	 *
	 * @throws PersistenceException if there was a problem
	 */
	public void disconnect() throws PersistenceException;

	/**
	 * Registers the given trial definition object on the data resource.
	 * If successful this method should guarantee that
	 * {@link #trialExists(TrialDefinition)} returns <code>true</code>.
	 *
	 * @param trialDefinition The trial definition to be registered.
	 * @throws PersistenceException if the trial cannot be registered
	 */
	public void registerTrial(TrialDefinition trialDefinition) throws PersistenceException;

	/**
	 * @param trialDefinition The object whose existence on the data resource to be checked.
	 * @return <code>true</code> if the TrialDefinition is present in the data source.
	 *         <code>false</code> otherwise
	 */
	public boolean trialExists(TrialDefinition trialDefinition);

	/**
	 * Retrieves the number of participants in a given trial, specified by the {@link TrialDefinition} parameter,
	 * who the provided response values. <code>attrName.length</code> should be equal to <code>val.length</code>.
	 *
	 * @param trialDefinition
	 * @param args
	 * @return number of participants matching
	 */
	// mrt - this is only used in unit testing
//	public int getCount(TrialDefinition trialDefinition, Map<String, Integer> args);

	/**
	 * Retrieves the {@link StrategyStatistics} object for a given {@link TrialDefinition} and the number of
	 * stratified group. The stratified group number is required as every stratified group
	 * have its own statistics.
	 *
	 * @param trialDefinition The trial object for which the statics should be returned
	 * @return A {@link StrategyStatistics} object for the given group.
	 * @throws SQLException if something on the database side goes wrong.
	 */
	public Statistics getStrategyStatistics(TrialDefinition trialDefinition) throws SQLException;

	/**
	 * @param trialDefinition    The trial object for which the statics should be updated
	 * @param participant        The enumeration index of the stratified group within the trial definitions
	 *                           for which the statics should be updated.
	 * @param strategyStatistics The strategy statics which are to be updated on the data base.
	 * @param treatment          The treatment allocation arm that the patient have been assigned to.
	 * @return A {@link StrategyStatistics} object for the given group.
	 * @throws SQLException if something on the database side goes wrong.
	 */
	public boolean update(TrialDefinition trialDefinition, Participant participant, Statistics strategyStatistics, int treatment) throws SQLException;

	/**
	 * The method is intended for decoupling the DBConnector from the LifeGuideAPI.
	 *
	 * @param lifeGuideAPI Exact implementation of the LifeGuideAPI interface.
	 */
	public void setLifeGuideAPI(LifeGuideAPI lifeGuideAPI);

	/**
	 * This method should use the internal LifeGuideAPI object to retrieve the {@link Participant} object.
	 *
	 * @param id The numerical id of the participant required.
	 * @return A {@link Participant} object for the given id.
	 */
	public Participant getParticipant(int id);

	/**
	 * Returns the {@link TrialDefinition} object just from the name it has been registered with
	 *
	 * @param name The name of the trial
	 * @return The object representing the trial definition.
	 * @throws SQLException           if something on the database side is going wrong.
	 * @throws ClassNotFoundException if the {@link Strategy} class provided in the {@link TrialDefinition} is not found.
	 */
	public TrialDefinition getTrialDefinition(String name) throws PersistenceException, InvalidTrialException ;

	/**
	 * Returns an Set<String> object with the names of all the definitions.
	 *
	 * @return An ArrayList containing the String names for all the TrialDefinitions in the database
	 */
	public Set<String> getTrialDefinitionNames();
}
