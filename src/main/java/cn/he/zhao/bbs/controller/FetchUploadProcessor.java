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
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entity.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.util.net.MimeTypes;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Fetch file and upload processor.
 * <p>
 * <ul>
 * <li>Fetches the remote file and upload it (/fetch-upload), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Apr 5, 2018
 * @since 1.5.0
 */
@Controller
public class FetchUploadProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUploadProcessor.class);

    /**
     * Option query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Fetches the remote file and upload it.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/fetch-upload", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void fetchUpload(Map<String, Object> dataModel,
                            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            return;
        }

        final String originalURL = requestJSONObject.optString(Common.URL);

        HttpResponse res = null;
        byte[] data;
        String contentType;
        try {
            final HttpRequest req = HttpRequest.get(originalURL);
            res = req.send();

            if (HttpServletResponse.SC_OK != res.statusCode()) {
                return;
            }

            data = res.bodyBytes();
            contentType = res.contentType();
        } catch (final Exception e) {
            LOGGER.error( "Fetch file [url=" + originalURL + "] failed", e);

            return;
        } finally {
            if (null != res) {
                try {
                    res.close();
                } catch (final Exception e) {
                    LOGGER.error( "Close response failed", e);
                }
            }
        }

        String suffix;
        String[] exts = MimeTypes.findExtensionsByMimeTypes(contentType, false);
        if (null != exts && 0 < exts.length) {
            suffix = exts[0];
        } else {
            suffix = StringUtils.substringAfter(contentType, "/");
        }

        final String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;

        if (Symphonys.getBoolean("qiniu.enabled")) {
            final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
            final UploadManager uploadManager = new UploadManager(new Configuration());

            uploadManager.put(data, "e/" + fileName, auth.uploadToken(Symphonys.get("qiniu.bucket")),
                    null, contentType, false);

            dataModel.put(Common.URL, Symphonys.get("qiniu.domain") + "/e/" + fileName);
            dataModel.put("originalURL", originalURL);
        } else {
            try (final OutputStream output = new FileOutputStream(Symphonys.get("upload.dir") + fileName)) {
                IOUtils.write(data, output);
            }

            dataModel.put(Common.URL,  SpringUtil.getServerPath() + "/upload/" + fileName);
            dataModel.put("originalURL", originalURL);
        }

        dataModel.put(Keys.STATUS_CODE,true);
    }
}
