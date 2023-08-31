package cn.gmlee.tools.gray.mod;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@SuppressWarnings("all")
public class User extends Rule {
    private List<String> ids = Collections.emptyList();
    private List<String> usernames = Collections.emptyList();
}
