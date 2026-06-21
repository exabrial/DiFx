package com.github.exabrial.difx.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import com.github.exabrial.difx.bootstrap.cdi.qualifier.StartupStage;
import com.github.exabrial.difx.fxml.DiFxViewLoader;
import com.github.exabrial.difx.fxml.FxControllerAndView;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Observes the startup stage, loads the Ping view through the DiFx view loader (whose controller is resolved from the container), and
 * shows it. This is the entry presenter; it ties phases 1 through 4 together.
 */
@ApplicationScoped
public class DemoShellPresenter {
	@Inject
	private DiFxViewLoader viewLoader;

	public void onStartup(@Observes @StartupStage final Stage primaryStage) {
		final FxControllerAndView<PingController> pingView = viewLoader.load(PingController.class);
		primaryStage.setScene(new Scene(pingView.view(), 480, 200));
		primaryStage.setTitle("DiFx -- phases 1-4");
		primaryStage.show();
	}
}
