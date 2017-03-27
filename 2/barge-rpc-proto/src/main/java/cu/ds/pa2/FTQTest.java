package cu.ds.pa2;

import java.io.File;

/*
 * This test code is actually the application layer. It will create a FTQueue
 * raft cluster, and do the following test operation.
 *  1. Create queue_1 with label 22
 *  2. Create queue_2 with label 23
 *  3. Push 71 to queue_1
 *  4. Push 76 to queue_1
 *  5. Push 33 to queue_2
 *  6. Pop a value from queue_1									: expected 71
 *  7. Return the queue_id for label 23					: expected 1
 *  8. Stop 2 replicas to simulate node failure
 *  9. Return the first value of queue_2				: expected 33
 * 10. Pop the value from queue_2								: expected 33
 * 11. Return the size of queue_1								: expected 1
 */
public class FTQTest {
	
	private static final File parent = new File(System.getProperty("basedir", "."), "FTQTest");
	
	public static void main(String...argv) throws Throwable {
		FTQueues ftq = new FTQueues(parent);
		ftq.startRaft();
		ftq.waitForLeaderElection();
		int label_1 = 22, label_2 = 23;
		int qid_1 = ftq.commitCreate(label_1);	// 1
		int qid_2 = ftq.commitCreate(label_2);	// 2
		ftq.commitPush(qid_1, 71);							// 3
		ftq.commitPush(qid_1, 76);							// 4
		ftq.commitPush(qid_2, 33);							// 5
		System.out.println("CCCCCCCCCCCCCCCCCCCCCCC--Pop qid_1--CCCCCCCCCCCCCCCCCCCCCCCCC: " + ftq.commitPop(qid_1));		// 6
		System.out.println("CCCCCCCCCCCCCCCCCC--Queue ID of queue 2--CCCCCCCCCCCCCCCCCCCC: " + ftq.commitId(label_2));	// 7
		ftq.stop2();														// 8
		System.out.println("CCCCCCCCCCCCCCCCCCCC--Top of queue 2--CCCCCCCCCCCCCCCCCCCCCCC: " + ftq.commitTop(qid_2));		// 9
		System.out.println("CCCCCCCCCCCCCCCCCCCCCC--Pop queue 2--CCCCCCCCCCCCCCCCCCCCCCCC: " + ftq.commitPop(qid_2));		// 10
		System.out.println("CCCCCCCCCCCCCCCCCCCCC--Size of qid_1--CCCCCCCCCCCCCCCCCCCCCCC: " + ftq.commitSize(qid_1));	// 11
//		Thread.sleep(100000);
		ftq.stopRaft();
	}
}
