package bJ;

import java.io.Serializable;

public interface ap<T, TResult> extends Serializable {
    TResult invoke(T var1);
}
