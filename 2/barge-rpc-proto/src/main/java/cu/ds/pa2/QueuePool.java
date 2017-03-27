package cu.ds.pa2;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.LinkedList;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.robotninjas.barge.NettyClusterConfig;
import org.robotninjas.barge.NettyRaftService;
import org.robotninjas.barge.NettyReplica;
import org.robotninjas.barge.RaftException;
import org.robotninjas.barge.StateMachine;
import org.robotninjas.barge.utils.Files;

import com.google.common.util.concurrent.SettableFuture;

/*
 * QueuePool is an implementation of a state machine replica. It is the real
 * object that executes the requests from application client on the server.
 */
public class QueuePool implements StateMachine {

	private HashMap<Integer, LinkedList<Integer> > qpool;
	private HashMap<Integer, Integer> id2lbl;
	private HashMap<Integer, Integer> lbl2id;
	
	private int id;
	
	/*
	 * Need in Raft replicas
	 */
	private int raftId;										// Raft replica ID
	private List<NettyReplica> replicas;	// Replica group
	private FTQueues ftqs;								// Reference to the raft manager
	private File logDir;									// Parent directory of log
	private NettyRaftService service;			// Object that provides raft protocol
	
	public QueuePool(int raftId, List<NettyReplica> replicas, FTQueues ftqs) {
		this.raftId = raftId;
		this.replicas = replicas;
		this.ftqs = ftqs;
		
		qpool = new HashMap<Integer, LinkedList<Integer> >();
		id2lbl = new HashMap<Integer, Integer>();
		lbl2id = new HashMap<Integer, Integer>();
		id = 0;
	}
	
	/*
	 * This is where a replica apply executions of log files.
	 * @see org.robotninjas.barge.StateMachine#applyOperation(java.nio.ByteBuffer)
	 */
	@Override
	public Object applyOperation(ByteBuffer bb) {
		int label, queue_id, item;
		Object ret = null;
		switch(bb.getInt(FTQueues.OP)) {
		case FTQueues.QCREATE:
			label = bb.getInt(FTQueues.ARG0);
			ret = new Integer(qCreate(label));
			
			break;
		case FTQueues.QID:
			queue_id = bb.getInt(FTQueues.ARG0);
			ret = new Integer(qId(queue_id));
			
			break;
			
		case FTQueues.QPUSH:
			queue_id = bb.getInt(FTQueues.ARG0);
			item = bb.getInt(FTQueues.ARG1);
			qPush(queue_id, item);
			
			break;
			
		case FTQueues.QPOP:
			queue_id = bb.getInt(FTQueues.ARG0);
			ret = new Integer(qPop(queue_id));
			
			break;
			
		case FTQueues.QTOP:
			queue_id = bb.getInt(FTQueues.ARG0);
			ret = new Integer(qTop(queue_id));
			
			break;
			
		case FTQueues.QSIZE:
			queue_id = bb.getInt(FTQueues.ARG0);
			ret = new Integer(qSize(queue_id));
			
			break;
		}		
		return ret;
	}

	/*
	 * Creates a queue with the specified label, and a queue ID will be assigned
	 * automatically.
	 * Operation code: QCREATE[1]
	 */
	public int qCreate (int label) {
		if (lbl2id.containsKey(label)) {
			return lbl2id.get(label);
		}
		
		lbl2id.put(label, id);
		id2lbl.put(id, label);
		qpool.put(id, new LinkedList<Integer>());
		id++;
		return lbl2id.get(label);
	}
	
	/*
	 * Return the queue id for the given queue's label.
	 * If the given label isn't found, it will return -1.
	 * Operation code: QCREATE[2]
	 */
	public int qId (int label) {
		if (lbl2id.containsKey(label)) {
			return lbl2id.get(label);
		} else {
			return (-1);
		}
	}
	
	/*
	 * Put the item at the end of the queue
	 * Operation code: QPUSH[3]
	 */
	public void qPush (int queue_id, int item) {
		if (!qpool.containsKey(queue_id)) {
			return;
		}
		qpool.get(queue_id).addLast(item);
	}
	
	/*
	 * Remove an item from the head of the queue.
	 * If the queue doesn't exist or is empty, return -1.
	 * Operation code: QPOP[4]
	 */
	public int qPop (int queue_id) {
		if (!qpool.containsKey(queue_id) || qpool.get(queue_id).isEmpty()) {
			return -1;
		}
		return qpool.get(queue_id).removeFirst();
		
	}
	
	/*
	 * Peek the first element of the queue.
	 * If the queue doesn't exist or is empty, return -1.
	 * Operation code: QTOP[5]
	 */
	public int qTop (int queue_id) {
		if (!qpool.containsKey(queue_id) || qpool.get(queue_id).isEmpty()) {
			return -1;
		}
		return qpool.get(queue_id).getFirst();
	}
	
	/*
	 * Return the capacity of the queue.
	 * If the specified queue can't be found, return -1.
	 * Operation code: QSIZE[6]
	 */
	public int qSize (int queue_id) {
		if (!qpool.containsKey(queue_id)) {
			return (-1);
		}
		return qpool.get(queue_id).size();
	}
	
	/*
	 * Combine the local and remote replicas, create a log file, setup timeout
	 * value, and launch this replica's raft service.
	 */
	public void start() {
		int clusSz = replicas.size();
		NettyReplica[] confRemote = new NettyReplica[clusSz - 1];
		
		for (int i = 0; i < clusSz - 1; i++) {
			// Get the other remote replicas
			confRemote[i] = replicas.get((raftId + i + 1) % clusSz);
		}
		
		// Create cluster configuration for local and remote replicas
		NettyClusterConfig config = NettyClusterConfig.from(replicas.get(raftId % clusSz), confRemote);
		
		// Create raft service with cluster configuration, log directory, and transistionListener
		service = NettyRaftService.newBuilder(config)
					.logDir(logDir)
					.timeout(500)
					.transitionListener(ftqs)
					.build(this);
		service.startAsync().awaitRunning();
	}
	
	/*
	 * Stop this replica
	 */
	public void stop() {
		service.stopAsync().awaitTerminated();
	}
	
	/*
	 * Create log directory for this replica
	 */
	public File makeLogDirectory(File parentDirectory) throws IOException {		
		logDir = new File(parentDirectory, "log" + raftId);
		
		if (logDir.exists()) {
			Files.delete(logDir);
		}
				
		if (!logDir.exists() && !logDir.mkdirs()) {
			throw new IllegalStateException("Cannot create log directory " + logDir);
		}
		
		return logDir;
	}
	
	/*
	 * Delete log directory
	 */
	public void deleteDirectory() {
		Files.delete(logDir);
	}
	
	/*
	 * This function should be called by the leader, which will commit operation
	 * to the other followers.
	 */
	public int commit(byte[] bytes) throws RaftException, InterruptedException, ExecutionException {
		Object ret = service.commit(bytes);
		@SuppressWarnings("unchecked")
		SettableFuture<Integer> result = (SettableFuture<Integer>) ret;
		if (result.get() == null)
			return -100;
		return result.get();
	}
	
	public boolean isLeader() {
		return service.isLeader();
	}
}


