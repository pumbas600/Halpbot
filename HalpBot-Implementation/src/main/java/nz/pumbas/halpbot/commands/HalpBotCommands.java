package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.Permission;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Implicit;
import nz.pumbas.halpbot.converters.Converters;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.units.Prefix;
import nz.pumbas.halpbot.customparameters.units.Unit;
import nz.pumbas.halpbot.objects.Exceptional;

public class HalpBotCommands
{

    @Command(alias = "source")
    public String source() {
        return "You can see the source code for me here: https://github.com/pumbas600/HalpBot :kissing_heart:";
    }

    @Command(alias = "suggestion")
    public String suggestion() {
        return "You can note issues and suggestions for me here: https://github.com/pumbas600/HalpBot/issues";
    }

    @Command(alias = "choose", description = "Randomly chooses one of the items")
    public String choose(@Implicit String[] choices) {
        // Use of @Implicit means that its not necessary to surround the choices with [...]
        return choices[(int)(Math.random() * choices.length)];
    }

    @Command(alias = "permission",
             description = "Tests command permissions (If you have permission to view audit logs)",
             permissions = Permission.VIEW_AUDIT_LOGS)
    public String permission() {
        return "You have permission to use this command!";
    }

    @Command(alias = "centroid", description = "Finds the centroid defined by the specified shapes")
    public String centroid(@Implicit Shape[] shapes)
    {
        double sumAx = 0;
        double sumAy = 0;
        double totalA = 0;

        for (Shape shape : shapes) {
            sumAx += shape.getArea() * shape.getxPos();
            sumAy += shape.getArea() * shape.getyPos();
            totalA += shape.getArea();
        }

        return String.format("x: %.2f, y: %.2f", sumAx / totalA, sumAy / totalA);
    }

    public static final TypeConverter<Unit> UNIT_CONVERTER = TypeConverter.builder(Unit.class)
        .convert(
            ctx -> Converters.DOUBLE_CONVERTER.getMapper()
                .apply(ctx)
                .map(value -> {
                    Exceptional<String> eUnit = Converters.STRING_CONVERTER.getMapper().apply(ctx);
                    if (eUnit.caught()) eUnit.rethrow();
                    String unit = eUnit.get();
                    if (1 < unit.length() && Prefix.isPrefix(unit.charAt(0)))
                        return new Unit(value, Prefix.getPrefix(unit.charAt(0)), unit.substring(1));
                    else
                        return new Unit(value, Prefix.DEFAULT, unit);
                }))
        .register();

    public static final TypeConverter<Prefix> PREFIX_CONVERTER = TypeConverter.builder(Prefix.class)
        .convert(ctx ->
            Converters.ENUM_CONVERTER.getMapper().apply(ctx)
                .map(prefix -> (Prefix) prefix)
                .orExceptional(() ->
                    Converters.CHARACTER_CONVERTER.getMapper().apply(ctx)
                        .map(prefix -> {
                            if (Prefix.isPrefix(prefix))
                                return Prefix.getPrefix(prefix);
                            throw new IllegalArgumentException("That is not a valid prefix");
                        })
                ))
        .register();

    @Command(alias = "convert", description = "Converts the number to the specified prefix")
    public Unit convert(Unit unit, Prefix toPrefix) {
        return unit.to(toPrefix);
    }
}
