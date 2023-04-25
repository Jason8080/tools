package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.TreeUtil;
import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * .
 *
 * @author Jas°
 * @date 2021/10/19 (周二)
 */
public class TreeTests {

    @Data
    static class Tree<T extends Tree> implements cn.gmlee.tools.base.mod.Tree<T> {
        private Long id;
        private Long parentId;
        private Collection<T> children;

        public Tree(Long id, Long parentId) {
            this.id = id;
            this.parentId = parentId;
        }
    }

    @Test
    public void test() throws Exception {
        List<Tree> list = new ArrayList();
        list.add(new Tree(1L, 0L));
        list.add(new Tree(2L, null));
        list.add(new Tree(3L, 1L));
        list.add(new Tree(4L, 1L));
        list.add(new Tree(5L, 2L));
        list.add(new Tree(6L, 2L));
        list.add(new Tree(7L, 6L));
        list.add(new Tree(8L, 7L));
        Collection<Tree> tree = TreeUtil.tree(list);
        System.out.println(JsonUtil.toJson(tree));
    }
}
