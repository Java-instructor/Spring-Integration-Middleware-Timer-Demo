package com.java.instructor.timer.channel;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.scheduling.support.PeriodicTrigger;

@Configuration
public class FilePollerChannelConfig {

	private final Logger log = LoggerFactory.getLogger(FilePollerChannelConfig.class);

	public static String INPUT_DIR;
	public static String OUTPUT_DIR;
	public static String FILE_PATTERN = "*.xml";

	static {
		String currentPath = Paths.get("").toAbsolutePath().toString();
		INPUT_DIR = currentPath + File.separator + "input";
		OUTPUT_DIR = currentPath + File.separator + "output";
	}

	@Bean
	public MessageChannel filePollerChannel() {
		return MessageChannels.direct().get();
	}

	@Bean
	public PollableChannel testLogHandlerChannel() {
		return new QueueChannel(5);
	}
	
	@Bean
	public MessageChannel writeFileChannel() {
		return MessageChannels.direct().get();
	}
	
	
	@Bean
	@InboundChannelAdapter(value = "filePollerChannel", poller = @Poller(fixedDelay = "5000"))
	public MessageSource<File> filePoller() {

		FileReadingMessageSource sourceReader = new FileReadingMessageSource();
		sourceReader.setDirectory(new File(INPUT_DIR));
		sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
		log.info("reading the file. ....");
		return sourceReader;
	}
	@Bean
	@Transformer(inputChannel = "filePollerChannel", outputChannel = "testLogHandlerChannel")
	public FileToStringTransformer readFileToString() {
		return new FileToStringTransformer();
	}

	@Transformer(inputChannel = "testLogHandlerChannel", outputChannel="writeFileChannel")
	public Message<?> writeFileChannelTransformer(final Message inputMessage) {

		final String fileName= (String) inputMessage.getHeaders().get("file_name");			
	    final String filenameWithoutExt= FilenameUtils.getBaseName(fileName);
	    final String fileExtension = FilenameUtils.getExtension(fileName);
	    final String outputFileName=filenameWithoutExt +"Updated."+fileExtension;
	    log.info("outputFileName-->"+outputFileName +" payload:"+inputMessage.getPayload());
	    final Message<?> message = MessageBuilder.withPayload(inputMessage.getPayload())
				.copyHeaders(inputMessage.getHeaders())
				.setHeader("file_name",outputFileName).build();

		return message;
	}
	
	@Bean
	@ServiceActivator(inputChannel = "writeFileChannel")
	public MessageHandler fileWritingMessageHandler() {		
		FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
		handler.setFileExistsMode(FileExistsMode.REPLACE);
		handler.setDeleteSourceFiles(true);
		handler.setExpectReply(false);
		return handler;
	}
	
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata defaultPoller() {
	    PollerMetadata pollerMetadata = new PollerMetadata();
	    pollerMetadata.setTrigger(new PeriodicTrigger(10));
	    return pollerMetadata;
	}
}
