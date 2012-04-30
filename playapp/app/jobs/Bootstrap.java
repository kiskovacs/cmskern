package jobs;

import models.ContentNode;
import models.ContentType;
import models.Role;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;
import utils.MongoDbUtils;

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
        Logger.info("     ... using database '%s' on MongoDB: %s", MongoDbUtils.getDBName(), MongoDbUtils.getDBServers());

        if (ContentType.count() == 0) {
            Logger.info("Importing bootstrap content types ...");
            Fixtures.loadModels("bootstrap-contenttypes.yml");
        }
        if (Role.count() == 0) {
            Logger.info("Importing bootstrap roles and users ...");
            Fixtures.loadModels("bootstrap-users_and_roles.yml");
        }

        // ~~
        Logger.info("Ensure MongoDB indexes are set ...");
        ContentNode.createIndexes();
    }

}
