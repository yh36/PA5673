package pa3.spouts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;


public class WordReader implements IRichSpout {

    private SpoutOutputCollector collector;
    private FileReader fileReader;
    private boolean completed = false;
    private TopologyContext context;
    
    public boolean isDistributed() {
	return false;
    }

    @Override
    public void ack(Object msgId) {
	System.out.println("OK:" + msgId);
    }

    @Override
    public void fail(Object msgId) {
	System.out.println("FAIL:" + msgId);
    }

    @Override
    public void close() {}

    @Override
    public void activate() {}

    @Override
    public void deactivate() {}

    /**
     * The only thing that the methods will do is emit each file line
     */
    @Override
    public void nextTuple() {
	/**
	 * The nextuple it is called forever, so if we have been readed the file we
	 * will wait and then retrun
	 */
	if (completed) {
	    try {
		Thread.sleep(1);
	    } catch (InterruptedException e) {
		// Do nothing
	    }
	    return;
	}
	String str;
	// Open the reader
	BufferedReader reader = new BufferedReader(fileReader);
	try {
	    // Read all lines
	    while ((str = reader.readLine()) != null) {
		/**
		 * By each line emit a new value with the line as a tuple
		 */
		this.collector.emit(new Values(str), str);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Error reading tuple", e);
	} finally {
	    completed = true;
	}
    }

    /**
     * We will create the file and get the collector object
     */
    @Override
    public void open(Map conf, TopologyContext context,
	    SpoutOutputCollector collector) {
	try {
	    this.context = context;
	    this.fileReader = new FileReader(conf.get("wordsFile").toString());
	} catch (FileNotFoundException e) {
	    throw new RuntimeException("Error reading file[" + conf.get("wordsFile")
		    + "]");
	}
	this.collector = collector;
    }

    /**
     * Declare the output field "word"
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
	declarer.declare(new Fields("line"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
	return null;
    }
}
