package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.Int;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * Jdbc工具类
 *
 * @author Jason
 */
@Slf4j
public class JdbcUtil {
    public static final String SELECT = "SELECT";
    public static final String INSERT = "INSERT";
    public static final String SHOW = "SHOW";
    private static final ThreadUtil.Pool pool = ThreadUtil.getIndependentPool();

    /**
     * 从当前线程获取连接.
     *
     * @param dataSource the data source
     * @param autoCommit the auto commit
     * @return the connection
     */
    public static Connection get(DataSource dataSource, boolean autoCommit) {
        try {
            Connection con = dataSource.getConnection();
            con.setAutoCommit(autoCommit);
            return con;
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 从新线程获取连接.
     *
     * @param dataSource the data source
     * @param autoCommit the auto commit
     * @return the connection
     */
    public static Connection newGet(DataSource dataSource, boolean autoCommit) {
        try {
            Connection con = pool.sync(() -> dataSource.getConnection());
            con.setAutoCommit(autoCommit);
            return con;
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * Exec list.
     *
     * @param v2r the v 2 r
     * @param sql the sql
     * @return the list
     */
    public static List<Map<String, Object>> exec(Function.V2r<Connection> v2r, String sql) {
        return exec(v2r, sql, Object.class);
    }

    /**
     * Exec list.
     *
     * @param v2r   the v 2 r
     * @param sql   the sql
     * @param close the close
     * @return the list
     */
    public static List<Map<String, Object>> exec(Function.V2r<Connection> v2r, String sql, boolean close) {
        return exec(v2r, sql, Object.class, close);
    }

    /**
     * Execute list.
     *
     * @param v2r        the v 2 r
     * @param sql        the sql
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static List<Map<String, Object>> execute(Function.V2r<Connection> v2r, String sql, boolean close, Object... parameters) {
        return execute(v2r, sql, Object.class, close, parameters);
    }

    /**
     * Exec list.
     *
     * @param <T>   the type parameter
     * @param v2r   the v 2 r
     * @param sql   the sql
     * @param clazz the clazz
     * @return the list
     */
    public static <T> List<Map<String, T>> exec(Function.V2r<Connection> v2r, String sql, Class<T> clazz) {
        return exec(v2r, sql, clazz, true);
    }

    /**
     * Exec list.
     *
     * @param <T>   the type parameter
     * @param v2r   the v 2 r
     * @param sql   the sql
     * @param clazz the clazz
     * @param close the close
     * @return the list
     */
    public static <T> List<Map<String, T>> exec(Function.V2r<Connection> v2r, String sql, Class<T> clazz, boolean close) {
        try {
            Connection con = v2r.run();
            return exec(con, sql, clazz, close);
        } catch (Throwable throwable) {
            return ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Execute list.
     *
     * @param <T>        the type parameter
     * @param v2r        the v 2 r
     * @param sql        the sql
     * @param clazz      the clazz
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static <T> List<Map<String, T>> execute(Function.V2r<Connection> v2r, String sql, Class<T> clazz, boolean close, Object... parameters) {
        try {
            Connection con = v2r.run();
            return execute(con, sql, clazz, close, parameters);
        } catch (Throwable throwable) {
            return ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Exec list.
     *
     * @param dataSource the data source
     * @param sql        the sql
     * @return the list
     */
    public static List<Map<String, Object>> exec(DataSource dataSource, String sql) {
        return exec(dataSource, sql, Object.class, true);
    }

    /**
     * Exec list.
     *
     * @param dataSource the data source
     * @param sql        the sql
     * @param close      the close
     * @return the list
     */
    public static List<Map<String, Object>> exec(DataSource dataSource, String sql, boolean close) {
        return exec(dataSource, sql, Object.class, close);
    }

    /**
     * Execute list.
     *
     * @param dataSource the data source
     * @param sql        the sql
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static List<Map<String, Object>> execute(DataSource dataSource, String sql, boolean close, Object... parameters) {
        return execute(dataSource, sql, Object.class, close, parameters);
    }

    /**
     * Exec list.
     *
     * @param <T>        the type parameter
     * @param dataSource the data source
     * @param sql        the sql
     * @param clazz      the clazz
     * @return the list
     */
    public static <T> List<Map<String, T>> exec(DataSource dataSource, String sql, Class<T> clazz) {
        return exec(dataSource, sql, clazz, true);
    }

    /**
     * Exec list.
     *
     * @param <T>        the type parameter
     * @param dataSource the data source
     * @param sql        the sql
     * @param clazz      the clazz
     * @param close      the close
     * @return the list
     */
    public static <T> List<Map<String, T>> exec(DataSource dataSource, String sql, Class<T> clazz, boolean close) {
        try {
            Connection con = dataSource.getConnection();
            return exec(con, sql, clazz, close);
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * Execute list.
     *
     * @param <T>        the type parameter
     * @param dataSource the data source
     * @param sql        the sql
     * @param clazz      the clazz
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static <T> List<Map<String, T>> execute(DataSource dataSource, String sql, Class<T> clazz, boolean close, Object... parameters) {
        try {
            Connection con = dataSource.getConnection();
            return execute(con, sql, clazz, close, parameters);
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }


    /**
     * Exec list.
     *
     * @param url      the url
     * @param username the username
     * @param password the password
     * @param sql      the sql
     * @return the list
     */
    public static List<Map<String, Object>> exec(String url, String username, String password, String sql) {
        return exec(url, username, password, sql, Object.class);
    }

    /**
     * Execute list.
     *
     * @param url        the url
     * @param username   the username
     * @param password   the password
     * @param sql        the sql
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static List<Map<String, Object>> execute(String url, String username, String password, String sql, boolean close, Object... parameters) {
        return execute(url, username, password, sql, Object.class, close, parameters);
    }

    /**
     * Exec t.
     *
     * @param <T>      the type parameter
     * @param url      the url
     * @param username the username
     * @param password the password
     * @param sql      the sql
     * @param clazz    the clazz
     * @return the t
     */
    public static <T> List<Map<String, T>> exec(String url, String username, String password, String sql, Class<T> clazz) {
        Connection con = connection(url, username, password);
        return exec(con, sql, clazz);
    }

    /**
     * Execute list.
     *
     * @param <T>        the type parameter
     * @param url        the url
     * @param username   the username
     * @param password   the password
     * @param sql        the sql
     * @param clazz      the clazz
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static <T> List<Map<String, T>> execute(String url, String username, String password, String sql, Class<T> clazz, boolean close, Object... parameters) {
        Connection con = connection(url, username, password);
        return execute(con, sql, clazz, close, parameters);
    }

    /**
     * Exec list.
     *
     * @param con the con
     * @param sql the sql
     * @return the list
     */
    public static List<Map<String, Object>> exec(Connection con, String sql) {
        return exec(con, sql, Object.class);
    }

    /**
     * Exec list.
     *
     * @param con   the con
     * @param sql   the sql
     * @param close the close
     * @return the list
     */
    public static List<Map<String, Object>> exec(Connection con, String sql, boolean close) {
        return exec(con, sql, Object.class, close);
    }

    /**
     * Execute list.
     *
     * @param con        the con
     * @param sql        the sql
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static List<Map<String, Object>> execute(Connection con, String sql, boolean close, Object... parameters) {
        return execute(con, sql, Object.class, close, parameters);
    }

    /**
     * Exec list.
     *
     * @param <T>   the type parameter
     * @param con   the con
     * @param sql   the sql
     * @param clazz the clazz
     * @return the list
     */
    public static <T> List<Map<String, T>> exec(Connection con, String sql, Class<T> clazz) {
        return exec(con, sql, clazz, true);
    }

    /**
     * Exec list.
     *
     * @param <T>   the type parameter
     * @param con   the con
     * @param sql   the sql
     * @param clazz the clazz
     * @param close 默认自动关闭链接
     * @return the list
     */
    public static <T> List<Map<String, T>> exec(Connection con, String sql, Class<T> clazz, boolean close) {
        try {
            if (!con.isClosed()) {
                // 设置隔离级别
                con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                // 创建Statement对象
                PreparedStatement ps = QuickUtil.is(isInsert(sql),
                        // 返回自增主键
                        () -> con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS),
                        () -> con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                );
                // 处理参数
                setPreparedStatementParameters(ps);
                // 转换Json
                List<Map<String, T>> result = getResult(sql, ps, clazz);
                // 返回Obj
                return result;
            }
        } catch (Exception e) {
            log.error(String.format("执行%s出错", sql), e);
        } finally {
            commit(con, close);
        }
        return ExceptionUtil.cast();
    }

    /**
     * Execute list.
     *
     * @param <T>        the type parameter
     * @param con        the con
     * @param sql        the sql
     * @param clazz      the clazz
     * @param close      the close
     * @param parameters the parameters
     * @return the list
     */
    public static <T> List<Map<String, T>> execute(Connection con, String sql, Class<T> clazz, boolean close, Object... parameters) {
        try {
            if (!con.isClosed()) {
                // 设置隔离级别
                con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                // 创建Statement对象
                PreparedStatement ps = QuickUtil.is(isInsert(sql),
                        // 返回自增主键
                        () -> con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS),
                        () -> con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                );
                // 处理参数
                setPreparedStatementParameters(ps, parameters);
                // 转换Json
                List<Map<String, T>> result = getResult(sql, ps, clazz);
                // 返回Obj
                return result;
            }
        } catch (Exception e) {
            log.error(String.format("执行%s出错", sql), e);
        } finally {
            commit(con, close);
        }
        return ExceptionUtil.cast();
    }

    /**
     * Close.
     *
     * @param con   the con
     * @param close the close
     */
    public static void commit(Connection con, boolean close) {
        try {
            if (close) {
                if (!con.getAutoCommit()) {
                    con.commit();
                    con.setAutoCommit(true);
                }
                con.close();
            }
        } catch (SQLException e) {
            log.error(String.format("提交失败"), e);
        }
    }

    /**
     * Close.
     *
     * @param con   the con
     * @param close the close
     */
    public static void rollback(Connection con, boolean close) {
        try {
            if (close) {
                if (!con.getAutoCommit()) {
                    con.rollback();
                    con.setAutoCommit(true);
                }
                con.close();
            }
        } catch (SQLException e) {
            log.error(String.format("回滚失败"), e);
        }
    }

    /**
     * Close.
     *
     * @param con the con
     */
    public static void commit(Connection con) {
        commit(con, true);
    }

    /**
     * Close.
     *
     * @param con the con
     */
    public static void rollback(Connection con) {
        rollback(con, true);
    }

    /**
     * Sets prepared statement parameters.
     *
     * @param ps         the ps
     * @param parameters the parameters
     * @throws SQLException the sql exception
     */
    public static void setPreparedStatementParameters(PreparedStatement ps, Object... parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            Object parameter = parameters[i - 1];
            if (BoolUtil.notNull(parameter)) {
                boolean is = BoolUtil.isBaseClass(parameter, String.class, Date.class, Timestamp.class);
                AssertUtil.isTrue(is, String.format("参数不是基本数据类型: %s", parameter));
            }
            ps.setObject(i, parameter);
        }
    }

    private static <T> List<Map<String, T>> getResult(String sql, PreparedStatement ps, Class<T> clazz) throws SQLException {
        // 执行sql语句
        if (isResultSet(sql)) {
            ResultSet rs = ps.executeQuery();
            try {
                return getResult(rs, clazz);
            } finally {
                // 关闭资源
                rs.close();
                ps.close();
            }
        } else {
            List<Map<String, T>> array = new ArrayList();
            Map data = new HashMap(1);
            ps.execute();
            data.put("count", ps.getUpdateCount());
            // 返回自增主键
            getGeneratedKeys(sql, ps, data);
            ps.close();
            array.add(data);
            return array;
        }
    }

    private static void getGeneratedKeys(String sql, PreparedStatement ps, Map data) throws SQLException {
        if (isInsert(sql)) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Object id = rs.getObject(1);
                data.put("id", id);
            }
        }
    }

    /**
     * 获取影响行数.
     *
     * @param result the result
     * @return the count
     */
    public static Integer getCount(List<Map> result) {
        if (BoolUtil.notEmpty(result)) {
            Map map = result.get(0);
            if (BoolUtil.notEmpty(map)) {
                Object count = map.get("count");
                AssertUtil.notNull(count, String.format("结果可能不是1个count!"));
                return (Integer) count;
            }
        }
        return Int.ZERO;
    }

    /**
     * 获取是否影响数据库.
     *
     * @param result the result
     * @return the bool
     */
    public static Boolean getBool(List<Map> result) {
        return getCount(result) > 0;
    }

    /**
     * Gets result.
     *
     * @param <T>   the type parameter
     * @param rs    the rs
     * @param clazz the clazz
     * @return the result
     * @throws SQLException the sql exception
     */
    public static <T> List<Map<String, T>> getResult(ResultSet rs, Class<T> clazz) throws SQLException {
        List<Map<String, T>> array = new ArrayList();
        // 获得结果集结构信息,元数据
        ResultSetMetaData md = rs.getMetaData();
        // 收集结果
        int count = md.getColumnCount();
        boolean isObj = Object.class.equals(clazz);
        while (rs.next()) {
            int number = 0;
            Map<String, T> data = new HashMap(0);
            for (int i = 1; i <= count; i++) {
                T val = isObj ? (T) rs.getObject(i) : rs.getObject(i, clazz);
                String label = md.getColumnLabel(i);
                String key = number > 0 ? String.format("%s %s", label, number) : label;
                // 发现重复列名
                if (data.containsKey(key)) {
                    key = String.format("%s %s", label, ++number);
                }
                data.put(key, val);
            }
            array.add(data);
        }
        return array;
    }

    private static boolean isResultSet(String sql) {
        boolean isSelect = sql.startsWith(SELECT) || sql.startsWith(SELECT.toLowerCase());
        boolean isShow = sql.startsWith(SHOW) || sql.startsWith(SHOW.toLowerCase());
        return isSelect || isShow;
    }

    private static boolean isInsert(String sql) {
        return sql.startsWith("insert") || sql.startsWith("INSERT");
    }

    private static Connection connection(String url, String username, String password) {
        try {
            //1、加载驱动
            Class.forName("com.mysql.jdbc.Driver");
            //2、链接数据库
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            log.error(String.format("获取连接%s:%s出错", username, password), e);
            return ExceptionUtil.cast(e);
        }
    }
}
