package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.app.LocalDBConnector;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class MassParticipantTest {

    public static void main(String[] args) {
        //30 150 600 1200 6000 12000
        double POPULATION_SIZE = 12000;
        int iterations = 1000;

        LocalDBConnector database = new LocalDBConnector();
        database.connect();

        TrialDefinition tDef = null;
        try {
            tDef = TrialLoader.loadTrial("src/test/resources/klapa.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        database.registerTrial(tDef);
        List<Participant> participants = new ArrayList<Participant>();

        for (int i = 0; i < POPULATION_SIZE; ++i) {
            Participant participant = ParticipantGenerator.generate(tDef);
            participants.add(participant);
            database.addParticipant(tDef, participant);
        }

        double[][] results = new double[tDef.getTreatmentCount()][iterations];
        for (int i = 0; i < iterations; i++) {
            for (Participant p : participants) {
                int alloc = 0;
                try {
                    alloc = Strategy.allocate(tDef.getTrialName(), p.getId(), database);
                } catch (AllocationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                ++results[alloc][i];
            }
            database.deleteTrial(tDef.getTrialName());
            database.registerTrial(tDef);
        }
        database.deleteTrial(tDef.getTrialName());
        for (int i = 0; i < iterations; ++i) {
            for (int j = 0; j < tDef.getTreatmentCount(); ++j)
                System.out.print((results[j][i]) / POPULATION_SIZE + ((j < tDef.getTreatmentCount() - 1) ? ", " : "\n"));
        }
    }

}
