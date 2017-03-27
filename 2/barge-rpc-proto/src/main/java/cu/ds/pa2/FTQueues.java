package cu.ds.pa2;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.robotninjas.barge.NettyReplica;
import org.robotninjas.barge.RaftException;
import org.robotninjas.barge.state.Raft;
import org.robotninjas.barge.state.Raft.StateType;
import org.robotninjas.barge.state.StateTransitionListener;
import org.robotninjas.barge.utils.Prober;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/*
 * FTQueues class is responsible for managing the raft cluster. It will create
 * a cluster of 5 QueuePool replicas, start each replica and transfer requests
 * and responses between application layer and middle layer. All methods here
 * are raft APIs provided to application layer. 
 */
public class FTQueues implements StateTransitionListener {
	
	private final int numOfRep = 5;
	private final List<NettyReplica> replicas;
	private final List<QueuePool> qps;
	private final File parent;
	private final Map<Raft, StateType> states = Maps.newConcurrentMap();
	private byte[] ops = new byte[192];		//[0, 64): operation_code, [64, 128): arg0, [128, 192): arg1
	
	public static final int OP = 0, ARG0 = 64, ARG1 = 128;
	public static final int QCREATE = 1, QID = 2, QPUSH = 3, QPOP = 4, QTOP = 5, QSIZE = 6;
	
	/*
	 * Create 5 local host replicas for fault tolerant queue.
	 */
	public FTQueues(File parent) {
		this.parent = parent;
		
		replicas = Lists.newArrayList();
		qps = Lists.newArrayList();
		
		for (int i = 10001; i <= (10000 + numOfRep); i++) {
			replicas.add(NettyReplica.fromString("localhost:" + i));
			qps.add(new QueuePool(i - 10000, replicas, this));
		}
	}
	
	/*
	 * Start raft cluster replication
	 */
	public void startRaft() throws Throwable {
		for (QueuePool qp : qps) {
			qp.makeLogDirectory(parent);
			qp.start();
		}
	}
	
	/*
	 * Stop raft cluster replication
	 */
	public void stopRaft() {
		for (QueuePool qp : qps) {
			qp.stop();
			qp.deleteDirectory();
		}
	}
	
	/*
	 * Stop two of five replicas
	 */
	public void stop2() {
		qps.get(1).stop();
		qps.get(2).stop();
	}
	
	/*
	 * Create a FTQueue with specified label
	 */
	public int commitCreate(int label) throws RaftException, InterruptedException, ExecutionException {
		ByteBuffer bb = ByteBuffer.wrap(ops);
		bb.clear();
		bb.putInt(OP, QCREATE).putInt(ARG0, label);
		return getLeader().get().commit(ops);
	}
	
	/*
	 * Return the queue ID for the given queue label
	 */
	public int commitId(int label) throws RaftException, InterruptedException, ExecutionException {
		ByteBuffer bb = ByteBuffer.wrap(ops);
		bb.clear();
		bb.putInt(OP, QID).putInt(ARG0, label);
		return getLeader().get().commit(ops);
	}
	
	/*
	 * Put value into FTQueue
	 */
	public void commitPush(int queue_id, int item) throws RaftException, InterruptedException, ExecutionException {
		ByteBuffer bb = ByteBuffer.wrap(ops);
		bb.putInt(OP, QPUSH).putInt(ARG0, queue_id).putInt(ARG1, item);
		getLeader().get().commit(ops);
	}
	
	/*
	 * Pop the first element from the Queue
	 */
	public int commitPop(int queue_id) throws RaftException, InterruptedException, ExecutionException {
		ByteBuffer bb = ByteBuffer.wrap(ops);
		bb.clear();
		bb.putInt(OP, QPOP).putInt(ARG0, queue_id);
		return getLeader().get().commit(ops);
	}
	
	/*
	 * Peek the top value of the queue
	 */
	public int commitTop(int queue_id) throws RaftException, InterruptedException, ExecutionException {
		ByteBuffer bb = ByteBuffer.wrap(ops);
		bb.clear();
		bb.putInt(OP, QTOP).putInt(ARG0, queue_id);
		return getLeader().get().commit(ops);
	}
	
	/*
	 * Get the capacity of the queue 
	 */
	public int commitSize(int queue_id) throws RaftException, InterruptedException, ExecutionException {
		ByteBuffer bb = ByteBuffer.wrap(ops);
		bb.clear();
		bb.putInt(OP, QSIZE).putInt(ARG0, queue_id);
		return getLeader().get().commit(ops);
	}
	
	/*
	 * Return the leader of the cluster
	 */
	private Optional<QueuePool> getLeader() {
		for (QueuePool qp : qps) {
			if (qp.isLeader()) {
				return Optional.of(qp);
			}
		}
		return Optional.absent();
	}
	
	/*
	 * Wait for all replicas to launch and a leader has been elected
	 */
	public void waitForLeaderElection() throws InterruptedException {
		new Prober(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return thereIsOneLeader();
			}
		}).probe(10000);
	}
	
	/*
	 * A legal leader exists
	 */
	private Boolean thereIsOneLeader() {
		int numOfLeaders = 0;
		int numOfFollowers = 0;
		
		for (StateType state : states.values()) {
			switch (state) {
			case LEADER:
				numOfLeaders++;
				break;
			
			case FOLLOWER:
				numOfFollowers++;
				break;
				
			default:
				break;
			}
		}
		return (numOfLeaders == 1) && (numOfLeaders + numOfFollowers == replicas.size());
	}

	@Override
	public void changeState(Raft context, StateType from, StateType to) {
		states.put(context, to);
	}

	@Override
	public void invalidTransition(Raft arg0, StateType arg1, StateType arg2) {
		// IGNORED		
	}

	@Override
	public void stop(Raft arg0) {
		// IGNORED		
	}

}
