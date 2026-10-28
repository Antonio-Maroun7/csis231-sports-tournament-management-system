package com.antonio.sportstournamentclient.team;
import javafx.fxml.FXML; import javafx.scene.control.Label;
/** Controller for team-details.fxml. */
public final class TeamDetailsController { @FXML private Label title; public void setTeam(long id){TeamApi.find(id).thenAccept(t->title.setText(t.teamName()));} }

