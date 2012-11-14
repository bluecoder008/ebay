import java.net.*;
import java.io.*;


/**
 * This class represents the handling of a simple HTTP client functionality
 *
 * <p>The instances of this class also run in their own threads.
 *
 */
public class SimpleHttpClient extends Thread {

    private URLConnection _conn = null;
    private String _urlStr = null;
    private URL _url = null;
    private String _result = null;
    private MetricsChecker _checker;

    /**
     * ctor
     * \param urlPat The url pattern for the HTTP endpoint
     * \param checker The MetricsChecker object to notify of the results
     *
     */
    public SimpleHttpClient(String urlStr, MetricsChecker checker)
    throws MalformedURLException, IOException
    {
        _urlStr  = urlStr;
        _checker = checker;
    }

    public String result() 
    {
        return _result;
    }

    public void run() 
    {
        StringBuilder sb = new StringBuilder();
        try {
            _url = new URL(_urlStr);
            _conn = _url.openConnection();
            BufferedReader in = 
                new BufferedReader(
                    new InputStreamReader(_conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
                sb.append(inputLine + "\n");
            in.close();
        } catch (Exception ex) {
             ex.printStackTrace();
        }
        if ( sb.toString().length() > 0 ) {
            _result = sb.toString();
            if ( _checker != null ) 
                _checker.increment();
        }
    }

    public static void main(String[] args) 
    throws IOException, InterruptedException
    {
        SimpleHttpClient client = new SimpleHttpClient("http://localhost/metric",null);
        client.start();
        client.join();
        System.out.println( client.result() );
    }
}

