package aZ;

import aU.b;
import com.grapecity.documents.excel.internals.bJ.ab;
import com.grapecity.documents.excel.internals.bJ.v;

public class k extends g {
    private final f a;

    public k(f var1) {
        this.a = var1;
    }

    private void a() {
        if (v.h(v.n(), this.a.b)) {
            j.a(i.c());
        }
    }

    @Override
    public void a(b var1) {
        if (this.a.c <= 0) {
            j.a(i.a());
        }

        this.a();
        --this.a.c;
    }

    @Override
    public void b(b var1) {
        if (this.a.d <= 0) {
            j.a(i.b());
        }

        this.a();
        --this.a.d;
    }

    @Override
    public String a(b var1, m var2, ab var3) {
        switch (var2) {
            case b:
            case d:
                return i.a(var3);
            case c:
                return i.b(var3);
            default:
                return i.c(var3);
        }
    }

    @Override
    public void c(b var1) {
        this.a();
    }

}
