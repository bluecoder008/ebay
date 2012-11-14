import java.sql.Timestamp;

/**
* Implementation: Responsible for persistence of metric data
*/
public class MetricsDaoImpl implements MetricsDao {
/**
* 
* @param metrics the JSON string representing the Metric collected
* @param timestamp time of the collection of the metric
* @throws RuntimeException or subclasses representing exceptions 
*/
public void storeMetrics(String metrics, Timestamp timestamp) {

   // Code to call MongoDB java driver to store metrics to
   // backend store

}
}
