/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.he.zhao.bbs.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.he.zhao.bbs.model.Common;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Geography utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.0, Jun 10, 2018
 * @since 1.3.0
 */
public final class Geos {

    /**
     * Logger.
     */
    public static final Logger logger = LoggerFactory.getLogger(Geos.class);

    /**
     * Private constructor.
     */
    private Geos() {
    }

    /**
     * Gets country, province and city of the specified IP.
     *
     * @param ip the specified IP
     * @return address info, for example      <pre>
     * {
     *     "country": "",
     *     "province": "",
     *     "city": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public static JSONObject getAddress(final String ip) {
        final String ak = Symphonys.get("baidu.lbs.ak");
        if (StringUtils.isBlank(ak) || !Networks.isIPv4(ip)) {
            return null;
        }

        HttpURLConnection conn = null;
        try {
            final URL url = new URL("http://api.map.baidu.com/location/ip?ip=" + ip + "&ak=" + ak);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            final StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }

            final JSONObject data = new JSONObject(sb.toString());
            if (0 != data.optInt("status")) {
                return getAddressTaobao(ip);
            }

            final String content = data.optString("address");
            final String country = content.split("\\|")[0];
            if (!"CN".equals(country) && !"HK".equals(country) && !"TW".equals(country)) {
                logger.warn( "Found other country via Baidu [" + country + ", " + ip + "]");

                return null;
            }

            final String province = content.split("\\|")[1];
            String city = content.split("\\|")[2];
            if ("None".equals(province) || "None".equals(city)) {
                return getAddressTaobao(ip);
            }

            city = StringUtils.replace(city, "市", "");

            final JSONObject ret = new JSONObject();
            ret.put(Common.COUNTRY, "中国");
            ret.put(Common.PROVINCE, province);
            ret.put(Common.CITY, city);

            return ret;
        } catch (final SocketTimeoutException e) {
            logger.error( "Get location from Baidu timeout [ip=" + ip + "]");

            return null;
        } catch (final Exception e) {
            logger.error( "Can't get location from Baidu [ip=" + ip + "]", e);

            return null;
        } finally {
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (final Exception e) {
                    logger.error( "Close HTTP connection error", e);
                }
            }
        }
    }

    /**
     * Gets province, city of the specified IP by Sina API.
     *
     * @param ip the specified IP
     * @return address info, for example      <pre>
     * {
     *     "province": "",
     *     "city": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    private static JSONObject getAddressSina(final String ip) {
        HttpURLConnection conn = null;
        try {
            final URL url = new URL("http://int.dpool.sina.com.cn/iplookup/iplookup.php?ip=" + ip + "&format=json");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            final StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }

            final JSONObject data = new JSONObject(sb.toString());
            if (1 != data.optInt("ret")) {
                return null;
            }

            final String country = data.optString("country");
            final String province = data.optString("province");
            String city = data.optString("city");
            city = StringUtils.replace(city, "市", "");

            final JSONObject ret = new JSONObject();
            ret.put(Common.COUNTRY, country);
            ret.put(Common.PROVINCE, province);
            ret.put(Common.CITY, city);

            return ret;
        } catch (final Exception e) {
            logger.error( "Can't get location from Sina [ip=" + ip + "]", e);

            return null;
        } finally {
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (final Exception e) {
                    logger.error( "Close HTTP connection error", e);
                }
            }
        }
    }

    /**
     * Gets province, city of the specified IP by Taobao API.
     *
     * @param ip the specified IP
     * @return address info, for example      <pre>
     * {
     *     "province": "",
     *     "city": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    private static JSONObject getAddressTaobao(final String ip) {
        HttpURLConnection conn = null;
        try {
            final URL url = new URL("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            final StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }

            final JSONObject data = new JSONObject(sb.toString());
            if (0 != data.optInt("code")) {
                return null;
            }

            final String country = data.optString("country");
            final String province = data.optString("region");
            String city = data.optString("city");
            city = StringUtils.replace(city, "市", "");

            final JSONObject ret = new JSONObject();
            ret.put(Common.COUNTRY, country);
            ret.put(Common.PROVINCE, province);
            ret.put(Common.CITY, city);

            return ret;
        } catch (final Exception e) {
            logger.error( "Can't get location from Taobao [ip=" + ip + "]", e);

            return null;
        } finally {
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (final Exception e) {
                    logger.error( "Close HTTP connection error", e);
                }
            }
        }
    }
}

