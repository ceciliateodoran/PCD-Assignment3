package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import distributed.messages.ValueMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarrackRoutes {

    private final static Logger log = LoggerFactory.getLogger(UserRoutes.class);
    private final ActorRef<UserRegistry.Command> userRegistryActor;
    private final Duration askTimeout;
    private final Scheduler scheduler;

    public BarrackRoutes(ActorSystem<?> system, ActorRef<ValueMsg> userRegistryActor) {
        this.userRegistryActor = userRegistryActor;
        this.scheduler = system.scheduler();
        this.askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
    }

    private CompletionStage<UserRegistry.GetUserResponse> getUser(String name) {
        return AskPattern.ask(userRegistryActor, ref -> new UserRegistry.GetUser(name, ref), askTimeout, scheduler);
    }

    private CompletionStage<UserRegistry.ActionPerformed> deleteUser(String name) {
        return AskPattern.ask(userRegistryActor, ref -> new UserRegistry.DeleteUser(name, ref), askTimeout, scheduler);
    }

    private CompletionStage<UserRegistry.Users> getUsers() {
        return AskPattern.ask(userRegistryActor, UserRegistry.GetUsers::new, askTimeout, scheduler);
    }

    private CompletionStage<UserRegistry.ActionPerformed> createUser(User user) {
        return AskPattern.ask(userRegistryActor, ref -> new UserRegistry.CreateUser(user, ref), askTimeout, scheduler);
    }

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    //#all-routes
    public Route userRoutes() {
        return pathPrefix("users", () ->
                concat(
                        //#users-get-delete
                        pathEnd(() ->
                                concat(
                                        get(() ->
                                                onSuccess(getUsers(),
                                                        users -> complete(StatusCodes.OK, users, Jackson.marshaller())
                                                )
                                        ),
                                        post(() ->
                                                entity(
                                                        Jackson.unmarshaller(User.class),
                                                        user ->
                                                                onSuccess(createUser(user), performed -> {
                                                                    log.info("Create result: {}", performed.description);
                                                                    return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                                                })
                                                )
                                        )
                                )
                        ),
                        //#users-get-delete
                        //#users-get-post
                        path(PathMatchers.segment(), (String name) ->
                                concat(
                                        get(() ->
                                                        //#retrieve-user-info
                                                        rejectEmptyResponse(() ->
                                                                onSuccess(getUser(name), performed ->
                                                                        complete(StatusCodes.OK, performed.maybeUser, Jackson.marshaller())
                                                                )
                                                        )
                                                //#retrieve-user-info
                                        ),
                                        delete(() ->
                                                        //#users-delete-logic
                                                        onSuccess(deleteUser(name), performed -> {
                                                                    log.info("Delete result: {}", performed.description);
                                                                    return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                                                }
                                                        )
                                                //#users-delete-logic
                                        )
                                )
                        )
                        //#users-get-post
                )
        );
    }

}
