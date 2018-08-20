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
package cn.he.zhao.bbs.controller;

import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.model.*;
import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.model.my.Image;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Strings;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.patchca.color.SingleColorFactory;
import org.patchca.filter.predefined.CurvesRippleFilterFactory;
import org.patchca.service.Captcha;
import org.patchca.service.ConfigurableCaptchaService;
import org.patchca.word.RandomWordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Captcha processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.3.0.0, Jun 12, 2018
 * @since 0.2.2
 */
@Controller
public class CaptchaProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaProcessor.class);

    /**
     * Key of captcha.
     */
    public static final String CAPTCHA = "captcha";

    /**
     * Captchas.
     */
    public static final Set<String> CAPTCHAS = new HashSet<>();

    /**
     * Captcha length.
     */
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * Checks whether the specified captcha is invalid.
     *
     * @param captcha the specified captcha
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    public static boolean invalidCaptcha(final String captcha) {
        if (Strings.isEmptyOrNull(captcha) || captcha.length() != CAPTCHA_LENGTH) {
            return true;
        }

        boolean ret = !CaptchaProcessor.CAPTCHAS.contains(captcha);
        if (!ret) {
            CaptchaProcessor.CAPTCHAS.remove(captcha);
        }

        return ret;
    }

    /**
     * Gets captcha.
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void get(Map<String, Object> dataModel, final HttpServletResponse response) {
//        final PNGRenderer renderer = new PNGRenderer();
//        context.setRenderer(renderer);

        try {
            response.setContentType("image/png");
            OutputStream outputStream = response.getOutputStream();

            final ConfigurableCaptchaService cs = new ConfigurableCaptchaService();
            cs.setColorFactory(new SingleColorFactory(new Color(25, 60, 170)));
            cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory()));
            final RandomWordFactory randomWordFactory = new RandomWordFactory();
            randomWordFactory.setCharacters("abcdefghijklmnprstuvwxy23456789");
            randomWordFactory.setMinLength(CAPTCHA_LENGTH);
            randomWordFactory.setMaxLength(CAPTCHA_LENGTH);
            cs.setWordFactory(randomWordFactory);
            final Captcha captcha = cs.getCaptcha();
            final String challenge = captcha.getChallenge();
            final BufferedImage bufferedImage = captcha.getImage();

            if (CAPTCHAS.size() > 64) {
                CAPTCHAS.clear();
            }

            CAPTCHAS.add(challenge);

//            final HttpServletResponse response = response;
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

//            renderImg(renderer, bufferedImage);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            final byte[] data = baos.toByteArray();
            final Image captchaImg = new Image();
            captchaImg.setData(data);


            outputStream.write(captchaImg.getData());
            outputStream.flush();
            outputStream.close();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Gets captcha for login.
     */
    @RequestMapping(value = "/captcha/login", method = RequestMethod.GET)
    public void getLoginCaptcha(Map<String, Object> dataModel, HttpServletRequest request, HttpServletResponse response) {
        try {
//            final HttpServletRequest request = context.getRequest();
//            final HttpServletResponse response = response;

            final String userId = request.getParameter(Common.NEED_CAPTCHA);
            if (StringUtils.isBlank(userId)) {
                return;
            }

            final JSONObject wrong = LoginProcessor.WRONG_PWD_TRIES.get(userId);
            if (null == wrong) {
                return;
            }

            if (wrong.optInt(Common.WRON_COUNT) < 3) {
                return;
            }

//            final PNGRenderer renderer = new PNGRenderer();
//            context.setRenderer(renderer);
            response.setContentType("image/png");
            OutputStream outputStream = response.getOutputStream();


            final ConfigurableCaptchaService cs = new ConfigurableCaptchaService();
            cs.setColorFactory(new SingleColorFactory(new Color(26, 52, 96)));
            cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory()));
            final RandomWordFactory randomWordFactory = new RandomWordFactory();
            randomWordFactory.setCharacters("abcdefghijklmnprstuvwxy23456789");
            randomWordFactory.setMinLength(CAPTCHA_LENGTH);
            randomWordFactory.setMaxLength(CAPTCHA_LENGTH);
            cs.setWordFactory(randomWordFactory);
            final Captcha captcha = cs.getCaptcha();
            final String challenge = captcha.getChallenge();
            final BufferedImage bufferedImage = captcha.getImage();

            wrong.put(CAPTCHA, challenge);

            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

//            renderImg(renderer, bufferedImage);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            final byte[] data = baos.toByteArray();
            final Image captchaImg = new Image();
            captchaImg.setData(data);


            outputStream.write(captchaImg.getData());
            outputStream.flush();
            outputStream.close();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

//    private void renderImg(final PNGRenderer renderer, final BufferedImage bufferedImage) throws IOException {
//        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            ImageIO.write(bufferedImage, "png", baos);
//            final byte[] data = baos.toByteArray();
//            final Image captchaImg = new Image();
//            captchaImg.setData(data);
//            renderer.setImage(captchaImg);
//        }
//    }
}
