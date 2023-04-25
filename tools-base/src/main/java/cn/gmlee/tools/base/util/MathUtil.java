package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * 算术工具类
 * <p>
 * 主要的思想如下：
 * 程序从左到右的扫描表达式，提取出操作数，操作符，以及括号
 * 如果提取的项是你操作数直接将其压入operandStack栈中
 * 如果提取的项是+，-运算符，处理operatorStack栈定中的所有运算符，处理完之后将提取出的运算符压入栈中
 * 如果提取的项是*,/运算符，处理栈顶的所有*,/运算符，如果此时的栈顶的运算符是+，-那么直接入栈，处理完之后将提取出的运算符入栈
 * 如果提取的是'(',那么直接压入operatorStack栈中
 * 如果提取的是')',重复处理来自operatorStack栈顶的运算符，知道看到栈顶的'('
 *
 * @author Jas°
 */
public class MathUtil {

    private static final Logger logger = LoggerFactory.getLogger(MathUtil.class);

    /**
     * 支持计算的类型
     */
    private static final List<Class> classes = Arrays.asList(new Class[]{Boolean.class, BigDecimal.class, Integer.class, BigInteger.class});

    /**
     * 计算符号
     * 运算符+流程符
     */
    public static final List<Character> calculations = new ArrayList();
    /**
     * 运算符 + - * /
     */
    public static final List<Character> operators = new ArrayList();
    /**
     * 流程符
     */
    public static final List<Character> process = new ArrayList();
    /**
     * 逻辑运算符
     */
    public static final List<String> logic = new ArrayList();
    /**
     * 比较符
     */
    public static final List<String> compares = new ArrayList();
    /**
     * 逻辑符
     */
    public static final List<String> bool = new ArrayList();


    /**
     * The constant ADD.
     */
    public static final char ADD = '+';
    /**
     * The constant SUBTRACT.
     */
    public static final char SUBTRACT = '-';
    /**
     * The constant MULTIPLY.
     */
    public static final char MULTIPLY = '*';
    /**
     * The constant DIVIDE.
     */
    public static final char DIVIDE = '/';

    /**
     * The constant ARC_LEFT.
     */
    public static final char ARC_LEFT = '(';
    /**
     * The constant ARC_RIGHT.
     */
    public static final char ARC_RIGHT = ')';

    /**
     * The constant MORE_AND_EQUAL.
     */
    public static final String MORE_AND_EQUAL = ">=";
    /**
     * The constant LESS_AND_EQUAL.
     */
    public static final String LESS_AND_EQUAL = "<=";
    /**
     * The constant NOT_EQUAL.
     */
    public static final String NOT_EQUAL = "!=";
    /**
     * The constant EQUAL.
     */
    public static final String EQUAL = "==";
    /**
     * The constant MORE.
     */
    public static final String MORE = ">";
    /**
     * The constant LESS.
     */
    public static final String LESS = "<";

    /**
     * The constant AND.
     */
    public static final String AND = "&&";
    /**
     * The constant OR.
     */
    public static final String OR = "||";
    /**
     * The constant NOT.
     */
    public static final String NOT = "!";

    /**
     * The constant IFSETOR_Q.
     */
    public static final char IFSETOR_Q = '?';
    /**
     * The constant IFSETOR_T.
     */
    public static final char IFSETOR_T = ':';

    /**
     * The constant SPACE.
     */
    public static final String SPACE = " ";
    /**
     * The constant EMPTY.
     */
    public static final String EMPTY = "";

    static {
        operators.add(ADD);
        operators.add(SUBTRACT);
        operators.add(MULTIPLY);
        operators.add(DIVIDE);
        process.add(ARC_LEFT);
        process.add(ARC_RIGHT);
        calculations.addAll(operators);
        calculations.addAll(process);
        compares.add(MORE_AND_EQUAL);
        compares.add(LESS_AND_EQUAL);
        compares.add(NOT_EQUAL);
        compares.add(EQUAL);
        compares.add(MORE);
        compares.add(LESS);
        bool.add(AND);
        bool.add(OR);
        bool.add(NOT);
        logic.addAll(bool);
        logic.addAll(compares);
    }

