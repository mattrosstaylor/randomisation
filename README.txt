Standalone Randomisation Module
===============================

Initialisation:

create user random@localhost identified by 'spork718';
create database random;
grant all on random to random;

Usage:
java -jar randomisation.jar register_trial trial_name trial_definition_path
java -jar randomisation.jar add_participant trial_name participant_identifier [user_data_path]
java -jar randomisation.jar get_allocation trial_name participant_identifier
