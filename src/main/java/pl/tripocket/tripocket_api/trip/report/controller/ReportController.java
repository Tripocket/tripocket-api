package pl.tripocket.tripocket_api.trip.report.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse;
import pl.tripocket.tripocket_api.trip.report.service.ReportService;

@RestController
@RequestMapping("/trips/{tripId}/reports")
public class ReportController {

  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @GetMapping("/categories")
  public ResponseEntity<CategoryReportResponse> getCategories(@PathVariable UUID tripId) {
    return ResponseEntity.ok(reportService.getCategoryReport(tripId));
  }

  @GetMapping("/settlements")
  public ResponseEntity<SettlementReportResponse> getSettlements(@PathVariable UUID tripId) {
    return ResponseEntity.ok(reportService.getSettlementReport(tripId));
  }
}
