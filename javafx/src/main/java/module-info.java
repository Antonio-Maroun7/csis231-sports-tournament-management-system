module com.antonio.sportstournamentclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.antonio.sportstournamentclient to javafx.fxml;
    exports com.antonio.sportstournamentclient;
}