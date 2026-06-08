package aV;

public enum h {
    a(-1), // Evaluation    -1	评估模式
    b(0), // Unlicensed      0	    未授权
    c(1); // Licensed        1	    已授权

    private final int d;

    h(int var3) {
        this.d = var3;
    }

    public int getValue() {
        return this.d;
    }
}
