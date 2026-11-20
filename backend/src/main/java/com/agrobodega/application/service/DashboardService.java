package com.agrobodega.application.service;

import com.agrobodega.application.dto.response.DashboardResponse;
import com.agrobodega.application.dto.response.EntryReceiptResponseDTO;
import com.agrobodega.application.dto.response.ExitReceiptResponseDTO;
import com.agrobodega.domain.entity.Lot;
import com.agrobodega.domain.entity.ProductBase;
import com.agrobodega.infrastructure.repository.FarmerRepository;
import com.agrobodega.infrastructure.repository.LotRepository;
import com.agrobodega.infrastructure.repository.ProductBaseRepository;
import com.agrobodega.infrastructure.repository.TraderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Resumen operativo: inventario, movimientos del dia, ultimos 7 dias y actividad reciente. */
@Service
public class DashboardService {

    private static final int DEFAULT_LOW_STOCK = 20;
    private static final int RECENT_LIMIT = 8;

    private final LotRepository lotRepository;
    private final FarmerRepository farmerRepository;
    private final TraderRepository traderRepository;
    private final ProductBaseRepository productBaseRepository;
    private final InventoryService inventoryService;
    private final CatalogService catalogService;
    private final TimeService timeService;

    public DashboardService(LotRepository lotRepository,
                            FarmerRepository farmerRepository,
                            TraderRepository traderRepository,
                            ProductBaseRepository productBaseRepository,
                            InventoryService inventoryService,
                            CatalogService catalogService,
                            TimeService timeService) {
        this.lotRepository = lotRepository;
        this.farmerRepository = farmerRepository;
        this.traderRepository = traderRepository;
        this.productBaseRepository = productBaseRepository;
        this.inventoryService = inventoryService;
        this.catalogService = catalogService;
        this.timeService = timeService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse build() {
        LocalDate today = timeService.today();
        int threshold = lowStockThreshold();

        // --- Inventario actual (bultos por producto) ---
        Map<String, Long> stockMap = new LinkedHashMap<>();
        for (ProductBase product : productBaseRepository.findAll()) {
            stockMap.put(product.getName(), 0L);
        }
        long stockBultos = 0;
        long lotsWithStock = 0;
        for (Lot lot : lotRepository.findAll()) {
            long quantity = lot.getQuantity().longValue();
            if (quantity > 0) {
                stockBultos += quantity;
                lotsWithStock++;
                stockMap.merge(lot.getProductBase().getName(), quantity, Long::sum);
            }
        }

        List<DashboardResponse.StockItem> stockByProduct = new ArrayList<>();
        for (Map.Entry<String, Long> item : stockMap.entrySet()) {
            stockByProduct.add(new DashboardResponse.StockItem(
                    item.getKey(), item.getValue(), item.getValue() <= threshold));
        }
        stockByProduct.sort(Comparator.comparingLong(DashboardResponse.StockItem::bultos).reversed());

        List<DashboardResponse.StockItem> lowStock = stockByProduct.stream()
                .filter(DashboardResponse.StockItem::low)
                .toList();

        // --- Movimientos (ya agrupados por entrada/salida) ---
        List<EntryReceiptResponseDTO> entries = inventoryService.getEntries();
        List<ExitReceiptResponseDTO> exits = inventoryService.getExits();

        long entriesToday = 0;
        long entryBultosToday = 0;
        for (EntryReceiptResponseDTO e : entries) {
            if (today.equals(e.getEntryDate())) {
                entriesToday++;
                entryBultosToday += e.getTotalQuantity().longValue();
            }
        }
        long exitsToday = 0;
        long exitBultosToday = 0;
        for (ExitReceiptResponseDTO x : exits) {
            if (today.equals(x.getExitDate())) {
                exitsToday++;
                exitBultosToday += x.getTotalQuantity().longValue();
            }
        }

        // --- Ultimos 7 dias ---
        List<DashboardResponse.DayPoint> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long in = 0;
            for (EntryReceiptResponseDTO e : entries) {
                if (day.equals(e.getEntryDate())) {
                    in += e.getTotalQuantity().longValue();
                }
            }
            long out = 0;
            for (ExitReceiptResponseDTO x : exits) {
                if (day.equals(x.getExitDate())) {
                    out += x.getTotalQuantity().longValue();
                }
            }
            last7Days.add(new DashboardResponse.DayPoint(day, in, out));
        }

        // --- Actividad reciente (entradas y salidas mezcladas) ---
        List<DashboardResponse.Activity> activity = new ArrayList<>();
        for (EntryReceiptResponseDTO e : entries.stream().limit(RECENT_LIMIT).toList()) {
            String detail = e.getItems().stream()
                    .map(i -> i.getProductName() + " · " + i.getClassificationName())
                    .distinct()
                    .collect(Collectors.joining(", "));
            activity.add(new DashboardResponse.Activity("ENTRY", e.getOccurredAt(), e.getEntryDate(),
                    e.getFarmerName(), detail, e.getTotalQuantity().longValue()));
        }
        for (ExitReceiptResponseDTO x : exits.stream().limit(RECENT_LIMIT).toList()) {
            String detail = x.getItems().stream()
                    .map(i -> i.getProductName() + " · " + i.getClassificationName())
                    .distinct()
                    .collect(Collectors.joining(", "));
            activity.add(new DashboardResponse.Activity("EXIT", x.getOccurredAt(), x.getExitDate(),
                    x.getTraderName(), detail, x.getTotalQuantity().longValue()));
        }
        activity.sort(Comparator
                .comparing((DashboardResponse.Activity a) -> timeService.instantOf(a.occurredAt(), a.date()))
                .reversed());
        List<DashboardResponse.Activity> recent = activity.stream().limit(RECENT_LIMIT).toList();

        return new DashboardResponse(
                stockBultos,
                lotsWithStock,
                farmerRepository.count(),
                traderRepository.count(),
                productBaseRepository.count(),
                new DashboardResponse.DayTotals(entriesToday, entryBultosToday),
                new DashboardResponse.DayTotals(exitsToday, exitBultosToday),
                threshold,
                stockByProduct,
                lowStock,
                last7Days,
                recent);
    }

    private int lowStockThreshold() {
        try {
            return Integer.parseInt(catalogService.getParameter("LOW_STOCK_BULTOS").trim());
        } catch (Exception e) {
            return DEFAULT_LOW_STOCK;
        }
    }
}
