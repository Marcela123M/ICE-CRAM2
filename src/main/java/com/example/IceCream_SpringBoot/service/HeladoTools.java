package com.example.IceCream_SpringBoot.service;

import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.model.VentaDocument;
import com.example.IceCream_SpringBoot.repository.VentaRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HeladoTools {

    private final HeladoService heladoService;
    private final VentaRepository ventaRepository;

    public HeladoTools(HeladoService heladoService, VentaRepository ventaRepository) {
        this.heladoService = heladoService;
        this.ventaRepository = ventaRepository;
    }

    @Tool(description = "Lista todos los helados disponibles en el inventario")
    public List<HeladoDocument> listarHelados() {
        List<HeladoDocument> almacen = heladoService.getListaAlmacen();
        List<HeladoDocument> heladeria = heladoService.getListaHeladeria();
        almacen.addAll(heladeria);
        return almacen;
    }

    @Tool(description = "Busca un helado por su nombre")
    public List<HeladoDocument> buscarHeladoPorNombre(String nombre) {
        List<HeladoDocument> almacen = heladoService.getListaAlmacen();
        List<HeladoDocument> heladeria = heladoService.getListaHeladeria();
        almacen.addAll(heladeria);
        return almacen.stream()
                .filter(h -> h.getNombre().equalsIgnoreCase(nombre))
                .collect(Collectors.toList());
    }

    @Tool(description = "Ver las unidades disponibles de un helado especifico por nombre")
    public String verUnidades(String nombre) {
        List<HeladoDocument> almacen = heladoService.getListaAlmacen();
        List<HeladoDocument> heladeria = heladoService.getListaHeladeria();

        StringBuilder resultado = new StringBuilder("Unidades de '" + nombre + "':\n");
        boolean encontrado = false;

        for (HeladoDocument h : almacen) {
            if (h.getNombre().equalsIgnoreCase(nombre)) {
                resultado.append("- Almacen: ").append(h.getUnidades()).append(" unidades\n");
                encontrado = true;
            }
        }
        for (HeladoDocument h : heladeria) {
            if (h.getNombre().equalsIgnoreCase(nombre)) {
                resultado.append("- Heladeria: ").append(h.getUnidades()).append(" unidades\n");
                encontrado = true;
            }
        }

        if (!encontrado) return "No se encontró el helado '" + nombre + "'.";
        return resultado.toString();
    }

    @Tool(description = "Mueve unidades de un helado del almacen a la heladeria")
    public String moverHeladoAHeladeria(String nombre, int unidades) {
        boolean resultado = heladoService.moverHeladoAHeladeria(nombre, unidades);
        if (resultado) {
            return "Se movieron " + unidades + " unidades de '" + nombre + "' del almacen a la heladeria.";
        }
        return "No se pudo mover el helado. Verifica que exista en el almacen y tenga suficientes unidades.";
    }

    @Tool(description = "Elimina un helado por su nombre y ubicacion. Guarda historial.")
    public String eliminarHelado(String nombre, String ubicacion) {
        boolean resultado = heladoService.eliminarHelado(ubicacion, nombre);
        if (resultado) {
            return "Helado '" + nombre + "' eliminado correctamente de " + ubicacion + ".";
        }
        return "No se encontró el helado '" + nombre + "' en " + ubicacion + ".";
    }

    @Tool(description = "Modifica el precio o las unidades de un helado por su nombre y ubicacion. nuevoPrecio debe ser un numero decimal como 15.0, nuevasUnidades debe ser un numero entero como 10.")
    public String modificarHelado(String nombre, String ubicacion, Double nuevoPrecio, Integer nuevasUnidades) {
        try {
            boolean resultado = heladoService.editarHelado(
                    ubicacion, nombre, nombre,
                    null, null,
                    nuevasUnidades != null ? nuevasUnidades : -1,
                    nuevoPrecio != null ? nuevoPrecio : -1
            );
            if (resultado) {
                return "Helado '" + nombre + "' actualizado correctamente.";
            }
            return "No se encontró el helado '" + nombre + "' en " + ubicacion + ".";
        } catch (Exception e) {
            return "Error al modificar el helado: " + e.getMessage();
        }
    }

    @Tool(description = "Agrega un nuevo helado al inventario en almacen o heladeria")
    public String agregarHelado(String nombre, String sabor, String tipo, double precio, int unidades, String ubicacion) {
        if (heladoService.heladoExiste(nombre)) {
            return "El helado '" + nombre + "' ya existe. No se puede agregar duplicado.";
        }
        boolean resultado = heladoService.agregarAlAlmacen(nombre, sabor, tipo, precio, unidades);
        if (resultado) {
            return "Helado '" + nombre + "' agregado correctamente en " + ubicacion + ".";
        }
        return "No se pudo agregar el helado '" + nombre + "'.";
    }

    @Tool(description = "Consulta las ultimas ventas registradas en el sistema")
    public String consultarUltimasVentas() {
        List<VentaDocument> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();
        if (ventas.isEmpty()) return "No hay ventas registradas.";

        int limite = Math.min(10, ventas.size());
        StringBuilder resultado = new StringBuilder("Ultimas " + limite + " ventas:\n");

        for (int i = 0; i < limite; i++) {
            VentaDocument v = ventas.get(i);
            resultado.append("- Fecha: ").append(v.getFechaVenta())
                    .append(" | Vendedor: ").append(v.getVendedor())
                    .append(" | Total: $").append(v.getTotal())
                    .append(" | Pago: ").append(v.getMetodoPago()).append("\n");
        }
        return resultado.toString();
    }

    @Tool(description = "Consulta el total de ventas y el monto total vendido")
    public String consultarResumenVentas() {
        List<VentaDocument> ventas = ventaRepository.findAll();
        if (ventas.isEmpty()) return "No hay ventas registradas.";

        double total = ventas.stream().mapToDouble(VentaDocument::getTotal).sum();
        return "Total de ventas: " + ventas.size() + " | Monto total: $" + total;
    }

    @Tool(description = "Verifica si hay helados con pocas unidades (menos de 5) y alerta al administrador")
    public String verificarStockBajo() {
        List<HeladoDocument> almacen = heladoService.getListaAlmacen();
        List<HeladoDocument> heladeria = heladoService.getListaHeladeria();

        StringBuilder alerta = new StringBuilder();
        boolean hayStockBajo = false;

        alerta.append("⚠️ ALERTA DE STOCK BAJO:\n\n");

        for (HeladoDocument h : almacen) {
            if (h.getUnidades() < 5) {
                alerta.append("• ALMACEN - ").append(h.getNombre())
                        .append(": ").append(h.getUnidades()).append(" unidades\n");
                hayStockBajo = true;
            }
        }

        for (HeladoDocument h : heladeria) {
            if (h.getUnidades() < 5) {
                alerta.append("• HELADERIA - ").append(h.getNombre())
                        .append(": ").append(h.getUnidades()).append(" unidades\n");
                hayStockBajo = true;
            }
        }

        if (!hayStockBajo) return "✅ Todo el inventario tiene stock suficiente.";
        return alerta.toString();
    }

    @Tool(description = "Genera un informe TXT con el inventario completo de helados y lo guarda en la carpeta info")
    public String generarInformeInventario() {
        try {
            List<HeladoDocument> almacen = heladoService.getListaAlmacen();
            List<HeladoDocument> heladeria = heladoService.getListaHeladeria();

            StringBuilder informe = new StringBuilder();
            informe.append("========================================\n");
            informe.append("     INFORME DE INVENTARIO ICE CREAM    \n");
            informe.append("     Fecha: ").append(java.time.LocalDateTime.now()).append("\n");
            informe.append("========================================\n\n");

            informe.append("--- ALMACEN (").append(almacen.size()).append(" helados) ---\n");
            for (HeladoDocument h : almacen) {
                informe.append("• ").append(h.getNombre())
                        .append(" | Sabor: ").append(h.getSabor())
                        .append(" | Precio: $").append(h.getPrecio())
                        .append(" | Unidades: ").append(h.getUnidades()).append("\n");
            }

            informe.append("\n--- HELADERIA (").append(heladeria.size()).append(" helados) ---\n");
            for (HeladoDocument h : heladeria) {
                informe.append("• ").append(h.getNombre())
                        .append(" | Sabor: ").append(h.getSabor())
                        .append(" | Precio: $").append(h.getPrecio())
                        .append(" | Unidades: ").append(h.getUnidades()).append("\n");
            }

            String ruta = "info/informe_inventario.txt";
            java.nio.file.Files.writeString(java.nio.file.Path.of(ruta), informe.toString());
            return "Informe de inventario generado correctamente en 'info/informe_inventario.txt'";
        } catch (Exception e) {
            return "Error al generar el informe: " + e.getMessage();
        }
    }

    @Tool(description = "Genera un informe TXT con el historial de ventas y lo guarda en la carpeta info")
    public String generarInformeVentas() {
        try {
            List<VentaDocument> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();

            StringBuilder informe = new StringBuilder();
            informe.append("========================================\n");
            informe.append("       INFORME DE VENTAS ICE CREAM      \n");
            informe.append("     Fecha: ").append(java.time.LocalDateTime.now()).append("\n");
            informe.append("========================================\n\n");

            if (ventas.isEmpty()) {
                informe.append("No hay ventas registradas.\n");
            } else {
                double totalGeneral = 0;
                informe.append("--- HISTORIAL DE VENTAS (").append(ventas.size()).append(" registros) ---\n\n");
                for (VentaDocument v : ventas) {
                    informe.append("• Fecha: ").append(v.getFechaVenta())
                            .append(" | Vendedor: ").append(v.getVendedor())
                            .append(" | Total: $").append(v.getTotal())
                            .append(" | Pago: ").append(v.getMetodoPago()).append("\n");
                    totalGeneral += v.getTotal();
                }
                informe.append("\n--- RESUMEN ---\n");
                informe.append("Total de ventas: ").append(ventas.size()).append("\n");
                informe.append("Monto total: $").append(totalGeneral).append("\n");
            }

            String ruta = "info/informe_ventas.txt";
            java.nio.file.Files.writeString(java.nio.file.Path.of(ruta), informe.toString());
            return "Informe de ventas generado correctamente en 'info/informe_ventas.txt'";
        } catch (Exception e) {
            return "Error al generar el informe: " + e.getMessage();
        }
    }

    @Tool(description = "Genera un informe TXT completo con inventario y ventas y lo guarda en la carpeta info")
    public String generarInformeCompleto() {
        try {
            List<HeladoDocument> almacen = heladoService.getListaAlmacen();
            List<HeladoDocument> heladeria = heladoService.getListaHeladeria();
            List<VentaDocument> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();

            StringBuilder informe = new StringBuilder();
            informe.append("========================================\n");
            informe.append("     INFORME COMPLETO ICE CREAM         \n");
            informe.append("     Fecha: ").append(java.time.LocalDateTime.now()).append("\n");
            informe.append("========================================\n\n");

            informe.append("--- ALMACEN (").append(almacen.size()).append(" helados) ---\n");
            for (HeladoDocument h : almacen) {
                informe.append("• ").append(h.getNombre())
                        .append(" | Sabor: ").append(h.getSabor())
                        .append(" | Precio: $").append(h.getPrecio())
                        .append(" | Unidades: ").append(h.getUnidades()).append("\n");
            }

            informe.append("\n--- HELADERIA (").append(heladeria.size()).append(" helados) ---\n");
            for (HeladoDocument h : heladeria) {
                informe.append("• ").append(h.getNombre())
                        .append(" | Sabor: ").append(h.getSabor())
                        .append(" | Precio: $").append(h.getPrecio())
                        .append(" | Unidades: ").append(h.getUnidades()).append("\n");
            }

            informe.append("\n--- VENTAS (").append(ventas.size()).append(" registros) ---\n");
            double totalGeneral = 0;
            for (VentaDocument v : ventas) {
                informe.append("• Fecha: ").append(v.getFechaVenta())
                        .append(" | Vendedor: ").append(v.getVendedor())
                        .append(" | Total: $").append(v.getTotal())
                        .append(" | Pago: ").append(v.getMetodoPago()).append("\n");
                totalGeneral += v.getTotal();
            }

            informe.append("\n--- RESUMEN GENERAL ---\n");
            informe.append("Total helados almacen: ").append(almacen.size()).append("\n");
            informe.append("Total helados heladeria: ").append(heladeria.size()).append("\n");
            informe.append("Total ventas: ").append(ventas.size()).append("\n");
            informe.append("Monto total vendido: $").append(totalGeneral).append("\n");

            String ruta = "info/informe_completo.txt";
            java.nio.file.Files.writeString(java.nio.file.Path.of(ruta), informe.toString());
            return "Informe completo generado correctamente en 'info/informe_completo.txt'";
        } catch (Exception e) {
            return "Error al generar el informe: " + e.getMessage();
        }
    }
}