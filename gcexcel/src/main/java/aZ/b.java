package aZ;

import com.grapecity.documents.excel.internals.bJ.ab;
import com.grapecity.documents.excel.internals.bJ.v;

public class b extends g {

    private final f a;

    public b(f var1) {
        this.a = var1;
    }

    private void d(aU.b var1) {
        if (var1.c()) {
            v var2 = this.a.a;
            if (v.h(v.n(), var2)) {
                j.a(i.l());
            }
        }
    }

    @Override
    public void a(aU.b var1) {
        this.d(var1);
    }

    @Override
    public void b(aU.b var1) {
        this.d(var1);
    }

    @Override
    public String a(aU.b var1, m var2, ab var3) {
        switch (var2) {
            case b:
            case d:
                if (var1.c()) {
                    return i.h(var3);
                }

                return i.d(var3);
            case c:
                if (var1.c()) {
                    return i.j(var3);
                }

                return i.e(var3);
            default:
                return null;
        }
    }

    @Override
    public void c(aU.b var1) {
        this.d(var1);
    }
}
