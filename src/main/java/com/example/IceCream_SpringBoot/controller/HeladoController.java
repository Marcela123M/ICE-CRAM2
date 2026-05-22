package com.example.IceCream_SpringBoot.controller;

import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.repository.HeladoEliminadoRepository;
import com.example.IceCream_SpringBoot.service.HeladoService;
import com.example.IceCream_SpringBoot.service.WekaPrecioService;
import com.example.IceCream_SpringBoot.service.WekaPredictorService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HeladoController {

    @Autowired
    private HeladoService heladoService;

    @Autowired
    private HeladoEliminadoRepository heladoEliminadoRepository;

    @Autowired
    private WekaPredictorService wekaPredictorService;

    @Autowired
    private WekaPrecioService wekaPrecioService;

    @GetMapping("/agregarAlAlmacen")
    public String agregarAlAlmacen(Model model) {
        List<HeladoDocument> helados = heladoService.getListaAlmacen();
        int totalHelados = helados.size();
        int totalUnidades = helados.stream().mapToInt(HeladoDocument::getUnidades).sum();
        model.addAttribute("totalHelados", totalHelados);
        model.addAttribute("totalUnidades", totalUnidades);
        model.addAttribute("listaHelados", helados);
        return "agregarAlAlmacen";
    }

    @PostMapping("/sumarStockRapido")
    public String sumarStockRapido(@RequestParam String nombreHelado,
            @RequestParam int cantidad,
            RedirectAttributes redirectAttributes) {
        boolean success = heladoService.sumarUnidadesAlmacen(nombreHelado, cantidad);
        if (success) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "Stock actualizado: Se agregaron " + cantidad + " unidades de " + nombreHelado);
        } else {
            redirectAttributes.addFlashAttribute("error", "Error: No se encontró el helado seleccionado.");
        }
        return "redirect:/agregarAlAlmacen";
    }

    @PostMapping("/agregarAlAlmacen")
    public String registerHelado(@RequestParam String nombre,
            @RequestParam String sabor,
            @RequestParam String tipo,
            @RequestParam double precio,
            @RequestParam int unidades,
            RedirectAttributes redirectAttributes) {
        if (heladoService.heladoExiste(nombre)) {
            redirectAttributes.addFlashAttribute("error", "El nombre del helado ya existe.");
            return "redirect:/agregarAlAlmacen";
        }
        boolean success = heladoService.agregarAlAlmacen(nombre, sabor, tipo, precio, unidades);
        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Helado agregado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el helado. Intentalo de nuevo.");
        }
        return "redirect:/agregarAlAlmacen";
    }

    @GetMapping("/moverHelado")
    public String mostrarFormularioMoverHelado(Model model) {
        List<HeladoDocument> listaAlmacen = heladoService.getListaAlmacen();
        List<HeladoDocument> listaHeladeria = heladoService.getListaHeladeria();
        Map<String, Integer> stockHeladeriaMap = listaHeladeria.stream()
                .collect(Collectors.toMap(HeladoDocument::getNombre, HeladoDocument::getUnidades));
        model.addAttribute("helados", listaAlmacen);
        model.addAttribute("stockHeladeriaMap", stockHeladeriaMap);
        return "MoverAheladeria";
    }

    @PostMapping("/moverHelado")
    public String moverHelado(@RequestParam String nombreHelado,
            @RequestParam int unidadesMover,
            RedirectAttributes redirectAttributes) {
        boolean success = heladoService.moverHeladoAHeladeria(nombreHelado, unidadesMover);
        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Helado movido correctamente a la heladeria.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al mover el helado. Verifica las unidades disponibles.");
        }
        return "redirect:/moverHelado";
    }

    @GetMapping("/heladosAlmacen")
    public String mostrarHeladosAlmacen(Model model) {
        model.addAttribute("helados", heladoService.getListaAlmacen());
        return "listaHelados";
    }

    @GetMapping("/heladosHeladeria")
    public String mostrarHeladosHeladeria(Model model) {
        model.addAttribute("helados", heladoService.getListaHeladeria());
        return "listaHelados";
    }

    @PostMapping("/obtenerHeladosPorUbicacion")
    @ResponseBody
    public List<HeladoDocument> obtenerHeladosPorUbicacion(@RequestParam String ubicacion) {
        if ("almacen".equals(ubicacion)) {
            return heladoService.getListaAlmacen();
        } else if ("heladeria".equals(ubicacion)) {
            return heladoService.getListaHeladeria();
        }
        return List.of();
    }

    @PostMapping("/obtenerDatosHelado")
    @ResponseBody
    public HeladoDocument obtenerDatosHelado(@RequestParam String nombreHelado,
            @RequestParam String ubicacion) {
        return heladoService.obtenerHeladoPorNombreYUbicacion(nombreHelado, ubicacion);
    }

    @GetMapping("/editarHelado")
    public String mostrarFormularioEditar() {
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
            RedirectAttributes redirectAttributes) {
        boolean success = heladoService.editarHelado(ubicacion, nombreOriginal, nombreNuevo, sabor, tipo, unidades, precio);
        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Helado editado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al editar el helado.");
        }
        return "redirect:/editarHelado";
    }

    @GetMapping("/eliminarHelado")
    public String mostrarEliminarHelado(Model model) {
        model.addAttribute("heladosDeshabilitados", heladoEliminadoRepository.findAll());
        return "EliminarHelado";
    }

    @PostMapping("/eliminarHelado")
    public String eliminarHelado(@RequestParam String ubicacion,
            @RequestParam String nombre,
            RedirectAttributes redirectAttributes) {
        boolean success = heladoService.eliminarHelado(ubicacion, nombre);
        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "El helado \"" + nombre + "\" fue deshabilitado exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo deshabilitar el helado \"" + nombre + "\". Intenta nuevamente.");
        }
        return "redirect:/eliminarHelado";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException ex,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("error", "Faltan campos requeridos");
        String uri = request.getRequestURI();
        if (uri.contains("/editarHelado")) return "EditarHelado";
        if (uri.contains("/eliminarHelado")) return "EliminarHelado";
        if (uri.contains("/moverHelado")) return "MoverAheladeria";
        return "ErrorGeneral";
    }

    @GetMapping("/reporte")
    public String verReporte() {
        return "reporte";
    }

    @GetMapping("/reporteOpciones")
    public String verReporteOpciones() {
        return "reporteOpciones";
    }

    @GetMapping("/panelPredictivo")
    public String mostrarPanelIA() {
        return "PanelPredictivo";
    }

    @PostMapping("/predecirSabor")
    @ResponseBody
    public String predecirSabor(@RequestParam String tipo,
            @RequestParam double precioUnitario,
            @RequestParam double cantidad,
            @RequestParam double edadCliente,
            @RequestParam String metodoPago) {
        try {
            String sabor = wekaPredictorService.predecirSabor(tipo, precioUnitario, cantidad, edadCliente, metodoPago);
            return "El modelo J48 predice que el cliente elegira: " + sabor.toUpperCase();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/predecirPrecio")
    @ResponseBody
    public String predecirPrecio(@RequestParam String sabor,
            @RequestParam String tipo) {
        try {
            double precio = wekaPrecioService.predecirPrecio(sabor, tipo);
            return String.format("El modelo M5P sugiere un precio de: $%.2f", precio);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    @GetMapping("/modeloMatematico")
    public String modeloMatematico() {
        return "modelo";
}
}
