/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.common;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import org.dockbox.hartshorn.core.Modifiers;
import org.dockbox.hartshorn.core.boot.HartshornApplicationFactory;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.management.ManagementFactory;
import java.util.function.Function;

import javax.security.auth.login.LoginException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.utilities.ErrorManager;

public class HalpbotBuilder
{
    private final ApplicationContext applicationContext;

    public HalpbotBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static HalpbotBuilder create(Class<?> main,
                                        String[] args,
                                        Modifiers... modifiers) {
        // Taken from Hartshorn just to enable the usage of HalpbotApplicationFactory
        for (final Modifiers modifier : modifiers)
            if (modifier == Modifiers.DEBUG) setDebugActive();

        MDC.put("process_id", ManagementFactory.getRuntimeMXBean().getName());

        ApplicationContext applicationContext = new HalpbotApplicationFactory()
            .loadDefaults()
            .activator(TypeContext.of(main))
            .arguments(args)
            .modifiers(modifiers)
            .create();
        applicationContext.get(ErrorManager.class); // Create an instance of the ErrorManager

        return new HalpbotBuilder(applicationContext);
    }

    private static void setDebugActive() {
        final ILoggerFactory factory = LoggerFactory.getILoggerFactory();

        if (factory instanceof LoggerContext loggerContext) {
            for (final Logger logger : loggerContext.getLoggerList()) {
                logger.setLevel(Level.DEBUG);
            }
        } else {
            final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
        }
    }

    public void build(Function<String, JDABuilder> jdaBuilder) {
        HalpbotCore halpbotCore = this.applicationContext.get(HalpbotCore.class);
        BotConfiguration botConfiguration = this.applicationContext.get(BotConfiguration.class);

        try {
            JDABuilder builder = jdaBuilder.apply(botConfiguration.token());
            JDA jda = builder.build();
            halpbotCore.initialise(jda);
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            this.applicationContext.log().error("There was an error while building the JDA instance", e);
        }
    }
}
