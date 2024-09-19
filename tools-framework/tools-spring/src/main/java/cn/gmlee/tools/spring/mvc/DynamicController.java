package cn.gmlee.tools.spring.mvc;

public interface DynamicController {

    <P, R> R handle(P p);

}
