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

	// public static void main(String[] args) {

	// 	try {
	// 		factory = new AnnotationConfiguration().
	// 			configure().
	// 			addPackage("uk.ac.soton.ecs.lifeguide.randomisation"). //add package if used.
	// 			addAnnotatedClass(Trial.class).
	// 			addAnnotatedClass(Arm.class).
	// 			addAnnotatedClass(Attribute.class).
	// 			addAnnotatedClass(Grouping.class).
	// 			buildSessionFactory();
	// 	}
	// 	catch (Throwable ex) { 
	// 		System.err.println("Failed to create sessionFactory object." + ex);
	// 		throw new ExceptionInInitializerError(ex); 
	// 	}


	// 	Session session = factory.openSession();
	// 	Transaction tx = null;
	// 	Integer generatedId = null;
	// 	try {
	// 		tx = session.beginTransaction();
	// 		Trial trial = new Trial();
	// 		trial.setName("Fishing");

	// 		Arm t1 = new Arm();
	// 		t1.setName("control");
	// 		Arm t2 = new Arm();
	// 		t2.setName("trial");

	// 		trial.addArm(t1);
	// 		trial.addArm(t2);

	// 		session.save(t1);
	// 		session.save(t2);

	// 		trial.setDefaultArm(t2);

	// 		trial.getParameters().put("fish-type", "carp");
	// 		trial.getParameters().put("fish-size", "really big");
	// 		trial.getStatistics().put("fish-caught", 0f);
	// 		trial.getStatistics().put("fish-escaped", 40239f);

	// 		trial.setStrategy("hide in the bushes until they come by and then throw rocks at them");

	// 		generatedId = (Integer) session.save(trial);
	// 		tx.commit();
	// 	}
	// 	catch (HibernateException e) {
	// 		e.printStackTrace();
	// 	}
	// 	finally {
	// 		session.close();
	// 	}
	//}

	public DataManager(String whatev, String whatevs, String seriously, String whatever) throws PersistenceException {
	
		try {
			factory = new AnnotationConfiguration().
				configure().
				addPackage("uk.ac.soton.ecs.lifeguide.randomisation"). //add package if used.
				addAnnotatedClass(Trial.class).
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

	public void registerTrial(Trial trial) throws PersistenceException {
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

	public Participant getParticipant(int fuckYou) {
		return null;
	}

	public boolean update(Trial trial, Participant participant, Map<String, Float> strategyStatistics, int treatment) throws PersistenceException {
		return true;
	}
}