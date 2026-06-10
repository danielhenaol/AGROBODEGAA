package com.agrobodega.infrastructure.config;

import com.agrobodega.domain.entity.Farmer;
import com.agrobodega.domain.entity.ProductBase;
import com.agrobodega.domain.entity.ProductClassification;
import com.agrobodega.domain.entity.Trader;
import com.agrobodega.infrastructure.repository.FarmerRepository;
import com.agrobodega.infrastructure.repository.ProductBaseRepository;
import com.agrobodega.infrastructure.repository.ProductClassificationRepository;
import com.agrobodega.infrastructure.repository.TraderRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final ProductBaseRepository productBaseRepository;
    private final ProductClassificationRepository classificationRepository;
    private final FarmerRepository farmerRepository;
    private final TraderRepository traderRepository;

    public DataInitializer(ProductBaseRepository productBaseRepository,
                           ProductClassificationRepository classificationRepository,
                           FarmerRepository farmerRepository,
                           TraderRepository traderRepository) {
        this.productBaseRepository = productBaseRepository;
        this.classificationRepository = classificationRepository;
        this.farmerRepository = farmerRepository;
        this.traderRepository = traderRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productBaseRepository.count() == 0) {
            ProductBase tomate = productBaseRepository.save(
                    new ProductBase(null, "Tomate", "Tomate fresco")
            );

            ProductBase papa = productBaseRepository.save(
                    new ProductBase(null, "Papa", "Papa criolla y pastusa")
            );

            ProductBase cebolla = productBaseRepository.save(
                    new ProductBase(null, "Cebolla", "Cebolla cabezona")
            );

            classificationRepository.save(
                    new ProductClassification(null, "Primera calidad", tomate)
            );

            classificationRepository.save(
                    new ProductClassification(null, "Segunda calidad", tomate)
            );

            classificationRepository.save(
                    new ProductClassification(null, "Primera calidad", papa)
            );

            classificationRepository.save(
                    new ProductClassification(null, "Segunda calidad", papa)
            );

            classificationRepository.save(
                    new ProductClassification(null, "Primera calidad", cebolla)
            );
        }

        if (farmerRepository.count() == 0) {
            farmerRepository.save(
                    new Farmer(null, "Carlos", "Martínez", "3001234567")
            );

            farmerRepository.save(
                    new Farmer(null, "Luis", "Gómez", "3009876543")
            );
        }

        if (traderRepository.count() == 0) {
            traderRepository.save(
                    new Trader(null, "Supermercado", "La 14", "3105551234")
            );

            traderRepository.save(
                    new Trader(null, "Almacenes", "Éxito", "3115559876")
            );
        }
    }
}