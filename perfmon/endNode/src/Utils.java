import java.net.URI;

import com.sun.net.httpserver.HttpExchange;

class Utils
{
    static String getQueryValue(HttpExchange exchange, String key)
    {
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();

        if ( query != null ) {
            String[] parts = query.split("=");

            if (parts[0].equalsIgnoreCase(key))
                 return parts[1];
        }
        return "";
    }
}
