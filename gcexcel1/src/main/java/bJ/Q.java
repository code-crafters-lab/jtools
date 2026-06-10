package bJ;

import java.io.Serializable;

public class Q implements Serializable {
    public static final Q a = new Q();

    public Q() {
    }

    public boolean a(Object var1, Object var2) {
        if (var1 == var2) {
            return true;
        } else {
            return var1 == null ? false : var1.equals(var2);
        }
    }

    public int a(Object var1) {
        return var1 == null ? -1 : var1.hashCode();
    }
}
