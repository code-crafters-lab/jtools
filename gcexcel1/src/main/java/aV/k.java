package aV;

import com.grapecity.documents.excel.Event;
import com.grapecity.documents.excel.EventArgs;
import  com.grapecity.documents.excel.internals.bJ.v;

public class k implements c {
    private String b;
    private a c;
    private b d;
    private v e = new v();
    private Boolean f = null;
    private boolean g = false;
    public Event<aW.b<EventArgs>> a = new Event<>();
    private bJ.ap<Character, aW.a> h = (var0x) -> new aW.a("Sample", "A1");
    private bJ.ap<Character, aW.d> i = (var0x) -> {
        try {
            return new aW.d("udkL9Yp1FotxuamEH+Q3l7d6r+PAhQ6YypLjvRCINZ19FKWe4DiN4VCRkxrP4kPaubPzoi0WNHThfTzrc8PToRuzQmbCxsvb1IsS6rOssOjFVvT09InQw5jogiY9nnVnA0lgmHd43KY1jvkiLV/iK+h4n0tz9w9D812eL6RVOYKzeN+uXsdOxrS01D+liBPx3UGfcNYW/jY5me7OQQPB5I16zvmifWqr68EVuRchI5xc+nDzu6lZghrMl33Lsbx8JljQiXmUi2PGSdXmj6adqPjeyLdHlPWuFMUEljYgVgaxAKxGBNyPm08lVMFnTpJUpQLB9Inc51itnFrob9wpEQ==", "AQAB");
        } catch (Exception var2) {
            return null;
        }
    };
    private bJ.c<Boolean, bJ.ao<b>> j = this::a;

    public final void a(String var1) throws Exception {
        if (!bJ.bR.a(var1, this.b)) {
            this.b = var1;
            if (this.a != null) {
                for(aW.b<EventArgs> var3 : this.a.listeners()) {
                    var3.a(this, EventArgs.Empty);
                }
            }
        }

    }

    public final String a() {
        return "93W7";
    }

    public final String b() {
        return "localhost";
    }

    public final String c() {
        return this.b;
    }

    public Event<aW.b<EventArgs>> d() {
        return this.a;
    }

    public final h e() {
        return this.d != null ? this.d.c() : aV.h.b;
    }

    public final boolean f() {
        if (this.f != null) {
            return this.f;
        } else if (this.d != null && v.a(v.n(), this.e) <= 0) {
            return false;
        } else {
            this.f = true;
            return true;
        }
    }

    public final boolean g() {
        return this.g;
    }

    public k() {
        this.c = new a(this, this.h, this.i, this.j);
    }

    /**
     * 处理许可证验证结果
     *
     * <p>根据特征验证结果决定如何处理许可证供应商返回的解析结果：
     * <ul>
     *   <li>特征验证未通过或旧格式许可证：检查结果对象的许可证类型和令牌完整性，
     *       若存在有效令牌则根据额外标志位决定是否缓存为有效状态</li>
     *   <li>特征验证通过：根据结果中的重试间隔设置定时器用于后续重新验证，
     *       无重试间隔时直接标记为无效</li>
     * </ul>
     *
     * @param var1 特征验证是否通过；{@code true} 表示域名/IP 验证通过，
     *             {@code false} 表示旧格式许可证或域名/IP 验证未通过
     * @param var2 许可证结果供应商，通过 {@code invoke()} 获取解析后的许可证对象
     */
    private void a(boolean var1, bJ.ao<b> var2) {
        this.d = var2.invoke();
        if (!var1) {
            if (this.d.c() != aV.h.b) {
                if (bJ.bR.a(this.d.a())) {
                    this.d = null;
                    this.g = true;
                } else if (this.d.d()) { // 授权过期了
                    this.f = true;
                } else {
                    this.d = null; //
                }
            } else {
                this.d = null;
            }
        } else if (this.d.e() != null) {
            this.e = v.r().a((double) this.d.e());
            this.f = null;
        } else {
            this.f = false;
        }

    }
}
