package com.antonio.sportstournamentclient.core;
import javafx.fxml.FXMLLoader; import javafx.scene.Node; import javafx.scene.Scene; import javafx.stage.Stage;
/** Centralized FXML navigation helper. */
public final class Navigation {
 private Navigation(){}
 public static void show(Node source,String resource) { try { Stage s=(Stage)source.getScene().getWindow(); s.setScene(new Scene(FXMLLoader.load(Navigation.class.getResource(resource)),1000,700)); } catch(Exception e){throw new IllegalStateException("Cannot load "+resource,e);} }
}

