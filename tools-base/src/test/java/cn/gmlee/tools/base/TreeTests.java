package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.Tree;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.TreeUtil;
import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        List<MyTree<Integer>> list = new ArrayList();
        list.add(new MyTree(1, 0));
        list.add(new MyTree(2, null));
        list.add(new MyTree(3, 1));
        list.add(new MyTree(4, 1));
        list.add(new MyTree(5, 2));
        list.add(new MyTree(6, 2));
        list.add(new MyTree(7, 6));
        list.add(new MyTree(8, 7));
        Collection<MyTree<Integer>> tree = TreeUtil.tree(list, 0, null);
        List<Integer> collect = TreeUtil.list(tree).stream().map(MyTree::getId).collect(Collectors.toList());
        System.out.println(JsonUtil.format(collect));
    }
}
