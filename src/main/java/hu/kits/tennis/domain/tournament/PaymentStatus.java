package hu.kits.tennis.domain.tournament;

public enum PaymentStatus {

    NOT_PAID("Nincs fizetve"),
    PAID("Utalva"),
    PAID_CASH("KP-ban fizetve"),
    INVOICE_SENT("Számlázva");
    
    private PaymentStatus(String label) {
        this.label = label;
    }

    public final String label;
    
}
