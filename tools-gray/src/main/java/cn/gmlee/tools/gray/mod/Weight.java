package cn.gmlee.tools.gray.mod;

import lombok.Data;

import java.math.BigDecimal;

@Data
@SuppressWarnings("all")
public class Weight extends Rule {
    private BigDecimal ratio = BigDecimal.valueOf(100);
}
