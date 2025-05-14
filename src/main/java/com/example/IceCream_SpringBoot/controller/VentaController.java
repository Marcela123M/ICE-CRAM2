package com.example.IceCream_SpringBoot.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.IceCream_SpringBoot.model.Cliente;
import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.repository.ClienteRepository;
import com.example.IceCream_SpringBoot.service.HeladoService;
import com.example.IceCream_SpringBoot.service.VentaService;

import java.util.List;

@Controller
public class VentaController {

    @Autowired
    private HeladoService heladoService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ClienteRepository clienteRepository;

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
            Principal principal) {
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
                vendedor);

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

    @GetMapping("/buscarCliente")
    @ResponseBody
    public ResponseEntity<?> buscarCliente(@RequestParam String cedula) {
        Cliente cliente = clienteRepository.findByCedula(cedula);
        if (cliente != null) {
            return ResponseEntity.ok(cliente);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

}