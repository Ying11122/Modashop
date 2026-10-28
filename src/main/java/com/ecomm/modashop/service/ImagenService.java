package com.ecomm.modashop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImagenService {

    private final Path directorio;

    public ImagenService(@Value("${app.uploads-dir}") String carpeta) throws IOException {
        this.directorio = Path.of(carpeta).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
    }

    /** Guarda la imagen y devuelve la URL pública (por ejemplo /uploads/abc.jpg). */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se ha enviado ningún archivo");
        }

        String extension = detectarExtension(archivo);
        String nombre = UUID.randomUUID() + "." + extension;

        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, directorio.resolve(nombre));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen");
        }
        return "/uploads/" + nombre;
    }

    // Se mira el contenido real del archivo (sus primeros bytes), no el nombre ni el tipo
    // que declara el cliente, que pueden falsearse.
    private String detectarExtension(MultipartFile archivo) {
        byte[] b = new byte[12];
        int leidos;
        try (InputStream in = archivo.getInputStream()) {
            leidos = in.readNBytes(b, 0, b.length);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo leer el archivo");
        }

        if (leidos == 12) {
            // JPEG: FF D8 FF
            if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
                return "jpg";
            }
            // PNG: 89 'P' 'N' 'G' 0D 0A 1A 0A
            if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                    && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
                return "png";
            }
            // WebP: 'RIFF' .... 'WEBP'
            if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
                return "webp";
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Formato no admitido. Usa JPG, PNG o WebP");
    }
}