package pa3.bolts;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;


public class WordCounter implements IRichBolt {

    Integer id;
    String name;
    Map<String, Integer> counters;
    private OutputCollector collector;

    /**
     * At the end of the spout (when the cluster is shutdown, we will show the
     * word counters.
     */
    @Override
    public void cleanup() {
	System.out.println("-- Word Counter [" + name + "-" + id + "] --");
	for (Map.Entry<String, Integer> entry : counters.entrySet()) {
	    System.out.println(entry.getKey() + ": " + entry.getValue());
	}
    }

    @Override
    public void execute(Tuple input) {
	String str = input.getString(0);
	/**
	 * If the word doesn't exist in the map, we will create this; if not,
	 * we will add 1
	 */
	if (!counters.containsKey(str)) {
	    counters.put(str, 1);
	} else {
	    Integer c = counters.get(str) + 1;
	    counters.put(str, c);
	}
	// Set the tuple as Acknowledge
	collector.ack(input);
    }

    /**
     * On create
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context,
	    OutputCollector collector) {
	this.counters = new HashMap<String, Integer>();
	this.collector = collector;
	this.name = context.getThisComponentId();
	this.id = context.getThisTaskId();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {}

    @Override
    public Map<String, Object> getComponentConfiguration() {
	return null;
    }
}
