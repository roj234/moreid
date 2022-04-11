package moreid.util;

import io.netty.buffer.UnpooledHeapByteBuf;
import roj.reflect.DirectAccessor;

/**
 * @author Roj234
 * @since  2021/2/2 16:07
 */
public interface Reflects {
    void setArray(Object buffer, byte[] array);

    Reflects INSTANCE = DirectAccessor
            .builder(Reflects.class)
            .delegate(UnpooledHeapByteBuf.class, "setArray")
            .build();
}
