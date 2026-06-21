package com.github.exabrial.difx.demo;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GreetingMessage {

	public String text() {
		return "DiFx + CDI is alive";
	}
}
