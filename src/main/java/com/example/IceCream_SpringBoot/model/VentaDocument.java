package com.example.IceCream_SpringBoot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ventas")
public class VentaDocument {

    @Id
    private String id;
    private HeladoDocument helado;
    private String vendedor;
    private Cliente cliente;
    private String metodoPago;
    private double total;
    private LocalDateTime fechaVenta;

    public VentaDocument() {}

    public VentaDocument(HeladoDocument helado, String vendedor, Cliente cliente, String metodoPago, double total, LocalDateTime fechaVenta) {
        this.helado = helado;
        this.vendedor = vendedor;
        this.cliente = cliente;
        this.metodoPago = metodoPago;
        this.total = total;
        this.fechaVenta = fechaVenta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HeladoDocument getHelado() {
        return helado;
    }

    public void setHelado(HeladoDocument helado) {
        this.helado = helado;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }
}
