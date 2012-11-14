import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * A simplisic but efficient HTTP server that accepts user inputs (nodes + url pattern)
 * and fetches the system metrics in "scatter-gather" fashion
 *
 */
public class WebApp {

    public static void main(String[] args) throws IOException {

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext( "/checkNodes", new WebAppHandler() );
        server.createContext( "/checkStats", new WebAppHandler() );
        server.start();
	System.out.println("Metric Server started listening on port " + port); 
        System.out.println("\tFollowing URLs accepted:\n");
        System.out.println("\t\t /checkNodes");
        System.out.println("\t\t /checkStats?seqNum=<n>");

    }

}

class WebAppHandler implements HttpHandler {

    private static long seqNumber = 0;

    private static HashMap<Long, MetricsChecker> checkerMap
        = new HashMap<Long, MetricsChecker>();

    private final int BUFSIZE = 4096;

    private synchronized long newSeqNumber() {

         if ( ++seqNumber == Long.MAX_VALUE ) {
             seqNumber = 1;
         }
         return seqNumber;
    }

    private synchronized void addChecker(MetricsChecker ck) {
	checkerMap.put(ck.getSeqNumber(), ck);
    }

    public void handle(HttpExchange exchange)
    throws IOException 
    {

        String httpMethod = exchange.getRequestMethod();
    
        if ( "Post".equalsIgnoreCase(httpMethod) ) {
            Headers respHeaders = exchange.getResponseHeaders();
	    String requestBody = readPostRequest(exchange);
            Map<String, String>
                parameters = parseRequestBody(requestBody);

            OutputStream respBody = exchange.getResponseBody(); 

            String[] nodes = null;
            String  urlPat = null;

            for( Map.Entry<String, String>
                entry : parameters.entrySet() ) 
            {
                if ( entry.getKey().equals("nodes") ) {
                    nodes = entry.getValue().split(",");
                }
                if ( entry.getKey().equals("urlPat") ) {
                    urlPat = entry.getValue();   
                } 
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>OK<br/>\n");
            if ( nodes != null && urlPat != null ) {
                MetricsChecker checker = new MetricsChecker(nodes, urlPat, newSeqNumber());
                addChecker(checker);
                sb.append("request received - session sequence number: " 
                          + checker.getSeqNumber() 
                          + "<br/>\n"
                         );
                Thread t = new Thread(checker);
                t.start();
            }
	    sb.append("</body></html>\n");

            String resp = sb.toString();
	    exchange.sendResponseHeaders(200, resp.length());
	    respBody.write(resp.getBytes());
            respBody.close();
        }  
        else if ( "Get".equalsIgnoreCase(httpMethod) ) {

            URI requestedUri = exchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            String[] parts = query.split("=");
        
            String key = parts[0];
            String val = parts[1];

            if ( !key.equals("seqNum") ) 
                return; 
 
            Long seqNum = Long.parseLong(val);

	    exchange.sendResponseHeaders(200, 0);
            OutputStream respBody = exchange.getResponseBody(); 
            MetricsChecker ck = checkerMap.get(seqNum);
	    if ( ck == null ) {
	        respBody.write(("invalid sequence number: " + val).getBytes());
            } else {         
	        respBody.write(ck.status().getBytes());
            }
            respBody.close();
        }
    }

    private String readPostRequest(HttpExchange exch)
    throws java.io.IOException
    {

        // read the query string from the request body
        StringBuilder sb = new StringBuilder();
        InputStream in = exch.getRequestBody();
        try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             byte buf[] = new byte[BUFSIZE];
             for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                 sb.append( new String(buf, 0, n) );
             }
             return sb.toString();
        } finally {
           in.close();
        }
    }

    private Map<String,String> parseRequestBody(String request) {

        Map<String,String> parms = new HashMap<String,String>();

        String[] fields = request.split("&");

        for (String field: fields) {

           String parts[] = field.split("=");

           if ( parts.length == 2 ) {
               String name = parts[0];
               String value = URLDecoder.decode(parts[1]);
               parms.put(name, value);
           }
        }
        return parms;
    }
}
