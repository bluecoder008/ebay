import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.Calendar;
import java.sql.Timestamp;

/**
 * This class represents the handling of a single user request in a 'scatter-gather'
 * fashion. It is running on its own thread, and may also spawn off threads
 * to talk to each end node in question.
 *
 */
public class MetricsChecker implements Runnable {

    private long _sn;
    private String[] _nodes;
    private String _urlPat;
    private int _completed;

    private String _result;
    private MetricsDao _dao;

    /**
     * ctor
     * \param nodes The list of nodes to query metric against
     * \param urlPat The url pattern for the HTTP endpoint
     */
    MetricsChecker(String[] nodes, String urlPat, long sn ) 
    {
        _sn     = sn;
        _urlPat = urlPat;
        _nodes  = nodes;
        _dao = new MetricsDaoImpl();
    }

    /**
     * The execution routine (in separate thread) for the MetricsChecker
     * This run creates one "SimpleHttpClient" for each node (scatter),
     * Then waits for each client to complete and assembles the responses (gather)
     */
    public void run() {

        _completed = 0;
        SimpleHttpClient[] clients = new SimpleHttpClient[_nodes.length];
        int n=0;
        try {
        
            for(String node : _nodes ) {  
                String url = _urlPat.replaceFirst("<node>", node);
                clients[n] = new SimpleHttpClient(url, this);
                clients[n++].start();
            }
        } catch(MalformedURLException malFormedURL) {
	    System.err.println("MalformedURLException caught.");
        } catch(IOException ioExcept) {
	    System.err.println("IOException caught.");
        }

        // Wait for all sub-queries to complete
        while( _completed < _nodes.length ) {
            Thread.yield();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for(n=0; n < _nodes.length; n++) {

	   sb.append("\t\"" + _nodes[n] + "\" : " +
                     clients[n].result() );
           if ( n < _nodes.length - 1 ) {
               sb.append("\t,\n");
           }
        }
        sb.append("}\n");
        _result = sb.toString();

        // write to the backend db store
        _dao.storeMetrics(_result,
              new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()) );
    }   

    /**
     * This routine allows user to check the status for current query
     *
     */
    public String status() {

        StringBuilder sb = new StringBuilder();
        
        if ( _completed < _nodes.length ) {

            int percent = _completed / _nodes.length * 100;
            sb.append("In progress ... " + percent + "% completed ..");    
        } else {
            sb.append("Query completed:\n" + _result);
        }
        return sb.toString();
    }

    public long getSeqNumber() {
        return _sn;
    }

    /**
     * This routine is called by sub-query (SimpleSHttpClient) to update
     * the count of completed nodes
     * 
     * It is synchronized since it needs to be thread-safe (called from multiple threads).
     *
     */
    synchronized public void increment() {
        _completed++;
    }
}

