package cn.gmlee.tools.spring.mvc;

public interface DynamicController<P, R> {

    R api(P p);

}
