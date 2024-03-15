package fr.thomas.quizzgameserver.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jasypt.util.text.BasicTextEncryptor;

public class Config {
	
	// Specifications
	private Properties theConfig;
	private BasicTextEncryptor theEncryptor;
	
	// Implementations
	public Config() {
		
		this.theEncryptor = new BasicTextEncryptor();
		this.theEncryptor.setPassword("P@ssw0rd");
		
		this.theConfig = new Properties();
		
		try (FileInputStream input = new FileInputStream("./data/sbcg.cfg")){
			theConfig.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String readAParam(String aKey) {
		return theConfig.getProperty(aKey);
	}
	
	public String decryptAParam(String aKey) {
		return theEncryptor.decrypt(theConfig.getProperty(aKey));
	}
}