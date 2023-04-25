package cn.gmlee.tools.dt.kit;

import cn.gmlee.tools.base.util.IdUtil;
import cn.gmlee.tools.dt.dao.entity.TxInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Tx kit.
 */
public class TxKit {

    private static final Map<String, Object> codeMap = new ConcurrentHashMap();

    /**
     * Generate code string.
     *
     * @param txInfo the tx info
     * @return the string
     */
    public synchronized static String generateCode(TxInfo txInfo) {
        String code = IdUtil.uuidReplaceUpperCase();
        if (codeMap.containsKey(code)) {
            return generateCode(txInfo);
        }
        codeMap.put(code, txInfo);
        return code;
    }
}
