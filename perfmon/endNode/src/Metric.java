import org.hyperic.sigar.SigarException;

/**
 * This class provides the system resource uage by combining
 * the restuls from "CpuUsage" and "MemUsage"
 *
 * <p> Both "CpuUsage" and "MemUsage" derive from this class
 */
public class Metric {

    private String specific = "";

    public Metric() {}

    public Metric(String specific) {
        this.specific = specific;
    }

    String getUsage() throws SigarException {

        StringBuilder sb = new StringBuilder();

        try {
            sb.append( "{\n");
            if ( "".equals(specific) ) {
                sb.append( new CpuUsage().getUsage() + ",\n" );
                sb.append( new MemUsage().getUsage() + "\n" );
            } else if ( "Cpu".equalsIgnoreCase(specific) ) {
                sb.append( new CpuUsage().getUsage() + ",\n" );
            } else if ( "Mem".equalsIgnoreCase(specific) ) {
                sb.append( new MemUsage().getUsage() + "\n" );
            }
            sb.append( "}\n");
        } catch (SigarException se) {
            return "";
        }
        return sb.toString();
    }

    static protected void println(String line) {
        System.out.println(line);
    }

    public static void main(String[] args) throws Exception {

        System.out.println( new Metric().getUsage() );

    }
}
