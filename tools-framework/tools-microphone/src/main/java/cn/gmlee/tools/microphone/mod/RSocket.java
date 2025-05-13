package cn.gmlee.tools.microphone.mod;

import cn.gmlee.tools.base.mod.R;
import lombok.Data;

import java.io.Serializable;

/**
 * The type R socket.
 *
 * @param <T> the type parameter
 */
@Data
public class RSocket<T> extends R<T> implements Serializable {
    private String sessionId;
    private byte[] bytes;

    /**
     * Instantiates a new R socket.
     *
     * @param bytes the bytes
     */
    public RSocket(byte... bytes) {
        if (bytes != null) {
            this.bytes = bytes;
        }
    }

    /**
     * Instantiates a new R socket.
     *
     * @param t the t
     */
    public RSocket(T t) {
        super.data = t;
    }
}
