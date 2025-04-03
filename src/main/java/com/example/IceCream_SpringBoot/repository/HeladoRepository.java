package com.example.IceCream_SpringBoot.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.IceCream_SpringBoot.model.HeladoDocument;

public interface HeladoRepository extends MongoRepository<HeladoDocument, String> {
    List<HeladoDocument> findByUbicacion(String ubicacion);
    List<HeladoDocument> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
