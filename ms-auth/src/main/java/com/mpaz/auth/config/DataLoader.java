package com.mpaz.auth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mpaz.auth.model.Usuario;
import com.mpaz.auth.repository.UsuarioRepository;

/**
 * Carga los usuarios ficticios de prueba solo si la tabla está vacía.
 * El ORDEN importa: los demás servicios asumen estos identificadores (1 a 10).
 * Todos usan el mismo PIN de prueba (propiedad mpaz.seed.pin).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String pin;

    public DataLoader(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                      @Value("${mpaz.seed.pin:1234}") String pin) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.pin = pin;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(pin);
        List<Usuario> usuarios = List.of(
                nuevo("utp", "Jefatura UTP", "UTP", hash),                 // id 1
                nuevo("docente", "Docente de prueba", "DOCENTE", hash),    // id 2
                nuevo("ana", "Ana", "ESTUDIANTE", hash),                   // id 3  (2° A)
                nuevo("beto", "Beto", "ESTUDIANTE", hash),                 // id 4  (2° A)
                nuevo("carla", "Carla", "ESTUDIANTE", hash),               // id 5  (2° A)
                nuevo("gael", "Gael", "ESTUDIANTE", hash),                 // id 6  (2° A)
                nuevo("diego", "Diego", "ESTUDIANTE", hash),               // id 7  (2° B)
                nuevo("eva", "Eva", "ESTUDIANTE", hash),                   // id 8  (2° B)
                nuevo("isidora", "Isidora", "ESTUDIANTE", hash),           // id 9  (2° B)
                nuevo("felipe", "Felipe", "ESTUDIANTE", hash)              // id 10 (2° B)
        );
        // Se guardan de uno en uno para que los identificadores salgan en este orden.
        usuarios.forEach(usuarioRepository::save);
    }

    private Usuario nuevo(String usuario, String nombre, String rol, String hash) {
        return new Usuario(null, usuario, hash, rol, nombre, true);
    }
}