    /**
     * 在运算符前后插入空格
     * 这个函数的作用就是处理栈中的两个数据，然后将栈中的两个数据运算之后将结果存储在栈中
     *
     * @param expression
     * @return
     */
    private static String insetNumberBlanks(String expression) {
        String result = EMPTY;
        for (int i = 0; i < expression.length(); i++) {
            if (calculations.contains(expression.charAt(i))) {
                result += SPACE + expression.charAt(i) + SPACE;
            } else {
                result += expression.charAt(i);
            }
        }
        return result;
    }

    /**
     * 在比较符前后插入空格
     * 这个函数的作用就是处理栈中的两个数据，然后将栈中的两个数据运算之后将结果存储在栈中
     *
     * @param expression
     * @return
     */
    private static String insetBooleanBlanks(String expression) {
        for (String str : compares) {
            if (str.length() > 1) {
                expression = expression.replace(str, SPACE + str + SPACE);
            }
        }
        for (String str : bool) {
            if (str.length() > 1) {
                expression = expression.replace(str, SPACE + str + SPACE);
            }
        }
        for (int i = 1; i < expression.length(); i++) {
            String charAt = EMPTY + expression.charAt(i);
            if (MORE.equals(charAt) || LESS.equals(charAt)) {
                if (!SPACE.equals(EMPTY + expression.charAt(i - 1))) {
                    expression = expression.substring(0, i) + SPACE + charAt + SPACE + expression.substring(++i);
                }
            }
        }
        for (int i = 0; i < expression.length(); i++) {
            String charAt = EMPTY + expression.charAt(i);
            if (NOT.equals(charAt) && i < expression.length() - 1) {
                if (!(EMPTY + expression.charAt(i) + EMPTY + expression.charAt(i + 1)).equals(NOT_EQUAL)) {
                    expression = expression.substring(0, i) + charAt + SPACE + expression.substring(++i);
                }
            }
        }
        return expression;
    }

    /**
     * Execute t.
     *
     * @param <T>        the type parameter
     * @param expression the expression
     * @param tClass     the t class
     * @return the t
     */
    public static <T> T execute(String expression, Class<T> tClass) {
        AssertUtil.isTrue(classes.contains(tClass), "暂不支持" + tClass + "类型");
        expression = expression.replace(SPACE, EMPTY);
        try {
            if (Boolean.class.equals(tClass)) {
                return (T) executeBoolean(expression);
            }
            return executeNumber(insetNumberBlanks(expression), tClass);
        } catch (Exception e) {
            logger.error("执行数学表达式出错", e);
        }
        return null;
    }

