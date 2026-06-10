package com.agrobodega.application.service;

import com.agrobodega.application.dto.request.EntryMovementRequestDTO;
import com.agrobodega.application.dto.request.ExitMovementRequestDTO;
import com.agrobodega.application.dto.response.EntryMovementResponseDTO;
import com.agrobodega.application.dto.response.ExitMovementResponseDTO;
import com.agrobodega.domain.entity.EntryCharge;
import com.agrobodega.domain.entity.EntryMovement;
import com.agrobodega.domain.entity.ExitCharge;
import com.agrobodega.domain.entity.ExitMovement;
import com.agrobodega.domain.entity.Farmer;
import com.agrobodega.domain.entity.Lot;
import com.agrobodega.domain.entity.ProductBase;
import com.agrobodega.domain.entity.ProductClassification;
import com.agrobodega.domain.entity.Trader;
import com.agrobodega.domain.enums.ChargeType;
import com.agrobodega.infrastructure.controller.InventoryStreamController;
import com.agrobodega.infrastructure.repository.EntryChargeRepository;
import com.agrobodega.infrastructure.repository.EntryMovementRepository;
import com.agrobodega.infrastructure.repository.ExitChargeRepository;
import com.agrobodega.infrastructure.repository.ExitMovementRepository;
import com.agrobodega.infrastructure.repository.FarmerRepository;
import com.agrobodega.infrastructure.repository.LotRepository;
import com.agrobodega.infrastructure.repository.ProductBaseRepository;
import com.agrobodega.infrastructure.repository.ProductClassificationRepository;
import com.agrobodega.infrastructure.repository.TraderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InventoryService {

    private static final BigDecimal ENTRY_COMMISSION_RATE = new BigDecimal("0.05");
    private static final BigDecimal EXIT_COMMISSION_RATE = new BigDecimal("0.03");

    private final ProductBaseRepository productBaseRepository;
    private final ProductClassificationRepository classificationRepository;
    private final FarmerRepository farmerRepository;
    private final TraderRepository traderRepository;
    private final LotRepository lotRepository;
    private final EntryMovementRepository entryMovementRepository;
    private final ExitMovementRepository exitMovementRepository;
    private final EntryChargeRepository entryChargeRepository;
    private final ExitChargeRepository exitChargeRepository;
    private final NotificationService notificationService;
    private final InventoryStreamController inventoryStreamController;

    public InventoryService(
            ProductBaseRepository productBaseRepository,
            ProductClassificationRepository classificationRepository,
            FarmerRepository farmerRepository,
            TraderRepository traderRepository,
            LotRepository lotRepository,
            EntryMovementRepository entryMovementRepository,
            ExitMovementRepository exitMovementRepository,
            EntryChargeRepository entryChargeRepository,
            ExitChargeRepository exitChargeRepository,
            NotificationService notificationService,
            InventoryStreamController inventoryStreamController) {

        this.productBaseRepository = productBaseRepository;
        this.classificationRepository = classificationRepository;
        this.farmerRepository = farmerRepository;
        this.traderRepository = traderRepository;
        this.lotRepository = lotRepository;
        this.entryMovementRepository = entryMovementRepository;
        this.exitMovementRepository = exitMovementRepository;
        this.entryChargeRepository = entryChargeRepository;
        this.exitChargeRepository = exitChargeRepository;
        this.notificationService = notificationService;
        this.inventoryStreamController = inventoryStreamController;
    }

    @Transactional
    public EntryMovementResponseDTO registerEntry(EntryMovementRequestDTO request) {
        ProductBase productBase = productBaseRepository.findById(request.getProductBaseId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductClassification classification = classificationRepository.findById(request.getClassificationId())
                .orElseThrow(() -> new RuntimeException("Clasificación no encontrada"));

        Farmer farmer = farmerRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new RuntimeException("Cosechero no encontrado"));

        BigDecimal totalPrice = request.getQuantity()
                .multiply(request.getPricePerUnit())
                .setScale(2, RoundingMode.HALF_UP);

        EntryMovement entryMovement = new EntryMovement();
        entryMovement.setFarmer(farmer);
        entryMovement.setClassification(classification);
        entryMovement.setLotNumber(request.getLotNumber());
        entryMovement.setQuantity(request.getQuantity());
        entryMovement.setPricePerUnit(request.getPricePerUnit());
        entryMovement.setTotalPrice(totalPrice);
        entryMovement.setEntryDate(request.getEntryDate());
        entryMovement = entryMovementRepository.save(entryMovement);

        BigDecimal commissionAmount = totalPrice
                .multiply(ENTRY_COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        EntryCharge entryCharge = new EntryCharge();
        entryCharge.setEntryMovement(entryMovement);
        entryCharge.setAmount(commissionAmount);
        entryCharge.setChargeType(ChargeType.ENTRY_COMMISSION);
        entryChargeRepository.save(entryCharge);

        EntryMovementResponseDTO response = new EntryMovementResponseDTO();
        response.setEntryMovementId(entryMovement.getId());
        response.setProductName(productBase.getName());
        response.setClassificationName(classification.getName());
        response.setFarmerName(farmer.getName() + " " + farmer.getLastName());
        response.setLotNumber(request.getLotNumber());
        response.setQuantity(request.getQuantity());
        response.setPricePerUnit(request.getPricePerUnit());
        response.setTotalPrice(totalPrice);
        response.setCommissionAmount(commissionAmount);
        response.setEntryDate(request.getEntryDate());

        notificationService.sendEntryNotification(
                farmer.getName() + " " + farmer.getLastName(),
                productBase.getName(),
                classification.getName(),
                request.getLotNumber(),
                request.getQuantity().doubleValue(),
                totalPrice.doubleValue()
        );

        inventoryStreamController.broadcastInventoryUpdate(
                "Nueva entrada: " + productBase.getName() +
                        " | Cosechero: " + farmer.getName() +
                        " | Cantidad: " + request.getQuantity() +
                        " | Total: $" + totalPrice
        );

        return response;
    }

    @Transactional
    public ExitMovementResponseDTO registerExit(ExitMovementRequestDTO request) {
        Lot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        if (lot.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Stock insuficiente.");
        }

        Trader trader = traderRepository.findById(request.getTraderId())
                .orElseThrow(() -> new RuntimeException("Negociante no encontrado"));

        BigDecimal totalPrice = request.getQuantity()
                .multiply(request.getPricePerUnit())
                .setScale(2, RoundingMode.HALF_UP);

        ExitMovement exitMovement = new ExitMovement();
        exitMovement.setLot(lot);
        exitMovement.setTrader(trader);
        exitMovement.setQuantity(request.getQuantity());
        exitMovement.setPricePerUnit(request.getPricePerUnit());
        exitMovement.setTotalPrice(totalPrice);
        exitMovement.setExitDate(request.getExitDate());
        exitMovement = exitMovementRepository.save(exitMovement);

        lot.setQuantity(lot.getQuantity().subtract(request.getQuantity()));
        lotRepository.save(lot);

        BigDecimal commissionAmount = totalPrice
                .multiply(EXIT_COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        ExitCharge exitCharge = new ExitCharge();
        exitCharge.setExitMovement(exitMovement);
        exitCharge.setAmount(commissionAmount);
        exitCharge.setChargeType(ChargeType.EXIT_COMMISSION);
        exitChargeRepository.save(exitCharge);

        ExitMovementResponseDTO response = new ExitMovementResponseDTO();
        response.setExitMovementId(exitMovement.getId());
        response.setLotId(lot.getId());
        response.setProductName(lot.getProductBase().getName());
        response.setClassificationName(lot.getClassification().getName());
        response.setTraderName(trader.getName() + " " + trader.getLastName());
        response.setQuantity(request.getQuantity());
        response.setPricePerUnit(request.getPricePerUnit());
        response.setTotalPrice(totalPrice);
        response.setCommissionAmount(commissionAmount);
        response.setRemainingLotQuantity(lot.getQuantity());
        response.setExitDate(request.getExitDate());

        inventoryStreamController.broadcastInventoryUpdate(
                "Nueva salida: " + lot.getProductBase().getName() +
                        " | Negociante: " + trader.getName() +
                        " | Cantidad: " + request.getQuantity() +
                        " | Total: $" + totalPrice
        );

        return response;
    }
}