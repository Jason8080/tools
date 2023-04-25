package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Int;

import java.math.BigDecimal;

/**
 * 坐标测量工具
 *
 * @author Jas °
 */
public class CoordsUtil {

    /**
     * 3.14159265;  //π
     */
    private static final double PI = Math.PI;
    /**
     * 地球半径
     */
    private static final BigDecimal EARTH_RADIUS_DECIMAL = BigDecimal.valueOf(6378137);
    /**
     * 地球周长
     */
    private static final BigDecimal EARTH_PERIMETER_DECIMAL = BigDecimal.valueOf(24901);
    /**
     * π/180
     */
    private static final double RAD = Math.PI / 180.0;
    private static final BigDecimal RAD_DECIMAL = BigDecimal.valueOf(Math.PI / 180.0);

    /**
     * 常数
     */
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(10000);

    /**
     * 获取指定坐标的范围 (单位米)
     *
     * @param lng  经度
     * @param lat  纬度
     * @param dist 范围（米）
     * @return 返回最大 、最小经纬度minLng, minLat, maxLng, maxLat
     */
    public static Double[] getAround(Double lng, Double lat, Integer dist) {
        AssertUtil.gt(lng, Int.ZERO, String.format("坐标值太小"));
        AssertUtil.gt(lat, Int.ZERO, String.format("坐标值太小"));
        AssertUtil.gt(dist, Int.ZERO, String.format("距离太小"));

        BigDecimal latitude = BigDecimal.valueOf(lat);
        BigDecimal longitude = BigDecimal.valueOf(lng);

        //地球的周长是24901英里
        BigDecimal degree = BigDecimalUtil.multiply(24901, 1609).divide(BigDecimal.valueOf(360));
        BigDecimal distMile = BigDecimal.valueOf(dist);

        //先计算纬度
        BigDecimal dpmLat = BigDecimalUtil.divide(BigDecimal.ONE, degree);
        BigDecimal radiusLat = BigDecimalUtil.multiply(dpmLat, distMile);
        BigDecimal minLat = latitude.subtract(radiusLat);
        BigDecimal maxLat = latitude.subtract(radiusLat);

        //计算经度
        //纬度的余弦
        BigDecimal mpdLng = BigDecimalUtil.multiply(degree.doubleValue(), Math.cos(latitude.multiply(BigDecimalUtil.divide(PI, 180D)).doubleValue()));
        BigDecimal dpmLng = BigDecimalUtil.divide(BigDecimal.ONE, mpdLng);
        BigDecimal radiusLng = BigDecimalUtil.multiply(dpmLng, distMile);
        BigDecimal minLng = longitude.subtract(radiusLng);
        BigDecimal maxLng = longitude.add(radiusLng);
        // 最小经度，最小纬度，最大经度，最大纬度
        return new Double[]{minLng.doubleValue(), minLat.doubleValue(), maxLng.doubleValue(), maxLat.doubleValue()};
    }

    /**
     * 获取两点之间的距离 (单位米).
     *
     * @param lng1 经度1
     * @param lat1 纬度1
     * @param lng2 经度2
     * @param lat2 纬度3
     * @return 返回距离 （米）
     */
    public static Double getDist(Double lng1, Double lat1, Double lng2, Double lat2) {
        if (BoolUtil.gt(lng1, Int.ZERO) && BoolUtil.gt(lat1, Int.ZERO) && BoolUtil.gt(lng2, Int.ZERO) && BoolUtil.gt(lat2, Int.ZERO)) {
            String dist = getDist(lng1.toString(), lat1.toString(), lng2.toString(), lat2.toString());
            return new Double(dist);
        }
        return 0D;
    }

    private static BigDecimal getDist(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (BoolUtil.gt(lng1, BigDecimal.ZERO) && BoolUtil.gt(lat1, BigDecimal.ZERO) && BoolUtil.gt(lng2, BigDecimal.ZERO) && BoolUtil.gt(lat2, BigDecimal.ZERO)) {
            BigDecimal radLat1 = lat1.multiply(RAD_DECIMAL);
            BigDecimal radLat2 = lat2.multiply(RAD_DECIMAL);
            BigDecimal a = radLat1.subtract(radLat2);
            BigDecimal b = (lng1.subtract(lng2)).multiply(RAD_DECIMAL);
            BigDecimal pow = BigDecimal.valueOf(Math.pow(Math.sin(a.divide(TWO).doubleValue()), 2));
            BigDecimal pow1 = BigDecimal.valueOf(Math.pow(Math.sin(b.divide(TWO).doubleValue()), 2));
            BigDecimal cos = BigDecimal.valueOf(Math.cos(radLat1.doubleValue()));
            BigDecimal cos1 = BigDecimal.valueOf(Math.cos(radLat2.doubleValue()));
            BigDecimal asin = BigDecimal.valueOf(Math.asin(Math.sqrt(pow.add(cos.multiply(cos1).multiply(pow1)).doubleValue())));
            BigDecimal s = TWO.multiply(asin);
            s = s.multiply(EARTH_RADIUS_DECIMAL);
            BigDecimal round = BigDecimal.valueOf(Math.round(s.multiply(THOUSAND).doubleValue()));
            s = round.divide(THOUSAND);
            return s;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取两点之间的距离 (单位米).
     *
     * @param lng1 the lng 1
     * @param lat1 the lat 1
     * @param lng2 the lng 2
     * @param lat2 the lat 2
     * @return the dist by big decimal
     */
    public static String getDist(String lng1, String lat1, String lng2, String lat2) {
        if (BoolUtil.gt(lng1, Int.ZERO) && BoolUtil.gt(lat1, Int.ZERO) && BoolUtil.gt(lng2, Int.ZERO) && BoolUtil.gt(lat2, Int.ZERO)) {
            BigDecimal dist = getDist(new BigDecimal(lng1), new BigDecimal(lat1), new BigDecimal(lng2), new BigDecimal(lat2));
            return dist.toString();
        }
        return "0";
    }
}
