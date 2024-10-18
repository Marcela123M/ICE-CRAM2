package com.example.IceCream_SpringBoot.service;

import com.example.IceCream_SpringBoot.model.User;
import com.example.IceCream_SpringBoot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    //private Set<User> usuariosRegistrados = new HashSet<>();
    @Autowired
    private UserRepository userRepository;

    public boolean usuarioExiste(String usuario) {
        return userRepository.findByUsuario(usuario).isPresent();
    }

    public boolean registrarNuevoUsuario(String usuario, String contrasena, String pin) {
        if (usuarioExiste(usuario)) {
            return false;
        }
        if (!pin.equals("admin123")) {
            return false;
        }

        User user = new User(usuario, contrasena);
        userRepository.save(user);
        return true;
    }

    public boolean validarUsuario(String usuario, String contrasena) {
        Optional<User> user = userRepository.findByUsuario(usuario);
        return user.isPresent() && user.get().getContrasena().equals(contrasena);
    }
}
