package com.example.IceCream_SpringBoot.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.service.HeladoService;
import com.example.IceCream_SpringBoot.service.VentaService;

import java.util.List;

@Controller
public class VentaController {

    @Autowired
    private HeladoService heladoService;

    @Autowired
    private VentaService ventaService;

    @GetMapping("/venderHelados")
    public String mostrarFormularioVender(Model model) {
        // 1) Carga lista de helados que están en la "heladeria"
        List<HeladoDocument> listaHelados = heladoService.getListaHeladeria();
        model.addAttribute("helados", listaHelados);
        return "VenderHelados";
    }

    @PostMapping("/venderHelados")
    public String procesarVenta(
            @RequestParam String nombreHelado,
            @RequestParam int unidadesVender,
            @RequestParam String metodoPago,
            @RequestParam double totalAPagar,
            @RequestParam String nombreCliente,
            @RequestParam String cedula,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @RequestParam String telefono,
            Model model,
            Principal principal
    ) {
        String vendedor = principal.getName();
        boolean success = ventaService.procesarVenta(
            nombreHelado,
            unidadesVender,
            metodoPago,
            totalAPagar,
            nombreCliente,
            cedula,
            fechaNacimiento.atStartOfDay(),
            telefono,
            vendedor
        );

        if (success) {
            model.addAttribute("mensaje", "¡Venta realizada con éxito!");
        } else {
            model.addAttribute("error", "Error al vender el helado.");
        }

        // 2) Recargar la lista para remostrar el formulario
        List<HeladoDocument> listaHelados = heladoService.getListaHeladeria();
        model.addAttribute("helados", listaHelados);

        return "VenderHelados";
    }
}