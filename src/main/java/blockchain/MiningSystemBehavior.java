package blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

public class MiningSystemBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private final PoolRouter<ManagerBehavior.Command> poolRouter;
    private final ActorRef<ManagerBehavior.Command> managers;

    private MiningSystemBehavior(ActorContext<ManagerBehavior.Command> context) {
        super(context);
        this.poolRouter = Routers.pool(3,
                        Behaviors.supervise(ManagerBehavior.create())
                                .onFailure(SupervisorStrategy.restart()))
                .withRandomRouting();
        this.managers = getContext().spawn(poolRouter, "managerPool");
    }

    public static Behavior<ManagerBehavior.Command> create() {
        return Behaviors.setup(MiningSystemBehavior::new);
    }

    @Override
    public Receive<ManagerBehavior.Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    managers.tell(message);
                    return Behaviors.same();
                })
                .build();
    }
}
