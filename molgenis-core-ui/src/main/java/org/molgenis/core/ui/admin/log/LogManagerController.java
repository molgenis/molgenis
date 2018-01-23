package org.molgenis.core.ui.admin.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.web.PluginController;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.molgenis.core.ui.admin.log.LogManagerController.URI;

@Controller
@RequestMapping(URI)
public class LogManagerController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(LogManagerController.class);

	public static final String ID = "logmanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private static final List<Level> LOG_LEVELS;

	static
	{
		LOG_LEVELS = Arrays.asList(Level.ALL, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.OFF);
	}

	public LogManagerController()
	{
		super(URI);
	}

	@GetMapping
	public String init(Model model)
	{
		ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
		if (!(iLoggerFactory instanceof LoggerContext))
		{
			throw new RuntimeException("Logger factory is not a Logback logger context");
		}
		LoggerContext loggerContext = (LoggerContext) iLoggerFactory;

		List<Logger> loggers = new ArrayList<>();
		for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList())
		{
			if (logger.getLevel() != null || logger.iteratorForAppenders().hasNext())
			{
				loggers.add(logger);
			}
		}

		model.addAttribute("loggers", loggers);
		model.addAttribute("levels", LOG_LEVELS);
		model.addAttribute("hasWritePermission", SecurityUtils.currentUserIsSu());
		return "view-logmanager";
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@PostMapping("/logger/{loggerName}/{loggerLevel}")
	@ResponseStatus(HttpStatus.OK)
	public void updateLogLevel(@PathVariable(value = "loggerName") String loggerName,
			@PathVariable(value = "loggerLevel") String loggerLevelStr)
	{
		// SLF4j logging facade does not support runtime change of log level, cast to Logback logger
		org.slf4j.Logger logger = LoggerFactory.getLogger(loggerName);
		if (!(logger instanceof ch.qos.logback.classic.Logger))
		{
			throw new RuntimeException("Root logger is not a Logback logger");
		}
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;

		// update log level
		Level loggerLevel;
		try
		{
			loggerLevel = Level.valueOf(loggerLevelStr);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException("Invalid log level [" + loggerLevelStr + "]");
		}
		logbackLogger.setLevel(loggerLevel);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@PostMapping("/loggers/reset")
	@ResponseStatus(HttpStatus.OK)
	public void resetLoggers()
	{
		ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
		if (!(iLoggerFactory instanceof LoggerContext))
		{
			throw new RuntimeException("Logger factory is not a Logback logger context");
		}
		LoggerContext loggerContext = (LoggerContext) iLoggerFactory;
		ContextInitializer ci = new ContextInitializer(loggerContext);
		URL url = ci.findURLOfDefaultConfigurationFile(true);
		loggerContext.reset();
		try
		{
			ci.configureByResource(url);
		}
		catch (JoranException e)
		{
			LOG.error("Error reloading log configuration", e);
			throw new RuntimeException(e);
		}
	}
}
