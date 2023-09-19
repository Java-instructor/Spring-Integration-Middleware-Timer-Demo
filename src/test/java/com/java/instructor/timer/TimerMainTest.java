package com.java.instructor.timer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TimerMainTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimerMainTest.class);
	
	@Autowired
	private ApplicationContext applicationContext;

	@Test(timeout = 6000) // timeout may differ number of channels flows the messages.
	public void checkJobCreatedFileRecived() throws Exception {
		PollableChannel testLogHandlerChannel = applicationContext.getBean("testLogHandlerChannel",	PollableChannel.class);
		Message<?> msg = testLogHandlerChannel.receive();
		LOGGER.info(" Junit test verification for payload ::" + msg.getPayload());
		assertThat(msg.getPayload(), is(notNullValue()));

	}

}
