package apap.ti._5.Insurance_2306275310_be.repository;

/**
 * Interface Projection (DTO) untuk menampung hasil query Native SQL statistik bulanan.
 */
public interface MonthlyOrderCount {
    Integer getMonth();
    Long getCount();
}