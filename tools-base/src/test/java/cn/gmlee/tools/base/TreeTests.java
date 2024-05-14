package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.Tree;
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
    static class MyTree<ID> implements Tree<MyTree,ID> {
        private ID id;
        private ID parentId;
        private Collection<MyTree> children;

        public MyTree(ID id, ID parentId) {
            this.id = id;
            this.parentId = parentId;
        }
    }

    @Test
    public void test() throws Exception {
        List<MyTree<Long>> list = new ArrayList();
        list.add(new MyTree(1L, 0L));
        list.add(new MyTree(2L, null));
        list.add(new MyTree(3L, 1L));
        list.add(new MyTree(4L, 1L));
        list.add(new MyTree(5L, 2L));
        list.add(new MyTree(6L, 2L));
        list.add(new MyTree(7L, 6L));
        list.add(new MyTree(8L, 7L));
        Collection<MyTree<Long>> tree = TreeUtil.tree(list);
        System.out.println(JsonUtil.format(tree));
    }
}
