package aX;

import java.util.UUID;
import com.grapecity.documents.excel.internals.bJ.ax;
import com.grapecity.documents.excel.internals.bJ.bC;
import com.grapecity.documents.excel.internals.bJ.bO;
import com.grapecity.documents.excel.internals.bJ.bR;

public class f {
    private static final UUID d = ax.a(a() ? "64c28c83-fab2-4c5d-bd94-7e2dee4da186" : "383d4eff-9ef1-4198-ad4d-eb11035a7bc6", "d");
    private static final UUID e = ax.a(a() ? "e9b2a94e-afa0-40ab-be43-b86d3719c5f7" : "6bf630ea-22d3-47b5-bb9e-2102f3c52186", "d");
    public static final String a = "GCEXCEL_JAVA_DEPLOY_LICENSE_V9";
    private static Boolean f;
    private final UUID g;
    public static final f b;
    public static final f c;

    public static synchronized boolean a() {
        if (f == null) {
            String var0 = System.getenv("GCLMTEST");
            f = bR.d(var0, "true", bO.d);
        }

        return bC.a(f);
    }

    public static UUID b() {
        return e;
    }

    public static UUID c() {
        return d;
    }

    private f(UUID var1) {
        this.g = var1;
    }

    public int hashCode() {
        boolean var1 = true;
        int var2 = 1;
        var2 = 31 * var2 + (this.g == null ? 0 : this.g.hashCode());
        return var2;
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (var1 == null) {
            return false;
        } else if (this.getClass() != var1.getClass()) {
            return false;
        } else {
            f var2 = (f)var1;
            if (this.g == null) {
                if (var2.g != null) {
                    return false;
                }
            } else if (!this.g.equals(var2.g)) {
                return false;
            }

            return true;
        }
    }

    static {
        b = new f(e);
        c = new f(d);
    }
}
