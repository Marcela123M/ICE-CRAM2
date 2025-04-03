package com.example.IceCream_SpringBoot.service;

import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.repository.HeladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HeladoService {

    @Autowired
    private HeladoRepository heladoRepository;

    public boolean heladoExiste(String nombre) {
        return heladoRepository.existsByNombre(nombre);
    }

    public boolean agregarAlAlmacen(String nombre, String sabor, String tipo, double precio, int unidades) {
        if (heladoExiste(nombre)) {
            return false;
        }
        HeladoDocument helado = new HeladoDocument(nombre, sabor, tipo, precio, unidades, "almacen");
        heladoRepository.save(helado);
        return true;
    }

    public boolean moverHeladoAHeladeria(String nombre, int unidadesMover) {
        Optional<HeladoDocument> optionalHelado = heladoRepository.findByUbicacion("almacen")
                .stream().filter(h -> h.getNombre().equals(nombre)).findFirst();

        if (optionalHelado.isEmpty() || optionalHelado.get().getUnidades() < unidadesMover) {
            return false;
        }

        HeladoDocument heladoAlmacen = optionalHelado.get();
        heladoAlmacen.setUnidades(heladoAlmacen.getUnidades() - unidadesMover);
        heladoRepository.save(heladoAlmacen);

        Optional<HeladoDocument> optionalHeladeria = heladoRepository.findByUbicacion("heladeria")
                .stream().filter(h -> h.getNombre().equals(nombre)).findFirst();

        if (optionalHeladeria.isPresent()) {
            HeladoDocument heladoHeladeria = optionalHeladeria.get();
            heladoHeladeria.setUnidades(heladoHeladeria.getUnidades() + unidadesMover);
            heladoRepository.save(heladoHeladeria);
        } else {
            heladoRepository.save(new HeladoDocument(nombre, heladoAlmacen.getSabor(), heladoAlmacen.getTipo(), heladoAlmacen.getPrecio(), unidadesMover, "heladeria"));
        }

        return true;
    }

    public HeladoDocument obtenerHeladoPorNombreYUbicacion(String nombre, String ubicacion) {
        return heladoRepository.findByUbicacion(ubicacion)
                .stream().filter(h -> h.getNombre().equals(nombre)).findFirst().orElse(null);
    }

    /*public boolean editarHelado(String ubicacion, String nombreOriginal, String nombreNuevo, String sabor, String tipo, int unidades, double precio) {
        Optional<HeladoDocument> optionalHelado = heladoRepository.findByUbicacion(ubicacion)
                .stream().filter(h -> h.getNombre().equals(nombreOriginal)).findFirst();

        if (optionalHelado.isPresent()) {
            HeladoDocument helado = optionalHelado.get();
            helado.setNombre(nombreNuevo);
            helado.setSabor(sabor);
            helado.setTipo(tipo);
            helado.setUnidades(unidades);
            helado.setPrecio(precio);
            heladoRepository.save(helado);
            return true;
        }
        return false;
    }*/

    public boolean editarHelado(String ubicacion, String nombreOriginal, String nombreNuevo, String sabor, String tipo, int unidades, double precio) {
        // Buscar el helado en la ubicación específica
        Optional<HeladoDocument> optionalHelado = heladoRepository.findByUbicacion(ubicacion)
                .stream().filter(h -> h.getNombre().equals(nombreOriginal)).findFirst();
    
        // Si existe el helado en la ubicación específica, actualizar unidades y precio
        if (optionalHelado.isPresent()) {
            HeladoDocument helado = optionalHelado.get();
            helado.setUnidades(unidades);
            helado.setPrecio(precio);
            heladoRepository.save(helado);
    
            // Actualizar nombre, sabor y tipo en todas las ubicaciones
            actualizarEnTodasUbicaciones(nombreOriginal, nombreNuevo, sabor, tipo);
    
            return true;
        }
        return false;
    }
    
    // Método para actualizar el nombre, sabor y tipo en todas las ubicaciones
    private void actualizarEnTodasUbicaciones(String nombreOriginal, String nombreNuevo, String sabor, String tipo) {
        List<HeladoDocument> helados = heladoRepository.findByNombre(nombreOriginal);
    
        if (!helados.isEmpty()) {
            for (HeladoDocument helado : helados) {
                helado.setNombre(nombreNuevo);
                helado.setSabor(sabor);
                helado.setTipo(tipo);
            }
            heladoRepository.saveAll(helados);
        }
    }
    

    public boolean eliminarHelado(String ubicacion, String nombre) {
        Optional<HeladoDocument> optionalHelado = heladoRepository.findByUbicacion(ubicacion)
                .stream().filter(h -> h.getNombre().equals(nombre)).findFirst();

        if (optionalHelado.isPresent()) {
            heladoRepository.delete(optionalHelado.get());
            return true;
        }
        return false;
    }

    public boolean venderHelados(String nombre, int unidadesVender, String metodoPago, double totalAPagar) {
        Optional<HeladoDocument> optionalHelado = heladoRepository.findByUbicacion("heladeria")
                .stream().filter(h -> h.getNombre().equals(nombre)).findFirst();

        if (optionalHelado.isEmpty() || optionalHelado.get().getUnidades() < unidadesVender) {
            return false;
        }

        HeladoDocument heladoH = optionalHelado.get();
        heladoH.setUnidades(heladoH.getUnidades() - unidadesVender);
        heladoRepository.save(heladoH);

        heladoRepository.save(new HeladoDocument(nombre, heladoH.getSabor(), heladoH.getTipo(), totalAPagar, unidadesVender, "vendido"));

        if (metodoPago.equals("efectivo")) {
            heladoRepository.save(new HeladoDocument(nombre, heladoH.getSabor(), heladoH.getTipo(), totalAPagar, unidadesVender, "vendido_efectivo"));
        } else if (metodoPago.equals("tarjeta")) {
            heladoRepository.save(new HeladoDocument(nombre, heladoH.getSabor(), heladoH.getTipo(), totalAPagar, unidadesVender, "vendido_tarjeta"));
        }

        return true;
    }

    public List<HeladoDocument> getListaAlmacen() {
        return heladoRepository.findByUbicacion("almacen");
    }

    public List<HeladoDocument> getListaHeladeria() {
        return heladoRepository.findByUbicacion("heladeria");
    }

    public List<HeladoDocument> getListaHeladosVendidos() {
        return heladoRepository.findByUbicacion("vendido");
    }

    public List<HeladoDocument> getListaVendidoEfectivo() {
        return heladoRepository.findByUbicacion("vendido_efectivo");
    }

    public List<HeladoDocument> getListaVendidoTarjeta() {
        return heladoRepository.findByUbicacion("vendido_tarjeta");
    }
}
