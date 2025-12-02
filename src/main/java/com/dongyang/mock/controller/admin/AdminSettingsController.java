package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DTO.ServerStatsDTO;
import com.dongyang.mock.util.ServerStatsUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/settings")
public class AdminSettingsController extends BaseAdminController {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 서버 상태 조회
        ServerStatsDTO serverStats = ServerStatsUtil.getServerStats();
        req.setAttribute("serverStats", serverStats);

        setCurrentPath(req, "/admin/settings");
        req.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp").forward(req, resp);
    }
}

