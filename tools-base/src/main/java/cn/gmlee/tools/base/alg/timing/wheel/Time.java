package cn.gmlee.tools.base.alg.timing.wheel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The type Time.
 */
@Data
@EqualsAndHashCode
public class Time implements Serializable {
    /**
     * 当前时间 1 ~ max
     */
    protected volatile AtomicInteger current = new AtomicInteger(0);
    /**
     * 当前轮数 0 ~ 2^63-1
     */
    protected volatile AtomicLong age = new AtomicLong(0);

    /**
     * Instantiates a new Time.
     *
     * @param age     the age
     * @param current the current
     */
    public Time(long age, int current){
        this.age.set(age);
        this.current.set(current);
    }
}
