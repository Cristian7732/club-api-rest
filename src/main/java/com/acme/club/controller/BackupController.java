package com.acme.club.controller;

import org.h2.tools.Script;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.core.io.InputStreamResource;

@Controller
@RequestMapping("/api")
public class BackupController {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @GetMapping("/db")
    public String backup(HttpServletRequest request, Model model) {
        String message = (String) request.getSession().getAttribute("message");
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "backup";
    }

    @PostMapping("/backup")
    public String createBackup(@RequestParam("user") String user, HttpServletRequest request,
            HttpSession session) {
        try (Connection connection = DriverManager.getConnection(databaseUrl, username, password)) {
            String[] parts = databaseUrl.split(";");
            String databaseName = parts[0].substring(parts[0].lastIndexOf(":") + 1) + "_" + user;
            Script.process(connection, "./" + databaseName + ".sql", "", "");

            File file = new File("./" + databaseName + ".sql");
            if (!file.exists()) {
                return "error";
            }

            request.getSession().setAttribute("message", "Backup file created successfully");
            request.getSession().setAttribute("user", user); // Almacenar el nombre de usuario en la sesión

            // Elimina el atributo de error de la sesión
            session.removeAttribute("error");
            return "redirect:/api/db";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/download")
    public String downloadBackup(HttpSession session) {
        String user = (String) session.getAttribute("user"); // Obtener el nombre de usuario de la sesión
        String[] parts = databaseUrl.split(";");
        String databaseName = parts[0].substring(parts[0].lastIndexOf(":") + 1) + "_" + user;
        File file = new File("./" + databaseName + ".sql");
        if (!file.exists()) {
            session.setAttribute("error", "Backup file not found");
            return "redirect:/api/db";
        }
        session.removeAttribute("error");
        return "redirect:/api/downloadFile";
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<?> downloadFile(HttpSession session) {
        String user = (String) session.getAttribute("user"); // Obtener el nombre de usuario de la sesión
        String[] parts = databaseUrl.split(";");
        String databaseName = parts[0].substring(parts[0].lastIndexOf(":") + 1) + "_" + user;
        File file = new File("./" + databaseName + ".sql");
        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/sql"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .body(resource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create InputStreamResource");
        } finally {
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    // Elimina el atributo de éxito de la sesión
                    session.removeAttribute("message");
                }
            }
        }
    }

}
