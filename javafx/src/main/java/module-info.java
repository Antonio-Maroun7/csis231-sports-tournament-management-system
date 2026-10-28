/**
 * Module descriptor of the JavaFX client.
 *
 * Feature-based packages: every package that holds an FXML controller is opened to
 * javafx.fxml (so FXMLLoader can inject @FXML fields), and every dto package is opened
 * to Jackson (so it can build the request/response records from JSON).
 */
module com.antonio.sportstournamentclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // FXML controllers
    opens com.antonio.sportstournamentclient to javafx.fxml;
    opens com.antonio.sportstournamentclient.core.layout to javafx.fxml;
    opens com.antonio.sportstournamentclient.auth to javafx.fxml;
    opens com.antonio.sportstournamentclient.dashboard to javafx.fxml;
    opens com.antonio.sportstournamentclient.team to javafx.fxml;
    opens com.antonio.sportstournamentclient.user to javafx.fxml;

    // request / response records read and written by Jackson
    opens com.antonio.sportstournamentclient.auth.dto to com.fasterxml.jackson.databind;
    opens com.antonio.sportstournamentclient.team.dto to com.fasterxml.jackson.databind;
    opens com.antonio.sportstournamentclient.user.dto to com.fasterxml.jackson.databind;

    exports com.antonio.sportstournamentclient;
}
