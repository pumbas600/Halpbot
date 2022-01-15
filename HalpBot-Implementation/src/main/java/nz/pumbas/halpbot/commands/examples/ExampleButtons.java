package nz.pumbas.halpbot.commands.examples;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Duration;

@Service
public class ExampleButtons
{
    @Command(description = "Displays two test buttons")
    public void buttons(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Click on one of these buttons!")
                .setActionRow(
                        // When the button is clicked, the @ButtonAction with the matching id is invoked
                        Button.primary("halpbot:example:primary", "Primary button!"),
                        Button.secondary("halpbot:example:secondary", "Secondary button!")
                ).queue();
    }

    @ButtonAction(id = "halpbot:example:primary")
    public String primary(ButtonClickEvent event) { // You can directly pass the event
        return "%s clicked the primary button!".formatted(event.getUser().getName());
    }

    // The display field specifies that the result should only be displayed for 20 seconds before being deleted
    @ButtonAction(id = "halpbot:example:secondary", display = @Duration(20))
    public String secondary(@Source User user) { // Alternatively, you can retrieve fields from the event using @Source
        return "%s clicked the secondary button!".formatted(user.getName());
    }
}
