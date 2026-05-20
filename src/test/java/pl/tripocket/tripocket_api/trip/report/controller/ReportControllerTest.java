package pl.tripocket.tripocket_api.trip.report.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse.CategoryEntry;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse.Settlement;
import pl.tripocket.tripocket_api.trip.report.service.ReportService;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

  private static final UUID TRIP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_A  = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID USER_B  = UUID.fromString("00000000-0000-0000-0000-000000000020");

  @Mock ReportService reportService;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService)).build();
  }

  @Test
  void getCategories_returns200WithBody() throws Exception {
    when(reportService.getCategoryReport(TRIP_ID))
        .thenReturn(new CategoryReportResponse(
            TRIP_ID, "PLN",
            List.of(new CategoryEntry("food", new BigDecimal("320.00")))));

    mockMvc.perform(get("/trips/{tripId}/reports/categories", TRIP_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.baseCurrency").value("PLN"))
        .andExpect(jsonPath("$.categories[0].category").value("food"))
        .andExpect(jsonPath("$.categories[0].total").value(320.00));
  }

  @Test
  void getSettlements_returns200WithBody() throws Exception {
    when(reportService.getSettlementReport(TRIP_ID))
        .thenReturn(new SettlementReportResponse(
            TRIP_ID, "PLN",
            List.of(new Settlement(USER_A, "alice", USER_B, "bob", new BigDecimal("100.00")))));

    mockMvc.perform(get("/trips/{tripId}/reports/settlements", TRIP_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.baseCurrency").value("PLN"))
        .andExpect(jsonPath("$.settlements[0].debtorUsername").value("alice"))
        .andExpect(jsonPath("$.settlements[0].creditorUsername").value("bob"))
        .andExpect(jsonPath("$.settlements[0].amount").value(100.00));
  }
}
