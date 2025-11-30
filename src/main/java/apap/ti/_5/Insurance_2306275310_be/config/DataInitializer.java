package apap.ti._5.Insurance_2306275310_be.config;

import apap.ti._5.Insurance_2306275310_be.model.*;
import apap.ti._5.Insurance_2306275310_be.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final InsurancePlanRepository insurancePlanRepository;
    private final PolicyRepository policyRepository;
    private final OrderedPlanRepository orderedPlanRepository;

    private final String ID_PROVIDER_AXA = "22f94d65-29d3-415b-9482-38210039e2cb";
    private final String ID_USER_BUDI = "c06102e8-9316-4ce7-b5cc-cecb96dee8ad";

    public DataInitializer(InsurancePlanRepository insurancePlanRepository,
                           PolicyRepository policyRepository,
                           OrderedPlanRepository orderedPlanRepository) {
        this.insurancePlanRepository = insurancePlanRepository;
        this.policyRepository = policyRepository;
        this.orderedPlanRepository = orderedPlanRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (insurancePlanRepository.count() == 0) {
            System.out.println("--- SEEDING DATA INSURANCE (BYPASS MODE) ---");

            // 1. BUAT PLAN AXA (INS1)
            InsurancePlan plan = new InsurancePlan();
            plan.setId("INS1");
            plan.setProviderId(ID_PROVIDER_AXA);
            plan.setPlanName("AXA Hotel Protection");
            plan.setPrice(50000);
            plan.setCoverage(10000000);
            plan.setCoverageDetails("Cover cancel hotel.");
            plan.setApplicableService(List.of(ServiceEnum.ACCOMMODATION));
            plan.setExpiredByDays(30);
            insurancePlanRepository.save(plan);

            // 2. BUAT POLICY DUMMY (BUDI - SUDAH LUNAS/PAID)
            // Biar bisa langsung tes Klaim tanpa ribet bayar
            createPaidPolicyForBudi(plan);
            
            System.out.println("--- SEEDING SELESAI ---");
        }
    }

    private void createPaidPolicyForBudi(InsurancePlan plan) {
        Policy policy = new Policy();
        policy.setId("POL-DUMMY");
        policy.setUserId(ID_USER_BUDI);
        policy.setBookingId("BOOK-ACC-DUMMY"); // ID Booking Dummy
        policy.setService(ServiceEnum.ACCOMMODATION);
        policy.setStartDate(LocalDate.now());
        policy.setStatus("PAID"); // Status PAID
        policy.setTotalPrice(plan.getPrice());
        policy.setTotalCoverage(plan.getCoverage());

        OrderedPlan op = new OrderedPlan();
        op.setId("POL-DUMMY-OP1"); 
        op.setStatus("PAID"); 
        op.setExpiredDate(LocalDate.now().plusDays(plan.getExpiredByDays()));
        op.setInsurancePlan(plan);
        op.setPolicy(policy);
        
        List<OrderedPlan> list = new ArrayList<>();
        list.add(op);
        policy.setOrderedPlans(list);

        policyRepository.save(policy);
    }
}