
This is programming assignment two from Distributed System course at University of Colorado Boulder<br/>
Team members: Yi Hou and Yu-Chih Cho

An implementation of the [Raft Consensus Protocol].<br/>
[1]: http://raftconsensus.github.io/ <br/>
[2]: https://github.com/mgodave/barge

We implemented the Raft Protocol with Java based on barge https://github.com/mgodave/barge, and created a distributed, fault-tolerant queue data structure, FTQueue that exports the following operations to the clients:

`int qCreate (int label); //Creates a new queue of integers; associates this queue with label and returns a queue id (int)`<br/>
`int qId (int label); //returns queue id of the queue associated with label`<br/>
`void qPush (int queue_id, int item); // enters item in the queue`<br/>
`int qPop (int queue_id); // removes an item from the queue and returns it `<br/>
`int qTop (int queue_id); // returns the value of the first element in the queue `<br/>
`int qSize (int queue_id); // returns the number of items in the queue`<br/>

Compile/Run
======
step 1. find the FTQueue implementation under barge-rpc-proto/src/main/java/cu/ds/pa2/ <br/>
step 2. build maven project <br/>
step 3. run java application <br/>
step 4. see data structure operation <br/>
i.e. Client can ask server (replicas) to push item into queue. On the other hand, when servers pop the item from the queue, it can be sent and printed on the client side<br/>

Result
======
We run the five replicas in the local host<br/>
localhost:10001<br/>
localhost:10002<br/>
localhost:10003<br/>
localhost:10004<br/>
localhost:10005<br/>
It can be shown that if we kill two of replicas, the server can still run with the correct output. <br/>

`public static void main(String...argv) throws Throwable {`
		`FTQueues ftq = new FTQueues(parent);`
		`ftq.startRaft();`
		`ftq.waitForLeaderElection();
    
		int label_1 = 22, label_2 = 23;
		int qid_1 = ftq.commitCreate(label_1);//create queue 1
		int qid_2 = ftq.commitCreate(label_2);//create queue 2
    
		ftq.commitPush(qid_1, 71);//push item 71 into queue 1 
		ftq.commitPush(qid_1, 76);//push item 76 into queue 1
		ftq.commitPush(qid_2, 33);//push item 33 into queue 2
		System.out.println("Pop qid_1: " + ftq.commitPop(qid_1));//client get the item that pop from the queue 1
		System.out.println("Queue ID of queue 2:" + ftq.commitId(label_2));//combine the queue id with label 
		ftq.stop2();//if we stop two of replicas
		System.out.println("Top of queue 2: " + ftq.commitTop(qid_2));//client should still get the first element from queue 2 correctly
		System.out.println("Pop queue 2: " + ftq.commitPop(qid_2));		
		System.out.println("Size of qid_1: " + ftq.commitSize(qid_1));	
		ftq.stopRaft();
	}`
  
  
What works 
======
Leader Election <br/>
Log Replication<br/>

What doesn't works 
======
Log Compaction<br/>
Dynamic Membership<br/>
