package com.example.IceCream_SpringBoot.service;

import com.example.IceCream_SpringBoot.model.Helado;
import com.example.IceCream_SpringBoot.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HeladoService {

    private ArrayList<Helado> listaAlmacen;
    private ArrayList<Helado> listaHeladeria;
    private ArrayList<Helado> listaHeladosVendidos;
    private ArrayList<Helado> listaVendidoEfectivo;
    private ArrayList<Helado> listaVendidoTarjeta;

    public HeladoService() {
        this.listaAlmacen = new ArrayList<>();
        this.listaHeladeria = new ArrayList<>();
        this.listaHeladosVendidos = new ArrayList<>();
        this.listaVendidoEfectivo = new ArrayList<>();
        this.listaVendidoTarjeta = new ArrayList<>();
    }

    public boolean heladoExiste(String nombre) {
        return listaAlmacen.stream().anyMatch(h -> h.getNombre().equals(nombre));
    }

    public boolean agregarAlAlmacen(String nombre, String sabor, double precio, int unidades) {
        if (heladoExiste(nombre)) {
            return false;
        }

        Helado helado = new Helado(nombre, sabor, precio, unidades);
        listaAlmacen.add(helado);
        return true;
    }

    public boolean moverHeladoAHeladeria(String nombre, int unidadesMover) {
        Helado heladoAlmacen = listaAlmacen.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        if (heladoAlmacen == null || heladoAlmacen.getUnidades() < unidadesMover) {
            return false; // No hay suficientes unidades o el helado no existe
        }

        heladoAlmacen.setUnidades(heladoAlmacen.getUnidades() - unidadesMover);

        Helado heladoHeladeria = listaHeladeria.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        if (heladoHeladeria != null) {
            heladoHeladeria.setUnidades(heladoHeladeria.getUnidades() + unidadesMover);
        } else {
            listaHeladeria.add(new Helado(nombre, heladoAlmacen.getSabor(), heladoAlmacen.getPrecio(), unidadesMover));
        }

        return true;
    }

    public boolean venderHelados(String nombre, int unidadesVender, String metodoPago){
        Helado heladoAlmacen = listaHeladeria.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        if (heladoAlmacen == null || heladoAlmacen.getUnidades() < unidadesVender) {
            return false; // No hay suficientes unidades o el helado no existe
        }

        heladoAlmacen.setUnidades(heladoAlmacen.getUnidades() - unidadesVender);

        Helado heladoHeladeria = listaHeladosVendidos.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        Helado heladoEfectivo = listaVendidoEfectivo.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        Helado heladoTarjeta = listaVendidoTarjeta.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        if (heladoHeladeria != null) {
            heladoHeladeria.setUnidades(heladoHeladeria.getUnidades() + unidadesVender);

            if (metodoPago.equals("efectivo") && heladoEfectivo != null) {
                heladoEfectivo.setUnidades(heladoEfectivo.getUnidades() + unidadesVender);
            } else if (metodoPago.equals("tarjeta") && heladoTarjeta != null) {
                heladoTarjeta.setUnidades(heladoEfectivo.getUnidades() + unidadesVender);
            }
        } else {
            listaHeladosVendidos.add(new Helado(nombre, heladoAlmacen.getSabor(), heladoAlmacen.getPrecio(), unidadesVender));

            if (metodoPago.equals("efectivo")) {
                listaVendidoEfectivo.add(new Helado(nombre, heladoAlmacen.getSabor(), heladoAlmacen.getPrecio(), unidadesVender));
            } else if (metodoPago.equals("tarjeta")) {
                listaVendidoTarjeta.add(new Helado(nombre, heladoAlmacen.getSabor(), heladoAlmacen.getPrecio(), unidadesVender));
            }
        }
        return true;
    }

    public boolean editarHelado(String ubicacion, String nombreOriginal, String nombreNuevo, String sabor, int unidades, double precio) {
        List<Helado> lista = ubicacion.equals("almacen") ? listaAlmacen : listaHeladeria;

        Helado helado = lista.stream()
                .filter(h -> h.getNombre().equals(nombreOriginal))
                .findFirst()
                .orElse(null);

        if (helado != null) {
            helado.setNombre(nombreNuevo);
            helado.setSabor(sabor);
            helado.setUnidades(unidades);
            helado.setPrecio(precio);
            return true;
        }

        return false;
    }

    public boolean eliminarHelado(String ubicacion, String nombre) {
        List<Helado> lista = ubicacion.equals("almacen") ? listaAlmacen : listaHeladeria;

        Helado helado = lista.stream()
                .filter(h -> h.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);

        if (helado != null) {
            lista.remove(helado);
            return true;
        }
        return false;
    }

    public ArrayList<Helado> getListaAlmacen() {
        return listaAlmacen;
    }

    public ArrayList<Helado> getListaHeladeria() {
        return listaHeladeria;
    }

    public ArrayList<Helado> getListaHeladosVendidos() {
        return listaHeladosVendidos;
    }

    public ArrayList<Helado> getListaVendidoEfectivo() {
        return listaVendidoEfectivo;
    }

    public ArrayList<Helado> getListaVendidoTarjeta() {
        return listaVendidoTarjeta;
    }
}
