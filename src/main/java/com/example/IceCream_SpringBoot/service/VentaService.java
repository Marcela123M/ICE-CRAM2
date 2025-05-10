package com.example.IceCream_SpringBoot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.IceCream_SpringBoot.model.Cliente;
import com.example.IceCream_SpringBoot.model.HeladoDocument;
import com.example.IceCream_SpringBoot.model.VentaDocument;
import com.example.IceCream_SpringBoot.repository.ClienteRepository;
import com.example.IceCream_SpringBoot.repository.HeladoRepository;
import com.example.IceCream_SpringBoot.repository.VentaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private HeladoRepository heladoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentaRepository ventaRepository;

    public boolean procesarVenta(String nombreHelado,
            int unidadesVender,
            String metodoPago,
            double totalEnviado, // lo recibimos pero validamos
            String nombreCliente,
            String cedula,
            LocalDateTime fechaNacimiento,
            String telefono,
            String vendedor) {

        Optional<HeladoDocument> optHelado = heladoRepository.findByNombreAndUbicacion(nombreHelado, "heladeria");
        if (optHelado.isEmpty() || optHelado.get().getUnidades() < unidadesVender) {
            return false;
        }

        HeladoDocument helado = optHelado.get();
        double precioUnitario = helado.getPrecio();
        double totalCalculado = precioUnitario * unidadesVender;

        // Si es con tarjeta, sumar 3% de recargo
        if (metodoPago.equalsIgnoreCase("tarjeta")) {
            totalCalculado += totalCalculado * 0.03;
        }

        // Validar contra el totalEnviado
        if (Math.abs(totalEnviado - totalCalculado) > 0.01) {
            return false;
        }

        // Restar inventario
        helado.setUnidades(helado.getUnidades() - unidadesVender);
        heladoRepository.save(helado);

        // Validar o crear cliente
        Cliente cliente;
        if (clienteRepository.existsByCedula(cedula)) {
            cliente = clienteRepository.findByCedula(cedula);
        } else {
            cliente = new Cliente(nombreCliente, cedula, fechaNacimiento.toLocalDate(), telefono);
            clienteRepository.save(cliente);
        }

        // Registrar venta
        VentaDocument venta = new VentaDocument(
                helado, vendedor, cliente, metodoPago, totalCalculado, LocalDateTime.now());
        ventaRepository.save(venta);

        return true;
    }
}
