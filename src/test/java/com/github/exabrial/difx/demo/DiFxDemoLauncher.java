package com.github.exabrial.difx.demo;

import com.github.exabrial.difx.bootstrap.DiFxLauncher;

import javafx.application.Application;

public class DiFxDemoLauncher extends DiFxLauncher {

	@Override
	protected Class<? extends Application> getApplicationClass() {
		return DemoApplication.class;
	}

	public static void main(final String[] arguments) {
		new DiFxDemoLauncher().launch(arguments);
	}
}
