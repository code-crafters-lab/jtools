package aV;

import java.util.Date;

public class e {
    public String a;
    public String b;
    public String[] c;
    public int d;
    public String e;
    public f f;

    public e() {
    }

    public String a(String var1) {
        if (this.f == null) {
            return "";
        } else {
            i var2 = this.b(var1);
            return var2 == null ? "" : var2.a;
        }
    }

    private i b(String var1) {
        for (int var2 = 0; var2 < this.f.j.length; ++var2) {
            if (this.f.j[var2].b.equals(var1)) {
                return this.f.j[var2];
            }
        }

        return null;
    }

    public Integer a() {
        if (this.f == null) {
            return 0;
        } else if (this.f.h != null) {
            int var1 = (int) ((this.f.h.getTime() - (new Date()).getTime()) / 86400000L);
            return var1;
        } else {
            return null;
        }
    }

    public boolean b() {
        if (this.f == null) {
            return true;
        } else if (this.f.h != null) {
            long e = this.f.h.getTime();
            long n = new Date().getTime();
            boolean b = e < n;
            return b;
        } else {
            return false;
        }
    }
}
