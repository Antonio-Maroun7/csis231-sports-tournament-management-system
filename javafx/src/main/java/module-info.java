module com.antonio.sportstournamentclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.antonio.sportstournamentclient to javafx.fxml;
    opens com.antonio.sportstournamentclient.core to javafx.fxml;
    opens com.antonio.sportstournamentclient.core.layout to javafx.fxml;
    opens com.antonio.sportstournamentclient.auth to javafx.fxml;
    opens com.antonio.sportstournamentclient.dashboard to javafx.fxml;
    opens com.antonio.sportstournamentclient.team to javafx.fxml;
    opens com.antonio.sportstournamentclient.user to javafx.fxml;
    opens com.antonio.sportstournamentclient.team.dto to com.fasterxml.jackson.databind;
    opens com.antonio.sportstournamentclient.user.dto to com.fasterxml.jackson.databind;
    opens com.antonio.sportstournamentclient.auth.dto to com.fasterxml.jackson.databind;
    exports com.antonio.sportstournamentclient;
    exports com.antonio.sportstournamentclient.core;
    exports com.antonio.sportstournamentclient.auth;
    exports com.antonio.sportstournamentclient.dashboard;
    exports com.antonio.sportstournamentclient.team;
    exports com.antonio.sportstournamentclient.user;
}

