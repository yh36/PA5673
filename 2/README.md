
This is programming assignment two from Distributed System course at University of Colorado Boulder
Team members: Yi Hou and Yu-Chih Cho

An implementation of the [Raft Consensus Protocol].<br/>
[1]: http://raftconsensus.github.io/ <br/>
[2]: https://github.com/mgodave/barge

We implemented the Raft Protocol with Java from barge https://github.com/mgodave/barge, and created a distributed, fault-tolerant queue data structure, FTQueue that exports the following operations to the clients:

`int qCreate (int label); //Creates a new queue of integers; associates this queue with label and returns a queue id (int)<br/>
int qId (int label); //returns queue id of the queue associated with label<br/>
void qPush (int queue_id, int item); // enters item in the queue<br/>
int qPop (int queue_id); // removes an item from the queue and returns it <br/>
int qTop (int queue_id); // returns the value of the first element in the queue <br/>
int qSize (int queue_id); // returns the number of items in the queue`

##Compile/Run

##What works 

##What doesn't works 
