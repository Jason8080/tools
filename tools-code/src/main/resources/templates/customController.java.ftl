package ${package.Controller};



import cn.gmlee.tools.base.anno.ApiPrint;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.mod.PageRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
<#if swagger2>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
</#if>
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ${package.Service}.${table.serviceName};
import ${package.Entity}.${entity};
import ${package.Controller}.vo.${entity}Vo;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
 * <p>
 * ${table.comment!} 前端控制器
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Validated
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
<#if swagger2>
@Api(tags = {"${table.comment!} 前端控制器"})
</#if>
@RequestMapping("<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>
     @Resource
     ${table.serviceName} ${table.serviceName?uncap_first};

    <#if swagger2>
     @ApiOperation(value = "批量保存")
    </#if>
     @ApiPrint(value = "批量保存")
     @PostMapping(value = "saveBatch")
    <#if swagger2>
     @ApiImplicitParams({
           @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
     })
    </#if>
     public R saveBatch(
           @RequestBody @NotNull(message = "数据是空") @Validated List<${entity}Vo> vos
      ) {
           ${table.serviceName?uncap_first}.saveBatch(vos);
           return R.OK;
      }

    <#if swagger2>
      @ApiOperation(value = "保存")
    </#if>
      @ApiPrint(value = "保存")
      @PostMapping(value = "save")
    <#if swagger2>
      @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
      })
    </#if>
      public R save(
            @RequestBody @Validated ${entity} ${entity?uncap_first}
      ) {
            ${table.serviceName?uncap_first}.save(${entity?uncap_first});
            return R.OK;
      }

    <#if swagger2>
      @ApiOperation(value = "新增/修改", notes = "有`主键`则修改反之新增")
    </#if>
      @ApiPrint(value = "新增/修改")
      @PostMapping(value = "modify")
    <#if swagger2>
      @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
      })
    </#if>
      public R modify(
            @RequestBody @Validated ${entity}Vo vo
      ) {
            ${table.serviceName?uncap_first}.modify(vo);
            return R.OK;
      }

    <#if swagger2>
      @ApiOperation(value = "逻辑删除")
    </#if>
      @ApiPrint(value = "逻辑删除")
    <#if swagger2>
      @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
            @ApiImplicitParam(name = "id", value = "编号", dataType = "long", dataTypeClass = Long.class),
      })
    </#if>
      @PostMapping(value = "logicDelById")
      public R logicDelById(
            @RequestBody @NotNull(message = "编号是空") Long id
      ) {
            ${table.serviceName?uncap_first}.logicDelById(id);
            return R.OK;
      }

    <#if swagger2>
      @ApiOperation(value = "获取单条")
    </#if>
      @ApiPrint(value = "获取单条")
      @GetMapping(value = "getById")
    <#if swagger2>
      @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
            @ApiImplicitParam(name = "id", value = "编号", paramType = "query", dataType = "long", dataTypeClass = Long.class),
      })
    </#if>
      public R getById(
            @NotNull(message = "编号是空") Long id
      ) {
            return R.OK.newly(${table.serviceName?uncap_first}.getById(id));
      }

    <#if swagger2>
      @ApiOperation(value = "获取列表")
    </#if>
      @ApiPrint(value = "获取列表")
      @PostMapping(value = "listBy")
    <#if swagger2>
      @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
      })
    </#if>
      public R listBy(
            @RequestBody ${entity}Vo vo
      ) {
            return R.OK.newly(${table.serviceName?uncap_first}.listBy(vo));
      }

    <#if swagger2>
      @ApiOperation(value = "分页查询")
    </#if>
      @ApiPrint(value = "分页查询")
      @PostMapping(value = "listPageBy")
    <#if swagger2>
      @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "起始页", paramType = "query", dataType = "integer", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "size", value = "页数量", paramType = "query", dataType = "integer", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "token", value = "身份令牌", paramType = "header", dataType = "string", dataTypeClass = String.class),
      })
    </#if>
      public R listPageBy(
            PageRequest page, @RequestBody ${entity}Vo vo
      ) {
            return R.OK.newly(${table.serviceName?uncap_first}.listPageBy(new Page(page.current, page.size), vo));
      }
}
</#if>
