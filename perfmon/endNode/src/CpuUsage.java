
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * This class provides a utility class which reports the current CPU utilization
 * on the current system.
 *
 * <p> This relies on the underlying "sigar" packge to query system resources
 *
 */
public class CpuUsage extends Metric
{

    public CpuUsage() {}

    @Override
    public String getUsage() throws SigarException {

        Sigar sigar = new Sigar();

        CpuInfo[] infos = sigar.getCpuInfoList();
        CpuPerc[] cpus = sigar.getCpuPercList();

        CpuInfo info = infos[0];

        StringBuilder json = new StringBuilder(); 

        json.append( "\"Cpu\" : {\n" );

        json.append( "\t\"Vender\"\t:\t\"" + info.getVendor() + "\",\n");
        json.append( "\t\"Model\" \t:\t\""  + info.getModel() + "\",\n");
        json.append( "\t\"MHz\"   \t:\t"  + info.getMhz() + ",\n");
        json.append( "\t\"Total CPUs\"\t:\t"  + info.getTotalCores() + ",\n");
        json.append( "\t\"Usage\"\t: {");
	
        int n = 0;
        for( CpuPerc cpu : cpus ) {
             if ( n > 0 ) {
                json.append(",");
             }
             json.append("\n");
             json.append("\t\t\"core" + n++ + "\"\t:\t\""
                         + CpuPerc.format(cpu.getCombined()) + "\"");
        }
        json.append("\n");
        json.append( "\t}\n");
        json.append( "}" );

        return json.toString();
    }

    static public void main(String[] args) throws Exception {

        println( new CpuUsage().getUsage() + "\n" );

    }
}
