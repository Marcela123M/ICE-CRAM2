package com.example.IceCream_SpringBoot.controller;

import com.example.IceCream_SpringBoot.model.Helado;
import com.example.IceCream_SpringBoot.service.HeladoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HeladoController {

    HeladoService heladoService = new HeladoService();

    @PostMapping("/agregarAlAlmacen")
    public String registerHelado(@RequestParam String nombre,
                            @RequestParam String sabor,
                            @RequestParam double precio,
                            @RequestParam int unidades,
                            Model model) {
        if (heladoService.heladoExiste(nombre)) {
            model.addAttribute("error", "El nombre del helado ya existe");
        }

        boolean success = heladoService.agregarAlAlmacen(nombre, sabor, precio, unidades);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al registrar el helado. Inténtalo de nuevo.");
            return "index";
        }
    }

    @GetMapping("/moverHelado")
    public String mostrarFormularioMoverHelado(Model model) {
        List<Helado> listaHelados = heladoService.getListaAlmacen();
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
        List<Helado> listaHelados = heladoService.getListaAlmacen();
        model.addAttribute("helados", listaHelados);
        return "listaHelados";
    }

    @GetMapping("/heladosHeladeria")
    public String mostrarHeladosHeladeria(Model model) {
        List<Helado> listaHelados = heladoService.getListaHeladeria();
        model.addAttribute("helados", listaHelados);
        return "listaHelados";
    }

    @GetMapping("/editarHelado")
    public String mostrarFormularioEditar(Model model) {
        return "EditarHelado";
    }

    @PostMapping("/obtenerHeladosPorUbicacion")
    @ResponseBody
    public List<Helado> obtenerHeladosPorUbicacion(@RequestParam String ubicacion) {
        if (ubicacion.equals("almacen")) {
            return heladoService.getListaAlmacen();
        } else if (ubicacion.equals("heladeria")) {
            return heladoService.getListaHeladeria();
        }
        return new ArrayList<>();
    }

    @PostMapping("/obtenerDatosHelado")
    @ResponseBody
    public Helado obtenerDatosHelado(@RequestParam String nombreHelado, @RequestParam String ubicacion) {
        if (ubicacion.equals("almacen")) {
            return heladoService.getListaAlmacen().stream()
                    .filter(h -> h.getNombre().equals(nombreHelado))
                    .findFirst()
                    .orElse(null);
        } else if (ubicacion.equals("heladeria")) {
            return heladoService.getListaHeladeria().stream()
                    .filter(h -> h.getNombre().equals(nombreHelado))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @PostMapping("/editarHelado")
    public String editarHelado(@RequestParam String ubicacion,
                               @RequestParam String nombreOriginal,
                               @RequestParam String nombreNuevo,
                               @RequestParam String sabor,
                               @RequestParam int unidades,
                               @RequestParam double precio,
                               Model model) {

        boolean success = heladoService.editarHelado(ubicacion, nombreOriginal, nombreNuevo, sabor, unidades, precio);

        if (success) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Error al editar el helado.");
            return "EditarHelado";
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request, Model model) {
        String paramName = ex.getParameterName();
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
    public String mostrarEliminarHelado(Model model){
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
        List<Helado> listaHelados = heladoService.getListaHeladeria();
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
    public String getHelados() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(heladoService.getListaHeladosVendidos());
    }

    @GetMapping("grafica")
    public String mostrarHeladosVendidos(Model model) {
        return "Grafica";
    }

}
