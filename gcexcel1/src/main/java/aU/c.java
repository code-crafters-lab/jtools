package aU;

class c extends b {
    private final aV.k a;

    public c(aV.k var1) {
        this.a = var1;
    }

    public a a() {
        if (this.a.g()) {
            return aU.a.b;
        } else {
            switch (this.a.e()) {
                case a:
                    if (this.a.f()) {
                        return aU.a.f;
                    }

                    return aU.a.e;
                case c:
                    if (this.a.f()) {
                        return aU.a.d;
                    }

                    return aU.a.c;
                default:
                    return this.a.f() ? aU.a.f : aU.a.a;
            }
        }
    }

    public f b() {
        switch (this.a.e()) {
            case a:
            case c:
                return f.c;
            default:
                return null;
        }
    }

    public boolean c() {
        return this.a.e() == aV.h.a;
    }
}

