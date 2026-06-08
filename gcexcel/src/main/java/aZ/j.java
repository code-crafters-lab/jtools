package aZ;

class j {
    j() {
    }

    public static void a(String var0) {
        throw new IllegalStateException(var0, new a());
    }

    public static boolean a(Throwable var0) {
        for(Throwable var1 = var0; var1 != null; var1 = var1.getCause()) {
            if (var1 instanceof a) {
                return true;
            }
        }

        return false;
    }

    private static class a extends Throwable {
        private a() {
        }
    }
}
