package aX;

public class d {
    private static e a;

    public d() {
    }

    public static e a() {
        return a;
    }

    public static void b() {
        a = e.a;
    }

    public static void a(e var0) {
        a = var0;
    }

    static {
        a = e.a;
    }
}
