package aV;

import java.util.Date;

public class f {
    public String a; // Id (License ID)
    public boolean b; // Evl (Evaluation 标记)
    public String c; // OId (Organization ID)
    public String d; // CNa (Company Name)
    public String e; // CId (Contact ID)
    public String f; // Dms (Domains 绑定)
    public String g; // Ips (IP 地址绑定)
    public Date h;   // Exp (过期时间)
    public Date i;  // Crt (创建时间)
    public i[] j;  // Prd (产品数组)
    public Boolean k;
    public String l;

    public f() {
        this.j = new i[0];
    }

    public f(String var1, boolean var2, String var3, String var4, String var5, String var6, String var7, Date var8, Date var9, i[] var10) {
        this.a = var1;
        this.b = var2;
        this.c = var3;
        this.d = var4;
        this.e = var5;
        this.f = var6;
        this.g = var7;
        this.h = var8;
        this.i = var9;
        this.j = var10;
        if (this.j == null) {
            this.j = new i[0];
        }

    }
}
