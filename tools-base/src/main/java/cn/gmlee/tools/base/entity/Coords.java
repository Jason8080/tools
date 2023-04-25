package cn.gmlee.tools.base.entity;

import lombok.Data;

import javax.validation.constraints.Digits;

/**
 * @author Jas
 */
@Data
public class Coords {
    /**
     * 经度
     */
    @Digits(integer = 3, fraction = 18, message = "非法经度")
    private String lon;
    /**
     * 纬度
     */
    @Digits(integer = 2, fraction = 18, message = "非法纬度")
    private String lat;
    /**
     * 距离
     */
    private Long distance;

    public Coords() {
    }

    public Coords(String lon, String lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public Coords(String lon, String lat, Long distance) {
        this.lon = lon;
        this.lat = lat;
        this.distance = distance;
    }
}
