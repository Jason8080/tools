package ${package.ServiceImpl};

import ${package.Entity}.${entity};
import ${package.Controller}.vo.${entity}Vo;
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};

import cn.gmlee.tools.base.util.BeanUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${superServiceImplClassPackage};
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * ${table.comment!} 服务实现类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Service
<#if kotlin>
open class ${table.serviceImplName} : ${superServiceImplClass}<${table.mapperName}, ${entity}>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceImplName} extends ${superServiceImplClass}<${table.mapperName}, ${entity}> implements ${table.serviceName} {

    @Resource
    ${table.mapperName} ${table.mapperName?uncap_first};

    @Override
    public void saveBatch(List<${entity}Vo> list) {
        list.stream().mapToInt(${entity?uncap_first}Vo -> {
            ${entity} ${entity?uncap_first} = BeanUtil.convert(${entity?uncap_first}Vo, ${entity}.class);
            return ${table.mapperName?uncap_first}.insert(${entity?uncap_first});
        }).sum();
    }

    @Override
    public ${entity} modify(${entity}Vo ${entity?uncap_first}Vo) {
        ${entity} ${entity?uncap_first} = BeanUtil.convert(${entity?uncap_first}Vo, ${entity}.class);
        if (Objects.isNull(${entity?uncap_first}.getId())) {
            ${table.mapperName?uncap_first}.insert(${entity?uncap_first});
        } else {
            ${table.mapperName?uncap_first}.updateById(${entity?uncap_first});
        }
        return ${entity?uncap_first};
    }

    @Override
    public void updateBatch(List<${entity}Vo> list) {
        list.stream().mapToInt(${entity?uncap_first}Vo -> {
            ${entity} ${entity?uncap_first} = BeanUtil.convert(${entity?uncap_first}Vo, ${entity}.class);
            return ${table.mapperName?uncap_first}.updateById(${entity?uncap_first});
        }).sum();
    }

    @Override
    public void logicDelById(Long id) {
        ${entity} ${entity?uncap_first} = new ${entity}();
        ${entity?uncap_first}.setId(id);
        ${entity?uncap_first}.setDel(true);
        ${table.mapperName?uncap_first}.updateById(${entity?uncap_first});
    }

    @Override
    public void logicDelByIds(Collection<Long> ids) {
        ids.forEach(id -> logicDelById(id));
    }

    @Override
    public List<${entity}> listBy(${entity}Vo ${entity?uncap_first}Vo) {
        ${entity} ${entity?uncap_first} = BeanUtil.convert(${entity?uncap_first}Vo, ${entity}.class);
        return ${table.mapperName?uncap_first}.selectList(new QueryWrapper(${entity?uncap_first}));
    }

    @Override
    public IPage listPageBy(Page page, ${entity}Vo ${entity?uncap_first}Vo) {
        ${entity} ${entity?uncap_first} = BeanUtil.convert(${entity?uncap_first}Vo, ${entity}.class);
        return ${table.mapperName?uncap_first}.selectPage(page, new QueryWrapper(${entity?uncap_first}));
    }
}
</#if>
