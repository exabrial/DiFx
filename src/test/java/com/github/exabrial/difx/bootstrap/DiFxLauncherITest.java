package com.github.exabrial.difx.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.exabrial.difx.demo.Ping;
import com.github.exabrial.difx.demo.PingViewModel;
import com.github.exabrial.difx.fxml.DiFxViewLoader;
import com.github.exabrial.difx.ui.UiExecutor;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import javafx.application.Application;

class DiFxLauncherITest {

	private DiFxLauncher newLauncher() {
		return new DiFxLauncher() {
			@Override
			protected Class<? extends Application> applicationClass() {
				return DiFxApplication.class;
			}
		};
	}

	@Test
	void bootstrapWiresCdiAndFxmlInfrastructure() {
		try (SeContainer container = newLauncher().bootstrap()) {
			final PingViewModel pingViewModel = container.select(PingViewModel.class).get();
			assertEquals("click Ping", pingViewModel.statusProperty().get());
			assertNotNull(container.select(DiFxViewLoader.class).get());
		}
	}

	@Test
	void synchronousObserverWritesViewModelOnFiringThread() {
		try (SeContainer container = newLauncher().bootstrap()) {
			final PingViewModel pingViewModel = container.select(PingViewModel.class).get();
			container.getBeanManager().getEvent().select(Ping.class).fire(new Ping("test"));
			assertTrue(pingViewModel.statusProperty().get().startsWith("@Observes on"));
		}
	}

	@Test
	void extensionRegistersDiFxBeansWithDiscoveryDisabled() {
		final SeContainerInitializer initializer = SeContainerInitializer.newInstance().disableDiscovery();
		try (SeContainer container = initializer.initialize()) {
			assertNotNull(container.select(UiExecutor.class).get());
			assertNotNull(container.select(DiFxViewLoader.class).get());
		}
	}
}
