package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.repository;

import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.CartonDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataMongoCartonRepository extends MongoRepository<CartonDocument, String> {

    List<CartonDocument> findByStatus(String status);
}