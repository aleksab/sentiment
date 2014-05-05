package no.hioa.sentiment.filmweb;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello Mongo!
 */
public class App
{
	public static void main(String[] args)
	{
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/bootstrap.xml");
		HelloMongo hello = context.getBean(HelloMongo.class);
		hello.run();
	}
}