    private static Boolean executeBoolean(String expression) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        //数字运算后的结果
        String afterExpression = EMPTY;
        String blanks = insetBooleanBlanks(expression);
        String[] split = blanks.split(SPACE);
        for (String str : split) {
            if (!compares.contains(str) && !bool.contains(str)) {
                BigDecimal number = executeNumber(insetNumberBlanks(str), BigDecimal.class);
                afterExpression += number;
            } else {
                afterExpression += str;
            }
        }
        return toCompare(insetBooleanBlanks(afterExpression));
    }

    /**
     * 执行简单的数字运算
     *
     * @param expression 表达式
     * @return
     */
    private static <T> T executeNumber(String expression, Class<T> tClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = tClass.getConstructor(String.class);
        boolean isContains = false;
        for (Character character : calculations) {
            if (expression.contains(character.toString())) {
                isContains = true;
            }
        }
        if (!isContains) {
            return constructor.newInstance(expression.trim());
        }
        //数字符
        Stack<T> operandStack = new Stack();
        //运算符
        Stack<Character> operatorStack = new Stack();
        String[] tokens = expression.split(SPACE);
        for (String token : tokens) {
            //如果是空格的话就继续循环，什么也不操作
            if (token.length() == 0) {
                continue;
            }
            //如果是加减的话，因为加减的优先级最低，因此这里的只要遇到加减号，无论操作符栈中的是什么运算符都要运算
            else if (token.charAt(0) == ADD || token.charAt(0) == SUBTRACT) {
                //当栈不是空的，并且栈中最上面的一个元素是加减乘除的人任意一个
                while (!operatorStack.isEmpty() && (operators.contains(operatorStack.peek()))) {
                    //开始运算
                    operator(operandStack, operatorStack);
                }
                //运算完之后将当前的运算符入栈
                operatorStack.push(token.charAt(0));
            }
            //当前运算符是乘除的时候，因为优先级高于加减，因此要判断最上面的是否是乘除，如果是乘除就运算，否则的话直接入栈
            else if (token.charAt(0) == MULTIPLY || token.charAt(0) == DIVIDE) {
                while (!operatorStack.isEmpty() && (operatorStack.peek() == DIVIDE || operatorStack.peek() == MULTIPLY)) {
                    operator(operandStack, operatorStack);
                }
                //将当前操作符入栈
                operatorStack.push(token.charAt(0));
            }
            //如果是左括号的话直接入栈，什么也不用操作,trim()函数是用来去除空格的，由于上面的分割操作可能会令操作符带有空格
            else if (token.trim().charAt(0) == ARC_LEFT) {
                operatorStack.push(ARC_LEFT);
            }
            //如果是右括号的话，清除栈中的运算符直至左括号
            else if (token.trim().charAt(0) == ARC_RIGHT) {
                while (operatorStack.peek() != ARC_LEFT) {
                    //开始运算
                    operator(operandStack, operatorStack);
                }
                //这里的是运算完之后清除左括号
                operatorStack.pop();
            }
            //这里如果是数字的话直接如数据的栈
            else {
                //将数字字符串转换成数字然后压入栈中
                operandStack.push(constructor.newInstance(token));
            }
        }
        //最后当栈中不是空的时候继续运算，知道栈中为空即可
        while (!operatorStack.isEmpty()) {
            operator(operandStack, operatorStack);
        }
        //此时数据栈中的数据就是运算的结果
        return operandStack.pop();
    }

    /**
     * 计算
     * 这个函数的作用就是处理栈中的两个数据，然后将栈中的两个数据运算之后将结果存储在栈中
     *
     * @param operandStack
     * @param operatorStack
     */
    private static <T> void operator(Stack<T> operandStack, Stack<Character> operatorStack) {
        //弹出一个操作符
        char op = operatorStack.pop();
        //从存储数据的栈中弹出连个两个数用来和操作符op运算
        T op1 = operandStack.pop();
        T op2 = operandStack.pop();
        //如果操作符为+就执行加运算
        if (op == ADD) {
            operandStack.push(add(op1, op2));
        }
        //因为这个是栈的结构，自然是上面的数字是后面的，因此用op2-op1
        else if (op == SUBTRACT) {
            operandStack.push(subtract(op2, op1));
        } else if (op == MULTIPLY) {
            operandStack.push(multiply(op1, op2));
        } else if (op == DIVIDE) {
            operandStack.push(divide(op2, op1));
        }
    }

    private static <T> T add(T t1, T t2) {
        if (t1 instanceof BigDecimal) {
            return (T) ((BigDecimal) t1).add((BigDecimal) t2);
        } else if (t1 instanceof Integer) {
            return (T) Integer.valueOf((Integer) t1 + (Integer) t2);
        } else if (t1 instanceof BigInteger) {
            return (T) ((BigInteger) t1).add((BigInteger) t2);
        }
        throw new SkillException(XCode.THIRD_PARTY3000.code, "不支持" + t1.getClass() + "类型的\"+\"运算");
    }

    private static <T> T subtract(T t1, T t2) {
        if (t1 instanceof BigDecimal) {
            return (T) ((BigDecimal) t1).subtract((BigDecimal) t2);
        } else if (t1 instanceof Integer) {
            return (T) Integer.valueOf((Integer) t1 + (Integer) t2);
        } else if (t1 instanceof BigInteger) {
            return (T) ((BigInteger) t1).subtract((BigInteger) t2);
        }
        throw new SkillException(XCode.THIRD_PARTY3000.code, "不支持" + t1.getClass() + "类型的\"-\"运算");
    }

    private static <T> T multiply(T t1, T t2) {
        if (t1 instanceof BigDecimal) {
            return (T) ((BigDecimal) t1).multiply((BigDecimal) t2);
        } else if (t1 instanceof Integer) {
            return (T) Integer.valueOf((Integer) t1 + (Integer) t2);
        } else if (t1 instanceof BigInteger) {
            return (T) ((BigInteger) t1).multiply((BigInteger) t2);
        }
        throw new SkillException(XCode.THIRD_PARTY3000.code, "不支持" + t1.getClass() + "类型的\"*\"运算");
    }

    private static <T> T divide(T t1, T t2) {
        if (t1 instanceof BigDecimal) {
            return (T) ((BigDecimal) t1).divide((BigDecimal) t2);
        } else if (t1 instanceof Integer) {
            return (T) Integer.valueOf((Integer) t1 + (Integer) t2);
        } else if (t1 instanceof BigInteger) {
            return (T) ((BigInteger) t1).divide((BigInteger) t2);
        }
        throw new SkillException(XCode.THIRD_PARTY3000.code, "不支持" + t1.getClass() + "类型的\"/\"运算");
    }

    /**
     * 比较前处理
     *
     * @param expression
     * @return
     */
    private static Boolean toCompare(String expression) {
        //数字符
        Stack<BigDecimal> operandStack = new Stack();
        //比较符
        Stack<String> operatorStack = new Stack();
        //逻辑符
        Stack<String> logicStack = new Stack();
        //结果栈
        Stack<Boolean> resultStack = new Stack();
        String[] tokens = expression.split(SPACE);
        for (String token : tokens) {
            //如果是空格的话就继续循环，什么也不操作
            if (token.length() == 0) {
                continue;
            } else if (bool.contains(token)) {
                while (!operandStack.empty() && !operatorStack.empty()) {
                    boolean compare = compare(operandStack, operatorStack);
                    resultStack.push(compare);
                }
                logicStack.push(token);
            } else if (compares.contains(token)) {
                while (!operandStack.empty() && !operatorStack.empty()) {
                    boolean compare = compare(operandStack, operatorStack);
                    resultStack.push(compare);
                }
                operatorStack.push(token);
            } else {
                operandStack.push(new BigDecimal(token));
            }
        }
        //最后当栈中不是空的时候继续运算，知道栈中为空即可
        while (!operandStack.empty() && !operatorStack.empty()) {
            boolean compare = compare(operandStack, operatorStack);
            resultStack.push(compare);
        }
        while (!resultStack.empty() && !logicStack.empty()) {
            logicOperator(resultStack, logicStack);
        }
        return resultStack.pop();
    }

    /**
     * 逻辑运算
     *
     * @param resultStack
     * @param logicStack
     */
    private static void logicOperator(Stack<Boolean> resultStack, Stack<String> logicStack) {
        String op = logicStack.pop();
        Boolean op1 = resultStack.pop();
        Boolean op2 = resultStack.empty() ? null : resultStack.pop();
        if (op.equals(NOT)) {
            resultStack.push(!op1);
        } else if (op.equals(AND)) {
            resultStack.push(op1 && op2);
        } else if (op.equals(OR)) {
            resultStack.push(op1 || op2);
        } else {
            throw new SkillException(XCode.THIRD_PARTY3000.code, "不支持" + op + "逻辑符");
        }
    }

    /**
     * 比较
     *
     * @param operandStack
     * @param operatorStack
     * @return
     */
    private static boolean compare(Stack<BigDecimal> operandStack, Stack<String> operatorStack) {
        //弹出一个操作符
        String op = operatorStack.pop();
        //从存储数据的栈中弹出连个两个数用来和操作符op运算
        BigDecimal op1 = operandStack.pop();
        BigDecimal op2 = operandStack.pop();
        //放回后面的数字
        operandStack.push(op1);
        if (op.equals(MORE)) {
            return op2.compareTo(op1) > 0;
        } else if (op.equals(LESS)) {
            return op2.compareTo(op1) < 0;
        } else if (op.equals(EQUAL)) {
            return op2.compareTo(op1) == 0;
        } else if (op.equals(MORE_AND_EQUAL)) {
            return op2.compareTo(op1) >= 0;
        } else if (op.equals(LESS_AND_EQUAL)) {
            return op2.compareTo(op1) <= 0;
        } else if (op.equals(NOT_EQUAL)) {
            return op2.compareTo(op1) != 0;
        }
        throw new SkillException(XCode.THIRD_PARTY3000.code, "不支持" + op + "比较符");
    }


    /**
     * 获取表达式最终结果类型.
     *
     * @param expression the expression
     * @return the class
     */
    public static Class getClass(String expression) {
        Class clazz = BigDecimal.class;
        for (int i = 0; i < expression.length(); i++) {
            if (logic.contains(expression.charAt(i))) {
                return Boolean.class;
            }
        }
        return clazz;
    }
}
