package cn.gmlee.tools.gray.mod;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@SuppressWarnings("all")
public class Ip extends Rule {
    private List<String> ips = Collections.emptyList();
}
