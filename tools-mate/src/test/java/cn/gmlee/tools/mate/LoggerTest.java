package cn.gmlee.tools.mate;

import cn.gmlee.tools.base.mod.PageRequest;
import cn.gmlee.tools.base.mod.PageResponse;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.mate.dao.entity.Log;
import cn.gmlee.tools.mate.dao.mapper.ExLogMapper;
import cn.gmlee.tools.mate.dao.mapper.LogMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class LoggerTest {

    @Resource
    LogMapper logMapper;

    @Resource
    ExLogMapper exLogMapper;

    @Test
    public void list() throws Exception {
        PageRequest pr = new PageRequest();
        Page<Log> page = logMapper.list(Page.of(pr.current, pr.size), "10.129.0.63");
        System.out.println(new PageResponse(pr, page.getTotal(), page.getRecords()));
    }

    @Test
    public void save() throws Exception {
        Log log = new Log();
        log.setSysName("哈哈");
        log.setRequestParams("127.0.0.100");
        exLogMapper.save(log);
    }

    @Test
    public void update() throws Exception {
        Log log = new Log();
        log.setId(1L);
        log.setSysName("哈哈"+ TimeUtil.getCurrentDatetime());
        log.setRequestParams("127.0.0.1");
        logMapper.insert(log);
    }
 
}