import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import blockchain.ManagerBehavior;
import blockchain.WorkerBehavior;
import model.Block;
import model.HashResult;
import org.junit.jupiter.api.Test;
import utils.BlocksData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MiningTests {

    @Test
    void testMiningFailsIfNonceNotInRange() {
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());
        testActor.run(message);
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        assertEquals(0, logMessages.size());
    }

    @Test
    void testMiningPassesIfNonceIsInRange() {
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 930000, 5, testInbox.getRef());
        testActor.run(message);
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        assertEquals(1, logMessages.size());
        assertEquals("930724 : 00000094ca51e739f86a02e745de69d9943fc7c1c06b629d3f605aa077f71e74", logMessages.get(0).message());
    }

    @Test
    void testMessageReceivedIfNonceIsInRange() {
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 930000, 5, testInbox.getRef());
        testActor.run(message);

        HashResult expected = new HashResult();
        expected.foundAHash("00000094ca51e739f86a02e745de69d9943fc7c1c06b629d3f605aa077f71e74", 930724);
        testInbox.expectMessage(new ManagerBehavior.HashResultCommand(expected));
    }

    @Test
    void testNoMessageReceivedIfNotNonceInRange() {
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());
        testActor.run(message);

        assertFalse(testInbox.hasMessages());
    }


}
