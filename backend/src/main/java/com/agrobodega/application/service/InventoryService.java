package com.agrobodega.application.service;

import com.agrobodega.infrastructure.exception.BusinessException;

import com.agrobodega.application.dto.request.EntryItemRequestDTO;
import com.agrobodega.application.dto.request.EntryRequestDTO;
import com.agrobodega.application.dto.request.ExitItemRequestDTO;
import com.agrobodega.application.dto.request.ExitRequestDTO;
import com.agrobodega.application.dto.response.EntryReceiptResponseDTO;
import com.agrobodega.application.dto.response.ExitReceiptResponseDTO;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InventoryService {

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
    private final CatalogService catalogService;
    private final InventoryStreamController inventoryStreamController;
    private final TimeService timeService;

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
            CatalogService catalogService,
            InventoryStreamController inventoryStreamController,
            TimeService timeService) {

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
        this.catalogService = catalogService;
        this.inventoryStreamController = inventoryStreamController;
        this.timeService = timeService;
    }

    /**
     * Registra UNA entrada con todas sus lineas: un cosechero, un lote, una fecha y
     * varios productos. Cada linea suma bultos a su lote; se envia un solo correo.
     */
    @Transactional
    public EntryReceiptResponseDTO registerEntry(EntryRequestDTO request) {
        Farmer farmer = farmerRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new BusinessException("FARMER_NOT_FOUND"));
        String farmerName = farmer.getName() + " " + farmer.getLastName();

        Instant occurredAt = timeService.resolve(request.getOccurredAt(), request.getEntryDate());
        LocalDate entryDate = timeService.toLocalDate(occurredAt);

        BigDecimal commissionRate = catalogService.getDecimal("ENTRY_COMMISSION_RATE");
        UUID groupId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        List<EntryReceiptResponseDTO.Item> items = new ArrayList<>();
        List<String> itemLines = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (EntryItemRequestDTO item : request.getItems()) {
            ProductBase productBase = productBaseRepository.findById(item.getProductBaseId())
                    .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND"));
            ProductClassification classification = classificationRepository.findById(item.getClassificationId())
                    .orElseThrow(() -> new BusinessException("CLASSIFICATION_NOT_FOUND"));
            if (!classification.getProductBase().getId().equals(productBase.getId())) {
                throw new BusinessException("CLASSIFICATION_MISMATCH");
            }

            BigDecimal linePrice = item.getQuantity()
                    .multiply(item.getPricePerUnit())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineCommission = linePrice.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

            EntryMovement movement = new EntryMovement();
            movement.setEntryGroupId(groupId);
            movement.setCreatedAt(now);
            movement.setFarmer(farmer);
            movement.setClassification(classification);
            movement.setLotNumber(request.getLotNumber());
            movement.setQuantity(item.getQuantity());
            movement.setPricePerUnit(item.getPricePerUnit());
            movement.setTotalPrice(linePrice);
            movement.setEntryDate(entryDate);
            movement.setOccurredAt(occurredAt);
            movement = entryMovementRepository.save(movement);

            EntryCharge charge = new EntryCharge();
            charge.setEntryMovement(movement);
            charge.setAmount(lineCommission);
            charge.setChargeType(ChargeType.ENTRY_COMMISSION);
            entryChargeRepository.save(charge);

            // Regla de negocio: toda entrada suma existencias (bultos) a su lote, o lo crea.
            Lot lot = lotRepository
                    .findByProductBaseIdAndClassificationIdAndFarmerIdAndLotNumber(
                            productBase.getId(), classification.getId(), farmer.getId(), request.getLotNumber())
                    .orElseGet(() -> {
                        Lot nuevo = new Lot();
                        nuevo.setProductBase(productBase);
                        nuevo.setClassification(classification);
                        nuevo.setFarmer(farmer);
                        nuevo.setLotNumber(request.getLotNumber());
                        nuevo.setQuantity(BigDecimal.ZERO);
                        return nuevo;
                    });
            lot.setQuantity(lot.getQuantity().add(item.getQuantity()));
            lot.setEntryDate(entryDate);
            lotRepository.save(lot);

            items.add(toItem(movement, productBase.getName(), classification.getName(), lineCommission));
            itemLines.add(String.format("• %s · %s: %s bultos × $%s = $%s",
                    productBase.getName(), classification.getName(),
                    item.getQuantity().setScale(0, RoundingMode.HALF_UP).toPlainString(),
                    item.getPricePerUnit().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    linePrice.toPlainString()));

            totalQuantity = totalQuantity.add(item.getQuantity());
            totalPrice = totalPrice.add(linePrice);
            totalCommission = totalCommission.add(lineCommission);
        }

        EntryReceiptResponseDTO response = new EntryReceiptResponseDTO();
        response.setId(groupId);
        response.setFarmerName(farmerName);
        response.setLotNumber(request.getLotNumber());
        response.setEntryDate(entryDate);
        response.setOccurredAt(occurredAt);
        response.setItems(items);
        response.setTotalQuantity(totalQuantity);
        response.setTotalPrice(totalPrice);
        response.setTotalCommission(totalCommission);

        notificationService.sendEntryNotification(
                farmerName, request.getLotNumber(), timeService.format(occurredAt),
                itemLines, totalQuantity.doubleValue(), totalPrice.doubleValue());

        inventoryStreamController.broadcastInventoryUpdate(
                "Nueva entrada | Cosechero: " + farmerName +
                        " | Productos: " + items.size() +
                        " | Bultos: " + totalQuantity.toPlainString() +
                        " | Total: $" + totalPrice);

        return response;
    }

    /** Lista las entradas agrupadas (mas recientes primero). Los registros antiguos sin grupo salen sueltos. */
    @Transactional(readOnly = true)
    public List<EntryReceiptResponseDTO> getEntries() {
        List<EntryMovement> all = new ArrayList<>(entryMovementRepository.findAll());
        all.sort(Comparator
                .comparing((EntryMovement m) -> timeService.instantOf(m.getOccurredAt(), m.getEntryDate()))
                .reversed());

        Map<UUID, List<EntryMovement>> groups = new LinkedHashMap<>();
        for (EntryMovement m : all) {
            UUID key = m.getEntryGroupId() != null ? m.getEntryGroupId() : m.getId();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }

        List<EntryReceiptResponseDTO> result = new ArrayList<>();
        groups.forEach((groupId, lines) -> {
            EntryMovement first = lines.get(0);
            EntryReceiptResponseDTO dto = new EntryReceiptResponseDTO();
            dto.setId(groupId);
            dto.setFarmerName(first.getFarmer().getName() + " " + first.getFarmer().getLastName());
            dto.setLotNumber(first.getLotNumber());
            dto.setEntryDate(first.getEntryDate());
            dto.setOccurredAt(first.getOccurredAt());

            List<EntryReceiptResponseDTO.Item> items = new ArrayList<>();
            BigDecimal quantity = BigDecimal.ZERO;
            BigDecimal price = BigDecimal.ZERO;
            for (EntryMovement m : lines) {
                items.add(toItem(m,
                        m.getClassification().getProductBase().getName(),
                        m.getClassification().getName(), null));
                quantity = quantity.add(m.getQuantity());
                price = price.add(m.getTotalPrice());
            }
            dto.setItems(items);
            dto.setTotalQuantity(quantity);
            dto.setTotalPrice(price);
            result.add(dto);
        });
        return result;
    }

    private EntryReceiptResponseDTO.Item toItem(EntryMovement m, String product, String classification,
                                                BigDecimal commission) {
        EntryReceiptResponseDTO.Item item = new EntryReceiptResponseDTO.Item();
        item.setMovementId(m.getId());
        item.setProductName(product);
        item.setClassificationName(classification);
        item.setQuantity(m.getQuantity());
        item.setPricePerUnit(m.getPricePerUnit());
        item.setTotalPrice(m.getTotalPrice());
        item.setCommissionAmount(commission);
        return item;
    }

    /**
     * Registra UNA salida con todas sus lineas: un negociante, una fecha y varios lotes.
     * Cada linea descuenta bultos de su lote (solo si hay existencias); se envia un solo correo.
     */
    @Transactional
    public ExitReceiptResponseDTO registerExit(ExitRequestDTO request) {
        Trader trader = traderRepository.findById(request.getTraderId())
                .orElseThrow(() -> new BusinessException("TRADER_NOT_FOUND"));
        String traderName = trader.getName() + " " + trader.getLastName();

        Instant occurredAt = timeService.resolve(request.getOccurredAt(), request.getExitDate());
        LocalDate exitDate = timeService.toLocalDate(occurredAt);

        BigDecimal commissionRate = catalogService.getDecimal("EXIT_COMMISSION_RATE");
        UUID groupId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        List<ExitReceiptResponseDTO.Item> items = new ArrayList<>();
        List<String> itemLines = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (ExitItemRequestDTO item : request.getItems()) {
            Lot lot = lotRepository.findByIdForUpdate(item.getLotId())
                    .orElseThrow(() -> new BusinessException("LOT_NOT_FOUND"));

            // Regla de negocio: solo puede salir lo que hay disponible en bodega.
            if (lot.getQuantity().compareTo(item.getQuantity()) < 0) {
                throw new BusinessException("INSUFFICIENT_STOCK");
            }

            BigDecimal linePrice = item.getQuantity()
                    .multiply(item.getPricePerUnit())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineCommission = linePrice.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

            ExitMovement movement = new ExitMovement();
            movement.setExitGroupId(groupId);
            movement.setCreatedAt(now);
            movement.setLot(lot);
            movement.setTrader(trader);
            movement.setQuantity(item.getQuantity());
            movement.setPricePerUnit(item.getPricePerUnit());
            movement.setTotalPrice(linePrice);
            movement.setExitDate(exitDate);
            movement.setOccurredAt(occurredAt);
            movement = exitMovementRepository.save(movement);

            lot.setQuantity(lot.getQuantity().subtract(item.getQuantity()));
            lotRepository.save(lot);

            ExitCharge charge = new ExitCharge();
            charge.setExitMovement(movement);
            charge.setAmount(lineCommission);
            charge.setChargeType(ChargeType.EXIT_COMMISSION);
            exitChargeRepository.save(charge);

            items.add(toExitItem(movement, lineCommission));
            itemLines.add(String.format("• %s · %s (%s, Lote %02d): %s bultos × $%s = $%s — quedan %s en el lote",
                    lot.getProductBase().getName(), lot.getClassification().getName(),
                    farmerName(lot), lot.getLotNumber() == null ? 0 : lot.getLotNumber(),
                    item.getQuantity().setScale(0, RoundingMode.HALF_UP).toPlainString(),
                    item.getPricePerUnit().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    linePrice.toPlainString(),
                    lot.getQuantity().setScale(0, RoundingMode.HALF_UP).toPlainString()));

            totalQuantity = totalQuantity.add(item.getQuantity());
            totalPrice = totalPrice.add(linePrice);
            totalCommission = totalCommission.add(lineCommission);
        }

        ExitReceiptResponseDTO response = new ExitReceiptResponseDTO();
        response.setId(groupId);
        response.setTraderName(traderName);
        response.setExitDate(exitDate);
        response.setOccurredAt(occurredAt);
        response.setItems(items);
        response.setTotalQuantity(totalQuantity);
        response.setTotalPrice(totalPrice);
        response.setTotalCommission(totalCommission);

        notificationService.sendExitNotification(
                traderName, timeService.format(occurredAt), itemLines,
                totalQuantity.doubleValue(), totalPrice.doubleValue());

        inventoryStreamController.broadcastInventoryUpdate(
                "Nueva salida | Negociante: " + traderName +
                        " | Productos: " + items.size() +
                        " | Bultos: " + totalQuantity.toPlainString() +
                        " | Total: $" + totalPrice);

        return response;
    }

    /** Lista las salidas agrupadas (mas recientes primero). */
    @Transactional(readOnly = true)
    public List<ExitReceiptResponseDTO> getExits() {
        List<ExitMovement> all = new ArrayList<>(exitMovementRepository.findAll());
        all.sort(Comparator
                .comparing((ExitMovement m) -> timeService.instantOf(m.getOccurredAt(), m.getExitDate()))
                .reversed());

        Map<UUID, List<ExitMovement>> groups = new LinkedHashMap<>();
        for (ExitMovement m : all) {
            UUID key = m.getExitGroupId() != null ? m.getExitGroupId() : m.getId();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }

        List<ExitReceiptResponseDTO> result = new ArrayList<>();
        groups.forEach((groupId, lines) -> {
            ExitMovement first = lines.get(0);
            ExitReceiptResponseDTO dto = new ExitReceiptResponseDTO();
            dto.setId(groupId);
            dto.setTraderName(first.getTrader().getName() + " " + first.getTrader().getLastName());
            dto.setExitDate(first.getExitDate());
            dto.setOccurredAt(first.getOccurredAt());

            List<ExitReceiptResponseDTO.Item> items = new ArrayList<>();
            BigDecimal quantity = BigDecimal.ZERO;
            BigDecimal price = BigDecimal.ZERO;
            for (ExitMovement m : lines) {
                items.add(toExitItem(m, null));
                quantity = quantity.add(m.getQuantity());
                price = price.add(m.getTotalPrice());
            }
            dto.setItems(items);
            dto.setTotalQuantity(quantity);
            dto.setTotalPrice(price);
            result.add(dto);
        });
        return result;
    }

    private ExitReceiptResponseDTO.Item toExitItem(ExitMovement m, BigDecimal commission) {
        Lot lot = m.getLot();
        ExitReceiptResponseDTO.Item item = new ExitReceiptResponseDTO.Item();
        item.setMovementId(m.getId());
        item.setLotId(lot.getId());
        item.setLotNumber(lot.getLotNumber());
        item.setProductName(lot.getProductBase().getName());
        item.setClassificationName(lot.getClassification().getName());
        item.setFarmerName(farmerName(lot));
        item.setQuantity(m.getQuantity());
        item.setPricePerUnit(m.getPricePerUnit());
        item.setTotalPrice(m.getTotalPrice());
        item.setCommissionAmount(commission);
        return item;
    }

    private String farmerName(Lot lot) {
        return lot.getFarmer() == null ? "Sin cosechero"
                : lot.getFarmer().getName() + " " + lot.getFarmer().getLastName();
    }
}
