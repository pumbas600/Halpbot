package nz.pumbas.halpbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.steamtables.SteamTable;
import nz.pumbas.steamtables.SteamTableManager;
import nz.pumbas.steamtables.models.Model;
import nz.pumbas.steamtables.models.ModelHelper;
import nz.pumbas.steamtables.models.SaturatedSteamModel;
import nz.pumbas.utilities.Singleton;
import nz.pumbas.utilities.enums.Flags;
import nz.pumbas.utilities.maps.MapHelper;
import nz.pumbas.utilities.maps.Row;

@CommandGroup(defaultPrefix = "$")
public class SteamLookupCommands
{
    @Command(alias = "columns", description = "Lists all the available columns for saturated steam look ups")
    public void onSteamColumn(MessageReceivedEvent event, SteamTable steamTable)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.orange);
        embedBuilder.setTitle("Steam Table Columns");

        Map<String, List<String>> columnAliases = new HashMap<>();
        Singleton.getInstance(SteamTableManager.class).getColumnMappings().forEach(columnModel -> {
            if (Flags.hasFlag(columnModel.tables, steamTable)) {
                if (!columnAliases.containsKey(columnModel.columnname))
                    columnAliases.put(columnModel.columnname, new ArrayList<>());
                columnAliases.get(columnModel.columnalias).add(columnModel.columnalias);
            }
        });
        StringBuilder columns = new StringBuilder();
        columnAliases.forEach((column, aliases) -> {
            aliases.sort(Comparator.comparing(String::length));
            columns.append(String.join(", ", aliases))
                .append(" (")
                .append(MapHelper.<String>getValue(steamTable.getModelType(), column, "units"))
                .append(")\n");
        });

        embedBuilder.addField(String.format("%s Columns", steamTable.getDisplayName()), columns.toString(), false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    private <T extends Model> void steamLookup(MessageReceivedEvent event, String selectColumn, String whereColumn,
                                               double target, Class<T> steamModelType)
    {
        SteamTableManager steamTableManager = Singleton.getInstance(SteamTableManager.class);
        SteamTable steamTable = SteamTable.of(steamModelType);

        Optional<T> oSteamModel = steamTableManager.selectRecord(
            steamModelType,
            selectColumn,
            whereColumn,
            target);

        selectColumn = steamTableManager.selectColumn(selectColumn, steamTable);
        whereColumn = steamTableManager.selectColumn(whereColumn, steamTable);

        if (oSteamModel.isPresent()) {
            T steamModel = oSteamModel.get();

            Row select = MapHelper.getFieldMap(steamModelType, selectColumn);
            Row where = MapHelper.getFieldMap(steamModelType, whereColumn);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.orange);
            embedBuilder.setTitle(String.format("%s Look Up", steamTable.getDisplayName()));
            embedBuilder.addField("Query",
                String.format("Select %s when %s = %s %s", select.getValue("displayName"), where.getValue(
                    "displayName"), target, where.getValue("units")), false);

            String result = String.format("%s %s", steamModel.getDouble(selectColumn), select.getString("units"));
            embedBuilder.addField("Result", result, false);

            if (steamModel.getDouble(whereColumn) != target) {
                embedBuilder.setFooter(String.format("I didn't have any information for when %s = %s %s,\nso I got " +
                        "the next closest thing (%s = %s %s).",
                    where.getValue("displayName"), target, where.getValue("units"),
                    where.getValue("displayName"), steamModel.getDouble(whereColumn), where.getValue("units")));
            }

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        } else throw new ErrorMessageException("That doesn't seem to be a valid query.");
    }

    @Command(alias = "saturated", command = "<steam> WORD,? <where|when> WORD <=|is> DOUBLE")
    public void onSaturated(MessageReceivedEvent event, String selectColumn, String whereColumn, double target)
    {
        this.steamLookup(event, selectColumn, whereColumn, target, SaturatedSteamModel.class);
//        SteamTableManager steamTableManager = Singleton.getInstance(SteamTableManager.class);
//        Optional<SaturatedSteamModel> oSaturatedModel = steamTableManager.selectRecord(SaturatedSteamModel.class,
//            selectColumn, whereColumn, target);
//
//        selectColumn = steamTableManager.selectColumn(selectColumn, SteamTable.SATURATED);
//        whereColumn = steamTableManager.selectColumn(whereColumn, SteamTable.SATURATED);
//
//        if (oSaturatedModel.isPresent()) {
//            SaturatedSteamModel saturatedModel = oSaturatedModel.get();
//
//            Row select = MapHelper.getFieldMap(SaturatedSteamModel.class, selectColumn);
//            Row where = MapHelper.getFieldMap(SaturatedSteamModel.class, whereColumn);
//
//            EmbedBuilder embedBuilder = new EmbedBuilder();
//            embedBuilder.setColor(Color.orange);
//            embedBuilder.setTitle("Saturated Steam Look Up");
//            embedBuilder.addField("Query",
//                String.format("Select %s when %s = %s %s", select.getValue("displayName"), where.getValue(
//                    "displayName"), target, where.getValue("units")), false);
//
//            String result = String.format("%s %s", saturatedModel.getDouble(selectColumn), select.getString("units"));
//            embedBuilder.addField("Result", result, false);
//
//            if (saturatedModel.getDouble(whereColumn) != target) {
//                embedBuilder.setFooter(String.format("I didn't have any information for when %s = %s %s,\nso I got " +
//                        "the next closest thing (%s = %s %s).",
//                    where.getValue("displayName"), target, where.getValue("units"),
//                    where.getValue("displayName"), saturatedModel.getDouble(whereColumn), where.getValue("units")));
//            }
//
//            event.getChannel().sendMessage(embedBuilder.build()).queue();
//        } else throw new ErrorMessageException("That doesn't seem to be a valid query.");
    }
}
