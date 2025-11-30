package apap.ti._5.Insurance_2306275310_be.config;

import apap.ti._5.Insurance_2306275310_be.model.*;
import apap.ti._5.Insurance_2306275310_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class inisialisasi data (Seeder).
 * Berjalan otomatis saat aplikasi pertama kali di-start.
 * Mengisi database dengan data Plan dan Policy dummy status PAID agar siap tes Klaim.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final InsurancePlanRepository insurancePlanRepository;
    private final PolicyRepository policyRepository;
    private final OrderedPlanRepository orderedPlanRepository;

    // --- DATA DARI JSON RESPONSE ---
    
    // Providers
    private final String ID_PROVIDER_AXA = "55bfb420-80d1-49ae-9486-401e624bd4ab";
    private final String ID_PROVIDER_ASTRA = "7d2e5937-327e-4bab-83c0-25da0d3db78a";
    private final String ID_PROVIDER_AIA = "2240a504-d838-441b-bd03-6fc2d9b52781"; // RESTORED
    private final String ID_PROVIDER_TESTKHAY = "7c297cbd-8622-4848-9a1d-93ec7927e370";

    // Customers
    private final String ID_USER_KHAYLA = "f7dd4d28-3c2d-4fc5-ba78-47b540d8613e"; // khaycust
    private final String ID_USER_CUSTOMER_TEST = "cb2886f8-0ba4-4266-b966-d7f75b41acb4"; // customer_test
    private final String ID_USER_FAWBUD = "d56614a5-51c9-4deb-a682-72afe07284b3"; // fawbud

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // Cek jika database kosong, baru isi data
        if (insurancePlanRepository.count() == 0) {
            System.out.println("--- START SEEDING DATA (WITH TOUR PACKAGE) ---");

            // ==========================================
            // 1. CREATE INSURANCE PLANS
            // ==========================================

            // A. AXA INSURANCE PLANS
            InsurancePlan axaHotelPlan = createPlan(
                    "INS-AXA-001", ID_PROVIDER_AXA, "AXA Hotel Protection", 50000, 10000000,
                    "Cover biaya pembatalan hotel dan kerusakan properti tamu.",
                    List.of(ServiceEnum.ACCOMMODATION), 30
            );

            InsurancePlan axaFlightPlan = createPlan(
                    "INS-AXA-002", ID_PROVIDER_AXA, "AXA Flight Delay Safe", 75000, 5000000,
                    "Kompensasi keterlambatan penerbangan > 4 jam.",
                    List.of(ServiceEnum.FLIGHT), 7
            );

            // B. ASTRA INSURANCE PLANS
            InsurancePlan astraRentalPlan = createPlan(
                    "INS-ASTRA-001", ID_PROVIDER_ASTRA, "Astra Car Rental Safe", 100000, 50000000,
                    "Perlindungan penuh untuk penyewaan kendaraan roda empat.",
                    List.of(ServiceEnum.RENTALS), 7
            );

            // C. AIA INSURANCE PLANS (RESTORED)
            InsurancePlan aiaTourPlan = createPlan(
                    "INS-AIA-001", ID_PROVIDER_AIA, "AIA Tour & Travel Secure", 150000, 25000000,
                    "Asuransi perjalanan wisata domestik dan internasional.",
                    List.of(ServiceEnum.TOUR_PACKAGE), 14
            );

            // D. TESTKHAY PROVIDER PLAN
            InsurancePlan khayPlan = createPlan(
                    "INS-KHAY-001", ID_PROVIDER_TESTKHAY, "Khay Premium Protection", 200000, 100000000,
                    "Proteksi premium untuk segala jenis layanan.",
                    List.of(ServiceEnum.FLIGHT, ServiceEnum.ACCOMMODATION), 365
            );

            System.out.println("--- INSURANCE PLANS CREATED ---");

            // ==========================================
            // 2. CREATE POLICIES (STATUS: PAID)
            // ==========================================
            
            // Policy 1: Khayla beli AXA Hotel (Siap Claim)
            createPaidPolicy(
                    "POL-KHAY-001", ID_USER_KHAYLA, "BOOK-ACC-001", 
                    axaHotelPlan, ServiceEnum.ACCOMMODATION
            );

            // Policy 2: Khayla beli Astra Rental (Siap Claim)
            createPaidPolicy(
                    "POL-KHAY-002", ID_USER_KHAYLA, "BOOK-RENT-002", 
                    astraRentalPlan, ServiceEnum.RENTALS
            );

            // Policy 3: Customer Test beli AIA Tour (RESTORED - Matches Mock ID BOOK-TOUR-003)
            createPaidPolicy(
                    "POL-CUST-001", ID_USER_CUSTOMER_TEST, "BOOK-TOUR-003", 
                    aiaTourPlan, ServiceEnum.TOUR_PACKAGE
            );

            // Policy 4: Fawbud beli AXA Flight
            createPaidPolicy(
                    "POL-FAW-001", ID_USER_FAWBUD, "BOOK-FLIGHT-004", 
                    axaFlightPlan, ServiceEnum.FLIGHT
            );

            System.out.println("--- POLICIES (PAID) CREATED ---");
            System.out.println("--- SEEDING FINISHED SUCCESSFULLY ---");
        }
    }

    /**
     * Helper untuk membuat Insurance Plan agar kodenya rapi.
     */
    private InsurancePlan createPlan(String id, String providerId, String name, Integer price, 
                                     Integer coverage, String details, List<ServiceEnum> services, Integer days) {
        InsurancePlan plan = new InsurancePlan();
        plan.setId(id);
        plan.setProviderId(providerId);
        plan.setPlanName(name);
        plan.setPrice(price);
        plan.setCoverage(coverage);
        plan.setCoverageDetails(details);
        plan.setApplicableService(services);
        plan.setExpiredByDays(days);
        return insurancePlanRepository.save(plan);
    }

    /**
     * Helper untuk membuat Policy & OrderedPlan yang sudah PAID.
     */
    private void createPaidPolicy(String policyId, String userId, String bookingId, 
                                  InsurancePlan plan, ServiceEnum serviceEnum) {
        
        Policy policy = new Policy();
        policy.setId(policyId);
        policy.setUserId(userId);
        policy.setBookingId(bookingId);
        policy.setService(serviceEnum);
        policy.setStartDate(LocalDate.now());
        policy.setStatus("PAID"); // Status PAID agar bisa langsung Claim
        policy.setTotalPrice(plan.getPrice());
        policy.setTotalCoverage(plan.getCoverage());
        policy.setBillId(UUID.randomUUID().toString()); // Dummy Bill ID

        // Buat Ordered Plan yang nyantol ke Policy ini
        OrderedPlan op = new OrderedPlan();
        op.setId(policyId + "-OP"); // ID unik: POL-KHAY-001-OP
        op.setStatus("PAID"); // Status PAID juga
        op.setExpiredDate(LocalDate.now().plusDays(plan.getExpiredByDays()));
        op.setInsurancePlan(plan);
        op.setPolicy(policy);

        // Set relasi list
        List<OrderedPlan> list = new ArrayList<>();
        list.add(op);
        policy.setOrderedPlans(list);

        policyRepository.save(policy);
    }
}