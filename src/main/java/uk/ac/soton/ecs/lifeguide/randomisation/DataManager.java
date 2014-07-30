package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.*;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

public class DataManager {

	private static SessionFactory factory;
	private Session session;

	public DataManager(String whatev, String whatevs, String seriously, String whatever) throws PersistenceException {
	
		try {
			factory = new AnnotationConfiguration().
				configure().
				addPackage("uk.ac.soton.ecs.lifeguide.randomisation"). //add package if used.
				addAnnotatedClass(Trial.class).
				addAnnotatedClass(Participant.class).
				addAnnotatedClass(Arm.class).
				addAnnotatedClass(Attribute.class).
				addAnnotatedClass(Grouping.class).
				buildSessionFactory();
		}
		catch (Exception e) { 
			throw new PersistenceException("Failed to create sessionFactory object.", e); 
		}
	}

	public void connect() {
		session = factory.openSession();
	}

	public void disconnect() {
		session.close();
	}

	public Trial getTrial(String name) {
		Query q = session.createQuery("from Trial where name = :name");
		q.setParameter("name", name);
		List<Trial> result = q.list();
		if (result.size() > 0) {
			return result.get(0);
		}
		else {
			return null;
		}
	}

	public void registerTrial(Trial trial) throws PersistenceException, InvalidTrialException {
		trial.setStatistics(Strategy.create(trial.getStrategy()).getStoredParametersImplementation(trial));

		Transaction tx = null;
		Integer generatedId = null;
		try {
			tx = session.beginTransaction();
			generatedId = (Integer) session.save(trial);
			tx.commit();
		}
		catch (HibernateException e) {
			session.close();
			session = DataManager.factory.openSession();
			throw new PersistenceException("Could not register trial", e);
		}
	}

	public Participant getParticipant(String trialName, String identifier) {
		Query q = session.createQuery("from Participant where identifier= :identifier and trial.name = :trialName");
		q.setParameter("identifier", identifier);
		q.setParameter("trialName", trialName);
		List<Participant> result = q.list();
		if (result.size() > 0) {
			return result.get(0);
		}
		else {
			return null;
		}
	}

	public boolean update(Trial trial, Participant participant, Arm arm) throws PersistenceException {
		participant.setAllocatedArm(arm);
		trial.addParticipant(participant);

		Transaction tx = null;
		Integer generatedId = null;
		try {
			tx = session.beginTransaction();
			generatedId = (Integer) session.save(trial);
			tx.commit();
		}
		catch (HibernateException e) {
			session.close();
			session = DataManager.factory.openSession();
			throw new PersistenceException("Could not allocate", e);
		}
		return true;
	}
}