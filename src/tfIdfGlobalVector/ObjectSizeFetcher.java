package tfIdfGlobalVector; /**
 * Created by nuplavikar on 7/9/15.
 */
import java.lang.instrument.Instrumentation;
public class ObjectSizeFetcher {
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static long sizeof(Object o) {


        return instrumentation.getObjectSize(o);
    }
}
