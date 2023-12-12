package cn.gmlee.tools.base.kit.task;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface TaskManager {
    Map<Serializable, Task> taskMap = new ConcurrentHashMap<>();
}
