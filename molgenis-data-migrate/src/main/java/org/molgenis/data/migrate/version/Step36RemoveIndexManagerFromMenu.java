package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import javax.sql.DataSource;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step36RemoveIndexManagerFromMenu extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step33UpdateForeignKeyDeferred.class);
  public static final String ITEMS = "items";

  private final DataSource dataSource;

  private static JsonObject removeFromMenu(JsonObject jsonObject) {
    JsonArray array = jsonObject.getAsJsonArray(ITEMS);
    Iterator<JsonElement> iterator = array.iterator();
    JsonArray newMenu = new JsonArray();
    while (iterator.hasNext()) {
      JsonElement element = iterator.next();
      JsonObject object = element.getAsJsonObject();
      if (!jsonElementHasValue(object, "type", "plugin")) {
        newMenu.add(removeFromMenu(object));
      } else {
        if (!jsonElementHasValue(object, "id", "indexmanager")) {
          newMenu.add(element);
        }
      }
    }
    jsonObject.remove(ITEMS);
    jsonObject.add(ITEMS, newMenu);
    return jsonObject;
  }

  private static boolean jsonElementHasValue(JsonObject jsonObject, String field, String value) {
    JsonElement element = jsonObject.get(field);
    return element != null ? element.getAsString().equals(value) : false;
  }

  public Step36RemoveIndexManagerFromMenu(DataSource dataSource) {
    super(35, 36);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Starting to remove 'indexmanager' from menu.");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    String menuString =
        jdbcTemplate.queryForObject(
            "SELECT molgenis_menu FROM public.\"sys_set_app#4f91996f\"", String.class);

    JsonParser parser = new JsonParser();
    JsonObject menuObject = (JsonObject) parser.parse(menuString);
    JsonObject newMenu = removeFromMenu(menuObject);
    jdbcTemplate.update(
        "UPDATE public.\"sys_set_app#4f91996f\" SET \"molgenis_menu\" = ?", menuString);
    LOG.debug("Finished to remove 'indexmanager' from menu.");
  }
}
