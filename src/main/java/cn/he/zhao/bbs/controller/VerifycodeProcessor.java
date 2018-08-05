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

import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.util.Symphonys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Verifycode processor.
 * <ul>
 * <li>Send email verifycode (/cron/verifycode/email), GET</li>
 * <li>Remove expired verifycodes (/cron/verifycode/remove-expired), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.5, May 31, 2018
 * @since 1.3.0
 */
@Controller
public class VerifycodeProcessor {

    /**
     * Verifycode management service.
     */
    @Autowired
    private VerifycodeMgmtService verifycodeMgmtService;

    /**
     * Sends email register verifycode.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/verifycode/email", method = RequestMethod.GET)
    public void sendEmailRegisterVerifycode(final HTTPRequestContext context,
                                            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        verifycodeMgmtService.sendEmailVerifycode();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Remove expired verifycodes.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/verifycode/remove-expired", method = RequestMethod.GET)
    public void removeExpriedVerifycodes(final HTTPRequestContext context,
                                         final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        verifycodeMgmtService.removeExpiredVerifycodes();

        context.renderJSON().renderTrueResult();
    }
}
