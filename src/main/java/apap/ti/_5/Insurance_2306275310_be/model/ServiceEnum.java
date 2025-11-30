package apap.ti._5.Insurance_2306275310_be.model;

/**
 * Enumerasi yang mendefinisikan jenis-jenis layanan (Service)
 * yang dapat dicakup oleh produk asuransi dalam sistem.
 * Digunakan untuk mengkategorikan InsurancePlan dan Policy.
 */
public enum ServiceEnum {

    /**
     * Layanan akomodasi, seperti hotel, villa, atau apartemen.
     */
    ACCOMMODATION,

    /**
     * Layanan transportasi udara atau penerbangan.
     */
    FLIGHT,

    /**
     * Layanan paket wisata atau tur perjalanan.
     */
    TOUR_PACKAGE,

    /**
     * Layanan penyewaan, seperti sewa mobil atau kendaraan lainnya.
     */
    RENTALS
}