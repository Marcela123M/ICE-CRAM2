package com.example.IceCream_SpringBoot.controller;

import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.service.HeladoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HeladoController {

    @Autowired
    private HeladoService heladoService; // Se inyecta correctamente

    @PostMapping("/agregarAlAlmacen")
    public String registerHelado(@RequestParam String nombre,
            @RequestParam String sabor,
            @RequestParam String tipo,
            @RequestParam double precio,
            @RequestParam int unidades,
            Model model) {
        if (heladoService.heladoExiste(nombre)) {
            model.addAttribute("error", "El nombre del helado ya existe");
            return "index";
        }

        boolean success = heladoService.agregarAlAlmacen(nombre, sabor, tipo, precio, unidades);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al registrar el helado. Inténtalo de nuevo.");
            return "index";
        }
    }

    @GetMapping("/moverHelado")
    public String mostrarFormularioMoverHelado(Model model) {
        List<HeladoDocument> listaHelados = heladoService.getListaAlmacen();
        model.addAttribute("helados", listaHelados);
        return "MoverAheladeria";
    }

    @PostMapping("/moverHelado")
    public String moverHelado(@RequestParam String nombreHelado,
            @RequestParam int unidadesMover,
            Model model) {
        boolean success = heladoService.moverHeladoAHeladeria(nombreHelado, unidadesMover);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al mover el helado.");
            return "MoverAheladeria";
        }
    }

    @GetMapping("/heladosAlmacen")
    public String mostrarHeladosAlmacen(Model model) {
        List<HeladoDocument> listaHelados = heladoService.getListaAlmacen();
        model.addAttribute("helados", listaHelados);
        return "listaHelados";
    }

    @GetMapping("/heladosHeladeria")
    public String mostrarHeladosHeladeria(Model model) {
        List<HeladoDocument> listaHelados = heladoService.getListaHeladeria();
        model.addAttribute("helados", listaHelados);
        return "listaHelados";
    }

    @PostMapping("/obtenerHeladosPorUbicacion")
    @ResponseBody
    public List<HeladoDocument> obtenerHeladosPorUbicacion(@RequestParam String ubicacion) {
        if (ubicacion.equals("almacen")) {
            return heladoService.getListaAlmacen();
        } else if (ubicacion.equals("heladeria")) {
            return heladoService.getListaHeladeria();
        }
        return List.of();
    }

    @PostMapping("/obtenerDatosHelado")
    @ResponseBody
    public HeladoDocument obtenerDatosHelado(@RequestParam String nombreHelado, @RequestParam String ubicacion) {
        return heladoService.obtenerHeladoPorNombreYUbicacion(nombreHelado, ubicacion);
    }

    @GetMapping("/editarHelado")
    public String mostrarFormularioEditar(Model model) {
        return "EditarHelado";
    }

    @PostMapping("/editarHelado")
    public String editarHelado(@RequestParam String ubicacion,
            @RequestParam String nombreOriginal,
            @RequestParam String nombreNuevo,
            @RequestParam String sabor,
            @RequestParam String tipo,
            @RequestParam int unidades,
            @RequestParam double precio,
            Model model) {
        boolean success = heladoService.editarHelado(ubicacion, nombreOriginal, nombreNuevo, sabor, tipo, unidades, precio);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al editar el helado.");
            return "EditarHelado";
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request,
            Model model) {
        model.addAttribute("error", "Faltan campos requeridos");

        String requestURI = request.getRequestURI();

        if (requestURI.contains("/editarHelado")) {
            return "EditarHelado";
        } else if (requestURI.contains("/eliminarHelado")) {
            return "EliminarHelado";
        } else if (requestURI.contains("/moverHelado")) {
            return "MoverAheladeria";
        } else {
            return "ErrorGeneral";
        }
    }

    @GetMapping("/eliminarHelado")
    public String mostrarEliminarHelado(Model model) {
        return "EliminarHelado";
    }

    @PostMapping("/eliminarHelado")
    public String eliminarHelado(@RequestParam String ubicacion,
            @RequestParam String nombre,
            Model model) {
        boolean success = heladoService.eliminarHelado(ubicacion, nombre);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al eliminar el helado.");
            return "EliminarHelado";
        }
    }

    @GetMapping("/venderHelados")
    public String mostrarVenderHelados(Model model) {
        List<HeladoDocument> listaHelados = heladoService.getListaHeladeria();
        model.addAttribute("helados", listaHelados);
        return "VenderHelados";
    }

    @PostMapping("/venderHelados")
    public String venderHelados(@RequestParam String nombreHelado,
            @RequestParam int unidadesVender,
            @RequestParam String metodoPago,
            @RequestParam double totalAPagar,
            Model model) {
        boolean success = heladoService.venderHelados(nombreHelado, unidadesVender, metodoPago, totalAPagar);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al vender el helado.");
            return "VenderHelados";
        }
    }

    @GetMapping("/heladosVendidos")
    @ResponseBody
    public List<HeladoDocument> getHeladosVendidos() {
        return heladoService.getListaHeladosVendidos();
    }

    @GetMapping("/heladosVendidosEfectivo")
    @ResponseBody
    public List<HeladoDocument> getHeladosEfectivo() {
        return heladoService.getListaVendidoEfectivo();
    }

    @GetMapping("/heladosVendidosTarjeta")
    @ResponseBody
    public List<HeladoDocument> getHeladosTarjeta() {
        return heladoService.getListaVendidoTarjeta();
    }

    @GetMapping("grafica")
    public String mostrarHeladosVendidos(Model model) {
        return "Grafica";
    }

    @GetMapping("graficaEfectivo")
    public String mostrarHeladosVendidosEfectivo(Model model) {
        return "GraficaEfectivo";
    }

    @GetMapping("graficaTarjeta")
    public String mostrarHeladosVendidosTarjeta(Model model) {
        return "GraficaTarjeta";
    }
}
