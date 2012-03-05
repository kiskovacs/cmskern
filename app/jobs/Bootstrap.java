package jobs;

import models.ContentNode;
import models.ContentType;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * Bootstrap class which is executed at the very beginning
 * when the application is started.
 *
 * @author Niko Schmuck
 * @since 29.01.2012
 */
@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        Logger.info("Starting cmskern bootstrap job (Play env: %s)...", Play.id);

        if (ContentType.count() == 0) {
            // Fixtures.deleteAll();
            Logger.info("Importing bootstrap data ...");
            Fixtures.load("bootstrap-data.yml");
        }

        // ~~
        Logger.info("Ensure MongoDB indexes are set ...");
        ContentNode.createIndexes();
    }

}
