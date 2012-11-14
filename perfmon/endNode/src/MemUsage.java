import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 *
 */
public class MemUsage extends Metric
{
    public MemUsage() {}

    public String getUsage() throws SigarException {

	Sigar sigar = new Sigar();

        Mem mem = sigar.getMem();

        StringBuilder json = new StringBuilder(); 

        json.append( "\"Mem\" : {\n" );

        json.append( "\t\"Total\"\t:\t\"" + mem.getRam() + "MB\",\n");
        json.append( "\t\"Free\"\t:\t" + mem.getFree() + ",\n");
        json.append( "\t\"Used\"\t:\t" + mem.getUsed() + "\n");
	
        json.append( "}\n" );

        return json.toString();
    }

    public static void main(String[] args) throws Exception {
        println( new MemUsage().getUsage() );
    }
}
