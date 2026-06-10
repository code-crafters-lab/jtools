package aZ;

import aU.b;
import com.grapecity.documents.excel.internals.bJ.ab;

public class c extends g {
    public c(f var1) {
    }

    private void d(b var1) {
        String var2;
        switch (var1.b()) {
            case a:
                if (var1.c()) {
                    var2 = i.f();
                } else {
                    var2 = i.e();
                }
                break;
            case b:
                if (var1.c()) {
                    var2 = i.h();
                } else {
                    var2 = i.i();
                }
                break;
            case c:
                if (var1.c()) {
                    var2 = i.k();
                } else {
                    var2 = i.j();
                }
                break;
            default:
                var2 = "Unspecified Error.";
        }

        j.a(var2);
    }

    @Override
    public void a(b var1) {
        this.d(var1);
    }

    @Override
    public void b(b var1) {
        this.d(var1);
    }

    @Override
    public String a(b var1, m var2, ab var3) {
        this.d(var1);
        return "6K+35aSn5L6g6auY5oqs6LS15omL77yM5Lmw5Yeg5Liq6K645Y+v6K+B5ZCn44CC5oi/6LS36KaB6L+Y5LiN6LW35LqG44CC";
    }

    @Override
    public void c(b var1) {
        this.d(var1);
    }
}
