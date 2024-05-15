package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.util.Collection;

@Data
public class SimpleTree<Code> implements Tree<SimpleTree, Code> {
    private Code id;
    private String name;
    private Code parentId;
    private Collection<SimpleTree> children;
}